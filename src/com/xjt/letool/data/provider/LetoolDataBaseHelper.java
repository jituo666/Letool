package com.xjt.letool.data.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LetoolDataBaseHelper extends SQLiteOpenHelper {

    protected static final String DATABASE_NAME = "letool.db";
    protected static final int DATABASE_VERSION = 1;

    private final Context mContext;
    private static LetoolDataBaseHelper sInstance;

    private LetoolDataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
        createLetoolTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void createLetoolTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXIST " + LetoolProvider.TABLE_THUMBNAILS + " (" +
                LetooContent.Thumbnails._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                LetooContent.Thumbnails.ORIGINAL_PATH + " TEXT," +
                LetooContent.Thumbnails.DATE_TAKEN + " INTEGER," +
                LetooContent.Thumbnails.MICRO_THUMBS_DATA + " BLOB," +
                LetooContent.Thumbnails.THUMBS_DATA + " BLOB " +
                ");");
    }
}
