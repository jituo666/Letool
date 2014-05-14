package com.xjt.letool.imagedata.blobcache;

import java.io.ByteArrayInputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.opengl.ETC1Util;
import android.opengl.ETC1Util.ETC1Texture;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.ThreadPool.Job;
import com.xjt.letool.common.ThreadPool.JobContext;
import com.xjt.letool.imagedata.utils.BitmapDecodeUtils;
import com.xjt.letool.imagedata.utils.BitmapUtils;
import com.xjt.letool.imagedata.utils.BytesBufferPool.BytesBuffer;
import com.xjt.letool.metadata.MediaItem;
import com.xjt.letool.metadata.MediaPath;

public abstract class ETCCacheRequest implements Job<ETC1Texture> {

    private static final String TAG = ETCCacheRequest.class.getSimpleName();

    protected LetoolApp mApplication;
    private MediaPath mPath;
    private int mType;
    private int mTargetSize;
    private long mTimeModified;

    public ETCCacheRequest(LetoolApp application, MediaPath path, long timeModified, int type, int targetSize) {
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
    public ETC1Texture run(JobContext jc) {
        BlobCacheService cacheService = mApplication.getImageCacheService();
        BytesBuffer buffer = MediaItem.getBytesBufferPool().get();

        try {
            boolean found = cacheService.getImageData(mPath, mTimeModified, mType, buffer);
            if (jc.isCancelled())
                return null;
            if (found) {
                return ETC1Util.createTexture(new ByteArrayInputStream(buffer.data, buffer.offset, buffer.length));
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
        ETC1Texture texture;
        texture = ETC1Util.compressTexture(bitmap.compress(CompressFormat.JPEG, 60, 0), mTargetSize, mTargetSize, 2, 0);
        //byte[] array = BitmapUtils.compressToBytes(bitmap);
        if (jc.isCancelled())
            return null;
        cacheService.putImageData(mPath, mTimeModified, mType, texture.getData().array());
        return texture;
    }

    public abstract Bitmap onDecodeOriginal(JobContext jc, int targetSize);
}
