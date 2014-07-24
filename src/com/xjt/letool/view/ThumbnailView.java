
package com.xjt.letool.view;

import com.xjt.letool.LetoolContext;
import com.xjt.letool.R;
import com.xjt.letool.animations.AnimationTime;
import com.xjt.letool.animations.ThumbnailAnim;
import com.xjt.letool.animations.ThumbnailRisingAnim;
import com.xjt.letool.animations.ThumbnailScatteringAnim;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.SynchronizedHandler;
import com.xjt.letool.utils.RelativePosition;
import com.xjt.letool.utils.Utils;
import com.xjt.letool.views.layout.ThumbnailLayout;
import com.xjt.letool.views.opengl.GLESCanvas;
import com.xjt.letool.views.utils.UIListener;
import com.xjt.letool.views.utils.ViewScrollerHelper;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * @Author Jituo.Xuan
 * @Date 2:50:38 PM Mar 25, 2014
 * @Comments:null
 */

public class ThumbnailView extends GLBaseView {

    private static final String TAG = ThumbnailView.class.getSimpleName();

    public static final int OVERSCROLL_3D = 0;
    public static final int OVERSCROLL_SYSTEM = 1;
    public static final int OVERSCROLL_NONE = 2;

    public static final int RENDER_MORE_PASS = 1;
    public static final int RENDER_MORE_FRAME = 2;

    private int mStartIndex = ThumbnailLayout.INDEX_NONE;

    private ThumbnailAnim mAnimation = null;
    private final Rect mTempRect = new Rect(); // to prevent allocating memory
    private boolean mDownInScrolling;
    private ViewScrollerHelper mScroller;
    private GestureDetector mGestureDetector;
    private UIListener mUIListener;
    private SynchronizedHandler mHandler;
    private Listener mListener;
    private ThumbnailLayout mLayout;
    private Renderer mRenderer;

    private ScrollBarView mScrollBar;

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
            // Log.i(TAG,
            // "--------------------------------------------------Scroll");
            mScroller.startScroll(Math.round(distance), 0, mLayout.getScrollLimit());
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
                invalidate();
                break;
        }
        return true;
    }

    ////////////////////////////////////////////////////////////////Render/////////////////////////////////////////////////////////////////

    public static interface Renderer {

        public void prepareDrawing();

        public void onVisibleThumbnailRangeChanged(int visibleStart, int visibleEnd);

        public void onThumbnailSizeChanged(int width, int height);

        public int renderThumbnail(GLESCanvas canvas, int index, int pass, int width, int height);

    }

    public void setThumbnailRenderer(Renderer render) {
        mRenderer = render;

        if (mRenderer != null) {
            mRenderer.onThumbnailSizeChanged(mLayout.getThumbnailWidth(), mLayout.getThumbnailHeight());
        }
    }

    @Override
    protected void render(GLESCanvas canvas) {
        super.render(canvas);
        if (mRenderer == null || mLayout.getThumbnailCount() == 0)
            return;
        mRenderer.prepareDrawing();
        long animTime = AnimationTime.get();
        boolean more = mScroller.advanceAnimation(animTime);
        updateScrollPosition(mScroller.getPosition(), false);
        more |= advanceAnimation(animTime);
        if (mAnimation != null) {
            more |= mAnimation.calculate(animTime);
        }
        canvas.translate(-mScrollX, -mScrollY);
        //LLog.i(TAG, "render item start:" + mLayout.getVisibleThumbnailStart() + " end:" + mLayout.getVisibleThumbnailEnd());
        for (int i = mLayout.getVisibleThumbnailEnd() - 1; i >= mLayout.getVisibleThumbnailStart(); --i) {
            if ((renderItem(canvas, i, 0) & RENDER_MORE_FRAME) != 0) {
                more = true;
            }
        }

        canvas.translate(mScrollX, mScrollY);
        renderChild(canvas, mScrollBar);
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

    ////////////////////////////////////////////////////////////Layout////////////////////////////////////////////////////////////////////

    public ThumbnailView(LetoolContext activity, ThumbnailLayout layout) {
        mGestureDetector = new GestureDetector(activity.getAppContext(), new MyGestureListener());
        mScroller = new ViewScrollerHelper(activity.getAppContext());
        mHandler = new SynchronizedHandler(activity.getGLController());
        mLayout = layout;
        int w = Math.round(activity.getAppContext().getResources().getDimension(R.dimen.common_scroll_bar_width));
        int h = Math.round(activity.getAppContext().getResources().getDimension(R.dimen.common_scroll_bar_height));
        if (ThumbnailLayout.WIDE) {
            mScrollBar = new ScrollBarView(activity.getAppContext(), h, w);
        } else {
            mScrollBar = new ScrollBarView(activity.getAppContext(), w, h);
        }
        mScrollBar.setVisibility(View.INVISIBLE);
        addComponent(mScrollBar);
    }

    // Make sure we are still at a resonable scroll position after the size
    // is changed (like orientation change). We choose to keep the center
    // visible thumbnail still visible. This is arbitrary but reasonable.
    @SuppressLint("WrongCall")
    @Override
    protected void onLayout(boolean changeSize, int l, int t, int r, int b) {
        if (!changeSize)
            return;
        int w = r - l;
        int h = b - t;
        mScrollBar.layout(0, 0, w, h);
        int visibleCenterIndex = (mLayout.getVisibleThumbnailStart() + mLayout.getVisibleThumbnailEnd()) / 2;
        mLayout.setThumbnailViewSize(r - l, b - t);
        LLog.i(TAG, " onLayout visibleCenterIndex:" + visibleCenterIndex);
        resetVisibleRange(visibleCenterIndex);
        showScrollBarView();
    }

    private void showScrollBarView() {

        if (mLayout.getThumbnailCount() > 0 && mLayout.getScrollLimit() <= 0) {
            mScrollBar.setVisibility(View.INVISIBLE);
        } else if (mLayout.getVisibleThumbnailEnd() > 0) {
            mScrollBar.setVisibility(View.VISIBLE);
        }
    }

    public void setThumbnailCount(int thumbnailCount) {
        mLayout.setThumbnailCount(thumbnailCount);
        // mStartIndex is applied the first time setSlotCount is called.
        if (mStartIndex != ThumbnailLayout.INDEX_NONE) {
            setCenterIndex(mStartIndex);
            mStartIndex = ThumbnailLayout.INDEX_NONE;
        }
        // Reset the scroll position to avoid scrolling over the updated limit.
        setScrollPosition(ThumbnailLayout.WIDE ? mScrollX : mScrollY);
        showScrollBarView();
    }

    public void setCenterIndex(int index) {
        int thumbnailCount = mLayout.getThumbnailCount();
        if (index < 0 || index >= thumbnailCount) {
            return;
        }
        Rect rect = mLayout.getThumbnailRect(index, mTempRect);
        int position = ThumbnailLayout.WIDE ? (rect.left + rect.right - getWidth()) / 2 : (rect.top + rect.bottom - getHeight()) / 2;
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

    public Rect getThumbnailRect(int thumbnailIndex) {
        return mLayout.getThumbnailRect(thumbnailIndex, mTempRect);
    }

    public int getScrollX() {
        return mScrollX;
    }

    public int getScrollY() {
        return mScrollY;
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
        if (mListener != null) {
            mListener.onScrollPositionChanged(position, mLayout.getScrollLimit());
            mScrollBar.setContentPosition(position, mLayout.getScrollLimit());
        }
    }

    public int getVisibleThumbnailStart() {
        return mLayout.getVisibleThumbnailStart();
    }

    public int getVisibleThumbnailEnd() {
        return mLayout.getVisibleThumbnailEnd();
    }
}
