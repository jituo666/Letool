package com.xjt.newpic.edit.pipeline;

import android.graphics.Bitmap;

import com.xjt.newpic.edit.filters.FiltersManager;
import com.xjt.newpic.edit.imageshow.ImageManager;

public class PreviewRenderingRequestTask extends ProcessingTask {
    
    private static final String TAG = PreviewRenderingRequestTask.class.getSimpleName();
    
    private CachingPipeline mPreviewPipeline = null;
    private boolean mHasUnhandledPreviewRequest = false;
    private boolean mPipelineIsOn = false;

    public PreviewRenderingRequestTask() {
        mPreviewPipeline = new CachingPipeline(FiltersManager.getPreviewManager(), "Preview");
    }

    public void setOriginal(Bitmap bitmap) {
        mPreviewPipeline.setOriginal(bitmap);
        mPipelineIsOn = true;
    }

    public void postRenderingRequest() {
        if (!mPipelineIsOn) {
            return;
        }
        mHasUnhandledPreviewRequest = true;
        if (postRequest(null)) {
            mHasUnhandledPreviewRequest = false;
        }
    }

    @Override
    public boolean isPriorityTask() {
        return true;
    }

    @Override
    public Result doInBackground(Request message) {
        SharedBuffer buffer = ImageManager.getImage().getPreviewBuffer();
        SharedPreset preset = ImageManager.getImage().getPreviewPreset();
        ImagePreset renderingPreset = preset.dequeuePreset();
        if (renderingPreset != null) {
            mPreviewPipeline.compute(buffer, renderingPreset, 0);
            // set the preset we used in the buffer for later inspection UI-side
            buffer.getProducer().setPreset(renderingPreset);
            buffer.getProducer().sync();
            buffer.swapProducer(); // push back the result
        }
        return null;
    }

    @Override
    public void onResult(Result message) {
        ImageManager.getImage().notifyObservers();
        if (mHasUnhandledPreviewRequest) {
            postRenderingRequest();
        }
    }

    public void setPipelineIsOn(boolean pipelineIsOn) {
        mPipelineIsOn = pipelineIsOn;
    }
}
