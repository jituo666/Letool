
package com.xjt.letool.activities;

import java.util.List;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.LetoolContext;
import com.xjt.letool.R;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.OrientationManager;
import com.xjt.letool.common.ThreadPool;
import com.xjt.letool.fragment.LetoolFragment;
import com.xjt.letool.imagedata.utils.LetoolBitmapPool;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.metadata.MediaItem;
import com.xjt.letool.view.GLBaseView;
import com.xjt.letool.view.GLController;
import com.xjt.letool.view.LetoolTopBar;
import com.xjt.letool.view.LetoolBottomBar;
import com.xjt.letool.view.LetoolSlidingMenu;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * @Author Jituo.Xuan
 * @Date 6:16:39 PM Apr 20, 2014
 * @Comments:null
 */
public class BaseFragmentActivity extends FragmentActivity implements LetoolContext {

    private static final String TAG = BaseFragmentActivity.class.getSimpleName();

    private LetoolTopBar mTopBar;
    private LetoolBottomBar mBottomBar;
    private Toast mExitToast;
    private LetoolSlidingMenu mSlidingMenu;
    private OrientationManager mOrientationManager;

    protected FragmentManager mFragmentManager;

    protected boolean mIsMainActivity = false;
    private boolean mWaitingForExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getSupportFragmentManager();

        mOrientationManager = new OrientationManager(this);
        mSlidingMenu = new LetoolSlidingMenu(this, mFragmentManager, findViewById(R.id.action_bar));
        mTopBar = new LetoolTopBar(this, (ViewGroup) findViewById(R.id.action_bar));
        mBottomBar = new LetoolBottomBar(this, (ViewGroup) findViewById(R.id.bottom_bar));
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
        if (getLetoolTopBar().getActionBarMode() == LetoolTopBar.ACTION_BAR_MODE_SELECTION) {
            getLetoolTopBar().exitSelection();
            return;
        }
        Fragment f = mFragmentManager.findFragmentByTag(LetoolFragment.FRAGMENT_TAG_SLIDING_MENU);
        if (f != null) {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            Fragment aphaHolder = mFragmentManager.findFragmentByTag(LetoolFragment.FRAGMENT_TAG_SLIDING_MENU_ALPHA);
            if (aphaHolder != null) {
                ft.setCustomAnimations(0, R.anim.alpha_out);
                ft.remove(aphaHolder);
            }
            ft.setCustomAnimations(0, R.anim.slide_left_out);
            ft.remove(f);
            ft.commit();
        } else if (mIsMainActivity) {

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
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public class ExitListener implements OnClickListener, OnCancelListener {

        @Override
        public void onCancel(DialogInterface dialog) {
            dialog.dismiss();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                finish();
            } else {
                dialog.dismiss();
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    public OrientationManager getOrientationManager() {
        return mOrientationManager;
    }

    public LetoolSlidingMenu getLetoolSlidingMenu() {
        return mSlidingMenu;
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            List<Fragment> list = mFragmentManager.getFragments();
            for (Fragment f : list) {
                if (f != null && (f instanceof LetoolFragment) && f.isResumed()) {
                    //((LetoolFragment) f).onMenuClicked();
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

	@Override
	public GLController getGLController() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMainView(GLBaseView view) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMainView(View view) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pushContentFragment(Fragment newFragment, boolean backToStack) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void popContentFragment() {
		// TODO Auto-generated method stub
		
	}
}
