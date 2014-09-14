package com.xjt.newpic.edit.imageshow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.xjt.newpic.edit.filters.FilterPoint;
import com.xjt.newpic.edit.filters.RedEyeCandidate;

public class ImageRedEye extends ImagePoint {
    private static final String LOGTAG = "ImageRedEyes";
    private RectF mCurrentRect = null;

    public ImageRedEye(Context context) {
        super(context);
    }

    @Override
    public void resetParameter() {
        super.resetParameter();
        invalidate();
    }

    @Override

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (event.getPointerCount() > 1) {
            return true;
        }

        if (didFinishScalingOperation()) {
            return true;
        }

        float ex = event.getX();
        float ey = event.getY();

        // let's transform (ex, ey) to displayed image coordinates
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mCurrentRect = new RectF();
            mCurrentRect.left = ex - mTouchPadding;
            mCurrentRect.top = ey - mTouchPadding;
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            mCurrentRect.right = ex + mTouchPadding;
            mCurrentRect.bottom = ey + mTouchPadding;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mCurrentRect != null) {
                // transform to original coordinates
                Matrix originalNoRotateToScreen = getImageToScreenMatrix(false);
                Matrix originalToScreen = getImageToScreenMatrix(true);
                Matrix invert = new Matrix();
                originalToScreen.invert(invert);
                RectF r = new RectF(mCurrentRect);
                invert.mapRect(r);
                RectF r2 = new RectF(mCurrentRect);
                invert.reset();
                originalNoRotateToScreen.invert(invert);
                invert.mapRect(r2);
                mRedEyeRep.addRect(r, r2);
                this.resetImageCaches(this);
            }
            mCurrentRect = null;
        }
        mEditorRedEye.commitLocalRepresentation();
        invalidate();
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setStyle(Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2);
        if (mCurrentRect != null) {
            paint.setColor(Color.RED);
            RectF drawRect = new RectF(mCurrentRect);
            canvas.drawRect(drawRect, paint);
        }
    }

    @Override
    protected void drawPoint(FilterPoint point, Canvas canvas, Matrix originalToScreen,
            Matrix originalRotateToScreen, Paint paint) {
        RedEyeCandidate candidate = (RedEyeCandidate) point;
        RectF rect = candidate.getRect();
        RectF drawRect = new RectF();
        originalToScreen.mapRect(drawRect, rect);
        RectF fullRect = new RectF();
        originalRotateToScreen.mapRect(fullRect, rect);
        paint.setColor(Color.BLUE);
        canvas.drawRect(fullRect, paint);
        canvas.drawLine(fullRect.centerX(), fullRect.top,
                fullRect.centerX(), fullRect.bottom, paint);
        canvas.drawLine(fullRect.left, fullRect.centerY(),
                fullRect.right, fullRect.centerY(), paint);
        paint.setColor(Color.GREEN);
        float dw = drawRect.width();
        float dh = drawRect.height();
        float dx = fullRect.centerX() - dw / 2;
        float dy = fullRect.centerY() - dh / 2;
        drawRect.set(dx, dy, dx + dw, dy + dh);
        canvas.drawRect(drawRect, paint);
        canvas.drawLine(drawRect.centerX(), drawRect.top,
                drawRect.centerX(), drawRect.bottom, paint);
        canvas.drawLine(drawRect.left, drawRect.centerY(),
                drawRect.right, drawRect.centerY(), paint);
        canvas.drawCircle(drawRect.centerX(), drawRect.centerY(),
                mTouchPadding, paint);
    }
}
