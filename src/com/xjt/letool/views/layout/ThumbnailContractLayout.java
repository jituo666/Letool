package com.xjt.letool.views.layout;

import android.graphics.Rect;

public class ThumbnailContractLayout extends ThumbnailLayout {

    public ThumbnailContractLayout(ThumbnailLayoutSpec spec) {
        mSpec = spec;
    }

    // Calculate
    // (1) mUnitCount: the number of slots we can fit into one column (or row).
    // (2) mContentLength: the width (or height) we need to display all the
    //     columns (rows).
    // (3) padding[]: the vertical and horizontal padding we need in order
    //     to put the slots towards to the center of the display.
    //
    // The "major" direction is the direction the user can scroll. The other
    // direction is the "minor" direction.
    //
    // The comments inside this method are the description when the major
    // directon is horizontal (X), and the minor directon is vertical (Y).
    private void initLayoutParameters(
            int majorLength, int minorLength, /* The view width and height */
            int majorUnitSize, int minorUnitSize, /* The slot width and height */
            int[] padding) {
        int unitCount = (minorLength + mThumbnailGap) / (minorUnitSize + mThumbnailGap);
        if (unitCount == 0)
            unitCount = 1;
        mUnitCount = unitCount;

        // We put extra padding above and below the column.
        int availableUnits = Math.min(mUnitCount, mThumbnailCount);
        int usedMinorLength = availableUnits * minorUnitSize +
                (availableUnits - 1) * mThumbnailGap;
        padding[0] = (minorLength - usedMinorLength) / 2;

        // Then calculate how many columns we need for all slots.
        int count = ((mThumbnailCount + mUnitCount - 1) / mUnitCount);
        mContentLength = count * majorUnitSize + (count - 1) * mThumbnailGap;

        // If the content length is less then the screen width, put
        // extra padding in left and right.
        padding[1] = Math.max(0, (majorLength - mContentLength) / 2);
    }

    @Override
    protected void initLayoutParameters() {
        // Initialize mThumbnailWidth and mThumbnailHeight from mSpec
        if (mSpec.slotWidth != -1) {
            mThumbnailGap = 0;
            mThumbnailWidth = mSpec.slotWidth;
            mThumbnailHeight = mSpec.slotHeight;
        } else {
            int rows = (mWidth > mHeight) ? mSpec.rowsLand : mSpec.rowsPort;
            mThumbnailGap = mSpec.slotGap;
            mThumbnailHeight = Math.max(1, (mHeight - (rows - 1) * mThumbnailGap) / rows);
            mThumbnailWidth = mThumbnailHeight - mSpec.slotHeightAdditional;
        }

        if (mRenderer != null) {
            mRenderer.onThumbnailSizeChanged(mThumbnailWidth, mThumbnailHeight);
        }

        int[] padding = new int[2];
        if (WIDE) {
            initLayoutParameters(mWidth, mHeight, mThumbnailWidth, mThumbnailHeight, padding);
            mVerticalPadding.startAnimateTo(padding[0]);
            mHorizontalPadding.startAnimateTo(padding[1]);
        } else {
            initLayoutParameters(mHeight, mWidth, mThumbnailHeight, mThumbnailWidth, padding);
            mVerticalPadding.startAnimateTo(padding[1]);
            mHorizontalPadding.startAnimateTo(padding[0]);
        }
        updateVisibleThumbnailRange();
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

        int index = WIDE
                ? (columnIdx * mUnitCount + rowIdx)
                : (rowIdx * mUnitCount + columnIdx);

        return index >= mThumbnailCount ? INDEX_NONE : index;
    }

    @Override
    protected void updateVisibleThumbnailRange() {
        int position = mScrollPosition;

        if (WIDE) {
            int startCol = position / (mThumbnailWidth + mThumbnailGap);
            int start = Math.max(0, mUnitCount * startCol);
            int endCol = (position + mWidth + mThumbnailWidth + mThumbnailGap - 1) /
                    (mThumbnailWidth + mThumbnailGap);
            int end = Math.min(mThumbnailCount, mUnitCount * endCol);
            setVisibleRange(start, end);
        } else {
            int startRow = position / (mThumbnailHeight + mThumbnailGap);
            int start = Math.max(0, mUnitCount * startRow);
            int endRow = (position + mHeight + mThumbnailHeight + mThumbnailGap - 1) /
                    (mThumbnailHeight + mThumbnailGap);
            int end = Math.min(mThumbnailCount, mUnitCount * endRow);
            setVisibleRange(start, end);
        }
    }
}
