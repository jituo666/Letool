
package com.xjt.letool.imagedata.blobcache;

import android.content.Context;

import com.xjt.letool.imagedata.blobcache.BlobCache.LookupRequest;
import com.xjt.letool.imagedata.utils.BytesBufferPool.BytesBuffer;
import com.xjt.letool.metadata.MediaPath;
import com.xjt.letool.utils.LetoolUtils;
import com.xjt.letool.utils.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;

public class BlobCacheService {

    @SuppressWarnings("unused")
    private static final String TAG = BlobCacheService.class.getSimpleName();

    private static final String IMAGE_CACHE_FILE = "imgcache";
    private static final int IMAGE_CACHE_MAX_ENTRIES = 5000;
    private static final int IMAGE_CACHE_MAX_BYTES = 200 * 1024 * 1024;
    private static final int IMAGE_CACHE_VERSION = 7;

    private BlobCache mCache;

    public BlobCacheService(Context context) {
        mCache = BlobCacheManager.getCache(context, IMAGE_CACHE_FILE, IMAGE_CACHE_MAX_ENTRIES,
                IMAGE_CACHE_MAX_BYTES, IMAGE_CACHE_VERSION);
    }

    /**
     * Gets the cached image data for the given <code>path</code>,
     *  <code>timeModified</code> and <code>type</code>.
     * The image data will be stored in <code>buffer.data</code>, started from
     * <code>buffer.offset</code> for <code>buffer.length</code> bytes. If the
     * buffer.data is not big enough, a new byte array will be allocated and returned.
     * @return true if the image data is found; false if not found.
     */
    public boolean getImageData(MediaPath path, long timeModified, int type, BytesBuffer buffer) {
        byte[] key = makeKey(path, timeModified, type);
        long cacheKey = Utils.crc64Long(key);
        try {
            LookupRequest request = new LookupRequest();
            request.key = cacheKey;
            request.buffer = buffer.data;
            synchronized (mCache) {
                if (!mCache.lookup(request))
                    return false;
            }
            if (isSameKey(key, request.buffer)) {
                buffer.data = request.buffer;
                buffer.offset = key.length;
                buffer.length = request.length - buffer.offset;
                return true;
            }
        } catch (IOException ex) {
            // ignore.
        }
        return false;
    }

    public void putImageData(MediaPath path, long timeModified, int type, byte[] value) {
        byte[] key = makeKey(path, timeModified, type);
        long cacheKey = Utils.crc64Long(key);
        ByteBuffer buffer = ByteBuffer.allocate(key.length + value.length);
        buffer.put(key);
        buffer.put(value);
        synchronized (mCache) {
            try {
                mCache.insert(cacheKey, buffer.array());
            } catch (IOException ex) {
                // ignore.
            }
        }
    }

    public void clearImageData(MediaPath path, long timeModified, int type) {
        byte[] key = makeKey(path, timeModified, type);
        long cacheKey = Utils.crc64Long(key);
        synchronized (mCache) {
            try {
                mCache.clearEntry(cacheKey);
            } catch (IOException ex) {
                // ignore.
            }
        }
    }

    private static byte[] makeKey(MediaPath path, long timeModified, int type) {
        return LetoolUtils.getBytes(path.toString() + "+" + timeModified + "+" + type);
    }

    private static boolean isSameKey(byte[] key, byte[] buffer) {
        int n = key.length;
        if (buffer.length < n) {
            return false;
        }
        for (int i = 0; i < n; ++i) {
            if (key[i] != buffer[i]) {
                return false;
            }
        }
        return true;
    }
}
