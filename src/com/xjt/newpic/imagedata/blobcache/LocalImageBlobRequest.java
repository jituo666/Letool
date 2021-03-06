package com.xjt.newpic.imagedata.blobcache;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.xjt.newpic.NpApp;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.common.ThreadPool.JobContext;
import com.xjt.newpic.imagedata.exif.ExifInterface;
import com.xjt.newpic.imagedata.utils.BitmapDecodeUtils;
import com.xjt.newpic.metadata.MediaItem;
import com.xjt.newpic.metadata.MediaPath;

/**
 * @Author Jituo.Xuan
 * @Date 8:18:56 PM Jul 24, 2014
 * @Comments:null
 */
public class LocalImageBlobRequest extends BlobCacheRequest {

    private static final String TAG = LocalImageBlobRequest.class.getSimpleName();

    private String mLocalFilePath;

    public LocalImageBlobRequest(NpApp application, MediaPath path, long timeModified,
            int type, String localFilePath) {
        super(application, path, timeModified, type, MediaItem.getTargetSize(type));
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