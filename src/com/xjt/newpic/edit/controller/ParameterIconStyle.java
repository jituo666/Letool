package com.xjt.newpic.edit.controller;

import android.graphics.Bitmap;

public class ParameterIconStyle extends BasicParameterStyle {
    Bitmap[] mBitmaps;

    public ParameterIconStyle(int id, Bitmap[] styles) {
        super(id, styles.length);
        mBitmaps = styles;
    }

    @Override
    public void getIcon(int index, BitmapCaller caller) {
        caller.available(mBitmaps[index]);
    }
}
