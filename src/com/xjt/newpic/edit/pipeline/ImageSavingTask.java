
package com.xjt.newpic.edit.pipeline;

import android.graphics.Bitmap;
import android.net.Uri;

import com.xjt.newpic.common.LLog;
import com.xjt.newpic.edit.tools.SaveImage;

public class ImageSavingTask extends ProcessingTask {

    private static final String TAG = ImageSavingTask.class.getSimpleName();
    private ProcessingService mProcessingService;

    static class SaveRenderingRequest implements Request {

        Uri selectedUri;
        ImagePreset preset;
        int quality;
        float sizeFactor;
        Bitmap previewImage;
        boolean exit;
    }

    static class UpdateProgress implements Update {

        int max;
        int current;
    }

    static class UpdatePreviewSaved implements Update {

        Uri uri;
        boolean exit;
    }

    static class URIResult implements Result {

        Uri uri;
        boolean exit;
    }

    public ImageSavingTask(ProcessingService service) {
        mProcessingService = service;
    }

    public void postRenderingRequest(Uri selectedUri, ImagePreset preset, Bitmap previewImage, int quality, float sizeFactor, boolean exit) {
        SaveRenderingRequest request = new SaveRenderingRequest();
        request.selectedUri = selectedUri;
        request.preset = preset;
        request.quality = quality;
        request.sizeFactor = sizeFactor;
        request.previewImage = previewImage;
        request.exit = exit;
        postRequest(request);
    }

    //运行在HandlerThread中
    public Result doInBackground(Request message) {
        SaveRenderingRequest request = (SaveRenderingRequest) message;
        Uri selectedUri = request.selectedUri;
        LLog.i(TAG, "---selectedUri:" + (selectedUri == null ? null : selectedUri.toString()));
        Bitmap previewImage = request.previewImage;
        ImagePreset preset = request.preset;
        final boolean exit = request.exit;

        SaveImage saveImage = new SaveImage(mProcessingService, selectedUri, previewImage,
                new SaveImage.Callback() {

                    @Override
                    public void onPreviewSaved(Uri uri) {
                        UpdatePreviewSaved previewSaved = new UpdatePreviewSaved();
                        previewSaved.uri = uri;
                        previewSaved.exit = exit;
                        postUpdate(previewSaved);
                    }

                    @Override
                    public void onProgress(int max, int current) {
                        UpdateProgress updateProgress = new UpdateProgress();
                        updateProgress.max = max;
                        updateProgress.current = current;
                        postUpdate(updateProgress);
                    }
                });
        Uri uri = saveImage.processAndSaveImage(preset, request.quality, request.sizeFactor, request.exit);
        URIResult result = new URIResult();
        result.uri = uri;
        result.exit = request.exit;
        return result;
    }

    @Override
    public void onResult(Result message) {
        URIResult result = (URIResult) message;
        mProcessingService.completeSaveImage(result.uri);
    }

    @Override
    public void onUpdate(Update message) {
        if (message instanceof UpdatePreviewSaved) {
            Uri uri = ((UpdatePreviewSaved) message).uri;
            boolean exit = ((UpdatePreviewSaved) message).exit;
            mProcessingService.completePreviewSaveImage(uri, exit);
        } else if (message instanceof UpdateProgress) {
            UpdateProgress progress = (UpdateProgress) message;
            mProcessingService.updateProgress(progress.max, progress.current);
        }
    }

}
