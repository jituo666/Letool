
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

    private Bitmap scaleBitmp(Bitmap b, float sw, float sh) {

        if (sw != 1.0f || sh != 1.f) {
            Bitmap result = Bitmap.createBitmap(Math.round(b.getWidth() * sw), Math.round(b.getHeight() * sh), Config.ARGB_8888);
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

        LLog.i(TAG, "----------------------applyHelper:" + ImageManager.getImage().getOriginalBounds()
                + " w:" + w + " h:" + h + " scale:" + scale);
        Rect orig = ImageManager.getImage().getOriginalBounds();
        Bitmap brush = BitmapFactory.decodeResource(mResources, mParameters.getDrawableResource());
        int minM = Math.min(orig.width(), orig.height());

        float fscaleW = 1.0f, fscaleH = 1.0f;
        if (minM < brush.getWidth() * 8f) {
            scale = minM / 8.f / brush.getWidth();
            LLog.i(TAG, "----0------scale:" + scale + " minM:" + minM);
            if (minM == orig.width()) {
                int round = Math.round(orig.height() / (minM / 8.f));
                fscaleW = scale;
                fscaleH = orig.height() * 1.0f / round / brush.getHeight();
            } else {
                int round = Math.round(orig.width() * 1.0f / (minM / 8.f));
                LLog.i(TAG, "----0------round:" + round + " minM:" + minM);
                fscaleW = orig.width() * 1.0f / round / brush.getWidth();
                fscaleH = scale;
            }
        } else {
            fscaleW = (orig.width() * 1.f / Math.round(orig.width() / brush.getWidth())) / brush.getWidth();
            fscaleH = (orig.height() * 1.f / Math.round(orig.height() / brush.getHeight())) / brush.getHeight();
        }

        LLog.i(TAG, "----1------rw:" + fscaleW + " rh:" + fscaleH);
        fscaleW = fscaleW * (w * 1.f / orig.width());
        fscaleH = fscaleH * (w * 1.f / orig.width());

        LLog.i(TAG, "----2------sw:" + fscaleW + " sh:" + (fscaleH) + " r:" + w * 1.f / orig.width());
        final Bitmap fb = scaleBitmp(brush, fscaleW, fscaleH);
        LLog.i(TAG, "----3------wr:" + w * 1.f / fb.getWidth() + " hr:" + (h * 1.f / fb.getHeight()));
        mPaint.setShader(new BitmapShader(fb, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
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
