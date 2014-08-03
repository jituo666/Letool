
package com.xjt.newpic.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xjt.newpic.R;
import com.xjt.newpic.surpport.CommonToggleButton;


/**
 * @Author Jituo.Xuan
 * @Date 5:55:36 AM Aug 1, 2014
 * @Comments:null
 */
public class LetoolPreference extends RelativeLayout {

    private TextView mMajorText;
    private TextView mMinorText;
    private CommonToggleButton mSwitch;

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
        mSwitch = (CommonToggleButton) findViewById(R.id.toggle);
    }

    public void setSettingItemText(CharSequence title, CharSequence desc, boolean withToogle) {
        mMajorText.setText(title);
        mMinorText.setText(desc);
        mSwitch.setVisibility(withToogle ? View.VISIBLE : View.GONE);
    }

    public void setSettingItemText(int title, int desc) {
        mMajorText.setText(title);
        mMinorText.setText(desc);
    }

    public void setChecked(boolean ischeck) {
        if (mSwitch != null) {
            mSwitch.setChecked(ischeck);
        }
    }

    public boolean isChecked() {
        if (mSwitch != null) {
            return mSwitch.isChecked();
        }
        return false;
    }
}
