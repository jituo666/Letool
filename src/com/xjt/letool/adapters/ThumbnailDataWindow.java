
package com.xjt.letool.adapters;

import android.graphics.Bitmap;
import android.opengl.ETC1Util.ETC1Texture;
import android.os.Message;

import com.xjt.letool.common.Future;
import com.xjt.letool.common.FutureListener;
import com.xjt.letool.common.JobLimiter;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.SynchronizedHandler;
import com.xjt.letool.fragment.LetoolFragment;
import com.xjt.letool.imagedata.utils.BitmapLoader;
import com.xjt.letool.imagedata.utils.ETC1TextureLoader;
import com.xjt.letool.metadata.MediaItem;
import com.xjt.letool.metadata.MediaPath;
import com.xjt.letool.metadata.loader.ThumbnailDataLoader;
import com.xjt.letool.utils.Utils;
import com.xjt.letool.views.layout.ThumbnailExpandLayout.SortTag;
import com.xjt.letool.views.opengl.BitmapTexture;
import com.xjt.letool.views.opengl.ETC1CompressedTexture;
import com.xjt.letool.views.opengl.Texture;
import com.xjt.letool.views.opengl.TextureUploader;
import com.xjt.letool.views.opengl.TiledTexture;
import com.xjt.letool.views.render.ThumbnailRenderer;
import com.xjt.letool.views.render.ThumbnailRendererWithTag;
import com.xjt.letool.views.utils.AlbumSortTagMaker;

import java.util.ArrayList;

/**
 * control the data window ,[activate range:media data], [content range: meta
 * data] 1,cache image 2,compute meta data cache range
 * 
 * @author jetoo
 * 
 */
public class ThumbnailDataWindow implements ThumbnailDataLoader.DataChangedListener {

    private static final String TAG = ThumbnailDataWindow.class.getSimpleName();

    private static final int MSG_UPDATE_ENTRY = 0;
    private static final int JOB_LIMIT = 2;
    private int mActiveRequestCount = 0;
    private boolean mIsActive = false;

    private final ThumbnailDataLoader mDataSource;
    private DataListener mDataListener;
    private final AlbumEntry mImageData[];
    private final SynchronizedHandler mHandler;
    private final JobLimiter mThreadPool;
    private int mSize;

    private int mContentStart = 0;
    private int mContentEnd = 0;

    private int mActiveStart = 0;
    private int mActiveEnd = 0;

    public static interface DataListener {

        public void onSizeChanged(int size, ArrayList<SortTag> tags);

        public void onContentChanged();
    }

    public static class AlbumEntry {

        public MediaItem item;
        public MediaPath path;
        public int rotation;
        public int mediaType;
        public boolean isWaitDisplayed;
        public ETC1CompressedTexture compressTexture;
        private ETC1TextureLoader contentLoader;
    }

    public ThumbnailDataWindow(LetoolFragment fragment, ThumbnailDataLoader source, int cacheSize) {
        this(fragment, source, cacheSize, null);
    }

    public ThumbnailDataWindow(LetoolFragment fragment, ThumbnailDataLoader source, int cacheSize,
            ThumbnailRendererWithTag.SortTagSpec sortTagSpec) {
        source.setDataChangedListener(this);
        mDataSource = source;
        mImageData = new AlbumEntry[cacheSize];
        mSortTagEntry = new SortTagEntry[cacheSize / 2];
        mSize = source.size();
        mHandler = new SynchronizedHandler(fragment.getGLController()) {

            @Override
            public void handleMessage(Message message) {
                if (message.what == MSG_UPDATE_ENTRY) {
                    ((ThumbnailLoader) message.obj).updateEntry();
                } else if (message.what == MSG_UPDATE_ALBUM_ENTRY) {
                    ((SortTagLoader) message.obj).updateEntry();
                }
            }
        };
        mThreadPool = new JobLimiter(fragment.getThreadPool(), JOB_LIMIT);
        mTextureUploader = new TextureUploader(fragment.getGLController());
        if (sortTagSpec != null)
            mSortTagMaker = new AlbumSortTagMaker(sortTagSpec);

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
                freeThumbnailContent(i);
            }
            mDataSource.setActiveWindow(contentStart, contentEnd);
            for (int i = contentStart; i < contentEnd; ++i) {
                prepareThumbnailContent(i);
            }
        } else {
            for (int i = mContentStart; i < contentStart; ++i) {
                freeThumbnailContent(i);
            }
            for (int i = contentEnd, n = mContentEnd; i < n; ++i) {
                freeThumbnailContent(i);
            }
            mDataSource.setActiveWindow(contentStart, contentEnd);
            for (int i = contentStart, n = mContentStart; i < n; ++i) {
                prepareThumbnailContent(i);
            }
            for (int i = mContentEnd; i < contentEnd; ++i) {
                prepareThumbnailContent(i);
            }
        }

        mContentStart = contentStart;
        mContentEnd = contentEnd;
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

        int contentStart = Utils.clamp((start + end) / 2 - data.length / 2, 0, Math.max(0, mSize - data.length));
        int contentEnd = Math.min(contentStart + data.length, mSize);
        setContentWindow(contentStart, contentEnd);

        if (mIsActive)
            updateAllImageRequests();
    }

    public AlbumEntry get(int slotIndex) {
        if (!isActiveThumbnail(slotIndex)) {
            Utils.fail("invalid slot: %s outsides (%s, %s)", slotIndex, mActiveStart, mActiveEnd);
        }
        return mImageData[slotIndex % mImageData.length];
    }

    public boolean isActiveThumbnail(int slotIndex) {
        return slotIndex >= mActiveStart && slotIndex < mActiveEnd;
    }

    // We would like to request non active slots in the following order:
    // Order:    8 6 4 2                   1 3 5 7
    //         |---------|---------------|---------|
    //                   |<-  active  ->|
    //         |<-------- cached range ----------->|
    private void requestNonactiveImages() {
        int range = Math.max((mContentEnd - mActiveEnd), (mActiveStart - mContentStart));
        for (int i = 0; i < range; ++i) {
            requestThumbnailImage(mActiveEnd + i);
            requestThumbnailImage(mActiveStart - 1 - i);
        }
    }

    private void cancelNonactiveImages() {
        int range = Math.max((mContentEnd - mActiveEnd), (mActiveStart - mContentStart));
        for (int i = 0; i < range; ++i) {
            cancelThumbnailImage(mActiveEnd + i);
            cancelThumbnailImage(mActiveStart - 1 - i);
        }
    }

    // return whether the request is in progress or not
    private boolean requestThumbnailImage(int slotIndex) {
        if (slotIndex < mContentStart || slotIndex >= mContentEnd)
            return false;
        AlbumEntry entry = mImageData[slotIndex % mImageData.length];
        if (entry.compressTexture != null || entry.item == null)
            return false;
        entry.contentLoader.startLoad();
        return entry.contentLoader.isRequestInProgress();
    }

    private void cancelThumbnailImage(int slotIndex) {
        if (slotIndex < mContentStart || slotIndex >= mContentEnd)
            return;
        AlbumEntry item = mImageData[slotIndex % mImageData.length];
        if (item.contentLoader != null)
            item.contentLoader.cancelLoad();
    }

    private void prepareThumbnailContent(int slotIndex) {
        AlbumEntry entry = new AlbumEntry();
        MediaItem item = mDataSource.get(slotIndex); // item could be null;
        entry.item = item;
        entry.mediaType = (item == null) ? MediaItem.MEDIA_TYPE_UNKNOWN : entry.item.getMediaType();
        entry.path = (item == null) ? null : item.getPath();
        entry.rotation = (item == null) ? 0 : item.getRotation();
        entry.contentLoader = new ThumbnailLoader(slotIndex, entry.item);
        mImageData[slotIndex % mImageData.length] = entry;
    }

    private void freeThumbnailContent(int slotIndex) {
        AlbumEntry data[] = mImageData;
        int index = slotIndex % data.length;
        AlbumEntry entry = data[index];
        if (entry.contentLoader != null)
            entry.contentLoader.recycle();
        if (entry.compressTexture != null)
            entry.compressTexture.recycle();
        data[index] = null;
    }

    private void updateAllImageRequests() {
        mActiveRequestCount = 0;
        for (int i = mActiveStart, n = mActiveEnd; i < n; ++i) {
            if (requestThumbnailImage(i))
                ++mActiveRequestCount;
        }
        if (mActiveRequestCount == 0) {
            requestNonactiveImages();
        } else {
            cancelNonactiveImages();
        }
    }

    public void resume() {
        LLog.i(TAG, " resume1:" + System.currentTimeMillis());
        mIsActive = true;
        TiledTexture.prepareResources();
        for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
            prepareThumbnailContent(i);
        }
        LLog.i(TAG, " resume2:" + System.currentTimeMillis());
        updateAllImageRequests(); // Frist start no use, just for backing from other
    }

    public void pause() {
        mIsActive = false;
        TiledTexture.freeResources();
        for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
            freeThumbnailContent(i);
        }
    }

    @Override
    public void onContentChanged(int index) {
        if (index >= mContentStart && index < mContentEnd && mIsActive) {
            freeThumbnailContent(index);
            prepareThumbnailContent(index);
            updateAllImageRequests();
            if (mDataListener != null && isActiveThumbnail(index)) {
                mDataListener.onContentChanged();
            }
        }
    }

    @Override
    public void onSizeChanged(int size, ArrayList<SortTag> tags) {
        if (mSize != size) {
            mSize = size;
            mSortTags = tags;
            if (mDataListener != null) {
                LLog.i(TAG, "--------------tags-tags--------mSize:" + mSize + " tag null?:" + (tags == null));
                mDataListener.onSizeChanged(mSize, tags);
            }
            if (mContentEnd > mSize)
                mContentEnd = mSize;
            if (mActiveEnd > mSize)
                mActiveEnd = mSize;
        }
    }

    private class ThumbnailLoader extends ETC1TextureLoader {

        private final int mThumbnailIndex;

        private final MediaItem mItem;

        public ThumbnailLoader(int slotIndex, MediaItem item) {
            mThumbnailIndex = slotIndex;
            mItem = item;
        }

        @Override
        protected Future<ETC1Texture> submitETC1TextureTask(FutureListener<ETC1Texture> l) {
            return mThreadPool.submit(mItem.requestImage(MediaItem.TYPE_MICROTHUMBNAIL, 0), this);
        }

        @Override
        protected void onLoadComplete(ETC1Texture bitmap) {
            mHandler.obtainMessage(MSG_UPDATE_ENTRY, this).sendToTarget();
        }

        public void updateEntry() {
            ETC1Texture texture = getETC1Texture();
            if (texture == null)
                return; // error or recycled

            AlbumEntry entry = mImageData[mThumbnailIndex % mImageData.length];
            entry.compressTexture = new ETC1CompressedTexture(texture);
            if (isActiveThumbnail(mThumbnailIndex)) {

                --mActiveRequestCount;
                if (mActiveRequestCount == 0)
                    requestNonactiveImages();
                if (mDataListener != null) {
                    mDataListener.onContentChanged();
                }
            } else {

            }
        }
    }

    // ----------------------------------------------------------tag--------------------------------------------------------
    private SortTagEntry mSortTagEntry[];
    private AlbumSortTagMaker mSortTagMaker;
    private ArrayList<SortTag> mSortTags;
    private int mTagStart = 0;
    private int mTagEnd = 0;
    private int mActiveTagStart = 0;
    private int mActiveTagEnd = 0;
    private int mActiveRequestTagCount = 0;
    private final TextureUploader mTextureUploader;

    private static final int MSG_UPDATE_ALBUM_ENTRY = 1;

    public static class SortTagEntry {

        // public Path path;
        public BitmapTexture bitmapTexture;
        public Texture content;
        public BitmapLoader tagLoader;
    }

    public SortTagEntry getSortTagEntry(int tagIndex) {
        if (!isActiveTag(tagIndex)) {
            Utils.fail("invalid tag: %s outsides (%s, %s)", tagIndex, mActiveTagStart, mActiveTagEnd);
        }
        return mSortTagEntry[tagIndex % mSortTagEntry.length];
    }

    public SortTag getSortTag(int tagIndex) {
        return mSortTags.get(tagIndex);
    }

    public void setSortTagMetrics(int width, int height) {
        if (mSortTagMaker != null)
            mSortTagMaker.setSortTagMetrics(width, height);
    }

    public boolean isActiveTag(int tagIndex) {
        return tagIndex >= mActiveTagStart && tagIndex < mActiveTagEnd;
    }

    private void setTagWindow(int tagStart, int tagEnd) {
        if (tagStart == mTagStart && tagEnd == mTagEnd)
            return;
        if (!mIsActive) {
            mTagStart = tagStart;
            mTagEnd = tagEnd;
            return;
        }
        if (tagStart >= mTagEnd || mTagStart >= tagEnd) {
            for (int i = mTagStart, n = mTagEnd; i < n; ++i) {
                freeTagContent(i);
            }
            for (int i = tagStart; i < tagEnd; ++i) {
                prepareTagContent(i);
            }
            //Log.i(TAG, "-----1------" + mTagStart + ":" + mTagEnd + ":" + tagStart + ":" + tagEnd);
        } else {
            for (int i = mTagStart; i < tagStart; ++i) {
                freeTagContent(i);
            }
            for (int i = tagEnd, n = mTagEnd; i < n; ++i) {
                freeTagContent(i);
            }
            for (int i = tagStart, n = mTagStart; i < n; ++i) {
                prepareTagContent(i);
            }
            for (int i = mTagEnd; i < tagEnd; ++i) {
                prepareTagContent(i);
            }
        }
        mTagStart = tagStart;
        mTagEnd = tagEnd;
    }

    public void setActiveTagWindow(int start, int end) {
        if (!(start <= end && end - start <= mSortTagEntry.length && end <= mSortTags.size())) {
            Utils.fail("%s, %s, %s, %s", start, end, mSortTagEntry.length, mSortTags.size());
        }
        SortTagEntry data[] = mSortTagEntry;
        mActiveTagStart = start;
        mActiveTagEnd = end;
        int contentStart = Utils.clamp((start + end) / 2 - data.length / 2, 0, Math.max(0, mSortTags.size() - data.length));
        int contentEnd = Math.min(contentStart + data.length, mSortTags.size());
        setTagWindow(contentStart, contentEnd);
        updateTextureUploadQueueTag();
        if (mIsActive)
            updateAllImageRequestsTag();
    }

    private void uploadBgTextureInTag(int index) {
        if (index < mTagEnd && index >= mTagStart) {
            SortTagEntry entry = mSortTagEntry[index % mSortTagEntry.length];
            if (entry.bitmapTexture != null) {
                mTextureUploader.addBgTexture(entry.bitmapTexture);
            }
        }
    }

    private void updateTextureUploadQueueTag() {
        if (!mIsActive)
            return;
        mTextureUploader.clear();
        // add foreground textures
        for (int i = mActiveTagStart, n = mActiveTagEnd; i < n; ++i) {
            SortTagEntry entry = mSortTagEntry[i % mSortTagEntry.length];
            if (entry.bitmapTexture != null) {
                mTextureUploader.addFgTexture(entry.bitmapTexture);
            }
        }
        // add background textures
        int range = Math.max((mTagEnd - mActiveTagEnd), (mActiveTagStart - mTagStart));
        for (int i = 0; i < range; ++i) {
            uploadBgTextureInTag(mActiveTagEnd + i);
            uploadBgTextureInTag(mActiveTagStart - i - 1);
        }
    }

    // We would like to request non active slots in the following order:
    // Order: 8 6 4 2 1 3 5 7
    // |---------|---------------|---------|
    // |<- active ->|
    // |<-------- cached range ----------->|
    private void requestNonactiveImagesTag() {
        int range = Math.max((mTagEnd - mActiveTagEnd), (mActiveTagStart - mTagStart));
        for (int i = 0; i < range; ++i) {
            requestTagImage(mActiveTagEnd + i);
            requestTagImage(mActiveTagStart - 1 - i);
        }
    }

    private void cancelNonactiveImagesTag() {
        int range = Math.max((mTagEnd - mActiveTagEnd), (mActiveTagStart - mTagStart));
        for (int i = 0; i < range; ++i) {
            cancelTagImage(mActiveTagEnd + i);
            cancelTagImage(mActiveTagStart - 1 - i);
        }
    }

    private void cancelTagImage(int slotIndex) {
        if (slotIndex < mTagStart || slotIndex >= mTagEnd)
            return;
        SortTagEntry item = mSortTagEntry[slotIndex % mSortTagEntry.length];
        if (item.tagLoader != null)
            item.tagLoader.cancelLoad();
    }

    // return whether the request is in progress or not
    private boolean requestTagImage(int tagIndex) {
        if (tagIndex < mTagStart || tagIndex >= mTagEnd)
            return false;
        SortTagEntry entry = mSortTagEntry[tagIndex % mSortTagEntry.length];
        if (entry.content != null)
            return false;
        entry.tagLoader.startLoad();
        return entry.tagLoader.isRequestInProgress();
    }

    private void freeTagContent(int tagIndex) {
        SortTagEntry data[] = mSortTagEntry;
        int index = tagIndex % data.length;
        SortTagEntry entry = data[index];
        if (entry.tagLoader != null)
            entry.tagLoader.recycle();
        if (entry.bitmapTexture != null)
            entry.bitmapTexture.recycle();
        data[index] = null;
    }

    private void prepareTagContent(int tagIndex) {
        SortTagEntry entry = new SortTagEntry();
        entry.tagLoader = new SortTagLoader(tagIndex, mSortTags.get(tagIndex).name, String.format("%d",
                mSortTags.get(tagIndex).count));
        mSortTagEntry[tagIndex % mSortTagEntry.length] = entry;
    }

    private void updateAllImageRequestsTag() {
        mActiveRequestTagCount = 0;
        for (int i = mActiveTagStart, n = mActiveTagEnd; i < n; ++i) {
            if (requestTagImage(i))
                ++mActiveRequestTagCount;
        }
        if (mActiveRequestTagCount == 0) {
            requestNonactiveImagesTag();
        } else {
            cancelNonactiveImagesTag();
        }
    }

    private static interface EntryUpdater {

        public void updateEntry();
    }

    private class SortTagLoader extends BitmapLoader implements EntryUpdater {

        private final int mSortTagIndex;
        private final String mTagName;
        private final String mCount;

        public SortTagLoader(int index, String title, String count) {
            mSortTagIndex = index;
            mTagName = title;
            mCount = count;
        }

        @Override
        protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l) {
            return mThreadPool.submit(mSortTagMaker.requestTag(mTagName, mCount), l);
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
            SortTagEntry entry = mSortTagEntry[mSortTagIndex % mSortTagEntry.length];
            BitmapTexture texture = new BitmapTexture(bitmap);
            texture.setOpaque(false);
            entry.bitmapTexture = texture;
            entry.content = entry.bitmapTexture;
            mTextureUploader.addFgTexture(texture);
            if (isActiveTag(mSortTagIndex)) {
                mTextureUploader.addFgTexture(entry.bitmapTexture);
                --mActiveRequestTagCount;
                if (mActiveRequestTagCount == 0)
                    requestNonactiveImagesTag();
                if (mDataListener != null)
                    mDataListener.onContentChanged();
            } else {
                mTextureUploader.addBgTexture(entry.bitmapTexture);
            }
        }
    }
}
