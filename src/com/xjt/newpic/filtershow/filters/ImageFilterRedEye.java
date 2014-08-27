package com.xjt.newpic.filtershow.filters;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;

import java.util.Vector;

public class ImageFilterRedEye extends ImageFilter {
    private static final String LOGTAG = "ImageFilterRedEye";
    FilterRedEyeRepresentation mParameters = new FilterRedEyeRepresentation();

    public ImageFilterRedEye() {
        mName = "Red Eye";
    }

    @Override
    public FilterRepresentation getDefaultRepresentation() {
        return new FilterRedEyeRepresentation();
    }

    public boolean isNil() {
        return mParameters.isNil();
    }

    public Vector<FilterPoint> getCandidates() {
        return mParameters.getCandidates();
    }

    public void clear() {
        mParameters.clearCandidates();
    }

    native protected void nativeApplyFilter(Bitmap bitmap, int w, int h, short[] matrix);

    @Override
    public void useRepresentation(FilterRepresentation representation) {
        FilterRedEyeRepresentation parameters = (FilterRedEyeRepresentation) representation;
        mParameters = parameters;
    }

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        short[] rect = new short[4];

        int size = mParameters.getNumberOfCandidates();
        Matrix originalToScreen = getOriginalToScreenMatrix(w, h);
        for (int i = 0; i < size; i++) {
            RectF r = new RectF(((RedEyeCandidate) (mParameters.getCandidate(i))).mRect);
            originalToScreen.mapRect(r);
            if (r.intersect(0, 0, w, h)) {
                rect[0] = (short) r.left;
                rect[1] = (short) r.top;
                rect[2] = (short) r.width();
                rect[3] = (short) r.height();
                nativeApplyFilter(bitmap, w, h, rect);
            }
        }
        return bitmap;
    }
}
