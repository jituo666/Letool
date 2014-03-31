package com.xjt.letool.views.layout;

import com.xjt.letool.common.LLog;

import android.graphics.Rect;

public class ThumbnailContractLayout extends ThumbnailLayout {

    private static final String TAG = "ThumbnailContractLayout";

    public ThumbnailContractLayout(ThumbnailLayoutSpec spec) {
        mSpec = spec;
    }

    // Calculate
    // (1) mUnitCount: the number of thumbnails we can fit into one column (or row).
    // (2) mContentLength: the width (or height) we need to display all the columns (rows).
    // (3) padding[]: the vertical and horizontal padding we need in order to put the thumbnails towards to the center of the display.
    //
    // The "major" direction is the direction the user can scroll. The other direction is the "minor" direction.
    // The comments inside this method are the description when the major direction is horizontal (X), and the minor direction is vertical (Y).
    private void initThumbnailLayoutParameters(int majorLength, int minorLength, int majorUnitSize, int minorUnitSize, int[] padding) {
        int unitCount = (minorLength + mThumbnailGap) / (minorUnitSize + mThumbnailGap);
        if (unitCount == 0)
            unitCount = 1;
        mUnitCount = unitCount;

        // We put extra padding above and below the column.
        int availableUnits = Math.min(mUnitCount, mThumbnailCount);
        int usedMinorLength = availableUnits * minorUnitSize + (availableUnits - 1) * mThumbnailGap;
        padding[0] = (minorLength - usedMinorLength) / 2;

        // Then calculate how many columns we need for all thumbnails.
        int count = ((mThumbnailCount + mUnitCount - 1) / mUnitCount);
        mContentLength = count * majorUnitSize + (count - 1) * mThumbnailGap;

        // If the content length is less then the screen width, put extra padding in left and right.
        padding[1] = Math.max(0, (majorLength - mContentLength) / 2);
        LLog.i(TAG, " initLayoutParameters rows:" + count + " column;" + unitCount);
    }

    @Override
    protected void initThumbnailParameters() {
        // Initialize mThumbnailWidth and mThumbnailHeight from mSpec
        mThumbnailGap = mSpec.thumbnailGap;
        if (!WIDE) {
            int rows = (mWidth < mHeight) ? mSpec.rowsLand : mSpec.rowsPort;
            mThumbnailWidth = Math.max(1, (mWidth - (rows - 1) * mThumbnailGap) / rows);
            mThumbnailHeight = mThumbnailWidth + mSpec.labelHeight;
        } else {

            int rows = (mWidth > mHeight) ? mSpec.rowsLand : mSpec.rowsPort;
            mThumbnailHeight = Math.max(1, (mHeight - (rows - 1) * mThumbnailGap) / rows);
            mThumbnailWidth = mThumbnailHeight - mSpec.labelHeight;
        }

        if (mRenderer != null) {
            mRenderer.onThumbnailSizeChanged(mThumbnailWidth, mThumbnailHeight);
        }

        int[] padding = new int[2];
        if (WIDE) {
            initThumbnailLayoutParameters(mWidth, mHeight, mThumbnailWidth, mThumbnailHeight, padding);
            mVerticalPadding.startAnimateTo(padding[0]);
            mHorizontalPadding.startAnimateTo(padding[1]);
        } else {
            initThumbnailLayoutParameters(mHeight, mWidth, mThumbnailHeight, mThumbnailWidth, padding);
            mVerticalPadding.startAnimateTo(padding[1]);
            mHorizontalPadding.startAnimateTo(padding[0]);
        }
        LLog.i(TAG, " padding[0]:" + padding[0] + " padding[1]:" + padding[1]);
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
            int endRow = (position + mHeight + mThumbnailHeight + mThumbnailGap - 1) / (mThumbnailHeight + mThumbnailGap);
            int end = Math.min(mThumbnailCount, mUnitCount * endRow);
            setVisibleRange(start, end);
        }
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

        int x = mHorizontalPadding.get() + col * (mThumbnailWidth + mThumbnailGap);
        int y = mVerticalPadding.get() + row * (mThumbnailHeight + mThumbnailGap);
        rect.set(x, y, x + mThumbnailWidth, y + mThumbnailHeight);
        return rect;
    }

    @Override
    public int getThumbnailIndexByPosition(float x, float y) {
        int absoluteX = Math.round(x) + (WIDE ? mScrollPosition : 0);
        int absoluteY = Math.round(y) + (WIDE ? 0 : mScrollPosition);

        absoluteX -= mHorizontalPadding.get();
        absoluteY -= mVerticalPadding.get();

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

        int index = WIDE ? (columnIdx * mUnitCount + rowIdx) : (rowIdx * mUnitCount + columnIdx);

        return index >= mThumbnailCount ? INDEX_NONE : index;
    }

}
