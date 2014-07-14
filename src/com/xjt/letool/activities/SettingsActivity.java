package com.xjt.letool.activities;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.LetoolContext;
import com.xjt.letool.R;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.OrientationManager;
import com.xjt.letool.common.ThreadPool;
import com.xjt.letool.fragment.SettingFragment;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.view.GLBaseView;
import com.xjt.letool.view.GLController;
import com.xjt.letool.view.LetoolBottomBar;
import com.xjt.letool.view.LetoolSlidingMenu;
import com.xjt.letool.view.LetoolTopBar;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

public class SettingsActivity extends FragmentActivity implements LetoolContext {

    private static final String TAG = SettingsActivity.class.getSimpleName();
    
	private LetoolTopBar mTopBar;
	private LetoolSlidingMenu mSlidingMenu;

    private void startFirstFragment() {
        Fragment fragment = new SettingFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.local_image_browse_main_view, fragment);
        ft.commit();
    }
    
	@Override
	protected void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.local_browse_image);
		mTopBar = new LetoolTopBar(this,(ViewGroup) findViewById(R.id.local_image_browse_top_bar));
		mSlidingMenu = new LetoolSlidingMenu(this, getSupportFragmentManager(),findViewById(R.id.local_image_browse_top_bar));
		startFirstFragment();
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
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public LetoolTopBar getLetoolTopBar() {
		return mTopBar;
	}

	@Override
	public LetoolBottomBar getLetoolBottomBar() {
		return null;
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
		return null;
	}

	@Override
	public boolean isImageBrwosing() {
		return false;
	}

	@Override
	public GLController getGLController() {
		return null;
	}

	@Override
	public void setMainView(GLBaseView view) {

	}

	@Override
	public void setMainView(View view) {

	}

	@Override
	public void pushContentFragment(Fragment newFragment, Fragment oldFragment,
			boolean backup) {

	}

	@Override
	public void popContentFragment() {

	}

}
