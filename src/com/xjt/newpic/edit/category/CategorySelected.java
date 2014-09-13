
package com.xjt.newpic.edit.category;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.xjt.newpic.R;
import com.xjt.newpic.common.ApiHelper;
import com.xjt.newpic.common.LLog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

public class CategorySelected extends View implements AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    private static final String TAG = CategorySelected.class.getSimpleName();

    private Paint mPaint = new Paint();
    private int mMargin = 20;
    private int mRidus;
    private Point mPoint = new Point(0, 0);
    private ValueAnimator mWaveAnimator;

    public CategorySelected(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMargin = getResources().getDimensionPixelSize(R.dimen.touch_circle_size);
        mRidus = (int) getResources().getDimension(R.dimen.category_panel_icon_size) / 2;
        createAnimation();
    }

    private void createAnimation() {
        mWaveAnimator = ValueAnimator.ofFloat(1.0f, 2.0f);
        mWaveAnimator.setDuration(300);
        mWaveAnimator.addUpdateListener(this);
        mWaveAnimator.addListener(this);
    }

    public void onDraw(Canvas canvas) {
        mPaint.reset();
        mPaint.setStrokeWidth(mMargin);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.argb(128, 128, 128, 128));
        canvas.drawCircle(mPoint.x, mPoint.y, mRidus * (Float) mWaveAnimator.getAnimatedValue() - mMargin, mPaint);
    }

    public void setCircleCenter(Point p) {
        mPoint = p;
    }

    public void startWaveAnimator() {
        mWaveAnimator.start();
    }

    @SuppressLint("NewApi")
    @Override
    public void onAnimationUpdate(ValueAnimator v) {
        if (ApiHelper.AT_LEAST_11) {
            setAlpha((2f - (Float) v.getAnimatedValue()));
        } else {
            ViewHelper.setAlpha(this, (2f - (Float) v.getAnimatedValue()));
        }
        invalidate();
    }

    @Override
    public void onAnimationCancel(Animator a) {
        setVisibility(View.INVISIBLE);

    }

    @Override
    public void onAnimationEnd(Animator a) {
        setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAnimationRepeat(Animator a) {

    }

    @Override
    public void onAnimationStart(Animator a) {

    }

}
