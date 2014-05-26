
package com.xjt.letool.metadata.loader;

import android.os.Message;
import android.os.Process;

import com.xjt.letool.common.LLog;
import com.xjt.letool.common.SynchronizedHandler;
import com.xjt.letool.fragment.LetoolFragment;
import com.xjt.letool.metadata.ContentListener;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.metadata.MediaItem;
import com.xjt.letool.metadata.MediaObject;
import com.xjt.letool.metadata.MediaPath;
import com.xjt.letool.metadata.MediaSet;
import com.xjt.letool.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class ThumbnailDataLoader {

    private static final String TAG = ThumbnailDataLoader.class.getSimpleName();
    private static final int DATA_CACHE_SIZE = 1000;

    private static final int MSG_LOAD_START = 1;
    private static final int MSG_LOAD_FINISH = 2;
    private static final int MSG_RUN_OBJECT = 3;

    private static final int MIN_LOAD_COUNT = 32;
    private static final int MAX_LOAD_COUNT = 48;

    private final MediaItem[] mData;
    private final long[] mItemVersion;
    private final long[] mSetVersion;
    private int mActiveStart = 0;
    private int mActiveEnd = 0;
    private int mContentStart = 0;
    private int mContentEnd = 0;
    private int mSize = 0;
    private final MediaSet mSource;
    private long mSourceVersion = MediaObject.INVALID_DATA_VERSION;

    private final SynchronizedHandler mMainHandler;
    private MySourceListener mSourceListener = new MySourceListener();
    private DataLoadingListener mLoadingListener;
    private DataChangedListener mDataChangedListener;
    private ReloadTask mReloadTask;
    private long mFailedVersion = MediaObject.INVALID_DATA_VERSION; // the data version on which last loading failed

    public static interface DataChangedListener {

        public void onSizeChanged(int size);

        public void onContentChanged(int index);
    }

    public ThumbnailDataLoader(LetoolFragment context, MediaSet mediaSet) {
        mSource = mediaSet;
        mData = new MediaItem[DATA_CACHE_SIZE];
        mItemVersion = new long[DATA_CACHE_SIZE];
        mSetVersion = new long[DATA_CACHE_SIZE];
        Arrays.fill(mItemVersion, MediaObject.INVALID_DATA_VERSION);
        Arrays.fill(mSetVersion, MediaObject.INVALID_DATA_VERSION);

        mMainHandler = new SynchronizedHandler(context.getGLController()) {

            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_RUN_OBJECT:
                        ((Runnable) message.obj).run();
                        return;
                    case MSG_LOAD_START:
                        if (mLoadingListener != null)
                            mLoadingListener.onLoadingStarted();
                        return;
                    case MSG_LOAD_FINISH:
                        if (mLoadingListener != null) {
                            boolean loadFailed = (mFailedVersion == MediaObject.INVALID_DATA_VERSION);
                            mLoadingListener.onLoadingFinished(loadFailed);
                        }
                        return;
                }
            }
        };
    }

    public void resume() {
        LLog.i(TAG, "resume" + System.currentTimeMillis());
        mSource.addContentListener(mSourceListener);
        mReloadTask = new ReloadTask();
        mReloadTask.start();
    }

    public void pause() {
        mReloadTask.terminate();
        mReloadTask = null;
        mSource.removeContentListener(mSourceListener);
    }

    public int findItem(MediaPath path) {
        for (int i = mContentStart; i < mContentEnd; i++) {
            MediaItem item = mData[i % DATA_CACHE_SIZE];
            if (item != null && path == item.getPath()) {
                return i;
            }
        }
        return -1;
    }

    public MediaItem get(int index) {
        if (!isActive(index)) {
            throw new IllegalArgumentException(String.format("%s not in (%s, %s)", index, mActiveStart, mActiveEnd));
        }
        return mData[index % mData.length];
    }

    public int getActiveStart() {
        return mActiveStart;
    }

    public boolean isActive(int index) {
        return index >= mActiveStart && index < mActiveEnd;
    }

    public int size() {
        return mSize;
    }

    public MediaSet getMediaSource() {
        return mSource;
    }

    private void clearThumbnail(int Index) {
        mData[Index] = null;
        mItemVersion[Index] = MediaObject.INVALID_DATA_VERSION;
        mSetVersion[Index] = MediaObject.INVALID_DATA_VERSION;
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart == mContentStart && contentEnd == mContentEnd)
            return;
        int end = mContentEnd;
        int start = mContentStart;
        // We need change the content window before calling reloadData(...)
        synchronized (this) {
            mContentStart = contentStart;
            mContentEnd = contentEnd;
        }
        if (contentStart >= end || start >= contentEnd) {
            for (int i = start, n = end; i < n; ++i) {
                clearThumbnail(i % DATA_CACHE_SIZE);
            }
        } else {
            for (int i = start; i < contentStart; ++i) {
                clearThumbnail(i % DATA_CACHE_SIZE);
            }
            for (int i = contentEnd, n = end; i < n; ++i) {
                clearThumbnail(i % DATA_CACHE_SIZE);
            }
        }
        if (mReloadTask != null)
            mReloadTask.notifyDirty();
    }

    /**
     * 1,set image/video's meta data activate range
     * 2,set iamge/video's meta data cache range
     */
    public void setActiveWindow(int start, int end) {
        if (start == mActiveStart && end == mActiveEnd)
            return;
        Utils.assertTrue(start <= end && end - start <= mData.length && end <= mSize);
        int length = mData.length;
        mActiveStart = start;
        mActiveEnd = end;
        if (start == end) // If no data is visible, keep the cache content
            return;
        int contentStart = Utils.clamp((start + end) / 2 - length / 2, 0, Math.max(0, mSize - length));
        int contentEnd = Math.min(contentStart + length, mSize);
        if (mContentStart > start || mContentEnd < end || Math.abs(contentStart - mContentStart) > MIN_LOAD_COUNT) {
            setContentWindow(contentStart, contentEnd);
        }
    }

    private class MySourceListener implements ContentListener {

        @Override
        public void onContentDirty() {
            if (mReloadTask != null)
                mReloadTask.notifyDirty();
        }
    }

    public void setDataChangedListener(DataChangedListener listener) {
        mDataChangedListener = listener;
    }

    public void setLoadingListener(DataLoadingListener listener) {
        mLoadingListener = listener;
    }

    private <T> T executeAndWait(Callable<T> callable) {
        FutureTask<T> task = new FutureTask<T>(callable);
        mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_RUN_OBJECT, task));
        try {
            return task.get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static class UpdateInfo {

        public long version;
        public int reloadStart;
        public int reloadCount;
        public int size;
        public ArrayList<MediaItem> items;
    }

    private class GetUpdateInfo implements Callable<UpdateInfo> {

        private final long mVersion;

        public GetUpdateInfo(long version) {
            mVersion = version;
        }

        public UpdateInfo call() throws Exception {
            UpdateInfo info = new UpdateInfo();
            long version = mVersion;
            info.version = mSourceVersion;
            info.size = mSize;
            long setVersion[] = mSetVersion;
            for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
                int index = i % DATA_CACHE_SIZE;
                if (setVersion[index] != version) {
                    info.reloadStart = i;
                    info.reloadCount = Math.min(MAX_LOAD_COUNT, n - i);
                    return info;
                }
            }
            return mSourceVersion == mVersion ? null : info;
        }
    }

    private class UpdateContent implements Callable<Void> {

        private UpdateInfo mUpdateInfo;

        public UpdateContent(UpdateInfo info) {
            mUpdateInfo = info;
        }

        @Override
        public Void call() throws Exception {
            UpdateInfo info = mUpdateInfo;
            mSourceVersion = info.version;
            if (mSize != info.size) {
                mSize = info.size;
                if (mDataChangedListener != null)
                    mDataChangedListener.onSizeChanged(mSize);
                if (mContentEnd > mSize)
                    mContentEnd = mSize;
                if (mActiveEnd > mSize)
                    mActiveEnd = mSize;
            }

            ArrayList<MediaItem> items = info.items;

            if (items == null)
                return null;
            int start = Math.max(info.reloadStart, mContentStart);
            int end = Math.min(info.reloadStart + items.size(), mContentEnd);

            for (int i = start; i < end; ++i) {
                int index = i % DATA_CACHE_SIZE;
                mSetVersion[index] = info.version;
                MediaItem updateItem = items.get(i - info.reloadStart);
                long itemVersion = updateItem.getDataVersion();
                if (mItemVersion[index] != itemVersion) {
                    mItemVersion[index] = itemVersion;
                    mData[index] = updateItem;
                    if (mDataChangedListener != null && i >= mActiveStart && i < mActiveEnd) {
                        LLog.i(TAG, "UpdateContent:" + index + " :" + System.currentTimeMillis());
                        mDataChangedListener.onContentChanged(i);
                    }
                }
            }
            return null;
        }
    }

    /*
     * The thread model of ReloadTask * [Reload Task] [Main Thread] | |
     * getUpdateInfo() --> | (synchronous call) (wait) <---- getUpdateInfo() | |
     * Load Data | | | updateContent() --> | (synchronous call) (wait)
     * updateContent() | | | |
     */
    private class ReloadTask extends Thread {

        private volatile boolean mActive = true;
        private volatile boolean mDirty = true;
        private boolean mIsLoading = false;

        private void updateLoading(boolean loading) {
            if (mIsLoading == loading)
                return;
            mIsLoading = loading;
            if (loading)
                LLog.i(TAG, "MSG_LOAD_START" + System.currentTimeMillis());
            else
                LLog.i(TAG, "MSG_LOAD_FINISH" + System.currentTimeMillis());
            mMainHandler.sendEmptyMessage(loading ? MSG_LOAD_START : MSG_LOAD_FINISH);
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            boolean updateComplete = false;
            while (mActive) {
                synchronized (this) {
                    if (mActive && !mDirty && updateComplete) {
                        updateLoading(false);
                        Utils.waitWithoutInterrupt(this);
                        continue;
                    }
                }
                mDirty = false;
                updateLoading(true);
                long version;
                synchronized (DataManager.LOCK) {
                    version = mSource.reload();
                }
                UpdateInfo info = executeAndWait(new GetUpdateInfo(version));
                updateComplete = info == null;
                if (updateComplete)
                    continue;
                synchronized (DataManager.LOCK) {
                    if (info.version != version) {
                        info.size = mSource.getMediaItemCount(false); 
                        info.version = version;
                    }
                    if (info.reloadCount > 0) {
                        info.items = mSource.getMediaItem(info.reloadStart, info.reloadCount);
                    }
                }
                executeAndWait(new UpdateContent(info));
            }
            updateLoading(false);
        }

        public synchronized void notifyDirty() {
            mDirty = true;
            notifyAll();
        }

        public synchronized void terminate() {
            mActive = false;
            notifyAll();
        }
    }
}
