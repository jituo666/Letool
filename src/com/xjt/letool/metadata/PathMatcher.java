
package com.xjt.letool.metadata;

import com.xjt.letool.common.LLog;

import java.util.HashMap;

public class PathMatcher {

    private static final String TAG = PathMatcher.class.getSimpleName();
    private HashMap<String, Integer> mPathMap = new HashMap<String, Integer>();

    public void add(String pattern, int kind) {
        mPathMap.put(pattern, kind);
    }

    public int match(String key) {

        LLog.i(TAG, " (mPathMap == null ?)" + (mPathMap.size()) + " key:" + (key == null));
        for (String s:mPathMap.keySet()) {
            LLog.i(TAG, " (mPathMap == key list :)" + s);
        }
        return mPathMap.get(key);
    }
}
