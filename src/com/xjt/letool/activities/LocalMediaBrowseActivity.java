
package com.xjt.letool.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.LetoolContext;
import com.xjt.letool.R;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.OrientationManager;
import com.xjt.letool.common.ThreadPool;
import com.xjt.letool.fragment.GalleryFragment;
import com.xjt.letool.fragment.PhotoFragment;
import com.xjt.letool.fragment.SlidingMenuFragment;
import com.xjt.letool.fragment.VideoFragment;
import com.xjt.letool.imagedata.utils.LetoolBitmapPool;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.metadata.MediaItem;
import com.xjt.letool.metadata.MediaSetUtils;
import com.xjt.letool.view.GLBaseView;
import com.xjt.letool.view.GLController;
import com.xjt.letool.view.GLRootView;
import com.xjt.letool.view.LetoolBottomBar;
import com.xjt.letool.view.LetoolSlidingMenu;
import com.xjt.letool.view.LetoolTopBar;

public class LocalMediaBrowseActivity extends FragmentActivity implements LetoolContext {

    private static final String TAG = LocalMediaBrowseActivity.class.getSimpleName();
    
    public static final String KEY_ALBUM_TITLE = "album_title";
    public static final String KEY_MEDIA_PATH = "media-path";
    public static final String KEY_ALBUM_ID = "album_id";
    public static final String KEY_IS_CAMERA_SOURCE = "is_camera_source";
    public static final String KEY_IS_IMAGE = "is_image";
    public boolean mIsImage = true;
    
    private LetoolTopBar mTopBar;
    private LetoolBottomBar mBottomBar;
    private LetoolSlidingMenu mSlidingMenu;
    private ViewGroup mMainView;
    private GLRootView mGLESView;
    private Toast mExitToast;
    private OrientationManager mOrientationManager;
    private boolean mWaitingForExit = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.local_browse_image);
        if (getIntent().hasExtra(KEY_IS_IMAGE)) {
        	mIsImage = getIntent().getBooleanExtra(KEY_IS_IMAGE, true);
        }
        mTopBar = new LetoolTopBar(this, (ViewGroup) findViewById(R.id.local_image_browse_top_bar));
        mBottomBar = new LetoolBottomBar(this, (ViewGroup) findViewById(R.id.local_image_browse_bottom_bar));
        mSlidingMenu = new LetoolSlidingMenu(this, getSupportFragmentManager(), findViewById(R.id.local_image_browse_top_bar));
        mMainView = (ViewGroup) findViewById(R.id.local_image_browse_main_view);
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
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void startFirstFragment() {
        Fragment fragment = null;
        if (MediaSetUtils.MY_ALBUM_BUCKETS.length <= 0) {
            fragment = new GalleryFragment();
            Bundle data = new Bundle();
            data.putString(LocalMediaBrowseActivity.KEY_MEDIA_PATH, getDataManager()
            		.getTopSetPath(isImageBrwosing() ?DataManager.INCLUDE_LOCAL_IMAGE_SET_ONLY:DataManager.INCLUDE_LOCAL_VIDEO_SET_ONLY));
            fragment.setArguments(data);
        } else {

            fragment = mIsImage?new PhotoFragment():new VideoFragment();
            Bundle data = new Bundle();
            data.putString(LocalMediaBrowseActivity.KEY_MEDIA_PATH, getDataManager()
            		.getTopSetPath(isImageBrwosing() ?DataManager.INCLUDE_LOCAL_IMAGE_ONLY:DataManager.INCLUDE_LOCAL_VIDEO_ONLY));
            data.putBoolean(LocalMediaBrowseActivity.KEY_IS_CAMERA_SOURCE, true);
            fragment.setArguments(data);

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
            Fragment f = getSupportFragmentManager().findFragmentByTag(SlidingMenuFragment.class.getSimpleName());
            if (f != null) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Fragment aphaHolder = getSupportFragmentManager().findFragmentByTag(LetoolSlidingMenu.AlphaFragment.class.getSimpleName());
                if (aphaHolder != null) {
                    ft.setCustomAnimations(0, R.anim.alpha_out);
                    ft.remove(aphaHolder);
                }
                ft.setCustomAnimations(0, R.anim.slide_left_out);
                ft.remove(f);
                ft.commit();
            } else {
                if (mWaitingForExit) {
                    if (mExitToast != null) {
                        mExitToast.cancel();
                    }
                    finish();
                } else {
                    mWaitingForExit = true;
                    mExitToast = Toast.makeText(this, R.string.common_exit_tip, Toast.LENGTH_SHORT);
                    mExitToast.show();
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            mWaitingForExit = false;
                        }
                    }, 3000);
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LLog.i(TAG, "onKeyDown menu1:" + keyCode);
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            LLog.i(TAG, "onKeyDown menu2:" + getSupportFragmentManager().getBackStackEntryCount());
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                mSlidingMenu.toggle();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
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
    public LetoolSlidingMenu getLetoolSlidingMenu() {
        return mSlidingMenu;
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

	@Override
	public boolean isImageBrwosing() {
		return mIsImage;
	}

}