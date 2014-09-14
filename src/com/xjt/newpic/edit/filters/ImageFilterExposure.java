package com.xjt.newpic.edit.filters;

import com.xjt.newpic.R;

import android.graphics.Bitmap;

public class ImageFilterExposure extends SimpleImageFilter {
    private static final String SERIALIZATION_NAME = "EXPOSURE";
    public ImageFilterExposure() {
        mName = "Exposure";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterBasicRepresentation representation =
                (FilterBasicRepresentation) super.getDefaultRepresentation();
        representation.setName("Exposure");
        representation.setSerializationName(SERIALIZATION_NAME);
        representation.setFilterClass(ImageFilterExposure.class);
        representation.setTextId(R.string.exposure);
        representation.setMinimum(-100);
        representation.setMaximum(100);
        representation.setDefaultValue(0);
        representation.setSampleResource(R.drawable.effect_sample_29);
        representation.setSupportsPartialRendering(true);
        return representation;
    }

    native protected void nativeApplyFilter(Bitmap bitmap, int w, int h, float bright);

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
        if (getParameters() == null) {
            return bitmap;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float value = getParameters().getValue();
        nativeApplyFilter(bitmap, w, h, value);
        return bitmap;
    }
}
