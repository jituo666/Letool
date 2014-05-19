
package com.xjt.letool.imagedata.provider;

import com.xjt.letool.common.LLog;
import com.xjt.letool.utils.StorageUtils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class LetoolProvider extends ContentProvider {

    private static final String TAG = LetoolProvider.class.getSimpleName();

    private static final String NO_DELETES_INSERTS_OR_UPDATES = "LetoolProvider does not support deletes, inserts, or updates for this URI.";
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    public static final String AUTHORITY = "lt_provider_authories";

    static final String TABLE_THUMBNAILS = "thumbnails";

    private static final int URI_THUMBNAILS = 1;

    private SQLiteOpenHelper mOpenHelper;

    static {
        URI_MATCHER.addURI(AUTHORITY, TABLE_THUMBNAILS, URI_THUMBNAILS);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Context context = getContext();
        int affectedRows = 0;

        switch (URI_MATCHER.match(uri)) {
            case URI_THUMBNAILS:
                affectedRows = db.delete(TABLE_THUMBNAILS, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException(NO_DELETES_INSERTS_OR_UPDATES + uri);
        }

        if (affectedRows > 0) {
            context.getContentResolver().notifyChange(LetoolContent.CONTENT_URI, null);
        }
        return affectedRows;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = URI_MATCHER.match(uri);
        String table = TABLE_THUMBNAILS;
        switch (match) {
            case URI_THUMBNAILS: {
                table = TABLE_THUMBNAILS;
                break;
            }
            default:
                LLog.e(TAG, "insert: invalid request: " + uri);
                return null;
        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues finalValues;
        Uri res = LetoolContent.CONTENT_URI;
        long rowId;
        if (table.equals(TABLE_THUMBNAILS)) {
            finalValues = new ContentValues(values);
            if ((rowId = db.insert(table, null, finalValues)) <= 0) {
                LLog.e(TAG, "LetoolProvider.insert: failed! " + finalValues);
                return null;
            }
            res = Uri.parse(res + "/" + rowId);
        }

        return res;
    }

    @Override
    public boolean onCreate() {
        if (StorageUtils.externalStorageAvailable()) {
            mOpenHelper = LetoolDataBaseHelper.getInstance(getContext());
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] args, String sortOrder) {
        Cursor cursor = null;
        int math = URI_MATCHER.match(uri);
        switch (math) {
            case URI_THUMBNAILS:
                cursor = getThumbnailByPath(projection, selection, args, sortOrder);
                break;
            default:
                throw new IllegalStateException("Unrecognized URI:" + uri);
        }

        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), LetoolContent.CONTENT_URI);
        }
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        return 0;
    }

    private Cursor getThumbnailByPath(String[] projection, String selection, String[] selectionArgs, String order) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setDistinct(true);
        queryBuilder.setTables(TABLE_THUMBNAILS);
        return queryBuilder.query(mOpenHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, order);
    }
}
