
package com.xjt.letool.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class GlobalPreference {

    private static SharedPreferences sPrefs = null;
    private static final String PREFS_KEY_APP_UPDATE = "last_app_update_time";
    private static final String PREFS_KEY_APP_CHECK_UPDATE = "last_app_update_check_time";
    private static final String PREFS_KEY_CAMERA_SOURCE_DIRS = "camera_source_dirs";

    private static SharedPreferences initSharedPreferences(Context ctx) {
        if (sPrefs == null) {
            sPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        }
        return sPrefs;
    }

    public static long getLastAppUpdateCheckTime(Context ctx) {
        SharedPreferences prefs = initSharedPreferences(ctx);
        return prefs.getLong(PREFS_KEY_APP_CHECK_UPDATE, 0);
    }

    public static void setAppUpdateCheckTime(Context ctx, long defTime) {
        SharedPreferences prefs = initSharedPreferences(ctx);
        SharedPreferencesCompat.apply(prefs.edit().putLong(PREFS_KEY_APP_CHECK_UPDATE, defTime));
    }

    public static long getLastAppUpdateTime(Context ctx) {
        SharedPreferences prefs = initSharedPreferences(ctx);
        return prefs.getLong(PREFS_KEY_APP_UPDATE, 0);
    }

    public static void setAppUpdateTime(Context ctx, long defTime) {
        SharedPreferences prefs = initSharedPreferences(ctx);
        SharedPreferencesCompat.apply(prefs.edit().putLong(PREFS_KEY_APP_UPDATE, defTime));
    }

    public static String getPhotoDirs(Context ctx) {
        SharedPreferences prefs = initSharedPreferences(ctx);
        return prefs.getString(PREFS_KEY_CAMERA_SOURCE_DIRS, "");
    }

    public static void setPhotoDirs(Context ctx, String dirs) {
        SharedPreferences prefs = initSharedPreferences(ctx);
        SharedPreferencesCompat.apply(prefs.edit().putString(PREFS_KEY_CAMERA_SOURCE_DIRS, dirs));
    }
}
