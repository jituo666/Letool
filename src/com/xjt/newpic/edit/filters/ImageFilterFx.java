package com.xjt.newpic.edit.filters;

import com.xjt.newpic.common.LLog;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageFilterFx extends ImageFilter {

    private static final String TAG = ImageFilterFx.class.getSimpleName();
    private FilterFxRepresentation mParameters = null;
    private Bitmap mFxBitmap = null;
    private Resources mResources = null;
    private int mFxBitmapId = 0;

    public ImageFilterFx() {
    }

    @Override
    public void freeResources() {
        if (mFxBitmap != null)
            mFxBitmap.recycle();
        mFxBitmap = null;
    }

    @Override
    public FilterRepresentation getDefaultRepresentation() {
        return null;
    }

    public void useRepresentation(FilterRepresentation representation) {
        FilterFxRepresentation parameters = (FilterFxRepresentation) representation;
        mParameters = parameters;
    }

    public FilterFxRepresentation getParameters() {
        return mParameters;
    }

    native protected void nativeApplyFilter(Bitmap bitmap, int w, int h, Bitmap fxBitmap, int fxw, int fxh, int start, int end);

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
        if (getParameters() == null || mResources == null) {
            return bitmap;
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int bitmapResourceId = getParameters().getBitmapResource();
        if (bitmapResourceId == 0) { // null filter fx
            return bitmap;
        }

        if (mFxBitmap == null || mFxBitmapId != bitmapResourceId) {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inScaled = false;
            mFxBitmapId = bitmapResourceId;
            if (mFxBitmapId != 0) {
                mFxBitmap = BitmapFactory.decodeResource(mResources, mFxBitmapId, o);
            } else {
                LLog.w(TAG, "bad resource for filter: " + mName);
            }
        }

        if (mFxBitmap == null) {
            return bitmap;
        }

        int fxw = mFxBitmap.getWidth();
        int fxh = mFxBitmap.getHeight();

        int stride = w * 4;
        int max = stride * h;
        int increment = stride * 256; // 256 lines
        for (int i = 0; i < max; i += increment) {
            int start = i;
            int end = i + increment;
            if (end > max) {
                end = max;
            }
            if (!getEnvironment().needsStop()) {
                nativeApplyFilter(bitmap, w, h, mFxBitmap, fxw, fxh, start, end);
            }
        }

        return bitmap;
    }

    public void setResources(Resources resources) {
        mResources = resources;
    }

}
