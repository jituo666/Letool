package com.xjt.letool.anims;

import android.graphics.Rect;
import android.view.animation.DecelerateInterpolator;

import com.xjt.letool.opengl.GLESCanvas;

public abstract class ThumbnailAnim extends Animation {
    protected float mProgress = 0;

    public ThumbnailAnim() {
        setInterpolator(new DecelerateInterpolator(4));
        setDuration(1500);
    }

    @Override
    protected void onCalculate(float progress) {
        mProgress = progress;
    }

    abstract public void apply(GLESCanvas canvas, int slotIndex, Rect target);
}
