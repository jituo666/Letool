
package com.xjt.letool.activities;

import com.xjt.letool.R;
import com.xjt.letool.common.LLog;
import com.xjt.letool.data.DataManager;
import com.xjt.letool.data.MediaSetUtils;
import com.xjt.letool.fragments.ThumbnailFragment;
import com.xjt.letool.utils.Utils;

import android.os.Bundle;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.WindowManager;

/**
 * @Author Jituo.Xuan
 * @Date 9:54:30 AM Apr 19, 2014
 * @Comments:null
 */
public class LetoolMainActivity extends LetoolBaseActivity {

    private static final String TAG = LetoolMainActivity.class.getSimpleName();

    public static final String ACTION_REVIEW = "com.android.camera.action.REVIEW";
    public static final String KEY_DISMISS_KEYGUARD = "dismiss-keyguard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getIntent().getBooleanExtra(KEY_DISMISS_KEYGUARD, false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
        setContentView(R.layout.layout_main);
        super.onCreate(savedInstanceState);
        Fragment fragment = new ThumbnailFragment();
        Bundle data = new Bundle();
        data.putLong(DataManager.KEY_ALBUM_ID, MediaSetUtils.CAMERA_BUCKET_ID);
        data.putString(DataManager.KEY_MEDIA_PATH, getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
        data.putBoolean(DataManager.KEY_IS_CAMERA, true);
        data.putString(DataManager.KEY_ALBUM_TITLE, getString(R.string.common_photo));
        fragment.setArguments(data);
        getSupportFragmentManager().beginTransaction().add(R.id.root_container, fragment, "PhotoFragment").commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    private void initializeByIntent() {

        Intent intent = getIntent();
        String action = intent.getAction();

        if (Intent.ACTION_GET_CONTENT.equalsIgnoreCase(action)) {
            startGetContent(intent);
        } else if (Intent.ACTION_PICK.equalsIgnoreCase(action)) {
            // We do NOT really support the PICK intent. Handle it as
            // the GET_CONTENT. However, we need to translate the type
            // in the intent here.
            LLog.w(TAG, "action PICK is not supported");
            String type = Utils.ensureNotNull(intent.getType());
            if (type.startsWith("vnd.android.cursor.dir/")) {
                if (type.endsWith("/image"))
                    intent.setType("image/*");
                if (type.endsWith("/video"))
                    intent.setType("video/*");
            }
            startGetContent(intent);
        } else if (Intent.ACTION_VIEW.equalsIgnoreCase(action) || ACTION_REVIEW.equalsIgnoreCase(action)) {
            startViewAction(intent);
        } else {
            startDefaultPage();
        }
    }

    public void startDefaultPage() {

        Bundle data = new Bundle();
        data.putString(DataManager.KEY_MEDIA_PATH, getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_SET_ONLY));
        //getPageManager().startState(ThumbnailSetPage.class, data);
    }

    private void startGetContent(Intent intent) {

        Bundle data = intent.getExtras() != null
                ? new Bundle(intent.getExtras())
                : new Bundle();
        /*
         * data.putBoolean(KEY_GET_CONTENT, true); int typeBits =
         * LetoolUtils.determineTypeBits(this, intent);
         * data.putInt(KEY_TYPE_BITS, typeBits);
         * data.putString(AlbumSetPage.KEY_MEDIA_PATH
         * ,getDataManager().getTopSetPath(typeBits));
         */
        //getPageManager().startState(ThumbnailSetPage.class, data);
    }

    /*
     * private String getContentType(Intent intent) { String type =
     * intent.getType(); if (type != null) { return
     * LetoolUtils.MIME_TYPE_PANORAMA360.equals(type) ? MediaItem.MIME_TYPE_JPEG
     * : type; } Uri uri = intent.getData(); try { return
     * getContentResolver().getType(uri); } catch (Throwable t) { LLog.w(TAG,
     * "get type fail", t); return null; } }
     */

    private void startViewAction(Intent intent) {

        /*
         * Boolean slideshow = intent.getBooleanExtra(EXTRA_SLIDESHOW, false);
         * if (slideshow) { getActionBar().hide(); DataManager manager =
         * getDataManager(); Path path = manager.findPathByUri(intent.getData(),
         * intent.getType()); if (path == null || manager.getMediaObject(path)
         * instanceof MediaItem) { path = Path.fromString(
         * manager.getTopSetPath(DataManager.INCLUDE_IMAGE)); } Bundle data =
         * new Bundle(); data.putString(SlideshowPage.KEY_SET_PATH,
         * path.toString()); data.putBoolean(SlideshowPage.KEY_RANDOM_ORDER,
         * true); data.putBoolean(SlideshowPage.KEY_REPEAT, true); if
         * (intent.getBooleanExtra(EXTRA_DREAM, false)) {
         * data.putBoolean(SlideshowPage.KEY_DREAM, true); }
         * getStateManager().startState(SlideshowPage.class, data); } else {
         * Bundle data = new Bundle(); DataManager dm = getDataManager(); Uri
         * uri = intent.getData(); String contentType = getContentType(intent);
         * if (contentType == null) { Toast.makeText(this,
         * R.string.no_such_item, Toast.LENGTH_LONG).show(); finish(); return; }
         * if (uri == null) { int typeBits =
         * GalleryUtils.determineTypeBits(this, intent);
         * data.putInt(KEY_TYPE_BITS, typeBits);
         * data.putString(AlbumSetPage.KEY_MEDIA_PATH,
         * getDataManager().getTopSetPath(typeBits));
         * getStateManager().startState(AlbumSetPage.class, data); } else if
         * (contentType.startsWith( ContentResolver.CURSOR_DIR_BASE_TYPE)) { int
         * mediaType = intent.getIntExtra(KEY_MEDIA_TYPES, 0); if (mediaType !=
         * 0) { uri = uri.buildUpon().appendQueryParameter( KEY_MEDIA_TYPES,
         * String.valueOf(mediaType)) .build(); } Path setPath =
         * dm.findPathByUri(uri, null); MediaSet mediaSet = null; if (setPath !=
         * null) { mediaSet = (MediaSet) dm.getMediaObject(setPath); } if
         * (mediaSet != null) { if (mediaSet.isLeafAlbum()) {
         * data.putString(AlbumPage.KEY_MEDIA_PATH, setPath.toString());
         * data.putString(AlbumPage.KEY_PARENT_MEDIA_PATH,
         * dm.getTopSetPath(DataManager.INCLUDE_ALL));
         * getStateManager().startState(AlbumPage.class, data); } else {
         * data.putString(AlbumSetPage.KEY_MEDIA_PATH, setPath.toString());
         * getStateManager().startState(AlbumSetPage.class, data); } } else {
         * startDefaultPage(); } } else { Path itemPath = dm.findPathByUri(uri,
         * contentType); Path albumPath = dm.getDefaultSetOf(itemPath);
         * data.putString(PhotoPage.KEY_MEDIA_ITEM_PATH, itemPath.toString());
         * data.putBoolean(PhotoPage.KEY_READONLY, true); // TODO: Make the
         * parameter "SingleItemOnly" public so other // activities can
         * reference it. boolean singleItemOnly = (albumPath == null) ||
         * intent.getBooleanExtra("SingleItemOnly", false); if (!singleItemOnly)
         * { data.putString(PhotoPage.KEY_MEDIA_SET_PATH, albumPath.toString());
         * // when FLAG_ACTIVITY_NEW_TASK is set, (e.g. when intent is fired //
         * from notification), back button should behave the same as up button
         * // rather than taking users back to the home screen if
         * (intent.getBooleanExtra(PhotoPage.KEY_TREAT_BACK_AS_UP, false) ||
         * ((intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0)) {
         * data.putBoolean(PhotoPage.KEY_TREAT_BACK_AS_UP, true); } }
         * getStateManager().startState(SinglePhotoPage.class, data); } }
         */
    }
}
