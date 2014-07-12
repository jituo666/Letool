
package com.xjt.letool.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.LetoolContext;
import com.xjt.letool.R;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.OrientationManager;
import com.xjt.letool.common.ThreadPool;
import com.xjt.letool.fragment.GalleryFragment;
import com.xjt.letool.fragment.PhotoFragment;
import com.xjt.letool.imagedata.utils.LetoolBitmapPool;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.metadata.MediaItem;
import com.xjt.letool.metadata.MediaSetUtils;
import com.xjt.letool.preference.GlobalPreference;
import com.xjt.letool.view.GLBaseView;
import com.xjt.letool.view.GLController;
import com.xjt.letool.view.GLRootView;
import com.xjt.letool.view.LetoolBottomBar;
import com.xjt.letool.view.LetoolTopBar;

public class LocalImageBrowseActivity extends FragmentActivity implements LetoolContext {

    private static final String TAG = LocalImageBrowseActivity.class.getSimpleName();
    private LetoolTopBar mTopBar;
    private LetoolBottomBar mBottomBar;
    private ViewGroup mMainView;
    private GLRootView mGLESView;
    private OrientationManager mOrientationManager;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.local_browse_image);
        mTopBar = new LetoolTopBar(this, (ViewGroup) findViewById(R.id.local_image_browse_top_bar));
        mBottomBar = new LetoolBottomBar(this, (ViewGroup) findViewById(R.id.local_image_browse_bottom_bar));
        mMainView = (ViewGroup) findViewById(R.id.main_view);
        mGLESView = (GLRootView) mMainView.findViewById(R.id.gl_root_view);
        mOrientationManager = new OrientationManager(this);
        startFirstFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGLESView.getVisibility() == View.VISIBLE)
            mGLESView.onResume();
        getDataManager().resume();
        mOrientationManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGLESView.getVisibility() == View.VISIBLE)
            mGLESView.onPause();
        getDataManager().pause();
        mOrientationManager.pause();
        LetoolBitmapPool.getInstance().clear();
        MediaItem.getBytesBufferPool().clear();
    }

    @Override
    protected void onStop() {
        if (getSupportFragmentManager().findFragmentByTag(PhotoFragment.class.getSimpleName()) != null) {
            GlobalPreference.setLastUIComponnents(this, PhotoFragment.class.getSimpleName());
        } else if (getSupportFragmentManager().findFragmentByTag(GalleryFragment.class.getSimpleName()) != null) {
            GlobalPreference.setLastUIComponnents(this, GalleryFragment.class.getSimpleName());
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void startFirstFragment() {
        Fragment fragment = null;
        LLog.i(TAG, " startFirstFragment :" + GlobalPreference.getLastUIComponents(this));
        if (MediaSetUtils.MY_ALBUM_BUCKETS.length <= 0) {
            fragment = new GalleryFragment();
            Bundle data = new Bundle();
            data.putString(ThumbnailActivity.KEY_MEDIA_PATH, getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_SET_ONLY));
            fragment.setArguments(data);
        } else {
            if (GalleryFragment.class.getSimpleName().equals(GlobalPreference.getLastUIComponents(this))) {
                fragment = new GalleryFragment();
                Bundle data = new Bundle();
                data.putString(ThumbnailActivity.KEY_MEDIA_PATH, getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_SET_ONLY));
                fragment.setArguments(data);
            } else {
                fragment = new PhotoFragment();
                Bundle data = new Bundle();
                data.putString(ThumbnailActivity.KEY_MEDIA_PATH, getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
                data.putBoolean(ThumbnailActivity.KEY_IS_PHOTO_ALBUM, true);
                fragment.setArguments(data);
            }
        }
        pushContentFragment(fragment, null, false);
    }

    public void setMainView(GLBaseView view) {
        mGLESView.setContentPane(view);
        mGLESView.setVisibility(View.VISIBLE);
        ViewGroup normalView = (ViewGroup) mMainView.findViewById(R.id.normal_root_view);
        normalView.removeAllViews();
        normalView.setVisibility(View.GONE);
    }

    @Override
    public void setMainView(View view) {
        mGLESView.setVisibility(View.GONE);
        ViewGroup normalView = (ViewGroup) mMainView.findViewById(R.id.normal_root_view);
        normalView.removeAllViews();
        normalView.addView(view);
        normalView.setVisibility(View.VISIBLE);
    }

    public void pushContentFragment(Fragment newFragment, Fragment oldFragment, boolean backup) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (oldFragment != null) {
            ft.remove(oldFragment);
            if (backup)
                ft.addToBackStack(null);
        }
        LLog.i(TAG, " add :" + newFragment.getClass().getSimpleName());
        ft.add(newFragment, newFragment.getClass().getSimpleName());
        ft.commit();
    }

    public void popContentFragment() {
        LLog.i(TAG, " popBackStack :" + getSupportFragmentManager().getBackStackEntryCount());
        getSupportFragmentManager().popBackStack();
    }

    //

    @Override
    public void onBackPressed() {
        if (getLetoolTopBar().getActionBarMode() == LetoolTopBar.ACTION_BAR_MODE_SELECTION) {
            getLetoolTopBar().exitSelection();
            return;
        }
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            popContentFragment();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public LetoolTopBar getLetoolTopBar() {
        return mTopBar;
    }

    @Override
    public LetoolBottomBar getLetoolBottomBar() {
        return mBottomBar;
    }

    @Override
    public DataManager getDataManager() {
        return ((LetoolApp) getApplication()).getDataManager();
    }

    @Override
    public Context getAppContext() {
        return this;
    }

    @Override
    public ThreadPool getThreadPool() {
        return ((LetoolApp) getApplication()).getThreadPool();
    }

    @Override
    public OrientationManager getOrientationManager() {
        return mOrientationManager;
    }

    public GLController getGLController() {
        return mGLESView;
    }

}
