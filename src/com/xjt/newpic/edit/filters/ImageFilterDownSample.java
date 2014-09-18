package com.xjt.newpic.edit.filters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.cache.ImageLoader;
import com.xjt.newpic.edit.imageshow.ImageManager;

public class ImageFilterDownSample extends SimpleImageFilter {
    private static final String SERIALIZATION_NAME = "DOWNSAMPLE";
    private static final int ICON_DOWNSAMPLE_FRACTION = 8;
    private ImageLoader mImageLoader;

    public ImageFilterDownSample(ImageLoader loader) {
        mName = "Downsample";
        mImageLoader = loader;
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterBasicRepresentation representation = (FilterBasicRepresentation) super.getDefaultRepresentation();
        representation.setName("Downsample");
        representation.setSerializationName(SERIALIZATION_NAME);

        representation.setFilterClass(ImageFilterDownSample.class);
        representation.setMaximum(100);
        representation.setMinimum(1);
        representation.setValue(50);
        representation.setDefaultValue(50);
        representation.setPreviewValue(3);
        representation.setTextId(R.string.downsample);
        return representation;
    }

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
        if (getParameters() == null) {
            return bitmap;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int p = getParameters().getValue();

        // size of original precached image
        Rect size = ImageManager.getImage().getOriginalBounds();
        int orig_w = size.width();
        int orig_h = size.height();

        if (p > 0 && p < 100) {
            // scale preview to same size as the resulting bitmap from a "save"
            int newWidth = orig_w * p / 100;
            int newHeight = orig_h * p / 100;

            // only scale preview if preview isn't already scaled enough
            if (newWidth <= 0 || newHeight <= 0 || newWidth >= w || newHeight >= h) {
                return bitmap;
            }
            Bitmap ret = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            if (ret != bitmap) {
                bitmap.recycle();
            }
            return ret;
        }
        return bitmap;
    }
}
