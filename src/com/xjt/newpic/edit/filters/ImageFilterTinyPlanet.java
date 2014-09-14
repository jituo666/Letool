package com.xjt.newpic.edit.filters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.edit.cache.BitmapCache;
import com.xjt.newpic.edit.cache.ImageLoader;
import com.xjt.newpic.edit.imageshow.MasterImage;
import com.xjt.newpic.edit.pipeline.ImagePreset;

/**
 * An image filter which creates a tiny planet projection.
 */
public class ImageFilterTinyPlanet extends SimpleImageFilter {

    private static final String LOGTAG = ImageFilterTinyPlanet.class.getSimpleName();
    private FilterTinyPlanetRepresentation mParameters = new FilterTinyPlanetRepresentation(0);
    public static final String GOOGLE_PANO_NAMESPACE = "http://ns.google.com/photos/1.0/panorama/";

    public static final String CROPPED_AREA_IMAGE_WIDTH_PIXELS = "CroppedAreaImageWidthPixels";
    public static final String CROPPED_AREA_IMAGE_HEIGHT_PIXELS = "CroppedAreaImageHeightPixels";
    public static final String CROPPED_AREA_FULL_PANO_WIDTH_PIXELS = "FullPanoWidthPixels";
    public static final String CROPPED_AREA_FULL_PANO_HEIGHT_PIXELS = "FullPanoHeightPixels";
    public static final String CROPPED_AREA_LEFT = "CroppedAreaLeftPixels";
    public static final String CROPPED_AREA_TOP = "CroppedAreaTopPixels";

    public ImageFilterTinyPlanet() {
        mName = "TinyPlanet";
    }

    @Override
    public void useRepresentation(FilterRepresentation representation) {
        FilterTinyPlanetRepresentation parameters = (FilterTinyPlanetRepresentation) representation;
        mParameters = parameters;
    }

    @Override
    public FilterRepresentation getDefaultRepresentation() {
        return new FilterTinyPlanetRepresentation(0);
    }

    native protected void nativeApplyFilter(
            Bitmap bitmapIn, int width, int height, Bitmap bitmapOut, int outSize, float scale,
            float angle);

    @Override
    public Bitmap apply(Bitmap bitmapIn, float scaleFactor, int quality) {
        int w = bitmapIn.getWidth();
        int h = bitmapIn.getHeight();
        int outputSize = (int) (w / 2f);
        ImagePreset preset = getEnvironment().getImagePreset();
        Bitmap mBitmapOut = null;
        if (preset != null) {
            XMPMeta xmp = ImageLoader.getXmpObject(MasterImage.getImage().getActivity());
            // Do nothing, just use bitmapIn as is if we don't have XMP.
            if (xmp != null) {
                bitmapIn = applyXmp(bitmapIn, xmp, w);
            }
        }
        if (mBitmapOut != null) {
            if (outputSize != mBitmapOut.getHeight()) {
                mBitmapOut = null;
            }
        }
        while (mBitmapOut == null) {
            try {
                mBitmapOut = getEnvironment().getBitmap(outputSize,
                        outputSize, BitmapCache.TINY_PLANET);
            } catch (java.lang.OutOfMemoryError e) {
                System.gc();
                outputSize /= 2;
                LLog.v(LOGTAG, "No memory to create Full Tiny Planet create half");
            }
        }
        nativeApplyFilter(bitmapIn, bitmapIn.getWidth(), bitmapIn.getHeight(), mBitmapOut,
                outputSize, mParameters.getZoom() / 100f, mParameters.getAngle());

        return mBitmapOut;
    }

    private Bitmap applyXmp(Bitmap bitmapIn, XMPMeta xmp, int intermediateWidth) {
        try {
            int croppedAreaWidth =
                    getInt(xmp, CROPPED_AREA_IMAGE_WIDTH_PIXELS);
            int croppedAreaHeight =
                    getInt(xmp, CROPPED_AREA_IMAGE_HEIGHT_PIXELS);
            int fullPanoWidth =
                    getInt(xmp, CROPPED_AREA_FULL_PANO_WIDTH_PIXELS);
            int fullPanoHeight =
                    getInt(xmp, CROPPED_AREA_FULL_PANO_HEIGHT_PIXELS);
            int left = getInt(xmp, CROPPED_AREA_LEFT);
            int top = getInt(xmp, CROPPED_AREA_TOP);

            if (fullPanoWidth == 0 || fullPanoHeight == 0) {
                return bitmapIn;
            }
            // Make sure the intermediate image has the similar size to the
            // input.
            Bitmap paddedBitmap = null;
            float scale = intermediateWidth / (float) fullPanoWidth;
            while (paddedBitmap == null) {
                try {
                    paddedBitmap = Bitmap.createBitmap(
                            (int) (fullPanoWidth * scale), (int) (fullPanoHeight * scale),
                            Bitmap.Config.ARGB_8888);
                } catch (java.lang.OutOfMemoryError e) {
                    System.gc();
                    scale /= 2;
                }
            }
            Canvas paddedCanvas = new Canvas(paddedBitmap);

            int right = left + croppedAreaWidth;
            int bottom = top + croppedAreaHeight;
            RectF destRect = new RectF(left * scale, top * scale, right * scale, bottom * scale);
            paddedCanvas.drawBitmap(bitmapIn, null, destRect, null);
            bitmapIn = paddedBitmap;
        } catch (XMPException ex) {
            // Do nothing, just use bitmapIn as is.
        }
        return bitmapIn;
    }

    private static int getInt(XMPMeta xmp, String key) throws XMPException {
        if (xmp.doesPropertyExist(GOOGLE_PANO_NAMESPACE, key)) {
            return xmp.getPropertyInteger(GOOGLE_PANO_NAMESPACE, key);
        } else {
            return 0;
        }
    }
}
