package com.xjt.letool.pages;

import com.xjt.letool.R;
import com.xjt.letool.activities.LetoolBaseActivity;
import com.xjt.letool.anims.StateTransAnim;
import com.xjt.letool.common.LLog;
import com.xjt.letool.utils.LetoolUtils;
import com.xjt.letool.views.GLBaseView;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

abstract public class PageState {
    private static final String TAG = "PageState";

    protected static final int FLAG_HIDE_ACTION_BAR = 1;
    protected static final int FLAG_HIDE_STATUS_BAR = 2;
    protected static final int FLAG_SCREEN_ON_WHEN_PLUGGED = 4;
    protected static final int FLAG_SCREEN_ON_ALWAYS = 8;
    protected static final int FLAG_ALLOW_LOCK_WHILE_SCREEN_ON = 16;
    protected static final int FLAG_SHOW_WHEN_LOCKED = 32;

    protected LetoolBaseActivity mActivity;
    protected Bundle mData;
    protected int mFlags;

    protected ResultEntry mReceivedResults;
    protected ResultEntry mResult;

    protected static class ResultEntry {
        public int requestCode;
        public int resultCode = Activity.RESULT_CANCELED;
        public Intent resultData;
    }

    private boolean mDestroyed = false;
    private boolean mPlugged = false;
    boolean mIsFinishing = false;

    private static final String KEY_TRANSITION_IN = "transition-in";

    private StateTransAnim.Transition mNextTransition =
            StateTransAnim.Transition.None;
    private StateTransAnim mIntroAnimation;
    private GLBaseView mContentPane;

    protected PageState() {
    }

    protected void setContentPane(GLBaseView content) {
        mContentPane = content;
        if (mIntroAnimation != null) {
            mContentPane.setIntroAnimation(mIntroAnimation);
            mIntroAnimation = null;
        }

        mContentPane.setBackgroundColor(getBackgroundColor());
        mActivity.getGLController().setContentPane(mContentPane);
    }

    void initialize(LetoolBaseActivity activity, Bundle data) {
        mActivity = activity;
        mData = data;
    }

    public Bundle getData() {
        return mData;
    }

    protected void onBackPressed() {
        mActivity.getPageManager().finishState(this);
    }

    protected void setStateResult(int resultCode, Intent data) {
        if (mResult == null)
            return;
        mResult.resultCode = resultCode;
        mResult.resultData = data;
    }

    protected void onConfigurationChanged(Configuration config) {
    }

    protected void onSaveState(Bundle outState) {
    }

    protected void onStateResult(int requestCode, int resultCode, Intent data) {
    }

    protected float[] mBackgroundColor;

    protected int getBackgroundColorId() {
        return R.color.default_background;
    }

    protected float[] getBackgroundColor() {
        return mBackgroundColor;
    }

    protected void onCreate(Bundle data, Bundle storedState) {
        mBackgroundColor = LetoolUtils.intColorToFloatARGBArray(mActivity.getResources().getColor(getBackgroundColorId()));
    }

    protected void clearStateResult() {
    }

    BroadcastReceiver mPowerIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                boolean plugged = (0 != intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0));

                if (plugged != mPlugged) {
                    mPlugged = plugged;
                    setScreenFlags();
                }
            }
        }
    };

    private void setScreenFlags() {
        final Window win = mActivity.getWindow();
        final WindowManager.LayoutParams params = win.getAttributes();
        if ((0 != (mFlags & FLAG_SCREEN_ON_ALWAYS)) ||
                (mPlugged && 0 != (mFlags & FLAG_SCREEN_ON_WHEN_PLUGGED))) {
            params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        }
        if (0 != (mFlags & FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)) {
            params.flags |= WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;
        }
        if (0 != (mFlags & FLAG_SHOW_WHEN_LOCKED)) {
            params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        } else {
            params.flags &= ~WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        }
        win.setAttributes(params);
    }

    protected void transitionOnNextPause(Class<? extends PageState> outgoing,
            Class<? extends PageState> incoming, StateTransAnim.Transition hint) {
        //        if (outgoing == SinglePhotoPage.class && incoming == AlbumPage.class) {
        //            mNextTransition = StateTransAnim.Transition.Outgoing;
        //        } else if (outgoing == AlbumPage.class && incoming == SinglePhotoPage.class) {
        //            mNextTransition = StateTransAnim.Transition.PhotoIncoming;
        //        } else {
        //            mNextTransition = hint;
        //        }
    }

    protected void performHapticFeedback(int feedbackConstant) {
        mActivity.getWindow().getDecorView().performHapticFeedback(feedbackConstant,
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
    }

    protected void onPause() {
        if (0 != (mFlags & FLAG_SCREEN_ON_WHEN_PLUGGED)) {
            ((Activity) mActivity).unregisterReceiver(mPowerIntentReceiver);
        }
        if (mNextTransition != StateTransAnim.Transition.None) {
            mActivity.getTransitionStore().put(KEY_TRANSITION_IN, mNextTransition);
            //PreparePageFadeoutTexture.prepareFadeOutTexture(mActivity, mContentPane);
            mNextTransition = StateTransAnim.Transition.None;
        }
    }

    // should only be called by StateManager
    void resume() {
        LetoolBaseActivity activity = mActivity;
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            if ((mFlags & FLAG_HIDE_ACTION_BAR) != 0) {
                actionBar.hide();
            } else {
                actionBar.show();
            }
            int stateCount = mActivity.getPageManager().getStateCount();
            mActivity.getLetoolActionBar().setDisplayOptions(stateCount > 1, true);
            // Default behavior, this can be overridden in ActivityState's onResume.
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }

        activity.invalidateOptionsMenu();

        setScreenFlags();

        boolean lightsOut = ((mFlags & FLAG_HIDE_STATUS_BAR) != 0);
        mActivity.getGLController().setLightsOutMode(lightsOut);

        ResultEntry entry = mReceivedResults;
        if (entry != null) {
            mReceivedResults = null;
            onStateResult(entry.requestCode, entry.resultCode, entry.resultData);
        }

        if (0 != (mFlags & FLAG_SCREEN_ON_WHEN_PLUGGED)) {
            // we need to know whether the device is plugged in to do this correctly
            final IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            activity.registerReceiver(mPowerIntentReceiver, filter);
        }

        onResume();

        // the transition store should be cleared after resume;
        mActivity.getTransitionStore().clear();
    }

    // a subclass of ActivityState should override the method to resume itself
    protected void onResume() {
        //        RawTexture fade = mActivity.getTransitionStore().get(PreparePageFadeoutTexture.KEY_FADE_TEXTURE);
        //        mNextTransition = mActivity.getTransitionStore().get(
        //                KEY_TRANSITION_IN, StateTransAnim.Transition.None);
        //        if (mNextTransition != StateTransAnim.Transition.None) {
        //            mIntroAnimation = new StateTransAnim(mNextTransition, fade);
        //            mNextTransition = StateTransAnim.Transition.None;
        //        }
    }

    protected boolean onCreateActionBar(Menu menu) {
        return true;
    }

    protected boolean onItemSelected(MenuItem item) {
        return false;
    }

    protected void onDestroy() {
        mDestroyed = true;
    }

    boolean isDestroyed() {
        return mDestroyed;
    }

    public boolean isFinishing() {
        return mIsFinishing;
    }

    protected MenuInflater getSupportMenuInflater() {
        return mActivity.getMenuInflater();
    }
}
