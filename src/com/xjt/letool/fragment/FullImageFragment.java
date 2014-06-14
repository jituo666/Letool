
package com.xjt.letool.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;

import android.graphics.Color;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.xjt.letool.LetoolApp;
import com.xjt.letool.R;
import com.xjt.letool.metadata.MediaDetails;
import com.xjt.letool.metadata.MediaItem;
import com.xjt.letool.metadata.MediaPath;
import com.xjt.letool.metadata.MediaSet;
import com.xjt.letool.metadata.MediaSetUtils;
import com.xjt.letool.metadata.source.LocalAlbum;
import com.xjt.letool.selectors.ContractSelector;
import com.xjt.letool.share.ShareManager;
import com.xjt.letool.share.ShareManager.ShareTo;
import com.xjt.letool.stat.StatConstants;
import com.xjt.letool.utils.LetoolUtils;
import com.xjt.letool.view.LetoolDialog;
import com.xjt.letool.view.DetailsHelper;
import com.xjt.letool.view.SingleDeleteMediaListener.DeleteMediaProgressListener;
import com.xjt.letool.view.DetailsHelper.CloseListener;
import com.xjt.letool.view.FullImageView;
import com.xjt.letool.view.GLBaseView;
import com.xjt.letool.view.GLController;
import com.xjt.letool.view.GLRootView;
import com.xjt.letool.view.LetoolTopBar;
import com.xjt.letool.view.DetailsHelper.DetailsSource;
import com.xjt.letool.view.LetoolBottomBar;
import com.xjt.letool.view.SingleDeleteMediaListener;
import com.xjt.letool.views.opengl.GLESCanvas;

import com.xjt.letool.activities.BaseFragmentActivity;
import com.xjt.letool.activities.ThumbnailActivity;
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

    private static final String TAG = FullImageFragment.class.getSimpleName();

    private static final int MSG_HIDE_BARS = 1;
    private static final int MSG_ON_FULL_SCREEN_CHANGED = 4;
    private static final int MSG_UNFREEZE_GLROOT = 6;
    private static final int MSG_REFRESH_BOTTOM_CONTROLS = 8;
    private static final int MSG_ON_CAMERA_CENTER = 9;
    private static final int MSG_ON_PICTURE_CENTER = 10;
    private static final int MSG_REFRESH_IMAGE = 11;
    private static final int MSG_UPDATE_PHOTO_UI = 12;
    private static final int MSG_UPDATE_DEFERRED = 14;
    private static final int MSG_UPDATE_SHARE_URI = 15;
    private static final int MSG_UPDATE_PANORAMA_UI = 16;

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

    private ContractSelector mSelectionManager;

    private FullImageView mFullImageView;
    private FullImageFragment.Model mModel;
    private DetailsHelper mDetailsHelper;
    private boolean mShowDetails;

    private MediaSet mMediaSet;

    private int mCurrentIndex = 0;
    private boolean mShowBars = true;
    private MediaItem mCurrentPhoto = null;
    private boolean mIsActive;
    private OrientationManager mOrientationManager;
    private boolean mStartInFilmstrip;
    private int mTotalCount = 0;

    private static final long DEFERRED_UPDATE_MS = 250;
    private boolean mDeferredUpdateWaiting = false;
    private long mDeferUpdateUntil = Long.MAX_VALUE;

    private BaseFragmentActivity mActivity;

    private Handler mHandler;

    public static interface Model extends FullImageView.Model {

        public void resume();

        public void pause();

        public boolean isEmpty();

        public void setCurrentPhoto(MediaPath path, int indexHint);
    }

    private final GLBaseView mRootPane = new GLBaseView() {

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
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

    private void showBars(boolean withAnim) {
        if (mShowBars)
            return;
        mShowBars = true;
        mOrientationManager.unlockOrientation();
        getLetoolTopBar().setVisible(View.VISIBLE, withAnim);
        getLetoolBottomBar().setVisible(View.VISIBLE, withAnim);

    }

    private void hideBars(boolean withAnim) {
        if (!mShowBars)
            return;
        mShowBars = false;
        getLetoolTopBar().setVisible(View.GONE, withAnim);
        getLetoolBottomBar().setVisible(View.GONE, withAnim);
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
        final LetoolTopBar actionBar = getLetoolTopBar();
        actionBar.getActionPanel().post(new Runnable() {

            @Override
            public void run() {
                actionBar.setTitleText(message);
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////
    //  Callbacks from FullImageView
    ////////////////////////////////////////////////////////////////////////////
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
        mGLRootView.unfreeze();
    }

    @Override
    public void onFilmModeChanged(boolean enabled) {
        if (enabled) {
            mHandler.removeMessages(MSG_HIDE_BARS);
        } else {
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_navi) {
            getActivity().finish();
        } else if (v.getId() == R.id.action_share) {

            MobclickAgent.onEvent(getActivity(), StatConstants.EVENT_KEY_FULL_IMAGE_SHARE);
            showShareDialog();
        } else if (v.getId() == R.id.action_detail) {

            MobclickAgent.onEvent(getActivity(), StatConstants.EVENT_KEY_FULL_IMAGE_DETAIL);
            if (mShowDetails) {
                hideDetails();
            } else {
                showDetails();
            }
        } else if (v.getId() == R.id.action_delete) {

            MobclickAgent.onEvent(getActivity(), StatConstants.EVENT_KEY_FULL_IMAGE_DELETE);
            SingleDeleteMediaListener cdl = new SingleDeleteMediaListener(getActivity(), mCurrentPhoto.getPath(), getDataManager(),
                    new DeleteMediaProgressListener() {

                        @Override
                        public void onConfirmDialogDismissed(boolean confirmed) {
                            if (confirmed) {
                                mTotalCount = mMediaSet.getMediaCount();
                                if (mTotalCount > 0) {
                                    updateActionBarMessage(getString(R.string.full_image_browse, Math.min(mCurrentIndex + 1, mTotalCount), mTotalCount));
                                } else {
                                    // not medias
                                    Toast.makeText(getActivity(), R.string.full_image_browse_empty, Toast.LENGTH_SHORT).show();
                                    getActivity().finish();
                                    return;
                                }
                            }
                        }

                    });

            final LetoolDialog dlg = new LetoolDialog(getActivity());
            dlg.setTitle(R.string.common_recommend);
            dlg.setOkBtn(R.string.common_ok, cdl);
            dlg.setCancelBtn(R.string.common_cancel, cdl);
            dlg.setMessage(R.string.common_delete_cur_tip);
            dlg.setDividerVisible(true);
            dlg.show();

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (BaseFragmentActivity) this.getActivity();
    }

    private void initViews() {
        mSelectionManager = new ContractSelector(mActivity, false);
        mFullImageView = new FullImageView(this);
        mFullImageView.setListener(this);
        mRootPane.addComponent(mFullImageView);
        mOrientationManager = mActivity.getOrientationManager();
        mGLRootView.setOrientationSource(mOrientationManager);
    }

    private void initDatas() {
        Bundle data = this.getArguments();
        boolean isCamera = data.getBoolean(ThumbnailActivity.KEY_IS_PHOTO_ALBUM);
        if (!isCamera) {
            String albumTitle = data.getString(ThumbnailActivity.KEY_ALBUM_TITLE);
            int albumId = data.getInt(ThumbnailActivity.KEY_ALBUM_ID, 0);
            String albumMediaPath = data.getString(ThumbnailActivity.KEY_MEDIA_PATH);
            LLog.i(TAG, " photo fragment onCreateView id:" + albumId + " albumTitle:" + albumTitle + " albumMediaPath:" + albumMediaPath + " isCamera:");
            mMediaSet = getDataManager().getMediaSet(new MediaPath(albumMediaPath, albumId));
        } else {
            mMediaSet = new LocalAlbum(new MediaPath(data.getString(ThumbnailActivity.KEY_MEDIA_PATH), MediaSetUtils.MY_ALBUM_BUCKETS[0])
                    , (LetoolApp) getActivity().getApplication()
                    , MediaSetUtils.MY_ALBUM_BUCKETS, true, getString(R.string.common_photo));
        }
        mStartInFilmstrip = data.getBoolean(KEY_START_IN_FILMSTRIP, false);
        mCurrentIndex = data.getInt(KEY_INDEX_HINT, 0);
        mSelectionManager.setSourceMediaSet(mMediaSet);
    }

    private void initBrowseActionBar() {
        LetoolTopBar actionBar = getLetoolTopBar();
        actionBar.setOnActionMode(LetoolTopBar.ACTION_BAR_MODE_FULL_IMAGE, this);
        actionBar.setTitleIcon(R.drawable.ic_action_previous_item);
        actionBar.setVisible(View.VISIBLE, false);
        LetoolBottomBar bottomBar = getLetoolBottomBar();
        bottomBar.setOnActionMode(LetoolBottomBar.BOTTOM_BAR_MODE_FULL_IMAGE, this);
        bottomBar.setVisible(View.VISIBLE, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.gl_root_view, container, false);
        mGLRootView = (GLRootView) rootView.findViewById(R.id.gl_root_view);
        initViews();
        initDatas();
        initBrowseActionBar();

        if (mCurrentPhoto == null) {
            mTotalCount = mMediaSet.getAllMediaItems();
            if (mTotalCount > 0) {
                if (mCurrentIndex >= mTotalCount)
                    mCurrentIndex = 0;
                mCurrentPhoto = mMediaSet.getMediaItem(mCurrentIndex, 1).get(0);
            } else {
                return rootView;
            }
        }

        PhotoDataAdapter pda = new PhotoDataAdapter(this, mFullImageView, mMediaSet, mCurrentPhoto.getPath(), mCurrentIndex);
        mModel = pda;
        mFullImageView.setModel(mModel);

        pda.setDataListener(new PhotoDataAdapter.DataListener() {

            @Override
            public void onPhotoChanged(int index, MediaItem item) {
                mCurrentIndex = index;
                updateActionBarMessage(getString(R.string.full_image_browse, Math.min(mCurrentIndex + 1, mTotalCount), mTotalCount));

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
        mHandler = new SynchronizedHandler(mGLRootView) {

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
                        mGLRootView.unfreeze();
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

        mFullImageView.setFilmMode(mStartInFilmstrip && mMediaSet.getAllMediaItems() > 1);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mModel == null) {
            mActivity.finish();
            return;
        }
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
    public void onPause() {
        super.onPause();
        mGLRootView.onPause();
        mGLRootView.lockRenderThread();
        try {
            mIsActive = false;
            mGLRootView.unfreeze();
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
            mGLRootView.unlockRenderThread();
        }
    }

    @Override
    public void onDestroy() {
        mGLRootView.setOrientationSource(null);
        mHandler.removeCallbacksAndMessages(null); // Remove all pending messages.
        super.onDestroy();
    }

    //////////////////////////////////////////////////////////[detail]/////////////////////////////////////////////////////////////////
    private void showDetails() {
        mShowDetails = true;
        if (mDetailsHelper == null) {
            mDetailsHelper = new DetailsHelper(mActivity, mRootPane, new MyDetailsSource());
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
            return mMediaSet != null ? mMediaSet.getAllMediaItems() : 1;
        }

        @Override
        public int setIndex() {
            return mModel.getCurrentIndex();
        }
    }

    @Override
    public GLController getGLController() {
        return mGLRootView;
    }

    //////////////////////////////////////////////////////////[share]/////////////////////////////////////////////////////////////////

    private void showShareDialog() {

        final ShareManager shareManager = new ShareManager(getAndroidContext());
        final List<ShareTo> shareToList = shareManager.getShareToList();
        if (shareToList.size() <= 0) {
            Toast.makeText(mActivity, R.string.share_app_install_tip, Toast.LENGTH_SHORT).show();
            return;
        }
        final ArrayList<String> shareData = new ArrayList<String>();
        shareData.add(mCurrentPhoto.getFilePath());
        final LetoolDialog dlg = new LetoolDialog(getActivity());
        dlg.setTitle(R.string.common_share_to);
        dlg.setOkBtn(R.string.common_cancel, null);

        ListView l = dlg.setListAdapter(new ShareToAdapter(shareToList));
        l.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int postion, long id) {
                shareManager.onShareTo(getActivity(), shareToList.get(postion).shareToType, shareData);
                dlg.dismiss();
            }
        });
        dlg.show();
    }

    private class ShareToAdapter extends BaseAdapter {

        final List<ShareTo> mShareToList;

        public ShareToAdapter(List<ShareTo> data) {
            mShareToList = data;
        }

        @Override
        public int getCount() {
            return mShareToList.size();
        }

        @Override
        public Object getItem(int position) {
            return mShareToList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View v;
            if (convertView == null) {
                v = getActivity().getLayoutInflater().inflate(R.layout.share_list_item, parent, false);
            } else {
                v = convertView;
            }
            TextView textView = (TextView) v.findViewById(R.id.app_name);
            textView.setText(mShareToList.get(position).shareToTitle);
            ImageView imageView = (ImageView) v.findViewById(R.id.app_icon);
            imageView.setImageDrawable(mShareToList.get(position).shareToIcon);
            return v;
        }
    }

}
