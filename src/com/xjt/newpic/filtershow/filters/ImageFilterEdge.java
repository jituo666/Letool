package com.xjt.newpic.filtershow.filters;

import android.graphics.Bitmap;

import com.xjt.newpic.R;

public class ImageFilterEdge extends SimpleImageFilter {
    private static final String SERIALIZATION_NAME = "EDGE";
    public ImageFilterEdge() {
        mName = "Edge";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterRepresentation representation = super.getDefaultRepresentation();
        representation.setName("Edge");
        representation.setSerializationName(SERIALIZATION_NAME);
        representation.setFilterClass(ImageFilterEdge.class);
        representation.setTextId(R.string.edge);
        representation.setSupportsPartialRendering(true);
        representation.setSampleResource(R.drawable.effect_sample_42);
        return representation;
    }

    native protected void nativeApplyFilter(Bitmap bitmap, int w, int h, float p);

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
        if (getParameters() == null) {
            return bitmap;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float p = getParameters().getValue() + 101;
        p = (float) p / 100;
        nativeApplyFilter(bitmap, w, h, p);
        return bitmap;
    }
}
