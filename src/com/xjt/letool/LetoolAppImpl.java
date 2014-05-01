
package com.xjt.letool;

import com.xjt.letool.common.ThreadPool;
import com.xjt.letool.imagedata.blobcache.BlobCacheService;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.utils.LetoolUtils;

import android.app.Application;
import android.content.Context;

public class LetoolAppImpl extends Application implements LetoolApp {

    private static final String TAG = "LetoolAppImpl";

    private BlobCacheService mImageCacheService;
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
    public BlobCacheService getImageCacheService() {

        synchronized (mLock) { // This method may block on file I/O so a dedicated lock is needed here.
            if (mImageCacheService == null) {
                mImageCacheService = new BlobCacheService(getAndroidContext());
            }
            return mImageCacheService;
        }
    }

    @Override
    public Context getAndroidContext() {
        return this;
    }

}
