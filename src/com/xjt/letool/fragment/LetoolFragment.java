
package com.xjt.letool.fragment;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.LetoolContext;
import com.xjt.letool.activities.BaseActivity;
import com.xjt.letool.common.ThreadPool;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.view.GLController;
import com.xjt.letool.view.LetoolActionBar;
import com.xjt.letool.view.LetoolBottomBar;
import com.xjt.letool.view.LetoolSlidingMenu;
import com.xjt.letool.view.LetoolActionBar.OnActionModeListener;

import android.content.Context;
import android.os.Looper;
import android.support.v4.app.Fragment;

/**
 * @Author Jituo.Xuan
 * @Date 11:24:54 PM Apr 19, 2014
 * @Comments:null
 */
public abstract class LetoolFragment extends Fragment implements LetoolContext, OnActionModeListener {

    private static final String TAG = LetoolFragment.class.getSimpleName();

    public static final String FRAGMENT_TAG_PHOTO = PhotoFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG_FULL_IMAGE = FullImageFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG_SLIDING_MENU = SlidingMenuFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG_SLIDING_MENU_ALPHA = LetoolSlidingMenu.AlphaFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG_FOLDER = GalleryFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG_SETTINGS = SettingFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG_ABOUT = AboutFragment.class.getSimpleName();

    @Override
    public DataManager getDataManager() {
        return ((LetoolApp) getActivity().getApplication()).getDataManager();
    }

    @Override
    public ThreadPool getThreadPool() {
        return ((LetoolApp) getActivity().getApplication()).getThreadPool();
    }

    @Override
    public Context getAndroidContext() {
        return getActivity().getApplicationContext();
    }

    @Override
    public Looper getMainLooper() {
        return getActivity().getMainLooper();
    }

    public LetoolActionBar getLetoolActionBar() {
        return ((BaseActivity) getActivity()).getLetoolActionBar();
    }

    public LetoolBottomBar getLetoolBottomBar() {
        return ((BaseActivity) getActivity()).getLetoolBottomBar();
    }

    public LetoolSlidingMenu getLetoolSlidingMenu() {
        return ((BaseActivity) getActivity()).getLetoolSlidingMenu();
    }

    public abstract GLController getGLController();

    public void onMenuClicked() {

    }
}
