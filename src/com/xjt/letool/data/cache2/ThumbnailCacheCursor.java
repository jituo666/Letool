
package com.xjt.letool.data.cache2;

import android.content.Context;
import android.database.Cursor;

import com.xjt.letool.common.LLog;
import com.xjt.letool.data.provider.LetoolContent;

public class ThumbnailCacheCursor {

    private static final String TAG = ThumbnailCacheCursor.class.getSimpleName();
    private Cursor mCursor;
    private Context mApp;

    public ThumbnailCacheCursor(Context app) {
        mApp = app;
    }

    public void resume() {
        mCursor = mApp.getContentResolver().query(LetoolContent.Thumbnails.CONTENT_URI, null, null, null, null);
        LLog.i(TAG, "" + mCursor.getCount());
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void pause() {
        if (mCursor != null) {
            mCursor.close();
        }
    }
}
