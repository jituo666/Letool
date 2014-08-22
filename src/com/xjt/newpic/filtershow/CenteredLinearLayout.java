package com.xjt.newpic.filtershow;

import com.xjt.newpic.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class CenteredLinearLayout extends LinearLayout {
    private final int mMaxWidth;

    public CenteredLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CenteredLinearLayout);
        mMaxWidth = a.getDimensionPixelSize(R.styleable.CenteredLinearLayout_max_width, 0);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (mMaxWidth > 0 && parentWidth > mMaxWidth) {
            int measureMode = MeasureSpec.getMode(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, measureMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
