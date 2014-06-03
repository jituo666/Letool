
package com.xjt.letool.views.layout;

import android.graphics.Rect;

import com.xjt.letool.common.LLog;

import java.util.ArrayList;

public class ThumbnailExpandLayout extends ThumbnailLayout {

    private static final String TAG = ThumbnailExpandLayout.class.getSimpleName();
    protected ArrayList<ThumbnailPos> mThumbnailPositions;

    protected int mVisibleTagStart;
    protected int mVisibleTagEnd;

    public static class SortTag {

        public String name = "";
        public int index = 0;
        public int offset = 0;
        public int count = 0;
        public boolean checked = false;
        public Rect pos;

        @Override
        public String toString() {
            return "tag index:" + index + ":tagname:" + name + ":tagoffset:" + offset + ":tagcount:" + count;
        }
    }

    public static class ThumbnailPos {

        public ThumbnailPos(boolean checked, Rect r) {
            isChecked = checked;
            rect = r;
        }

        public boolean isChecked;
        public Rect rect;
    }

    public ThumbnailExpandLayout(ThumbnailLayoutSpec spec) {
        mSpec = spec;
    }

    // 计算每个标签产生的Thumbnail位移
    public void computeSortTagOffsets() {
        int size = mSortTags.size();
        SortTag divider0 = mSortTags.get(0);
        divider0.offset = 0;
        if (size == 1) {
            divider0.count = mThumbnailCount;
        } else {
            for (int i = 1; i < size; i++) {
                SortTag dividerCur = mSortTags.get(i);
                SortTag dividerPre = mSortTags.get(i - 1);
                int mode = (dividerCur.index + dividerPre.offset) % mColumnInMinorDirection;
                dividerCur.offset = (mColumnInMinorDirection - mode) % mColumnInMinorDirection
                        + dividerPre.offset;
                dividerPre.count = dividerCur.index - dividerPre.index;
                if (i == size - 1) {
                    dividerCur.count = mThumbnailCount - dividerCur.index;
                }
            }
        }
    }

    private void initLayoutParameters(int majorLength, int minorLength) {
        mThumbnailPositions = new ArrayList<ThumbnailPos>();
        if (mSortTags == null || mSortTags.size() == 0) {
            return;
        }
        final long starttime = System.currentTimeMillis();
        computeSortTagOffsets();
        LLog.i(TAG, "-------------compute tags spend time" + (System.currentTimeMillis() - starttime));
        int yOffset = 0, sortTagOffset = 1;
        int columLength = mThumbnailWidth + mThumbnailGap;
        int rowLength = mThumbnailHeight + mThumbnailGap;
        int tagLength = mSpec.tagHeight + mThumbnailGap;
        // 计算每个slot的位置
        for (int i = 0; i < mThumbnailCount; i++) {
            if (mSortTags.size() != 1) {
                if (sortTagOffset < mSortTags.size()) {
                    if (i < mSortTags.get(sortTagOffset).index) {
                        yOffset = mSortTags.get(sortTagOffset - 1).offset;
                    } else {
                        yOffset = mSortTags.get(sortTagOffset).offset;
                        sortTagOffset += 1;
                    }
                }
            } else {
                mSortTags.get(0).count = mThumbnailCount;
                yOffset = 0;
            }
            int col, row, realIndex;
            realIndex = i + yOffset;
            if (WIDE) {
                col = realIndex / mColumnInMinorDirection;
                row = realIndex - col * mColumnInMinorDirection;
            } else {
                row = realIndex / mColumnInMinorDirection;
                col = realIndex - row * mColumnInMinorDirection;
            }
            int x = col * columLength;
            int y = row * rowLength + (sortTagOffset * tagLength);
            Rect rect = new Rect();
            rect.set(x, y, x + mThumbnailWidth, y + mThumbnailHeight);
            mThumbnailPositions.add(new ThumbnailPos(false, rect));
        }
        for (SortTag tag : mSortTags) {
            Rect rect = mThumbnailPositions.get(tag.index).rect;
            tag.pos = new Rect(rect.left, rect.top - mSpec.tagHeight, rect.left + mSpec.tagWidth, rect.top);
        }
        if (mThumbnailPositions.size() > 0) {
            mContentLengthInMajorDirection = mThumbnailPositions.get(mThumbnailPositions.size() - 1).rect.bottom;
        }
        LLog.i(TAG, "-------------compute all positons spend time" + (System.currentTimeMillis() - starttime));
    }

    @Override
    protected void initThumbnailLayoutParameters() {

        mThumbnailGap = mSpec.thumbnailGap;
        if (!WIDE) {
            mColumnInMinorDirection = (mWidth < mHeight) ? mSpec.rowsLand : mSpec.rowsPort;
            mThumbnailWidth = Math.max(1, (mWidth - (mColumnInMinorDirection - 1) * mThumbnailGap) / mColumnInMinorDirection);
            mThumbnailHeight = mThumbnailWidth + mSpec.labelHeight;
        } else {
            mColumnInMinorDirection = (mWidth > mHeight) ? mSpec.rowsLand : mSpec.rowsPort;
            mThumbnailHeight = Math.max(1, (mHeight - (mColumnInMinorDirection - 1) * mThumbnailGap) / mColumnInMinorDirection);
            mThumbnailWidth = mThumbnailHeight - mSpec.labelHeight;
        }

        if (mRenderer != null) {
            mRenderer.onThumbnailSizeChanged(mThumbnailWidth, mThumbnailHeight);
        }
        if (WIDE) {
            initLayoutParameters(mWidth, mHeight);
        } else {
            initLayoutParameters(mHeight, mWidth);
        }
        updateVisibleTagRange();
        updateVisibleThumbnailRange();
    }

    // 计算tag可视范围
    protected void updateVisibleTagRange() {
        if (mSortTags == null || mSortTags.size() == 0)
            return;
        int position = mScrollPosition;
        int start = 0, end = 0;
        boolean findStart = false;
        for (int i = 0; i < mSortTags.size(); i++) {
            if (!findStart && mSortTags.get(i).pos.top >= (position - mSpec.tagHeight)) {
                start = i;
                findStart = true;
            }
            if (mSortTags.get(i).pos.bottom <= (position + mHeight + mSpec.tagHeight)) {
                end = i + 1;
            }
        }
        if (!findStart) {
            start = mSortTags.size();
        }
        setVisibleTagRange(start, end);
    }

    private void setVisibleTagRange(int start, int end) {
        if (start == mVisibleTagStart && end == mVisibleTagEnd)
            return;
        if (start < end) {
            mVisibleTagStart = start;
            mVisibleTagEnd = end;
        } else {
            mVisibleTagStart = mVisibleTagEnd = 0;
        }
        if (mRenderer != null) {
            LLog.i(TAG, "-------------setVisibleTagRange:" + start + ":" + end + ":mScrollPosition: " + mScrollPosition);
            mRenderer.onVisibleTagRangeChanged(mVisibleTagStart, mVisibleTagEnd);
        }
    }

    public void setThumbnailViewSize(int width, int height) {
        mRenderer.initSortTagMetrics(mSpec.tagWidth, mSpec.tagHeight);
        super.setThumbnailViewSize(width, height);
    }

    // 计算thumbnail可视范围
    @Override
    protected void updateVisibleThumbnailRange() {
        int position = mScrollPosition;
        int start = 0, end = 0;
        boolean findStart = false;
        for (int i = 0; i < mThumbnailPositions.size(); i++) {
            if (!findStart && mThumbnailPositions.get(i).rect.top >= (position - mThumbnailHeight)) {
                start = i;
                findStart = true;
            }
            if (mThumbnailPositions.get(i).rect.bottom <= (position + mHeight + mThumbnailHeight)) {
                end = i + 1;
            }
        }
        if (!findStart) {
            start = mThumbnailCount;
        }
        setVisibleThumbnailRange(start, end);
    }

    public ArrayList<SortTag> getSortTags() {
        return mSortTags;
    }

    public ArrayList<ThumbnailPos> getThumbnailPos() {
        return mThumbnailPositions;
    }

    @Override
    public int getThumbnailIndexByPosition(float x, float y) {
        int absoluteX = Math.round(x) + (WIDE ? mScrollPosition : 0);
        int absoluteY = Math.round(y) + (WIDE ? 0 : mScrollPosition);
        for (int index = 0; index < mThumbnailPositions.size(); index++) {
            if (mThumbnailPositions.get(index).rect.contains(absoluteX, absoluteY)) {
                return index;
            }
        }
        return INDEX_NONE;
    }

    public int getTagIndexByPosition(float x, float y) {
        if (mSortTags == null) {
            return INDEX_NONE;
        }
        int absoluteX = Math.round(x) + (WIDE ? mScrollPosition : 0);
        int absoluteY = Math.round(y) + (WIDE ? 0 : mScrollPosition);
        for (int index = 0; index < mSortTags.size(); index++) {
            /*
             * LLog.i(TAG, "absoluteX:" + absoluteX + " absoluteY" + absoluteY +
             * " :" + mSortTags.get(index).pos.left + ":" +
             * mSortTags.get(index).pos.top + ":" +
             * mSortTags.get(index).pos.right + ":" +
             * mSortTags.get(index).pos.bottom);
             */
            if (mSortTags.get(index).pos.contains(absoluteX, absoluteY)) {
                return index;
            }
        }
        return INDEX_NONE;
    }

    public Rect getThumbnailRect(int index, Rect rect) {
        if (mThumbnailPositions.size() > 0)
            rect = mThumbnailPositions.get(index).rect;
        else {
            rect = new Rect();
        }
        return rect;
    }

    public int getVisibleTagStart() {
        return mVisibleTagStart;
    }

    public int getVisibleTagEnd() {
        return mVisibleTagEnd;
    }

}
