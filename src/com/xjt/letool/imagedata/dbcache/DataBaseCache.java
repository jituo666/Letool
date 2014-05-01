package com.xjt.letool.imagedata.dbcache;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import com.xjt.letool.common.LLog;
import com.xjt.letool.imagedata.provider.LetoolContent;

/**
 * @Author Jituo.Xuan
 * @Date 9:48:12 AM Apr 26, 2014
 * @Comments:null
 */
public class DataBaseCache {

    private static final String TAG = DataBaseCache.class.getSimpleName();

    private Cursor mCursor;
    private Context mApp;
    private long mBucketId;
    private final String[] mProjection;
    private final String[] mSelectioArgs;
    private final String mSelection;
    private final String mOrderClause;
    private long mDateTaken = 0;
    private String mPath;
    private boolean mIsAlbumSet = false;

    public DataBaseCache(Context app) {
        mApp = app;
        mSelection = "1 = 1) GROUP BY (" + LetoolContent.Thumbnails.BUKET_ID;
        mSelectioArgs = null;
        mOrderClause = " MAX(" + LetoolContent.Thumbnails.DATE_TAKEN + ") DESC ";
        mProjection = new String[] { LetoolContent.Thumbnails.BUKET_ID, LetoolContent.Thumbnails.THUMBS_DATA };
        mIsAlbumSet = true;
    }

    public DataBaseCache(Context app, long bucketId) {
        mApp = app;
        mBucketId = bucketId;
        mSelection = LetoolContent.Thumbnails.BUKET_ID + " = ? ";
        mSelectioArgs = new String[] { String.valueOf(bucketId) };
        mOrderClause = LetoolContent.Thumbnails.DATE_TAKEN + " DESC ";
        mProjection = new String[] { LetoolContent.Thumbnails.THUMBS_DATA };
        mIsAlbumSet = false;
    }

    public void resume() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        try {
            mCursor = mApp.getContentResolver().query(LetoolContent.Thumbnails.CONTENT_URI, mProjection, mSelection, mSelectioArgs, mOrderClause);
            LLog.i(TAG, "" + mCursor.getCount());
        } catch (SQLiteException e) {
            LLog.e(TAG, e.getMessage());
        }
    }

    public byte[] getThumbData(int index, String path, long dateTaken) {
        if (mCursor != null && mCursor.moveToPosition(index)) {
            int columnIndex = mCursor.getColumnIndex(LetoolContent.Thumbnails.THUMBS_DATA);
            if (columnIndex >= 0) {
                return mCursor.getBlob(columnIndex);
            }
        }
        mPath = path;
        mDateTaken = dateTaken;
        if (mIsAlbumSet) {
            mBucketId = mPath.subSequence(0, mPath.lastIndexOf("/")).hashCode();
            LLog.i(TAG, " getThumbData buketid-path" + mPath.subSequence(0, mPath.lastIndexOf("/")));
        }
        return null;
    }

    public void putThumbData(byte data[]) {
        try {
            ContentResolver res = mApp.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(LetoolContent.Thumbnails.BUKET_ID, mBucketId);
            values.put(LetoolContent.Thumbnails.PATH_ID, mPath.hashCode());
            values.put(LetoolContent.Thumbnails.DATE_TAKEN, mDateTaken);
            values.put(LetoolContent.Thumbnails.THUMBS_DATA, data);
            res.insert(LetoolContent.Thumbnails.CONTENT_URI, values);
        } catch (SQLiteException e) {
            LLog.e(TAG, e.getMessage());
        }
    }

    public void pause() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }
}
