package com.xjt.newpic.edit.pipeline;

import android.graphics.Bitmap;

public class SharedBuffer {

    private static final String TAG = SharedBuffer.class.getSimpleName();

    private volatile Buffer mProducer = null;
    private volatile Buffer mConsumer = null;
    private volatile Buffer mIntermediate = null;

    private volatile boolean mNeedsSwap = false;
    private volatile boolean mNeedsRepaint = true;

    public synchronized void setProducer(Bitmap producer) {
        if (mProducer != null && !mProducer.isSameSize(producer)) {
            mProducer.remove();
            mProducer = null;
        }
        if (mProducer == null) {
            mProducer = new Buffer(producer);
        } else {
            mProducer.useBitmap(producer);
        }
    }

    public synchronized Buffer getProducer() {
        return mProducer;
    }

    public synchronized Buffer getConsumer() {
        return mConsumer;
    }

    public synchronized void swapProducer() {
        Buffer intermediate = mIntermediate;
        mIntermediate = mProducer;
        mProducer = intermediate;
        mNeedsSwap = true;
    }

    public synchronized void swapConsumerIfNeeded() {
        if (!mNeedsSwap) {
            return;
        }
        Buffer intermediate = mIntermediate;
        mIntermediate = mConsumer;
        mConsumer = intermediate;
        mNeedsSwap = false;
    }

    public synchronized void invalidate() {
        mNeedsRepaint = true;
    }

    public synchronized boolean checkRepaintNeeded() {
        if (mNeedsRepaint) {
            mNeedsRepaint = false;
            return true;
        }
        return false;
    }

}

