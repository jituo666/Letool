package com.xjt.newpic.edit.pipeline;

public class SharedPreset {

    private volatile ImagePreset mProducerPreset = null;
    private volatile ImagePreset mConsumerPreset = null;
    private volatile ImagePreset mIntermediatePreset = null;
    private volatile boolean mHasNewContent = false;

    public synchronized void enqueuePreset(ImagePreset preset) {
        if (mProducerPreset == null || (!mProducerPreset.same(preset))) {
            mProducerPreset = new ImagePreset(preset);
        } else {
            mProducerPreset.updateWith(preset);
        }
        ImagePreset temp = mIntermediatePreset;
        mIntermediatePreset = mProducerPreset;
        mProducerPreset = temp;
        mHasNewContent = true;
    }

    public synchronized ImagePreset dequeuePreset() {
        if (!mHasNewContent) {
            return mConsumerPreset;
        }
        ImagePreset temp = mConsumerPreset;
        mConsumerPreset = mIntermediatePreset;
        mIntermediatePreset = temp;
        mHasNewContent = false;
        return mConsumerPreset;
    }
}
