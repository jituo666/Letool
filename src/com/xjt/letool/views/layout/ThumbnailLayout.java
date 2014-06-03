
package com.xjt.letool.views.layout;

import android.graphics.Rect;

import com.xjt.letool.common.LLog;
import com.xjt.letool.view.ThumbnailView;
import com.xjt.letool.view.ThumbnailView.Renderer;
import com.xjt.letool.views.layout.ThumbnailExpandLayout.TimelineTag;

import java.util.ArrayList;

public abstract class ThumbnailLayout {

    private static final String TAG = ThumbnailLayout.class.getSimpleName();

    public static final boolean WIDE = false;
    public static final int INDEX_NONE = -1;

    protected int mVisibleThumbnailStart;
    protected int mVisibleThumbnailEnd;

    protected int mThumbnailCount;
    protected int mThumbnailWidth;
    protected int mThumbnailHeight;
    protected int mThumbnailGap;

    protected ThumbnailLayoutSpec mSpec;
    protected Renderer mRenderer;

    protected int mWidth;
    protected int mHeight;

    protected int mColumnInMinorDirection; // treat it as columns
    protected int mContentLengthInMajorDirection;
    protected int mScrollPosition;

    private LayoutListener mLayoutListener;

    protected ArrayList<TimelineTag> mSortTags;

    public interface LayoutListener {

        public void onLayoutFinshed(int count);
    }

    public int getThumbnailCount() {
        return mThumbnailCount;
    }

    public int getThumbnailWidth() {
        return mThumbnailWidth;
    }

    public int getThumbnailHeight() {
        return mThumbnailHeight;
    }

    public int getVisibleThumbnailStart() {
        return mVisibleThumbnailStart;
    }

    public int getVisibleThumbnailEnd() {
        return mVisibleThumbnailEnd;
    }

    public int getScrollLimit() {
        int limit = WIDE ? mContentLengthInMajorDirection - mWidth : mContentLengthInMajorDirection - mHeight;
        return limit <= 0 ? 0 : limit;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setLayoutListener(LayoutListener l) {
        mLayoutListener = l;
    }

    public void setThumbnailViewSize(int width, int height) {
        mWidth = width;
        mHeight = height;
        initThumbnailLayoutParameters();
    }

    public void setRenderer(ThumbnailView.Renderer render) {
        mRenderer = render;
    }

    public void setThumbnailCount(int thumbnailCount) {
        setThumbnailCount(thumbnailCount, null);
    }

    public void setThumbnailCount(int thumbnailCount, ArrayList<TimelineTag> tags) {
        if (thumbnailCount == mThumbnailCount)
            return;
        mSortTags = tags;
        mThumbnailCount = thumbnailCount;
        initThumbnailLayoutParameters();
        if (mLayoutListener != null) {
            mLayoutListener.onLayoutFinshed(thumbnailCount);
        }
    }

    public void setScrollPosition(int position) {
        if (mScrollPosition == position)
            return;
        mScrollPosition = position;
        updateVisibleTagRange();
        updateVisibleThumbnailRange();
    }

    protected void setVisibleThumbnailRange(int start, int end) {
        if (start == mVisibleThumbnailStart && end == mVisibleThumbnailEnd)
            return;
        if (start < end) {
            mVisibleThumbnailStart = start;
            mVisibleThumbnailEnd = end;
        } else {
            mVisibleThumbnailStart = mVisibleThumbnailEnd = 0;
        }
        if (mRenderer != null) {
            mRenderer.onVisibleThumbnailRangeChanged(mVisibleThumbnailStart, mVisibleThumbnailEnd);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public abstract Rect getThumbnailRect(int thumbnailIndex, Rect rect);

    public abstract int getThumbnailIndexByPosition(float x, float y);

    protected abstract void initThumbnailLayoutParameters();

    protected abstract void updateVisibleThumbnailRange();

    protected abstract void updateVisibleTagRange();

}
