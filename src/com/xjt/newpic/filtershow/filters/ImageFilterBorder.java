
package com.xjt.newpic.filtershow.filters;

import com.xjt.newpic.common.LLog;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LongSparseArray;

public class ImageFilterBorder extends ImageFilter {

    private static final String TAG = ImageFilterBorder.class.getSimpleName();

    private FilterImageBorderRepresentation mParameters = null;
    private Resources mResources = null;

    private LongSparseArray<Drawable> mDrawables = new LongSparseArray<Drawable>();

    public ImageFilterBorder() {
        mName = "Border";
    }

    public void useRepresentation(FilterRepresentation representation) {
        FilterImageBorderRepresentation parameters = (FilterImageBorderRepresentation) representation;
        mParameters = parameters;
    }

    public FilterImageBorderRepresentation getParameters() {
        return mParameters;
    }

    public void freeResources() {
        mDrawables.clear();
    }

    public Bitmap applyHelper(Bitmap bitmap, float scale1, float scale2) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Rect bounds = new Rect(0, 0, (int) (w * scale1), (int) (h * scale1));
        Canvas canvas = new Canvas(bitmap);
        canvas.scale(scale2, scale2);
        Drawable drawable = getDrawable(getParameters().getDrawableResource(), bitmap.getHeight());
        drawable.setBounds(bounds);
        drawable.draw(canvas);
        return bitmap;
    }

    @SuppressLint("NewApi")
    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
        if (getParameters() == null || getParameters().getDrawableResource() == 0) {
            return bitmap;
        }
        float scale2 = scaleFactor * 2.0f;
        float scale1 = 1 / scale2;
        return applyHelper(bitmap, scale1, scale2);
    }

    public void setResources(Resources resources) {
        if (mResources != resources) {
            mResources = resources;
            mDrawables.clear();
        }
    }

    @SuppressLint("NewApi")
    public Drawable getDrawable(int rsc, int maxHeight) {
        Drawable drawable = mDrawables.get(rsc);
        if (drawable == null && mResources != null && rsc != 0) {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            int height = BitmapFactory.decodeResource(mResources, rsc).getHeight();
            int sample = 1;
            while (height > maxHeight) {
                sample = sample * 2;
                height = height / 2;
            }
            o = new BitmapFactory.Options();
            o.inSampleSize = sample;
            Bitmap b = BitmapFactory.decodeResource(mResources, rsc, o);
            drawable = new BitmapDrawable(mResources, b);
            mDrawables.put(rsc, drawable);
        }
        return drawable;
    }

}
