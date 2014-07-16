
package com.xjt.letool.utils;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.ConditionVariable;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.xjt.letool.R;
import com.xjt.letool.activities.LocalMediaBrowseActivity;
import com.xjt.letool.common.ApiHelper;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.PackagesMonitor;
import com.xjt.letool.common.ThreadPool.CancelListener;
import com.xjt.letool.common.ThreadPool.JobContext;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.metadata.MediaItem;
import com.xjt.letool.views.opengl.TiledScreenNail;

public class LetoolUtils {

    private static final String TAG = LetoolUtils.class.getSimpleName();

    public static final String MIME_TYPE_IMAGE = "image/*";
    public static final String MIME_TYPE_VIDEO = "video/*";
    public static final String MIME_TYPE_ALL = "*/*";

    private static final String MAPS_PACKAGE_NAME = "com.google.android.apps.maps";
    private static final String MAPS_CLASS_NAME = "com.google.android.maps.MapsActivity";

    private static final String DIR_TYPE_IMAGE = "vnd.android.cursor.dir/image";
    private static final String DIR_TYPE_VIDEO = "vnd.android.cursor.dir/video";

    private static final String PREFIX_PHOTO_EDITOR_UPDATE = "editor-update-";
    private static final String PREFIX_HAS_PHOTO_EDITOR = "has-editor-";

    private static final String KEY_CAMERA_UPDATE = "camera-update";
    private static final String KEY_HAS_CAMERA = "has-camera";

    private static float sPixelDensity = -1f;
    private static boolean sCameraAvailableInitialized = false;
    private static boolean sCameraAvailable;

    public static void initialize(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        sPixelDensity = metrics.density;
        LLog.i("letooltag", " resume sPixelDensity:" + sPixelDensity);
        Resources r = context.getResources();
        TiledScreenNail.setPlaceholderColor(r.getColor(R.color.bitmap_screennail_placeholder));
        initializeThumbnailSizes(metrics, r);
    }

    private static void initializeThumbnailSizes(DisplayMetrics metrics, Resources r) {
        int maxPixels = Math.min(metrics.heightPixels, metrics.widthPixels);
        // For screen-nails, we never need to completely fill the screen
        MediaItem.setThumbnailSizes(maxPixels / 3 * 2, maxPixels / 3);
        TiledScreenNail.setMaxSide(maxPixels / 2);
    }

    public static float[] intColorToFloatARGBArray(int from) {
        return new float[] {
                Color.alpha(from) / 255f,
                Color.red(from) / 255f,
                Color.green(from) / 255f,
                Color.blue(from) / 255f
        };
    }

    public static float dpToPixel(float dp) {
        return sPixelDensity * dp;
    }

    public static int dpToPixel(int dp) {
        return Math.round(dpToPixel((float) dp));
    }

    public static int meterToPixel(float meter) {
        // 1 meter = 39.37 inches, 1 inch = 160 dp.
        return Math.round(dpToPixel(meter * 39.37f * 160));
    }

    public static byte[] getBytes(String in) {
        byte[] result = new byte[in.length() * 2];
        int output = 0;
        for (char ch : in.toCharArray()) {
            result[output++] = (byte) (ch & 0xFF);
            result[output++] = (byte) (ch >> 8);
        }
        return result;
    }

    // Below are used the detect using database in the render thread. It only
    // works most of the time, but that's ok because it's for debugging only.

    private static volatile Thread sCurrentThread;
    private static volatile boolean sWarned;

    public static void setRenderThread() {
        sCurrentThread = Thread.currentThread();
    }

    public static void assertNotInRenderThread() {
        if (!sWarned) {
            if (Thread.currentThread() == sCurrentThread) {
                sWarned = true;
                LLog.w(TAG, new Throwable("Should not do this in render thread"));
            }
        }
    }

    private static final double RAD_PER_DEG = Math.PI / 180.0;
    private static final double EARTH_RADIUS_METERS = 6367000.0;

    public static double fastDistanceMeters(double latRad1, double lngRad1,
            double latRad2, double lngRad2) {
        if ((Math.abs(latRad1 - latRad2) > RAD_PER_DEG)
                || (Math.abs(lngRad1 - lngRad2) > RAD_PER_DEG)) {
            return accurateDistanceMeters(latRad1, lngRad1, latRad2, lngRad2);
        }
        // Approximate sin(x) = x.
        double sineLat = (latRad1 - latRad2);

        // Approximate sin(x) = x.
        double sineLng = (lngRad1 - lngRad2);

        // Approximate cos(lat1) * cos(lat2) using
        // cos((lat1 + lat2)/2) ^ 2
        double cosTerms = Math.cos((latRad1 + latRad2) / 2.0);
        cosTerms = cosTerms * cosTerms;
        double trigTerm = sineLat * sineLat + cosTerms * sineLng * sineLng;
        trigTerm = Math.sqrt(trigTerm);

        // Approximate arcsin(x) = x
        return EARTH_RADIUS_METERS * trigTerm;
    }

    public static double accurateDistanceMeters(double lat1, double lng1,
            double lat2, double lng2) {
        double dlat = Math.sin(0.5 * (lat2 - lat1));
        double dlng = Math.sin(0.5 * (lng2 - lng1));
        double x = dlat * dlat + dlng * dlng * Math.cos(lat1) * Math.cos(lat2);
        return (2 * Math.atan2(Math.sqrt(x), Math.sqrt(Math.max(0.0, 1.0 - x)))) * EARTH_RADIUS_METERS;
    }

    public static final double toMile(double meter) {
        return meter / 1609;
    }

    // For debugging, it will block the caller for timeout millis.
    public static void fakeBusy(JobContext jc, int timeout) {
        final ConditionVariable cv = new ConditionVariable();
        jc.setCancelListener(new CancelListener() {
            @Override
            public void onCancel() {
                cv.open();
            }
        });
        cv.block(timeout);
        jc.setCancelListener(null);
    }

    public static boolean isEditorAvailable(Context context, String mimeType) {
        int version = PackagesMonitor.getPackagesVersion(context);

        String updateKey = PREFIX_PHOTO_EDITOR_UPDATE + mimeType;
        String hasKey = PREFIX_HAS_PHOTO_EDITOR + mimeType;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getInt(updateKey, 0) != version) {
            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> infos = packageManager.queryIntentActivities(
                    new Intent(Intent.ACTION_EDIT).setType(mimeType), 0);
            prefs.edit().putInt(updateKey, version)
                    .putBoolean(hasKey, !infos.isEmpty())
                    .commit();
        }

        return prefs.getBoolean(hasKey, true);
    }

    public static boolean isAnyCameraAvailable(Context context) {
        int version = PackagesMonitor.getPackagesVersion(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getInt(KEY_CAMERA_UPDATE, 0) != version) {
            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> infos = packageManager.queryIntentActivities(
                    new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA), 0);
            prefs.edit().putInt(KEY_CAMERA_UPDATE, version)
                    .putBoolean(KEY_HAS_CAMERA, !infos.isEmpty())
                    .commit();
        }
        return prefs.getBoolean(KEY_HAS_CAMERA, true);
    }

    public static boolean isCameraAvailable(Context context) {
        if (sCameraAvailableInitialized)
            return sCameraAvailable;
        PackageManager pm = context.getPackageManager();
        Intent cameraIntent = IntentHelper.getCameraIntent(context);
        List<ResolveInfo> apps = pm.queryIntentActivities(cameraIntent, 0);
        sCameraAvailableInitialized = true;
        sCameraAvailable = !apps.isEmpty();
        return sCameraAvailable;
    }

    public static void startCameraActivity(Context context) {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // This will only occur if Camera was disabled while Gallery is open
            // since we cache our availability check. Just abort the attempt.
            LLog.e(TAG, "Camera activity previously detected but cannot be found", e);
        }
    }

    public static void startGalleryActivity(Context context) {
        Intent intent = new Intent(context, LocalMediaBrowseActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean isValidLocation(double latitude, double longitude) {
        // TODO: change || to && after we fix the default location issue
        return true;//(latitude != MediaItem.INVALID_LATLNG || longitude != MediaItem.INVALID_LATLNG);
    }

    public static String formatLatitudeLongitude(String format, double latitude,
            double longitude) {
        // We need to specify the locale otherwise it may go wrong in some language
        // (e.g. Locale.FRENCH)
        return String.format(Locale.ENGLISH, format, latitude, longitude);
    }

    public static void showOnMap(Context context, double latitude, double longitude) {
        try {
            // We don't use "geo:latitude,longitude" because it only centers
            // the MapView to the specified location, but we need a marker
            // for further operations (routing to/from).
            // The q=(lat, lng) syntax is suggested by geo-team.
            String uri = formatLatitudeLongitude("http://maps.google.com/maps?f=q&q=(%f,%f)",
                    latitude, longitude);
            ComponentName compName = new ComponentName(MAPS_PACKAGE_NAME,
                    MAPS_CLASS_NAME);
            Intent mapsIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(uri)).setComponent(compName);
            context.startActivity(mapsIntent);
        } catch (ActivityNotFoundException e) {
            // Use the "geo intent" if no GMM is installed
            LLog.e(TAG, "GMM activity not found!", e);
            String url = formatLatitudeLongitude("geo:%f,%f", latitude, longitude);
            Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(mapsIntent);
        }
    }

    public static void setViewPointMatrix(float matrix[], float x, float y, float z) {
        // The matrix is
        // -z,  0,  x,  0
        //  0, -z,  y,  0
        //  0,  0,  1,  0
        //  0,  0,  1, -z
        Arrays.fill(matrix, 0, 16, 0);
        matrix[0] = matrix[5] = matrix[15] = -z;
        matrix[8] = x;
        matrix[9] = y;
        matrix[10] = matrix[11] = 1;
    }

    public static int getBucketId(String path) {
        return path.toLowerCase().hashCode();
    }

    // Return the local path that matches the given bucketId. If no match is found, return null
    public static String searchDirForPath(File dir, int bucketId) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String path = file.getAbsolutePath();
                    if (LetoolUtils.getBucketId(path) == bucketId) {
                        return path;
                    } else {
                        path = searchDirForPath(file, bucketId);
                        if (path != null)
                            return path;
                    }
                }
            }
        }
        return null;
    }

    // Returns a (localized) string for the given duration (in seconds).
    public static String formatDuration(final Context context, int duration) {
        int h = duration / 3600;
        int m = (duration - h * 3600) / 60;
        int s = duration - (h * 3600 + m * 60);
        String durationValue;
        if (h == 0) {
            durationValue = String.format(context.getString(R.string.details_ms), m, s);
        } else {
            durationValue = String.format(context.getString(R.string.details_hms), h, m, s);
        }
        return durationValue;
    }

    @TargetApi(ApiHelper.VERSION_CODES.HONEYCOMB)
    public static int determineTypeBits(Context context, Intent intent) {
        int typeBits = 0;
        String type = intent.resolveType(context);

        if (MIME_TYPE_ALL.equals(type)) {
            typeBits = DataManager.INCLUDE_ALL;
        } else if (MIME_TYPE_IMAGE.equals(type) ||
                DIR_TYPE_IMAGE.equals(type)) {
            typeBits = DataManager.INCLUDE_IMAGE;
        } else if (MIME_TYPE_VIDEO.equals(type) ||
                DIR_TYPE_VIDEO.equals(type)) {
            typeBits = DataManager.INCLUDE_VIDEO;
        } else {
            typeBits = DataManager.INCLUDE_ALL;
        }

        if (ApiHelper.HAS_INTENT_EXTRA_LOCAL_ONLY) {
            if (intent.getBooleanExtra(Intent.EXTRA_LOCAL_ONLY, false)) {
                typeBits |= DataManager.INCLUDE_LOCAL_ONLY;
            }
        }

        return typeBits;
    }

    public static boolean hasSpaceForSize(long size) {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return false;
        }

        String path = Environment.getExternalStorageDirectory().getPath();
        try {
            StatFs stat = new StatFs(path);
            return stat.getAvailableBlocks() * (long) stat.getBlockSize() > size;
        } catch (Exception e) {
            LLog.i(TAG, "Fail to access external storage", e);
        }
        return false;
    }

    //for debug 
    public static void printfSystemDirs() {

        LLog.i(TAG, "================UtilStorage o:" + UtilStorage.getInstance().getExternalSdCardPath() + "/DCIM/Camera");
        LLog.i(TAG, "================UtilStorage i:" + UtilStorage.getInstance().getInnerSdCardPath() + "/DCIM/Camera");
        LLog.i(TAG, "================getRootDirectory:" + Environment.getRootDirectory().toString());
        LLog.i(TAG, "================getDataDirectory:" + Environment.getDataDirectory().toString());
        LLog.i(TAG, "================getExternalStorageDirectory:" + Environment.getExternalStorageDirectory().toString());
        LLog.i(TAG, "================getExternalStorageState:" + Environment.getExternalStorageState());
        LLog.i(TAG, "================getDownloadCacheDirectory:" + Environment.getDownloadCacheDirectory().toString());
        //
        LLog.i(TAG, "================DIRECTORY_DCIM:" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString());
        LLog.i(TAG, "================DIRECTORY_PICTURES:" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString());
        LLog.i(TAG, "================DIRECTORY_DOWNLOADS:" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
        LLog.i(TAG, "================DIRECTORY_MOVIES:" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString());
        LLog.i(TAG, "================DIRECTORY_MUSIC:" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString());

    }
}
