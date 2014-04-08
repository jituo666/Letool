package com.xjt.letool.data.utils;

import com.xjt.letool.utils.Utils;

public class DBUpdateHelper {

    private boolean mUpdated = false;

    public int update(int original, int update) {
        if (original != update) {
            mUpdated = true;
            original = update;
        }
        return original;
    }

    public long update(long original, long update) {
        if (original != update) {
            mUpdated = true;
            original = update;
        }
        return original;
    }

    public double update(double original, double update) {
        if (original != update) {
            mUpdated = true;
            original = update;
        }
        return original;
    }

    public <T> T update(T original, T update) {
        if (!Utils.equals(original, update)) {
            mUpdated = true;
            original = update;
        }
        return original;
    }

    public boolean isUpdated() {
        return mUpdated;
    }
}
