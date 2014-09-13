
package com.xjt.newpic.edit.category;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.xjt.newpic.common.LLog;

public class CategoryListView extends LinearLayout {

    private static final String TAG = CategoryListView.class.getSimpleName();

    private CategoryAdapter mAdapter;
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

    public CategoryListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAdapter(CategoryAdapter adapter) {
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(mDataSetObserver);
        fillContent();
    }

    public void fillContent() {
        removeAllViews();
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
