
package com.xjt.letool.fragments;

import java.lang.ref.WeakReference;

import com.xjt.letool.EyePosition;
import com.xjt.letool.R;
import com.xjt.letool.activities.LetoolBaseActivity;
import com.xjt.letool.activities.LetoolThumbnailActivity;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.SynchronizedHandler;
import com.xjt.letool.data.DataManager;
import com.xjt.letool.data.MediaDetails;
import com.xjt.letool.data.MediaPath;
import com.xjt.letool.data.MediaSet;
import com.xjt.letool.data.loader.DataLoadingListener;
import com.xjt.letool.data.loader.ThumbnailSetDataLoader;
import com.xjt.letool.opengl.FadeTexture;
import com.xjt.letool.opengl.GLESCanvas;
import com.xjt.letool.selectors.SelectionListener;
import com.xjt.letool.selectors.SelectionManager;
import com.xjt.letool.utils.LetoolUtils;
import com.xjt.letool.views.DetailsHelper;
import com.xjt.letool.views.GLBaseView;
import com.xjt.letool.views.GLController;
import com.xjt.letool.views.GLRootView;
import com.xjt.letool.views.LetoolActionBar;
import com.xjt.letool.views.ThumbnailView;
import com.xjt.letool.views.ViewConfigs;
import com.xjt.letool.views.DetailsHelper.CloseListener;
import com.xjt.letool.views.layout.ThumbnailContractLayout;
import com.xjt.letool.views.layout.ThumbnailLayout;
import com.xjt.letool.views.render.ThumbnailSetRenderer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


/**
 * @Author Jituo.Xuan
 * @Date 9:40:26 PM Apr 20, 2014
 * @Comments:null
 */
public class FolderFragment extends LetoolFragment implements EyePosition.EyePositionListener, SelectionListener {

    private static final String TAG = "PictureFragment";
    private GLRootView mGLRootView;
    public static final String KEY_EMPTY_ALBUM = "empty-album";
    private static final int DATA_CACHE_SIZE = 256;
    private static final int MSG_PICK_ALBUM = 1;
    private static final int BIT_LOADING_RELOAD = 1;
    private static final int BIT_LOADING_SYNC = 2;
    private static final int REQUEST_DO_ANIMATION = 1;

    private ThumbnailView mThumbnailView;
    private boolean mIsActive = false;
    private ViewConfigs.AlbumSetPage mConfig;
    private ThumbnailSetRenderer mThumbnailViewRenderer;

    private SelectionManager mSelectionManager;
    private ThumbnailSetDataLoader mThumbnailSetAdapter;
    private MediaSet mMediaSet;

    private DetailsHelper mDetailsHelper;
    private MyDetailsSource mDetailsSource;
    private boolean mShowDetails;
    private EyePosition mEyePosition;
    private WeakReference<Toast> mEmptyAlbumToast = null;
    // The eyes' position of the user, the origin is at the center of the device and the unit is in pixels.
    private float mX;
    private float mY;
    private float mZ;

    private int mLoadingBits = 0;
    private boolean mShowedEmptyToastForSelf = false;
    private boolean mGetContent = false;
    private Handler mHandler;

    private final GLBaseView mRootPane = new GLBaseView() {

        private final float mMatrix[] = new float[16];

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            mEyePosition.resetPosition();
            int thumbnailViewLeft = left + mConfig.paddingLeft;
            int thumbnailViewTop = mConfig.paddingTop;
            int thumbnailViewBottom = bottom - top - mConfig.paddingBottom;
            int thumbnailViewRight = right - left - mConfig.paddingRight;
            if (mShowDetails) {
                mDetailsHelper.layout(left, thumbnailViewTop, right, bottom);
            } else {
                mThumbnailViewRenderer.setHighlightItemPath(null);
            }

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
                // If this is not the top of the gallery folder hierarchy,
                // tell the parent AlbumSetPage instance to handle displaying
                // the empty album toast, otherwise show it within this
                // instance
                //                if (mActivity.getPageManager().getStateCount() > 1) {
                //                    Intent result = new Intent();
                //                    result.putExtra(KEY_EMPTY_ALBUM, true);
                //                    setStateResult(Activity.RESULT_OK, result);
                //                    mActivity.getPageManager().finishState(this);
                //                } else {
                //                    mShowedEmptyToastForSelf = true;
                //                    showEmptyAlbumToast(Toast.LENGTH_LONG);
                //                    mThumbnailView.invalidate();
                //
                //                }
                return;
            }
        }
        // Hide the empty album toast if we are in the root instance of
        // AlbumSetPage and the album is no longer empty (for instance,
        // after a sync is completed and web albums have been synced)
        if (mShowedEmptyToastForSelf) {
            mShowedEmptyToastForSelf = false;
            hideEmptyAlbumToast();
        }
    }

    private void showEmptyAlbumToast(int toastLength) {
        Toast toast;
        if (mEmptyAlbumToast != null) {
            toast = mEmptyAlbumToast.get();
            if (toast != null) {
                toast.show();
                return;
            }
        }
        toast = Toast.makeText(getAndroidContext(), R.string.empty_album, toastLength);
        mEmptyAlbumToast = new WeakReference<Toast>(toast);
        toast.show();
    }

    private void hideEmptyAlbumToast() {
        if (mEmptyAlbumToast != null) {
            Toast toast = mEmptyAlbumToast.get();
            if (toast != null)
                toast.cancel();
        }
    }

    public void onSingleTapUp(int slotIndex) {
        if (!mIsActive)
            return;

        if (mSelectionManager.inSelectionMode()) {
            MediaSet targetSet = mThumbnailSetAdapter.getMediaSet(slotIndex);
            if (targetSet == null)
                return; // Content is dirty, we shall reload soon
            mSelectionManager.toggle(targetSet.getPath());
            mThumbnailView.invalidate();
        } else {
            // Show pressed-up animation for the single-tap.
            mThumbnailViewRenderer.setPressedIndex(slotIndex);
            mThumbnailViewRenderer.setPressedUp();
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PICK_ALBUM, slotIndex, 0), FadeTexture.DURATION);
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

    public void onLongTap(int slotIndex) {
        if (mGetContent)
            return;
        MediaSet set = mThumbnailSetAdapter.getMediaSet(slotIndex);
        if (set == null)
            return;
        mSelectionManager.setAutoLeaveSelectionMode(true);
        mSelectionManager.toggle(set.getPath());
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
        setHasOptionsMenu(true);
    }

    private void initializeViews() {
        mSelectionManager = new SelectionManager(this, true);
        mSelectionManager.setSelectionListener(this);
        mConfig = ViewConfigs.AlbumSetPage.get(getAndroidContext());
        ThumbnailLayout layout = new ThumbnailContractLayout(mConfig.albumSetSpec);
        mThumbnailView = new ThumbnailView(this, layout);
        mThumbnailView.setBackgroundColor(
                LetoolUtils.intColorToFloatARGBArray(getResources().getColor(R.color.default_background))
                );
        mThumbnailViewRenderer = new ThumbnailSetRenderer(this, mThumbnailView);
        layout.setRenderer(mThumbnailViewRenderer);
        mThumbnailView.setThumbnailRenderer(mThumbnailViewRenderer);
        mRootPane.addComponent(mThumbnailView);
        mThumbnailView.setListener(new ThumbnailView.SimpleListener() {
            @Override
            public void onDown(int index) {
                FolderFragment.this.onDown(index);
            }

            @Override
            public void onUp(boolean followedByLongPress) {
                FolderFragment.this.onUp(followedByLongPress);
            }

            @Override
            public void onSingleTapUp(int slotIndex) {
                FolderFragment.this.onSingleTapUp(slotIndex);
            }

            @Override
            public void onLongTap(int slotIndex) {
                FolderFragment.this.onLongTap(slotIndex);
            }
        });
    }

    private void initializeData() {
        Bundle data = getArguments();
        mGetContent = data.getBoolean(LetoolBaseActivity.KEY_GET_CONTENT, false);
        mMediaSet = getDataManager().getMediaSet(data.getString(LetoolBaseActivity.KEY_MEDIA_PATH), -1000);
        mSelectionManager.setSourceMediaSet(mMediaSet);
        mThumbnailSetAdapter = new ThumbnailSetDataLoader(this, mMediaSet, DATA_CACHE_SIZE);
        mThumbnailSetAdapter.setLoadingListener(new MyLoadingListener());
        mThumbnailViewRenderer.setModel(mThumbnailSetAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        LLog.i(TAG, "onCreateOptionsMenu");
        if (mGetContent) {
            inflater.inflate(R.menu.pickup, menu);
        } else {
            inflater.inflate(R.menu.albumset, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        /*
         * case R.id.action_cancel:
         * activity.setResult(Activity.RESULT_CANCELED); activity.finish();
         * return true; case R.id.action_select:
         * mSelectionManager.setAutoLeaveSelectionMode(false);
         * mSelectionManager.enterSelectionMode(); return true;
         */
            case R.id.action_details:
                if (mThumbnailSetAdapter.size() != 0) {
                    if (mShowDetails) {
                        hideDetails();
                    } else {
                        showDetails();
                    }
                } else {
                    Toast.makeText(getAndroidContext(), getText(R.string.no_albums_alert), Toast.LENGTH_SHORT).show();
                }
                return true;
                /*
                 * case R.id.action_camera: {
                 * LetoolUtils.startCameraActivity(activity); return true; }
                 * case R.id.action_manage_offline: { Bundle data = new
                 * Bundle(); String mediaPath =
                 * mActivity.getDataManager().getTopSetPath
                 * (DataManager.INCLUDE_ALL);
                 * data.putString(ThumbnailSetPage.KEY_MEDIA_PATH, mediaPath);
                 * mActivity.getPageManager().startState(ManageCachePage.class,
                 * data); return true; } case R.id.action_settings: {
                 * activity.startActivity(new Intent(activity,
                 * LetoolSettings.class)); return true; }
                 */
            default:
                return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LLog.i(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.gl_root_view, container, false);
        mGLRootView = (GLRootView) rootView.findViewById(R.id.gl_root_view);
        mHandler = new SynchronizedHandler(mGLRootView) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_PICK_ALBUM: {
                        pickAlbum(message.arg1);
                        break;
                    }
                    default: throw new AssertionError(message.what);
                }
            }
        };
        initializeViews();
        initializeData();
        mEyePosition = new EyePosition(getAndroidContext(), this);
        LetoolActionBar actionBar = getLetoolActionBar();
        actionBar.setOnActionMode(LetoolActionBar.ACTION_BAR_MODE_BROWSE, this);
        actionBar.setTitleIcon(R.drawable.ic_drawer);
        actionBar.setTitle(R.string.common_folder);
        return rootView;
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
        mGLRootView.onResume();
        mGLRootView.lockRenderThread();
        try {
            mIsActive = true;
            mGLRootView.setContentPane(mRootPane);
            mThumbnailSetAdapter.resume();
            mThumbnailViewRenderer.resume();
            mEyePosition.resume();
        } finally {
            mGLRootView.unlockRenderThread();
        }
    }

    @Override
    public void onPause() {

        super.onPause();
        LLog.i(TAG, "onPause");
        mGLRootView.onPause();
        mGLRootView.lockRenderThread();
        try {
            mIsActive = false;
            mThumbnailSetAdapter.pause();
            mThumbnailViewRenderer.pause();
            mEyePosition.pause();
        } finally {
            mGLRootView.unlockRenderThread();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        mGLRootView.lockRenderThread();
        try {

            super.onActivityResult(requestCode, resultCode, data);
            if (data != null && data.getBooleanExtra(KEY_EMPTY_ALBUM, false)) {
                showEmptyAlbumToast(Toast.LENGTH_SHORT);
            }
            switch (requestCode) {
                case REQUEST_DO_ANIMATION: {
                    mThumbnailView.startRisingAnimation();
                }
            }
        } finally {
            mGLRootView.unlockRenderThread();
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
        LLog.i(TAG, "onDestroy");
    }

    @Override
    public GLController getGLController() {
        return mGLRootView;
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
            mDetailsHelper = new DetailsHelper(this, mRootPane, mDetailsSource);
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
            //            Path id = mSelectionManager.getSelected(false).get(0);
            //            mIndex = mThumbnailSetAdapter.findSet(id);
            return mIndex;
        }

        @Override
        public MediaDetails getDetails() {
            //            MediaObject item = mThumbnailSetAdapter.getMediaSet(mIndex);
            //            if (item != null) {
            //                mThumbnailViewRenderer.setHighlightItemPath(item.getPath());
            //                return item.getDetails();
            //            } else {
            return null;
            //            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_navi) {
            getLetoolSlidingMenu().toggle();
        }
    }

    private void pickAlbum(int slotIndex) {
        if (!mIsActive)
            return;
        MediaSet targetSet = mThumbnailSetAdapter.getMediaSet(slotIndex);
        if (targetSet == null)
            return; // Content is dirty, we shall reload soon
        if (targetSet.getTotalMediaItemCount() == 0) {
            showEmptyAlbumToast(Toast.LENGTH_SHORT);
            return;
        }
        hideEmptyAlbumToast();
        MediaPath mediaPath = targetSet.getPath();
        if (!mGetContent) {
            Intent it = new Intent();
            it.setClass(getActivity(), LetoolThumbnailActivity.class);
            it.putExtra(LetoolBaseActivity.KEY_ALBUM_ID, mediaPath.getIdentity());
            it.putExtra(LetoolBaseActivity.KEY_MEDIA_PATH, getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
            it.putExtra(LetoolBaseActivity.KEY_IS_CAMERA, false);
            it.putExtra(LetoolBaseActivity.KEY_ALBUM_TITLE, targetSet.getName());
            startActivityForResult(it, LetoolThumbnailActivity.REQUEST_FOR_PHOTO);
            return;
        }
    }

    @Override
    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case SelectionManager.ENTER_SELECTION_MODE: {
                getLetoolActionBar().setOnActionMode(LetoolActionBar.ACTION_BAR_MODE_SELECTION, this);
                getLetoolActionBar().setSelectionManager(mSelectionManager);
                mRootPane.invalidate();
                break;
            }
            case SelectionManager.LEAVE_SELECTION_MODE: {
                getLetoolActionBar().setOnActionMode(LetoolActionBar.ACTION_BAR_MODE_BROWSE, this);
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
        int count = mSelectionManager.getSelectedCount();
        String format = getResources().getQuantityString(R.plurals.number_of_items_selected, count);
        getLetoolActionBar().setTitle(String.format(format, count));
    }
}
