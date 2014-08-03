package com.xjt.newpic.imagedata.utils;

import android.graphics.Bitmap;

import com.xjt.newpic.common.Future;
import com.xjt.newpic.common.FutureListener;


// We use this class to
//     1.) load bitmaps in background.
//     2.) as a place holder for the loaded bitmap
public abstract class BitmapLoader implements FutureListener<Bitmap> {
    @SuppressWarnings("unused")
    private static final String TAG = "BitmapLoader";

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
    private Future<Bitmap> mTask; // mTask is not null only when a task is on the way
    private Bitmap mBitmap;

    @Override
    public void onFutureDone(Future<Bitmap> future) {
        synchronized (this) {
            mTask = null;
            mBitmap = future.get();
            if (mState == STATE_RECYCLED) {
                if (mBitmap != null) {
                    LetoolBitmapPool.getInstance().put(mBitmap);
                    mBitmap = null;
                }
                return; // don't call callback
            }
            if (future.isCancelled() && mBitmap == null) {
                if (mState == STATE_REQUESTED) mTask = submitBitmapTask(this);
                return; // don't call callback
            } else {
                mState = mBitmap == null ? STATE_ERROR : STATE_LOADED;
            }
        }
        onLoadComplete(mBitmap);
    }

    public synchronized void startLoad() {
        if (mState == STATE_INIT) {
            mState = STATE_REQUESTED;
            if (mTask == null) mTask = submitBitmapTask(this);
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
        if (mBitmap != null) {
            LetoolBitmapPool.getInstance().put(mBitmap);
            mBitmap = null;
        }
        if (mTask != null) mTask.cancel();
    }

    public synchronized boolean isRequestInProgress() {
        return mState == STATE_REQUESTED;
    }

    public synchronized boolean isRecycled() {
        return mState == STATE_RECYCLED;
    }

    public synchronized Bitmap getBitmap() {
        return mBitmap;
    }

    abstract protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l);
    abstract protected void onLoadComplete(Bitmap bitmap);
}
