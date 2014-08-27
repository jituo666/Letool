package com.xjt.newpic.filtershow.filters;

import com.xjt.newpic.R;

import android.graphics.Bitmap;

public class ImageFilterVibrance extends SimpleImageFilter {
    private static final String SERIALIZATION_NAME = "VIBRANCE";
    public ImageFilterVibrance() {
        mName = "Vibrance";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterBasicRepresentation representation =
                (FilterBasicRepresentation) super.getDefaultRepresentation();
        representation.setName("Vibrance");
        representation.setSerializationName(SERIALIZATION_NAME);
        representation.setFilterClass(ImageFilterVibrance.class);
        representation.setTextId(R.string.vibrance);
        representation.setMinimum(-100);
        representation.setMaximum(100);
        representation.setDefaultValue(0);
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
