package com.xjt.newpic.edit.controller;

public class ParameterHue extends BasicParameterInt {
    public static String sParameterType = "ParameterHue";
    float[] mHSVO = new float[4];

    public ParameterHue(int id, int value) {
        super(id, value, 0, 360);
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
