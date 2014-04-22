
package com.xjt.letool.view;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xjt.letool.R;
import com.xjt.letool.selectors.SelectionManager;

public class LetoolActionBar {

    public static final int ACTION_BAR_MODE_NONE = -1;
    public static final int ACTION_BAR_MODE_BROWSE = 0;
    public static final int ACTION_BAR_MODE_SELECTION = 1;

    public static final int ACTION_BAR_MODE[] = {
            ACTION_BAR_MODE_BROWSE,
            ACTION_BAR_MODE_SELECTION
    };

    public static final int ACTION_MODE_LAYOUT_ID[] = {
            R.id.action_bar_normal,
            R.id.action_bar_selection
    };

    public static final int ACTION_MODE_TITLE_VIEW_ID[] = {
            R.id.navi_text,
            R.id.selection_counter
    };

    public static final int ACTION_IDS[] = {
            R.id.action_navi,
            R.id.operation_delete
    };

    public static interface OnActionModeListener extends View.OnClickListener {

    }

    private View mRootView;
    private View mActionModePanel;
    private OnActionModeListener mOnActionModeListener;
    private int mCurActionBarMode;
    private SelectionManager mSelectionManager;

    public LetoolActionBar(Context context, View rootView) {
        mRootView = rootView;
    }

    public void setOnActionMode(int actonMode, OnActionModeListener modeListener) {
        mCurActionBarMode = actonMode;
        for (int mode : ACTION_BAR_MODE) {
            View v = mRootView.findViewById(ACTION_MODE_LAYOUT_ID[mode]);
            if (v != null) {
                if (mode == mCurActionBarMode) {
                    mActionModePanel = v;
                    mActionModePanel.setVisibility(View.VISIBLE);
                } else {
                    v.setVisibility(View.GONE);
                }
            }
        }
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

    public int getActionBarMode() {
        return mCurActionBarMode;
    }

    public void setTitleIcon(int resId) {
        if (mCurActionBarMode == ACTION_BAR_MODE_BROWSE) {
            ImageView natviButton = (ImageView) mActionModePanel.findViewById(R.id.action_navi_tip);
            natviButton.setImageResource(resId);
        }
    }

    public void setTitle(CharSequence title) {
        TextView actionBarNaviText = (TextView) mActionModePanel.findViewById(ACTION_MODE_TITLE_VIEW_ID[mCurActionBarMode]);
        if (actionBarNaviText != null)
            actionBarNaviText.setText(title);
    }

    public void setTitle(int titleId) {
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
}
