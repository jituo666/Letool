package com.xjt.newpic.fragment;

import java.util.ArrayList;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.xjt.newpic.NpApp;
import com.xjt.newpic.NpContext;
import com.xjt.newpic.R;
import com.xjt.newpic.activities.NpMediaActivity;
import com.xjt.newpic.adapters.FullImageDataAdapter;
import com.xjt.newpic.common.GlobalConstants;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.common.OrientationManager;
import com.xjt.newpic.common.SynchronizedHandler;
import com.xjt.newpic.edit.NpEditActivity;
import com.xjt.newpic.metadata.MediaDetails;
import com.xjt.newpic.metadata.MediaItem;
import com.xjt.newpic.metadata.MediaObject;
import com.xjt.newpic.metadata.MediaPath;
import com.xjt.newpic.metadata.MediaSet;
import com.xjt.newpic.metadata.MediaSetUtils;
import com.xjt.newpic.metadata.source.LocalAlbum;
import com.xjt.newpic.selectors.SelectionManager;
import com.xjt.newpic.share.ShareManager;
import com.xjt.newpic.stat.StatConstants;
import com.xjt.newpic.utils.LetoolUtils;
import com.xjt.newpic.views.DetailsHelper;
import com.xjt.newpic.views.FullImageView;
import com.xjt.newpic.views.GLController;
import com.xjt.newpic.views.GLView;
import com.xjt.newpic.views.NpBottomBar;
import com.xjt.newpic.views.NpDialog;
import com.xjt.newpic.views.NpTopBar;
import com.xjt.newpic.views.SingleDeleteMediaListener;
import com.xjt.newpic.views.DetailsHelper.CloseListener;
import com.xjt.newpic.views.DetailsHelper.DetailsSource;
import com.xjt.newpic.views.NpTopBar.OnActionModeListener;
import com.xjt.newpic.views.SingleDeleteMediaListener.SingleDeleteMediaProgressListener;
import com.xjt.newpic.views.opengl.GLESCanvas;

/**
 * @Author Jituo.Xuan
 * @Date 9:40:15 PM Apr 20, 2014
 * @Comments:null
 */
public class FullImageFragment extends Fragment implements OnActionModeListener, FullImageView.Listener {

    private static final String TAG = FullImageFragment.class.getSimpleName();

    private static final int MSG_HIDE_BARS = 1;
    private static final int MSG_ON_FULL_SCREEN_CHANGED = 4;
    private static final int MSG_UNFREEZE_GLROOT = 6;
    private static final int MSG_REFRESH_BOTTOM_CONTROLS = 8;
    private static final int MSG_ON_UPDATE_TITLE = 10;
    private static final int MSG_REFRESH_IMAGE = 11;
    private static final int MSG_UPDATE_PHOTO_UI = 12;
    private static final int MSG_UPDATE_DEFERRED = 14;

    private static final int UNFREEZE_GLROOT_TIMEOUT = 250;

    public static final String KEY_INDEX_HINT = "index-hint";
    public static final String KEY_OPEN_ANIMATION_RECT = "open-animation-rect";
    public static final String KEY_START_IN_FILMSTRIP = "start-in-filmstrip";
    public static final String KEY_ALBUMPAGE_TRANSITION = "albumpage-transition";

    private SelectionManager mSelectionManager;

    private GLController mGLController;
    private FullImageView mFullImageView;
    private FullImageFragment.Model mModel;
    private DetailsHelper mDetailsHelper;
    private boolean mShowDetails;
    private boolean mIsCameraSource;

    private MediaSet mMediaSet;

    private int mCurrentIndex = 0;
    private boolean mShowBars = true;
    private MediaItem mCurrentPhoto = null;
    private boolean mIsActive;
    private OrientationManager mOrientationManager;
    private boolean mStartInFilmstrip;
    private int mTotalCount = 0;

    private static final long DEFERRED_UPDATE_MS = 0;
    private boolean mDeferredUpdateWaiting = false;
    private long mDeferUpdateUntil = Long.MAX_VALUE;

    private NpContext mLetoolContext;

    private static Handler mHandler;

    public static interface Model extends FullImageView.Model {

        public void resume();

        public void pause();

        public boolean isEmpty();

        public void setCurrentPhoto(MediaPath path, int indexHint);
    }

    private final GLView mRootPane = new GLView() {

        @Override
        protected void onLayout(boolean changed, int left, int top, int right,
                int bottom) {
            mFullImageView.layout(0, 0, right - left, bottom - top);
            if (mShowDetails) {
                mDetailsHelper.layout(left, 0, right, bottom);
            }
        }

        @Override
        protected void render(GLESCanvas canvas) {
            canvas.clearBuffer(LetoolUtils.intColorToFloatARGBArray(getResources().getColor(R.color.gl_background_color)));
            super.render(canvas);
        }
    };

    @Override
    public void onPictureCenter(boolean mIsCameraSource) {
        mFullImageView.setWantPictureCenterCallbacks(false);
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

    private void showBars(boolean withAnim) {
        if (mShowBars)
            return;
        mShowBars = true;
        mLetoolContext.getLetoolTopBar().setVisible(View.VISIBLE, withAnim);
        mLetoolContext.getLetoolBottomBar().setVisible(View.VISIBLE, withAnim);

    }

    private void hideBars(boolean withAnim) {
        if (!mShowBars)
            return;
        mShowBars = false;
        mLetoolContext.getLetoolTopBar().setVisible(View.GONE, withAnim);
        mLetoolContext.getLetoolBottomBar().setVisible(View.GONE, withAnim);
        mHandler.removeMessages(MSG_HIDE_BARS);
    }

    private void toggleBars() {
        if (mShowBars) {
            hideBars(true);
        } else {
            showBars(true);
        }
    }

    private void updateActionBarMessage(final String message) {
        final NpTopBar actionBar = mLetoolContext.getLetoolTopBar();
        actionBar.getActionPanel().post(new Runnable() {

            @Override
            public void run() {
                actionBar.setTitleText(message);
            }
        });
    }

    @Override
    public void onSingleTapConfirmed(int x, int y) {

        MediaItem item = mModel.getMediaItem(0);
        if (item == null) {
            return;
        }
        toggleBars();
    }

    @Override
    public void onFullScreenChanged(boolean full) {
        Message m = mHandler.obtainMessage(MSG_ON_FULL_SCREEN_CHANGED, full ? 1 : 0, 0);
        m.sendToTarget();
    }

    @Override
    public void onCurrentImageUpdated() {
        mGLController.unfreeze();
    }

    @Override
    public void onFilmModeChanged(boolean enabled) {
        if (enabled) {
            mHandler.removeMessages(MSG_HIDE_BARS);
        } else {
        }
    }

    private void overrideTransitionToEditor() {
        getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        //getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void launchPhotoEditor() {
        MediaItem current = mModel.getMediaItem(0);
        if (current == null || (current.getSupportedOperations() & MediaObject.SUPPORT_EDIT) == 0) {
            return;
        }
//        if (Math.min(current.getWidth(), current.getHeight()) < 120 || Math.max(current.getWidth(), current.getHeight()) < 180) {
//            Toast.makeText(getActivity(), R.string.pic_size_tip, Toast.LENGTH_SHORT).show();
//            return;
//        }
        Intent intent = new Intent(NpEditActivity.FILTER_EDIT_ACTION);
        intent.setDataAndType(current.getContentUri(), current.getMimeType()).setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (getActivity().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() == 0) {
            intent.setAction(Intent.ACTION_EDIT);
        }
        getActivity().startActivityForResult(intent, NpMediaActivity.REQUEST_CODE_EDITING);
        overrideTransitionToEditor();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_navi) {
            mLetoolContext.popContentFragment();
        } else if (v.getId() == R.id.action_edit) {

            MobclickAgent.onEvent(getActivity(), StatConstants.EVENT_KEY_EDIT_ENTER);
            launchPhotoEditor();
        } else if (v.getId() == R.id.action_share) {
            MobclickAgent.onEvent(getActivity(), StatConstants.EVENT_KEY_FULL_IMAGE_SHARE);
            ArrayList<Uri> uris = new ArrayList<Uri>();
            uris.add(Uri.parse("file://" + mModel.getMediaItem(0).getFilePath()));
            ShareManager.showAllShareDialog(getActivity(), GlobalConstants.MIMI_TYPE_IMAGE, uris, null);
        } else if (v.getId() == R.id.action_detail) {
            MobclickAgent.onEvent(getActivity(), StatConstants.EVENT_KEY_FULL_IMAGE_DETAIL);
            if (mShowDetails) {
                hideDetails();
            } else {
                showDetails();
            }
        } else if (v.getId() == R.id.action_delete) {
            SingleDeleteMediaListener cdl = new SingleDeleteMediaListener(
                    getActivity(), mCurrentPhoto.getPath(), mLetoolContext.getDataManager(),
                    new SingleDeleteMediaProgressListener() {

                        @Override
                        public void onConfirmDialogDismissed(boolean confirmed) {
                            if (confirmed) {
                                MobclickAgent.onEvent(getActivity(), StatConstants.EVENT_KEY_FULL_IMAGE_DELETE_OK);
                                mTotalCount = mMediaSet.getMediaCount();
                                if (mTotalCount > 0) {
                                    mHandler.sendEmptyMessage(MSG_ON_UPDATE_TITLE);
                                } else {
                                    // not medias
                                    Toast.makeText(getActivity(), R.string.full_image_browse_empty, Toast.LENGTH_SHORT).show();
                                    mLetoolContext.popContentFragment();
                                    return;
                                }
                            }
                        }

                    });

            final NpDialog dlg = new NpDialog(getActivity());
            dlg.setTitle(R.string.common_recommend);
            dlg.setOkBtn(R.string.common_ok, cdl, R.drawable.np_common_pressed_left_bg);
            dlg.setCancelBtn(R.string.common_cancel, cdl, R.drawable.np_common_pressed_right_bg);
            dlg.setMessage(R.string.common_delete_cur_pic_tip);
            dlg.show();
        }
    }

    private void initDataMode() {
        FullImageDataAdapter pda = new FullImageDataAdapter(mLetoolContext, mFullImageView,
                mMediaSet, mCurrentPhoto.getPath(), mCurrentIndex);
        mModel = pda;
        mFullImageView.setModel(mModel);

        pda.setDataListener(new FullImageDataAdapter.DataListener() {

            @Override
            public void onPhotoChanged(int index, MediaItem item) {
                mCurrentIndex = index;
                mHandler.sendEmptyMessage(MSG_ON_UPDATE_TITLE);

            }

            @Override
            public void onLoadingStarted() {
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

        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLetoolContext = (NpContext) getActivity();
        initViews();
        initDatas();

        if (mCurrentPhoto == null) {
            mTotalCount = mMediaSet.updateMediaSet();
            if (mTotalCount > 0) {
                if (mCurrentIndex >= mTotalCount)
                    mCurrentIndex = 0;
                mCurrentPhoto = mMediaSet.getMediaItem(mCurrentIndex, 1).get(0);
            } else {
                return;
            }
        }

        initDataMode();
       
        mFullImageView.setOpenAnimationRect((Rect) getArguments().getParcelable(KEY_OPEN_ANIMATION_RECT));
        mFullImageView.setFilmMode(mStartInFilmstrip && mMediaSet.updateMediaSet() > 1);
    }

    private void initViews() {
        mSelectionManager = new SelectionManager(mLetoolContext, false);
        mFullImageView = new FullImageView(mLetoolContext);
        mFullImageView.setListener(this);
        mRootPane.addComponent(mFullImageView);
        mOrientationManager = mLetoolContext.getOrientationManager();
    }

    private void initDatas() {
        Bundle data = this.getArguments();
        mIsCameraSource = data.getBoolean(NpMediaActivity.KEY_IS_CAMERA_SOURCE);
        if (!mIsCameraSource) {
            String albumTitle = data.getString(NpMediaActivity.KEY_ALBUM_TITLE);
            int albumId = data.getInt(NpMediaActivity.KEY_ALBUM_ID, 0);
            String albumMediaPath = data.getString(NpMediaActivity.KEY_MEDIA_PATH);
            LLog.i(TAG, " photo fragment onCreateView id:" + albumId + " albumTitle:" + albumTitle + " albumMediaPath:" + albumMediaPath + " mIsCameraSource:");
            mMediaSet = mLetoolContext.getDataManager().getMediaSet(new MediaPath(albumMediaPath, albumId));
        } else {
            boolean isImage = mLetoolContext.isImageBrwosing();
            mMediaSet = new LocalAlbum(new MediaPath(data.getString(NpMediaActivity.KEY_MEDIA_PATH), MediaSetUtils.getBucketsIds()[0]),
                    (NpApp) getActivity().getApplication(),
                    MediaSetUtils.getBucketsIds(), isImage, getString(isImage ? R.string.common_picture : R.string.common_video));
        }
        mStartInFilmstrip = data.getBoolean(KEY_START_IN_FILMSTRIP, false);
        mCurrentIndex = data.getInt(KEY_INDEX_HINT, 0);
        mSelectionManager.setSourceMediaSet(mMediaSet);
    }

    private void initBars() {
        NpTopBar topBar = mLetoolContext.getLetoolTopBar();
        topBar.setOnActionMode(NpTopBar.ACTION_BAR_MODE_FULL_IMAGE, this);
        topBar.setTitleIcon(R.drawable.ic_action_previous_item);
        topBar.setVisible(View.VISIBLE, false);
        ViewGroup nativeButtons = (ViewGroup) topBar.getActionPanel().findViewById(R.id.navi_buttons);
        nativeButtons.setVisibility(View.GONE);
        //
        NpBottomBar bottomBar = mLetoolContext.getLetoolBottomBar();
        bottomBar.setOnActionMode(NpBottomBar.BOTTOM_BAR_MODE_FULL_IMAGE, this);
        bottomBar.setVisible(View.VISIBLE, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initBars();
        mGLController = mLetoolContext.getGLController();
        mGLController.setOrientationSource(mOrientationManager);
        mHandler = new SynchronizedHandler(mGLController) {

            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_HIDE_BARS: {
                        hideBars(true);
                        break;
                    }
                    case MSG_REFRESH_BOTTOM_CONTROLS: {
                        break;
                    }
                    case MSG_ON_FULL_SCREEN_CHANGED: {
                        break;
                    }
                    case MSG_UNFREEZE_GLROOT: {
                        mGLController.unfreeze();
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
                    case MSG_ON_UPDATE_TITLE: {
                        if (isAdded())
                            updateActionBarMessage(getString(R.string.full_image_browse, Math.min(mCurrentIndex + 1, mTotalCount), mTotalCount));
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
                    default:
                        throw new AssertionError(message.what);
                }
            }
        };
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
        mGLController.onResume();
        if (mModel == null) {
            return;
        }
        mGLController.lockRenderThread();
        try {

            LLog.i(TAG, "               -------onResume:" + System.currentTimeMillis());
            if (mLetoolContext.isAlbumDirty()) {
                mCurrentIndex = 0;
                initDataMode();
                mLetoolContext.setAlbumDirty(false);
            }
            mGLController.freeze();
            mIsActive = true;
            mGLController.setContentPane(mRootPane);
            mModel.resume();
            mFullImageView.resume();
            if (!mShowBars) {
                mGLController.setLightsOutMode(true);
            }
            mHandler.sendEmptyMessageDelayed(MSG_UNFREEZE_GLROOT, UNFREEZE_GLROOT_TIMEOUT);
        } finally {
            mGLController.unlockRenderThread();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
        mGLController.onPause();
        mGLController.lockRenderThread();
        try {
            mIsActive = false;
            mGLController.unfreeze();
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
        } finally {
            mGLController.unlockRenderThread();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGLController.setOrientationSource(null);
        mHandler.removeCallbacksAndMessages(null); // Remove all pending messages.

    }

    // ////////////////////////////////////////////////////////[detail]/////////////////////////////////////////////////////////////////
    private void showDetails() {
        mShowDetails = true;
        if (mDetailsHelper == null) {
            mDetailsHelper = new DetailsHelper(mLetoolContext, mRootPane, new MyDetailsSource());
            mDetailsHelper.setCloseListener(new CloseListener() {

                @Override
                public void onClose() {
                    hideDetails();
                }
            });
        }
        mDetailsHelper.show();
    }

    private void hideDetails() {
        mShowDetails = false;
        mDetailsHelper.hide();
    }

    private class MyDetailsSource implements DetailsSource {

        @Override
        public MediaDetails getDetails() {
            return mModel.getMediaItem(0).getDetails();
        }

        @Override
        public int size() {
            return mMediaSet != null ? mMediaSet.updateMediaSet() : 1;
        }

        @Override
        public int setIndex() {
            return mModel.getCurrentIndex();
        }
    }

}
