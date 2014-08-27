package com.xjt.newpic.filtershow.filters;

import android.graphics.RectF;

public class RedEyeCandidate implements FilterPoint {
    RectF mRect = new RectF();
    RectF mBounds = new RectF();

    public RedEyeCandidate(RedEyeCandidate candidate) {
        mRect.set(candidate.mRect);
        mBounds.set(candidate.mBounds);
    }

    public RedEyeCandidate(RectF rect, RectF bounds) {
        mRect.set(rect);
        mBounds.set(bounds);
    }

    public boolean equals(RedEyeCandidate candidate) {
        if (candidate.mRect.equals(mRect)
                && candidate.mBounds.equals(mBounds)) {
            return true;
        }
        return false;
    }

    public boolean intersect(RectF rect) {
        return mRect.intersect(rect);
    }

    public RectF getRect() {
        return mRect;
    }
}
