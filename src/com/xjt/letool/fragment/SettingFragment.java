
package com.xjt.letool.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xjt.letool.R;
import com.xjt.letool.settings.LetoolPreference;
import com.xjt.letool.view.GLController;
import com.xjt.letool.view.LetoolActionBar;

/**
 * @Author Jituo.Xuan
 * @Date 9:47:49 AM Apr 19, 2014
 * @Comments:null
 */
public class SettingFragment extends LetoolFragment {

    private LetoolPreference mAuthorDesc;
    private LetoolPreference mClearCache;
    private LetoolPreference mVersionCheck;
    private LetoolPreference mAbout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initBrowseActionBar() {
        LetoolActionBar actionBar = getLetoolActionBar();
        actionBar.setOnActionMode(LetoolActionBar.ACTION_BAR_MODE_SETTINGS, this);
        actionBar.setTitleIcon(R.drawable.ic_drawer);
        actionBar.setTitleText(R.string.common_settings);
    }

    private void initViews() {
        mClearCache.setSettingItemText(getString(R.string.clear_cache_title), getString(R.string.clear_cache_desc));
        mVersionCheck.setSettingItemText(getString(R.string.version_update_check_title), getString(R.string.version_update_check_desc));

        mAuthorDesc.setSettingItemText(getString(R.string.author_title), getString(R.string.author_desc));
        mAbout.setSettingItemText(getString(R.string.app_about_title), getString(R.string.app_about_desc));
        mAuthorDesc.setOnClickListener(this);
        mClearCache.setOnClickListener(this);
        mVersionCheck.setOnClickListener(this);
        mAbout.setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.app_settings, container, false);
        initBrowseActionBar();
        mAuthorDesc = (LetoolPreference) rootView.findViewById(R.id.author_desc);
        mClearCache = (LetoolPreference) rootView.findViewById(R.id.clear_cache);
        mVersionCheck = (LetoolPreference) rootView.findViewById(R.id.version_update_check);
        mAbout = (LetoolPreference) rootView.findViewById(R.id.app_about);
        initViews();
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_navi) {
            getLetoolSlidingMenu().toggle();
        } else if (v.getId() == R.id.clear_cache) {
        } else if (v.getId() == R.id.version_update_check) {
        } else if (v.getId() == R.id.author_desc) {
        } else if (v.getId() == R.id.app_about) {
        }
    }

    @Override
    public GLController getGLController() {
        return null;
    }
}
