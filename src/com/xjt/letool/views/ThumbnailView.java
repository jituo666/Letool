package com.xjt.letool.views;

import com.xjt.letool.LetoolBaseActivity;
import com.xjt.letool.SynchronizedHandler;
import com.xjt.letool.anims.Animation;
import com.xjt.letool.anims.AnimationTime;
import com.xjt.letool.common.LLog;
import com.xjt.letool.opengl.GLESCanvas;
import com.xjt.letool.utils.Utils;
import com.xjt.letool.views.layout.ThumbnailLayout;

import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;

/**
 * @Author Jituo.Xuan
 * @Date 2:50:38 PM Mar 25, 2014
 * @Comments:null
 */
/**
 * @Author Jituo.Xuan
 * @Date 2:50:42 PM Mar 25, 2014
 * @Comments:null
 */
public class ThumbnailView extends GLImageView {

    private static final String TAG = "ThumbnailView";

    public static final int OVERSCROLL_3D = 0;
    public static final int OVERSCROLL_SYSTEM = 1;
    public static final int OVERSCROLL_NONE = 2;

    public static final int RENDER_MORE_PASS = 1;
    public static final int RENDER_MORE_FRAME = 2;

    private int mStartIndex = ThumbnailLayout.INDEX_NONE;
    // to prevent allocating memory
    private ThumbnailViewAnim mAnimation = null;
    private final Rect mTempRect = new Rect();
    private boolean mDownInScrolling;
    private int mOverscrollEffect = OVERSCROLL_3D;
    private ScrollerHelper mScroller;
    private final Paper mPaper = new Paper();
    private GestureDetector mGestureDetector;
    private UserInteractionListener mUIListener;
    private SynchronizedHandler mHandler;
    private Listener mListener;
    private ThumbnailLayout mLayout;
    private Render mRenderer;

    //////////////////////////////////////////////////////////////Animations////////////////////////////////////////////////////////////////////

    public void startScatteringAnimation(RelativePosition position) {
        mAnimation = new ScatteringAnimation(position);
        mAnimation.start();
        if (mLayout.getThumbnailHeight() != 0)
            invalidate();
    }

    public void startRisingAnimation() {
        mAnimation = new RisingAnimation();
        mAnimation.start();
        if (mLayout.getThumbnailCount() != 0)
            invalidate();
    }

    public static abstract class ThumbnailViewAnim extends Animation {
        protected float mProgress = 0;

        public ThumbnailViewAnim() {
            setInterpolator(new DecelerateInterpolator(4));
            setDuration(1500);
        }

        @Override
        protected void onCalculate(float progress) {
            mProgress = progress;
        }

        abstract public void apply(GLESCanvas canvas, int slotIndex, Rect target);
    }

    public static class RisingAnimation extends ThumbnailViewAnim {
        private static final int RISING_DISTANCE = 128;

        @Override
        public void apply(GLESCanvas canvas, int slotIndex, Rect target) {
            canvas.translate(0, 0, RISING_DISTANCE * (1 - mProgress));
        }
    }

    public static class ScatteringAnimation extends ThumbnailViewAnim {
        private int PHOTO_DISTANCE = 1000;
        private RelativePosition mCenter;

        public ScatteringAnimation(RelativePosition center) {
            mCenter = center;
        }

        @Override
        public void apply(GLESCanvas canvas, int slotIndex, Rect target) {
            canvas.translate(
                    (mCenter.getX() - target.centerX()) * (1 - mProgress),
                    (mCenter.getY() - target.centerY()) * (1 - mProgress),
                    slotIndex * PHOTO_DISTANCE * (1 - mProgress));
            canvas.setAlpha(mProgress);
        }
    }

    //////////////////////////////////////////////////////////////Event Handler//////////////////////////////////////////////////////////////
    public interface Listener {
        public void onDown(int index);

        public void onUp(boolean followedByLongPress);

        public void onSingleTapUp(int index);

        public void onLongTap(int index);

        public void onScrollPositionChanged(int position, int total);
    }

    public static class SimpleListener implements Listener {
        @Override
        public void onDown(int index) {
        }

        @Override
        public void onUp(boolean followedByLongPress) {
        }

        @Override
        public void onSingleTapUp(int index) {
        }

        @Override
        public void onLongTap(int index) {
        }

        @Override
        public void onScrollPositionChanged(int position, int total) {
        }
    }

    private class MyGestureListener implements GestureDetector.OnGestureListener {
        private boolean isDown;

        private void cancelDown(boolean byLongPress) {
            if (!isDown)
                return;
            isDown = false;
            if (mListener != null)
            mListener.onUp(byLongPress);
        }

        @Override
        public boolean onDown(MotionEvent arg0) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            LLog.i(TAG, "onFling");
            cancelDown(false);
            int scrollLimit = mLayout.getScrollLimit();
            if (scrollLimit == 0)
                return false;
            float velocity = ThumbnailLayout.WIDE ? velocityX : velocityY;
            mScroller.fling((int) -velocity, 0, scrollLimit);
            if (mUIListener != null)
                mUIListener.onUserInteractionBegin();
            invalidate();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            LLog.i(TAG, "onLongPress");
            cancelDown(true);
            if (mDownInScrolling)
                return;
            lockRendering();
            try {
                int index = mLayout.getThumbnailIndexByPosition(e.getX(), e.getY());
                if (index != ThumbnailLayout.INDEX_NONE &&  mListener != null)
                    mListener.onLongTap(index);
            } finally {
                unlockRendering();
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            LLog.i(TAG, "onScroll");
            cancelDown(false);
            float distance = ThumbnailLayout.WIDE ? distanceX : distanceY;
            int overDistance = mScroller.startScroll(
                    Math.round(distance), 0, mLayout.getScrollLimit());
            if (mOverscrollEffect == OVERSCROLL_3D && overDistance != 0) {
                mPaper.overScroll(overDistance);
            }
            invalidate();
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            GLController root = getGLController();
            root.lockRenderThread();
            try {
                if (isDown)
                    return;
                int index = mLayout.getThumbnailIndexByPosition(e.getX(), e.getY());
                if (index != ThumbnailLayout.INDEX_NONE) {
                    isDown = true;
                    mListener.onDown(index);
                }
            } finally {
                root.unlockRenderThread();
            }

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            LLog.i(TAG, "onSingleTapUp");
            cancelDown(false);
            if (mDownInScrolling)
                return true;
            int index = mLayout.getThumbnailIndexByPosition(e.getX(), e.getY());
            if (index != ThumbnailLayout.INDEX_NONE)
                mListener.onSingleTapUp(index);
            return true;
        }

    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setUserInteractionListener(UserInteractionListener listener) {
        mUIListener = listener;
    }

    @Override
    protected boolean onTouch(MotionEvent event) {
        if (mUIListener != null)
            mUIListener.onUserInteraction();
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mDownInScrolling = !mScroller.isFinished();
            mScroller.forceFinished();
            break;
        case MotionEvent.ACTION_UP:
            mPaper.onRelease();
            invalidate();
            break;
        }
        return true;
    }

    ////////////////////////////////////////////////////////////////Render///////////////////////////////////////////////////////

    public static interface Render {
        public void prepareDrawing();

        public void onVisibleRangeChanged(int visibleStart, int visibleEnd);

        public void onThumbnailSizeChanged(int width, int height);

        public int renderThumbnail(GLESCanvas canvas, int index, int pass, int width, int height);
    }

    public void setThumbnailRenderer(Render render) {
        mRenderer = render;
        if (mRenderer != null) {
            mRenderer.onThumbnailSizeChanged(mLayout.getThumbnailWidth(), mLayout.getThumbnailHeight());
            mRenderer.onVisibleRangeChanged(getVisibleStart(), getVisibleEnd());
        }
    }

    @Override
    protected void render(GLESCanvas canvas) {
        super.render(canvas);
        if (mRenderer == null)
            return;
        mRenderer.prepareDrawing();
        long animTime = AnimationTime.get();
        boolean more = mScroller.advanceAnimation(animTime);
        more |= mLayout.advanceAnimation(animTime);
        updateScrollPosition(mScroller.getPosition(), false);

        if (mAnimation != null) {
            more |= mAnimation.calculate(animTime);
        }

        canvas.translate(-mScrollX, -mScrollY);

        for (int i = mLayout.getVisibleEnd() - 1; i >= mLayout.getVisibleStart(); --i) {
            int r = renderItem(canvas, i, 0);
            if ((r & RENDER_MORE_FRAME) != 0)
                more = true;
        }
        canvas.translate(mScrollX, mScrollY);

        if (more)
            invalidate();
    }

    private int renderItem(GLESCanvas canvas, int index, int pass) {
        canvas.save(GLESCanvas.SAVE_FLAG_ALPHA | GLESCanvas.SAVE_FLAG_MATRIX);
        Rect rect = mLayout.getThumbnailRect(index, mTempRect);
        canvas.translate(rect.left, rect.top, 0);
        if (mAnimation != null && mAnimation.isActive()) {
            mAnimation.apply(canvas, index, rect);
        }
        int result = mRenderer.renderThumbnail(canvas, index, pass, rect.right - rect.left, rect.bottom - rect.top);
        canvas.restore();
        return result;
    }

    ////////////////////////////////////////////////////////////Layout//////////////////////////////////////////////////////////

    public ThumbnailView(LetoolBaseActivity activity, ThumbnailLayout layout) {
        mGestureDetector = new GestureDetector(activity, new MyGestureListener());
        mScroller = new ScrollerHelper(activity);
        mHandler = new SynchronizedHandler(activity.getGLController());
        mLayout = layout;
        setThumbnailCount(100);
    }

    // Return true if the layout parameters have been changed
    public boolean setThumbnailCount(int slotCount) {
        boolean changed = mLayout.setThumbnailCount(slotCount);
        // mStartIndex is applied the first time setSlotCount is called.
        if (mStartIndex != ThumbnailLayout.INDEX_NONE) {
            setCenterIndex(mStartIndex);
            mStartIndex = ThumbnailLayout.INDEX_NONE;
        }
        // Reset the scroll position to avoid scrolling over the updated limit.
        setScrollPosition(ThumbnailLayout.WIDE ? mScrollX : mScrollY);
        return changed;
    }

    public void setCenterIndex(int index) {
        int slotCount = mLayout.getThumbnailCount();
        if (index < 0 || index >= slotCount) {
            return;
        }
        Rect rect = mLayout.getThumbnailRect(index, mTempRect);
        int position = ThumbnailLayout.WIDE
                ? (rect.left + rect.right - getWidth()) / 2
                : (rect.top + rect.bottom - getHeight()) / 2;
    }

    // Make sure we are still at a resonable scroll position after the size
    // is changed (like orientation change). We choose to keep the center
    // visible slot still visible. This is arbitrary but reasonable.
    @Override
    protected void onLayout(boolean changeSize, int l, int t, int r, int b) {
        mLayout.setSize(r - l, b - t);
        int visibleIndex = (mLayout.getVisibleStart() + mLayout.getVisibleEnd()) / 2;
        resetVisibleRange(visibleIndex);
    }

    public int getVisibleStart() {
        return mLayout.getVisibleStart();
    }

    public int getVisibleEnd() {
        return mLayout.getVisibleEnd();
    }

    protected void onScrollPositionChanged(int newPosition) {
        int limit = mLayout.getScrollLimit();
        if (mListener != null)
            mListener.onScrollPositionChanged(newPosition, limit);
    }

    private void updateScrollPosition(int position, boolean force) {
        if (!force && (ThumbnailLayout.WIDE ? position == mScrollX : position == mScrollY))
            return;
        if (ThumbnailLayout.WIDE) {
            mScrollX = position;
        } else {
            mScrollY = position;
        }
        mLayout.setScrollPosition(position);
        onScrollPositionChanged(position);
    }

    public void setScrollPosition(int position) {
        position = Utils.clamp(position, 0, mLayout.getScrollLimit());
        mScroller.setPosition(position);
        updateScrollPosition(position, false);
    }

    public void resetVisibleRange(int centerIndex) {
        Rect rect = mLayout.getThumbnailRect(centerIndex, mTempRect);
        int visibleBegin = ThumbnailLayout.WIDE ? mScrollX : mScrollY;
        int visibleLength = ThumbnailLayout.WIDE ? getWidth() : getHeight();
        int visibleEnd = visibleBegin + visibleLength;
        int slotBegin = ThumbnailLayout.WIDE ? rect.left : rect.top;
        int slotEnd = ThumbnailLayout.WIDE ? rect.right : rect.bottom;

        int position = visibleBegin;
        if (visibleLength < slotEnd - slotBegin) {
            position = visibleBegin;
        } else if (slotBegin < visibleBegin) {
            position = slotBegin;
        } else if (slotEnd > visibleEnd) {
            position = slotEnd - visibleLength;
        }
        setScrollPosition(position);
    }
}
