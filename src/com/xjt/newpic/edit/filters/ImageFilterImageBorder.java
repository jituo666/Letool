
package com.xjt.newpic.edit.filters;

import com.xjt.newpic.R;
import com.xjt.newpic.common.LLog;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
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

    public void applyHelper(Canvas canvas, float scale, int w, int h) {
        if (getParameters() == null) {
            return;
        }
        float size = 16;//getParameters().getBorderSize();
        float radius = 16;//getParameters().getBorderRadius();

        mPaint.reset();
        mPaint.setShader(new BitmapShader(BitmapFactory.decodeResource(mResources, R.drawable.ic_launcher), Shader.TileMode.REPEAT,Shader.TileMode.REPEAT));
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
        applyHelper(canvas, scaleFactor,bitmap.getWidth(), bitmap.getHeight());
        return bitmap;
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
