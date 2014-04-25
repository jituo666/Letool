
package com.xjt.letool.data.cache2;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.ThreadPool.Job;
import com.xjt.letool.common.ThreadPool.JobContext;
import com.xjt.letool.data.MediaItem;
import com.xjt.letool.data.provider.LetoolContent;
import com.xjt.letool.data.utils.BitmapDecodeUtils;
import com.xjt.letool.data.utils.BitmapUtils;

public abstract class ThumbnailCacheRequest implements Job<Bitmap> {

    private static final String TAG = ThumbnailCacheRequest.class.getSimpleName();

    protected LetoolApp mApplication;
    private String mPath;
    private int mType;
    private int mTargetSize;
    private long mTimeModified;
    private Cursor mCursor;

    public ThumbnailCacheRequest(LetoolApp application, Cursor cursor, String path, long timeModified, int type, int targetSize) {
        mApplication = application;
        mPath = path;
        mType = type;
        mTargetSize = targetSize;
        mTimeModified = timeModified;
        mCursor = cursor;
    }

    private String debugTag() {
        return mPath + "," + mTimeModified + "," +
                ((mType == MediaItem.TYPE_THUMBNAIL) ? "THUMB" :
                        (mType == MediaItem.TYPE_MICROTHUMBNAIL) ? "MICROTHUMB" : "?");
    }

    @Override
    public Bitmap run(JobContext jc) {
        ContentResolver res = mApplication.getContentResolver();

        if (jc.isCancelled())
            return null;

        long time = System.currentTimeMillis();
        if (mCursor != null && mCursor.moveToFirst()) {
            int columnIndex = mCursor.getColumnIndex(LetoolContent.Thumbnails.DATE_TAKEN);
            int columnIndexData = mCursor.getColumnIndex(LetoolContent.Thumbnails.THUMBS_DATA);
            do {
                if (mCursor.getInt(columnIndex) == mTimeModified) {
                    byte data[] = mCursor.getBlob(columnIndexData);
                    if (data != null && data.length > 0) {
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
                        LLog.w(TAG, (data == null ? 0 : data.length) + "-----------------data end " + (System.currentTimeMillis() - time));
                        return bitmap;
                    }
                    break;
                }
            } while (mCursor.moveToNext());
        }
        LLog.w(TAG, "-----------------no cache ,new bitmap " + debugTag());
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
        try {
            ContentValues values = new ContentValues();
            values.put(LetoolContent.Thumbnails.ORIGINAL_PATH, mPath);
            values.put(LetoolContent.Thumbnails.DATE_TAKEN, mTimeModified);
            values.put(LetoolContent.Thumbnails.THUMBS_DATA, array);
            res.insert(LetoolContent.Thumbnails.CONTENT_URI, values);
        } catch (Exception e) {
            LLog.i(TAG, "---------------------" + e.getMessage());
        }
        return bitmap;
    }

    public abstract Bitmap onDecodeOriginal(JobContext jc, int targetSize);
}
