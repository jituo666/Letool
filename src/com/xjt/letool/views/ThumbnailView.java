package com.xjt.letool.views;

import com.xjt.letool.LetoolActivity;
import com.xjt.letool.SynchronizedHandler;
import com.xjt.letool.opengl.GLESCanvas;
import com.xjt.letool.utils.Utils;
import com.xjt.letool.views.layout.ThumbnailLayout;
import com.xjt.letool.views.layout.ThumbnailLayoutSpec;

import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;

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

    public static final int OVERSCROLL_3D = 0;
    public static final int OVERSCROLL_SYSTEM = 1;
    public static final int OVERSCROLL_NONE = 2;

    // to prevent allocating memory
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
    private ThumbnailRenderer mRenderer;

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

    public static interface ThumbnailRenderer {
        public void prepareDrawing();

        public void onVisibleRangeChanged(int visibleStart, int visibleEnd);

        public void onSlotSizeChanged(int width, int height);

        public int renderSlot(GLESCanvas canvas, int index, int pass, int width, int height);
    }

    private class MyGestureListener implements GestureDetector.OnGestureListener {
        private boolean isDown;

        private void cancelDown(boolean byLongPress) {
            if (!isDown)
                return;
            isDown = false;
            mListener.onUp(byLongPress);
        }

        @Override
        public boolean onDown(MotionEvent arg0) {
            return false;
        }

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
        public void onLongPress(MotionEvent e) {
            cancelDown(true);
            if (mDownInScrolling)
                return;
            lockRendering();
            try {
                int index = mLayout.getSlotIndexByPosition(e.getX(), e.getY());
                if (index != ThumbnailLayout.INDEX_NONE)
                    mListener.onLongTap(index);
            } finally {
                unlockRendering();
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
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
                int index = mLayout.getSlotIndexByPosition(e.getX(), e.getY());
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
            cancelDown(false);
            if (mDownInScrolling)
                return true;
            int index = mLayout.getSlotIndexByPosition(e.getX(), e.getY());
            if (index != ThumbnailLayout.INDEX_NONE)
                mListener.onSingleTapUp(index);
            return true;
        }

    }

    public void setThumbnailLayoutSpec(ThumbnailLayoutSpec spec) {
        mLayout.setSlotSpec(spec);
    }

    public void setThumbnailRenderer(ThumbnailRenderer render) {
        mRenderer = render;
        if (mRenderer != null) {
            mRenderer.onSlotSizeChanged(mLayout.getSlotWidth(), mLayout.getSlotHeight());
            mRenderer.onVisibleRangeChanged(getVisibleStart(), getVisibleEnd());
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setUserInteractionListener(UserInteractionListener listener) {
        mUIListener = listener;
    }

    public int getVisibleStart() {
        return mLayout.getVisibleStart();
    }

    public int getVisibleEnd() {
        return mLayout.getVisibleEnd();
    }

    public ThumbnailView(LetoolActivity activity, ThumbnailLayoutSpec spec) {
        mGestureDetector = new GestureDetector(activity, new MyGestureListener());
        mScroller = new ScrollerHelper(activity);
        mHandler = new SynchronizedHandler(activity.getGLController());
        setThumbnailLayoutSpec(spec);
    }

    protected void onScrollPositionChanged(int newPosition) {
        int limit = mLayout.getScrollLimit();
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

    public void makeThumbnailVisible(int index) {
        Rect rect = mLayout.getSlotRect(index, mTempRect);
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

    // Make sure we are still at a resonable scroll position after the size
    // is changed (like orientation change). We choose to keep the center
    // visible slot still visible. This is arbitrary but reasonable.
    @Override
    protected void onLayout(boolean changeSize, int l, int t, int r, int b) {
        mLayout.setSize(r - l, b - t);
        int visibleIndex = (mLayout.getVisibleStart() + mLayout.getVisibleEnd()) / 2;
        makeThumbnailVisible(visibleIndex);
    }

    @Override
    protected boolean onTouch(MotionEvent event) {
        return true;
    }

    @Override
    protected void render(GLESCanvas canvas) {

    }
}
