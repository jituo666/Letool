
package com.xjt.letool.metadata.source;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.common.LLog;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.metadata.DataNotifier;
import com.xjt.letool.metadata.MediaItem;
import com.xjt.letool.metadata.MediaPath;
import com.xjt.letool.metadata.MediaSet;
import com.xjt.letool.metadata.image.LocalFullImage;
import com.xjt.letool.metadata.image.LocalImage;
import com.xjt.letool.metadata.image.LocalMediaItem;
import com.xjt.letool.metadata.video.LocalVideo;
import com.xjt.letool.utils.LetoolUtils;

import java.util.ArrayList;

public class PhotoAlbum extends MediaSet {

    private static final String TAG = PhotoAlbum.class.getSimpleName();

    private final LetoolApp mApplication;
    private final ContentResolver mResolver;
    private final Uri mBaseUri;
    private final DataNotifier mNotifier;
    //
    private String mWhereClause;
    private final String mOrderClause;
    private String[] mProjection;
    private final String mName;
    private final boolean mIsImage;
    private final String mItemPath;
    private Cursor mAlbumCursor;
    private boolean mFullInfo = false;

    public PhotoAlbum(MediaPath path, LetoolApp application, int[] bucketId, boolean isImage, String name) {
        super(path, nextVersionNumber());
        mApplication = application;
        mResolver = application.getContentResolver();
        mName = name;
        mIsImage = isImage;

        StringBuilder sbValue = new StringBuilder();
        if (bucketId.length <= 0) {
            mWhereClause = null;
        } else {
            for (int id : bucketId) {
                sbValue.append(id).append(",");
            }
            sbValue = sbValue.deleteCharAt(sbValue.length() - 1);
        }
        final String subWhere = " in(" + sbValue.toString() + ")";
        if (isImage) {
            mWhereClause = ImageColumns.BUCKET_ID + subWhere;
            mOrderClause = ImageColumns.DATE_TAKEN + " DESC, "
                    + ImageColumns._ID + " DESC";
            mBaseUri = Images.Media.EXTERNAL_CONTENT_URI;
            mProjection = LocalImage.PROJECTION;
            mItemPath = LocalImage.ITEM_PATH;
        } else {
            mWhereClause = VideoColumns.BUCKET_ID + subWhere;
            mOrderClause = VideoColumns.DATE_TAKEN + " DESC, "
                    + VideoColumns._ID + " DESC";
            mBaseUri = Video.Media.EXTERNAL_CONTENT_URI;
            mProjection = LocalVideo.PROJECTION;
            mItemPath = LocalVideo.ITEM_PATH;
        }
        mNotifier = new DataNotifier(this, mBaseUri, application);
    }

    @Override
    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        long time = System.currentTimeMillis();
        ArrayList<MediaItem> list = new ArrayList<MediaItem>();
        LetoolUtils.assertNotInRenderThread();
        if (mAlbumCursor == null)
            return list;
        if (mAlbumCursor.moveToPosition(start)) {
            int i = 0;
            do {
                int id = mAlbumCursor.getInt(0);  // _id must be in the first column
                MediaPath childPath = new MediaPath(mItemPath, id);
                MediaItem item = loadOrUpdateItem(childPath, mAlbumCursor, mApplication.getDataManager(), mApplication, mIsImage);
                list.add(item);
            } while ((++i < count) && mAlbumCursor.moveToNext());
        }
        LLog.w(TAG, "query getMediaItem count:" + count + " spend " + (System.currentTimeMillis() - time));
        return list;
    }

    private MediaItem loadOrUpdateItem(MediaPath path, Cursor cursor, DataManager dataManager, LetoolApp app, boolean isImage) {
        LocalMediaItem item = (LocalMediaItem) path.getObject();
        if (item == null) {
            if (isImage) {
                if (mFullInfo) {
                    item = new LocalFullImage(path, app, cursor);
                } else {
                    item = new LocalImage(path, app, cursor);
                }
            } else {
                item = new LocalVideo(path, app, cursor);
            }
        } else {
            item.updateContent(cursor);
        }
        return item;
    }

    @Override
    public int getMediaItemCount(boolean withFullInfo) {
        long time = System.currentTimeMillis();
        mFullInfo = withFullInfo;
        if (mAlbumCursor == null) {
            if( mFullInfo ) {
                mProjection = LocalFullImage.PROJECTION;
            } else {
                mProjection = LocalImage.PROJECTION;
            }
            mAlbumCursor = mResolver.query(mBaseUri, mProjection, mWhereClause, null, mOrderClause);
            if (mAlbumCursor == null) {
                LLog.w(TAG, " mAlbumCursor == null");
                return 0;
            }
        }
        LLog.i(TAG, "----------------getMediaItemCount:" + mAlbumCursor.getCount() + " spend " + (System.currentTimeMillis() - time));
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
        mResolver.delete(mBaseUri, mWhereClause, null);
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
}
