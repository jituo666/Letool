
package com.xjt.letool.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xjt.letool.R;
import com.xjt.letool.activities.BaseActivity;
import com.xjt.letool.settings.LetoolPreference;
import com.xjt.letool.view.GLController;
import com.xjt.letool.view.LetoolActionBar;

/**
 * @Author Jituo.Xuan
 * @Date 9:47:49 AM Apr 19, 2014
 * @Comments:null
 */
public class SettingFragment extends LetoolFragment {

    private BaseActivity mActivity;

    private LetoolPreference mPhotoDir;
    private LetoolPreference mGalleryDir;
    private LetoolPreference mAbout;

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

    private void initViews() {
        mPhotoDir.setMajorText(getString(R.string.photo_dir_title));
        mPhotoDir.setMinorText(getString(R.string.photo_dir_desc));
        mGalleryDir.setMajorText(getString(R.string.gallery_dir_title));
        mGalleryDir.setMinorText(getString(R.string.gallery_dir_desc));
        mAbout.setMajorText(getString(R.string.sliding_menu_title_about));
        mAbout.setMinorText(getString(R.string.app_about_desc));
        mPhotoDir.setOnClickListener(this);
        mGalleryDir.setOnClickListener(this);
        mAbout.setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.app_settings, container, false);
        initBrowseActionBar();
        mPhotoDir = (LetoolPreference) rootView.findViewById(R.id.photo_direcory);
        mGalleryDir = (LetoolPreference) rootView.findViewById(R.id.gallery_direcory);
        mAbout = (LetoolPreference) rootView.findViewById(R.id.app_about);
        initViews();
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_navi) {
            getLetoolSlidingMenu().toggle();
        } else if (v.getId() == R.id.photo_direcory) {
            //getLetoolSlidingMenu().toggle();
        }
        else if (v.getId() == R.id.gallery_direcory) {
            //getLetoolSlidingMenu().toggle();
        }
        else if (v.getId() == R.id.app_about) {
            //getLetoolSlidingMenu().toggle();
        }
    }

    @Override
    public GLController getGLController() {
        return null;
    }
}
