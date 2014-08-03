
package com.xjt.newpic.settings;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xjt.newpic.R;

/**
 * @Author Jituo.Xuan
 * @Date 5:55:42 AM Aug 1, 2014
 * @Comments:null
 */
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
