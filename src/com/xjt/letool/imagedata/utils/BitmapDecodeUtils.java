
package com.xjt.letool.imagedata.utils;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.os.Build;
import android.util.FloatMath;

import com.xjt.letool.common.ApiHelper;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.ThreadPool.CancelListener;
import com.xjt.letool.common.ThreadPool.JobContext;
import com.xjt.letool.metadata.MediaItem;
import com.xjt.letool.utils.Utils;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;

public class BitmapDecodeUtils {

    private static final String TAG = "BitmapDecodeUtils";

    private static class DecodeCanceller implements CancelListener {

        Options mOptions;

        public DecodeCanceller(Options options) {
            mOptions = options;
        }

        @Override
        public void onCancel() {
            mOptions.requestCancelDecode();
        }
    }

    @TargetApi(ApiHelper.VERSION_CODES.HONEYCOMB)
    public static void setOptionsMutable(Options options) {
        if (ApiHelper.HAS_OPTIONS_IN_MUTABLE)
            options.inMutable = true;
    }

    public static Bitmap decode(JobContext jc, FileDescriptor fd, Options options) {
        if (options == null)
            options = new Options();
        jc.setCancelListener(new DecodeCanceller(options));
        setOptionsMutable(options);
        return ensureGLCompatibleBitmap(BitmapFactory.decodeFileDescriptor(fd, null, options));
    }

    public static void decodeBounds(JobContext jc, FileDescriptor fd, Options options) {
        Utils.assertTrue(options != null);
        options.inJustDecodeBounds = true;
        jc.setCancelListener(new DecodeCanceller(options));
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        options.inJustDecodeBounds = false;
    }

    public static Bitmap decode(JobContext jc, byte[] bytes, Options options) {
        return decode(jc, bytes, 0, bytes.length, options);
    }

    public static Bitmap decode(JobContext jc, byte[] bytes, int offset, int length, Options options) {
        if (options == null)
            options = new Options();
        jc.setCancelListener(new DecodeCanceller(options));
        setOptionsMutable(options);
        Bitmap org = BitmapFactory.decodeByteArray(bytes, offset, length, options);
        Bitmap b = ensureGLCompatibleBitmap(org);
        return b;
    }

    public static void decodeBounds(JobContext jc, byte[] bytes, int offset, int length, Options options) {
        Utils.assertTrue(options != null);
        options.inJustDecodeBounds = true;
        jc.setCancelListener(new DecodeCanceller(options));
        BitmapFactory.decodeByteArray(bytes, offset, length, options);
        options.inJustDecodeBounds = false;
    }

    public static Bitmap decodeThumbnail(JobContext jc, String filePath, Options options, int targetSize, int type) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            FileDescriptor fd = fis.getFD();

            return decodeThumbnail(jc, fd, options, targetSize, type);
        } catch (Exception ex) {
            LLog.w(TAG, ex);
            return null;
        } finally {
            Utils.closeSilently(fis);
        }
    }

    public static Bitmap decodeThumbnail(JobContext jc, FileDescriptor fd, Options options, int targetSize, int type) {
        if (options == null)
            options = new Options();
        jc.setCancelListener(new DecodeCanceller(options));

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        if (jc.isCancelled())
            return null;

        int w = options.outWidth;
        int h = options.outHeight;
        if (type == MediaItem.TYPE_MICROTHUMBNAIL) {
            // We center-crop the original image as it's micro thumbnail. In this case, we want to make sure the shorter side >= "targetSize".
            float scale = (float) targetSize / Math.min(w, h);
            options.inSampleSize = BitmapUtils.computeSampleSizeLarger(scale);

            // For an extremely wide image, e.g. 300x30000, we may got OOM when decoding it for TYPE_MICROTHUMBNAIL. So we add a max number of pixels limit here.
            final int MAX_PIXEL_COUNT = 640000; // 400 x 1600
            if ((w / options.inSampleSize) * (h / options.inSampleSize) > MAX_PIXEL_COUNT) {
                options.inSampleSize = BitmapUtils.computeSampleSize(FloatMath.sqrt((float) MAX_PIXEL_COUNT / (w * h)));
            }
        } else {
            // For screen nail, we only want to keep the longer side >= targetSize.
            float scale = (float) targetSize / Math.max(w, h);
            options.inSampleSize = BitmapUtils.computeSampleSizeLarger(scale);
        }

        options.inJustDecodeBounds = false;
        setOptionsMutable(options);
        Bitmap result = BitmapFactory.decodeFileDescriptor(fd, null, options);
        if (result == null)
            return null;

        // We need to resize down if the decoder does not support inSampleSize (For example, GIF images)
        float scale = (float) targetSize / (type == MediaItem.TYPE_MICROTHUMBNAIL
                ? Math.min(result.getWidth(), result.getHeight())
                : Math.max(result.getWidth(), result.getHeight()));

        if (scale <= 0.5)
            result = BitmapUtils.resizeBitmapByScale(result, scale, true);
        return ensureGLCompatibleBitmap(result);
    }

    /**
     * Decodes the bitmap from the given byte array if the image size is larger than the given requirement.
     * Note: The returned image may be resized down. However, both width and height must be
     * larger than the <code>targetSize</code>.
     */
    public static Bitmap decodeIfBigEnough(JobContext jc, byte[] data, Options options, int targetSize) {
        if (options == null)
            options = new Options();
        jc.setCancelListener(new DecodeCanceller(options));

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        if (jc.isCancelled())
            return null;
        if (options.outWidth < targetSize || options.outHeight < targetSize) {
            return null;
        }
        options.inSampleSize = BitmapUtils.computeSampleSizeLarger(
                options.outWidth, options.outHeight, targetSize);
        options.inJustDecodeBounds = false;
        setOptionsMutable(options);

        return ensureGLCompatibleBitmap(BitmapFactory.decodeByteArray(data, 0, data.length, options));
    }

    // TODO: This function should not be called directly from DecodeUtils.requestDecode(...), since we don't have the knowledge
    // if the bitmap will be uploaded to GL.
    public static Bitmap ensureGLCompatibleBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.getConfig() != null)
            return bitmap;
        Bitmap newBitmap = bitmap.copy(Config.ARGB_8888, false);
        bitmap.recycle();
        return newBitmap;
    }

    public static BitmapRegionDecoder createBitmapRegionDecoder(JobContext jc, byte[] bytes, int offset, int length,
            boolean shareable) {
        if (offset < 0 || length <= 0 || offset + length > bytes.length) {
            throw new IllegalArgumentException(String.format("offset = %s, length = %s, bytes = %s", offset, length, bytes.length));
        }
        try {
            return BitmapRegionDecoder.newInstance(bytes, offset, length, shareable);
        } catch (Throwable t) {
            LLog.w(TAG, t);
            return null;
        }
    }

    public static BitmapRegionDecoder createBitmapRegionDecoder(JobContext jc, String filePath, boolean shareable) {
        try {
            return BitmapRegionDecoder.newInstance(filePath, shareable);
        } catch (Throwable t) {
            LLog.w(TAG, t);
            return null;
        }
    }

    public static BitmapRegionDecoder createBitmapRegionDecoder(JobContext jc, FileDescriptor fd, boolean shareable) {
        try {
            return BitmapRegionDecoder.newInstance(fd, shareable);
        } catch (Throwable t) {
            LLog.w(TAG, t);
            return null;
        }
    }

    public static BitmapRegionDecoder createBitmapRegionDecoder(JobContext jc, InputStream is, boolean shareable) {
        try {
            return BitmapRegionDecoder.newInstance(is, shareable);
        } catch (Throwable t) {
            // We often cancel the creating of bitmap region decoder,
            // so just log one line.
            LLog.w(TAG, "requestCreateBitmapRegionDecoder: " + t);
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Bitmap decodeUsingPool(JobContext jc, byte[] data, int offset, int length, BitmapFactory.Options options) {

        if (options == null)
            options = new BitmapFactory.Options();
        if (options.inSampleSize < 1)
            options.inSampleSize = 1;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inBitmap = (options.inSampleSize == 1) ? findCachedBitmap(jc, data, offset, length, options) : null;
        try {
            Bitmap bitmap = decode(jc, data, offset, length, options);
            if (options.inBitmap != null && options.inBitmap != bitmap) {
                LetoolBitmapPool.getInstance().put(options.inBitmap);
                options.inBitmap = null;
            }
            //b = bitmap;
            return bitmap;
        } catch (IllegalArgumentException e) {
            if (options.inBitmap == null)
                throw e;

            LLog.w(TAG, "decode fail with a given bitmap, try decode to a new bitmap");
            LetoolBitmapPool.getInstance().put(options.inBitmap);
            options.inBitmap = null;
            return decode(jc, data, offset, length, options);
        }
    }

    // This is the same as the method above except the source data comes from a file descriptor instead of a byte array.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Bitmap decodeUsingPool(JobContext jc, FileDescriptor fileDescriptor, Options options) {

        if (options == null)
            options = new BitmapFactory.Options();
        if (options.inSampleSize < 1)
            options.inSampleSize = 1;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inBitmap = (options.inSampleSize == 1) ? findCachedBitmap(jc, fileDescriptor, options) : null;
        try {
            Bitmap bitmap = BitmapDecodeUtils.decode(jc, fileDescriptor, options);
            if (options.inBitmap != null && options.inBitmap != bitmap) {
                LetoolBitmapPool.getInstance().put(options.inBitmap);
                options.inBitmap = null;
            }
            return bitmap;
        } catch (IllegalArgumentException e) {
            if (options.inBitmap == null)
                throw e;

            LLog.w(TAG, "decode fail with a given bitmap, try decode to a new bitmap");
            LetoolBitmapPool.getInstance().put(options.inBitmap);
            options.inBitmap = null;
            return decode(jc, fileDescriptor, options);
        }
    }

    private static Bitmap findCachedBitmap(JobContext jc, byte[] data, int offset, int length, Options options) {
        decodeBounds(jc, data, offset, length, options);
        return LetoolBitmapPool.getInstance().get(options.outWidth, options.outHeight);
    }

    private static Bitmap findCachedBitmap(JobContext jc, FileDescriptor fileDescriptor,
            Options options) {
        decodeBounds(jc, fileDescriptor, options);
        return LetoolBitmapPool.getInstance().get(options.outWidth, options.outHeight);
    }
}
