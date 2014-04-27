package com.xjt.letool.adapters;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Message;

import com.xjt.letool.R;
import com.xjt.letool.activities.BaseActivity;
import com.xjt.letool.common.Future;
import com.xjt.letool.common.FutureListener;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.SynchronizedHandler;
import com.xjt.letool.common.ThreadPool;
import com.xjt.letool.data.DataSourceType;
import com.xjt.letool.data.MediaItem;
import com.xjt.letool.data.MediaObject;
import com.xjt.letool.data.MediaPath;
import com.xjt.letool.data.MediaSet;
import com.xjt.letool.data.cache2.ThumbCacheLoader;
import com.xjt.letool.data.loader.ThumbnailSetDataLoader;
import com.xjt.letool.data.utils.BitmapLoader;
import com.xjt.letool.utils.Utils;
import com.xjt.letool.views.fragment.LetoolFragment;
import com.xjt.letool.views.opengl.BitmapTexture;
import com.xjt.letool.views.opengl.Texture;
import com.xjt.letool.views.opengl.TextureUploader;
import com.xjt.letool.views.opengl.TiledTexture;
import com.xjt.letool.views.render.ThumbnailSetRenderer;
import com.xjt.letool.views.utils.AlbumLabelMaker;

public class ThumbnailSetDataWindow implements ThumbnailSetDataLoader.DataListener {
    private static final String TAG = "ThumbnailSetDataWindow";
    private static final int MSG_UPDATE_ALBUM_ENTRY = 1;

    public static interface Listener {
        public void onSizeChanged(int size);

        public void onContentChanged();
    }

    private final ThumbnailSetDataLoader mSource;
    private int mSize;

    private int mContentStart = 0;
    private int mContentEnd = 0;

    private int mActiveStart = 0;
    private int mActiveEnd = 0;

    private Listener mListener;

    private final AlbumSetEntry mData[];
    private final SynchronizedHandler mHandler;
    private final ThreadPool mThreadPool;
    private final AlbumLabelMaker mLabelMaker;
    private final String mLoadingText;

    private final TiledTexture.Uploader mContentUploader;
    private final TextureUploader mLabelUploader;

    private int mActiveRequestCount = 0;
    private boolean mIsActive = false;
    private BitmapTexture mLoadingLabel;

    private int mSlotWidth;

    public static class AlbumSetEntry {
        public MediaSet album;
        public MediaItem coverItem;
        public Texture content;
        public BitmapTexture labelTexture;
        public TiledTexture bitmapTexture;
        public MediaPath setPath;
        public String title;
        public int totalCount;
        public int sourceType;
        public int cacheFlag;
        public int cacheStatus;
        public int rotation;
        public boolean isWaitLoadingDisplayed;
        public long setDataVersion;
        public long coverDataVersion;
        private BitmapLoader labelLoader;
        private BitmapLoader coverLoader;
    }

    public ThumbnailSetDataWindow(LetoolFragment fragment, ThumbnailSetDataLoader source, ThumbnailSetRenderer.LabelSpec labelSpec, int cacheSize) {
        source.setModelListener(this);
        mSource = source;
        mData = new AlbumSetEntry[cacheSize];
        mSize = source.size();
        mThreadPool = fragment.getThreadPool();

        mLabelMaker = new AlbumLabelMaker(fragment.getAndroidContext(), labelSpec);
        mLoadingText = fragment.getAndroidContext().getString(R.string.loading);
        mContentUploader = new TiledTexture.Uploader(fragment.getGLController());
        mLabelUploader = new TextureUploader(fragment.getGLController());

        mHandler = new SynchronizedHandler(fragment.getGLController()) {
            @Override
            public void handleMessage(Message message) {
                Utils.assertTrue(message.what == MSG_UPDATE_ALBUM_ENTRY);
                ((EntryUpdater) message.obj).updateEntry();
            }
        };
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public AlbumSetEntry get(int thumbnailIndex) {
        if (!isActiveSlot(thumbnailIndex)) {
            Utils.fail("invalid thumbnail: %s outsides (%s, %s)", thumbnailIndex, mActiveStart, mActiveEnd);
        }
        return mData[thumbnailIndex % mData.length];
    }

    public int size() {
        return mSize;
    }

    public boolean isActiveSlot(int thumbnailIndex) {
        return thumbnailIndex >= mActiveStart && thumbnailIndex < mActiveEnd;
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart == mContentStart && contentEnd == mContentEnd)
            return;

        if (contentStart >= mContentEnd || mContentStart >= contentEnd) {
            for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
                freeSlotContent(i);
            }
            mSource.setActiveWindow(contentStart, contentEnd);
            for (int i = contentStart; i < contentEnd; ++i) {
                prepareSlotContent(i);
            }
        } else {
            for (int i = mContentStart; i < contentStart; ++i) {
                freeSlotContent(i);
            }
            for (int i = contentEnd, n = mContentEnd; i < n; ++i) {
                freeSlotContent(i);
            }
            mSource.setActiveWindow(contentStart, contentEnd);
            for (int i = contentStart, n = mContentStart; i < n; ++i) {
                prepareSlotContent(i);
            }
            for (int i = mContentEnd; i < contentEnd; ++i) {
                prepareSlotContent(i);
            }
        }

        mContentStart = contentStart;
        mContentEnd = contentEnd;
    }

    public void setActiveWindow(int start, int end) {
        //LLog.i(TAG, "tart:" + start + " end :" + end);
        if (!(start <= end && end - start <= mData.length && end <= mSize)) {
            Utils.fail("start = %s, end = %s, length = %s, size = %s", start, end, mData.length, mSize);
        }

        AlbumSetEntry data[] = mData;
        mActiveStart = start;
        mActiveEnd = end;
        int contentStart = Utils.clamp((start + end) / 2 - data.length / 2, 0, Math.max(0, mSize - data.length));
        int contentEnd = Math.min(contentStart + data.length, mSize);
        setContentWindow(contentStart, contentEnd);

        if (mIsActive) {
            updateTextureUploadQueue();
            updateAllImageRequests();
        }
    }

    // We would like to request non active thumbnails in the following order:
    // Order:    8 6 4 2                   1 3 5 7
    //         |---------|---------------|---------|
    //                   |<-  active  ->|
    //         |<-------- cached range ----------->|
    private void requestNonactiveImages() {
        int range = Math.max(mContentEnd - mActiveEnd, mActiveStart - mContentStart);
        for (int i = 0; i < range; ++i) {
            requestImagesInSlot(mActiveEnd + i);
            requestImagesInSlot(mActiveStart - 1 - i);
        }
    }

    private void cancelNonactiveImages() {
        int range = Math.max(mContentEnd - mActiveEnd, mActiveStart - mContentStart);
        for (int i = 0; i < range; ++i) {
            cancelImagesInSlot(mActiveEnd + i);
            cancelImagesInSlot(mActiveStart - 1 - i);
        }
    }

    private void requestImagesInSlot(int thumbnailIndex) {
        if (thumbnailIndex < mContentStart || thumbnailIndex >= mContentEnd)
            return;
        AlbumSetEntry entry = mData[thumbnailIndex % mData.length];
        if (entry.coverLoader != null)
            entry.coverLoader.startLoad();
        if (entry.labelLoader != null)
            entry.labelLoader.startLoad();
    }

    private void cancelImagesInSlot(int thumbnailIndex) {
        if (thumbnailIndex < mContentStart || thumbnailIndex >= mContentEnd)
            return;
        AlbumSetEntry entry = mData[thumbnailIndex % mData.length];
        if (entry.coverLoader != null)
            entry.coverLoader.cancelLoad();
        if (entry.labelLoader != null)
            entry.labelLoader.cancelLoad();
    }

    private static long getDataVersion(MediaObject object) {
        return object == null
                ? MediaSet.INVALID_DATA_VERSION
                : object.getDataVersion();
    }

    private void freeSlotContent(int thumbnailIndex) {
        AlbumSetEntry entry = mData[thumbnailIndex % mData.length];
        if (entry.coverLoader != null)
            entry.coverLoader.recycle();
        if (entry.labelLoader != null)
            entry.labelLoader.recycle();
        if (entry.labelTexture != null)
            entry.labelTexture.recycle();
        if (entry.bitmapTexture != null)
            entry.bitmapTexture.recycle();
        mData[thumbnailIndex % mData.length] = null;
    }

    private boolean isLabelChanged(
            AlbumSetEntry entry, String title, int totalCount, int sourceType) {
        return !Utils.equals(entry.title, title)
                || entry.totalCount != totalCount
                || entry.sourceType != sourceType;
    }

    private void updateAlbumSetEntry(AlbumSetEntry entry, int thumbnailIndex) {
        MediaSet album = mSource.getMediaSet(thumbnailIndex);
        MediaItem cover = mSource.getCoverItem(thumbnailIndex);
        int totalCount = mSource.getTotalCount(thumbnailIndex);

        entry.album = album;
        entry.setDataVersion = getDataVersion(album);
        entry.cacheFlag = identifyCacheFlag(album);
        entry.cacheStatus = identifyCacheStatus(album);
        entry.setPath = (album == null) ? null : album.getPath();

        String title = (album == null) ? "" : Utils.ensureNotNull(album.getName());
        int sourceType = DataSourceType.identifySourceType(album);
        if (isLabelChanged(entry, title, totalCount, sourceType)) {
            entry.title = title;
            entry.totalCount = totalCount;
            entry.sourceType = sourceType;
            if (entry.labelLoader != null) {
                entry.labelLoader.recycle();
                entry.labelLoader = null;
                entry.labelTexture = null;
            }
            if (album != null) {
                entry.labelLoader = new AlbumLabelLoader(thumbnailIndex, title, totalCount, sourceType);
            }
        }

        entry.coverItem = cover;
        if (getDataVersion(cover) != entry.coverDataVersion) {
            entry.coverDataVersion = getDataVersion(cover);
            entry.rotation = (cover == null) ? 0 : cover.getRotation();
            if (entry.coverLoader != null) {
                entry.coverLoader.recycle();
                entry.coverLoader = null;
                entry.bitmapTexture = null;
                entry.content = null;
            }
            if (cover != null) {
                entry.coverLoader = new AlbumCoverLoader(thumbnailIndex, cover);
            }
        }
    }

    private void prepareSlotContent(int thumbnailIndex) {
        AlbumSetEntry entry = new AlbumSetEntry();
        updateAlbumSetEntry(entry, thumbnailIndex);
        mData[thumbnailIndex % mData.length] = entry;
    }

    private static boolean startLoadBitmap(BitmapLoader loader) {
        if (loader == null)
            return false;
        loader.startLoad();
        return loader.isRequestInProgress();
    }

    private void uploadBackgroundTextureInSlot(int index) {
        if (index < mContentStart || index >= mContentEnd)
            return;
        AlbumSetEntry entry = mData[index % mData.length];
        if (entry.bitmapTexture != null) {
            mContentUploader.addTexture(entry.bitmapTexture);
        }
        if (entry.labelTexture != null) {
            mLabelUploader.addBgTexture(entry.labelTexture);
        }
    }

    private void updateTextureUploadQueue() {
        if (!mIsActive)
            return;
        mContentUploader.clear();
        mLabelUploader.clear();

        // Upload foreground texture
        for (int i = mActiveStart, n = mActiveEnd; i < n; ++i) {
            AlbumSetEntry entry = mData[i % mData.length];
            if (entry.bitmapTexture != null) {
                mContentUploader.addTexture(entry.bitmapTexture);
            }
            if (entry.labelTexture != null) {
                mLabelUploader.addFgTexture(entry.labelTexture);
            }
        }

        // add background textures
        int range = Math.max((mContentEnd - mActiveEnd), (mActiveStart - mContentStart));
        for (int i = 0; i < range; ++i) {
            uploadBackgroundTextureInSlot(mActiveEnd + i);
            uploadBackgroundTextureInSlot(mActiveStart - i - 1);
        }
    }

    private void updateAllImageRequests() {
        mActiveRequestCount = 0;
        for (int i = mActiveStart, n = mActiveEnd; i < n; ++i) {
            AlbumSetEntry entry = mData[i % mData.length];
            if (startLoadBitmap(entry.coverLoader))
                ++mActiveRequestCount;
            if (startLoadBitmap(entry.labelLoader))
                ++mActiveRequestCount;
        }
        if (mActiveRequestCount == 0) {
            requestNonactiveImages();
        } else {
            cancelNonactiveImages();
        }
    }

    @Override
    public void onSizeChanged(int size) {
        if (mIsActive && mSize != size) {
            mSize = size;
            if (mListener != null)
                mListener.onSizeChanged(mSize);
            if (mContentEnd > mSize)
                mContentEnd = mSize;
            if (mActiveEnd > mSize)
                mActiveEnd = mSize;
        }
    }

    @Override
    public void onContentChanged(int index) {
        if (!mIsActive) {
            return; // paused, ignore thumbnail changed event
        }

        // If the updated content is not cached, ignore it
        if (index < mContentStart || index >= mContentEnd) {
            LLog.w(TAG, String.format("invalid update: %s is outside (%s, %s)", index, mContentStart, mContentEnd));
            return;
        }

        AlbumSetEntry entry = mData[index % mData.length];
        updateAlbumSetEntry(entry, index);
        updateAllImageRequests();
        updateTextureUploadQueue();
        if (mListener != null && isActiveSlot(index)) {
            mListener.onContentChanged();
        }
    }

    public BitmapTexture getLoadingTexture() {
        if (mLoadingLabel == null) {
            Bitmap bitmap = mLabelMaker.requestLabel(mLoadingText, "", DataSourceType.TYPE_NOT_CATEGORIZED).run(ThreadPool.JOB_CONTEXT_STUB);
            mLoadingLabel = new BitmapTexture(bitmap);
            mLoadingLabel.setOpaque(false);
        }
        return mLoadingLabel;
    }

    public void pause() {
        mIsActive = false;
        mLabelUploader.clear();
        mContentUploader.clear();
        TiledTexture.freeResources();
        for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
            freeSlotContent(i);
        }
    }

    public void resume() {
        mIsActive = true;
        TiledTexture.prepareResources();
        for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
            prepareSlotContent(i);
        }
        updateAllImageRequests();
    }

    private static interface EntryUpdater {
        public void updateEntry();
    }
    
    private class AlbumCoverLoader extends BitmapLoader implements EntryUpdater {
        private MediaItem mMediaItem;
        private final int mSlotIndex;

        public AlbumCoverLoader(int thumbnailIndex, MediaItem item) {
            mSlotIndex = thumbnailIndex;
            mMediaItem = item;
        }

        @Override
        protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l) {
            return mThreadPool.submit(mMediaItem.requestImage(MediaItem.TYPE_MICROTHUMBNAIL), l);
        }

        @Override
        protected void onLoadComplete(Bitmap bitmap) {
            mHandler.obtainMessage(MSG_UPDATE_ALBUM_ENTRY, this).sendToTarget();
        }

        @Override
        public void updateEntry() {
            Bitmap bitmap = getBitmap();
            if (bitmap == null)
                return; // error or recycled

            AlbumSetEntry entry = mData[mSlotIndex % mData.length];
            TiledTexture texture = new TiledTexture(bitmap);
            entry.bitmapTexture = texture;
            entry.content = texture;

            if (isActiveSlot(mSlotIndex)) {
                mContentUploader.addTexture(texture);
                --mActiveRequestCount;
                if (mActiveRequestCount == 0)
                    requestNonactiveImages();
                if (mListener != null)
                    mListener.onContentChanged();
            } else {
                mContentUploader.addTexture(texture);
            }
        }
    }

    private static int identifyCacheFlag(MediaSet set) {
        if (set == null || (set.getSupportedOperations()
                & MediaSet.SUPPORT_CACHE) == 0) {
            return MediaSet.CACHE_FLAG_NO;
        }

        return set.getCacheFlag();
    }

    private static int identifyCacheStatus(MediaSet set) {
        if (set == null || (set.getSupportedOperations()
                & MediaSet.SUPPORT_CACHE) == 0) {
            return MediaSet.CACHE_STATUS_NOT_CACHED;
        }

        return set.getCacheStatus();
    }

    private class AlbumLabelLoader extends BitmapLoader implements EntryUpdater {
        private final int mSlotIndex;
        private final String mTitle;
        private final int mTotalCount;
        private final int mSourceType;

        public AlbumLabelLoader(int thumbnailIndex, String title, int totalCount, int sourceType) {
            mSlotIndex = thumbnailIndex;
            mTitle = title;
            mTotalCount = totalCount;
            mSourceType = sourceType;
        }

        @Override
        protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l) {
            return mThreadPool.submit(mLabelMaker.requestLabel(
                    mTitle, String.valueOf(mTotalCount), mSourceType), l);
        }

        @Override
        protected void onLoadComplete(Bitmap bitmap) {
            mHandler.obtainMessage(MSG_UPDATE_ALBUM_ENTRY, this).sendToTarget();
        }

        @Override
        public void updateEntry() {
            Bitmap bitmap = getBitmap();
            if (bitmap == null)
                return; // Error or recycled

            AlbumSetEntry entry = mData[mSlotIndex % mData.length];
            BitmapTexture texture = new BitmapTexture(bitmap);
            texture.setOpaque(false);
            entry.labelTexture = texture;

            if (isActiveSlot(mSlotIndex)) {
                mLabelUploader.addFgTexture(texture);
                --mActiveRequestCount;
                if (mActiveRequestCount == 0)
                    requestNonactiveImages();
                if (mListener != null)
                    mListener.onContentChanged();
            } else {
                mLabelUploader.addBgTexture(texture);
            }
        }
    }

    public void onThumbnailSizeChanged(int width, int height) {
        if (mSlotWidth == width)
            return;

        mSlotWidth = width;
        mLoadingLabel = null;
        mLabelMaker.setLabelWidth(mSlotWidth);

        if (!mIsActive)
            return;

        for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
            AlbumSetEntry entry = mData[i % mData.length];
            if (entry.labelLoader != null) {
                entry.labelLoader.recycle();
                entry.labelLoader = null;
                entry.labelTexture = null;
            }
            if (entry.album != null) {
                entry.labelLoader = new AlbumLabelLoader(i, entry.title, entry.totalCount, entry.sourceType);
            }
        }
        updateAllImageRequests();
        updateTextureUploadQueue();
    }
}
