package com.xjt.newpic.metadata;

import android.net.Uri;

import com.xjt.newpic.LetoolApp;

import java.util.concurrent.atomic.AtomicBoolean;

public class DataNotifier {

    private MediaSet mMediaSet;
    private AtomicBoolean mContentDirty = new AtomicBoolean(true);

    public DataNotifier(MediaSet set, Uri uri, LetoolApp app) {
        mMediaSet = set;
        app.getDataManager().registerDataNotifier(uri, this);
    }

    public boolean isDirty() {
        return mContentDirty.compareAndSet(true, false);
    }

    public void fakeChange() {
        onChange(false);
    }

    public void onChange(boolean selfChange) {
        if (mContentDirty.compareAndSet(false, true)) {
            mMediaSet.notifyContentChanged();
        }
    }
}