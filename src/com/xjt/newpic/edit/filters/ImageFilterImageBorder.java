
package com.xjt.newpic.edit.filters;

import com.xjt.newpic.R;

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

public class ImageFilterImageBorder extends ImageFilter {

    private static final String TAG = ImageFilterImageBorder.class.getSimpleName();

    private FilterImageBorderRepresentation mParameters = null;
    private Resources mResources = null;
    Paint mPaint = new Paint();
    RectF mBounds = new RectF();
    RectF mInsideBounds = new RectF();
    Path mBorderPath = new Path();

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
            Bitmap result = Bitmap.createBitmap(Math.round(b.getWidth() * scale ), Math.round(b.getHeight() * scale), Config.ARGB_8888);
            Canvas c = new Canvas(result);
            c.drawBitmap(b, new Rect(0, 0, b.getWidth(), b.getHeight()), new Rect(0, 0, result.getWidth(), result.getHeight()), null);
            return result;
        } else {
            return b;
        }
    }

    public void applyHelper(Canvas canvas, float scale, int w, int h) {
        if (getParameters() == null) {
            return;
        }
        float size = getParameters().getBorderSize();
        float radius = getParameters().getBorderRadius();

        mPaint.reset();
        Bitmap b = scaleBitmp(BitmapFactory.decodeResource(mResources, R.drawable.edit_border_tile12), scale);

        mPaint.setShader(new BitmapShader(b, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
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
        applyHelper(canvas, scaleFactor, bitmap.getWidth(), bitmap.getHeight());
        return bitmap;
    }

    public void setResources(Resources resources) {
        if (mResources != resources) {
            mResources = resources;
            mDrawables.clear();
        }
    }
}
