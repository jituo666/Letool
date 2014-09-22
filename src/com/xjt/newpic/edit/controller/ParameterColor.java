
package com.xjt.newpic.edit.controller;

import android.graphics.Color;

import java.util.Arrays;

import com.xjt.newpic.edit.filters.FilterDrawRepresentation;

public class ParameterColor implements Parameter {

    public static String sParameterType = "ParameterColor";
    protected Control mControl;
    protected FilterView mEditor;
    float[] mHSVO = new float[4];
    String mParameterName;
    int mValue;
    public final int ID;

    int[] mBasColors = {
            FilterDrawRepresentation.DEFAULT_COLOR1,
            FilterDrawRepresentation.DEFAULT_COLOR2,
            FilterDrawRepresentation.DEFAULT_COLOR3,
            FilterDrawRepresentation.DEFAULT_COLOR4,
            FilterDrawRepresentation.DEFAULT_COLOR5,
    };

    public ParameterColor(int id, int defaultColor) {
        ID = id;
        Color.colorToHSV(defaultColor, mHSVO);
        mHSVO[3] = ((defaultColor >> 24) & 0xFF) / (float) 255;
        mValue = Color.HSVToColor((int) (mHSVO[3] * 255), mHSVO);
    }

    @Override
    public String getParameterType() {
        return sParameterType;
    }

    public void setColor(float[] hsvo) {
        mHSVO = hsvo;
        mValue = Color.HSVToColor((int) (hsvo[3] * 255), mHSVO);
    }

    public float[] getColor() {
        return mHSVO;
    }

    public void copyFrom(Parameter src) {
        if (!(src instanceof ParameterColor)) {
            throw new IllegalArgumentException(src.getClass().getName());
        }
        ParameterColor p = (ParameterColor) src;

        mValue = p.mValue;
        System.arraycopy(p.mHSVO, 0, mHSVO, 0, 4);
    }

    @Override
    public String getParameterName() {
        return mParameterName;
    }

    @Override
    public String getValueString() {
        return "(" + Integer.toHexString(mValue) + ")";
    }

    @Override
    public void setController(Control control) {
        mControl = control;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        mValue = value;
        Color.colorToHSV(mValue, mHSVO);
        mHSVO[3] = ((mValue >> 24) & 0xFF) / (float) 255;
    }

    @Override
    public String toString() {
        return getValueString();
    }

    @Override
    public void setFilterView(FilterView editor) {
        mEditor = editor;
    }

    public void copyPalletFrom(ParameterColor parameterColor) {
        System.arraycopy(parameterColor.mBasColors, 0, mBasColors, 0, mBasColors.length);
    }

    public void setColorpalette(int[] palette) {
        mBasColors = Arrays.copyOf(palette, palette.length);
    }

    public int[] getColorPalette() {
        return mBasColors;
    }
}
