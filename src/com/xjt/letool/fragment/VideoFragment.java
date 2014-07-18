
package com.xjt.letool.fragment;

import com.umeng.analytics.MobclickAgent;
import com.xjt.letool.LetoolApp;
import com.xjt.letool.LetoolContext;
import com.xjt.letool.R;
import com.xjt.letool.activities.LocalMediaActivity;
import com.xjt.letool.activities.MoviePlayActivity;
import com.xjt.letool.common.EyePosition;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.SynchronizedHandler;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.metadata.MediaItem;
import com.xjt.letool.metadata.MediaPath;
import com.xjt.letool.metadata.MediaSet;
import com.xjt.letool.metadata.MediaSetUtils;
import com.xjt.letool.metadata.loader.DataLoadingListener;
import com.xjt.letool.metadata.loader.ThumbnailDataLoader;
import com.xjt.letool.metadata.source.LocalAlbum;
import com.xjt.letool.selectors.ContractSelectListener;
import com.xjt.letool.selectors.ContractSelector;
import com.xjt.letool.stat.StatConstants;
import com.xjt.letool.surpport.MenuItem;
import com.xjt.letool.surpport.PopupMenu;
import com.xjt.letool.surpport.PopupMenu.OnMenuItemClickListener;
import com.xjt.letool.utils.LetoolUtils;
import com.xjt.letool.utils.RelativePosition;
import com.xjt.letool.utils.StorageUtils;
import com.xjt.letool.utils.Utils;
import com.xjt.letool.view.BatchDeleteMediaListener;
import com.xjt.letool.view.BatchDeleteMediaListener.DeleteMediaProgressListener;
import com.xjt.letool.view.DetailsHelper;
import com.xjt.letool.view.GLBaseView;
import com.xjt.letool.view.GLController;
import com.xjt.letool.view.LetoolBottomBar;
import com.xjt.letool.view.LetoolEmptyView;
import com.xjt.letool.view.LetoolTopBar;
import com.xjt.letool.view.LetoolDialog;
import com.xjt.letool.view.LetoolTopBar.OnActionModeListener;
import com.xjt.letool.view.ThumbnailView;
import com.xjt.letool.views.layout.ThumbnailContractLayout;
import com.xjt.letool.views.layout.ThumbnailLayout;
import com.xjt.letool.views.layout.ThumbnailLayout.LayoutListener;
import com.xjt.letool.views.opengl.FadeTexture;
import com.xjt.letool.views.opengl.GLESCanvas;
import com.xjt.letool.views.render.ThumbnailVideoRenderer;
import com.xjt.letool.views.utils.ViewConfigs;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @Author Jituo.Xuan
 * @Date 9:48:35 AM Apr 19, 2014
 * @Comments:null
 */
public class VideoFragment extends Fragment implements EyePosition.EyePositionListener, ContractSelectListener, OnActionModeListener,
        LayoutListener, OnMenuItemClickListener {

    private static final String TAG = VideoFragment.class.getSimpleName();

    private static final int BIT_LOADING_RELOAD = 1;
    private static final int MSG_LAYOUT_CONFIRMED = 0;
    private static final int MSG_PICK_PHOTO = 1;

    private static final int POP_UP_MENU_ITEM_SELECT = 0;
    private static final int POP_UP_MENU_ITEM_CAMERA = 1;

    private LetoolContext mLetoolContext;

    // photo data
    private MediaPath mVideoDataPath;
    private MediaSet mVideoData;
    private ThumbnailDataLoader mVideoDataLoader;
    private int mLoadingBits = 0;

    // views
    private GLController mGLController;
    private ViewConfigs.VideoPage mConfig;
    private ThumbnailView mThumbnailView;
    private ThumbnailVideoRenderer mRender;
    private RelativePosition mOpenCenter = new RelativePosition();
    private boolean mIsActive = false;

    private String mAlbumTitle;
    private boolean mIsCameraSource = false;
    private boolean mIsSDCardMountedCorreclty = false;
    private boolean mHasDefaultDCIMDirectory = false;
    private boolean mGetContent;
    private SynchronizedHandler mHandler;
    protected ContractSelector mSelector;
    private EyePosition mEyePosition; // The eyes' position of the user, the origin is at the center of the device and the unit is in pixels.
    private float mUserDistance; // in pixel
    private float mX;
    private float mY;
    private float mZ;

    private LayoutInflater mLayoutInflater;

    private final GLBaseView mRootPane = new GLBaseView() {

        private final float mMatrix[] = new float[16];

        @Override
        protected void onLayout(boolean changed, int left, int top, int right,
                int bottom) {
            mEyePosition.resetPosition();
            LetoolTopBar actionBar = mLetoolContext.getLetoolTopBar();
            int thumbnailViewLeft = left + mConfig.paddingLeft;
            int thumbnailViewRight = right - left - mConfig.paddingRight;
            int thumbnailViewTop = top + mConfig.paddingTop + actionBar.getHeight();
            int thumbnailViewBottom = bottom - top - mConfig.paddingBottom;
            mRender.setHighlightItemPath(null);
            // Set the mThumbnailView as a reference point to the open animation
            mOpenCenter.setReferencePosition(0, thumbnailViewTop);
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
        if (mLoadingBits == 0 && mIsActive) {
            if (mVideoDataLoader.size() == 0) {
                showEmptyView(R.string.common_error_no_movie);
            }
        }
    }

    @Override
    public void onLayoutFinshed(int count) {
        mHandler.obtainMessage(MSG_LAYOUT_CONFIRMED, count, 0).sendToTarget();
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

    private void onSingleTapUp(int videoIndex) {
        if (!mIsActive)
            return;

        if (mSelector.inSelectionMode()) {
            MediaItem item = mVideoDataLoader.get(videoIndex);
            if (item == null)
                return; // Item not ready yet, ignore the click
            mSelector.toggle(item.getPath());
            mThumbnailView.invalidate();
        } else { // Render transition in pressed state
            mRender.setPressedIndex(videoIndex);
            mRender.setPressedUp();
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PICK_PHOTO, videoIndex, 0), FadeTexture.DURATION);
        }
    }

    public void onLongTap(int videoIndex) {
        MobclickAgent.onEvent(mLetoolContext.getAppContext(), StatConstants.EVENT_KEY_PHOTO_LONG_PRESSED);
        if (mGetContent)
            return;
        MediaItem item = mVideoDataLoader.get(videoIndex);
        if (item == null)
            return;
        mSelector.toggle(item.getPath());
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
        mLetoolContext.setMainView(mRootPane);
        mGLController = mLetoolContext.getGLController();
        mLayoutInflater = getActivity().getLayoutInflater();
        mIsSDCardMountedCorreclty = StorageUtils.externalStorageAvailable();
        mHasDefaultDCIMDirectory = MediaSetUtils.MY_ALBUM_BUCKETS.length > 0;
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
                        playVideo(message.arg1);
                        break;
                    }
                    default:
                        throw new AssertionError(message.what);
                }
            }
        };
        mEyePosition = new EyePosition(mLetoolContext.getAppContext(), this);
        if (!mIsSDCardMountedCorreclty) {
            showEmptyView(R.string.common_error_nosdcard);
            return;
        } else if (mIsCameraSource && !mHasDefaultDCIMDirectory) {
            showEmptyView(R.string.common_error_nodcim);
            return;
        }
    }

    private void initializeData() {
        Bundle data = getArguments();
        mIsCameraSource = data.getBoolean(LocalMediaActivity.KEY_IS_CAMERA_SOURCE);
        if (mIsCameraSource) {
            if (MediaSetUtils.MY_ALBUM_BUCKETS.length > 0) {
                mAlbumTitle = getString(R.string.common_photo);
                mVideoDataPath = new MediaPath(data.getString(LocalMediaActivity.KEY_MEDIA_PATH), MediaSetUtils.MY_ALBUM_BUCKETS[0]);
                mVideoData = new LocalAlbum(mVideoDataPath, (LetoolApp) getActivity().getApplication(), MediaSetUtils.MY_ALBUM_BUCKETS, mLetoolContext.isImageBrwosing(),
                        getString(R.string.common_photo));
                mHasDefaultDCIMDirectory = true;
            } else {
                mHasDefaultDCIMDirectory = false;
                return;
            }
        } else {
            mAlbumTitle = data.getString(LocalMediaActivity.KEY_ALBUM_TITLE);
            mVideoDataPath = new MediaPath(data.getString(LocalMediaActivity.KEY_MEDIA_PATH), data.getInt(LocalMediaActivity.KEY_ALBUM_ID));
            mVideoData = mLetoolContext.getDataManager().getMediaSet(mVideoDataPath);
            if (mVideoData == null) {
                Utils.fail("MediaSet is null. Path = %s", mVideoDataPath);
            }
        }
        mVideoDataLoader = new ThumbnailDataLoader(mLetoolContext, mVideoData);
        mVideoDataLoader.setLoadingListener(new MetaDataLoadingListener());
    }

    private void initializeViews() {
        mSelector = new ContractSelector(mLetoolContext, false);
        mSelector.setSelectionListener(this);
        mConfig = ViewConfigs.VideoPage.get(mLetoolContext.getAppContext());
        ThumbnailLayout layout;
        layout = new ThumbnailContractLayout(mConfig.videoSpec);
        mThumbnailView = new ThumbnailView(mLetoolContext, layout);
        mThumbnailView.setBackgroundColor(LetoolUtils.intColorToFloatARGBArray(getResources().getColor(R.color.default_background_thumbnail)));
        mThumbnailView.setListener(new ThumbnailView.SimpleListener() {

            @Override
            public void onDown(int index) {
                VideoFragment.this.onDown(index);
            }

            @Override
            public void onUp(boolean followedByLongPress) {
                VideoFragment.this.onUp(followedByLongPress);
            }

            @Override
            public void onSingleTapUp(int videoIndex) {
                VideoFragment.this.onSingleTapUp(videoIndex);
            }

            @Override
            public void onLongTap(int videoIndex) {
                VideoFragment.this.onLongTap(videoIndex);
            }
        });
        mRender = new ThumbnailVideoRenderer(mLetoolContext, mThumbnailView, mSelector);
        layout.setRenderer(mRender);
        layout.setLayoutListener(this);
        mThumbnailView.setThumbnailRenderer(mRender);
        mRender.setModel(mVideoDataLoader);
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
            naviToPhoto.setText(R.string.common_record);
            naviToPhoto.setEnabled(false);
            TextView naviToGallery = (TextView) nativeButtons.findViewById(R.id.navi_to_gallery);
            naviToGallery.setText(mLetoolContext.isImageBrwosing()?R.string.common_gallery:R.string.common_video);
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
        String format = getResources().getQuantityString(R.plurals.number_of_items_selected, 0);
        actionBar.setTitleText(String.format(format, 0));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LLog.i(TAG, "onCreateView" + System.currentTimeMillis());
        initBars();
        return null;
    }

    private void showEmptyView(int resId) {
        LetoolEmptyView emptyView = (LetoolEmptyView)mLayoutInflater.inflate(R.layout.local_empty_view, null);
        emptyView.updataView(R.drawable.ic_launcher, resId);
        mLetoolContext.setMainView(emptyView);
    }

    @Override
    public void onStart() {
        super.onStart();
        LLog.i(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        mGLController.onResume();
        if (!mIsSDCardMountedCorreclty || mIsCameraSource && !mHasDefaultDCIMDirectory) {
            return;
        }
        LLog.i(TAG, "onResume" + System.currentTimeMillis());
        mGLController.lockRenderThread();
        try {
            mIsActive = true;
            // Set the reload bit here to prevent it exit this page in clearLoadingBit().
            setLoadingBit(BIT_LOADING_RELOAD);
            mVideoDataLoader.resume();
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
        LLog.i(TAG, "onPause");
        mGLController.onPause();
        if (!mIsSDCardMountedCorreclty || mIsCameraSource && !mHasDefaultDCIMDirectory) {
            return;
        }
        mGLController.lockRenderThread();
        try {
            mIsActive = false;
            mVideoDataLoader.pause();
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
        if (mVideoData != null) {
            mVideoData.closeCursor();
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
        if (!mIsSDCardMountedCorreclty)
            return;
        if (v.getId() == R.id.action_navi) {
            if (mIsCameraSource) {
                mLetoolContext.getLetoolSlidingMenu().toggle();
            } else {
                mLetoolContext.popContentFragment();
            }
        } else if (v.getId() == R.id.operation_delete) {

            MobclickAgent.onEvent(mLetoolContext.getAppContext(),
                    StatConstants.EVENT_KEY_PHOTO_DELETE);
            int count = mSelector.getSelectedCount();
            if (count <= 0) {
                Toast t = Toast.makeText(getActivity(),
                        R.string.common_selection_tip, Toast.LENGTH_SHORT);
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
            dlg.setDividerVisible(true);
            dlg.setMessage(R.string.common_delete_tip);
            dlg.setDividerVisible(true);
            dlg.show();

        }
        else if (v.getId() == R.id.navi_to_gallery) {
            GalleryFragment f = new GalleryFragment();
            Bundle data = new Bundle();
            data.putString(LocalMediaActivity.KEY_MEDIA_PATH, mLetoolContext.getDataManager()
            		.getTopSetPath(mLetoolContext.isImageBrwosing()?DataManager.INCLUDE_LOCAL_IMAGE_SET_ONLY:DataManager.INCLUDE_LOCAL_VIDEO_SET_ONLY));
            f.setArguments(data);
            mLetoolContext.pushContentFragment(f, this, false);
        }
        else if (v.getId() == R.id.selection_finished) {
            MobclickAgent.onEvent(mLetoolContext.getAppContext(),
                    StatConstants.EVENT_KEY_SELECT_OK);
            mSelector.leaveSelectionMode();
        }
    }

    public void showPopupMenu() {
        PopupMenu popup = new PopupMenu(this.getActivity());
        popup.setOnItemSelectedListener(this);
        popup.add(POP_UP_MENU_ITEM_SELECT, R.drawable.ic_action_accept, R.string.popup_menu_select_mode);
        popup.add(POP_UP_MENU_ITEM_CAMERA, R.drawable.ic_action_camera, R.string.popup_menu_take_picture);
        //popup.show(mMore);

    }

    @Override
    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case ContractSelector.ENTER_SELECTION_MODE: {
                initSelectionBar();
                mRootPane.invalidate();
                break;
            }
            case ContractSelector.LEAVE_SELECTION_MODE: {
                initBars();
                mRootPane.invalidate();
                break;
            }
            case ContractSelector.SELECT_ALL_MODE: {
                mRootPane.invalidate();
                break;
            }
        }
    }

    @Override
    public void onSelectionChange(MediaPath path, boolean selected) {
        int count = mSelector.getSelectedCount();
        String format = getResources().getQuantityString(R.plurals.number_of_items_selected, count);
        mLetoolContext.getLetoolTopBar().setTitleText(String.format(format, count));
    }

    private void playVideo(int videoIndex) {
    	MediaItem item = mVideoDataLoader.get(videoIndex);
        if (item == null)
            return;
        Context c = mLetoolContext.getAppContext();
        try {
        	Intent intent = new Intent();
        	intent.setClass(c, MoviePlayActivity.class);
            intent.setDataAndType(Uri.parse(item.getFilePath()), "video/*");
            intent.putExtra(Intent.EXTRA_TITLE, "");
        	c.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(c, c.getString(R.string.app_name),Toast.LENGTH_SHORT).show();
        }
    }

    // -----------------------------------------------details-----------------------------------------------------------------------

    @Override
    public void onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case POP_UP_MENU_ITEM_SELECT:
                MobclickAgent.onEvent(mLetoolContext.getAppContext(), StatConstants.EVENT_KEY_MENU_SELECT);
                if (mSelector != null) {
                    mSelector.enterSelectionMode();
                }
                break;
            case POP_UP_MENU_ITEM_CAMERA:
                MobclickAgent.onEvent(mLetoolContext.getAppContext(), StatConstants.EVENT_KEY_MENU_CAMERA);
                Intent it = new Intent();
                it.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                startActivity(it);
                break;

        }
    }

}
