package com.xjt.newpic.views.opengl;


import java.util.ArrayDeque;

import com.xjt.newpic.view.GLController;
import com.xjt.newpic.view.GLController.OnGLIdleListener;

public class TextureUploader implements OnGLIdleListener {
    private static final int INIT_CAPACITY = 64;
    private static final int QUOTA_PER_FRAME = 1;

    private final ArrayDeque<UploadedBitmapTexture> mFgTextures =
            new ArrayDeque<UploadedBitmapTexture>(INIT_CAPACITY);
    private final ArrayDeque<UploadedBitmapTexture> mBgTextures =
            new ArrayDeque<UploadedBitmapTexture>(INIT_CAPACITY);
    private final GLController mGLRoot;
    private volatile boolean mIsQueued = false;

    public TextureUploader(GLController root) {
        mGLRoot = root;
    }

    public synchronized void clear() {
        while (!mFgTextures.isEmpty()) {
            mFgTextures.pop().setIsUploading(false);
        }
        while (!mBgTextures.isEmpty()) {
            mBgTextures.pop().setIsUploading(false);
        }
    }

    // caller should hold synchronized on "this"
    private void queueSelfIfNeed() {
        if (mIsQueued) return;
        mIsQueued = true;
        mGLRoot.addOnGLIdleListener(this);
    }

    public synchronized void addBgTexture(UploadedBitmapTexture t) {
        if (t.isContentValid()) return;
        mBgTextures.addLast(t);
        t.setIsUploading(true);
        queueSelfIfNeed();
    }

    public synchronized void addFgTexture(UploadedBitmapTexture t) {
        if (t.isContentValid()) return;
        mFgTextures.addLast(t);
        t.setIsUploading(true);
        queueSelfIfNeed();
    }

    private int upload(GLESCanvas canvas, ArrayDeque<UploadedBitmapTexture> deque,
            int uploadQuota, boolean isBackground) {
        while (uploadQuota > 0) {
            UploadedBitmapTexture t;
            synchronized (this) {
                if (deque.isEmpty()) break;
                t = deque.removeFirst();
                t.setIsUploading(false);
                if (t.isContentValid()) continue;

                // this has to be protected by the synchronized block
                // to prevent the inner bitmap get recycled
                t.updateContent(canvas);
            }

            // It will took some more time for a texture to be drawn for
            // the first time.
            // Thus, when scrolling, if a new column appears on screen,
            // it may cause a UI jank even these textures are uploaded.
            if (isBackground) t.draw(canvas, 0, 0);
            --uploadQuota;
        }
        return uploadQuota;
    }

    @Override
    public boolean onGLIdle(GLESCanvas canvas, boolean renderRequested) {
        int uploadQuota = QUOTA_PER_FRAME;
        uploadQuota = upload(canvas, mFgTextures, uploadQuota, false);
        if (uploadQuota < QUOTA_PER_FRAME) mGLRoot.requestRender();
        upload(canvas, mBgTextures, uploadQuota, true);
        synchronized (this) {
            mIsQueued = !mFgTextures.isEmpty() || !mBgTextures.isEmpty();
            return mIsQueued;
        }
    }
}
