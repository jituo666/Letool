
package com.xjt.newpic.common;

import com.xjt.newpic.view.GLController;

import android.os.Handler;
import android.os.Message;

public class SynchronizedHandler extends Handler {

    private final GLController mGLController;

    public SynchronizedHandler(GLController root) {
        mGLController = root;
    }

    @Override
    public void dispatchMessage(Message message) {
        if (mGLController != null) {
            mGLController.lockRenderThread();
            try {
                super.dispatchMessage(message);
            } finally {
                mGLController.unlockRenderThread();
            }
        }
    }
}
