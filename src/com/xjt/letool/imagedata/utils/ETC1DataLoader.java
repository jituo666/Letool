package com.xjt.letool.imagedata.utils;

import android.opengl.ETC1Util.ETC1Texture;

import com.xjt.letool.common.Future;
import com.xjt.letool.common.FutureListener;


public abstract class ETC1DataLoader implements FutureListener<ETC1Texture> {

    @SuppressWarnings("unused")
    private static final String TAG = ETC1DataLoader.class.getSimpleName();

    /* Transition Map:
     *   INIT -> REQUESTED, RECYCLED
     *   REQUESTED -> INIT (cancel), LOADED, ERROR, RECYCLED
     *   LOADED, ERROR -> RECYCLED
     */
    private static final int STATE_INIT = 0;
    private static final int STATE_REQUESTED = 1;
    private static final int STATE_LOADED = 2;
    private static final int STATE_ERROR = 3;
    private static final int STATE_RECYCLED = 4;

    private int mState = STATE_INIT;
    private Future<ETC1Texture> mTask; // mTask is not null only when a task is on the way
    private ETC1Texture mETC1Texture;

    @Override
    public void onFutureDone(Future<ETC1Texture> future) {
        synchronized (this) {
            mTask = null;
            mETC1Texture = future.get();
            if (mState == STATE_RECYCLED) {
                if (mETC1Texture != null) {
                    mETC1Texture = null;
                }
                return; // don't call callback
            }
            if (future.isCancelled() && mETC1Texture == null) {
                if (mState == STATE_REQUESTED) mTask = submitETC1TextureTask(this);
                return; // don't call callback
            } else {
                mState = mETC1Texture == null ? STATE_ERROR : STATE_LOADED;
            }
        }
        onLoadComplete(mETC1Texture);
    }

    public synchronized void startLoad() {
        if (mState == STATE_INIT) {
            mState = STATE_REQUESTED;
            if (mTask == null) mTask = submitETC1TextureTask(this);
        }
    }

    public synchronized void cancelLoad() {
        if (mState == STATE_REQUESTED) {
            mState = STATE_INIT;
            if (mTask != null) mTask.cancel();
        }
    }

    // Recycle the loader and the bitmap
    public synchronized void recycle() {
        mState = STATE_RECYCLED;
        if (mETC1Texture != null) {
            mETC1Texture = null;
            mETC1Texture = null;
        }
        if (mTask != null) mTask.cancel();
    }

    public synchronized boolean isRequestInProgress() {
        return mState == STATE_REQUESTED;
    }

    public synchronized boolean isRecycled() {
        return mState == STATE_RECYCLED;
    }

    public synchronized ETC1Texture getETC1Texture() {
        return mETC1Texture;
    }

    abstract protected Future<ETC1Texture> submitETC1TextureTask(FutureListener<ETC1Texture> l);
    abstract protected void onLoadComplete(ETC1Texture bitmap);
}
