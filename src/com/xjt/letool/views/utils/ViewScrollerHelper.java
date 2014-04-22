
package com.xjt.letool.views.utils;

import com.xjt.letool.utils.Utils;

import android.content.Context;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

public class ViewScrollerHelper {

    private OverScroller mScroller;
    private int mOverflingDistance;
    private boolean mOverflingEnabled;

    public ViewScrollerHelper(Context context) {
        mScroller = new OverScroller(context);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mOverflingDistance = configuration.getScaledOverflingDistance();
    }

    public void setOverfling(boolean enabled) {
        mOverflingEnabled = enabled;
    }

    /**
     * Call this when you want to know the new location. The position will be
     * updated and can be obtained by getPosition(). Returns true if the
     * animation is not yet finished.
     */
    public boolean advanceAnimation(long currentTimeMillis) {
        return mScroller.computeScrollOffset();
    }

    public boolean isFinished() {
        return mScroller.isFinished();
    }

    public void forceFinished() {
        mScroller.forceFinished(true);
    }

    public int getPosition() {
        return mScroller.getCurrX();
    }

    public float getCurrVelocity() {
        return mScroller.getCurrVelocity();
    }

    public void setPosition(int position) {
        mScroller.startScroll(position, 0, 0, 0, 0);
        // This forces the scroller to reach the final position.
        mScroller.abortAnimation();
    }

    public void fling(int velocity, int min, int max) {
        int currX = getPosition();
        mScroller.fling(currX, 0, velocity, 0, min, max, 0, 0, mOverflingEnabled ? mOverflingDistance : 0, 0);
    }

    // Returns the distance that over the scroll limit.
    public int startScroll(int distance, int min, int max) {
        int currPosition = mScroller.getCurrX();
        int finalPosition = mScroller.isFinished() ? currPosition : mScroller.getFinalX();
        int newPosition = Utils.clamp(finalPosition + distance, min, max);
        if (newPosition != currPosition) {
            mScroller.startScroll(currPosition, 0, newPosition - currPosition, 0, 0);
        }
        return finalPosition + distance - newPosition;
    }
}
