package com.xjt.letool.activities;

import com.xjt.letool.BatchService;
import com.xjt.letool.LetoolActionBar;
import com.xjt.letool.LetoolApp;
import com.xjt.letool.LetoolContext;
import com.xjt.letool.OrientationManager;
import com.xjt.letool.R;
import com.xjt.letool.ThreadPool;
import com.xjt.letool.TransitionStore;
import com.xjt.letool.BatchService.LocalBinder;
import com.xjt.letool.R.id;
import com.xjt.letool.data.DataManager;
import com.xjt.letool.data.MediaItem;
import com.xjt.letool.data.utils.LetoolBitmapPool;
import com.xjt.letool.pages.PageManager;
import com.xjt.letool.views.GLController;
import com.xjt.letool.views.GLRootView;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

public class LetoolBaseActivity extends Activity implements LetoolContext {

    private GLRootView mGLRootView;
    private PageManager mPageManager;
    private LetoolActionBar mActionBar;
    private OrientationManager mOrientationManager;
    private boolean mDisableToggleStatusBar;
    private TransitionStore mTransitionStore = new TransitionStore();

    private BatchService mBatchService;
    private boolean mBatchServiceIsBound = false;
    private ServiceConnection mBatchServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBatchService = ((BatchService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mBatchService = null;
        }
    };

    protected void disableToggleStatusBar() {
        mDisableToggleStatusBar = true;
    }

    // Shows status bar in portrait view, hide in landscape view
    private void toggleStatusBarByOrientation() {
        if (mDisableToggleStatusBar)
            return;
        Window win = getWindow();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            win.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOrientationManager = new OrientationManager(this);
        toggleStatusBarByOrientation();
        getWindow().setBackgroundDrawable(null);
        doBindBatchService();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mGLRootView.lockRenderThread();
        try {
            super.onSaveInstanceState(outState);
            getPageManager().saveState(outState);
        } finally {
            mGLRootView.unlockRenderThread();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        mPageManager.onConfigurationChange(config);
        getLetoolActionBar().onConfigurationChanged();
        invalidateOptionsMenu();
        toggleStatusBarByOrientation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return getPageManager().createOptionsMenu(menu);
    }

    @Override
    public void setContentView(int resId) {
        super.setContentView(resId);
        mGLRootView = (GLRootView) findViewById(R.id.gl_root_view);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getExternalCacheDir() == null) {
            OnCancelListener onCancel = new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            };
            OnClickListener onClick = new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            };

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLRootView.lockRenderThread();
        try {
            getPageManager().resume();
            getDataManager().resume();
        } finally {
            mGLRootView.unlockRenderThread();
        }
        mGLRootView.onResume();
        mOrientationManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mOrientationManager.pause();
        mGLRootView.onPause();
        mGLRootView.lockRenderThread();
        try {
            getPageManager().pause();
            getDataManager().pause();
        } finally {
            mGLRootView.unlockRenderThread();
        }
        LetoolBitmapPool.getInstance().clear();
        MediaItem.getBytesBufferPool().clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGLRootView.lockRenderThread();
        try {
            getPageManager().destroy();
        } finally {
            mGLRootView.unlockRenderThread();
        }
        doUnbindBatchService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mGLRootView.lockRenderThread();
        try {
            getPageManager().notifyActivityResult(requestCode, resultCode, data);
        } finally {
            mGLRootView.unlockRenderThread();
        }
    }

    @Override
    public void onBackPressed() {
        // send the back event to the top sub-state
        GLController root = getGLController();
        root.lockRenderThread();
        try {
            getPageManager().onBackPressed();
        } finally {
            root.unlockRenderThread();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public DataManager getDataManager() {
        return ((LetoolApp) getApplication()).getDataManager();
    }

    @Override
    public Context getAndroidContext() {
        return this;
    }

    @Override
    public ThreadPool getThreadPool() {
        return ((LetoolApp) getApplication()).getThreadPool();
    }

    public GLController getGLController() {
        return mGLRootView;
    }

    public OrientationManager getOrientationManager() {
        return mOrientationManager;
    }

    public synchronized PageManager getPageManager() {
        if (mPageManager == null) {
            mPageManager = new PageManager(this);
        }
        return mPageManager;
    }

    public TransitionStore getTransitionStore() {
        return mTransitionStore;
    }

    public LetoolActionBar getLetoolActionBar() {
        if (mActionBar == null) {
            mActionBar = new LetoolActionBar(this);
        }
        return mActionBar;
    }

    private void doBindBatchService() {
        bindService(new Intent(this, BatchService.class), mBatchServiceConnection, Context.BIND_AUTO_CREATE);
        mBatchServiceIsBound = true;
    }

    private void doUnbindBatchService() {
        if (mBatchServiceIsBound) {
            // Detach our existing connection.
            unbindService(mBatchServiceConnection);
            mBatchServiceIsBound = false;
        }
    }

    public ThreadPool getBatchServiceThreadPoolIfAvailable() {
        if (mBatchServiceIsBound && mBatchService != null) {
            return mBatchService.getThreadPool();
        } else {
            throw new RuntimeException("Batch service unavailable");
        }
    }
}
