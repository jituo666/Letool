package com.xjt.newpic.filtershow.pipeline;

import android.graphics.Bitmap;

import com.xjt.newpic.filtershow.filters.FiltersManager;
import com.xjt.newpic.filtershow.imageshow.MasterImage;

public class UpdatePreviewTask extends ProcessingTask {
    private static final String TAG = UpdatePreviewTask.class.getSimpleName();
    
    private CachingPipeline mPreviewPipeline = null;
    private boolean mHasUnhandledPreviewRequest = false;
    private boolean mPipelineIsOn = false;

    public UpdatePreviewTask() {
        mPreviewPipeline = new CachingPipeline(
                FiltersManager.getPreviewManager(), "Preview");
    }

    public void setOriginal(Bitmap bitmap) {
        mPreviewPipeline.setOriginal(bitmap);
        mPipelineIsOn = true;
    }

    public void updatePreview() {
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
        SharedBuffer buffer = MasterImage.getImage().getPreviewBuffer();
        SharedPreset preset = MasterImage.getImage().getPreviewPreset();
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
        MasterImage.getImage().notifyObservers();
        if (mHasUnhandledPreviewRequest) {
            updatePreview();
        }
    }

    public void setPipelineIsOn(boolean pipelineIsOn) {
        mPipelineIsOn = pipelineIsOn;
    }
}
