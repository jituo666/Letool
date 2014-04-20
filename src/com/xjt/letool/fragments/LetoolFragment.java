
package com.xjt.letool.fragments;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.LetoolContext;
import com.xjt.letool.ThreadPool;
import com.xjt.letool.activities.LetoolBaseActivity;
import com.xjt.letool.common.LLog;
import com.xjt.letool.data.DataManager;
import com.xjt.letool.views.GLController;
import com.xjt.letool.views.LetoolActionBar;
import com.xjt.letool.views.LetoolActionBar.OnActionModeListener;
import com.xjt.letool.views.LetoolSlidingMenu;

import android.content.Context;
import android.os.Looper;
import android.support.v4.app.Fragment;

/**
 * @Author Jituo.Xuan
 * @Date 11:24:54 PM Apr 19, 2014
 * @Comments:null
 */
public abstract class LetoolFragment extends Fragment implements LetoolContext, OnActionModeListener {

    private static final String TAG = "LetoolFragment";

    public static final String FRAGMENT_TAG_THUMBNAIL = ThumbnailFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG_FULL_IMAGE = FullImageFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG_SLIDING_MENU = SlidingMenuFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG_FOLDER = FolderFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG_SETTINGS = SettingFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG_ABOUT = AboutFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG_HIDE_LIST = HideListFragment.class.getSimpleName();

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
        return ((LetoolBaseActivity)getActivity()).getLetoolActionBar();
    }

    public LetoolSlidingMenu getLetoolSlidingMenu() {
        return ((LetoolBaseActivity)getActivity()).getLetoolSlidingMenu();
    }

    public abstract GLController getGLController();
}
