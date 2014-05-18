
package com.xjt.letool;

import android.content.Context;
import android.content.res.Resources;
import android.os.Looper;

import com.xjt.letool.common.ThreadPool;
import com.xjt.letool.metadata.DataManager;

/**
 * @Author Jituo.Xuan
 * @Date 9:07:17 PM May 17, 2014
 * @Comments:null
 */
public interface LetoolContext {

    public DataManager getDataManager();

    public Context getAndroidContext();

    public Looper getMainLooper();

    public Resources getResources();

    public ThreadPool getThreadPool();

}
