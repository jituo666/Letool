package com.xjt.letool.common;

import com.xjt.letool.utils.Utils;
import com.xjt.letool.view.GLController;

import android.os.Handler;
import android.os.Message;

public class SynchronizedHandler extends Handler {

    private final GLController mGLController;

    public SynchronizedHandler(GLController root) {
        mGLController = Utils.checkNotNull(root);
    }

    @Override
    public void dispatchMessage(Message message) {
        mGLController.lockRenderThread();
        try {
            super.dispatchMessage(message);
        } finally {
            mGLController.unlockRenderThread();
        }
    }
}
