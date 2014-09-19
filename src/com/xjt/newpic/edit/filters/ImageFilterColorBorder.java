package com.xjt.newpic.edit.filters;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class ImageFilterColorBorder extends ImageFilter {

    private static final String TAG = ImageFilterColorBorder.class.getSimpleName();

    private FilterColorBorderRepresentation mParameters = null;
    Paint mPaint = new Paint();
    RectF mBounds = new RectF();
    RectF mInsideBounds = new RectF();
    Path mBorderPath = new Path();

    public ImageFilterColorBorder() {
        mName = "Border";
    }

    public FilterRepresentation getDefaultRepresentation() {
        return new FilterColorBorderRepresentation(0, Color.WHITE, 3, 2);
    }

    public void useRepresentation(FilterRepresentation representation) {
        FilterColorBorderRepresentation parameters = (FilterColorBorderRepresentation) representation;
        mParameters = parameters;
    }

    public FilterColorBorderRepresentation getParameters() {
        return mParameters;
    }
    private Resources mResources = null;
    public void setResources(Resources resources) {
        if (mResources != resources) {
            mResources = resources;
        }
    }
    
    private void applyHelper(Canvas canvas, int w, int h) {
        if (getParameters() == null) {
            return;
        }
        float size = getParameters().getBorderSize();
        float radius = getParameters().getBorderRadius();

        mPaint.reset();
        mPaint.setColor(getParameters().getColor());
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
        Canvas canvas = new Canvas(bitmap);
        applyHelper(canvas, bitmap.getWidth(), bitmap.getHeight());
        return bitmap;
    }

}
