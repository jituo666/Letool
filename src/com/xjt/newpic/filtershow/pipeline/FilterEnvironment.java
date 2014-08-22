package com.xjt.newpic.filtershow.pipeline;

import android.graphics.Bitmap;
import android.support.v4.util.LongSparseArray;
import android.support.v8.renderscript.Allocation;

import com.xjt.newpic.common.LLog;
import com.xjt.newpic.filtershow.cache.BitmapCache;
import com.xjt.newpic.filtershow.filters.FilterRepresentation;
import com.xjt.newpic.filtershow.filters.FilterUserPresetRepresentation;
import com.xjt.newpic.filtershow.filters.FiltersManagerInterface;
import com.xjt.newpic.filtershow.filters.ImageFilter;


public class FilterEnvironment {
    private static final String TAG = FilterEnvironment.class.getSimpleName();
    private ImagePreset mImagePreset;
    private float mScaleFactor;
    private int mQuality;
    private FiltersManagerInterface mFiltersManager;
    private PipelineInterface mPipeline;
    private volatile boolean mStop = false;
    private BitmapCache mBitmapCache;

    public static final int QUALITY_ICON = 0;
    public static final int QUALITY_PREVIEW = 1;
    public static final int QUALITY_FINAL = 2;

    public synchronized boolean needsStop() {
        return mStop;
    }

    public synchronized void setStop(boolean stop) {
        this.mStop = stop;
    }

    private LongSparseArray<Integer> generalParameters = new LongSparseArray<Integer>();

    public void setBitmapCache(BitmapCache cache) {
        mBitmapCache = cache;
    }

    public void cache(Buffer buffer) {
        mBitmapCache.cache(buffer);
    }

    public void cache(Bitmap bitmap) {
        mBitmapCache.cache(bitmap);
    }

    public Bitmap getBitmap(int w, int h, int type) {
        return mBitmapCache.getBitmap(w, h, type);
    }

    public Bitmap getBitmapCopy(Bitmap source, int type) {
        return mBitmapCache.getBitmapCopy(source, type);
    }

    public void setImagePreset(ImagePreset imagePreset) {
        mImagePreset = imagePreset;
    }

    public ImagePreset getImagePreset() {
        return mImagePreset;
    }

    public void setScaleFactor(float scaleFactor) {
        mScaleFactor = scaleFactor;
    }

    public float getScaleFactor() {
        return mScaleFactor;
    }

    public void setQuality(int quality) {
        mQuality = quality;
    }

    public int getQuality() {
        return mQuality;
    }

    public void setFiltersManager(FiltersManagerInterface filtersManager) {
        mFiltersManager = filtersManager;
    }

    public FiltersManagerInterface getFiltersManager() {
        return mFiltersManager;
    }

    public void applyRepresentation(FilterRepresentation representation,
                                    Allocation in, Allocation out) {
        ImageFilter filter = mFiltersManager.getFilterForRepresentation(representation);
        filter.useRepresentation(representation);
        filter.setEnvironment(this);
        if (filter.supportsAllocationInput()) {
            filter.apply(in, out);
        }
        filter.setGeneralParameters();
        filter.setEnvironment(null);
    }

    public Bitmap applyRepresentation(FilterRepresentation representation, Bitmap bitmap) {
        if (representation instanceof FilterUserPresetRepresentation) {
            // we allow instances of FilterUserPresetRepresentation in a preset only to know if one
            // has been applied (so we can show this in the UI). But as all the filters in them are
            // applied directly they do not themselves need to do any kind of filtering.
            return bitmap;
        }
        ImageFilter filter = mFiltersManager.getFilterForRepresentation(representation);
        if (filter == null){
            LLog.e(TAG,"No ImageFilter for "+representation.getSerializationName());
        }
        filter.useRepresentation(representation);
        filter.setEnvironment(this);
        Bitmap ret = filter.apply(bitmap, mScaleFactor, mQuality);
        if (bitmap != ret) {
            cache(bitmap);
        }
        filter.setGeneralParameters();
        filter.setEnvironment(null);
        return ret;
    }

    public PipelineInterface getPipeline() {
        return mPipeline;
    }

    public void setPipeline(PipelineInterface cachingPipeline) {
        mPipeline = cachingPipeline;
    }

    public synchronized void clearGeneralParameters() {
        generalParameters = null;
    }

    public synchronized Integer getGeneralParameter(int id) {
        if (generalParameters == null || generalParameters.indexOfValue(id) < 0) {
            return null;
        }
        return generalParameters.get(id);
    }

    public synchronized void setGeneralParameter(int id, int value) {
        if (generalParameters == null) {
            generalParameters = new LongSparseArray<Integer>();
        }

        generalParameters.put(id, value);
    }

    public BitmapCache getBimapCache() {
        return mBitmapCache;
    }
}
