package com.xjt.letool;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class BatchService extends Service {

    public class LocalBinder extends Binder {
        BatchService getService() {
            return BatchService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();
    private ThreadPool mThreadPool = new ThreadPool(1, 1);

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // The threadpool returned by getThreadPool must have only 1 thread
    // running at a time, as MenuExecutor (atrociously) depends on this
    // guarantee for synchronization.
    public ThreadPool getThreadPool() {
        return mThreadPool;
    }
}
