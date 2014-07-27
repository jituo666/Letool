
package com.xjt.letool.metadata.source;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.common.Future;
import com.xjt.letool.common.FutureListener;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.ThreadPool;
import com.xjt.letool.common.ThreadPool.JobContext;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.metadata.DataNotifier;
import com.xjt.letool.metadata.MediaObject;
import com.xjt.letool.metadata.MediaPath;
import com.xjt.letool.metadata.MediaSet;
import com.xjt.letool.metadata.MediaSetUtils;
import com.xjt.letool.utils.Utils;

import java.util.ArrayList;

public class LocalAlbumSet extends MediaSet implements FutureListener<ArrayList<MediaSet>> {

    public static final String PATH_IMAGE = "/local/image/";
    public static final String PATH_VIDEO = "/local/video/";

    private static final String TAG = LocalAlbumSet.class.getSimpleName();
    // The indices should match the following projections.
    private static final int INDEX_BUCKET_ID = 0;
    private static final int INDEX_BUCKET_NAME = 1;

    private static Uri mBaseUri = null;
    private static final Uri mWatchUriImage = Images.Media.EXTERNAL_CONTENT_URI;
    private static final Uri mWatchUriVideo = Video.Media.EXTERNAL_CONTENT_URI;

    // BUCKET_DISPLAY_NAME is a string like "Camera" which is the directory
    // name of where an image or video is in. BUCKET_ID is a hash of the path
    // name of that directory (see computeBucketValues() in MediaProvider for
    // details). MEDIA_TYPE is video, image, audio, etc.
    //
    // The "albums" are not explicitly recorded in the database, but each image
    // or video has the two columns (BUCKET_ID, MEDIA_TYPE). We define an
    // "album" to be the collection of images/videos which have the same value
    // for the two columns.
    //
    // The goal of the query (used in loadSubMediaSets()) is to find all albums,
    // that is, all unique values for (BUCKET_ID, MEDIA_TYPE). In the meantime
    // sort them by the timestamp of the latest image/video in each of the album.
    //
    // The order of columns below is important: it must match to the index in MediaStore.
    private static final String[] PROJECTION_BUCKET = {
            ImageColumns.BUCKET_ID, ImageColumns.BUCKET_DISPLAY_NAME
    };

    // We want to order the albums by reverse chronological order. We abuse the
    // "WHERE" parameter to insert a "GROUP BY" clause into the SQL statement.
    // The template for "WHERE" parameter is like:
    // SELECT ... FROM ... WHERE (%s)
    // and we make it look like: SELECT ... FROM ... WHERE (1) GROUP BY 1,(2)
    // The "(1)" means true. The "1,(2)" means the first two columns specified
    // after SELECT. Note that because there is a ")" in the template, we use "(2" to match it.
    private static final String BUCKET_GROUP_BY = "1) GROUP BY 1,(2";
    private static final String BUCKET_ORDER_BY = "MAX(datetaken) DESC";

    private final LetoolApp mApplication;
    private ArrayList<MediaSet> mAlbums = new ArrayList<MediaSet>();
    private final DataNotifier mNotifierMedia;
    private final String mName;
    private final Handler mHandler;
    private boolean mIsLoading;

    private Future<ArrayList<MediaSet>> mLoadTask;
    private ArrayList<MediaSet> mLoadBuffer;
    private boolean mIsImage;

    public LocalAlbumSet(MediaPath path, LetoolApp application, boolean isImage) {
        super(path, nextVersionNumber());
        mApplication = application;
        mHandler = new Handler(application.getMainLooper());
        mIsImage = isImage;
        if (isImage) {
            mBaseUri = mWatchUriImage;
            mNotifierMedia = new DataNotifier(this, mWatchUriImage, application);
        } else {
            mBaseUri = mWatchUriVideo;
            mNotifierMedia = new DataNotifier(this, mWatchUriVideo, application);
        }
        mName = "Albums";
    }

    @Override
    public MediaSet getSubMediaSet(int index) {
        return mAlbums.get(index);
    }

    @Override
    public int getSubMediaSetCount() {
        return mAlbums.size();
    }

    @Override
    public String getName() {
        return mName;
    }

    private BucketEntry[] loadBucketEntries(JobContext jc) {
        Uri uri = mBaseUri;

        LLog.v("DebugLoadingTime", "start quering media provider");
        Cursor cursor = mApplication.getContentResolver().query(uri, PROJECTION_BUCKET, BUCKET_GROUP_BY, null, BUCKET_ORDER_BY);
        if (cursor == null) {
            LLog.w(TAG, "cannot open local database: " + uri);
            return new BucketEntry[0];
        }
        ArrayList<BucketEntry> buffer = new ArrayList<BucketEntry>();

        try {
            while (cursor.moveToNext()) {
                int bucketId = cursor.getInt(INDEX_BUCKET_ID);
                boolean isCameraSource = false;
                for (int id : MediaSetUtils.getBucketsIds()) {
                    if (id == bucketId) {
                        isCameraSource = true;
                        break;
                    }
                }
                if (isCameraSource)
                    continue;
                BucketEntry entry = new BucketEntry(bucketId, cursor.getString(INDEX_BUCKET_NAME));
                if (!buffer.contains(entry)) {
                    buffer.add(entry);
                }
                if (jc.isCancelled())
                    return null;
            }
            LLog.v("DebugLoadingTime", "got " + buffer.size() + " buckets");
        } finally {
            cursor.close();
        }
        return buffer.toArray(new BucketEntry[buffer.size()]);
    }

    private static int findBucket(BucketEntry entries[], int bucketId) {
        for (int i = 0, n = entries.length; i < n; ++i) {
            if (entries[i].bucketId == bucketId)
                return i;
        }
        return -1;
    }

    private class AlbumsLoader implements ThreadPool.Job<ArrayList<MediaSet>> {

        @Override
        public ArrayList<MediaSet> run(JobContext jc) {
            // Note: it will be faster if we only select media_type and bucket_id. need to test the performance if that is worth
            BucketEntry[] entries = loadBucketEntries(jc);
            if (jc.isCancelled())
                return null;
            int offset = 0;
            int index = findBucket(entries, MediaSetUtils.DOWNLOAD_BUCKET_ID);
            if (index != -1) {
                circularShiftRight(entries, offset++, index);
            }

            ArrayList<MediaSet> albums = new ArrayList<MediaSet>();
            DataManager dataManager = mApplication.getDataManager();
            for (BucketEntry entry : entries) {
                MediaSet album = getLocalAlbum(dataManager, entry.bucketId, entry.bucketName, mIsImage);
                albums.add(album);
            }
            return albums;
        }
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

    public static String getBucketName(ContentResolver resolver, long bucketId) {
        Uri uri = mBaseUri.buildUpon().appendQueryParameter("limit", "1")
                .build();

        Cursor cursor = resolver.query(uri, PROJECTION_BUCKET, "bucket_id = ?",
                new String[] {
                    String.valueOf(bucketId)
                }, null);

        if (cursor == null) {
            LLog.w(TAG, "query fail: " + uri);
            return "";
        }
        try {
            return cursor.moveToNext() ? cursor.getString(INDEX_BUCKET_NAME) : "";
        } finally {
            cursor.close();
        }
    }

    @Override
    public synchronized boolean isLoading() {
        return mIsLoading;
    }

    public static synchronized long nextVersionNumber() {
        return ++MediaObject.sVersionSerial;
    }

    // synchronized on this function for
    // 1. Prevent calling reload() concurrently.
    // 2. Prevent calling onFutureDone() and reload() concurrently
    @Override
    public synchronized long reload() {
        if (mNotifierMedia.isDirty()) {
            if (mLoadTask != null)
                mLoadTask.cancel();
            mIsLoading = true;
            mLoadTask = mApplication.getThreadPool().submit(new AlbumsLoader(), this);
        }
        if (mLoadBuffer != null) {
            mAlbums = mLoadBuffer;
            mLoadBuffer = null;
            for (MediaSet album : mAlbums) {
                album.reload();
            }
            LLog.i("test-r", "enter reload()-2:" + mDataVersion);
            mDataVersion = nextVersionNumber();
        }
        return mDataVersion;
    }

    @Override
    public synchronized void onFutureDone(Future<ArrayList<MediaSet>> future) {
        if (mLoadTask != future)
            return; // ignore, wait for the latest task
        mLoadBuffer = future.get();
        mIsLoading = false;
        if (mLoadBuffer == null)
            mLoadBuffer = new ArrayList<MediaSet>();
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                notifyContentChanged();
            }
        });
    }

    // For debug only. Fake there is a ContentObserver.onChange() event.
    void fakeChange() {
        mNotifierMedia.fakeChange();
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

    // Circular shift the array range from a[i] to a[j] (inclusive). That is,
    // a[i] -> a[i+1] -> a[i+2] -> ... -> a[j], and a[j] -> a[i]
    private static <T> void circularShiftRight(T[] array, int i, int j) {
        T temp = array[j];
        for (int k = j; k > i; k--) {
            array[k] = array[k - 1];
        }
        array[i] = temp;
    }

    @Override
    public void closeCursor() {
        if (mAlbums != null && mAlbums.size() > 0) {
            for (MediaSet album : mAlbums) {
                album.closeCursor();
            }
        }
    }
}
