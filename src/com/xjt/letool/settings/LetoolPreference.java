
package com.xjt.letool.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
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

    public void setSettingItemText(CharSequence title, CharSequence desc) {
        mMajorText.setText(title);
        mMinorText.setText(desc);
    }

    public void setSettingItemText(int title, int desc) {
        mMajorText.setText(title);
        mMinorText.setText(desc);
    }
}
