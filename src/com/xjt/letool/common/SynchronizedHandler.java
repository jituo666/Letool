package com.xjt.letool.common;

import android.os.Handler;
import android.os.Message;

import com.xjt.letool.utils.Utils;
import com.xjt.letool.views.GLController;

public class SynchronizedHandler extends Handler {

    private final GLController mRoot;

    public SynchronizedHandler(GLController root) {
        mRoot = Utils.checkNotNull(root);
    }

    @Override
    public void dispatchMessage(Message message) {
        mRoot.lockRenderThread();
        try {
            super.dispatchMessage(message);
        } finally {
            mRoot.unlockRenderThread();
        }
    }
}
