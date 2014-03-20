package com.xjt.letool.views;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * @Author Jituo.Xuan
 * @Date 11:29:54 AM Mar 20, 2014
 */
public class PictureView extends GLSurfaceView implements GLSurfaceView.Renderer {

    public PictureView(Context context) {
        super(context);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

}
