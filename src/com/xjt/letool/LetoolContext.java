package com.xjt.letool;

import android.content.Context;
import android.content.res.Resources;
import android.os.Looper;

import com.xjt.letool.data.DataManager;

public interface LetoolContext {
    public DataManager getDataManager();

    public Context getAndroidContext();

    public Looper getMainLooper();

    public Resources getResources();

    public ThreadPool getThreadPool();
}
