
package com.xjt.newpic;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;

import com.xjt.newpic.common.OrientationManager;
import com.xjt.newpic.common.ThreadPool;
import com.xjt.newpic.metadata.DataManager;
import com.xjt.newpic.view.GLController;
import com.xjt.newpic.view.LetoolBottomBar;
import com.xjt.newpic.view.LetoolSlidingMenu;
import com.xjt.newpic.view.LetoolTopBar;

/**
 * @Author Jituo.Xuan
 * @Date 9:07:17 PM May 17, 2014
 * @Comments:null
 */
public interface NpContext {

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
    public View getGuidTipView();

    public void showEmptyView(int iconResIcon, int messageResId);

    public void hideEmptyView();

    public void pushContentFragment(Fragment newFragment, Fragment oldFragment, boolean backup);

    public void popContentFragment();

    public LetoolSlidingMenu getLetoolSlidingMenu();

}
