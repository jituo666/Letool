package com.xjt.newpic.edit.colorpicker;

public interface ColorListener {
    void setColor(float[] hsvo);
    public void addColorListener(ColorListener l);
}
