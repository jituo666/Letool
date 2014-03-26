package com.xjt.letool;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Looper;

public class LetoolAppImpl extends Application implements LetoolApp {

    private Object mLock = new Object();
    private DataManager mDataManager;
    private ThreadPool mThreadPool;

    @Override
    public void onCreate() {

    }

    @Override
    public synchronized DataManager getDataManager() {
        if (mDataManager == null) {
            mDataManager = new DataManager(this);
            //mDataManager.initializeSourceMap();
        }
        return mDataManager;
    }

    @Override
    public synchronized ThreadPool getThreadPool() {
        if (mThreadPool == null) {
            mThreadPool = new ThreadPool();
        }
        return mThreadPool;
    }

    @Override
    public Context getAndroidContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Looper getMainLooper() {
        // TODO Auto-generated method stub
        return null;
    }

}
