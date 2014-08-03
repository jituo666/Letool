package com.xjt.newpic.animations;

import com.xjt.newpic.views.opengl.GLESCanvas;


public abstract class CanvasAnim extends Animation {

    public abstract int getCanvasSaveFlags();
    public abstract void apply(GLESCanvas canvas);
}
