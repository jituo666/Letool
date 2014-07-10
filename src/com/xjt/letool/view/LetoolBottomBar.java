package com.xjt.letool.view;


import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;

import com.xjt.letool.R;
import com.xjt.letool.view.LetoolTopBar.OnActionModeListener;

/**
 * @Author Jituo.Xuan
 * @Date 5:44:20 PM May 13, 2014
 * @Comments:null
 */
public class LetoolBottomBar {

    public static final int BOTTOM_BAR_MODE_NONE = -1;
    public static final int BOTTOM_BAR_MODE_ALBUM = 0;
    public static final int BOTTOM_BAR_MODE_FULL_IMAGE = 1;

    public static final int BOTTOM_BAR_MODE[] = {
            BOTTOM_BAR_MODE_ALBUM,
            BOTTOM_BAR_MODE_FULL_IMAGE
    };

    public static final int BOTTOM_MODE_LAYOUT_ID[] = {
            R.layout.bottom_bar_full_image,
            R.layout.bottom_bar_full_image
    };

    public static final int BOTTOM_BUTTON_IDS[] = {
            R.id.action_share,
            R.id.action_detail,
            R.id.action_delete
    };

    private FragmentActivity mActivity;
    private ViewGroup mBarContainer;
    private View mBottomModePanel;
    private OnActionModeListener mOnActionModeListener;
    private int mCurBottomBarMode;

    public LetoolBottomBar(FragmentActivity activity, ViewGroup barContainer) {
        mActivity = activity;
        mBarContainer = barContainer;
    }

    public void setOnActionMode(int actonMode, OnActionModeListener modeListener) {
        mCurBottomBarMode = actonMode;
        mBottomModePanel = LayoutInflater.from(mActivity).inflate(BOTTOM_MODE_LAYOUT_ID[actonMode], null);
        mBarContainer.removeAllViews();
        ViewGroup.LayoutParams layoutParam = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mBarContainer.addView(mBottomModePanel, layoutParam);

        mOnActionModeListener = modeListener;
        for (int i : BOTTOM_BUTTON_IDS) {
            View v = mBottomModePanel.findViewById(i);
            if (v != null) {
                v.setOnClickListener(mOnActionModeListener);
            }
        }
    }

    public View getActionPanel() {
        return mBottomModePanel;
    }

    public int getActionBarMode() {
        return mCurBottomBarMode;
    }

    public void setVisible(int visible, boolean withAnim) {
        if (mBarContainer != null) {
            mBarContainer.setVisibility(visible);
            if (!withAnim) return;
            if (visible == View.VISIBLE) {
                mBarContainer.startAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.slide_bottom_in));
            } else {
                mBarContainer.startAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.slide_bottom_out));
            }
        }
    }

    public int getHeight() {
        if (mBarContainer != null) {
            return mBarContainer.getHeight();
        }
        return 0;
    }
}
