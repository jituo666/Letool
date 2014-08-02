
package com.xjt.letool.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @Author Jituo.Xuan
 * @Date 8:18:24 PM Jul 24, 2014
 * @Comments:null
 */
public class GlobalPreference {

    private static SharedPreferences sPrefs = null;
    private static final String PREFS_KEY_APP_UPDATE = "last_app_update_time";
    private static final String PREFS_KEY_APP_CHECK_UPDATE = "last_app_update_check_time";
    private static final String PREFS_KEY_CAMERA_SOURCE_DIRS = "camera_source_dirs";
    private static final String PREFS_KEY_ANIMATION_SWITCH = "animation_switch";
    private static final String PREFS_KEY_REMEMBER_LAST_UI = "remember_last_ui";
    private static final String PREFS_KEY_GUIDE_TIP_SHOW = "guide_tip_shown";

    private static final String PREFS_KEY_LAST_UI = "last_ui";

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

    public static boolean isAnimationOpen(Context ctx) {
        SharedPreferences prefs = initSharedPreferences(ctx);
        return prefs.getBoolean(PREFS_KEY_ANIMATION_SWITCH, true);
    }

    public static void setAnimationOpen(Context ctx, boolean open) {
        SharedPreferences prefs = initSharedPreferences(ctx);
        SharedPreferencesCompat.apply(prefs.edit().putBoolean(PREFS_KEY_ANIMATION_SWITCH, open));
    }

    public static boolean rememberLastUI(Context ctx) {
        SharedPreferences prefs = initSharedPreferences(ctx);
        return prefs.getBoolean(PREFS_KEY_REMEMBER_LAST_UI, true);
    }

    public static void setRememberLastUI(Context ctx, boolean remember) {
        SharedPreferences prefs = initSharedPreferences(ctx);
        SharedPreferencesCompat.apply(prefs.edit().putBoolean(PREFS_KEY_REMEMBER_LAST_UI, remember));
    }

    public static String getLastUI(Context ctx) {
        SharedPreferences prefs = initSharedPreferences(ctx);
        return prefs.getString(PREFS_KEY_LAST_UI, "");
    }

    public static void setLastUI(Context ctx, String lastUi) {
        SharedPreferences prefs = initSharedPreferences(ctx);
        SharedPreferencesCompat.apply(prefs.edit().putString(PREFS_KEY_LAST_UI, lastUi));
    }

    public static void setGuideTipShown(Context ctx, boolean open) {
        SharedPreferences prefs = initSharedPreferences(ctx);
        SharedPreferencesCompat.apply(prefs.edit().putBoolean(PREFS_KEY_GUIDE_TIP_SHOW, open));
    }

    public static boolean IsGuideTipShown(Context ctx) {
        SharedPreferences prefs = initSharedPreferences(ctx);
        return prefs.getBoolean(PREFS_KEY_GUIDE_TIP_SHOW, true);
    }
}
