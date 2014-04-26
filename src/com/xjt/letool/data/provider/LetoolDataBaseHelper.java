package com.xjt.letool.data.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;

public class LetoolDataBaseHelper extends SQLiteOpenHelper {

    protected static final String DATABASE_NAME = "letool.db";
    protected static final String DATABASE_PATH = Environment.getExternalStorageDirectory() + File.separator + DATABASE_NAME;
    protected static final int DATABASE_VERSION = 1;

    private static final int DB_CAHCE_PAGE_SIZE = 1024;
    private final Context mContext;
    private static LetoolDataBaseHelper sInstance;

    private LetoolDataBaseHelper(Context context) {
        super(context, DATABASE_PATH, null, DATABASE_VERSION);
        SQLiteDatabase.openOrCreateDatabase(DATABASE_PATH, null);
        mContext = context;
    }

    static synchronized LetoolDataBaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new LetoolDataBaseHelper(context);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.setPageSize(DB_CAHCE_PAGE_SIZE);
        createLetoolTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void createLetoolTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + LetoolProvider.TABLE_THUMBNAILS + " (" +
                LetoolContent.Thumbnails._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                LetoolContent.Thumbnails.BUKET_ID + " INTEGER," +
                LetoolContent.Thumbnails.PATH_ID + " INTEGER," +
                LetoolContent.Thumbnails.DATE_TAKEN + " INTEGER," +
                LetoolContent.Thumbnails.THUMBS_DATA + " BLOB " +
                ");");
    }
}
