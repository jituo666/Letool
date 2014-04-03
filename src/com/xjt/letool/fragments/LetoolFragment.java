package com.xjt.letool.fragments;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.LetoolContext;
import com.xjt.letool.ThreadPool;
import com.xjt.letool.common.LLog;
import com.xjt.letool.data.DataManager;
import com.xjt.letool.views.GLController;

import android.content.Context;
import android.os.Looper;
import android.support.v4.app.Fragment;

public abstract class LetoolFragment extends Fragment implements LetoolContext {
    private static final String TAG = "LetoolFragment";
    @Override
    public DataManager getDataManager() {
        LLog.i(TAG, "getDataManager:");
        return ((LetoolApp) getActivity().getApplication()).getDataManager();
    }

    @Override
    public ThreadPool getThreadPool() {
        LLog.i(TAG, "getThreadPool:");
        return ((LetoolApp) getActivity().getApplication()).getThreadPool();
    }

    @Override
    public Context getAndroidContext() {
        LLog.i(TAG, "getAndroidContext:");
        return getActivity().getApplicationContext();
    }
    
    @Override
    public Looper getMainLooper() {
        LLog.i(TAG, "getMainLooper:");
        return getActivity().getMainLooper();
    }
    
    public abstract GLController getGLController();
}
