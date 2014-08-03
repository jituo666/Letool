package com.xjt.newpic.imagedata.blobcache;

import android.graphics.Bitmap;

import com.xjt.newpic.LetoolApp;
import com.xjt.newpic.common.ThreadPool.JobContext;
import com.xjt.newpic.imagedata.utils.BitmapUtils;
import com.xjt.newpic.metadata.MediaItem;
import com.xjt.newpic.metadata.MediaPath;

/**
 * @Author Jituo.Xuan
 * @Date 8:19:00 PM Jul 24, 2014
 * @Comments:null
 */
public class LocalVideoBlobRequest extends BlobCacheRequest {
    private String mLocalFilePath;

    public LocalVideoBlobRequest(LetoolApp application, MediaPath path, long timeModified, int type, String localFilePath) {
        super(application, path, timeModified, type, MediaItem.getTargetSize(type));
        mLocalFilePath = localFilePath;
    }

    @Override
    public Bitmap onDecodeOriginal(JobContext jc, int type) {
        Bitmap bitmap = BitmapUtils.createVideoThumbnail(mLocalFilePath);
        if (bitmap == null || jc.isCancelled())
            return null;
        return bitmap;
    }
}