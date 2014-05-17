
package com.xjt.letool.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.xjt.letool.R;
import com.xjt.letool.activities.BaseActivity;
import com.xjt.letool.selectors.SelectionManager;

public class LetoolActionBar {

    public static final int ACTION_BAR_MODE_NONE = -1;
    public static final int ACTION_BAR_MODE_BROWSE = 0;
    public static final int ACTION_BAR_MODE_SELECTION = 1;
    public static final int ACTION_BAR_MODE_SETTINGS = 2;
    public static final int ACTION_BAR_MODE_FULL_IMAGE = 3;

    private BaseActivity mActivity;
    public static final int ACTION_BAR_MODE[] = {
            ACTION_BAR_MODE_BROWSE,
            ACTION_BAR_MODE_SELECTION,
            ACTION_BAR_MODE_SETTINGS,
            ACTION_BAR_MODE_FULL_IMAGE
    };

    public static final int ACTION_MODE_LAYOUT_ID[] = {
            R.layout.action_bar_common,
            R.layout.action_bar_selection,
            R.layout.action_bar_common,
            R.layout.action_bar_common
    };

    public static final int ACTION_MODE_TITLE_VIEW_ID[] = {
            R.id.navi_text,
            R.id.selection_counter,
            R.id.navi_text,
            R.id.navi_text,
    };

    public static final int ACTION_BUTTON_IDS[] = {
            R.id.action_navi,
            R.id.operation_delete,
            R.id.action_navi,
            R.id.action_navi,
    };

    public static interface OnActionModeListener extends View.OnClickListener {

    }

    private ViewGroup mBarContainer;
    private View mActionModePanel;
    private OnActionModeListener mOnActionModeListener;
    private int mCurActionBarMode;
    private SelectionManager mSelectionManager;

    public LetoolActionBar(BaseActivity activity, ViewGroup barContainer) {
        mActivity = activity;
        mBarContainer = barContainer;
    }

    public void setOnActionMode(int actonMode, OnActionModeListener modeListener) {
        mCurActionBarMode = actonMode;
        mActionModePanel = LayoutInflater.from(mActivity).inflate(ACTION_MODE_LAYOUT_ID[actonMode], null);
        mBarContainer.removeAllViews();
        ViewGroup.LayoutParams layoutParam = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mBarContainer.addView(mActionModePanel, layoutParam);

        mOnActionModeListener = modeListener;
        for (int i : ACTION_BUTTON_IDS) {
            View v = mActionModePanel.findViewById(i);
            if (v != null) {
                v.setOnClickListener(mOnActionModeListener);
            }
        }
    }

    public View getActionPanel() {
        return mActionModePanel;
    }

    public int getActionBarMode() {
        return mCurActionBarMode;
    }

    public void setTitleIcon(int resId) {
        if (mCurActionBarMode != ACTION_BAR_MODE_SELECTION) {
            ImageView natviButton = (ImageView) mActionModePanel.findViewById(R.id.action_navi_tip);
            natviButton.setImageResource(resId);
        }
    }

    public void setTitleText(CharSequence title) {
        TextView actionBarNaviText = (TextView) mActionModePanel.findViewById(ACTION_MODE_TITLE_VIEW_ID[mCurActionBarMode]);
        if (actionBarNaviText != null)
            actionBarNaviText.setText(title);
    }

    public void setTitleText(int titleId) {
        TextView actionBarNaviText = (TextView) mActionModePanel.findViewById(ACTION_MODE_TITLE_VIEW_ID[mCurActionBarMode]);
        if (actionBarNaviText != null)
            actionBarNaviText.setText(titleId);
    }

    public void setSelectionManager(SelectionManager selector) {
        mSelectionManager = selector;
    }

    public void exitSelection() {
        if (mCurActionBarMode == ACTION_BAR_MODE_SELECTION && mSelectionManager != null
                && mSelectionManager.inSelectionMode()) {
            mSelectionManager.leaveSelectionMode();
        }
    }

    public void setVisible(int visible) {
        if (mBarContainer != null) {

            mBarContainer.setVisibility(visible);
            if (visible == View.VISIBLE) {
                mBarContainer.startAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.slide_top_in));
            } else {
                mBarContainer.startAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.slide_top_out));
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
