
package com.xjt.letool.views.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xjt.letool.R;
import com.xjt.letool.activities.BaseActivity;
import com.xjt.letool.view.GLController;
import com.xjt.letool.view.LetoolActionBar;

/**
 * @Author Jituo.Xuan
 * @Date 9:47:49 AM Apr 19, 2014
 * @Comments:null
 */
public class SettingFragment extends LetoolFragment {

    private BaseActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (BaseActivity) getActivity();
    }

    private void initBrowseActionBar() {
        LetoolActionBar actionBar = getLetoolActionBar();
        actionBar.setOnActionMode(LetoolActionBar.ACTION_BAR_MODE_SETTINGS, this);
        actionBar.setTitleIcon(R.drawable.ic_drawer);
        actionBar.setTitleText(R.string.common_settings);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.app_settings, container, false);
        initBrowseActionBar();
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_navi) {
            getLetoolSlidingMenu().toggle();
        }
    }

    @Override
    public GLController getGLController() {
        return null;
    }
}
