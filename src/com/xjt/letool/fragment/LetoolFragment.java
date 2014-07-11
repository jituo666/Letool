
package com.xjt.letool.fragment;

import com.xjt.letool.LetoolContext;

import com.xjt.letool.view.LetoolSlidingMenu;
import com.xjt.letool.view.LetoolTopBar.OnActionModeListener;

import android.support.v4.app.Fragment;

/**
 * @Author Jituo.Xuan
 * @Date 11:24:54 PM Apr 19, 2014
 * @Comments:null
 */
public abstract class LetoolFragment extends Fragment implements OnActionModeListener {

    private static final String TAG = LetoolFragment.class.getSimpleName();

    public static final String FRAGMENT_TAG_PHOTO = PhotoFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG_FULL_IMAGE = FullImageFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG_SLIDING_MENU = SlidingMenuFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG_SLIDING_MENU_ALPHA = LetoolSlidingMenu.AlphaFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG_FOLDER = GalleryFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG_SETTINGS = SettingFragment.class.getSimpleName();
    public static final String FRAGMENT_TAG_ABOUT = AboutFragment.class.getSimpleName();

    private LetoolContext mLetoolContext;

/*    @Override
    public DataManager getDataManager() {
        return ((LetoolApp) getActivity().getApplication()).getDataManager();
    }

    @Override
    public ThreadPool getThreadPool() {
        return ((LetoolApp) getActivity().getApplication()).getThreadPool();
    }

    @Override
    public Context getAppContext() {
        return getActivity().getApplicationContext();
    }

    @Override
    public Looper getMainLooper() {
        return getActivity().getMainLooper();
    }

    public LetoolTopBar getLetoolTopBar() {
        return ((LocalImageBrowseActivity) getActivity()).getLetoolTopBar();
    }

    public LetoolBottomBar getLetoolBottomBar() {
        return ((LocalImageBrowseActivity) getActivity()).getLetoolBottomBar();
    }

    public LetoolSlidingMenu getLetoolSlidingMenu() {
        return ((LocalImageBrowseActivity) getActivity()).getLetoolSlidingMenu();
    }

    public void setMainView(GLBaseView view) {
        ((LocalImageBrowseActivity) getActivity()).setMainView(view);
    }

    public void pushContentFragment(Fragment f) {
         ((LocalImageBrowseActivity) getActivity()).pushContentFragment(f);
    }
    
	public void popContentFragment() {
		 ((LocalImageBrowseActivity) getActivity()).popContentFragment();
	}
	
    public GLController getGLController() {
    	return ((LocalImageBrowseActivity) getActivity()).getGLController();
    }

    public void onMenuClicked() {

    }*/
}
