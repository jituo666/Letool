
package com.xjt.letool.views.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;

import android.content.res.Configuration;
import android.graphics.Color;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.R;
import com.xjt.letool.data.MediaDetails;
import com.xjt.letool.data.MediaItem;
import com.xjt.letool.data.MediaObject;
import com.xjt.letool.data.MediaPath;
import com.xjt.letool.data.MediaSet;
import com.xjt.letool.selectors.SelectionManager;
import com.xjt.letool.utils.LetoolUtils;
import com.xjt.letool.utils.UsageStatistics;
import com.xjt.letool.view.DetailsHelper;
import com.xjt.letool.view.FullImageView;
import com.xjt.letool.view.GLBaseView;
import com.xjt.letool.view.GLController;
import com.xjt.letool.view.GLRootView;
import com.xjt.letool.view.DetailsHelper.DetailsSource;
import com.xjt.letool.views.opengl.GLESCanvas;

import com.xjt.letool.activities.BaseActivity;
import com.xjt.letool.adapters.PhotoDataAdapter;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.OrientationManager;
import com.xjt.letool.common.SynchronizedHandler;

/**
 * @Author Jituo.Xuan
 * @Date 9:40:15 PM Apr 20, 2014
 * @Comments:null
 */
public class FullImageFragment extends LetoolFragment implements FullImageView.Listener {

    private static final String TAG = "PhotoPage";

    private static final int MSG_HIDE_BARS = 1;
    private static final int MSG_ON_FULL_SCREEN_CHANGED = 4;
    private static final int MSG_UPDATE_ACTION_BAR = 5;
    private static final int MSG_UNFREEZE_GLROOT = 6;
    private static final int MSG_WANT_BARS = 7;
    private static final int MSG_REFRESH_BOTTOM_CONTROLS = 8;
    private static final int MSG_ON_CAMERA_CENTER = 9;
    private static final int MSG_ON_PICTURE_CENTER = 10;
    private static final int MSG_REFRESH_IMAGE = 11;
    private static final int MSG_UPDATE_PHOTO_UI = 12;
    private static final int MSG_UPDATE_DEFERRED = 14;
    private static final int MSG_UPDATE_SHARE_URI = 15;
    private static final int MSG_UPDATE_PANORAMA_UI = 16;

    private static final int HIDE_BARS_TIMEOUT = 3500;
    private static final int UNFREEZE_GLROOT_TIMEOUT = 250;

    public static final String KEY_MEDIA_SET_PATH = "media-set-path";
    public static final String KEY_MEDIA_ITEM_PATH = "media-item-path";
    public static final String KEY_INDEX_HINT = "index-hint";
    public static final String KEY_OPEN_ANIMATION_RECT = "open-animation-rect";
    public static final String KEY_APP_BRIDGE = "app-bridge";
    public static final String KEY_TREAT_BACK_AS_UP = "treat-back-as-up";
    public static final String KEY_START_IN_FILMSTRIP = "start-in-filmstrip";
    public static final String KEY_RETURN_INDEX_HINT = "return-index-hint";
    public static final String KEY_SHOW_WHEN_LOCKED = "show_when_locked";
    public static final String KEY_IN_CAMERA_ROLL = "in_camera_roll";
    public static final String KEY_READONLY = "read-only";

    public static final String KEY_ALBUMPAGE_TRANSITION = "albumpage-transition";
    public static final int MSG_ALBUMPAGE_NONE = 0;
    public static final int MSG_ALBUMPAGE_STARTED = 1;
    public static final int MSG_ALBUMPAGE_RESUMED = 2;
    public static final int MSG_ALBUMPAGE_PICKED = 4;

    public static final String ACTION_NEXTGEN_EDIT = "action_nextgen_edit";
    public static final String ACTION_SIMPLE_EDIT = "action_simple_edit";

    private GLRootView mGLRootView;

    private SelectionManager mSelectionManager;

    private FullImageView mFullImageView;
    private FullImageFragment.Model mModel;
    private DetailsHelper mDetailsHelper;
    private boolean mShowDetails;

    // mMediaSet could be null if there is no KEY_MEDIA_SET_PATH supplied. E.g., viewing a photo in gmail attachment
    private MediaSet mMediaSet;

    private int mCurrentIndex = 0;
    private Handler mHandler;
    private boolean mShowBars = true;
    private volatile boolean mActionBarAllowed = true;
    private boolean mIsMenuVisible;
    private MediaItem mCurrentPhoto = null;
    private boolean mIsActive;
    private String mSetPathString;
    private OrientationManager mOrientationManager;
    private boolean mTreatBackAsUp;
    private boolean mStartInFilmstrip;
    private boolean mHasCameraScreennailOrPlaceholder = false;

    private boolean mSkipUpdateCurrentPhoto = false;
    private static final long CAMERA_SWITCH_CUTOFF_THRESHOLD_MS = 300;

    private static final long DEFERRED_UPDATE_MS = 250;
    private boolean mDeferredUpdateWaiting = false;
    private long mDeferUpdateUntil = Long.MAX_VALUE;

    // The item that is deleted (but it can still be undeleted before commiting)
    private MediaPath mDeletePath;
    private boolean mDeleteIsFocus;  // whether the deleted item was in focus

    private Uri[] mNfcPushUris = new Uri[1];

    private BaseActivity mActivity;

    public static interface Model extends FullImageView.Model {

        public void resume();

        public void pause();

        public boolean isEmpty();

        public void setCurrentPhoto(MediaPath path, int indexHint);
    }

    private final GLBaseView mRootPane = new GLBaseView() {

        @Override
        protected void onLayout(
                boolean changed, int left, int top, int right, int bottom) {
            mFullImageView.layout(0, 0, right - left, bottom - top);
            if (mShowDetails) {
                mDetailsHelper.layout(left, 0, right, bottom);
            }
        }

        @Override
        protected void render(GLESCanvas canvas) {
            canvas.clearBuffer(LetoolUtils.intColorToFloatARGBArray(Color.BLACK));
            super.render(canvas);
        }
    };

    @Override
    public void onPictureCenter(boolean isCamera) {
        isCamera = isCamera || (mHasCameraScreennailOrPlaceholder);
        mFullImageView.setWantPictureCenterCallbacks(false);
        mHandler.removeMessages(MSG_ON_CAMERA_CENTER);
        mHandler.removeMessages(MSG_ON_PICTURE_CENTER);
        mHandler.sendEmptyMessage(isCamera ? MSG_ON_CAMERA_CENTER : MSG_ON_PICTURE_CENTER);
    }

    private void requestDeferredUpdate() {
        mDeferUpdateUntil = SystemClock.uptimeMillis() + DEFERRED_UPDATE_MS;
        if (!mDeferredUpdateWaiting) {
            mDeferredUpdateWaiting = true;
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_DEFERRED, DEFERRED_UPDATE_MS);
        }
    }

    private void updateUIForCurrentPhoto() {
        if (mCurrentPhoto == null)
            return;
        if (mShowDetails) {
            mDetailsHelper.reloadDetails();
        }

    }

    private void updateCurrentPhoto(MediaItem photo) {
        if (mCurrentPhoto == photo)
            return;
        mCurrentPhoto = photo;
        if (mFullImageView.getFilmMode()) {
            requestDeferredUpdate();
        } else {
            updateUIForCurrentPhoto();
        }
    }

    //////////////////////////////////////////////////////////////////////////
    //  Action Bar show/hide management
    //////////////////////////////////////////////////////////////////////////

    private void showBars() {
        if (mShowBars)
            return;
        mShowBars = true;
        mOrientationManager.unlockOrientation();
        getGLController().setLightsOutMode(false);
        refreshHidingMessage();
    }

    private void hideBars() {
        if (!mShowBars)
            return;
        mShowBars = false;
        getGLController().setLightsOutMode(true);
        mHandler.removeMessages(MSG_HIDE_BARS);
    }

    private void refreshHidingMessage() {
        mHandler.removeMessages(MSG_HIDE_BARS);
        if (!mIsMenuVisible && !mFullImageView.getFilmMode()) {
            mHandler.sendEmptyMessageDelayed(MSG_HIDE_BARS, HIDE_BARS_TIMEOUT);
        }
    }

    private boolean canShowBars() {
        // No bars if we are showing camera preview.
        if (mCurrentIndex == 0
                && !mFullImageView.getFilmMode())
            return false;

        // No bars if it's not allowed.
        if (!mActionBarAllowed)
            return false;

        Configuration config = getResources().getConfiguration();
        if (config.touchscreen == Configuration.TOUCHSCREEN_NOTOUCH) {
            return false;
        }

        return true;
    }

    private void wantBars() {
        if (canShowBars())
            showBars();
    }

    private void toggleBars() {
        if (mShowBars) {
            hideBars();
        } else {
            if (canShowBars())
                showBars();
        }
    }

    private void updateBars() {
        if (!canShowBars()) {
            hideBars();
        }
    }

    private void hideDetails() {
        mShowDetails = false;
        mDetailsHelper.hide();
    }

    ////////////////////////////////////////////////////////////////////////////
    //  Callbacks from PhotoView
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void onSingleTapUp(int x, int y) {

        MediaItem item = mModel.getMediaItem(0);
        if (item == null) {
            // item is not ready or it is camera preview, ignore
            return;
        }

        int supported = item.getSupportedOperations();
        boolean playVideo = ((supported & MediaItem.SUPPORT_PLAY) != 0);

        if (playVideo) {
            // determine if the point is at center (1/6) of the photo view.
            // (The position of the "play" icon is at center (1/6) of the photo)
            int w = mFullImageView.getWidth();
            int h = mFullImageView.getHeight();
            playVideo = (Math.abs(x - w / 2) * 12 <= w)
                    && (Math.abs(y - h / 2) * 12 <= h);
        }
        /*
                if (playVideo) {
                    getActivity().finish();
                } else if (goBack) {
                    onBackPressed();
                } else if (unlock) {
                    Intent intent = new Intent(mActivity, GalleryActivity.class);
                    intent.putExtra(GalleryActivity.KEY_DISMISS_KEYGUARD, true);
                    mActivity.startActivity(intent);
                } else if (launchCamera) {
                    launchCamera();
                } else {
                    toggleBars();
                }*/
    }

    @Override
    public void onActionBarAllowed(boolean allowed) {
        mActionBarAllowed = allowed;
        mHandler.sendEmptyMessage(MSG_UPDATE_ACTION_BAR);
    }

    @Override
    public void onActionBarWanted() {
        mHandler.sendEmptyMessage(MSG_WANT_BARS);
    }

    @Override
    public void onFullScreenChanged(boolean full) {
        Message m = mHandler.obtainMessage(
                MSG_ON_FULL_SCREEN_CHANGED, full ? 1 : 0, 0);
        m.sendToTarget();
    }

    // How we do delete/undo:
    //
    // When the user choose to delete a media item, we just tell the
    // FilterDeleteSet to hide that item. If the user choose to undo it, we
    // again tell FilterDeleteSet not to hide it. If the user choose to commit
    // the deletion, we then actually delete the media item.
    @Override
    public void onDeleteImage(MediaPath path, int offset) {
        onCommitDeleteImage();  // commit the previous deletion
        mDeletePath = path;
        mDeleteIsFocus = (offset == 0);
    }

    @Override
    public void onUndoDeleteImage() {
        if (mDeletePath == null)
            return;
        // If the deletion was done on the focused item, we want the model to
        // focus on it when it is undeleted.
        if (mDeleteIsFocus)
            mModel.setFocusHintPath(mDeletePath);

        mDeletePath = null;
    }

    @Override
    public void onCommitDeleteImage() {
        if (mDeletePath == null)
            return;
        mDeletePath = null;
    }

    public void playVideo(Activity activity, Uri uri, String title) {
        try {

        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, activity.getString(R.string.video_err),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mGLRootView.onPause();
        mGLRootView.lockRenderThread();
        try {
            mIsActive = false;
            getGLController().unfreeze();
            mHandler.removeMessages(MSG_UNFREEZE_GLROOT);

            DetailsHelper.pause();
            // Hide the detail dialog on exit
            if (mShowDetails)
                hideDetails();
            if (mModel != null) {
                mModel.pause();
            }
            mFullImageView.pause();
            mHandler.removeMessages(MSG_HIDE_BARS);
            mHandler.removeMessages(MSG_REFRESH_BOTTOM_CONTROLS);
            onCommitDeleteImage();
        } finally {
            mGLRootView.unlockRenderThread();
        }
    }

    @Override
    public void onCurrentImageUpdated() {
        getGLController().unfreeze();
    }

    @Override
    public void onFilmModeChanged(boolean enabled) {

        if (enabled) {
            mHandler.removeMessages(MSG_HIDE_BARS);
            UsageStatistics.onContentViewChanged(
                    UsageStatistics.COMPONENT_GALLERY, "FilmstripPage");
        } else {
            refreshHidingMessage();
            if (mCurrentIndex > 0) {
                UsageStatistics.onContentViewChanged(
                        UsageStatistics.COMPONENT_GALLERY, "SinglePhotoPage");
            } else {
                UsageStatistics.onContentViewChanged(
                        UsageStatistics.COMPONENT_CAMERA, "Unknown"); // TODO
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mGLRootView.onResume();
        mGLRootView.lockRenderThread();
        try {
            mGLRootView.freeze();
            mIsActive = true;

            mGLRootView.setContentPane(mRootPane);
            mModel.resume();
            mFullImageView.resume();

            if (!mShowBars) {
                mGLRootView.setLightsOutMode(true);
            }

            mHandler.sendEmptyMessageDelayed(MSG_UNFREEZE_GLROOT, UNFREEZE_GLROOT_TIMEOUT);
        } finally {
            mGLRootView.unlockRenderThread();
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mActivity = (BaseActivity) this.getActivity();
        super.onCreate(savedInstanceState);
    }

    private void initViews() {
        mSelectionManager = new SelectionManager(mActivity, false);
        mFullImageView = new FullImageView(this);
        mFullImageView.setListener(this);
        mFullImageView.setBackgroundColor(LetoolUtils.intColorToFloatARGBArray(getResources().getColor(R.color.default_background_thumbnail)));
        mRootPane.addComponent(mFullImageView);
        mOrientationManager = mActivity.getOrientationManager();
        getGLController().setOrientationSource(mOrientationManager);
    }

    private void initDatas() {
        Bundle data = this.getArguments();
        String albumTitle = data.getString(BaseActivity.KEY_ALBUM_TITLE);
        long albumId = data.getLong(BaseActivity.KEY_ALBUM_ID, 0);
        String albumMediaPath = data.getString(BaseActivity.KEY_MEDIA_PATH);
        LLog.i(TAG, " photo fragment onCreateView id:" + albumId + " albumTitle:" + albumTitle + " albumMediaPath:" + albumMediaPath + " isCamera:");
        mMediaSet = getDataManager().getMediaSet(new MediaPath(albumMediaPath, albumId));
        mTreatBackAsUp = data.getBoolean(KEY_TREAT_BACK_AS_UP, false);
        mStartInFilmstrip = data.getBoolean(KEY_START_IN_FILMSTRIP, false);
        mCurrentIndex = data.getInt(KEY_INDEX_HINT, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.gl_root_view, container, false);
        mGLRootView = (GLRootView) rootView.findViewById(R.id.gl_root_view);

        initViews();
        initDatas();
        mHandler = new SynchronizedHandler(getGLController()) {

            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_HIDE_BARS: {
                        hideBars();
                        break;
                    }
                    case MSG_REFRESH_BOTTOM_CONTROLS: {
                        break;
                    }
                    case MSG_ON_FULL_SCREEN_CHANGED: {
                        break;
                    }
                    case MSG_UPDATE_ACTION_BAR: {
                        updateBars();
                        break;
                    }
                    case MSG_WANT_BARS: {
                        wantBars();
                        break;
                    }
                    case MSG_UNFREEZE_GLROOT: {
                        getGLController().unfreeze();
                        break;
                    }
                    case MSG_UPDATE_DEFERRED: {
                        long nextUpdate = mDeferUpdateUntil - SystemClock.uptimeMillis();
                        if (nextUpdate <= 0) {
                            mDeferredUpdateWaiting = false;
                            updateUIForCurrentPhoto();
                        } else {
                            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_DEFERRED, nextUpdate);
                        }
                        break;
                    }
                    case MSG_ON_CAMERA_CENTER: {
                        break;
                    }
                    case MSG_ON_PICTURE_CENTER: {
                        break;
                    }
                    case MSG_REFRESH_IMAGE: {
                        final MediaItem photo = mCurrentPhoto;
                        mCurrentPhoto = null;
                        updateCurrentPhoto(photo);
                        break;
                    }
                    case MSG_UPDATE_PHOTO_UI: {
                        updateUIForCurrentPhoto();
                        break;
                    }
                    case MSG_UPDATE_SHARE_URI: {

                        break;
                    }
                    case MSG_UPDATE_PANORAMA_UI: {

                        break;
                    }
                    default:
                        throw new AssertionError(message.what);
                }
            }
        };

        mSelectionManager.setSourceMediaSet(mMediaSet);
        MediaPath itemPath = null;
        int mediaItemCount = mMediaSet.getMediaItemCount();
        if (mediaItemCount > 0) {
            if (mCurrentIndex >= mediaItemCount)
                mCurrentIndex = 0;
            itemPath = mMediaSet.getMediaItem(mCurrentIndex, 1).get(0).getPath();
        }
        PhotoDataAdapter pda = new PhotoDataAdapter(this, mFullImageView, mMediaSet, itemPath, mCurrentIndex, 0, false, false);
        mModel = pda;
        mFullImageView.setModel(mModel);

        pda.setDataListener(new PhotoDataAdapter.DataListener() {

            @Override
            public void onPhotoChanged(int index, MediaPath item) {
                int oldIndex = mCurrentIndex;
                mCurrentIndex = index;
                if (mHasCameraScreennailOrPlaceholder) {
                    if (mCurrentIndex > 0) {
                        mSkipUpdateCurrentPhoto = false;
                    }
                    if (oldIndex == 0 && mCurrentIndex > 0
                            && !mFullImageView.getFilmMode()) {
                        mFullImageView.setFilmMode(true);
                    } else if (oldIndex == 2 && mCurrentIndex == 1) {
                        mFullImageView.stopScrolling();
                    } else if (oldIndex >= 1 && mCurrentIndex == 0) {
                        mFullImageView.setWantPictureCenterCallbacks(true);
                        mSkipUpdateCurrentPhoto = true;
                    }
                }
                if (!mSkipUpdateCurrentPhoto) {
                    if (item != null) {
                        MediaItem photo = mModel.getMediaItem(0);
                        if (photo != null)
                            updateCurrentPhoto(photo);
                    }
                    updateBars();
                }
                refreshHidingMessage();
            }

            @Override
            public void onLoadingFinished(boolean loadingFailed) {
                if (!mModel.isEmpty()) {
                    MediaItem photo = mModel.getMediaItem(0);
                    if (photo != null)
                        updateCurrentPhoto(photo);
                } else if (mIsActive) {

                    getActivity().finish();
                }
            }

            @Override
            public void onLoadingStarted() {
            }
        });
        //} else {
        // Get default media set by the URI
        /*            MediaItem mediaItem = (MediaItem) getDataManager().getMediaObject(itemPath);
                    mModel = new SinglePhotoDataAdapter(mActivity, mFullImageView, mediaItem);
                    mFullImageView.setModel(mModel);
                    updateCurrentPhoto(mediaItem);
                    mShowSpinner = false;*/
        //}
        mFullImageView.setFilmMode(mStartInFilmstrip && mMediaSet.getMediaItemCount() > 1);
        return rootView;
    }

    @Override
    public void onDestroy() {

        getGLController().setOrientationSource(null);
        // Remove all pending messages.
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private class MyDetailsSource implements DetailsSource {

        @Override
        public MediaDetails getDetails() {
            return mModel.getMediaItem(0).getDetails();
        }

        @Override
        public int size() {
            return mMediaSet != null ? mMediaSet.getMediaItemCount() : 1;
        }

        @Override
        public int setIndex() {
            return mModel.getCurrentIndex();
        }
    }

    private static String getMediaTypeString(MediaItem item) {
        if (item.getMediaType() == MediaObject.MEDIA_TYPE_VIDEO) {
            return "Video";
        } else if (item.getMediaType() == MediaObject.MEDIA_TYPE_IMAGE) {
            return "Photo";
        } else {
            return "Unknown:" + item.getMediaType();
        }
    }

    @Override
    public GLController getGLController() {
        return mGLRootView;
    }

}
