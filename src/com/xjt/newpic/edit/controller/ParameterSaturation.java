package com.xjt.newpic.edit.controller;

public class ParameterSaturation extends BasicParameterInt {
    public static String sParameterType = "ParameterSaturation";
    float[] mHSVO = new float[4];

    public ParameterSaturation(int id, int value) {
        super(id, value, 0, 100);
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
