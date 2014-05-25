
package com.xjt.letool.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xjt.letool.R;
import com.xjt.letool.common.LLog;
import com.xjt.letool.fragment.LetoolFragment;
import com.xjt.letool.fragment.SlidingMenuFragment;

public class LetoolSlidingMenu {

    private static final String TAG = "LetoolSlidingMenu";

    private FragmentManager mFragmentManager;
    private Fragment mFragment;
    private Fragment mAlphaHolder;
    private View mMenuTip;
    private Context mContext;

    public LetoolSlidingMenu(Context context, FragmentManager fm, View menuTip) {
        mFragmentManager = fm;
        mMenuTip = menuTip;
        mContext = context;
        LLog.i(TAG, " mFragmentManager" + mFragmentManager);
    }

    public void toggle() {
        if (mFragmentManager.findFragmentByTag(LetoolFragment.FRAGMENT_TAG_SLIDING_MENU) == null) {

            mFragment = new SlidingMenuFragment();
            mAlphaHolder = new AlphaFragment();
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.setCustomAnimations(R.anim.alpha_in, 0);
            ft.add(R.id.root_container, mAlphaHolder, LetoolFragment.FRAGMENT_TAG_SLIDING_MENU_ALPHA);
            ft.commit();
            FragmentTransaction ft1 = mFragmentManager.beginTransaction();
            ft1.setCustomAnimations(R.anim.slide_left_in, 0);
            ft1.add(R.id.root_container, mFragment, LetoolFragment.FRAGMENT_TAG_SLIDING_MENU).commit();
            playTipAinm(true);
        } else if (mFragment != null) {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.setCustomAnimations(0, R.anim.alpha_out);
            ft.remove(mAlphaHolder);
            ft.setCustomAnimations(0, R.anim.slide_left_out);
            ft.remove(mFragment).commit();
            playTipAinm(false);
        }
    }

    private void playTipAinm(boolean expand) {
        if (mMenuTip == null)
            return;
        View tip = mMenuTip.findViewById(R.id.action_navi_tip);
        int distance = Math.round(mContext.getResources().getDimension(R.dimen.letool_action_bar_height) / 12);
        ObjectAnimator anim = null;
        if (expand) {
            anim = ObjectAnimator.ofFloat(tip, "x", tip.getX(), tip.getX() - distance).setDuration(300);

        } else {
            anim = ObjectAnimator.ofFloat(tip, "x", tip.getX(), tip.getX() + distance).setDuration(300);
        }
        anim.start();
    }

    public static class AlphaFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.slid_menu_alpha_holder, container, false);
            return rootView;
        }

    }
}
