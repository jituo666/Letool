
package com.xjt.newpic.edit.filters;

import android.content.res.Resources;

public class FiltersManager extends BaseFiltersManager {

    private static FiltersManager sInstance = null;
    private static FiltersManager sPreviewInstance = null;
    private static FiltersManager sHighresInstance = null;

    public FiltersManager() {
        init();
    }

    public static FiltersManager getManager() {
        if (sInstance == null) {
            sInstance = new FiltersManager();
        }
        return sInstance;
    }

    public static FiltersManager getPreviewManager() {
        if (sPreviewInstance == null) {
            sPreviewInstance = new FiltersManager();
        }
        return sPreviewInstance;
    }

    public static FiltersManager getHighresManager() {
        if (sHighresInstance == null) {
            sHighresInstance = new FiltersManager();
        }
        return sHighresInstance;
    }

    public static void reset() {
        sInstance = null;
        sPreviewInstance = null;
        sHighresInstance = null;
    }

    public static void setResources(Resources resources) {
        FiltersManager.getManager().setFilterResources(resources);
        FiltersManager.getPreviewManager().setFilterResources(resources);
        FiltersManager.getHighresManager().setFilterResources(resources);
    }
}
