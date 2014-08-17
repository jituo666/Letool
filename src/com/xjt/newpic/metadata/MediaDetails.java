
package com.xjt.newpic.metadata;

import android.annotation.TargetApi;
import android.media.ExifInterface;
import android.os.Build;

import com.xjt.newpic.R;
import com.xjt.newpic.common.ApiHelper;
import com.xjt.newpic.common.LLog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

public class MediaDetails implements Iterable<Entry<Integer, Object>> {

    @SuppressWarnings("unused")
    private static final String TAG = "MediaDetails";

    private TreeMap<Integer, Object> mDetails = new TreeMap<Integer, Object>();
    private HashMap<Integer, Integer> mUnits = new HashMap<Integer, Integer>();

    //    public static final int INDEX_TITLE = 1;
    public static final int INDEX_DESCRIPTION = 2;
    public static final int INDEX_DATETIME = 3;
    public static final int INDEX_LOCATION = 4;
    public static final int INDEX_WIDTH = 5;
    public static final int INDEX_HEIGHT = 6;
    public static final int INDEX_ORIENTATION = 7;
    public static final int INDEX_DURATION = 8;
    public static final int INDEX_MIMETYPE = 9;
    public static final int INDEX_SIZE = 10;

    // for EXIF
    public static final int INDEX_MAKE = 100;
    public static final int INDEX_MODEL = 101;
    public static final int INDEX_FLASH = 102;
    public static final int INDEX_FOCAL_LENGTH = 103;
    public static final int INDEX_WHITE_BALANCE = 104;
    public static final int INDEX_APERTURE = 105;
    public static final int INDEX_SHUTTER_SPEED = 106;
    public static final int INDEX_EXPOSURE_TIME = 107;
    public static final int INDEX_ISO = 108;

    // Put this last because it may be long.
    public static final int INDEX_PATH = 200;

    public static class FlashState {

        private static int FLASH_FIRED_MASK = 1;
        private static int FLASH_RETURN_MASK = 2 | 4;
        private static int FLASH_MODE_MASK = 8 | 16;
        private static int FLASH_FUNCTION_MASK = 32;
        private static int FLASH_RED_EYE_MASK = 64;
        private int mState;

        public FlashState(int state) {
            mState = state;
        }

        public boolean isFlashFired() {
            return (mState & FLASH_FIRED_MASK) != 0;
        }
    }

    public void addDetail(int index, Object value) {
        mDetails.put(index, value);
    }

    public Object getDetail(int index) {
        return mDetails.get(index);
    }

    public int size() {
        return mDetails.size();
    }

    public Iterator<Entry<Integer, Object>> iterator() {
        return mDetails.entrySet().iterator();
    }

    public void setUnit(int index, int unit) {
        mUnits.put(index, unit);
    }

    public boolean hasUnit(int index) {
        return mUnits.containsKey(index);
    }

    public int getUnit(int index) {
        return mUnits.get(index);
    }

    private static void setExifData(MediaDetails details, ExifInterface exif, String tag,
            int key) {
        String value = exif.getAttribute(tag);
        if (value != null) {
            if (key == MediaDetails.INDEX_FLASH) {
                MediaDetails.FlashState state = new MediaDetails.FlashState(
                        Integer.valueOf(value.toString()));
                details.addDetail(key, state);
            } else {
                details.addDetail(key, value);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void extractExifInfo(MediaDetails details, String filePath) {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            setExifData(details, exif, ExifInterface.TAG_FLASH, MediaDetails.INDEX_FLASH);
            setExifData(details, exif, ExifInterface.TAG_IMAGE_WIDTH, MediaDetails.INDEX_WIDTH);
            setExifData(details, exif, ExifInterface.TAG_IMAGE_LENGTH,MediaDetails.INDEX_HEIGHT);
            setExifData(details, exif, ExifInterface.TAG_MAKE, MediaDetails.INDEX_MAKE);
            setExifData(details, exif, ExifInterface.TAG_MODEL, MediaDetails.INDEX_MODEL);
            if (ApiHelper.AT_LEAST_11) {
                setExifData(details, exif, ExifInterface.TAG_APERTURE, MediaDetails.INDEX_APERTURE);
                setExifData(details, exif, ExifInterface.TAG_ISO, MediaDetails.INDEX_ISO);
                setExifData(details, exif, ExifInterface.TAG_EXPOSURE_TIME, MediaDetails.INDEX_EXPOSURE_TIME);
            }
            setExifData(details, exif, ExifInterface.TAG_WHITE_BALANCE, MediaDetails.INDEX_WHITE_BALANCE);

            double data = exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0);
            if (data != 0f) {
                details.addDetail(MediaDetails.INDEX_FOCAL_LENGTH, data);
                details.setUnit(MediaDetails.INDEX_FOCAL_LENGTH, R.string.unit_mm);
            }
        } catch (IOException ex) {
            // ignore it.
            LLog.w(TAG, "", ex);
        }
    }
}
