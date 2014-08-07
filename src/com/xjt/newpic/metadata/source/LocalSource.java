
package com.xjt.newpic.metadata.source;

import android.content.ContentProviderClient;
import android.content.UriMatcher;
import android.provider.MediaStore;

import com.xjt.newpic.LetoolApp;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.metadata.MediaObject;
import com.xjt.newpic.metadata.MediaPath;
import com.xjt.newpic.metadata.MediaSource;
import com.xjt.newpic.metadata.PathMatcher;
import com.xjt.newpic.metadata.image.LocalImage;
import com.xjt.newpic.metadata.video.LocalVideo;

public class LocalSource extends MediaSource {

    public static final String KEY_BUCKET_ID = "bucketId";

    private LetoolApp mApplication;
    private PathMatcher mMatcher;
    private static final int NO_MATCH = -1;
    private final UriMatcher mUriMatcher = new UriMatcher(NO_MATCH);
    private static final int LOCAL_IMAGE_ALBUMSET = 0;
    private static final int LOCAL_VIDEO_ALBUMSET = 1;
    private static final int LOCAL_IMAGE_ALBUM = 2;
    private static final int LOCAL_VIDEO_ALBUM = 3;
    private static final int LOCAL_IMAGE_ITEM = 4;
    private static final int LOCAL_VIDEO_ITEM = 5;
    private static final int LOCAL_ALL_ALBUMSET = 6;
    private static final int LOCAL_ALL_ALBUM = 7;

    private static final String TAG = "LocalSource";

    private ContentProviderClient mClient;

    public LocalSource(LetoolApp context) {
        super("local");
        mApplication = context;
        mMatcher = new PathMatcher();
        //set
        mMatcher.add("/local/image", LOCAL_IMAGE_ALBUMSET);
        mMatcher.add("/local/video", LOCAL_VIDEO_ALBUMSET);
        mMatcher.add("/local/all", LOCAL_ALL_ALBUMSET);

        //album
        mMatcher.add("/local/image/*", LOCAL_IMAGE_ALBUM);
        mMatcher.add("/local/video/*", LOCAL_VIDEO_ALBUM);
        mMatcher.add("/local/all/*", LOCAL_ALL_ALBUM);
        mMatcher.add("/local/image/item/*", LOCAL_IMAGE_ITEM);
        mMatcher.add("/local/video/item/*", LOCAL_VIDEO_ITEM);

        //item
        mUriMatcher.addURI(MediaStore.AUTHORITY, "external/images/media/#", LOCAL_IMAGE_ITEM);
        mUriMatcher.addURI(MediaStore.AUTHORITY, "external/video/media/#", LOCAL_VIDEO_ITEM);
        mUriMatcher.addURI(MediaStore.AUTHORITY, "external/images/media", LOCAL_IMAGE_ALBUM);
        mUriMatcher.addURI(MediaStore.AUTHORITY, "external/video/media", LOCAL_VIDEO_ALBUM);
        mUriMatcher.addURI(MediaStore.AUTHORITY, "external/file", LOCAL_ALL_ALBUM);
    }

    @Override
    public MediaObject createMediaObject(MediaPath path) {
        LetoolApp app = mApplication;
        LLog.i(TAG, " createMediaObject prefix:" + path.getPrefix() + " : " + mMatcher.match(path.getPrefix()));
        switch (mMatcher.match(path.getPrefix())) {
            case LOCAL_ALL_ALBUMSET:
            case LOCAL_IMAGE_ALBUMSET:
            	return new LocalAlbumSet(path, mApplication, true);
            case LOCAL_VIDEO_ALBUMSET:
                return new LocalAlbumSet(path, mApplication, false);
            case LOCAL_IMAGE_ALBUM:
                return new LocalAlbum(path, app, path.getIdentity(), true);
            case LOCAL_VIDEO_ALBUM:
                return new LocalAlbum(path, app, path.getIdentity(), false);
            case LOCAL_IMAGE_ITEM:
                return new LocalImage(path, mApplication, path.getIdentity());
            case LOCAL_VIDEO_ITEM:
                return new LocalVideo(path, mApplication, path.getIdentity());
            default:
                throw new RuntimeException("bad path: " + path);
        }
    }

    @Override
    public void resume() {
        mClient = mApplication.getContentResolver().acquireContentProviderClient(MediaStore.AUTHORITY);
    }

    @Override
    public void pause() {
        mClient.release();
        mClient = null;
    }
}
