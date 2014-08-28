package com.xjt.newpic.filtershow.filters;

import android.graphics.Bitmap;

import com.xjt.newpic.R;
import com.xjt.newpic.filtershow.imageshow.Spline;

public class ImageFilterCurves extends ImageFilter {

    private static final String LOGTAG = "ImageFilterCurves";
    FilterCurvesRepresentation mParameters = new FilterCurvesRepresentation(0);

    @Override
    public FilterRepresentation getDefaultRepresentation() {
        return new FilterCurvesRepresentation(R.drawable.effect_sample_37);
    }

    @Override
    public void useRepresentation(FilterRepresentation representation) {
        FilterCurvesRepresentation parameters = (FilterCurvesRepresentation) representation;
        mParameters = parameters;
    }

    public ImageFilterCurves() {
        mName = "Curves";
        reset();
    }

    public void populateArray(int[] array, int curveIndex) {
        Spline spline = mParameters.getSpline(curveIndex);
        if (spline == null) {
            return;
        }
        float[] curve = spline.getAppliedCurve();
        for (int i = 0; i < 256; i++) {
            array[i] = (int) (curve[i] * 255);
        }
    }

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
        if (!mParameters.getSpline(Spline.RGB).isOriginal()) {
            int[] rgbGradient = new int[256];
            populateArray(rgbGradient, Spline.RGB);
            nativeApplyGradientFilter(bitmap, bitmap.getWidth(), bitmap.getHeight(),
                    rgbGradient, rgbGradient, rgbGradient);
        }

        int[] redGradient = null;
        if (!mParameters.getSpline(Spline.RED).isOriginal()) {
            redGradient = new int[256];
            populateArray(redGradient, Spline.RED);
        }
        int[] greenGradient = null;
        if (!mParameters.getSpline(Spline.GREEN).isOriginal()) {
            greenGradient = new int[256];
            populateArray(greenGradient, Spline.GREEN);
        }
        int[] blueGradient = null;
        if (!mParameters.getSpline(Spline.BLUE).isOriginal()) {
            blueGradient = new int[256];
            populateArray(blueGradient, Spline.BLUE);
        }

        nativeApplyGradientFilter(bitmap, bitmap.getWidth(), bitmap.getHeight(),
                redGradient, greenGradient, blueGradient);
        return bitmap;
    }

    public void setSpline(Spline spline, int splineIndex) {
        mParameters.setSpline(splineIndex, new Spline(spline));
    }

    public Spline getSpline(int splineIndex) {
        return mParameters.getSpline(splineIndex);
    }

    public void reset() {
        Spline spline = new Spline();

        spline.addPoint(0.0f, 1.0f);
        spline.addPoint(1.0f, 0.0f);

        for (int i = 0; i < 4; i++) {
            mParameters.setSpline(i, new Spline(spline));
        }
    }

    public void useFilter(ImageFilter a) {
        ImageFilterCurves c = (ImageFilterCurves) a;
        for (int i = 0; i < 4; i++) {
            if (c.mParameters.getSpline(i) != null) {
                setSpline(c.mParameters.getSpline(i), i);
            }
        }
    }
}
