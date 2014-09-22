
package com.xjt.newpic.edit.controller;

import java.util.Arrays;

import com.xjt.newpic.edit.filters.FilterDrawRepresentation;

public class ParameterStyle implements Parameter {

    public static String sParameterType = ParameterStyle.class.getSimpleName();

    protected Control mControl;
    protected FilterView mEditor;
    String mParameterName;
    int mValue;
    public final int ID;

    int[] mBasStyles = {
            FilterDrawRepresentation.DEFAULT_STYLE1,
            FilterDrawRepresentation.DEFAULT_STYLE2,
            FilterDrawRepresentation.DEFAULT_STYLE3,
            FilterDrawRepresentation.DEFAULT_STYLE4,
            FilterDrawRepresentation.DEFAULT_STYLE5
    };

    public ParameterStyle(int id, int defaultStyle) {
        ID = id;
        mValue = defaultStyle;
    }

    @Override
    public String getParameterType() {
        return sParameterType;
    }

    public void setStyle(int v) {
        mValue = v;
    }

    public int getStyle() {
        return mValue;
    }

    public void copyFrom(Parameter src) {
        if (!(src instanceof ParameterStyle)) {
            throw new IllegalArgumentException(src.getClass().getName());
        }
        ParameterStyle p = (ParameterStyle) src;
        mValue = p.mValue;
    }

    @Override
    public String getParameterName() {
        return mParameterName;
    }

    @Override
    public String getValueString() {
        return "(" + mValue + ")";
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
    }

    @Override
    public String toString() {
        return getValueString();
    }

    @Override
    public void setFilterView(FilterView editor) {
        mEditor = editor;
    }

    public void copyPalletFrom(ParameterStyle parameterStyle) {
        System.arraycopy(parameterStyle.mBasStyles, 0, mBasStyles, 0, mBasStyles.length);
    }

    public void setStylePalette(int[] palette) {
        mBasStyles = Arrays.copyOf(palette, palette.length);
    }

    public int[] getStylePalette() {
        return mBasStyles;
    }
}
