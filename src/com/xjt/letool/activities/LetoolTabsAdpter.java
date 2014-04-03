package com.xjt.letool.activities;

import com.xjt.letool.common.LLog;
import com.xjt.letool.data.DataManager;
import com.xjt.letool.fragments.PhotoFragment;
import com.xjt.letool.fragments.PictureFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class LetoolTabsAdpter extends FragmentPagerAdapter {

    private static final String TAG = "LetoolTabsAdpter";

    public final int TAB_POS_PHOTO = 0;
    public final int TAB_POS_PICTURE = 1;
    private LetoolBaseActivity mActivity;

    public LetoolTabsAdpter(LetoolBaseActivity activity,FragmentManager fm) {
        super(fm);
        mActivity = activity;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case TAB_POS_PHOTO: {
//                Fragment fragment = new PhotoFragment();
//                LLog.i(TAG, "PhotoFragment");
//                return fragment;
                Fragment fragment = new PictureFragment();
                Bundle data = new Bundle();
                data.putString(DataManager.KEY_MEDIA_PATH, mActivity.getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_SET_ONLY));
                fragment.setArguments(data);
                return fragment;
            }
            case TAB_POS_PICTURE: {
                Fragment fragment = new PictureFragment();
                Bundle data = new Bundle();
                data.putString(DataManager.KEY_MEDIA_PATH, mActivity.getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_SET_ONLY));
                LLog.i(TAG, "PictureFragment");
                fragment.setArguments(data);
                return fragment;
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case TAB_POS_PHOTO: {
                return "照片";
            }
            case TAB_POS_PICTURE: {
                return "图库";
            }
        }
        return null;
    }

}
