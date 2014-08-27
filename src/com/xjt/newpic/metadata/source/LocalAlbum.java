
package com.xjt.newpic.metadata.source;

import android.content.ContentResolver;
import android.content.res.Resources;
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
    private Cursor mCursor;
    private boolean isMergedAlbum;

    public LocalAlbum(MediaPath path, LetoolApp application, int bucketId, boolean isImage) {
        this(path, application, new int[] {
                bucketId
        }, isImage, LocalAlbumSet.getBucketName(application.getContentResolver(), bucketId));
    }

    public LocalAlbum(MediaPath path, LetoolApp application, int[] bucketIds, boolean isImage, String name) {
        super(path, nextVersionNumber());
        mApplication = application;
        mResolver = application.getContentResolver();
        mName = name;
        mIsImage = isImage;
        mBucketId = bucketIds;
        isMergedAlbum = mBucketId.length > 1;
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
            if (!isMergedAlbum) {
                mWhereClause = ImageColumns.BUCKET_ID + " = ?";
            } else {
                mWhereClause = ImageColumns.BUCKET_ID + subWhere;
            }
            mBaseUri = Images.Media.EXTERNAL_CONTENT_URI;
            mOrderClause = ImageColumns.DATE_TAKEN + " DESC ";
            mProjection = LocalImage.PROJECTION;
            mItemPath = LocalImage.ITEM_PATH;
        } else {
            if (!isMergedAlbum) {
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

    @Override
    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        long time = System.currentTimeMillis();
        ArrayList<MediaItem> list = new ArrayList<MediaItem>();
        LetoolUtils.assertNotInRenderThread();
        if (mCursor == null || mCursor.isClosed()) {
            updateMediaSet();
        }
        if (mCursor == null || mCursor.isClosed()) {
            return null;
        }
        if (mCursor.moveToPosition(start)) {
            int i = 0;
            do {
                int id = mCursor.getInt(0);
                MediaPath childPath = new MediaPath(mItemPath, id);
                MediaItem item = loadOrUpdateItem(childPath, mCursor, mApplication.getDataManager(), mApplication, mIsImage);
                list.add(item);
            } while (++i < count && mCursor.moveToNext());
        }
        // LLog.w(TAG, "getMediaItem with count:" + count + " spend " + (System.currentTimeMillis() - time));
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
    public int updateMediaSet() {

        long time = System.currentTimeMillis();
        if (mCursor == null || mCursor.isClosed()) {
            if (!isMergedAlbum) {
                mCursor = mResolver.query(mBaseUri, mProjection, mWhereClause, new String[] {
                        String.valueOf(mBucketId[0])
                }, mOrderClause);
            } else {
                mCursor = mResolver.query(mBaseUri, mProjection, mWhereClause, null, mOrderClause);
            }
            if (mCursor == null) {
                return 0;
            }
        }
        //LLog.i(TAG, "----------------updateCursor Count:" + mCursor.getCount() + " spend " + (System.currentTimeMillis() - time));
        return mCursor.getCount();
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public long reload() {
        if (mNotifier.isDirty()) {
            destroyMediaSet();
            mDataVersion = nextVersionNumber();
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
        if (!isMergedAlbum) {
            mResolver.delete(mBaseUri, mWhereClause,
                    new String[] {
                        String.valueOf(mBucketId)
                    });
        } else {
            mResolver.delete(mBaseUri, mWhereClause, null);
        }
        mApplication.getDataManager().broadcastLocalDeletion();
    }

    @Override
    public boolean isLeafAlbum() {
        return true;
    }

    @Override
    public void destroyMediaSet() {
        if (mCursor != null) {
            try {
                mCursor.close();
            } finally {
                mCursor = null;
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
        if (mCursor != null && !mCursor.isClosed()) {
            return mCursor.getCount();
        }
        int count = 0;
        Cursor c = null;
        try {

            if (!isMergedAlbum) {
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

    public static String getLocalizedName(Resources res, int bucketId, String name) {
        //        if (bucketId == MediaSetUtils.CAMERA_BUCKET_ID) {
        //            return res.getString(R.string.folder_camera);
        //        } else if (bucketId == MediaSetUtils.DOWNLOAD_BUCKET_ID) {
        //            return res.getString(R.string.folder_download);
        //        } else if (bucketId == MediaSetUtils.IMPORTED_BUCKET_ID) {
        //            return res.getString(R.string.folder_imported);
        //        } else if (bucketId == MediaSetUtils.SNAPSHOT_BUCKET_ID) {
        //            return res.getString(R.string.folder_screenshot);
        //        } else if (bucketId == MediaSetUtils.EDITED_ONLINE_PHOTOS_BUCKET_ID) {
        //            return res.getString(R.string.folder_edited_online_photos);
        //        } else {
        return name;
        //        }
    }
}
