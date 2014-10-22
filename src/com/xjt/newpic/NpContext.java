
package com.xjt.newpic;

import android.content.Context;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.View;

import com.xjt.newpic.common.OrientationManager;
import com.xjt.newpic.common.ThreadPool;
import com.xjt.newpic.metadata.DataManager;
import com.xjt.newpic.views.GLController;
import com.xjt.newpic.views.NpBottomBar;
import com.xjt.newpic.views.NpSlidingMenu;
import com.xjt.newpic.views.NpTopBar;

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

    public boolean isImagePicking();
    //
    public NpTopBar getLetoolTopBar();

    public NpBottomBar getLetoolBottomBar();

    public OrientationManager getOrientationManager();

    //

    public void showEmptyView(int iconResIcon, int messageResId);

    public void hideEmptyView();

    public void pushContentFragment(Fragment newFragment, Fragment oldFragment, boolean backup);

    public void popContentFragment();
    public void setCapturedMediaUri(Uri uri);

    public NpSlidingMenu getSlidingMenu();
    //
    public boolean isAlbumDirty();
    public void setAlbumDirty(boolean dirty);

}
