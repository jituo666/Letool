package com.xjt.letool.views.fragment;

import com.xjt.letool.activities.BaseActivity;
import com.xjt.letool.common.LLog;
import com.xjt.letool.data.DataManager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class LetoolFragmentAdpter extends FragmentPagerAdapter {

    private static final String TAG = "LetoolTabsAdpter";

    public final int TAB_POS_PHOTO = 0;
    public final int TAB_POS_PICTURE = 1;
    private BaseActivity mActivity;

    private static final String[] CONTENT = new String[] { "照片", "图库" };
    private static final Class<?>[] fragments = new Class<?>[] {
            PhotoFragment.class,
            GalleryFragment.class
    };

    public LetoolFragmentAdpter(BaseActivity activity, FragmentManager fm) {
        super(fm);
        mActivity = activity;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case TAB_POS_PHOTO: {
                Fragment fragment = null;
                try {
                    fragment = (Fragment) fragments[TAB_POS_PHOTO].newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                Bundle data = new Bundle();
                data.putString(BaseActivity.KEY_MEDIA_PATH, mActivity.getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
                fragment.setArguments(data);
                return fragment;
            }
            case TAB_POS_PICTURE: {
                Fragment fragment = null;
                try {
                    fragment = (Fragment) fragments[TAB_POS_PICTURE].newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                Bundle data = new Bundle();
                data.putString(BaseActivity.KEY_MEDIA_PATH, mActivity.getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_SET_ONLY));
                LLog.i(TAG, "PictureFragment");
                fragment.setArguments(data);
                return fragment;
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        return CONTENT.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return CONTENT[position];
    }

}
