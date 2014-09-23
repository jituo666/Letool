
package com.xjt.newpic.edit.filters;

import com.xjt.newpic.common.LLog;
import com.xjt.newpic.edit.imageshow.ImageManager;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LongSparseArray;

public class ImageFilterImageBorder extends ImageFilter {

    private static final String TAG = ImageFilterImageBorder.class.getSimpleName();

    private FilterImageBorderRepresentation mParameters = null;
    private Resources mResources = null;
    private Paint mPaint = new Paint();

    private LongSparseArray<Drawable> mDrawables = new LongSparseArray<Drawable>();

    public ImageFilterImageBorder() {
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

    private Bitmap scaleBitmp(Bitmap b, float scale) {
        if (scale != 1.0f) {
            Bitmap result = Bitmap.createBitmap(Math.round(b.getWidth() * scale), Math.round(b.getHeight() * scale), Config.ARGB_8888);
            Canvas c = new Canvas(result);
            c.drawBitmap(b, new Rect(0, 0, b.getWidth(), b.getHeight()), new Rect(0, 0, result.getWidth(), result.getHeight()), null);
            return result;
        } else {
            return b;
        }
    }

    public void applyHelper(Canvas canvas, int w, int h) {
        float scale = 1.f;
        mPaint.reset();
        Rect orig = ImageManager.getImage().getOriginalBounds();
        Bitmap brush = BitmapFactory.decodeResource(mResources, mParameters.getDrawableResource());
        int maxM = Math.max(orig.width(), orig.height());
        if (maxM < brush.getWidth() * 4) {
            scale = maxM / 4.f / brush.getWidth() * w * 1.f / orig.width();
        } else {
            scale = w * 1.f / orig.width();
        }

        LLog.i(TAG, "----------------------applyHelper:" + ImageManager.getImage().getOriginalBounds()
                + " w:" + w + " h:" + h + " scale:" + scale);
        mPaint.setShader(new BitmapShader(scaleBitmp(brush, scale), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
        mPaint.setAntiAlias(true);
        canvas.drawRect(new Rect(0, 0, w, h), mPaint);
    }

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
        if (getParameters() == null || getParameters().getDrawableResource() == 0) {
            return bitmap;
        }
        Canvas canvas = new Canvas(bitmap);
        applyHelper(canvas, bitmap.getWidth(), bitmap.getHeight());
        return bitmap;
    }

    public void setResources(Resources resources) {
        if (mResources != resources) {
            mResources = resources;
            mDrawables.clear();
        }
    }
}
