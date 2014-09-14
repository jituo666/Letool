package com.xjt.newpic.edit.controller;

public class ParameterOpacity extends BasicParameterInt {
    public static String sParameterType = "ParameterOpacity";
    float[] mHSVO = new float[4];

    public ParameterOpacity(int id, int value) {
        super(id, value, 0, 255);
    }

    @Override
    public String getParameterType() {
        return sParameterType;
    }

    public void setColor(float[] hsvo) {
        mHSVO = hsvo;
    }

    public float[] getColor() {
        mHSVO[3] = getValue() / (float) getMaximum();
        return mHSVO;
    }
}
