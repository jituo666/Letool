
package com.xjt.newpic.edit.filters;

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

    private Bitmap scaleBitmp(Bitmap b, float scale) {
        if (scale < 1.0f) {
            Bitmap result = Bitmap.createBitmap(Math.round(b.getWidth() * scale ), Math.round(b.getHeight() * scale), Config.ARGB_8888);
            Canvas c = new Canvas(result);
            c.drawBitmap(b, new Rect(0, 0, b.getWidth(), b.getHeight()), new Rect(0, 0, result.getWidth(), result.getHeight()), null);
            return result;
        } else {
            return b;
        }
    }

    public void applyHelper(Canvas canvas, int w, int h) {
        float scale = 1.0f;
        if (getParameters() == null) {
            return;
        }
        float size = getParameters().getBorderSize();
        float radius = getParameters().getBorderRadius();
        int texture = getParameters().getTexture();

        mPaint.reset();
        Rect orig = ImageManager.getImage().getOriginalBounds();
        Bitmap brush = BitmapFactory.decodeResource(mResources, texture);
        int maxM = Math.max(orig.width(), orig.height());
        if (maxM < brush.getWidth() * 4) {
            scale = maxM / 4.f / brush.getWidth() * w * 1.f / orig.width();
        } else {
            scale = w * 1.f / orig.width();
        }

        mPaint.setShader(new BitmapShader(scaleBitmp(BitmapFactory.decodeResource(mResources, texture), scale), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
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
