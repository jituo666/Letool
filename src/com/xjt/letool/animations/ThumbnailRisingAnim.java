package com.xjt.letool.animations;

import android.graphics.Rect;

import com.xjt.letool.views.opengl.GLESCanvas;

public class ThumbnailRisingAnim extends ThumbnailAnim {
    private static final int RISING_DISTANCE = 1024;

    @Override
    public void apply(GLESCanvas canvas, int thumbnailIndex, Rect target) {
        canvas.translate(0, 0, RISING_DISTANCE * (1 - mProgress));
    }
}