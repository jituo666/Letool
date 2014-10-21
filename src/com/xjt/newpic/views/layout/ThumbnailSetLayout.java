
package com.xjt.newpic.views.layout;

import android.graphics.Rect;

/**
 * @Author Jituo.Xuan
 * @Date 8:17:32 PM Jul 24, 2014
 * @Comments:null
 */
public class ThumbnailSetLayout extends ThumbnailLayoutBase {

    private static final String TAG = ThumbnailSetLayout.class.getSimpleName();

    private boolean mIsList = false;

    public ThumbnailSetLayout(ThumbnailLayoutParam spec, boolean isList) {
        mSpec = spec;
        mIsList = isList;
    }

    private void initThumbnailLayoutParameters(int majorUnitSize) {
        int count = ((mThumbnailCount + mColumnInMinorDirection - 1) / mColumnInMinorDirection);
        mContentLengthInMajorDirection = count * majorUnitSize + (count - 1) * mThumbnailGap;
    }

    @Override
    protected void initThumbnailLayoutParameters() {
        // Initialize mThumbnailWidth and mThumbnailHeight from mSpec
        mThumbnailGap = mSpec.thumbnailGap;

        mColumnInMinorDirection = mSpec.rowsPort;
        if (mIsList) {
            mThumbnailWidth = mWidth;
            mThumbnailHeight = mSpec.labelHeight * 2;
        } else {
            mThumbnailWidth = Math.max(1, (mWidth - (mColumnInMinorDirection - 1) * mThumbnailGap) / mColumnInMinorDirection);
            mThumbnailHeight = mThumbnailWidth + (mSpec.labelHeight * 2) / 3; // 这是在一屏幕为了多显示图片
        }

        if (mRenderer != null) {
            mRenderer.onThumbnailSizeChanged(mThumbnailWidth, mThumbnailHeight);
        }

        initThumbnailLayoutParameters(mThumbnailHeight);

        if (mThumbnailCount > 0) {
            updateVisibleThumbnailRange();
        }
    }

    @Override
    protected void updateVisibleThumbnailRange() {
        int position = mScrollPosition;

        int startRow = position / (mThumbnailHeight + mThumbnailGap);
        int start = Math.max(0, mColumnInMinorDirection * startRow);
        int endRow = (position + mHeight + mThumbnailHeight + mThumbnailGap - 1)
                / (mThumbnailHeight + mThumbnailGap);
        int end = Math.min(mThumbnailCount, mColumnInMinorDirection * endRow);
        setVisibleThumbnailRange(start, end);

    }

    @Override
    public int getThumbnailIndexByPosition(float x, float y) {
        int absoluteX = Math.round(x);
        int absoluteY = Math.round(y) + mScrollPosition;
        if (absoluteX < 0 || absoluteY < 0) {
            return INDEX_NONE;
        }
        int columnIdx = absoluteX / (mThumbnailWidth + mThumbnailGap);
        int rowIdx = absoluteY / (mThumbnailHeight + mThumbnailGap);
        if (columnIdx >= mColumnInMinorDirection) {
            return INDEX_NONE;
        }

        if (absoluteX % (mThumbnailWidth + mThumbnailGap) >= mThumbnailWidth) {
            return INDEX_NONE;
        }
        if (absoluteY % (mThumbnailHeight + mThumbnailGap) >= mThumbnailHeight) {
            return INDEX_NONE;
        }
        int index = (rowIdx * mColumnInMinorDirection + columnIdx);
        return index >= mThumbnailCount ? INDEX_NONE : index;
    }

    @Override
    public Rect getThumbnailRect(int index, Rect rect) {
        int col, row;

        row = index / mColumnInMinorDirection;
        col = index - row * mColumnInMinorDirection;
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
