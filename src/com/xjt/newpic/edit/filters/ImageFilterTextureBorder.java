
package com.xjt.newpic.edit.filters;

import com.xjt.newpic.common.LLog;
import com.xjt.newpic.edit.imageshow.ImageManager;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LongSparseArray;

public class ImageFilterTextureBorder extends ImageFilter {

    private static final String TAG = ImageFilterTextureBorder.class.getSimpleName();

    private FilterTextureBorderRepresentation mParameters = null;
    private Resources mResources = null;
    Paint mPaint = new Paint();
    RectF mBounds = new RectF();
    RectF mInsideBounds = new RectF();
    Path mBorderPath = new Path();

    private LongSparseArray<Drawable> mDrawables = new LongSparseArray<Drawable>();

    public ImageFilterTextureBorder() {
        mName = "Border";
    }

    public void useRepresentation(FilterRepresentation representation) {
        FilterTextureBorderRepresentation parameters = (FilterTextureBorderRepresentation) representation;
        mParameters = parameters;
    }

    public FilterTextureBorderRepresentation getParameters() {
        return mParameters;
    }

    public void freeResources() {
        mDrawables.clear();
    }

    private Bitmap scaleBitmp(Bitmap b, double sw, double sh) {

        if (sw != 1.0f || sh != 1.f) {
            Bitmap result = Bitmap.createBitmap((int) Math.round(b.getWidth() * sw), (int) Math.round(b.getHeight() * sh), Config.ARGB_8888);
            Canvas c = new Canvas(result);
            c.drawBitmap(b, new Rect(0, 0, b.getWidth(), b.getHeight()), new Rect(0, 0, result.getWidth(), result.getHeight()), null);
            return result;
        } else {
            return b;
        }
    }

    public void applyHelper(Canvas canvas, int w, int h) {
        double scale = 1.0f;
        if (getParameters() == null) {
            return;
        }
        float size = getParameters().getBorderSize();
        float radius = getParameters().getBorderRadius();
        int texture = getParameters().getTexture();

        mPaint.reset();

        LLog.i(TAG, "----------------------applyHelper:" + ImageManager.getImage().getOriginalBounds()
                + " w:" + w + " h:" + h + " scale:" + scale);

        Rect orig = ImageManager.getImage().getOriginalBounds();
        Bitmap brush = BitmapFactory.decodeResource(mResources, texture);
        int minM = Math.min(orig.width(), orig.height());

        double fscaleW = 1.0f, fscaleH = 1.0f;
        if (minM < brush.getWidth() * 8f) {
            scale = minM / 8.f / brush.getWidth();
            LLog.i(TAG, "----0------scale:" + scale + " minM:" + minM);
            if (minM == orig.width()) {
                int round = Math.round(orig.height() * 1.f / (minM / 8.f));
                fscaleW = scale;
                fscaleH = orig.height() * 1.0f / round / brush.getHeight();
            } else {
                int round = Math.round(orig.width() * 1.0f / (minM / 8.f));
                LLog.i(TAG, "----0------round:" + round + " minM:" + minM);
                fscaleW = (orig.width() * 1.0f / round) / brush.getWidth();
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
        mBounds.set(0, 0, w, h);
        mBorderPath.reset();
        mBorderPath.moveTo(0, 0);
        float bs = size / 100.f * mBounds.width();
        float r = radius / 100.f * mBounds.width();
        mInsideBounds.set(mBounds.left + bs, mBounds.top + bs, mBounds.right - bs, mBounds.bottom - bs);
        mBorderPath.moveTo(mBounds.left, mBounds.top);
        mBorderPath.lineTo(mBounds.right, mBounds.top);
        mBorderPath.lineTo(mBounds.right, mBounds.bottom);
        mBorderPath.lineTo(mBounds.left, mBounds.bottom);
        mBorderPath.addRoundRect(mInsideBounds, r, r, Path.Direction.CCW);
        canvas.drawPath(mBorderPath, mPaint);

    }

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
        if (getParameters() == null) {
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
