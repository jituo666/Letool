package com.xjt.newpic.edit.history;

import android.graphics.Bitmap;
import android.util.Log;

import com.xjt.newpic.edit.filters.FilterRepresentation;
import com.xjt.newpic.edit.pipeline.ImagePreset;

public class HistoryItem {
    private static final String LOGTAG = "HistoryItem";
    private ImagePreset mImagePreset;
    private FilterRepresentation mFilterRepresentation;
    private Bitmap mPreviewImage;

    public HistoryItem(ImagePreset preset, FilterRepresentation representation) {
        mImagePreset = preset; // just keep a pointer to the current preset
        if (representation != null) {
            mFilterRepresentation = representation.copy();
        }
    }

    public ImagePreset getImagePreset() {
        return mImagePreset;
    }

    public FilterRepresentation getFilterRepresentation() {
        return mFilterRepresentation;
    }

    public Bitmap getPreviewImage() {
        return mPreviewImage;
    }

    public void setPreviewImage(Bitmap previewImage) {
        mPreviewImage = previewImage;
    }

}
