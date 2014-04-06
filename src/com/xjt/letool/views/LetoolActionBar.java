
package com.xjt.letool.views;

import android.content.Context;
import android.view.View;

import com.xjt.letool.R;

public class LetoolActionBar {

    public static final int ACTION_MODE_NONE = -1;
    public static final int ACTION_MODE_BROWSE = 0;
    public static final int ACTION_MODE_SELECTION = 1;

    public static final int ACTION_MODE[] = {
            R.id.action_bar_browse,
            R.id.action_bar_selection
    };

    public static final int ACTION_IDS[] = {
            R.id.left_natvi,
            R.id.operation_delete
    };

    public static interface OnActionModeListener extends View.OnClickListener {

    }

    private View mRootView;
    private View mActionModePanel;
    private OnActionModeListener mOnActionModeListener;
    private int mCurActionMode;

    public LetoolActionBar(Context context, View rootView) {
        mRootView = rootView;
    }

    public void setOnActionMode(int actonMode, OnActionModeListener modeListener) {
        mCurActionMode = actonMode;
        mActionModePanel = mRootView.findViewById(ACTION_MODE[mCurActionMode]);
        mOnActionModeListener = modeListener;
        for (int i : ACTION_IDS) {
            View v = mActionModePanel.findViewById(i);
            if (v != null) {
                v.setOnClickListener(mOnActionModeListener);
            }
        }
    }

    public View getActionPanel() {
        return mActionModePanel;
    }
}
