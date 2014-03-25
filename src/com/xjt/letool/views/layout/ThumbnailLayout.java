package com.xjt.letool.views.layout;

import android.graphics.Rect;

import com.xjt.letool.anims.Animation;
import com.xjt.letool.views.ThumbnailView.ThumbnailRenderer;

public abstract class ThumbnailLayout {

    public static final boolean WIDE = true;
    public static final int INDEX_NONE = -1;

    protected int mVisibleStart;
    protected int mVisibleEnd;

    protected int mSlotCount;
    protected int mSlotWidth;
    protected int mSlotHeight;
    protected int mSlotGap;

    protected ThumbnailLayoutSpec mSpec;
    protected ThumbnailRenderer mRenderer;

    protected int mWidth;
    protected int mHeight;

    protected int mUnitCount;
    protected int mContentLength;
    protected int mScrollPosition;
    protected IntegerAnimation mVerticalPadding = new IntegerAnimation();
    protected IntegerAnimation mHorizontalPadding = new IntegerAnimation();

    public void setSlotSpec(ThumbnailLayoutSpec spec) {
        mSpec = spec;
    }

    public int getSlotWidth() {
        return mSlotWidth;
    }

    public int getSlotHeight() {
        return mSlotHeight;
    }

    public int getVisibleStart() {
        return mVisibleStart;
    }

    public int getVisibleEnd() {
        return mVisibleEnd;
    }

    public int getScrollLimit() {
        int limit = WIDE ? mContentLength - mWidth : mContentLength - mHeight;
        return limit <= 0 ? 0 : limit;
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean setSlotCount(int slotCount) {
        if (slotCount == mSlotCount)
            return false;
        if (mSlotCount != 0) {
            mHorizontalPadding.setEnabled(true);
            mVerticalPadding.setEnabled(true);
        }
        mSlotCount = slotCount;
        int hPadding = mHorizontalPadding.getTarget();
        int vPadding = mVerticalPadding.getTarget();
        initLayoutParameters();
        return vPadding != mVerticalPadding.getTarget() || hPadding != mHorizontalPadding.getTarget();
    }

    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
        initLayoutParameters();
    }

    public void setScrollPosition(int position) {
        if (mScrollPosition == position)
            return;
        mScrollPosition = position;
        updateVisibleSlotRange();
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public abstract Rect getSlotRect(int index, Rect rect);

    public abstract int getSlotIndexByPosition(float x, float y);

    protected abstract void initLayoutParameters();

    protected abstract void updateVisibleSlotRange();

    protected void setVisibleRange(int start, int end) {
        if (start == mVisibleStart && end == mVisibleEnd)
            return;
        if (start < end) {
            mVisibleStart = start;
            mVisibleEnd = end;
        } else {
            mVisibleStart = mVisibleEnd = 0;
        }
        if (mRenderer != null) {
            mRenderer.onVisibleRangeChanged(mVisibleStart, mVisibleEnd);
        }
    }

    public boolean advanceAnimation(long animTime) {
        // use '|' to make sure both sides will be executed
        return mVerticalPadding.calculate(animTime) | mHorizontalPadding.calculate(animTime);
    }

    public static class IntegerAnimation extends Animation {
        private int mTarget;
        private int mCurrent = 0;
        private int mFrom = 0;
        private boolean mEnabled = false;

        public void setEnabled(boolean enabled) {
            mEnabled = enabled;
        }

        public void startAnimateTo(int target) {
            if (!mEnabled) {
                mTarget = mCurrent = target;
                return;
            }
            if (target == mTarget)
                return;

            mFrom = mCurrent;
            mTarget = target;
            setDuration(180);
            start();
        }

        public int get() {
            return mCurrent;
        }

        public int getTarget() {
            return mTarget;
        }

        @Override
        protected void onCalculate(float progress) {
            mCurrent = Math.round(mFrom + progress * (mTarget - mFrom));
            if (progress == 1f)
                mEnabled = false;
        }
    }
}
