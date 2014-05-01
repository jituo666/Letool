
package com.xjt.letool.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.xjt.letool.R;
import com.xjt.letool.common.LLog;
import com.xjt.letool.fragment.LetoolFragment;
import com.xjt.letool.fragment.SlidingMenuFragment;

public class LetoolSlidingMenu {

    private static final String TAG = "LetoolSlidingMenu";

    private FragmentManager mFragmentManager;
    private Fragment mFragment;

    public LetoolSlidingMenu(FragmentManager fm) {
        mFragmentManager = fm;
        LLog.i(TAG, " mFragmentManager" + mFragmentManager);
    }

    public void toggle() {
        if (mFragmentManager.findFragmentByTag(LetoolFragment.FRAGMENT_TAG_SLIDING_MENU) == null) {
            mFragment = new SlidingMenuFragment();
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.setCustomAnimations(R.anim.slide_left_in, 0);
            ft.add(R.id.root_container, mFragment, LetoolFragment.FRAGMENT_TAG_SLIDING_MENU).commit();
        } else if (mFragment != null) {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.setCustomAnimations(0, R.anim.slide_left_out);
            ft.remove(mFragment).commit();
        }
    }

}
