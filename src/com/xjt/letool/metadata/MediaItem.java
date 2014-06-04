package com.xjt.letool.metadata;

import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.opengl.ETC1Util.ETC1Texture;

import com.xjt.letool.common.LLog;
import com.xjt.letool.common.ThreadPool.Job;
import com.xjt.letool.imagedata.dbcache.DataBaseCache;
import com.xjt.letool.imagedata.utils.BytesBufferPool;
import com.xjt.letool.views.opengl.ScreenNail;

public abstract class MediaItem extends MediaObject {

    private static final String TAG = MediaItem.class.getSimpleName();

    public static final int TYPE_THUMBNAIL = 1;
    public static final int TYPE_MICROTHUMBNAIL = 2;

    public static final int CACHED_IMAGE_QUALITY = 95;

    public static final int IMAGE_READY = 0;
    public static final int IMAGE_WAIT = 1;
    public static final int IMAGE_ERROR = -1;

    public static final String MIME_TYPE_JPEG = "image/jpeg";

    private static final int BYTESBUFFE_POOL_SIZE = 4;
    private static final int BYTESBUFFER_SIZE = 200 * 1024;
    private static final BytesBufferPool sMicroThumbBufferPool = new BytesBufferPool(BYTESBUFFE_POOL_SIZE, BYTESBUFFER_SIZE);
    private static int sMicrothumbnailTargetSize = 128;
    private static int sThumbnailTargetSize = 640;

    public static final double INVALID_LATLNG = 0f;

    public abstract Job<Bitmap> requestImage(int type);

    public abstract Job<Bitmap> requestImage(int type, int index, long dateTaken, DataBaseCache loader);

    public abstract Job<ETC1Texture> requestImage(int type, int extra);

    public abstract Job<BitmapRegionDecoder> requestLargeImage();

    public MediaItem(MediaPath path, long version) {
        super(path, version);
    }

    public long getDateInMs() {
        return 0;
    }

    public String getName() {
        return null;
    }

    public void getLatLong(double[] latLong) {
        latLong[0] = INVALID_LATLNG;
        latLong[1] = INVALID_LATLNG;
    }

    public String[] getTags() {
        return null;
    }

    public int getFullImageRotation() {
        return getRotation();
    }

    public int getRotation() {
        return 0;
    }

    public long getSize() {
        return 0;
    }

    public abstract String getMimeType();

    public String getFilePath() {
        return "";
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public ScreenNail getScreenNail() {
        return null;
    }

    public static int getTargetSize(int type) {
        switch (type) {
            case TYPE_THUMBNAIL:
                return sThumbnailTargetSize;
            case TYPE_MICROTHUMBNAIL:
                return sMicrothumbnailTargetSize;
            default:
                throw new RuntimeException("should only request thumb/microthumb from cache");
        }
    }

    public static BytesBufferPool getBytesBufferPool() {
        return sMicroThumbBufferPool;
    }

    public static void setThumbnailSizes(int size, int microSize) {
        sThumbnailTargetSize = size;
        if (sMicrothumbnailTargetSize != microSize) {
            sMicrothumbnailTargetSize = Math.abs(microSize - 128) > Math.abs(microSize - 256) ? 256 : 128;
        }
        LLog.i(TAG, " MediaItem.sThumbnailTargetSize:" + sThumbnailTargetSize + " MediaItem.sMicrothumbnailTargetSize:" + sMicrothumbnailTargetSize);
    }
}
