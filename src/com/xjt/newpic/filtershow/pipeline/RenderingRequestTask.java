package com.xjt.newpic.filtershow.pipeline;

import android.graphics.Bitmap;

import com.xjt.newpic.filtershow.filters.FiltersManager;

public class RenderingRequestTask extends ProcessingTask {

    private CachingPipeline mPreviewPipeline = null;
    private boolean mPipelineIsOn = false;

    public void setPreviewScaleFactor(float previewScale) {
        mPreviewPipeline.setPreviewScaleFactor(previewScale);
    }

    static class Render implements Request {
        RenderingRequest request;
    }

    static class RenderResult implements Result {
        RenderingRequest request;
    }

    public RenderingRequestTask() {
        mPreviewPipeline = new CachingPipeline(FiltersManager.getManager(), "Normal");
    }

    public void setOriginal(Bitmap bitmap) {
        mPreviewPipeline.setOriginal(bitmap);
        mPipelineIsOn = true;
    }

    public void stop() {
        mPreviewPipeline.stop();
    }

    public void postRenderingRequest(RenderingRequest request) {
        if (!mPipelineIsOn) {
            return;
        }
        Render render = new Render();
        render.request = request;
        postRequest(render);
    }

    @Override
    public Result doInBackground(Request message) {
        RenderingRequest request = ((Render) message).request;
        RenderResult result = null;
        if (request.getType() == RenderingRequest.GEOMETRY_RENDERING) {
            mPreviewPipeline.renderGeometry(request);
        } else if (request.getType() == RenderingRequest.FILTERS_RENDERING) {
            mPreviewPipeline.renderFilters(request);
        } else {
            mPreviewPipeline.render(request);
        }
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

}
