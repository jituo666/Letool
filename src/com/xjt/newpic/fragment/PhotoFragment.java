
package com.xjt.newpic.fragment;

import com.umeng.analytics.MobclickAgent;
import com.xjt.newpic.LetoolApp;
import com.xjt.newpic.LetoolContext;
import com.xjt.newpic.R;
import com.xjt.newpic.activities.LetoolMainActivity;
import com.xjt.newpic.activities.LocalMediaActivity;
import com.xjt.newpic.activities.SettingsActivity;
import com.xjt.newpic.common.EyePosition;
import com.xjt.newpic.common.GlobalConstants;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.common.SynchronizedHandler;
import com.xjt.newpic.metadata.DataManager;
import com.xjt.newpic.metadata.MediaItem;
import com.xjt.newpic.metadata.MediaPath;
import com.xjt.newpic.metadata.MediaSet;
import com.xjt.newpic.metadata.MediaSetUtils;
import com.xjt.newpic.metadata.loader.DataLoadingListener;
import com.xjt.newpic.metadata.loader.ThumbnailDataLoader;
import com.xjt.newpic.metadata.source.LocalAlbum;
import com.xjt.newpic.preference.GlobalPreference;
import com.xjt.newpic.selectors.SelectionListener;
import com.xjt.newpic.selectors.SelectionManager;
import com.xjt.newpic.share.ShareManager;
import com.xjt.newpic.share.ShareManager.ShareListener;
import com.xjt.newpic.stat.StatConstants;
import com.xjt.newpic.utils.LetoolUtils;
import com.xjt.newpic.utils.RelativePosition;
import com.xjt.newpic.utils.StorageUtils;
import com.xjt.newpic.utils.Utils;
import com.xjt.newpic.view.BatchDeleteMediaListener;
import com.xjt.newpic.view.DetailsHelper;
import com.xjt.newpic.view.GLView;
import com.xjt.newpic.view.GLController;
import com.xjt.newpic.view.LetoolBottomBar;
import com.xjt.newpic.view.LetoolDialog;
import com.xjt.newpic.view.LetoolTopBar;
import com.xjt.newpic.view.ThumbnailView;
import com.xjt.newpic.view.BatchDeleteMediaListener.DeleteMediaProgressListener;
import com.xjt.newpic.view.LetoolTopBar.OnActionModeListener;
import com.xjt.newpic.views.layout.ThumbnailContractLayout;
import com.xjt.newpic.views.layout.ThumbnailLayout;
import com.xjt.newpic.views.opengl.FadeTexture;
import com.xjt.newpic.views.opengl.GLESCanvas;
import com.xjt.newpic.views.render.ThumbnailRenderer;
import com.xjt.newpic.views.utils.ViewConfigs;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @Author Jituo.Xuan
 * @Date 9:48:35 AM Apr 19, 2014
 * @Comments:null
 */
public class PhotoFragment extends Fragment implements EyePosition.EyePositionListener, SelectionListener, OnActionModeListener {

    private static final String TAG = PhotoFragment.class.getSimpleName();

    private static final int BIT_LOADING_RELOAD = 1;
    private static final int MSG_LAYOUT_CONFIRMED = 0;
    private static final int MSG_PICK_PHOTO = 1;

    private LetoolContext mLetoolContext;

    // photo data
    private MediaPath mDataSetPath;
    private MediaSet mDataSet;
    private ThumbnailDataLoader mAlbumDataSetLoader;
    private int mLoadingBits = 0;

    // views
    private GLController mGLController;
    private ViewConfigs.AlbumPage mConfig;
    private ThumbnailView mThumbnailView;
    private ThumbnailRenderer mRender;
    private RelativePosition mOpenCenter = new RelativePosition();
    private boolean mIsActive = false;

    private String mAlbumTitle;
    private boolean mIsCameraSource = false;
    private boolean mIsSDCardMountedCorreclty = false;
    private boolean mHasDefaultDCIMDirectory = false;

    private boolean mGetContent;
    private SynchronizedHandler mHandler;
    protected SelectionManager mSelector;
    private EyePosition mEyePosition; // The eyes' position of the user, the origin is at the center of the device and the unit is in pixels.
    private float mUserDistance; // in pixel
    private float mX;
    private float mY;
    private float mZ;

    private final GLView mRootPane = new GLView() {

        private final float mMatrix[] = new float[16];

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            mEyePosition.resetPosition();
            LetoolTopBar actionBar = mLetoolContext.getLetoolTopBar();
            int thumbnailViewLeft = left + mConfig.paddingLeft;
            int thumbnailViewRight = right - left - mConfig.paddingRight;
            int thumbnailViewTop = top + mConfig.paddingTop + actionBar.getHeight();
            int thumbnailViewBottom = bottom - top - mConfig.paddingBottom;
            mRender.setHighlightItemPath(null);
            // Set the mThumbnailView as a reference point to the open animation
            mOpenCenter.setReferencePosition(0, thumbnailViewTop);
            mOpenCenter.setAbsolutePosition((right - left) / 2, (bottom - top) / 2);
            mThumbnailView.layout(thumbnailViewLeft, thumbnailViewTop, thumbnailViewRight, thumbnailViewBottom);
            LetoolUtils.setViewPointMatrix(mMatrix, (right - left) / 2, (bottom - top) / 2, -mUserDistance);
        }

        @Override
        protected void render(GLESCanvas canvas) {
            canvas.save(GLESCanvas.SAVE_FLAG_MATRIX);
            LetoolUtils.setViewPointMatrix(mMatrix, getWidth() / 2 + mX, getHeight() / 2 + mY, mZ);
            canvas.multiplyMatrix(mMatrix, 0);
            super.render(canvas);
            canvas.restore();
        }
    };

    private class MetaDataLoadingListener implements DataLoadingListener {

        @Override
        public void onLoadingStarted() {
            setLoadingBit(BIT_LOADING_RELOAD);
        }

        @Override
        public void onLoadingFinished(boolean loadFailed) {
            clearLoadingBit(BIT_LOADING_RELOAD);
        }
    }

    private void setLoadingBit(int loadTaskBit) {
        mLoadingBits |= loadTaskBit;
    }

    private void clearLoadingBit(int loadTaskBit) {
        mLoadingBits &= ~loadTaskBit;

        LLog.i(TAG, " clearLoadingBit mLoadingBits:" + mLoadingBits + " mIsActive:" + mIsActive);
        if (mLoadingBits == 0 && mIsActive) {
            if (mAlbumDataSetLoader.size() == 0) {
                LLog.i(TAG, " clearLoadingBit mAlbumDataSetLoader.size():" + mAlbumDataSetLoader.size());
                mLetoolContext.showEmptyView(R.drawable.ic_no_picture, mIsCameraSource ? R.string.common_error_no_photos : R.string.common_error_no_gallery);
            } else {
                mLetoolContext.hideEmptyView();
                showGuideTip();
            }
        }
    }

    private void onDown(int index) {
        mRender.setPressedIndex(index);
    }

    private void onUp(boolean followedByLongPress) {
        if (followedByLongPress) {
            mRender.setPressedIndex(-1); // Avoid showing press-up animations for long-press.
        } else {
            mRender.setPressedUp();
        }
    }

    private void onSingleTapUp(int thumbnailIndex) {
        if (!mIsActive)
            return;
        if (mSelector.inSelectionMode()) {
            MediaItem item = mAlbumDataSetLoader.get(thumbnailIndex);
            if (item == null)
                return; // Item not ready yet, ignore the click
            MediaPath p = item.getPath();
            p.setFilePath(item.getFilePath());
            mSelector.toggle(p);
            mThumbnailView.invalidate();
        } else { // Render transition in pressed state
            mRender.setPressedIndex(thumbnailIndex);
            mRender.setPressedUp();
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PICK_PHOTO, thumbnailIndex, 0), FadeTexture.DURATION);
        }
    }

    public void onLongTap(int thumbnailIndex) {
        hideGuideTip();
        MobclickAgent.onEvent(mLetoolContext.getActivityContext(), StatConstants.EVENT_KEY_PHOTO_LONG_PRESSED);
        if (mGetContent)
            return;
        MediaItem item = mAlbumDataSetLoader.get(thumbnailIndex);
        if (item == null)
            return;
        MediaPath p = item.getPath();
        p.setFilePath(item.getFilePath());
        mSelector.toggle(p);
        mThumbnailView.invalidate();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LLog.i(TAG, "onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LLog.i(TAG, "onCreate");
        mLetoolContext = (LetoolContext) getActivity();
        mGLController = mLetoolContext.getGLController();

        initializeData();
        initializeViews();
        mHandler = new SynchronizedHandler(mGLController) {

            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_LAYOUT_CONFIRMED: {
                        // mLoadingInsie.setVisibility(View.GONE);
                        break;
                    }
                    case MSG_PICK_PHOTO: {
                        pickPhoto(message.arg1);
                        break;
                    }
                    default:
                        throw new AssertionError(message.what);
                }
            }
        };
        mEyePosition = new EyePosition(mLetoolContext.getActivityContext(), this);
        mThumbnailView.startScatteringAnimation(mOpenCenter);
    }

    private void initializeData() {
        Bundle data = getArguments();
        mIsCameraSource = data.getBoolean(LocalMediaActivity.KEY_IS_CAMERA_SOURCE);
        if (mIsCameraSource) {
            if (MediaSetUtils.getBucketsIds().length > 0) {
                mAlbumTitle = getString(R.string.common_photo);
                mDataSetPath = new MediaPath(data.getString(LocalMediaActivity.KEY_MEDIA_PATH), MediaSetUtils.getBucketsIds()[0]);
                mDataSet = new LocalAlbum(mDataSetPath, (LetoolApp) getActivity().getApplication(), MediaSetUtils.getBucketsIds(),
                        mLetoolContext.isImageBrwosing(),
                        getString(R.string.common_photo));
                mHasDefaultDCIMDirectory = true;
            } else {
                mHasDefaultDCIMDirectory = false;
                return;
            }
        } else {
            mAlbumTitle = data.getString(LocalMediaActivity.KEY_ALBUM_TITLE);
            mDataSetPath = new MediaPath(data.getString(LocalMediaActivity.KEY_MEDIA_PATH), data.getInt(LocalMediaActivity.KEY_ALBUM_ID));
            mDataSet = mLetoolContext.getDataManager().getMediaSet(mDataSetPath);
            if (mDataSet == null) {
                Utils.fail("MediaSet is null. Path = %s", mDataSetPath);
            }
        }
        mAlbumDataSetLoader = new ThumbnailDataLoader(mLetoolContext, mDataSet);
        mAlbumDataSetLoader.setLoadingListener(new MetaDataLoadingListener());
    }

    private void initializeViews() {
        mSelector = new SelectionManager(mLetoolContext, false);
        mSelector.setSelectionListener(this);
        mConfig = ViewConfigs.AlbumPage.get(mLetoolContext.getActivityContext());
        ThumbnailLayout layout;
        layout = new ThumbnailContractLayout(mConfig.albumSpec);
        mThumbnailView = new ThumbnailView(mLetoolContext, layout);
        mThumbnailView.setBackgroundColor(LetoolUtils.intColorToFloatARGBArray(getResources().getColor(R.color.gl_background_color)));
        mThumbnailView.setListener(new ThumbnailView.SimpleListener() {

            @Override
            public void onDown(int index) {
                PhotoFragment.this.onDown(index);
            }

            @Override
            public void onUp(boolean followedByLongPress) {
                PhotoFragment.this.onUp(followedByLongPress);
            }

            @Override
            public void onSingleTapUp(int thumbnailIndex) {
                PhotoFragment.this.onSingleTapUp(thumbnailIndex);
            }

            @Override
            public void onLongTap(int thumbnailIndex) {
                PhotoFragment.this.onLongTap(thumbnailIndex);
            }
        });
        mRender = new ThumbnailRenderer(mLetoolContext, mThumbnailView, mSelector);
        layout.setRenderer(mRender);
        mThumbnailView.setThumbnailRenderer(mRender);
        mRender.setModel(mAlbumDataSetLoader);
        mRootPane.addComponent(mThumbnailView);
    }

    private void initBars() {
        LetoolTopBar topBar = mLetoolContext.getLetoolTopBar();
        topBar.setOnActionMode(LetoolTopBar.ACTION_BAR_MODE_BROWSE, this);
        topBar.setVisible(View.VISIBLE, false);
        ViewGroup nativeButtons = (ViewGroup) topBar.getActionPanel().findViewById(R.id.navi_buttons);
        if (mIsCameraSource) {
            topBar.setTitleText(R.string.app_name);
            nativeButtons.setVisibility(View.VISIBLE);
            topBar.setTitleIcon(R.drawable.ic_drawer);
            TextView naviToPhoto = (TextView) nativeButtons.findViewById(R.id.navi_to_photo);
            naviToPhoto.setText(R.string.common_photo);
            naviToPhoto.setEnabled(false);
            TextView naviToGallery = (TextView) nativeButtons.findViewById(R.id.navi_to_gallery);
            naviToGallery.setText(mLetoolContext.isImageBrwosing() ? R.string.common_gallery : R.string.common_movies);
            naviToGallery.setEnabled(true);
            naviToGallery.setOnClickListener(this);
        } else {
            topBar.setTitleText(mAlbumTitle);
            nativeButtons.setVisibility(View.GONE);
            topBar.setTitleIcon(R.drawable.ic_action_previous_item);
        }
        LetoolBottomBar bottomBar = mLetoolContext.getLetoolBottomBar();
        bottomBar.setVisible(View.GONE, false);
    }

    private void initSelectionBar() {
        LetoolTopBar actionBar = mLetoolContext.getLetoolTopBar();
        actionBar.setOnActionMode(LetoolTopBar.ACTION_BAR_MODE_SELECTION, this);
        actionBar.setContractSelectionManager(mSelector);
        actionBar.getActionPanel().findViewById(R.id.operation_multi_share).setVisibility(View.VISIBLE);
        String format = getResources().getQuantityString(R.plurals.number_of_items, 0);
        actionBar.setTitleText(String.format(format, 0));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LLog.i(TAG, "onCreateView" + System.currentTimeMillis());
        initBars();
        mIsSDCardMountedCorreclty = StorageUtils.externalStorageAvailable();
        mHasDefaultDCIMDirectory = MediaSetUtils.getBucketsIds().length > 0;
        if (!mIsSDCardMountedCorreclty) {
            mLetoolContext.showEmptyView(R.drawable.ic_launcher, R.string.common_error_nosdcard);
        } else if (mIsCameraSource && !mHasDefaultDCIMDirectory) {
            mLetoolContext.showEmptyView(R.drawable.ic_no_picture, R.string.common_error_nodcim_photo);
            final LetoolDialog dlg = new LetoolDialog(getActivity());
            dlg.setTitle(R.string.common_recommend);
            dlg.setOkBtn(R.string.common_settings, new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dlg.dismiss();
                    Intent it = new Intent();
                    it.setClass(getActivity(), SettingsActivity.class);
                    it.putExtra(SettingsActivity.KEY_FROM_TIP, true);
                    startActivityForResult(it, LocalMediaActivity.REQUEST_CODE_SETTINGS);
                    getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
                }
            });
            dlg.setCancelBtn(R.string.common_cancel, new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dlg.dismiss();
                }
            });
            dlg.setMessage(R.string.camera_source_dirs_tip);
            dlg.show();
        } else {
            mLetoolContext.hideEmptyView();
        }
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        LLog.i(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
        LLog.i(TAG, "onResume mIsSDCardMountedCorreclty:" + mIsSDCardMountedCorreclty + " mIsCameraSource:" + mIsCameraSource
                + " mHasDefaultDCIMDirectory:" + mHasDefaultDCIMDirectory);
        if (!mIsSDCardMountedCorreclty || (mIsCameraSource && !mHasDefaultDCIMDirectory)) {
            return;
        }

        mIsActive = true;
        mGLController.setContentPane(mRootPane);
        mGLController.onResume();
        mGLController.lockRenderThread();
        try {
            setLoadingBit(BIT_LOADING_RELOAD);
            mAlbumDataSetLoader.resume();
            mRender.resume();
            mRender.setPressedIndex(-1);
            mEyePosition.resume();
        } finally {
            mGLController.unlockRenderThread();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
        LLog.i(TAG, "onPause");
        if (!mIsActive) {
            return;
        }
        mIsActive = false;
        mGLController.onPause();
        mGLController.lockRenderThread();
        try {
            mRender.setThumbnailFilter(null);
            mAlbumDataSetLoader.pause();
            mRender.pause();
            DetailsHelper.pause();
            mEyePosition.resume();
        } finally {
            mGLController.unlockRenderThread();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        LLog.i(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideGuideTip();
        LLog.i(TAG, "onDestroyView");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LLog.i(TAG, "onDetach");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDataSet != null) {
            mDataSet.destroyMediaSet();
        }
        if (GlobalPreference.rememberLastUI(getActivity())) {
            GlobalPreference.setLastUI(getActivity(), GlobalConstants.UI_TYPE_IMAGE_ITEMS);
        }
        LLog.i(TAG, "onDestroy");
    }

    @Override
    public void onEyePositionChanged(float x, float y, float z) {
        mRootPane.lockRendering();
        mX = x;
        mY = y;
        mZ = z;
        mRootPane.unlockRendering();
        mRootPane.invalidate();
    }

    @Override
    public void onClick(View v) {
        hideGuideTip();
        if (v.getId() == R.id.action_navi) {
            if (mIsCameraSource) {
                MobclickAgent.onEvent(mLetoolContext.getActivityContext(), StatConstants.EVENT_KEY_SLIDE_MENU);
                mLetoolContext.getLetoolSlidingMenu().toggle();
            } else {
                mLetoolContext.popContentFragment();
            }
        } else {
            if (!mIsSDCardMountedCorreclty)
                return;
            if (v.getId() == R.id.operation_delete) {
                int count = mSelector.getSelectedCount();
                if (count <= 0) {
                    Toast t = Toast.makeText(getActivity(), R.string.common_selection_tip, Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                    return;
                }
                BatchDeleteMediaListener cdl = new BatchDeleteMediaListener(
                        getActivity(), mLetoolContext.getDataManager(),
                        new DeleteMediaProgressListener() {

                            @Override
                            public void onConfirmDialogDismissed(boolean confirmed) {
                                if (confirmed) {
                                    MobclickAgent.onEvent(mLetoolContext.getActivityContext(), StatConstants.EVENT_KEY_PHOTO_DELETE);
                                    mSelector.leaveSelectionMode();
                                }
                            }

                            @Override
                            public ArrayList<MediaPath> onGetDeleteItem() {
                                return mSelector.getSelected(false);
                            }

                        });
                final LetoolDialog dlg = new LetoolDialog(getActivity());
                dlg.setTitle(R.string.common_recommend);
                dlg.setOkBtn(R.string.common_ok, cdl);
                dlg.setCancelBtn(R.string.common_cancel, cdl);
                dlg.setMessage(R.string.common_delete_tip);
                dlg.show();

            } else if (v.getId() == R.id.navi_to_gallery) {
                MobclickAgent.onEvent(mLetoolContext.getActivityContext(), mLetoolContext.isImageBrwosing() ? StatConstants.EVENT_KEY_CLICK_PICTURE :
                        StatConstants.EVENT_KEY_CLICK_MOVIE);
                GalleryFragment f = new GalleryFragment();
                Bundle data = new Bundle();
                data.putString(LocalMediaActivity.KEY_MEDIA_PATH, mLetoolContext.getDataManager()
                        .getTopSetPath(mLetoolContext.isImageBrwosing() ? DataManager.INCLUDE_LOCAL_IMAGE_SET_ONLY : DataManager.INCLUDE_LOCAL_VIDEO_SET_ONLY));
                f.setArguments(data);
                mLetoolContext.pushContentFragment(f, this, false);
            } else if (v.getId() == R.id.selection_finished) {
                MobclickAgent.onEvent(mLetoolContext.getActivityContext(), StatConstants.EVENT_KEY_SELECT_OK);
                mSelector.leaveSelectionMode();
            } else if (v.getId() == R.id.operation_multi_share) {
                ArrayList<Uri> uris = new ArrayList<Uri>();
                for (MediaPath p : mSelector.getSelected(false)) {
                    if (p.getFilePath().length() > 0) {
                        uris.add(Uri.parse("file://" + p.getFilePath()));
                    }
                }
                ShareManager.showAllShareDialog(getActivity(), GlobalConstants.MIMI_TYPE_IMAGE, uris,
                        new ShareListener() {

                            @Override
                            public void shareTriggered() {
                                if (mSelector.inSelectionMode())
                                    mSelector.leaveSelectionMode();
                            }
                        });
            }
        }
    }

    @Override
    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case SelectionManager.ENTER_SELECTION_MODE: {
                initSelectionBar();
                mRootPane.invalidate();
                break;
            }
            case SelectionManager.LEAVE_SELECTION_MODE: {
                initBars();
                mRootPane.invalidate();
                break;
            }
            case SelectionManager.SELECT_ALL_MODE: {
                mRootPane.invalidate();
                break;
            }
        }
    }

    @Override
    public void onSelectionChange(MediaPath path, boolean selected) {
        int count = mSelector.getSelectedCount();
        String format = getResources().getQuantityString(R.plurals.number_of_items, count);
        mLetoolContext.getLetoolTopBar().setTitleText(String.format(format, count));
    }

    private Rect getThumbnailRect(int index) {
        Rect offset = new Rect();
        mRootPane.getBoundsOf(mThumbnailView, offset);
        Rect r = mThumbnailView.getThumbnailRect(index);
        r.offset(offset.left - mThumbnailView.getScrollX(), offset.top - mThumbnailView.getScrollY());
        return r;
    }

    private void pickPhoto(int index) {
        hideGuideTip();
        Bundle data = new Bundle();
        if (mIsCameraSource) {
            data.putString(LocalMediaActivity.KEY_MEDIA_PATH, mLetoolContext.getDataManager()
                    .getTopSetPath(mLetoolContext.isImageBrwosing() ? DataManager.INCLUDE_LOCAL_IMAGE_ONLY : DataManager.INCLUDE_LOCAL_VIDEO_ONLY));
            data.putBoolean(LocalMediaActivity.KEY_IS_CAMERA_SOURCE, true);
        } else {
            data.putInt(LocalMediaActivity.KEY_ALBUM_ID, mDataSet.getPath().getIdentity());
            data.putString(LocalMediaActivity.KEY_MEDIA_PATH, mLetoolContext.getDataManager()
                    .getTopSetPath(mLetoolContext.isImageBrwosing() ? DataManager.INCLUDE_LOCAL_IMAGE_ONLY : DataManager.INCLUDE_LOCAL_VIDEO_ONLY));
            data.putBoolean(LocalMediaActivity.KEY_IS_CAMERA_SOURCE, false);
            data.putString(LocalMediaActivity.KEY_ALBUM_TITLE, mDataSet.getName());
        }
        Fragment fragment = new FullImageFragment();
        data.putInt(FullImageFragment.KEY_INDEX_HINT, index);
        data.putParcelable(FullImageFragment.KEY_OPEN_ANIMATION_RECT, getThumbnailRect(index));
        fragment.setArguments(data);
        mLetoolContext.pushContentFragment(fragment, this, true);
    }

    public void showGuideTip() {
        if (GlobalPreference.IsGuideTipShown(getActivity()) && mAlbumDataSetLoader.size() > 0) {
            final View tip = mLetoolContext.getGuidTipView();
            tip.setVisibility(View.VISIBLE);
            Animation a = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_bottom_in);
            a.setStartOffset(600);
            a.setDuration(600);
            tip.startAnimation(a);
            Button close = (Button) tip.findViewById(R.id.close_tip);
            close.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    tip.setVisibility(View.GONE);
                    tip.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_bottom_out));
                    GlobalPreference.setGuideTipShown(getActivity(), false);
                }
            });
        }
    }

    public void hideGuideTip() {
        if (GlobalPreference.IsGuideTipShown(getActivity())) {
            final View tip = mLetoolContext.getGuidTipView();
            if (tip.getVisibility() == View.VISIBLE) {
                tip.setVisibility(View.GONE);
                tip.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_bottom_out));
            }
        }
    }
}
