/*
 * Copyright (C) 2012 Tapas Mobile Ltd.  All Rights Reserved.
 */

package com.xjt.letool.preference;

import android.content.SharedPreferences;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SharedPreferencesCompat {
    private static Method sApplyMethod;
    private static Method sGetStringSetMethod;
    private static Method sPutStringSetMethod;
    private static final String SEPERATOR = "|";

    static {
        try {
            Class<?>[] arrayOfClass = new Class[0];
            sApplyMethod = SharedPreferences.Editor.class.getMethod("apply", arrayOfClass);
        } catch (NoSuchMethodException localNoSuchMethodException) {
            sApplyMethod = null;
        }
        try {
            Class<?>[] arrayOfClass = new Class[] {String.class, Set.class};
            sGetStringSetMethod = SharedPreferences.class.getMethod("getStringSet", arrayOfClass);
            sPutStringSetMethod = SharedPreferences.Editor.class.getMethod("putStringSet", arrayOfClass);
        } catch (NoSuchMethodException localNoSuchMethodException) {
            sGetStringSetMethod = null;
            sPutStringSetMethod = null;
        }
    }

    public static void apply(SharedPreferences.Editor paramEditor) {
        if (sApplyMethod != null) {
            try {
                Method localMethod = sApplyMethod;
                Object[] arrayOfObject = new Object[0];
                localMethod.invoke(paramEditor, arrayOfObject);
                return;
            } catch (IllegalAccessException localIllegalAccessException) {
                // ignore this, will to the final
            } catch (InvocationTargetException localInvocationTargetException) {
                // ignore this, will to the final
            }
        }
        // if anything wrong, will be here
        paramEditor.commit();
    }

    private static Set<String> getSets(SharedPreferences pref, String key, Set<String> set) {
        String val = pref.getString(key, null);
        if (val == null || val.equals("")) return set;
        String[] vals = val.split("\\" + SEPERATOR);
        HashSet<String> ret = new HashSet<String>(Arrays.asList(vals));
        return ret;
    }

    @SuppressWarnings("unchecked")
    public static Set<String> getStringSet(SharedPreferences pref, String key, Set<String> set) {
        Set<String> ret = null;
        if (sGetStringSetMethod != null) {
            try {
                Method localMethod = sGetStringSetMethod;
                Object[] arrayOfObject = new Object[] {key, set};
                Object o = localMethod.invoke(pref, arrayOfObject);
                if (o == null) return set;
                ret = (Set<String>) o;
                return ret;
            } catch (IllegalAccessException localIllegalAccessException) {
                // ignore this, will to the final
            } catch (InvocationTargetException localInvocationTargetException) {
                // ignore this, will to the final
            }
        }
        // if anything wrong, will be here
        ret = getSets(pref, key, set);
        return ret;
    }

    private static void putSets(SharedPreferences.Editor editor, String key, Set<String> set) {
        if (set == null) {
            editor.remove(key);
        }
        StringBuilder val = new StringBuilder();
        boolean first = true;
        for (String s : set) {
            //if (s.equals("")) continue;
            if (!first) {
                val.append(SEPERATOR);
            } else {
                first = false;
            }
            val.append(s);
        }
        editor.putString(key, val.toString());
    }

    public static void putStringSet(SharedPreferences.Editor editor, String key, Set<String> set) {
        if (sPutStringSetMethod != null) {
            try {
                Method localMethod = sPutStringSetMethod;
                Object[] arrayOfObject = new Object[] {key, set};
                localMethod.invoke(editor, arrayOfObject);
                return;
            } catch (IllegalAccessException localIllegalAccessException) {
                // ignore this, will to the final
            } catch (InvocationTargetException localInvocationTargetException) {
                // ignore this, will to the final
            }
        }
        // if anything wrong, will be here
        putSets(editor, key, set);
    }
}