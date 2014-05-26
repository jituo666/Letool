package com.xjt.letool.imagedata.blobcache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.R;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.ThreadPool.Job;
import com.xjt.letool.common.ThreadPool.JobContext;
import com.xjt.letool.imagedata.utils.BitmapDecodeUtils;
import com.xjt.letool.imagedata.utils.BitmapUtils;
import com.xjt.letool.imagedata.utils.BytesBufferPool.BytesBuffer;
import com.xjt.letool.metadata.MediaItem;
import com.xjt.letool.metadata.MediaPath;

public abstract class BlobCacheRequest implements Job<Bitmap> {

    private static final String TAG = BlobCacheRequest.class.getSimpleName();

    protected LetoolApp mApplication;
    private MediaPath mPath;
    private int mType;
    private int mTargetSize;
    private long mTimeModified;

//    private static Bitmap bitmap;

    public BlobCacheRequest(LetoolApp application, MediaPath path, long timeModified, int type, int targetSize) {
        mApplication = application;
        mPath = path;
        mType = type;
        mTargetSize = targetSize;
        mTimeModified = timeModified;
    }

    private String debugTag() {
        return mPath + "," + mTimeModified + ","
                + ((mType == MediaItem.TYPE_THUMBNAIL) ? "THUMB" : (mType == MediaItem.TYPE_MICROTHUMBNAIL) ? "MICROTHUMB" : "?");
    }

    @Override
    public Bitmap run(JobContext jc) {
//        if (bitmap == null || bitmap.isRecycled()) {
//            bitmap = BitmapFactory.decodeResource(mApplication.getResources(), R.drawable.sliding_menu_logo_bg);
//        }
                BlobCacheService cacheService = mApplication.getImageCacheService();
                BytesBuffer buffer = MediaItem.getBytesBufferPool().get();
                try {
                    boolean found = cacheService.getImageData(mPath, mTimeModified, mType, buffer);
                    if (jc.isCancelled())
                        return null;
                    if (found) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap;
                        if (mType == MediaItem.TYPE_MICROTHUMBNAIL) {
                            bitmap = BitmapDecodeUtils.decodeUsingPool(jc, buffer.data, buffer.offset, buffer.length, options);
                        } else {
                            bitmap = BitmapDecodeUtils.decodeUsingPool(jc, buffer.data, buffer.offset, buffer.length, options);
                        }
                        if (bitmap == null && !jc.isCancelled()) {
                            LLog.w(TAG, "decode cached failed " + debugTag());
                        }
                        return bitmap;
                    }
                } finally {
                    MediaItem.getBytesBufferPool().recycle(buffer);
                }
        
                Bitmap bitmap = onDecodeOriginal(jc, mType);
                if (jc.isCancelled())
                    return null;
                if (bitmap == null) {
                    LLog.w(TAG, "decode orig failed " + debugTag());
                    return null;
                }
                if (mType == MediaItem.TYPE_MICROTHUMBNAIL) {
                    bitmap = BitmapUtils.resizeAndCropCenter(bitmap, mTargetSize, true);
                } else {
                    bitmap = BitmapUtils.resizeDownBySideLength(bitmap, mTargetSize, true);
                }
                if (jc.isCancelled())
                    return null;
                byte[] array = BitmapUtils.compressToBytes(bitmap);
                if (jc.isCancelled())
                    return null;
                cacheService.putImageData(mPath, mTimeModified, mType, array);
        return bitmap;
    }

    public abstract Bitmap onDecodeOriginal(JobContext jc, int targetSize);
}
