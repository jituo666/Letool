
package com.xjt.newpic.edit.controller;

import java.util.Arrays;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.filters.FilterTextureBorderRepresentation;

public class ParameterTexture implements Parameter {

    public static String sParameterType = ParameterTexture.class.getSimpleName();
    protected Control mControl;
    protected FilterView mEditor;
    String mParameterName;
    int mValue;
    public final int ID;
    int[] mBasTextures = {
            FilterTextureBorderRepresentation.DEFAULT_TEXTURE1,
            FilterTextureBorderRepresentation.DEFAULT_TEXTURE2,
            FilterTextureBorderRepresentation.DEFAULT_TEXTURE3,
            FilterTextureBorderRepresentation.DEFAULT_TEXTURE4,
            FilterTextureBorderRepresentation.DEFAULT_TEXTURE5
    };

    public ParameterTexture(int id, int defaultTexture) {
        ID = id;
        mValue = defaultTexture;
    }

    @Override
    public String getParameterType() {
        return sParameterType;
    }

    public void setTexture(int v) {
        mValue = v;
    }

    public int getTexture() {
        return mValue;
    }

    public void copyFrom(Parameter src) {
        if (!(src instanceof ParameterTexture)) {
            throw new IllegalArgumentException(src.getClass().getName());
        }
        ParameterTexture p = (ParameterTexture) src;
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

    public void copyPalletFrom(ParameterTexture parameterTexture) {
        System.arraycopy(parameterTexture.mBasTextures, 0, mBasTextures, 0, mBasTextures.length);
    }

    public void setTexturePalette(int[] palette) {
        mBasTextures = Arrays.copyOf(palette, palette.length);
    }

    public int[] getTexturePalette() {
        return mBasTextures;
    }
}
