package com.xjt.letool.imagedata.blobcache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.graphics.Bitmap;
import android.opengl.ETC1Util;
import android.opengl.ETC1Util.ETC1Texture;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.ThreadPool.Job;
import com.xjt.letool.common.ThreadPool.JobContext;
import com.xjt.letool.imagedata.utils.BitmapUtils;
import com.xjt.letool.imagedata.utils.BytesBufferPool.BytesBuffer;
import com.xjt.letool.metadata.MediaItem;
import com.xjt.letool.metadata.MediaPath;

/**
 * @Author Jituo.Xuan
 * @Date 8:18:48 PM Jul 24, 2014
 * @Comments:null
 */
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
        return mPath + "," + mTimeModified + "," + ((mType == MediaItem.TYPE_THUMBNAIL) ? "THUMB" : (mType == MediaItem.TYPE_MICROTHUMBNAIL) ? "MICROTHUMB" : "?");
    }

    @Override
    public ETC1Texture run(JobContext jc) {
        BlobCacheService cacheService = mApplication.getBlobCacheService();
        BytesBuffer buffer = MediaItem.getBytesBufferPool().get();

        try {
            boolean found = cacheService.getImageData(mPath, mTimeModified, mType, buffer);
            if (jc.isCancelled())
                return null;
            if (found) {
                return ETC1Util.createTexture(new ByteArrayInputStream(buffer.data, buffer.offset, buffer.length));
            }
        } catch (IOException e) {

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
        bitmap = bitmap.copy(Bitmap.Config.RGB_565, false);
        if (jc.isCancelled())
            return null;

        ByteBuffer bb = ByteBuffer.allocateDirect(bitmap.getRowBytes() * bitmap.getHeight()); // size is good
        bb.order(ByteOrder.nativeOrder());
        bitmap.copyPixelsToBuffer(bb);
        bb.position(0);
        ETC1Texture texture = ETC1Util.compressTexture(bb, mTargetSize, mTargetSize, 2, 2 * mTargetSize);
        if (jc.isCancelled())
            return null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ETC1Util.writeTexture(texture, os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cacheService.putImageData(mPath, mTimeModified, mType, os.toByteArray());
        return texture;
    }

    public abstract Bitmap onDecodeOriginal(JobContext jc, int targetSize);
}
