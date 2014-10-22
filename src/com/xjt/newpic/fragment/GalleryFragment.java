
package com.xjt.newpic.fragment;

import java.io.File;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.xjt.newpic.NpApp;
import com.xjt.newpic.NpContext;
import com.xjt.newpic.R;
import com.xjt.newpic.activities.NpMediaActivity;
import com.xjt.newpic.common.EyePosition;
import com.xjt.newpic.common.GlobalConstants;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.common.SynchronizedHandler;
import com.xjt.newpic.metadata.DataManager;
import com.xjt.newpic.metadata.MediaDetails;
import com.xjt.newpic.metadata.MediaPath;
import com.xjt.newpic.metadata.MediaSet;
import com.xjt.newpic.metadata.loader.DataLoadingListener;
import com.xjt.newpic.metadata.loader.ThumbnailSetDataLoader;
import com.xjt.newpic.metadata.source.LocalAlbumSet;
import com.xjt.newpic.preference.GlobalPreference;
import com.xjt.newpic.selectors.SelectionListener;
import com.xjt.newpic.selectors.SelectionManager;
import com.xjt.newpic.stat.StatConstants;
import com.xjt.newpic.utils.LetoolUtils;
import com.xjt.newpic.utils.RelativePosition;
import com.xjt.newpic.utils.StorageUtils;
import com.xjt.newpic.views.DetailsHelper;
import com.xjt.newpic.views.DetailsHelper.CloseListener;
import com.xjt.newpic.views.GLController;
import com.xjt.newpic.views.GLView;
import com.xjt.newpic.views.NpTopBar;
import com.xjt.newpic.views.NpTopBar.OnActionModeListener;
import com.xjt.newpic.views.ThumbnailView;
import com.xjt.newpic.views.layout.ThumbnailLayoutBase;
import com.xjt.newpic.views.layout.ThumbnailLayoutParam;
import com.xjt.newpic.views.layout.ThumbnailSetLayout;
import com.xjt.newpic.views.opengl.FadeTexture;
import com.xjt.newpic.views.opengl.GLESCanvas;
import com.xjt.newpic.views.render.ThumbnailSetGridRenderer;
import com.xjt.newpic.views.render.ThumbnailSetListRenderer;
import com.xjt.newpic.views.render.ThumbnailSetRenderer;
import com.xjt.newpic.views.utils.ViewConfigs;

/**
 * @Author Jituo.Xuan
 * @Date 9:40:26 PM Apr 20, 2014
 * @Comments:null
 */
public class GalleryFragment extends Fragment implements OnActionModeListener, EyePosition.EyePositionListener, SelectionListener {

    private static final String TAG = GalleryFragment.class.getSimpleName();

    private static final int MSG_LAYOUT_CONFIRMED = 0;
    private static final int MSG_PICK_ALBUM = 1;

    private static final int BIT_LOADING_RELOAD = 1;
    public static final String GALLERY_AINMATION_FROM_CENTER = "g_anim_center";
    public static final int RESULT_CAPTURE_IMAGE = 1112;
    public static final int RESULT_CAPTURE_VIDEO = 1113;

    private ViewGroup mNativeButtons;
    private NpContext mLetoolContext;
    private GLController mGLController;
    private ThumbnailView mThumbnailView;
    private boolean mIsSDCardMountedCorreclty = false;
    private boolean mIsActive = false;
    private RelativePosition mOpenCenter = new RelativePosition();
    private ThumbnailSetRenderer mThumbnailViewRenderer;

    private SelectionManager mSelector;
    private ThumbnailSetDataLoader mThumbnailSetAdapter;
    private MediaSet mMediaSet;

    private DetailsHelper mDetailsHelper;
    private MyDetailsSource mDetailsSource;
    private boolean mShowDetails;
    private EyePosition mEyePosition;
    // The eyes' position of the user, the origin is at the center of the device and the unit is in pixels.
    private float mX;
    private float mY;
    private float mZ;

    private int mLoadingBits = 0;
    private Handler mHandler;

    private final GLView mRootPane = new GLView() {

        private final float mMatrix[] = new float[16];

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            mEyePosition.resetPosition();
            int paddingLeft = 0, paddingRight = 0, paddingTop = 0, paddingBottom = 0;
            if (mLetoolContext.isImagePicking() || (mLetoolContext.isImageBrwosing() ? GlobalPreference.isPictureGalleryListMode(getActivity())
                    : GlobalPreference.isVideoGalleryListMode(getActivity()))) {
                ViewConfigs.AlbumSetListPage config = ViewConfigs.AlbumSetListPage.get(mLetoolContext.getActivityContext());
                paddingLeft = config.paddingLeft;
                paddingRight = config.paddingRight;
                paddingTop = config.paddingTop;
                paddingBottom = config.paddingBottom;
            } else {
                ViewConfigs.AlbumSetGridPage config = ViewConfigs.AlbumSetGridPage.get(mLetoolContext.getActivityContext());
                paddingLeft = config.paddingLeft;
                paddingRight = config.paddingRight;
                paddingTop = config.paddingTop;
                paddingBottom = config.paddingBottom;
            }

            NpTopBar actionBar = mLetoolContext.getLetoolTopBar();
            int thumbnailViewLeft = left + paddingLeft;
            int thumbnailViewRight = right - left - paddingRight;
            int thumbnailViewTop = top + paddingTop + actionBar.getHeight();
            int thumbnailViewBottom = bottom - top - paddingBottom;
            if (mShowDetails) {
                mDetailsHelper.layout(left, thumbnailViewTop, right, bottom);
            } else {
                mThumbnailViewRenderer.setHighlightItemPath(null);
            }

            mOpenCenter.setReferencePosition(0, thumbnailViewTop);
            mOpenCenter.setAbsolutePosition((right - left) / 2, (bottom - top) / 2);

            mThumbnailView.layout(thumbnailViewLeft, thumbnailViewTop, thumbnailViewRight, thumbnailViewBottom);
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

    private class MyLoadingListener implements DataLoadingListener {

        @Override
        public void onLoadingStarted() {
            setLoadingBit(BIT_LOADING_RELOAD);
        }

        @Override
        public void onLoadingFinished(boolean loadingFailed) {
            clearLoadingBit(BIT_LOADING_RELOAD);
        }
    }

    private void setLoadingBit(int loadingBit) {
        mLoadingBits |= loadingBit;
    }

    private void clearLoadingBit(int loadingBit) {
        mLoadingBits &= ~loadingBit;
        if (mLoadingBits == 0 && mIsActive) {
            if (mThumbnailSetAdapter.size() == 0) {
                mLetoolContext.showEmptyView(mLetoolContext.isImageBrwosing() ? R.drawable.ic_no_picture : R.drawable.ic_no_video,
                        mLetoolContext.isImageBrwosing() ? R.string.common_error_no_gallery : R.string.common_error_no_movies);
                return;
            } else {
                mLetoolContext.hideEmptyView();
            }
        }
    }

    public void onSingleTapUp(int thumbnailIndex) {
        if (!mIsActive)
            return;
        if (mSelector.inSelectionMode()) {
            MediaSet targetSet = mThumbnailSetAdapter.getMediaSet(thumbnailIndex);
            if (targetSet == null)
                return; // Content is dirty, we shall reload soon
            mSelector.toggle(targetSet.getPath());
            mThumbnailView.invalidate();
        } else {
            // Show pressed-up animation for the single-tap.
            mThumbnailViewRenderer.setPressedIndex(thumbnailIndex);
            mThumbnailViewRenderer.setPressedUp();
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PICK_ALBUM, thumbnailIndex, 0), FadeTexture.DURATION);
        }
    }

    private void onDown(int index) {
        mThumbnailViewRenderer.setPressedIndex(index);
    }

    private void onUp(boolean followedByLongPress) {
        if (followedByLongPress) {
            // Avoid showing press-up animations for long-press.
            mThumbnailViewRenderer.setPressedIndex(-1);
        } else {
            mThumbnailViewRenderer.setPressedUp();
        }
    }

    public void onLongTap(int thumbnailIndex) {
        MobclickAgent.onEvent(mLetoolContext.getActivityContext(), StatConstants.EVENT_KEY_GALLERY_LONG_PRESSED);
        return;
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
        mLetoolContext = (NpContext) getActivity();

        initializeViews();
        initializeData();
        mEyePosition = new EyePosition(mLetoolContext.getActivityContext(), this);
        mThumbnailView.startScatteringAnimation(mOpenCenter, false, true, false);
    }

    private void initializeViews() {
        mSelector = new SelectionManager(mLetoolContext, true);
        mSelector.setSelectionListener(this);
        boolean isListView = mLetoolContext.isImagePicking();
        ThumbnailLayoutBase layout = null;
        ThumbnailLayoutParam layoutParam = null;
        if (isListView || (mLetoolContext.isImageBrwosing() ? GlobalPreference.isPictureGalleryListMode(getActivity())
                : GlobalPreference.isVideoGalleryListMode(getActivity()))) {
            layoutParam = ViewConfigs.AlbumSetListPage.get(mLetoolContext.getActivityContext()).albumSetListSpec;
            layout = new ThumbnailSetLayout(layoutParam, true);
        } else {
            layoutParam = ViewConfigs.AlbumSetGridPage.get(mLetoolContext.getActivityContext()).albumSetGridSpec;
            layout = new ThumbnailSetLayout(layoutParam, false);
        }
        mThumbnailView = new ThumbnailView(mLetoolContext, layout);
        mThumbnailView.setBackgroundColor(
                LetoolUtils.intColorToFloatARGBArray(getResources().getColor(R.color.cp_main_background_color))
                );
        if (isListView || (mLetoolContext.isImageBrwosing() ? GlobalPreference.isPictureGalleryListMode(getActivity())
                : GlobalPreference.isVideoGalleryListMode(getActivity()))) {
            mThumbnailViewRenderer = new ThumbnailSetListRenderer(mLetoolContext, mThumbnailView, mSelector);
        } else {
            mThumbnailViewRenderer = new ThumbnailSetGridRenderer(mLetoolContext, mThumbnailView, mSelector);
        }
        layout.setRenderer(mThumbnailViewRenderer);
        mThumbnailView.setThumbnailRenderer(mThumbnailViewRenderer);
        mRootPane.addComponent(mThumbnailView);
        mThumbnailView.setListener(new ThumbnailView.SimpleListener() {

            @Override
            public void onDown(int index) {
                GalleryFragment.this.onDown(index);
            }

            @Override
            public void onUp(boolean followedByLongPress) {
                GalleryFragment.this.onUp(followedByLongPress);
            }

            @Override
            public void onSingleTapUp(int thumbnailIndex) {
                GalleryFragment.this.onSingleTapUp(thumbnailIndex);
            }

            @Override
            public void onLongTap(int thumbnailIndex) {
                GalleryFragment.this.onLongTap(thumbnailIndex);
            }
        });
    }

    private void initializeData() {
        mMediaSet = new LocalAlbumSet(new MediaPath(mLetoolContext.getDataManager()
                .getTopSetPath(mLetoolContext.isImageBrwosing() ? DataManager.INCLUDE_LOCAL_IMAGE_ONLY : DataManager.INCLUDE_LOCAL_VIDEO_ONLY), -1000),
                (NpApp) getActivity().getApplication(), mLetoolContext.isImageBrwosing());
        mSelector.setSourceMediaSet(mMediaSet);
        mThumbnailSetAdapter = new ThumbnailSetDataLoader(mLetoolContext, mMediaSet);
        mThumbnailSetAdapter.setLoadingListener(new MyLoadingListener());
        mThumbnailViewRenderer.setModel(mThumbnailSetAdapter);
    }

    private void initBars() {
        NpTopBar topBar = mLetoolContext.getLetoolTopBar();
        topBar.setOnActionMode(NpTopBar.ACTION_BAR_MODE_BROWSE, this);
        topBar.setTitleIcon(R.drawable.ic_drawer);
        topBar.setTitleText(mLetoolContext.isImageBrwosing() ? R.string.common_gallery : R.string.common_movies);
        mNativeButtons = (ViewGroup) topBar.getActionPanel().findViewById(R.id.navi_buttons);
        if (mLetoolContext.isImagePicking()) {
            mNativeButtons.setVisibility(View.INVISIBLE);
        } else {
            mNativeButtons.setVisibility(View.VISIBLE);
            ImageView changeStyle = (ImageView) mNativeButtons.findViewById(R.id.action_action1);
            if (mLetoolContext.isImageBrwosing()) {
                if (GlobalPreference.isPictureGalleryListMode(getActivity())) {
                    changeStyle.setImageResource(R.drawable.ic_gallery_show_grid);
                } else {
                    changeStyle.setImageResource(R.drawable.ic_gallery_show_list);
                }
            } else {
                if (GlobalPreference.isVideoGalleryListMode(getActivity())) {
                    changeStyle.setImageResource(R.drawable.ic_gallery_show_grid);
                } else {
                    changeStyle.setImageResource(R.drawable.ic_gallery_show_list);
                }
            }
            changeStyle.setVisibility(View.VISIBLE);
            changeStyle.setOnClickListener(this);
        }
        //
        ImageView naviToGallery = (ImageView) mNativeButtons.findViewById(R.id.action_action2);
        naviToGallery.setOnClickListener(this);
    }

    private void initSelectionActionBar() {
        NpTopBar actionBar = mLetoolContext.getLetoolTopBar();
        actionBar.setOnActionMode(NpTopBar.ACTION_BAR_MODE_SELECTION, this);
        actionBar.setContractSelectionManager(mSelector);
        String format = getResources().getQuantityString(R.plurals.number_of_items, 0);
        actionBar.setTitleText(String.format(format, 0));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LLog.i(TAG, "onCreateView");
        initBars();
        mIsSDCardMountedCorreclty = StorageUtils.externalStorageAvailable();
        if (!mIsSDCardMountedCorreclty) {
            mLetoolContext.showEmptyView(R.drawable.ic_launcher, R.string.common_error_nosdcard);
        } else {
            mLetoolContext.hideEmptyView();
        }
        mGLController = mLetoolContext.getGLController();
        mHandler = new SynchronizedHandler(mGLController) {

            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_LAYOUT_CONFIRMED: {
                        //                        mLoadingInsie.setVisibility(View.GONE);
                        break;
                    }
                    case MSG_PICK_ALBUM: {
                        pickAlbum(message.arg1);
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
    public void onStart() {
        super.onStart();
        LLog.i(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        LLog.i(TAG, "onResume");
        MobclickAgent.onPageStart(TAG);
        if (!mIsSDCardMountedCorreclty)
            return;
        mGLController.setContentPane(mRootPane);
        mGLController.onResume();
        mGLController.lockRenderThread();
        try {
            mIsActive = true;
            setLoadingBit(BIT_LOADING_RELOAD);
            mThumbnailSetAdapter.resume();
            mThumbnailViewRenderer.resume();
            mEyePosition.resume();
        } finally {
            mGLController.unlockRenderThread();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LLog.i(TAG, "onPause");
        MobclickAgent.onPageEnd(TAG);
        mGLController.onPause();
        mGLController.lockRenderThread();
        try {
            mIsActive = false;
            mThumbnailSetAdapter.pause();
            mThumbnailViewRenderer.pause();
            mEyePosition.pause();
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
        if (mMediaSet != null) {
            mMediaSet.destroyMediaSet();
        }
        if (GlobalPreference.rememberLastUI(getActivity())) {
            GlobalPreference.setLastUI(getActivity(), mLetoolContext.isImageBrwosing() ?
                    GlobalConstants.UI_TYPE_IMAGE_SETS : GlobalConstants.UI_TYPE_VIDEO_SETS);
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

    private void hideDetails() {
        mShowDetails = false;
        mDetailsHelper.hide();
        mThumbnailViewRenderer.setHighlightItemPath(null);
        mThumbnailView.invalidate();
    }

    private void showDetails() {
        mShowDetails = true;
        if (mDetailsHelper == null) {
            mDetailsHelper = new DetailsHelper(mLetoolContext, mRootPane, mDetailsSource);
            mDetailsHelper.setCloseListener(new CloseListener() {

                @Override
                public void onClose() {
                    hideDetails();
                }
            });
        }
        mDetailsHelper.show();
    }

    private class MyDetailsSource implements DetailsHelper.DetailsSource {

        private int mIndex;

        @Override
        public int size() {
            return mThumbnailSetAdapter.size();
        }

        @Override
        public int setIndex() {
            return mIndex;
        }

        @Override
        public MediaDetails getDetails() {
            return null;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_navi) {
            MobclickAgent.onEvent(mLetoolContext.getActivityContext(), StatConstants.EVENT_KEY_SLIDE_MENU);
            mLetoolContext.getSlidingMenu().toggle();
        } else {
            if (!mIsSDCardMountedCorreclty)
                return;
            if (v.getId() == R.id.action_action3) {
                mSelector.leaveSelectionMode();
            } else if (v.getId() == R.id.action_action1) {
                MobclickAgent.onEvent(mLetoolContext.getActivityContext(), StatConstants.EVENT_KEY_LIST_GRID);
                if (mLetoolContext.isImageBrwosing())
                    GlobalPreference.setPictureGalleryListMode(getActivity(), !GlobalPreference.isPictureGalleryListMode(getActivity()));
                else
                    GlobalPreference.setVideoGalleryListMode(getActivity(), !GlobalPreference.isVideoGalleryListMode(getActivity()));
                GalleryFragment f = new GalleryFragment();
                Bundle data = new Bundle();
                data.putBoolean(GALLERY_AINMATION_FROM_CENTER, false);
                f.setArguments(data);
                mLetoolContext.pushContentFragment(f, this, false);
            } else if (v.getId() == R.id.action_action2) {
                MobclickAgent.onEvent(mLetoolContext.getActivityContext(), StatConstants.EVENT_KEY_NAVI_CAMERA);
                Intent it = new Intent();
                File f = new File(Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera/");
                if (!f.exists()) {
                    f.mkdirs();
                }
                if (mLetoolContext.isImageBrwosing()) {
                    if (f.exists()) {
                        Uri uri = Uri.fromFile(new File(f.getAbsolutePath() + "/NP_IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
                        it.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                        it.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        LLog.i(TAG, "        s1:" + uri.toString());
                        getActivity().startActivityForResult(it, RESULT_CAPTURE_IMAGE);
                        mLetoolContext.setCapturedMediaUri(uri);
                    }
                } else {
                    if (f.exists()) {

                        Uri uri = Uri.fromFile(new File(f.getAbsolutePath() + "/NP_VID_" + String.valueOf(System.currentTimeMillis()) + ".mp4"));
                        it.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
                        it.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        LLog.i(TAG, "        s2:" + uri.toString());
                        getActivity().startActivityForResult(it, RESULT_CAPTURE_VIDEO);
                        mLetoolContext.setCapturedMediaUri(uri);
                    }
                }
            }
        }
    }

    private void pickAlbum(int thumbnailIndex) {
        if (!mIsActive)
            return;
        MediaSet albumData = mThumbnailSetAdapter.getMediaSet(thumbnailIndex);
        if (albumData == null)
            return; // Content is dirty, we shall reload soon
        if (albumData.getTotalMediaItemCount() == 0) {
            Toast.makeText(getActivity(),
                    mLetoolContext.isImageBrwosing() ? R.string.common_error_no_photos : R.string.common_error_no_video
                    , Toast.LENGTH_SHORT).show();
            return;
        }
        MediaPath mediaPath = albumData.getPath();
        Fragment f = mLetoolContext.isImageBrwosing() ? new PhotoFragment() : new VideoFragment();
        Bundle data = new Bundle();
        data.putString(NpMediaActivity.KEY_MEDIA_PATH, mLetoolContext.getDataManager()
                .getTopSetPath(mLetoolContext.isImageBrwosing() ? DataManager.INCLUDE_LOCAL_IMAGE_ONLY : DataManager.INCLUDE_LOCAL_VIDEO_ONLY));
        data.putString(NpMediaActivity.KEY_ALBUM_TITLE, albumData.getName());
        data.putInt(NpMediaActivity.KEY_ALBUM_ID, mediaPath.getIdentity());
        data.putBoolean(NpMediaActivity.KEY_IS_CAMERA_SOURCE, false);
        f.setArguments(data);
        mLetoolContext.pushContentFragment(f, this, true);

    }

    @Override
    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case SelectionManager.ENTER_SELECTION_MODE: {
                initSelectionActionBar();
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

}
