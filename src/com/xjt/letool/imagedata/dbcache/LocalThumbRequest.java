package com.xjt.letool.imagedata.dbcache;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.ThreadPool.JobContext;
import com.xjt.letool.imagedata.exif.ExifInterface;
import com.xjt.letool.imagedata.utils.BitmapDecodeUtils;
import com.xjt.letool.metadata.MediaItem;

/**
 * @Author Jituo.Xuan
 * @Date 9:48:02 AM Apr 26, 2014
 * @Comments:null
 */
public class LocalThumbRequest extends DataBaseCacheRequest {

    private static final String TAG = LocalThumbRequest.class.getSimpleName();

    public LocalThumbRequest(DataBaseCache loader, int type, int index,  long dateTaken, String localFilePath) {
        super(loader, type, index, localFilePath, dateTaken, MediaItem.getTargetSize(type));
        mLocalFilePath = localFilePath;
    }

    @Override
    public Bitmap onDecodeOriginal(JobContext jc, final int type) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        int targetSize = MediaItem.getTargetSize(type);
        // try to decode from JPEG EXIF
        if (type == MediaItem.TYPE_MICROTHUMBNAIL) {
            ExifInterface exif = new ExifInterface();
            byte[] thumbData = null;
            try {
                exif.readExif(mLocalFilePath);
                thumbData = exif.getThumbnail();
            } catch (FileNotFoundException e) {
                LLog.w(TAG, "failed to find file to read thumbnail: " + mLocalFilePath);
            } catch (IOException e) {
                LLog.w(TAG, "failed to get thumbnail from: " + mLocalFilePath);
            }
            if (thumbData != null) {
                Bitmap bitmap = BitmapDecodeUtils.decodeIfBigEnough(jc, thumbData, options, targetSize);
                if (bitmap != null)
                    return bitmap;
            }
        }
        return BitmapDecodeUtils.decodeThumbnail(jc, mLocalFilePath, options, targetSize, type);
    }
}