
package com.xjt.letool.metadata.source;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.common.LLog;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.metadata.MediaPath;
import com.xjt.letool.metadata.MediaSet;
import com.xjt.letool.metadata.MediaSetUtils;
import com.xjt.letool.utils.Utils;

import java.util.ArrayList;

public class LocalSimpleAlbumSet {

    private static final String TAG = LocalSimpleAlbumSet.class.getSimpleName();

    public static final String PATH_IMAGE = "/local/image/";
    public static final String PATH_VIDEO = "/local/video/";

    private static final int INDEX_BUCKET_ID = 0;
    private static final int INDEX_BUCKET_NAME = 1;

    private static Uri mBaseUri = null;
    private static final Uri mWatchUriImage = Images.Media.EXTERNAL_CONTENT_URI;
    private static final Uri mWatchUriVideo = Video.Media.EXTERNAL_CONTENT_URI;
    private static final String[] PROJECTION_BUCKET = {
            ImageColumns.BUCKET_ID, ImageColumns.BUCKET_DISPLAY_NAME
    };

    private static final String BUCKET_GROUP_BY = "1) GROUP BY 1,(2";
    private static final String BUCKET_ORDER_BY = "MAX(datetaken) DESC";

    private final LetoolApp mApplication;

    private boolean mIsImage;

    public LocalSimpleAlbumSet(LetoolApp application, boolean isImage) {
        mApplication = application;
        mIsImage = isImage;
        if (isImage) {
            mBaseUri = mWatchUriImage;
        } else {
            mBaseUri = mWatchUriVideo;
        }
    }

    private BucketEntry[] loadBucketEntries() {
        Uri uri = mBaseUri;

        Cursor cursor = mApplication.getContentResolver().query(uri, PROJECTION_BUCKET, BUCKET_GROUP_BY, null, BUCKET_ORDER_BY);
        if (cursor == null) {
            return new BucketEntry[0];
        }
        ArrayList<BucketEntry> buffer = new ArrayList<BucketEntry>();

        try {
            while (cursor.moveToNext()) {
                int bucketId = cursor.getInt(INDEX_BUCKET_ID);
                boolean isCamera = false;
                for (int id : MediaSetUtils.getBucketsIds()) {
                    if (id == bucketId) {
                        isCamera = true;
                        break;
                    }
                }
                if (isCamera)
                    continue;
                BucketEntry entry = new BucketEntry(bucketId, cursor.getString(INDEX_BUCKET_NAME));
                if (!buffer.contains(entry)) {
                    buffer.add(entry);
                }
            }
            LLog.v("DebugLoadingTime", "got " + buffer.size() + " buckets");
        } finally {
            cursor.close();
        }
        return buffer.toArray(new BucketEntry[buffer.size()]);
    }

    public ArrayList<MediaSet> getAllAlbums() {
        BucketEntry[] entries = loadBucketEntries();
        ArrayList<MediaSet> albums = new ArrayList<MediaSet>();
        DataManager dataManager = mApplication.getDataManager();
        for (BucketEntry entry : entries) {
            MediaSet album = getLocalAlbum(dataManager, entry.bucketId, entry.bucketName, mIsImage);
            albums.add(album);
        }
        return albums;
    }

    private MediaSet getLocalAlbum(DataManager manager, int id, String name, boolean mIsImage) {
        synchronized (DataManager.LOCK) {
            if (mIsImage) {
                return new LocalAlbum(new MediaPath(PATH_IMAGE, id), mApplication, new int[] {
                        id
                }, true, name);
            } else {
                return new LocalAlbum(new MediaPath(PATH_VIDEO, id), mApplication, new int[] {
                        id
                }, false, name);

            }
        }
    }

    private static class BucketEntry {

        public String bucketName;
        public int bucketId;

        public BucketEntry(int id, String name) {
            bucketId = id;
            bucketName = Utils.ensureNotNull(name);
        }

        @Override
        public int hashCode() {
            return bucketId;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof BucketEntry))
                return false;
            BucketEntry entry = (BucketEntry) object;
            return bucketId == entry.bucketId;
        }
    }

}
