package com.xjt.newpic.edit.category;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

import com.nineoldandroids.view.ViewHelper;
import com.xjt.newpic.R;
import com.xjt.newpic.common.ApiHelper;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.edit.NpEditActivity;
import com.xjt.newpic.edit.ui.SelectionRenderer;

public class CategoryView extends CategoryIconView implements View.OnClickListener {

    private static final String TAG = CategoryView.class.getSimpleName();

    public static final int VERTICAL = 0;
    public static final int HORIZONTAL = 1;
    private Paint mPaint = new Paint();
    private CategoryAction mAction;
    private Paint mSelectPaint;
    private CategoryAdapter mAdapter;
    private int mSelectionStroke;
    private Paint mBorderPaint;
    private int mBorderStroke;
    private float mStartTouchX = 0;
    private float mStartTouchY = 0;
    private float mDeleteSlope = 20;
    private int mSelectionColor = Color.WHITE;
    private int mSpacerColor = Color.WHITE;
    private boolean mCanBeRemoved = false;
    private long mDoubleActionLast = 0;
    private long mDoubleTapDelay = 250;
    private Rect mTempRect = new Rect();

    public CategoryView(Context context) {
        super(context);
        setOnClickListener(this);
        Resources res = getResources();
        mSelectionStroke = res.getDimensionPixelSize(R.dimen.thumbnail_margin);
        mSelectPaint = new Paint();
        mSelectPaint.setStyle(Paint.Style.FILL);
        mSelectionColor = res.getColor(R.color.filtershow_category_selection);
        mSpacerColor = res.getColor(R.color.filtershow_categoryview_text);

        mSelectPaint.setColor(mSelectionColor);
        mBorderPaint = new Paint(mSelectPaint);
        mBorderPaint.setColor(Color.BLACK);
        mBorderStroke = mSelectionStroke / 3;
    }

    @Override
    public boolean isHalfImage() {
        if (mAction == null) {
            return false;
        }
        if (mAction.getType() == CategoryAction.CROP_VIEW) {
            return true;
        }
        if (mAction.getType() == CategoryAction.ADD_ACTION) {
            return true;
        }
        return false;
    }

    private boolean canBeRemoved() {
        return mCanBeRemoved;
    }

    private void drawSpacer(Canvas canvas) {
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mSelectionColor);
        if (getOrientation() == CategoryView.VERTICAL) {
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, getHeight() / 5, mPaint);
        } else {
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 5, mPaint);
        }
    }

    @Override
    public boolean needsCenterText() {
        if (mAction != null && mAction.getType() == CategoryAction.ADD_ACTION) {
            return true;
        }
        return super.needsCenterText();
    }

    public void onDraw(Canvas canvas) {
        if (mAction != null) {
            if (mAction.getType() == CategoryAction.SPACER) {
                drawSpacer(canvas);
                return;
            }
            if (mAction.isDoubleAction()) {
                return;
            }
            mTempRect.set(0, 0, getWidth(), getHeight());
            mAction.setImageFrame(mTempRect, getOrientation());
            Bitmap image = mAction.getImage();
            if (image != null) {
                setBitmap(image);
            } else {
                LLog.i(TAG, "**************** ondraw image is null");
            }
        }
        super.onDraw(canvas);
        if (mAdapter.isSelected(this)) {
            SelectionRenderer.drawSelection(canvas, 0, 0, getWidth(), getHeight(), mSelectionStroke, mSelectPaint, mBorderStroke, mBorderPaint);
        }
    }

    public void setAction(CategoryAction action, CategoryAdapter adapter) {
        mAction = action;
        setText(mAction.getName());
        mAdapter = adapter;
        mCanBeRemoved = action.canBeRemoved();
        setUseOnlyDrawable(false);
        if (mAction.getType() == CategoryAction.ADD_ACTION) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.filtershow_add);
            setBitmap(bitmap);
            setUseOnlyDrawable(true);
            setText(getResources().getString(R.string.filtershow_add_button_looks));
        } else {
            setBitmap(mAction.getImage());
        }
        invalidate();
    }

    @Override
    public void onClick(View view) {
        NpEditActivity activity = (NpEditActivity) getContext();
        if (mAction.getType() == CategoryAction.ADD_ACTION) {
            //
        } else if (mAction.getType() != CategoryAction.SPACER) {
            if (mAction.isDoubleAction()) {
                long current = System.currentTimeMillis() - mDoubleActionLast;
                if (current < mDoubleTapDelay) {
                    activity.showRepresentation(mAction.getRepresentation());
                }
                mDoubleActionLast = System.currentTimeMillis();
            } else {
                activity.showRepresentation(mAction.getRepresentation());
            }
            mAdapter.setSelected(this);
        }
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);
        NpEditActivity activity = (NpEditActivity) getContext();

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            activity.startTouchAnimation(this, event.getX(), event.getY());
        }
        if (!canBeRemoved()) {
            return ret;
        }
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mStartTouchY = event.getY();
            mStartTouchX = event.getX();
        }
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (ApiHelper.AT_LEAST_11) {
                setTranslationX(0);
                setTranslationY(0);
            } else {
                ViewHelper.setTranslationX(this, 0);
                ViewHelper.setTranslationY(this, 0);
            }
        }
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            float delta = event.getY() - mStartTouchY;
            if (getOrientation() == CategoryView.VERTICAL) {
                delta = event.getX() - mStartTouchX;
            }
            if (Math.abs(delta) > mDeleteSlope) {
                activity.setHandlesSwipeForView(this, mStartTouchX, mStartTouchY);
            }
        }
        return true;
    }

    public void delete() {
        mAdapter.remove(mAction);
    }
}
