package com.xjt.letool.animations;

import com.xjt.letool.opengl.GLESCanvas;


public abstract class CanvasAnim extends Animation {

    public abstract int getCanvasSaveFlags();
    public abstract void apply(GLESCanvas canvas);
}