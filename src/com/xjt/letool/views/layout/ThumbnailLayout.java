package com.xjt.letool.views.layout;

import android.graphics.Rect;

import com.xjt.letool.anims.Animation;
import com.xjt.letool.views.ThumbnailView.Render;

public abstract class ThumbnailLayout {

    private static final String TAG = "ThumbnailLayout";

    public static final boolean WIDE = true;
    public static final int INDEX_NONE = -1;

    protected int mVisibleStart;
    protected int mVisibleEnd;

    protected int mThumbnailCount;
    protected int mThumbnailWidth;
    protected int mThumbnailHeight;
    protected int mThumbnailGap;

    protected ThumbnailLayoutSpec mSpec;
    protected Render mRenderer;

    protected int mWidth;
    protected int mHeight;

    protected int mUnitCount;
    protected int mContentLength;
    protected int mScrollPosition;
    protected IntegerAnimation mVerticalPadding = new IntegerAnimation();
    protected IntegerAnimation mHorizontalPadding = new IntegerAnimation();

    public int getThumbnailCount() {
        return mThumbnailCount;
    }

    public int getThumbnailWidth() {
        return mThumbnailWidth;
    }

    public int getThumbnailHeight() {
        return mThumbnailHeight;
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
    public boolean setThumbnailCount(int slotCount) {
        if (slotCount == mThumbnailCount)
            return false;
        if (mThumbnailCount != 0) {
            mHorizontalPadding.setEnabled(true);
            mVerticalPadding.setEnabled(true);
        }
        mThumbnailCount = slotCount;
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
        updateVisibleThumbnailRange();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public abstract Rect getThumbnailRect(int index, Rect rect);

    public abstract int getThumbnailIndexByPosition(float x, float y);

    protected abstract void initLayoutParameters();

    protected abstract void updateVisibleThumbnailRange();

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
