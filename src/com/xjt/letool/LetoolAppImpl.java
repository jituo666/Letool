package com.xjt.letool;

import com.xjt.letool.data.DataManager;
import com.xjt.letool.data.cache.ImageCacheService;
import com.xjt.letool.utils.LetoolUtils;

import android.app.Application;
import android.content.Context;
public class LetoolAppImpl extends Application implements LetoolApp {

    private ImageCacheService mImageCacheService;
    private Object mLock = new Object();
    private DataManager mDataManager;
    private ThreadPool mThreadPool;

    @Override
    public void onCreate() {
        super.onCreate();
        LetoolUtils.initialize(this);
    }

    @Override
    public synchronized DataManager getDataManager() {
        if (mDataManager == null) {
            mDataManager = new DataManager(this);
            mDataManager.initializeSourceMap();
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
    public ImageCacheService getImageCacheService() {
        // This method may block on file I/O so a dedicated lock is needed here.
        synchronized (mLock) {
            if (mImageCacheService == null) {
                mImageCacheService = new ImageCacheService(getAndroidContext());
            }
            return mImageCacheService;
        }
    }

    @Override
    public Context getAndroidContext() {
        return this;
    }

}
