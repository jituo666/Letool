package com.xjt.letool.views.layout;

import android.graphics.Rect;

import com.xjt.letool.common.LLog;

public class ContractThumbnailLayout extends ThumbnailLayout {

    private static final String TAG = ContractThumbnailLayout.class.getSimpleName();

    public ContractThumbnailLayout(ThumbnailLayoutSpec spec) {
        mSpec = spec;
    }

    @Override
    protected void initThumbnailLayoutParameters() {
        // Initialize mThumbnailWidth and mThumbnailHeight from mSpec
        int colums = (mWidth > mHeight) ? mSpec.rowsLand : mSpec.rowsPort;
        mThumbnailGap = mSpec.thumbnailGap;
        mThumbnailWidth = Math.max(1, (mWidth - (colums - 1) * mThumbnailGap) / colums);
        mThumbnailHeight = mThumbnailWidth + mSpec.labelHeight;
        mColumnInMinorDirection = colums;
        if (mRenderer != null) {
            mRenderer.onThumbnailSizeChanged(mThumbnailWidth, mThumbnailHeight);
        }
        int count = ((mThumbnailCount + mColumnInMinorDirection - 1) / mColumnInMinorDirection);
        if (WIDE) {
            mContentLengthInMajorDirection = count * mThumbnailWidth + (count - 1) * mThumbnailGap;
        } else {
            mContentLengthInMajorDirection = count * mThumbnailHeight + (count - 1) * mThumbnailGap;
        }
        updateVisibleThumbnailRange();
    }

    @Override
    protected void updateVisibleThumbnailRange() {
        int position = mScrollPosition;
        if (WIDE) {
            int startCol = position / (mThumbnailWidth + mThumbnailGap);
            int start = Math.max(0, mColumnInMinorDirection * startCol);
            int endCol = (position + mWidth + mThumbnailWidth + mThumbnailGap - 1) / (mThumbnailWidth + mThumbnailGap);
            int end = Math.min(mThumbnailCount, mColumnInMinorDirection * endCol);
            setVisibleThumbnailRange(start, end);
        } else {
            int startRow = position / (mThumbnailHeight + mThumbnailGap);
            int start = Math.max(0, mColumnInMinorDirection * startRow);
            int endRow = (position + mHeight + mThumbnailHeight + mThumbnailGap - 1)
                    / (mThumbnailHeight + mThumbnailGap);
            int end = Math.min(mThumbnailCount, mColumnInMinorDirection * endRow);
            setVisibleThumbnailRange(start, end);
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
        if (!WIDE && columnIdx >= mColumnInMinorDirection) {
            return INDEX_NONE;
        }
        if (WIDE && rowIdx >= mColumnInMinorDirection) {
            return INDEX_NONE;
        }
        if (absoluteX % (mThumbnailWidth + mThumbnailGap) >= mThumbnailWidth) {
            return INDEX_NONE;
        }
        if (absoluteY % (mThumbnailHeight + mThumbnailGap) >= mThumbnailHeight) {
            return INDEX_NONE;
        }
        int index = WIDE ? (columnIdx * mColumnInMinorDirection + rowIdx) : (rowIdx
                * mColumnInMinorDirection + columnIdx);
        return index >= mThumbnailCount ? INDEX_NONE : index;
    }

    @Override
    public Rect getThumbnailRect(int index, Rect rect) {
        int col, row;
        if (WIDE) {
            col = index / mColumnInMinorDirection;
            row = index - col * mColumnInMinorDirection;
        } else {
            row = index / mColumnInMinorDirection;
            col = index - row * mColumnInMinorDirection;
        }
        int x = col * (mThumbnailWidth + mThumbnailGap);
        int y = row * (mThumbnailHeight + mThumbnailGap);
        rect.set(x, y, x + mThumbnailWidth, y + mThumbnailHeight);
        return rect;
    }

    @Override
    protected void updateVisibleTagRange() {
        // TODO Auto-generated method stub
        
    }

}
