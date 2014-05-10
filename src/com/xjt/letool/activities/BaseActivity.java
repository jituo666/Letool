
package com.xjt.letool.activities;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.LetoolContext;
import com.xjt.letool.R;
import com.xjt.letool.common.OrientationManager;
import com.xjt.letool.common.ThreadPool;
import com.xjt.letool.fragment.LetoolFragment;
import com.xjt.letool.imagedata.utils.LetoolBitmapPool;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.metadata.MediaItem;
import com.xjt.letool.view.LetoolActionBar;
import com.xjt.letool.view.LetoolSlidingMenu;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.ViewGroup;

/**
 * @Author Jituo.Xuan
 * @Date 6:16:39 PM Apr 20, 2014
 * @Comments:null
 */
public class BaseActivity extends FragmentActivity implements LetoolContext {

    private static final String TAG = BaseActivity.class.getSimpleName();

    public static final String KEY_ALBUM_TITLE = "album_title";
    public static final String KEY_MEDIA_PATH = "media-path";
    public static final String KEY_ALBUM_ID = "album_id";
    public static final String KEY_IS_CAMERA = "is_camera";
    public static final String KEY_GET_CONTENT = "get-content";
    public static final String KEY_TYPE_BITS = "type-bits";

    private LetoolActionBar mActionBar;
    private LetoolSlidingMenu mSlidingMenu;
    private OrientationManager mOrientationManager;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOrientationManager = new OrientationManager(this);
        mFragmentManager = getSupportFragmentManager();
        mSlidingMenu = new LetoolSlidingMenu(mFragmentManager);
        mActionBar = new LetoolActionBar(this, (ViewGroup)findViewById(R.id.action_bar));
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
        if (getLetoolActionBar().getActionBarMode() == LetoolActionBar.ACTION_BAR_MODE_SELECTION) {
            getLetoolActionBar().exitSelection();
            return;
        }
        Fragment f = mFragmentManager.findFragmentByTag(LetoolFragment.FRAGMENT_TAG_SLIDING_MENU);
        if (f != null) {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.setCustomAnimations(0, R.anim.slide_left_out);
            ft.remove(f);
            ft.commit();
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
