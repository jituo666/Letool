
package com.xjt.newpic.edit.filters;


import com.xjt.newpic.R;
import com.xjt.newpic.edit.controller.BasicParameterInt;
import com.xjt.newpic.edit.controller.Parameter;
import com.xjt.newpic.edit.editors.EditorVignette;
import com.xjt.newpic.edit.imageshow.Oval;
import com.xjt.newpic.surpport.JsonReader;
import com.xjt.newpic.surpport.JsonWriter;

import java.io.IOException;

public class FilterVignetteRepresentation extends FilterRepresentation implements Oval {
    private static final String LOGTAG = "FilterVignetteRepresentation";

    private float mCenterX = .5f;
    private float mCenterY = .5f;
    private float mRadiusX = .5f;
    private float mRadiusY = .5f;
    public static final int MODE_VIGNETTE = 0;
    public static final int MODE_EXPOSURE = 1;
    public static final int MODE_SATURATION = 2;
    public static final int MODE_CONTRAST = 3;
    public static final int MODE_FALLOFF = 4;
    private static int MIN = -100;
    private static int MAX = 100;
    private static int MAXFALLOF = 200;

    private BasicParameterInt mParamVignette = new BasicParameterInt(MODE_VIGNETTE, 50, MIN, MAX);
    private BasicParameterInt mParamExposure = new BasicParameterInt(MODE_EXPOSURE, 0, MIN, MAX);
    private BasicParameterInt mParamSaturation = new BasicParameterInt(MODE_SATURATION, 0, MIN, MAX);
    private BasicParameterInt mParamContrast = new BasicParameterInt(MODE_CONTRAST, 0, MIN, MAX);
    private BasicParameterInt mParamFalloff = new BasicParameterInt(MODE_FALLOFF, 40, 0, MAXFALLOF);
    private BasicParameterInt[] mAllParam = {
            mParamVignette,
            mParamExposure,
            mParamSaturation,
            mParamContrast,
            mParamFalloff};
    private int mParameterMode;

    public FilterVignetteRepresentation(int sr) {
        super("Vignette",sr);
        setSerializationName("VIGNETTE");
        setShowParameterValue(true);
        setFilterType(FilterRepresentation.TYPE_VIGNETTE);
        setTextId(R.string.vignette);
        setEditorId(EditorVignette.ID);
        setName("Vignette");
        setFilterClass(ImageFilterVignette.class);
    }

    @Override
    public void useParametersFrom(FilterRepresentation a) {
        super.useParametersFrom(a);
        FilterVignetteRepresentation rep = (FilterVignetteRepresentation) a;
        mCenterX = rep.mCenterX;
        mCenterY = rep.mCenterY;
        mRadiusX = rep.mRadiusX;
        mRadiusY = rep.mRadiusY;
        mParamVignette.setValue(rep.mParamVignette.getValue());
        mParamExposure.setValue(rep.mParamExposure.getValue());
        mParamSaturation.setValue(rep.mParamSaturation.getValue());
        mParamContrast.setValue(rep.mParamContrast.getValue());
        mParamFalloff.setValue(rep.mParamFalloff.getValue());
    }

    public int getValue(int mode) {
        return mAllParam[mode].getValue();
    }

    public void setValue(int mode, int value) {
        mAllParam[mode].setValue(value);
    }

    @Override
    public String toString() {
        return getName() + " : " + mCenterX + ", " + mCenterY + " radius: " + mRadiusX;
    }

    @Override
    public FilterRepresentation copy() {
        FilterVignetteRepresentation representation = new FilterVignetteRepresentation(0);
        copyAllParameters(representation);
        return representation;
    }

    @Override
    protected void copyAllParameters(FilterRepresentation representation) {
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    @Override
    public void setCenter(float centerX, float centerY) {
        mCenterX = centerX;
        mCenterY = centerY;
    }

    @Override
    public float getCenterX() {
        return mCenterX;
    }

    @Override
    public float getCenterY() {
        return mCenterY;
    }

    @Override
    public void setRadius(float radiusX, float radiusY) {
        mRadiusX = radiusX;
        mRadiusY = radiusY;
    }

    @Override
    public void setRadiusX(float radiusX) {
        mRadiusX = radiusX;
    }

    @Override
    public void setRadiusY(float radiusY) {
        mRadiusY = radiusY;
    }

    @Override
    public float getRadiusX() {
        return mRadiusX;
    }

    @Override
    public float getRadiusY() {
        return mRadiusY;
    }

    public boolean isCenterSet() {
        return mCenterX != Float.NaN;
    }

    @Override
    public boolean isNil() {
        return false;
    }

    @Override
    public boolean equals(FilterRepresentation representation) {
        if (!super.equals(representation)) {
            return false;
        }
        if (representation instanceof FilterVignetteRepresentation) {
            FilterVignetteRepresentation rep = (FilterVignetteRepresentation) representation;
            for (int i = 0; i < mAllParam.length; i++) {
                if (mAllParam[i].getValue() != rep.mAllParam[i].getValue())
                    return false;
            }
            if (rep.getCenterX() == getCenterX()
                    && rep.getCenterY() == getCenterY()
                    && rep.getRadiusX() == getRadiusX()
                    && rep.getRadiusY() == getRadiusY()) {
                return true;
            }
        }
        return false;
    }

    private static final String ELLIPSE = "ellipse";
    private static final String ARGS = "adjust";
    @Override
    public void serializeRepresentation(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name(ELLIPSE);
        writer.beginArray();
        writer.value(mCenterX);
        writer.value(mCenterY);
        writer.value(mRadiusX);
        writer.value(mRadiusY);
        writer.endArray();

        writer.name(ARGS);
        writer.beginArray();
        writer.value(mParamVignette.getValue());
        writer.value(mParamExposure.getValue());
        writer.value(mParamSaturation.getValue());
        writer.value(mParamContrast.getValue());
        writer.value(mParamFalloff.getValue());
        writer.endArray();
        writer.endObject();
    }


    @Override
    public void deSerializeRepresentation(JsonReader sreader) throws IOException {
        sreader.beginObject();

        while (sreader.hasNext()) {
            String name = sreader.nextName();
            if (name.startsWith(ELLIPSE)) {
                sreader.beginArray();
                sreader.hasNext();
                mCenterX = (float) sreader.nextDouble();
                sreader.hasNext();
                mCenterY = (float) sreader.nextDouble();
                sreader.hasNext();
                mRadiusX = (float) sreader.nextDouble();
                sreader.hasNext();
                mRadiusY = (float) sreader.nextDouble();
                sreader.hasNext();
                sreader.endArray();
            } else if (name.startsWith(ARGS)) {
                sreader.beginArray();
                sreader.hasNext();
                mParamVignette.setValue(sreader.nextInt());
                sreader.hasNext();
                mParamExposure.setValue(sreader.nextInt());
                sreader.hasNext();
                mParamSaturation.setValue(sreader.nextInt());
                sreader.hasNext();
                mParamContrast.setValue(sreader.nextInt());
                sreader.hasNext();
                mParamFalloff.setValue(sreader.nextInt());
                sreader.hasNext();
                sreader.endArray();
            } else  {
                sreader.skipValue();
            }
        }
        sreader.endObject();
    }
    public int getParameterMode() {
        return mParameterMode;
    }

    public void setParameterMode(int parameterMode) {
        mParameterMode = parameterMode;
    }

    public int getCurrentParameter() {
        return getValue(mParameterMode);
    }

    public void setCurrentParameter(int value) {
        setValue(mParameterMode, value);
    }

    public BasicParameterInt getFilterParameter(int index) {
        return mAllParam[index];
    }

}
