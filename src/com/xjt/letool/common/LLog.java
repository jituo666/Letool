
package com.xjt.letool.common;

/**
 * @Author Jituo.Xuan
 * @Date 8:19:24 PM Jul 24, 2014
 * @Comments:null
 */
public class LLog {

    private static final String TAG = "letooltag:";

    public static int v(String tag, String msg) {
        if (!GlobalConstants.DEBUG_LOG) {
            return 0;
        }
        tag = TAG + tag;
        return android.util.Log.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        if (!GlobalConstants.DEBUG_LOG) {
            return 0;
        }
        tag = TAG + tag;
        return android.util.Log.v(tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        if (!GlobalConstants.DEBUG_LOG) {
            return 0;
        }
        tag = TAG + tag;
        return android.util.Log.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (!GlobalConstants.DEBUG_LOG) {
            return 0;
        }
        tag = TAG + tag;
        return android.util.Log.d(tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        if (!GlobalConstants.DEBUG_LOG) {
            return 0;
        }
        tag = TAG + tag;
        return android.util.Log.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        if (!GlobalConstants.DEBUG_LOG) {
            return 0;
        }
        tag = TAG + tag;
        return android.util.Log.i(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        if (!GlobalConstants.DEBUG_LOG) {
            return 0;
        }
        tag = TAG + tag;
        return android.util.Log.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        if (!GlobalConstants.DEBUG_LOG) {
            return 0;
        }
        tag = TAG + tag;
        return android.util.Log.w(tag, msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        if (!GlobalConstants.DEBUG_LOG) {
            return 0;
        }
        tag = TAG + tag;
        return android.util.Log.w(tag, tr);
    }

    public static int e(String tag, String msg) {
        if (!GlobalConstants.DEBUG_LOG) {
            return 0;
        }
        tag = TAG + tag;
        return android.util.Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        if (!GlobalConstants.DEBUG_LOG) {
            return 0;
        }
        tag = TAG + tag;
        return android.util.Log.e(tag, msg, tr);
    }
}
