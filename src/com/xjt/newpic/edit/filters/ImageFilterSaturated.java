package com.xjt.newpic.edit.filters;

import com.xjt.newpic.R;

import android.graphics.Bitmap;

public class ImageFilterSaturated extends SimpleImageFilter {
    private static final String SERIALIZATION_NAME = "SATURATED";
    public ImageFilterSaturated() {
        mName = "Saturated";
    }

    @Override
    public FilterRepresentation getDefaultRepresentation() {
        FilterBasicRepresentation representation =
                (FilterBasicRepresentation) super.getDefaultRepresentation();
        representation.setName("Saturated");
        representation.setSerializationName(SERIALIZATION_NAME);
        representation.setFilterClass(ImageFilterSaturated.class);
        representation.setTextId(R.string.saturation);
        representation.setMinimum(-100);
        representation.setMaximum(100);
        representation.setDefaultValue(0);
        representation.setSupportsPartialRendering(true);
        return representation;
    }

    native protected void nativeApplyFilter(Bitmap bitmap, int w, int h, float saturation);

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
        if (getParameters() == null) {
            return bitmap;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int p = getParameters().getValue();
        float value = 1 +  p / 100.0f;
        nativeApplyFilter(bitmap, w, h, value);
        return bitmap;
    }
}
