package com.xjt.newpic.filtershow.filters;

import com.xjt.newpic.R;
import com.xjt.newpic.filtershow.editors.ImageOnlyEditor;

import android.graphics.Bitmap;

public class ImageFilterWBalance extends ImageFilter {
    private static final String SERIALIZATION_NAME = "WBALANCE";
    private static final String TAG = "ImageFilterWBalance";

    public ImageFilterWBalance() {
        mName = "WBalance";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterRepresentation representation = new FilterDirectRepresentation("WBalance",0);
        representation.setSerializationName(SERIALIZATION_NAME);
        representation.setFilterClass(ImageFilterWBalance.class);
        representation.setFilterType(FilterRepresentation.TYPE_WBALANCE);
        representation.setTextId(R.string.wbalance);
        representation.setShowParameterValue(false);
        representation.setEditorId(ImageOnlyEditor.ID);
        representation.setSupportsPartialRendering(true);
        representation.setIsBooleanFilter(true);
        return representation;
    }

    @Override
    public void useRepresentation(FilterRepresentation representation) {

    }

    native protected void nativeApplyFilter(Bitmap bitmap, int w, int h, int locX, int locY);

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        nativeApplyFilter(bitmap, w, h, -1, -1);
        return bitmap;
    }

}
