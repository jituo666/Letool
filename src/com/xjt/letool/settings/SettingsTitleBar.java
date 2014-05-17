
package com.xjt.letool.settings;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xjt.letool.R;

public class SettingsTitleBar {

    private ViewGroup mBarContainer;
    private TextView mTitle;
    private Context mContext;

    public static interface OnNaviListener extends View.OnClickListener {

    }

    public SettingsTitleBar(Context context, ViewGroup barContainer) {
        mBarContainer = barContainer;
        mTitle = (TextView) mBarContainer.findViewById(R.id.navi_text);
        mContext = context;
    }

    public void setTitle(CharSequence title) {
        mTitle.setText(title);
    }

    public void setNaviLisenter(OnNaviListener l) {
        mBarContainer.findViewById(R.id.action_navi).setOnClickListener(l);
    }
}
