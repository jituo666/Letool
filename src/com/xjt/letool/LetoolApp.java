package com.xjt.letool;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Looper;

import com.xjt.letool.common.ThreadPool;
import com.xjt.letool.imagedata.blobcache.BlobCacheService;
import com.xjt.letool.metadata.DataManager;

public interface LetoolApp {
    public DataManager getDataManager();

    public BlobCacheService getImageCacheService();
//    public DownloadCache getDownloadCache();
    public ThreadPool getThreadPool();

    public Context getAndroidContext();
    public Looper getMainLooper();
    public ContentResolver getContentResolver();
    public Resources getResources();
}
