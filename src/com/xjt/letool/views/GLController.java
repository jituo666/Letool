package com.xjt.letool.views;

import com.xjt.letool.animations.CanvasAnim;
import com.xjt.letool.common.OrientationSource;
import com.xjt.letool.opengl.GLESCanvas;

import android.content.Context;
import android.graphics.Matrix;

public interface GLController {
    // Listener will be called when GL is idle AND before each frame.
    // Mainly used for uploading textures.
    public static interface OnGLIdleListener {
        public boolean onGLIdle(GLESCanvas canvas, boolean renderRequested);
    }

    public void addOnGLIdleListener(OnGLIdleListener listener);
    public void registerLaunchedAnimation(CanvasAnim animation);
    public void requestRenderForced();
    public void requestRender();
    public void requestLayoutContentPane();

    public void lockRenderThread();
    public void unlockRenderThread();

    public void setContentPane(GLBaseView content);
    public void setOrientationSource(OrientationSource source);
    public int getDisplayRotation();
    public int getCompensation();
    public Matrix getCompensationMatrix();
    public void freeze();
    public void unfreeze();
    public void setLightsOutMode(boolean enabled);

    public Context getContext();
}
