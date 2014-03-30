package com.xjt.letool.data.source;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
import android.util.Log;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.R;
import com.xjt.letool.ThreadPool;
import com.xjt.letool.Future;
import com.xjt.letool.FutureListener;
import com.xjt.letool.ThreadPool.JobContext;
import com.xjt.letool.data.DataManager;
import com.xjt.letool.data.DataNotifier;
import com.xjt.letool.data.MediaObject;
import com.xjt.letool.data.MediaPath;
import com.xjt.letool.data.MediaSet;
import com.xjt.letool.data.MediaSetUtils;
import com.xjt.letool.utils.Utils;

import java.util.ArrayList;

// LocalAlbumSet lists all image or video albums in the local storage.
// The path should be "/local/image", "local/video" or "/local/all"
public class LocalAlbumSet extends MediaSet implements FutureListener<ArrayList<MediaSet>> {
    public static final String PATH_IMAGE = "/local/image/";
    public static final String PATH_VIDEO = "/local/video/";

    private static final String TAG = "LocalAlbumSet";
    private static final String EXTERNAL_MEDIA = "external";

    // The indices should match the following projections.
    private static final int INDEX_BUCKET_ID = 0;
    private static final int INDEX_MEDIA_TYPE = 1;
    private static final int INDEX_BUCKET_NAME = 2;

    private static final Uri mBaseUri = Files.getContentUri(EXTERNAL_MEDIA);
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
    // sort them by the timestamp of the latest image/video in each of the
    // album.
    //
    // The order of columns below is important: it must match to the index in
    // MediaStore.
    private static final String[] PROJECTION_BUCKET = {
            ImageColumns.BUCKET_ID,
            FileColumns.MEDIA_TYPE, ImageColumns.BUCKET_DISPLAY_NAME
    };

    // We want to order the albums by reverse chronological order. We abuse the
    // "WHERE" parameter to insert a "GROUP BY" clause into the SQL statement.
    // The template for "WHERE" parameter is like:
    // SELECT ... FROM ... WHERE (%s)
    // and we make it look like:
    // SELECT ... FROM ... WHERE (1) GROUP BY 1,(2)
    // The "(1)" means true. The "1,(2)" means the first two columns specified
    // after SELECT. Note that because there is a ")" in the template, we use
    // "(2" to match it.
    private static final String BUCKET_GROUP_BY = "1) GROUP BY 1,(2";
    private static final String BUCKET_ORDER_BY = "MAX(datetaken) DESC";

    private final LetoolApp mApplication;
    private final int mType;
    private ArrayList<MediaSet> mAlbums = new ArrayList<MediaSet>();
    private final DataNotifier mNotifierImage;
    private final DataNotifier mNotifierVideo;
    private final String mName;
    private final Handler mHandler;
    private boolean mIsLoading;

    private Future<ArrayList<MediaSet>> mLoadTask;
    private ArrayList<MediaSet> mLoadBuffer;

    public LocalAlbumSet(MediaPath path, LetoolApp application) {
        super(path, nextVersionNumber());
        mApplication = application;
        mHandler = new Handler(application.getMainLooper());
        mType = path.getMediaType();
        mNotifierImage = new DataNotifier(this, mWatchUriImage, application);
        mNotifierVideo = new DataNotifier(this, mWatchUriVideo, application);
        mName = application.getResources().getString(R.string.set_label_local_albums);
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

        Log.v("DebugLoadingTime", "start quering media provider");
        Cursor cursor = mApplication.getContentResolver().query(uri,
                PROJECTION_BUCKET, BUCKET_GROUP_BY, null, BUCKET_ORDER_BY);
        if (cursor == null) {
            Log.w(TAG, "cannot open local database: " + uri);
            return new BucketEntry[0];
        }
        ArrayList<BucketEntry> buffer = new ArrayList<BucketEntry>();
        int typeBits = 0;
        if ((mType & MEDIA_TYPE_IMAGE) != 0) {
            typeBits |= (1 << FileColumns.MEDIA_TYPE_IMAGE);
        }
        if ((mType & MEDIA_TYPE_VIDEO) != 0) {
            typeBits |= (1 << FileColumns.MEDIA_TYPE_VIDEO);
        }
        try {
            while (cursor.moveToNext()) {
                if ((typeBits & (1 << cursor.getInt(INDEX_MEDIA_TYPE))) != 0) {
                    BucketEntry entry = new BucketEntry(
                            cursor.getInt(INDEX_BUCKET_ID),
                            cursor.getString(INDEX_BUCKET_NAME));
                    if (!buffer.contains(entry)) {
                        buffer.add(entry);
                    }
                }
                if (jc.isCancelled())
                    return null;
            }
            Log.v("DebugLoadingTime", "got " + buffer.size() + " buckets");
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
        @SuppressWarnings("unchecked")
        public ArrayList<MediaSet> run(JobContext jc) {
            // Note: it will be faster if we only select media_type and
            // bucket_id.
            // need to test the performance if that is worth
            BucketEntry[] entries = loadBucketEntries(jc);

            if (jc.isCancelled())
                return null;

            int offset = 0;
            // Move camera and download bucket to the front, while keeping the
            // order of others.
            int index = findBucket(entries, MediaSetUtils.CAMERA_BUCKET_ID);
            if (index != -1) {
                circularShiftRight(entries, offset++, index);
            }
            index = findBucket(entries, MediaSetUtils.DOWNLOAD_BUCKET_ID);
            if (index != -1) {
                circularShiftRight(entries, offset++, index);
            }

            ArrayList<MediaSet> albums = new ArrayList<MediaSet>();
            DataManager dataManager = mApplication.getDataManager();
            for (BucketEntry entry : entries) {
                MediaSet album = getLocalAlbum(dataManager, mType, entry.bucketId, entry.bucketName);
                albums.add(album);
            }
            return albums;
        }
    }

    private MediaSet getLocalAlbum(DataManager manager, int type, long id, String name) {
        synchronized (DataManager.LOCK) {
            switch (type) {
                case MEDIA_TYPE_IMAGE:
                    return new LocalAlbum(new MediaPath(PATH_IMAGE, id), mApplication, id, true, name);
                case MEDIA_TYPE_VIDEO:
                    return new LocalAlbum(new MediaPath(PATH_VIDEO, id), mApplication, id, false, name);
            }
            throw new IllegalArgumentException(String.valueOf(type));
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
            Log.w(TAG, "query fail: " + uri);
            return "";
        }
        try {
            return cursor.moveToNext() ? cursor.getString(INDEX_BUCKET_NAME)
                    : "";
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

    @Override
    // synchronized on this function for
    // 1. Prevent calling reload() concurrently.
    // 2. Prevent calling onFutureDone() and reload() concurrently
    public synchronized long reload() {
        // "|" is used instead of "||" because we want to clear both flags.
        if (mNotifierImage.isDirty() | mNotifierVideo.isDirty()) {
            if (mLoadTask != null)
                mLoadTask.cancel();
            mIsLoading = true;
            mLoadTask = mApplication.getThreadPool().submit(new AlbumsLoader(),
                    this);
        }
        if (mLoadBuffer != null) {
            mAlbums = mLoadBuffer;
            mLoadBuffer = null;
            for (MediaSet album : mAlbums) {
                album.reload();
            }
            Log.i("test-r", "enter reload()-2:" + mDataVersion);
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
        mNotifierImage.fakeChange();
        mNotifierVideo.fakeChange();
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
}
