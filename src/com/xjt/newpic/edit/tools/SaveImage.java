package com.xjt.newpic.edit.tools;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.android.gallery3d.common.Utils;
import com.xjt.newpic.edit.NpEditActivity;
import com.xjt.newpic.edit.cache.ImageLoader;
import com.xjt.newpic.edit.filters.FiltersManager;
import com.xjt.newpic.edit.pipeline.CachingPipeline;
import com.xjt.newpic.edit.pipeline.ImagePreset;
import com.xjt.newpic.edit.pipeline.ProcessingService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.concurrent.CountDownLatch;

/**
 * Handles saving edited photo
 */
public class SaveImage {

    private static final String TAG = SaveImage.class.getSimpleName();

    /**
     * Callback for updates
     */
    public interface Callback {

        void onPreviewSaved(Uri uri);

        void onProgress(int max, int current);
    }

    public interface ContentResolverQueryCallback {

        void onCursorResult(Cursor cursor);
    }

    private static final int SAVE_IMAGE_QUALITY = 90;
    private static final String TIME_STAMP_NAME = "_yyyyMMdd_HHmmss";
    private static final String PREFIX_IMG = "NP_IMG";
    private static final String POSTFIX_JPG = ".jpg";

    private final Context mContext;
    private final Callback mCallback;
    private final File mDestinationFile;
    private final Uri mSelectedImageUri;

    private int mCurrentProcessingStep = 1;

    public static final int MAX_PROCESSING_STEPS = 6;
    public static final String DEFAULT_SAVE_DIRECTORY = "NewPicSaveDir";

    public SaveImage(Context context, Uri selectedImageUri, Bitmap previewImage, Callback callback) {
        mContext = context;
        mSelectedImageUri = selectedImageUri;
        mDestinationFile = getNewFile(context);
        mCallback = callback;
    }

    private static File getFinalSaveDirectory(Context context) {
        File saveDirectory = new File(Environment.getExternalStorageDirectory(), SaveImage.DEFAULT_SAVE_DIRECTORY);
        // Create the directory if it doesn't exist
        if (!saveDirectory.exists())
            saveDirectory.mkdirs();
        return saveDirectory;
    }

    private static File getNewFile(Context context) {
        File saveDirectory = getFinalSaveDirectory(context);
        String filename = new SimpleDateFormat(TIME_STAMP_NAME).format(new Date(System.currentTimeMillis()));
        return new File(saveDirectory, PREFIX_IMG + filename + POSTFIX_JPG);
    }

    private boolean writeImageData(File file, Bitmap image, int jpegCompressQuality) {
        boolean ret = false;
        OutputStream s = null;
        try {
            s = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, (jpegCompressQuality > 0) ? jpegCompressQuality : 1, s);
            s.flush();
            s.close();
            s = null;
            ret = true;
        } catch (FileNotFoundException e) {
            Log.w(TAG, "File not found: " + file.getAbsolutePath(), e);
        } catch (IOException e) {
            Log.w(TAG, "Could not write exif: ", e);
        } finally {
            Utils.closeSilently(s);
        }
        return ret;
    }

    private void resetProgress() {
        mCurrentProcessingStep = 0;
    }

    private void updateProgress() {
        if (mCallback != null) {
            mCallback.onProgress(MAX_PROCESSING_STEPS, ++mCurrentProcessingStep);
        }
    }


    public Uri processAndSaveImage(ImagePreset preset, int quality, float sizeFactor, boolean exit) {
        Uri uri = null;
        resetProgress();
        boolean noBitmap = true;
        int num_tries = 0;
        int sampleSize = 1;

        // Stopgap fix for low-memory devices.
        while (noBitmap) {
            try {
                updateProgress();
                // Try to do bitmap operations, downsample if low-memory
                Bitmap bitmap = ImageLoader.loadOrientedBitmapWithBackouts(mContext, mSelectedImageUri, sampleSize);
                if (bitmap == null) {
                    return null;
                }
                if (sizeFactor != 1f) {
                    // if we have a valid size
                    int w = (int) (bitmap.getWidth() * sizeFactor);
                    int h = (int) (bitmap.getHeight() * sizeFactor);
                    if (w == 0 || h == 0) {
                        w = 1;
                        h = 1;
                    }
                    bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
                }
                updateProgress();
                CachingPipeline pipeline = new CachingPipeline(FiltersManager.getManager(), "Saving");
                // 保存最后的原始图，这个可以控制内存的
                bitmap = pipeline.renderFinalImage(bitmap, preset);
                updateProgress();
                final CountDownLatch latch = new CountDownLatch(1);
                if (writeImageData(mDestinationFile, bitmap, quality)) {

                    MediaScannerConnection.scanFile(mContext,
                            new String[] {
                                mDestinationFile.getAbsolutePath()
                            },
                            null,
                            new MediaScannerConnection.OnScanCompletedListener() {

                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    latch.countDown();
                                }
                            });
                }
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                updateProgress();
                noBitmap = false;
            } catch (OutOfMemoryError e) {
                // Try 5 times before failing for good.
                if (++num_tries >= 5) {
                    throw e;
                }
                System.gc();
                sampleSize *= 2;
                resetProgress();
            }
        }
        return uri;
    }

    public static void saveImage(ImagePreset preset, final NpEditActivity filterShowActivity) {
        Uri selectedImageUri = filterShowActivity.getSelectedImageUri();
        Intent processIntent = ProcessingService.getSaveIntent(filterShowActivity, preset, selectedImageUri, SAVE_IMAGE_QUALITY, 1f, true);
        filterShowActivity.startService(processIntent);
    }

    public static void querySource(Context context, Uri sourceUri, String[] projection, ContentResolverQueryCallback callback) {
        ContentResolver contentResolver = context.getContentResolver();
        querySourceFromContentResolver(contentResolver, sourceUri, projection, callback);
    }

    private static void querySourceFromContentResolver(
            ContentResolver contentResolver, Uri sourceUri, String[] projection, ContentResolverQueryCallback callback) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(sourceUri, projection, null, null, null);
            if ((cursor != null) && cursor.moveToNext()) {
                callback.onCursorResult(cursor);
            }
        } catch (Exception e) {
            // Ignore error for lacking the data column from the source.
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
