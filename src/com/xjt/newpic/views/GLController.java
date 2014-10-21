
package com.xjt.newpic.views;

import com.xjt.newpic.animations.CanvasAnim;
import com.xjt.newpic.common.OrientationSource;
import com.xjt.newpic.views.opengl.GLESCanvas;

import android.graphics.Matrix;

/**
 * @Author Jituo.Xuan
 * @Date 8:17:47 PM Jul 24, 2014
 * @Comments:null
 */
public interface GLController {

    // Listener will be called when GL is idle AND before each frame. Mainly used for uploading textures.
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

    public void setContentPane(GLView content);

    public void setOrientationSource(OrientationSource source);

    public int getDisplayRotation();

    public int getCompensation();

    public Matrix getCompensationMatrix();

    public void freeze();

    public void unfreeze();

    //
    public void onPause();

    public void onResume();

    public void setLightsOutMode(boolean enabled);
}
