package com.xjt.letool.animations;

import android.graphics.Rect;

import com.xjt.letool.utils.RelativePosition;
import com.xjt.letool.views.opengl.GLESCanvas;

public class ThumbnailScatteringAnim extends ThumbnailAnim {
    private int PHOTO_DISTANCE = 1000;
    private RelativePosition mCenter;

    public ThumbnailScatteringAnim(RelativePosition center) {
        mCenter = center;
    }

    @Override
    public void apply(GLESCanvas canvas, int thumbnailIndex, Rect target) {
        canvas.translate((mCenter.getX() - target.centerX()) * (1 - mProgress),
                (mCenter.getY() - target.centerY()) * (1 - mProgress), thumbnailIndex * PHOTO_DISTANCE * (1 - mProgress));
        canvas.setAlpha(mProgress);
    }
}