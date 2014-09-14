package com.xjt.newpic.edit.controller;

import android.content.Context;

import com.xjt.newpic.edit.pipeline.RenderingRequestCaller;

public class BasicParameterStyle implements ParameterStyles {
    protected String mParameterName;
    protected int mSelectedStyle;
    protected int mNumberOfStyles;
    protected int mDefaultStyle = 0;
    protected Control mControl;
    protected FilterView mEditor;
    public final int ID;
    private final String LOGTAG = "BasicParameterStyle";

    @Override
    public void copyFrom(Parameter src) {
        if (!(src instanceof BasicParameterStyle)) {
            throw new IllegalArgumentException(src.getClass().getName());
        }
        BasicParameterStyle p = (BasicParameterStyle) src;
        mNumberOfStyles = p.mNumberOfStyles;
        mSelectedStyle = p.mSelectedStyle;
        mDefaultStyle = p.mDefaultStyle;
    }

    public BasicParameterStyle(int id, int numberOfStyles) {
        ID = id;
        mNumberOfStyles = numberOfStyles;
    }

    @Override
    public String getParameterName() {
        return mParameterName;
    }

    @Override
    public String getParameterType() {
        return sParameterType;
    }

    @Override
    public String getValueString() {
        return mParameterName + mSelectedStyle;
    }

    @Override
    public void setController(Control control) {
        mControl = control;
    }

    @Override
    public int getNumberOfStyles() {
        return mNumberOfStyles;
    }

    @Override
    public int getDefaultSelected() {
        return mDefaultStyle;
    }

    @Override
    public int getSelected() {
        return mSelectedStyle;
    }

    @Override
    public void setSelected(int selectedStyle) {
        mSelectedStyle = selectedStyle;
        if (mEditor != null) {
            mEditor.commitLocalRepresentation();
        }
    }

    @Override
    public void getIcon(int index, BitmapCaller caller) {
        mEditor.computeIcon(index, caller);
    }

    @Override
    public String getStyleTitle(int index, Context context) {
        return "";
    }

    @Override
    public String toString() {
        return getValueString();
    }

    @Override
    public void setFilterView(FilterView editor) {
        mEditor = editor;
    }
}
