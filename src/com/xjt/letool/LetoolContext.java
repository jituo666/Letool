
package com.xjt.letool;

import android.content.Context;
import android.content.res.Resources;
import android.os.Looper;

import com.xjt.letool.common.ThreadPool;
import com.xjt.letool.data.DataManager;
import com.xjt.letool.view.LetoolSlidingMenu;

public interface LetoolContext {

    public DataManager getDataManager();

    public Context getAndroidContext();

    public Looper getMainLooper();

    public Resources getResources();

    public ThreadPool getThreadPool();

//    public LetoolActionBar getLetoolActionBar();
//
//    public LetoolSlidingMenu getLetoolSlidingMenu();
}
