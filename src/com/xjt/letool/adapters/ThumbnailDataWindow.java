package com.xjt.letool.adapters;

import android.graphics.Bitmap;
import android.os.Message;

import com.xjt.letool.common.Future;
import com.xjt.letool.common.FutureListener;
import com.xjt.letool.common.JobLimiter;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.SynchronizedHandler;
import com.xjt.letool.data.MediaItem;
import com.xjt.letool.data.MediaPath;
import com.xjt.letool.data.cache2.ThumbCacheLoader;
import com.xjt.letool.data.loader.ThumbnailDataLoader;
import com.xjt.letool.data.utils.BitmapLoader;
import com.xjt.letool.utils.Utils;
import com.xjt.letool.views.fragment.LetoolFragment;
import com.xjt.letool.views.opengl.Texture;
import com.xjt.letool.views.opengl.TiledTexture;
import com.xjt.letool.views.opengl.TiledTextureUploader;

/**
 * control the data window ,[activate range:media data], [content range: meta
 * data] 1,cache image 2,compute meta data cache range
 * 
 * @author jetoo
 * 
 */
public class ThumbnailDataWindow implements ThumbnailDataLoader.DataListener {

    private static final String TAG = "AlbumDataWindow";

    private static final int MSG_UPDATE_ENTRY = 0;
    private static final int JOB_LIMIT = 1;
    private int mActiveRequestCount = 0;
    private boolean mIsActive = false;

    private final ThumbnailDataLoader mDataSource;
    private final AlbumEntry mImageData[];
    private final SynchronizedHandler mHandler;
    private final JobLimiter mThreadPool;
    private final TiledTextureUploader mTileUploader;

    private int mSize;

    private int mContentStart = 0;
    private int mContentEnd = 0;

    private int mActiveStart = 0;
    private int mActiveEnd = 0;

    private ThumbCacheLoader mThumbnailCacheLoader = null;
    private DataListener mDataListener;

    public static interface DataListener {
        public void onSizeChanged(int size);

        public void onContentChanged();
    }

    public static class AlbumEntry {
        public MediaItem item;
        public MediaPath path;
        public int rotation;
        public int mediaType;
        public boolean isWaitDisplayed;
        public TiledTexture bitmapTexture;
        public Texture content;
        private BitmapLoader contentLoader;
    }

    public ThumbnailDataWindow(LetoolFragment fragment, ThumbnailDataLoader source, int cacheSize) {
        source.setDataListener(this);
        mDataSource = source;
        mImageData = new AlbumEntry[cacheSize];
        mSize = source.size();

        mHandler = new SynchronizedHandler(fragment.getGLController()) {
            @Override
            public void handleMessage(Message message) {
                Utils.assertTrue(message.what == MSG_UPDATE_ENTRY);
                ((ThumbnailLoader) message.obj).updateEntry();
            }
        };

        mThreadPool = new JobLimiter(fragment.getThreadPool(), JOB_LIMIT);
        mTileUploader = new TiledTextureUploader(fragment.getGLController());
        mThumbnailCacheLoader = new ThumbCacheLoader(fragment.getAndroidContext(), source.getMediaSource().getPath().getIdentity());
    }

    public void setListener(DataListener listener) {
        mDataListener = listener;
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart == mContentStart && contentEnd == mContentEnd)
            return;

        if (!mIsActive) {
            mContentStart = contentStart;
            mContentEnd = contentEnd;
            mDataSource.setActiveWindow(contentStart, contentEnd);
            return;
        }

        if (contentStart >= mContentEnd || mContentStart >= contentEnd) {
            for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
                freeSlotContent(i);
            }
            mDataSource.setActiveWindow(contentStart, contentEnd);
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
            mDataSource.setActiveWindow(contentStart, contentEnd);
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

    private void updateTextureUploadQueue() {
        if (!mIsActive)
            return;
        mTileUploader.clear();

        // add foreground textures
        for (int i = mActiveStart, n = mActiveEnd; i < n; ++i) {
            AlbumEntry entry = mImageData[i % mImageData.length];
            if (entry.bitmapTexture != null) {
                mTileUploader.addTexture(entry.bitmapTexture);
            }
        }

        // add background textures
        int range = Math.max((mContentEnd - mActiveEnd), (mActiveStart - mContentStart));
        for (int i = 0; i < range; ++i) {
            uploadBgTextureInSlot(mActiveEnd + i);
            uploadBgTextureInSlot(mActiveStart - i - 1);
        }
    }

    /**
     * 1,set image or video cache range. 2,set image's/video's meta data range.
     * 1,|_____1/2 data length_____|__________________move by step__________________|______data length_________|
     */
    public void setActiveWindow(int start, int end) {
        if (!(start <= end && end - start <= mImageData.length && end <= mSize)) {
            Utils.fail("%s, %s, %s, %s", start, end, mImageData.length, mSize);
        }
        AlbumEntry data[] = mImageData;

        mActiveStart = start;
        mActiveEnd = end;

        int contentStart = Utils.clamp((start + end) / 2 - data.length / 2,
                0, Math.max(0, mSize - data.length));
        int contentEnd = Math.min(contentStart + data.length, mSize);
        setContentWindow(contentStart, contentEnd);
        updateTextureUploadQueue();
        if (mIsActive)
            updateAllImageRequests();
    }

    public AlbumEntry get(int slotIndex) {
        if (!isActiveSlot(slotIndex)) {
            Utils.fail("invalid slot: %s outsides (%s, %s)", slotIndex, mActiveStart, mActiveEnd);
        }
        return mImageData[slotIndex % mImageData.length];
    }

    public boolean isActiveSlot(int slotIndex) {
        return slotIndex >= mActiveStart && slotIndex < mActiveEnd;
    }

    private void uploadBgTextureInSlot(int index) {
        if (index < mContentEnd && index >= mContentStart) {
            AlbumEntry entry = mImageData[index % mImageData.length];
            if (entry.bitmapTexture != null) {
                mTileUploader.addTexture(entry.bitmapTexture);
            }
        }
    }

    // We would like to request non active slots in the following order:
    // Order:    8 6 4 2                   1 3 5 7
    //         |---------|---------------|---------|
    //                   |<-  active  ->|
    //         |<-------- cached range ----------->|
    private void requestNonactiveImages() {
        int range = Math.max((mContentEnd - mActiveEnd), (mActiveStart - mContentStart));
        for (int i = 0; i < range; ++i) {
            requestSlotImage(mActiveEnd + i);
            requestSlotImage(mActiveStart - 1 - i);
        }
    }

    private void cancelNonactiveImages() {
        int range = Math.max(
                (mContentEnd - mActiveEnd), (mActiveStart - mContentStart));
        for (int i = 0; i < range; ++i) {
            cancelSlotImage(mActiveEnd + i);
            cancelSlotImage(mActiveStart - 1 - i);
        }
    }

    // return whether the request is in progress or not
    private boolean requestSlotImage(int slotIndex) {
        if (slotIndex < mContentStart || slotIndex >= mContentEnd)
            return false;
        AlbumEntry entry = mImageData[slotIndex % mImageData.length];
        if (entry.content != null || entry.item == null)
            return false;
        entry.contentLoader.startLoad();
        return entry.contentLoader.isRequestInProgress();
    }

    private void cancelSlotImage(int slotIndex) {
        if (slotIndex < mContentStart || slotIndex >= mContentEnd)
            return;
        AlbumEntry item = mImageData[slotIndex % mImageData.length];
        if (item.contentLoader != null)
            item.contentLoader.cancelLoad();
    }

    private void prepareSlotContent(int slotIndex) {
        AlbumEntry entry = new AlbumEntry();
        MediaItem item = mDataSource.get(slotIndex); // item could be null;
        entry.item = item;
        entry.mediaType = (item == null) ? MediaItem.MEDIA_TYPE_UNKNOWN : entry.item.getMediaType();
        entry.path = (item == null) ? null : item.getPath();
        entry.rotation = (item == null) ? 0 : item.getRotation();
        entry.contentLoader = new ThumbnailLoader(slotIndex, entry.item);
        mImageData[slotIndex % mImageData.length] = entry;
    }

    private void freeSlotContent(int slotIndex) {
        AlbumEntry data[] = mImageData;
        int index = slotIndex % data.length;
        AlbumEntry entry = data[index];
        if (entry.contentLoader != null)
            entry.contentLoader.recycle();
        if (entry.bitmapTexture != null)
            entry.bitmapTexture.recycle();
        data[index] = null;
    }

    private void updateAllImageRequests() {
        mActiveRequestCount = 0;
        for (int i = mActiveStart, n = mActiveEnd; i < n; ++i) {
            if (requestSlotImage(i))
                ++mActiveRequestCount;
        }
        if (mActiveRequestCount == 0) {
            requestNonactiveImages();
        } else {
            cancelNonactiveImages();
        }
    }

    public void resume() {
        mIsActive = true;
        mThumbnailCacheLoader.resume();
        TiledTexture.prepareResources();
        for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
            prepareSlotContent(i);
        }
        updateAllImageRequests(); // Frist start no use, just for backing from other
    }

    public void pause() {
        mIsActive = false;
        mThumbnailCacheLoader.pause();
        mTileUploader.clear();
        TiledTexture.freeResources();
        for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
            freeSlotContent(i);
        }
    }

    @Override
    public void onContentChanged(int index) {
        //LLog.i(TAG, "onContentChanged:" + index);
        if (index >= mContentStart && index < mContentEnd && mIsActive) {
            freeSlotContent(index);
            prepareSlotContent(index);
            updateAllImageRequests();
            if (mDataListener != null && isActiveSlot(index)) {
                mDataListener.onContentChanged();
            }
        }
    }

    @Override
    public void onSizeChanged(int size) {
        if (mSize != size) {
            mSize = size;
            if (mDataListener != null)
                mDataListener.onSizeChanged(mSize);
            if (mContentEnd > mSize)
                mContentEnd = mSize;
            if (mActiveEnd > mSize)
                mActiveEnd = mSize;
        }
    }

    private class ThumbnailLoader extends BitmapLoader {
        private final int mSlotIndex;
        private final MediaItem mItem;
        long time = System.currentTimeMillis();

        public ThumbnailLoader(int slotIndex, MediaItem item) {
            mSlotIndex = slotIndex;
            mItem = item;
        }

        @Override
        protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l) {
            //mThumbnailCacheLoader.updateCurrentIndex(mSlotIndex, mItem.getDateInMs(), mItem.getFilePath());
            time = System.currentTimeMillis();
            return mThreadPool.submit(mItem.requestImage(MediaItem.TYPE_MICROTHUMBNAIL,
                    mSlotIndex, mItem.getDateInMs(), mThumbnailCacheLoader), this);
        }

        @Override
        protected void onLoadComplete(Bitmap bitmap) {
            mHandler.obtainMessage(MSG_UPDATE_ENTRY, this).sendToTarget();
        }

        public void updateEntry() {
            Bitmap bitmap = getBitmap();
            if (bitmap == null)
                return; // error or recycled
            AlbumEntry entry = mImageData[mSlotIndex % mImageData.length];
            entry.bitmapTexture = new TiledTexture(bitmap);
            entry.content = entry.bitmapTexture;
            if (isActiveSlot(mSlotIndex)) {
                mTileUploader.addTexture(entry.bitmapTexture);
                --mActiveRequestCount;
                if (mActiveRequestCount == 0)
                    requestNonactiveImages();
                if (mDataListener != null)
                    mDataListener.onContentChanged();
            } else {
                mTileUploader.addTexture(entry.bitmapTexture);
            }
        }
    }
}
