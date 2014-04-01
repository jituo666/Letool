package com.xjt.letool.views;

import com.xjt.letool.SynchronizedHandler;
import com.xjt.letool.activities.LetoolBaseActivity;
import com.xjt.letool.animations.AnimationTime;
import com.xjt.letool.animations.ThumbnailAnim;
import com.xjt.letool.animations.ThumbnailRisingAnim;
import com.xjt.letool.animations.ThumbnailScatteringAnim;
import com.xjt.letool.common.LLog;
import com.xjt.letool.opengl.GLESCanvas;
import com.xjt.letool.utils.RelativePosition;
import com.xjt.letool.utils.Utils;
import com.xjt.letool.views.layout.ThumbnailLayout;

import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * @Author Jituo.Xuan
 * @Date 2:50:38 PM Mar 25, 2014
 * @Comments:null
 */

public class ThumbnailView extends GLBaseView {

    private static final String TAG = "ThumbnailView";

    public static final int OVERSCROLL_3D = 0;
    public static final int OVERSCROLL_SYSTEM = 1;
    public static final int OVERSCROLL_NONE = 2;

    public static final int RENDER_MORE_PASS = 1;
    public static final int RENDER_MORE_FRAME = 2;

    private int mStartIndex = ThumbnailLayout.INDEX_NONE;

    private ThumbnailAnim mAnimation = null;
    private final Rect mTempRect = new Rect(); // to prevent allocating memory
    private boolean mDownInScrolling;
    private int mOverscrollEffect = OVERSCROLL_SYSTEM;
    private ViewScrollerHelper mScroller;
    private final ViewPaper mPaper = new ViewPaper();
    private GestureDetector mGestureDetector;
    private UIListener mUIListener;
    private SynchronizedHandler mHandler;
    private Listener mListener;
    private ThumbnailLayout mLayout;
    private Renderer mRenderer;

    //////////////////////////////////////////////////////////////Animations////////////////////////////////////////////////////////////////////

    public void startScatteringAnimation(RelativePosition position) {
        mAnimation = new ThumbnailScatteringAnim(position);
        mAnimation.start();
        if (mLayout.getThumbnailHeight() != 0)
            invalidate();
    }

    public void startRisingAnimation() {
        mAnimation = new ThumbnailRisingAnim();
        mAnimation.start();
        if (mLayout.getThumbnailCount() != 0)
            invalidate();
    }

    public boolean advanceAnimation(long animTime) {
        if (mAnimation != null) {
            return mAnimation.calculate(animTime);
        }
        return false;
    }

    public void setOverscrollEffect(int kind) {
        mOverscrollEffect = kind;
        mScroller.setOverfling(kind == OVERSCROLL_SYSTEM);
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

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
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
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            cancelDown(false);
            float distance = ThumbnailLayout.WIDE ? distanceX : distanceY;
            int overDistance = mScroller.startScroll(Math.round(distance), 0, mLayout.getScrollLimit());
            if (mOverscrollEffect == OVERSCROLL_3D && overDistance != 0) {
                mPaper.overScroll(overDistance);
            }
            invalidate();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            LLog.i(TAG, "onSingleTapUp");
            cancelDown(false);
            if (mDownInScrolling)
                return true;
            int index = mLayout.getThumbnailIndexByPosition(e.getX(), e.getY());
            if (index != ThumbnailLayout.INDEX_NONE && mListener != null)
                mListener.onSingleTapUp(index);
            return true;
        }

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
        public void onLongPress(MotionEvent e) {
            LLog.i(TAG, "onLongPress");
            cancelDown(true);
            if (mDownInScrolling)
                return;
            lockRendering();
            try {
                int index = mLayout.getThumbnailIndexByPosition(e.getX(), e.getY());
                if (index != ThumbnailLayout.INDEX_NONE && mListener != null)
                    mListener.onLongTap(index);
            } finally {
                unlockRendering();
            }
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
                    if (mListener != null)
                        mListener.onDown(index);
                }
            } finally {
                root.unlockRenderThread();
            }

        }

    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setUserInteractionListener(UIListener listener) {
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

    ////////////////////////////////////////////////////////////////Render/////////////////////////////////////////////////////////////////

    public static interface Renderer {
        public void prepareDrawing();

        public void onVisibleRangeChanged(int visibleStart, int visibleEnd);

        public void onThumbnailSizeChanged(int width, int height);

        public int renderThumbnail(GLESCanvas canvas, int index, int pass, int width, int height);
    }

    public void setThumbnailRenderer(Renderer render) {
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
        boolean paperActive = isPaperAcitivated();
        more |= paperActive;
        more |= advanceAnimation(animTime);
        canvas.translate(-mScrollX, -mScrollY);

        for (int i = mLayout.getVisibleEnd() - 1; i >= mLayout.getVisibleStart(); --i) {
            if ((renderItem(canvas, i, 0, paperActive) & RENDER_MORE_FRAME) != 0)
                more = true;
        }
        canvas.translate(mScrollX, mScrollY);

        if (more)
            invalidate();
    }

    private int renderItem(GLESCanvas canvas, int index, int pass, boolean paperActive) {
        canvas.save(GLESCanvas.SAVE_FLAG_ALPHA | GLESCanvas.SAVE_FLAG_MATRIX);
        Rect rect = mLayout.getThumbnailRect(index, mTempRect);
        if (paperActive) {
            if (ThumbnailLayout.WIDE) {
                canvas.multiplyMatrix(mPaper.getTransform(rect, mScrollX), 0);
            } else {
                canvas.multiplyMatrix(mPaper.getTransform(rect, mScrollY), 0);
            }
        } else {
            canvas.translate(rect.left, rect.top, 0);
        }
        if (mAnimation != null && mAnimation.isActive()) {
            mAnimation.apply(canvas, index, rect);
        }
        int result = mRenderer.renderThumbnail(canvas, index, pass, rect.right - rect.left, rect.bottom - rect.top);
        canvas.restore();
        return result;
    }

    private boolean isPaperAcitivated() {
        int oldX = mScrollX;
        if (mOverscrollEffect == OVERSCROLL_3D) {
            // Check if an edge is reached and notify mPaper if so.
            int newX = mScrollX;
            int limit = mLayout.getScrollLimit();
            if (oldX > 0 && newX == 0 || oldX < limit && newX == limit) {
                float v = mScroller.getCurrVelocity();
                if (newX == limit)
                    v = -v;
                // I don't know why, but getCurrVelocity() can return NaN.
                if (!Float.isNaN(v)) {
                    mPaper.edgeReached(v);
                }
            }
            return mPaper.advanceAnimation();
        }
        return false;
    }

    ////////////////////////////////////////////////////////////Layout////////////////////////////////////////////////////////////////////

    public ThumbnailView(LetoolBaseActivity activity, ThumbnailLayout layout) {
        mGestureDetector = new GestureDetector(activity, new MyGestureListener());
        mScroller = new ViewScrollerHelper(activity);
        mHandler = new SynchronizedHandler(activity.getGLController());
        mLayout = layout;
    }

    // Make sure we are still at a resonable scroll position after the size
    // is changed (like orientation change). We choose to keep the center
    // visible thumbnail still visible. This is arbitrary but reasonable.
    @Override
    protected void onLayout(boolean changeSize, int l, int t, int r, int b) {
        if (!changeSize)
            return;
        int visibleCenterIndex = (mLayout.getVisibleStart() + mLayout.getVisibleEnd()) / 2;
        mLayout.setThumbnailViewSize(r - l, b - t);
        LLog.i(TAG, " onLayout visibleCenterIndex:" + visibleCenterIndex);
        resetVisibleRange(visibleCenterIndex);
        if (mOverscrollEffect == OVERSCROLL_3D) {
            mPaper.setSize(r - l, b - t);
        }
    }

    /**
     * Return true if the layout parameters have been changed
     * @param thumbnailCount
     * @return
     */
    public void setThumbnailCount(int thumbnailCount) {
       mLayout.setThumbnailCount(thumbnailCount);
        // mStartIndex is applied the first time setSlotCount is called.
        if (mStartIndex != ThumbnailLayout.INDEX_NONE) {
            setCenterIndex(mStartIndex);
            mStartIndex = ThumbnailLayout.INDEX_NONE;
        }
        // Reset the scroll position to avoid scrolling over the updated limit.
        setScrollPosition(ThumbnailLayout.WIDE ? mScrollX : mScrollY);
        return;
    }

    public void setCenterIndex(int index) {
        int thumbnailCount = mLayout.getThumbnailCount();
        if (index < 0 || index >= thumbnailCount) {
            return;
        }
        Rect rect = mLayout.getThumbnailRect(index, mTempRect);
        int position = ThumbnailLayout.WIDE
                ? (rect.left + rect.right - getWidth()) / 2
                : (rect.top + rect.bottom - getHeight()) / 2;
        setScrollPosition(position);
    }

    public void resetVisibleRange(int centerIndex) {
        Rect rect = mLayout.getThumbnailRect(centerIndex, mTempRect);
        int visibleBegin = ThumbnailLayout.WIDE ? mScrollX : mScrollY;
        int visibleLength = ThumbnailLayout.WIDE ? getWidth() : getHeight();
        int visibleEnd = visibleBegin + visibleLength;
        int thumbnailBegin = ThumbnailLayout.WIDE ? rect.left : rect.top;
        int thumbnailEnd = ThumbnailLayout.WIDE ? rect.right : rect.bottom;

        int position = visibleBegin;
        if (visibleLength < thumbnailEnd - thumbnailBegin) {
            position = visibleBegin;
        } else if (thumbnailBegin < visibleBegin) {
            position = thumbnailBegin;
        } else if (thumbnailEnd > visibleEnd) {
            position = thumbnailEnd - visibleLength;
        }
        setScrollPosition(position);
    }

    public void setScrollPosition(int position) {
        position = Utils.clamp(position, 0, mLayout.getScrollLimit());
        mScroller.setPosition(position);
        updateScrollPosition(position, false);
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
        if (mListener != null)
            mListener.onScrollPositionChanged(position, mLayout.getScrollLimit());
    }

    public int getVisibleStart() {
        return mLayout.getVisibleStart();
    }

    public int getVisibleEnd() {
        return mLayout.getVisibleEnd();
    }

}
