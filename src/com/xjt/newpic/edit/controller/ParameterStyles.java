package com.xjt.newpic.edit.controller;

import android.content.Context;

public interface ParameterStyles extends Parameter {
    public static String sParameterType = "ParameterStyles";

    int getNumberOfStyles();

    int getDefaultSelected();

    int getSelected();

    void setSelected(int value);

    void getIcon(int index, BitmapCaller caller);

    String getStyleTitle(int index, Context context);
}
