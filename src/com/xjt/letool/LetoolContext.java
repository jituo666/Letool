
package com.xjt.letool;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.xjt.letool.common.OrientationManager;
import com.xjt.letool.common.ThreadPool;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.view.GLController;
import com.xjt.letool.view.LetoolBottomBar;
import com.xjt.letool.view.LetoolSlidingMenu;
import com.xjt.letool.view.LetoolTopBar;

/**
 * @Author Jituo.Xuan
 * @Date 9:07:17 PM May 17, 2014
 * @Comments:null
 */
public interface LetoolContext {

    public Context getActivityContext();

    //
    public DataManager getDataManager();

    public boolean isImageBrwosing();

    public ThreadPool getThreadPool();

    public GLController getGLController();

    //
    public LetoolTopBar getLetoolTopBar();

    public LetoolBottomBar getLetoolBottomBar();

    public OrientationManager getOrientationManager();

    //
    public void showEmptyView(int iconResIcon,int messageResId);

    public void hideEmptyView();

    public void pushContentFragment(Fragment newFragment, Fragment oldFragment, boolean backup);

    public void popContentFragment();

    public LetoolSlidingMenu getLetoolSlidingMenu();

}
