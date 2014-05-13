
package com.xjt.letool.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xjt.letool.R;

public class LetoolPreference extends RelativeLayout {

    private TextView mMajorText;
    private TextView mMinorText;

    public LetoolPreference(Context context) {
        super(context);
    }

    public LetoolPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMajorText = (TextView) findViewById(R.id.item_major_title);
        mMinorText = (TextView) findViewById(R.id.item_minor_title);
    }

    public void setMajorText(CharSequence title) {
        mMajorText.setText(title);
    }

    public void setMinorText(CharSequence title) {
        mMinorText.setText(title);
    }
}
