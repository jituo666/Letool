package com.xjt.letool.views;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.xjt.letool.common.ApiHelper;
import com.xjt.letool.opengl.GLES11Canvas;
import com.xjt.letool.opengl.GLES20Canvas;
import com.xjt.letool.opengl.GLESCanvas;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * @Author Jituo.Xuan
 * @Date 11:29:54 AM Mar 20, 2014
 */
public class PictureView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private GLESCanvas mCanvas;

    public PictureView(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean handled = false;
        return handled;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCanvas = ApiHelper.HAS_GLES20_REQUIRED ? new GLES20Canvas() : new GLES11Canvas();
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mCanvas.setSize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }
}
