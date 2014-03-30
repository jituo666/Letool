package com.xjt.letool;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Looper;

import com.xjt.letool.data.DataManager;
import com.xjt.letool.data.cache.ImageCacheService;

public interface LetoolApp {
    public DataManager getDataManager();

    public ImageCacheService getImageCacheService();
//    public DownloadCache getDownloadCache();
    public ThreadPool getThreadPool();

    public Context getAndroidContext();
    public Looper getMainLooper();
    public ContentResolver getContentResolver();
    public Resources getResources();
}
