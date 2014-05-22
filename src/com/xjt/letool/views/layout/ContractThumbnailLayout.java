package com.xjt.letool.views.layout;

import android.graphics.Rect;

public class ContractThumbnailLayout extends ThumbnailLayout {

    private static final String TAG = "ContractThumbnailLayout";

    public ContractThumbnailLayout(ThumbnailLayoutSpec spec) {
        mSpec = spec;
    }

    @Override
    protected void initThumbnailParameters() {
        // Initialize mThumbnailWidth and mThumbnailHeight from mSpec
        int colums = (mWidth > mHeight) ? mSpec.rowsLand : mSpec.rowsPort;
        mThumbnailGap = mSpec.thumbnailGap;
        mThumbnailWidth = Math.max(1, (mWidth - (colums - 1) * mThumbnailGap) / colums);
        mThumbnailHeight = mThumbnailWidth + mSpec.labelHeight;
        mUnitCount = colums;
        if (mRenderer != null) {
            mRenderer.onThumbnailSizeChanged(mThumbnailWidth, mThumbnailHeight);
        }
        int count = ((mThumbnailCount + mUnitCount - 1) / mUnitCount);
        if (WIDE) {
            mContentLength = count * mThumbnailWidth + (count - 1) * mThumbnailGap;
        } else {
            mContentLength = count * mThumbnailHeight + (count - 1) * mThumbnailGap;
        }
        updateVisibleThumbnailRange();
    }

    @Override
    protected void updateVisibleThumbnailRange() {
        int position = mScrollPosition;
        if (WIDE) {
            int startCol = position / (mThumbnailWidth + mThumbnailGap);
            int start = Math.max(0, mUnitCount * startCol);
            int endCol = (position + mWidth + mThumbnailWidth + mThumbnailGap - 1) / (mThumbnailWidth + mThumbnailGap);
            int end = Math.min(mThumbnailCount, mUnitCount * endCol);
            setVisibleRange(start, end);
        } else {
            int startRow = position / (mThumbnailHeight + mThumbnailGap);
            int start = Math.max(0, mUnitCount * startRow);
            int endRow = (position + mHeight + mThumbnailHeight + mThumbnailGap - 1)
                    / (mThumbnailHeight + mThumbnailGap);
            int end = Math.min(mThumbnailCount, mUnitCount * endRow);
            setVisibleRange(start, end);
        }
    }

    @Override
    public int getThumbnailIndexByPosition(float x, float y) {
        int absoluteX = Math.round(x) + (WIDE ? mScrollPosition : 0);
        int absoluteY = Math.round(y) + (WIDE ? 0 : mScrollPosition);
        if (absoluteX < 0 || absoluteY < 0) {
            return INDEX_NONE;
        }
        int columnIdx = absoluteX / (mThumbnailWidth + mThumbnailGap);
        int rowIdx = absoluteY / (mThumbnailHeight + mThumbnailGap);
        if (!WIDE && columnIdx >= mUnitCount) {
            return INDEX_NONE;
        }
        if (WIDE && rowIdx >= mUnitCount) {
            return INDEX_NONE;
        }
        if (absoluteX % (mThumbnailWidth + mThumbnailGap) >= mThumbnailWidth) {
            return INDEX_NONE;
        }
        if (absoluteY % (mThumbnailHeight + mThumbnailGap) >= mThumbnailHeight) {
            return INDEX_NONE;
        }
        int index = WIDE ? (columnIdx * mUnitCount + rowIdx) : (rowIdx
                * mUnitCount + columnIdx);
        return index >= mThumbnailCount ? INDEX_NONE : index;
    }

    @Override
    public Rect getThumbnailRect(int index, Rect rect) {
        int col, row;
        if (WIDE) {
            col = index / mUnitCount;
            row = index - col * mUnitCount;
        } else {
            row = index / mUnitCount;
            col = index - row * mUnitCount;
        }
        int x = col * (mThumbnailWidth + mThumbnailGap);
        int y = row * (mThumbnailHeight + mThumbnailGap);
        rect.set(x, y, x + mThumbnailWidth, y + mThumbnailHeight);
        return rect;
    }

}
