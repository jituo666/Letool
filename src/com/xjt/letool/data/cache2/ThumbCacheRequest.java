package com.xjt.letool.data.cache2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.xjt.letool.common.LLog;
import com.xjt.letool.common.ThreadPool.Job;
import com.xjt.letool.common.ThreadPool.JobContext;
import com.xjt.letool.data.MediaItem;
import com.xjt.letool.data.utils.BitmapDecodeUtils;
import com.xjt.letool.data.utils.BitmapUtils;

/**
 * @Author Jituo.Xuan
 * @Date 9:48:08 AM Apr 26, 2014
 * @Comments:null
 */
public abstract class ThumbCacheRequest implements Job<Bitmap> {

    private static final String TAG = ThumbCacheRequest.class.getSimpleName();

    protected String mLocalFilePath;
    private int mType;
    private int mTargetSize;
    private int mIndex;
    private long mDateToken;
    private ThumbCacheLoader mLoader;

    public ThumbCacheRequest(ThumbCacheLoader loader, int type, int index, String pathHashCode, long dateTaken, int targetSize) {
        mType = type;
        mTargetSize = targetSize;
        mLoader = loader;
        mIndex = index;
        mLocalFilePath = pathHashCode;
        mDateToken = dateTaken;
    }

    private String debugTag() {
        return ((mType == MediaItem.TYPE_THUMBNAIL) ? "THUMB" :
                (mType == MediaItem.TYPE_MICROTHUMBNAIL) ? "MICROTHUMB" : "?");
    }

    @Override
    public Bitmap run(JobContext jc) {
        if (jc.isCancelled())
            return null;

        long time = System.currentTimeMillis();

        byte data[] = mLoader.getThumbData(mIndex, mLocalFilePath, mDateToken);

        LLog.w(TAG, " read data from db, sepent: " + (System.currentTimeMillis() - time) + " datalengh:" + (data == null ? 0 : data.length));
        if (data != null && data.length > 0) {
            time = System.currentTimeMillis();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap;
            if (mType == MediaItem.TYPE_MICROTHUMBNAIL) {
                bitmap = BitmapDecodeUtils.decodeUsingPool(jc, data, 0, data.length, options);
            } else {
                bitmap = BitmapDecodeUtils.decodeUsingPool(jc, data, 0, data.length, options);
            }
            if (bitmap == null && !jc.isCancelled()) {
                LLog.w(TAG, "decode cached failed " + debugTag());
            }
            LLog.w(TAG,  "create bitmap from data, spent:" + (System.currentTimeMillis() - time));
            return bitmap;
        }
        LLog.w(TAG, "-----------------no cache ,create it " + debugTag());
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
        mLoader.putThumbData(array);
        if (jc.isCancelled())
            return null;

        return bitmap;
    }

    public abstract Bitmap onDecodeOriginal(JobContext jc, int targetSize);
}
