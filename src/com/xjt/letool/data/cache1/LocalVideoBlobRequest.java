package com.xjt.letool.data.cache1;

import android.graphics.Bitmap;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.common.ThreadPool.JobContext;
import com.xjt.letool.data.MediaItem;
import com.xjt.letool.data.MediaPath;
import com.xjt.letool.data.utils.BitmapUtils;

public class LocalVideoBlobRequest extends BlobCacheRequest {
    private String mLocalFilePath;

    public LocalVideoBlobRequest(LetoolApp application, MediaPath path, long timeModified,
            int type, String localFilePath) {
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