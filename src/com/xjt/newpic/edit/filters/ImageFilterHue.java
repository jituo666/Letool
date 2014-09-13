
package com.xjt.newpic.edit.filters;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.editors.BasicEditor;

import android.graphics.Bitmap;

public class ImageFilterHue extends SimpleImageFilter {

    private static final String SERIALIZATION_NAME = "HUE";
    private ColorSpaceMatrix cmatrix = null;

    public ImageFilterHue() {
        mName = "Hue";
        cmatrix = new ColorSpaceMatrix();
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterBasicRepresentation representation =
                (FilterBasicRepresentation) super.getDefaultRepresentation();
        representation.setName("Hue");
        representation.setSerializationName(SERIALIZATION_NAME);
        representation.setFilterClass(ImageFilterHue.class);
        representation.setMinimum(-180);
        representation.setMaximum(180);
        representation.setTextId(R.string.hue);
        representation.setEditorId(BasicEditor.ID);
        representation.setSupportsPartialRendering(true);
        representation.setSampleResource(R.drawable.effect_sample_38);
        return representation;
    }

    native protected void nativeApplyFilter(Bitmap bitmap, int w, int h, float[] matrix);

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
        if (getParameters() == null) {
            return bitmap;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float value = getParameters().getValue();
        cmatrix.identity();
        cmatrix.setHue(value);

        nativeApplyFilter(bitmap, w, h, cmatrix.getMatrix());

        return bitmap;
    }
}
