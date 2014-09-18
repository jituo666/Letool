package com.xjt.newpic.edit.pipeline;

import android.graphics.Bitmap;

import com.xjt.newpic.edit.filters.FiltersManager;

public class HighresRenderingRequestTask extends ProcessingTask {

    private CachingPipeline mHighresPreviewPipeline = null;
    private boolean mPipelineIsOn = false;

    public void setHighresPreviewScaleFactor(float highResPreviewScale) {
        mHighresPreviewPipeline.setHighResPreviewScaleFactor(highResPreviewScale);
    }

    public void setPreviewScaleFactor(float previewScale) {
        mHighresPreviewPipeline.setPreviewScaleFactor(previewScale);
    }

    static class RenderResult implements Result {
        RenderingRequest request;
    }

    public HighresRenderingRequestTask() {
        mHighresPreviewPipeline = new CachingPipeline( FiltersManager.getHighresManager(), "Highres");
    }

    public void setOriginal(Bitmap bitmap) {
        mHighresPreviewPipeline.setOriginal(bitmap);
    }

    public void setOriginalBitmapHighres(Bitmap originalHires) {
        mPipelineIsOn = true;
    }

    public void stop() {
        mHighresPreviewPipeline.stop();
    }

    public void postRenderingRequest(RenderingRequest request) {
        if (!mPipelineIsOn) {
            return;
        }
        postRequest(request);
    }

    @Override
    public Result doInBackground(Request message) {
        RenderingRequest request = ((RenderingRequest) message);
        RenderResult result = null;
        mHighresPreviewPipeline.renderHighres(request);
        result = new RenderResult();
        result.request = request;
        return result;
    }

    @Override
    public void onResult(Result message) {
        if (message == null) {
            return;
        }
        RenderingRequest request = ((RenderResult) message).request;
        request.markAvailable();
    }

    @Override
    public boolean isDelayedTask() { return true; }
}
