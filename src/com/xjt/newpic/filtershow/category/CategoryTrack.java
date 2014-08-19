package com.xjt.newpic.filtershow.category;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.xjt.newpic.R;

public class CategoryTrack extends LinearLayout {

    private CategoryAdapter mAdapter;
    private int mElemSize;
    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            if (getChildCount() != mAdapter.getCount()) {
                fillContent();
            } else {
                invalidate();
            }
        }
        @Override
        public void onInvalidated() {
            super.onInvalidated();
            fillContent();
        }
    };

    public CategoryTrack(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CategoryTrack);
        mElemSize = a.getDimensionPixelSize(R.styleable.CategoryTrack_iconSize, 0);
        a.recycle();
    }

    public void setAdapter(CategoryAdapter adapter) {
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(mDataSetObserver);
        fillContent();
    }

    public void fillContent() {
        removeAllViews();
        mAdapter.setItemWidth(mElemSize);
        mAdapter.setItemHeight(LayoutParams.MATCH_PARENT);
        int n = mAdapter.getCount();
        for (int i = 0; i < n; i++) {
            View view = mAdapter.getView(i, null, this);
            addView(view, i);
        }
        requestLayout();
    }

    @Override
    public void invalidate() {
        for (int i = 0; i < this.getChildCount(); i++) {
            View child = getChildAt(i);
            child.invalidate();
        }
    }

}
