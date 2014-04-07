
package com.xjt.letool.activities;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.LetoolContext;
import com.xjt.letool.OrientationManager;
import com.xjt.letool.R;
import com.xjt.letool.ThreadPool;
import com.xjt.letool.common.LLog;
import com.xjt.letool.data.DataManager;
import com.xjt.letool.data.MediaItem;
import com.xjt.letool.data.utils.LetoolBitmapPool;
import com.xjt.letool.fragments.LetoolFragmentAdpter;
import com.xjt.letool.fragments.SlidingMenuFragment;
import com.xjt.letool.surpport.TabPageIndicator;
import com.xjt.letool.views.LetoolActionBar;
import com.xjt.letool.views.LetoolSlidingMenu;

import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.widget.TextView;

public class LetoolActivity extends FragmentActivity implements LetoolContext {

    private static final String TAG = "LetoolActivity";

    private LetoolActionBar mActionBar;
    private LetoolSlidingMenu mSlidingMenu;
    private OrientationManager mOrientationManager;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOrientationManager = new OrientationManager(this);
        mFragmentManager = getSupportFragmentManager();

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new LetoolFragmentAdpter(this, mFragmentManager));
        TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);
        //
        mSlidingMenu = new LetoolSlidingMenu(mFragmentManager);
        mActionBar = new LetoolActionBar(this, findViewById(R.id.action_bar));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getExternalCacheDir() == null) {
            OnCancelListener onCancel = new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            };
            OnClickListener onClick = new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            };

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDataManager().resume();
        mOrientationManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mOrientationManager.pause();
        getDataManager().pause();

        LetoolBitmapPool.getInstance().clear();
        MediaItem.getBytesBufferPool().clear();
    }

    @Override
    public void onBackPressed() {
        List<Fragment> list = mFragmentManager.getFragments();
        if (list.size() > 2) {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            boolean hasTopFragment = false;
            for (int p = list.size(); p > 2; p--) {
                Fragment f = list.get(p - 1);
                if (f != null) {
                    hasTopFragment = true;
                    if (f instanceof SlidingMenuFragment) {
                        ft.setCustomAnimations(0, R.anim.slide_left_out);
                    } else {
                        ft.setCustomAnimations(0, 0);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                    }
                    ft.remove(f);
                }
            }
            if (!hasTopFragment) {
                super.onBackPressed();
            } else {
                ft.commit();
                TextView actionBarNaviText = (TextView)mActionBar.getActionPanel().findViewById(R.id.navi_text);
                actionBarNaviText.setText(R.string.app_name);
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public DataManager getDataManager() {
        LLog.i(TAG, "");
        return ((LetoolApp) getApplication()).getDataManager();
    }

    @Override
    public Context getAndroidContext() {
        return this;
    }

    @Override
    public ThreadPool getThreadPool() {
        return ((LetoolApp) getApplication()).getThreadPool();
    }

    public OrientationManager getOrientationManager() {
        return mOrientationManager;
    }

    public LetoolSlidingMenu getLetoolSlidingMenu() {
        return mSlidingMenu;
    }

    public LetoolActionBar getLetoolActionBar() {
        return mActionBar;
    }
}