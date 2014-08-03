
package com.xjt.newpic.metadata.source;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;

import com.xjt.newpic.LetoolApp;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.metadata.DataManager;
import com.xjt.newpic.metadata.DataNotifier;
import com.xjt.newpic.metadata.MediaItem;
import com.xjt.newpic.metadata.MediaPath;
import com.xjt.newpic.metadata.MediaSet;
import com.xjt.newpic.metadata.image.LocalImage;
import com.xjt.newpic.metadata.image.LocalMediaItem;
import com.xjt.newpic.metadata.video.LocalVideo;
import com.xjt.newpic.utils.LetoolUtils;

import java.util.ArrayList;

public class LocalAlbum extends MediaSet {

    private static final String TAG = LocalAlbum.class.getSimpleName();
    private String mWhereClause;
    private final String mOrderClause;
    private final Uri mBaseUri;
    private final String[] mProjection;

    private final LetoolApp mApplication;
    private final ContentResolver mResolver;
    private final int mBucketId[];
    private final String mName;
    private final boolean mIsImage;
    private final DataNotifier mNotifier;
    private final String mItemPath;
    private Cursor mAlbumCursor;

    public LocalAlbum(MediaPath path, LetoolApp application, int[] bucketIds, boolean isImage, String name) {
        super(path, nextVersionNumber());
        mApplication = application;
        mResolver = application.getContentResolver();
        mName = name;
        mIsImage = isImage;
        mBucketId = bucketIds;
        StringBuilder sbValue = new StringBuilder();
        if (bucketIds.length <= 0) {
            mWhereClause = null;
        } else {
            for (int id : bucketIds) {
                sbValue.append(id).append(",");
            }
            sbValue = sbValue.deleteCharAt(sbValue.length() - 1);
        }
        final String subWhere = " in(" + sbValue.toString() + ")";
        if (isImage) {
            if (mBucketId.length == 1) {
                mWhereClause = ImageColumns.BUCKET_ID + " = ?";
            } else {
                mWhereClause = ImageColumns.BUCKET_ID + subWhere;
            }
            mBaseUri = Images.Media.EXTERNAL_CONTENT_URI;
            mOrderClause = ImageColumns.DATE_TAKEN + " DESC ";
            mProjection = LocalImage.PROJECTION;
            mItemPath = LocalImage.ITEM_PATH;
        } else {
            if (mBucketId.length == 1) {
                mWhereClause = VideoColumns.BUCKET_ID + " = ?";
            } else {
                mWhereClause = VideoColumns.BUCKET_ID + subWhere;
            }
            mBaseUri = Video.Media.EXTERNAL_CONTENT_URI;
            mOrderClause = VideoColumns.DATE_TAKEN + " DESC ";
            mProjection = LocalVideo.PROJECTION;
            mItemPath = LocalVideo.ITEM_PATH;
        }
        //mWhereClause += " and 1=2 ";
        mNotifier = new DataNotifier(this, mBaseUri, application);
    }

    public LocalAlbum(MediaPath path, LetoolApp application, int bucketId, boolean isImage) {
        this(path, application, new int[] {
                bucketId
        }, isImage, LocalAlbumSet.getBucketName(application.getContentResolver(), bucketId));
    }

    @Override
    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        long time = System.currentTimeMillis();
        ArrayList<MediaItem> list = new ArrayList<MediaItem>();
        LetoolUtils.assertNotInRenderThread();
        if (mAlbumCursor == null || mAlbumCursor.isClosed()) {
            getAllMediaItems();
        }
        if (mAlbumCursor == null || mAlbumCursor.isClosed()) {
            return null;
        }
        if (mAlbumCursor.moveToPosition(start)) {
            int i = 0;
            do {
                int id = mAlbumCursor.getInt(0);
                MediaPath childPath = new MediaPath(mItemPath, id);
                MediaItem item = loadOrUpdateItem(childPath, mAlbumCursor, mApplication.getDataManager(), mApplication, mIsImage);
                list.add(item);
            } while (++i < count && mAlbumCursor.moveToNext());
        }
        LLog.w(TAG, "getMediaItem with count:" + count + " spend " + (System.currentTimeMillis() - time));
        return list;
    }

    private static MediaItem loadOrUpdateItem(MediaPath path, Cursor cursor, DataManager dataManager, LetoolApp app, boolean isImage) {
        LocalMediaItem item = (LocalMediaItem) path.getObject();
        if (item == null) {
            if (isImage) {
                item = new LocalImage(path, app, cursor);
            } else {
                item = new LocalVideo(path, app, cursor);
            }
        } else {
            item.updateContent(cursor);
        }
        return item;
    }

    @Override
    public int getAllMediaItems() {

        long time = System.currentTimeMillis();
        if (mAlbumCursor == null || mAlbumCursor.isClosed()) {
            if (mBucketId.length == 1) {
                mAlbumCursor = mResolver.query(mBaseUri, mProjection, mWhereClause, new String[] {String.valueOf(mBucketId[0])
                }, mOrderClause);
            } else {
                mAlbumCursor = mResolver.query(mBaseUri, mProjection, mWhereClause, null, mOrderClause);
            }
            if (mAlbumCursor == null) {
                return 0;
            }
        }
        LLog.i(TAG, "----------------getAllMediaItem Count:" + mAlbumCursor.getCount() + " spend " + (System.currentTimeMillis() - time));
        return mAlbumCursor.getCount();
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public long reload() {
        if (mNotifier.isDirty()) {
            mDataVersion = nextVersionNumber();
            closeCursor();
        }
        return mDataVersion;
    }

    @Override
    public int getSupportedOperations() {
        return SUPPORT_DELETE | SUPPORT_SHARE | SUPPORT_INFO;
    }

    @Override
    public void delete() {
        LetoolUtils.assertNotInRenderThread();
        mResolver.delete(mBaseUri, mWhereClause,
                new String[] {
                    String.valueOf(mBucketId)
                });
        mApplication.getDataManager().broadcastLocalDeletion();
    }

    @Override
    public boolean isLeafAlbum() {
        return true;
    }

    @Override
    public void closeCursor() {
        if (mAlbumCursor != null) {
            try {
                mAlbumCursor.close();
            } finally {
                mAlbumCursor = null;
            }
        }
    }

    public static Cursor getItemCursor(ContentResolver resolver, Uri uri, String[] projection, long id) {
        return resolver.query(uri, projection, "_id=?",
                new String[] {
                    String.valueOf(id)
                }, null);
    }

    @Override
    public int getMediaCount() {
        int count = 0;
        Cursor c = null;
        try {
            if (mBucketId.length == 1) {
                c = mResolver.query(mBaseUri, new String[] {
                        "count(*)"
                }, mWhereClause, new String[] {
                        String.valueOf(mBucketId[0])
                }, null);
            } else {
                c = mResolver.query(mBaseUri, new String[] {
                        "count(*)"
                }, mWhereClause, null, null);
            }
            if (c != null && c.moveToFirst()) {
                count = c.getInt(0);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return count;
    }
}
