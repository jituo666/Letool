
package com.xjt.letool.views.layout;

import android.graphics.Rect;

import com.xjt.letool.common.LLog;

/**
 * @Author Jituo.Xuan
 * @Date 8:17:32 PM Jul 24, 2014
 * @Comments:null
 */
public class ThumbnailSetContractLayout extends ThumbnailLayout {

    private static final String TAG = ThumbnailSetContractLayout.class.getSimpleName();

    public ThumbnailSetContractLayout(ThumbnailLayoutSpec spec) {
        mSpec = spec;
    }

    private void initThumbnailLayoutParameters(int majorUnitSize) {
        int count = ((mThumbnailCount + mColumnInMinorDirection - 1) / mColumnInMinorDirection);
        mContentLengthInMajorDirection = count * majorUnitSize + (count - 1) * mThumbnailGap;
    }

    @Override
    protected void initThumbnailLayoutParameters() {
        // Initialize mThumbnailWidth and mThumbnailHeight from mSpec
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
            initThumbnailLayoutParameters(mThumbnailWidth);
        } else {
            initThumbnailLayoutParameters(mThumbnailHeight);
        }
        if (mThumbnailCount > 0) {
            updateVisibleThumbnailRange();
        }
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
