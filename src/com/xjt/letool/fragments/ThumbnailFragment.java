
package com.xjt.letool.fragments;

import com.xjt.letool.EyePosition;
import com.xjt.letool.Future;
import com.xjt.letool.LetoolContext;
import com.xjt.letool.R;
import com.xjt.letool.TransitionStore;
import com.xjt.letool.activities.LetoolBaseActivity;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.SynchronizedHandler;
import com.xjt.letool.data.DataManager;
import com.xjt.letool.data.MediaDetails;
import com.xjt.letool.data.MediaItem;
import com.xjt.letool.data.MediaObject;
import com.xjt.letool.data.MediaPath;
import com.xjt.letool.data.MediaSet;
import com.xjt.letool.data.loader.DataLoadingListener;
import com.xjt.letool.data.loader.ThumbnailDataLoader;
import com.xjt.letool.opengl.FadeTexture;
import com.xjt.letool.opengl.GLESCanvas;
import com.xjt.letool.selectors.SelectionListener;
import com.xjt.letool.selectors.SelectionManager;
import com.xjt.letool.utils.LetoolUtils;
import com.xjt.letool.utils.RelativePosition;
import com.xjt.letool.utils.Utils;
import com.xjt.letool.views.DetailsHelper;
import com.xjt.letool.views.DetailsHelper.CloseListener;
import com.xjt.letool.views.GLBaseView;
import com.xjt.letool.views.GLController;
import com.xjt.letool.views.GLRootView;
import com.xjt.letool.views.LetoolActionBar;
import com.xjt.letool.views.ThumbnailView;
import com.xjt.letool.views.ViewConfigs;
import com.xjt.letool.views.layout.ThumbnailContractLayout;
import com.xjt.letool.views.layout.ThumbnailLayout;
import com.xjt.letool.views.render.ThumbnailRenderer;

import android.app.Activity;
import android.os.Bundle;
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
 * @Date 9:48:35 AM Apr 19, 2014
 * @Comments:null
 */
public class ThumbnailFragment extends LetoolFragment implements EyePosition.EyePositionListener,
        SelectionListener {

    private static final String TAG = ThumbnailFragment.class.getSimpleName();

    private GLRootView mGLRootView;
    private TransitionStore mTransitionStore = new TransitionStore();
    public static final String KEY_RESUME_ANIMATION = "resume_animation";
    private static final int BIT_LOADING_RELOAD = 1;
    private static final int BIT_LOADING_SYNC = 2;
    private static final int MSG_PICK_PHOTO = 0;

    //data
    private MediaPath mDataPath;
    private MediaSet mData;
    private DetailsHelper mDetailsHelper;
    private MyDetailsSource mDetailsSource;
    private ThumbnailDataLoader mAlbumDataLoader;
    private boolean mLoadingFailed;
    private int mLoadingBits = 0;
    private Future<Integer> mSyncTask = null; // synchronize data
    private boolean mInitialSynced = false;
    private boolean mShowDetails;
    private String mAlbumTitle;
    private boolean mIsCamera = false;

    //views
    private ViewConfigs.AlbumPage mConfig;
    private ThumbnailView mThumbnailView;
    private ThumbnailRenderer mRender;
    private RelativePosition mOpenCenter = new RelativePosition();
    private boolean mIsActive = false;
    private float mUserDistance; // in pixel

    private boolean mGetContent;
    private SynchronizedHandler mHandler;
    protected SelectionManager mSelector;
    private LetoolBaseActivity mActivity;
    private EyePosition mEyePosition;
    // The eyes' position of the user, the origin is at the center of the device and the unit is in pixels.
    private float mX;
    private float mY;
    private float mZ;

    private class MyLoadingListener implements DataLoadingListener {

        @Override
        public void onLoadingStarted() {
            mLoadingFailed = false;
            setLoadingBit(BIT_LOADING_RELOAD);
        }

        @Override
        public void onLoadingFinished(boolean loadFailed) {
            mLoadingFailed = loadFailed;
            clearLoadingBit(BIT_LOADING_RELOAD);
        }
    }

    private final GLBaseView mRootPane = new GLBaseView() {

        private final float mMatrix[] = new float[16];

        @Override
        protected void onLayout(
                boolean changed, int left, int top, int right, int bottom) {
            mEyePosition.resetPosition();
            int slotViewLeft = left + mConfig.paddingLeft;
            int slotViewTop = top + mConfig.paddingTop;
            int slotViewBottom = bottom - top - mConfig.paddingBottom;
            int slotViewRight = right - left - mConfig.paddingRight;

            if (mShowDetails) {
                mDetailsHelper.layout(slotViewLeft, slotViewTop, right, bottom);
            } else {
                mRender.setHighlightItemPath(null);
            }
            // Set the mThumbnailView as a reference point to the open animation
            mOpenCenter.setReferencePosition(0, slotViewTop);
            mThumbnailView.layout(slotViewLeft, slotViewTop, slotViewRight, slotViewBottom);
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

    private class MyDetailsSource implements DetailsHelper.DetailsSource {

        private int mIndex;

        @Override
        public int size() {
            return mAlbumDataLoader.size();
        }

        @Override
        public int setIndex() {
            MediaPath id = mSelector.getSelected(false).get(0);
            mIndex = mAlbumDataLoader.findItem(id);
            return mIndex;
        }

        @Override
        public MediaDetails getDetails() {
            // this relies on setIndex() being called beforehand
            MediaObject item = mAlbumDataLoader.get(mIndex);
            if (item != null) {
                mRender.setHighlightItemPath(item.getPath());
                return item.getDetails();
            } else {
                return null;
            }
        }
    }

    private void showDetails() {
        mShowDetails = true;
        if (mDetailsHelper == null) {
            mDetailsHelper = new DetailsHelper(mActivity, mRootPane, mDetailsSource);
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
        mRender.setHighlightItemPath(null);
        mThumbnailView.invalidate();
    }

    private void setLoadingBit(int loadTaskBit) {
        mLoadingBits |= loadTaskBit;
    }

    private void clearLoadingBit(int loadTaskBit) {
        mLoadingBits &= ~loadTaskBit;
        if (mLoadingBits == 0 && mIsActive) {
            if (mAlbumDataLoader.size() == 0) {
                Toast.makeText(getAndroidContext(),R.string.empty_album, Toast.LENGTH_LONG).show();
                //                mActivity.getPageManager().finishPage(AlbumPage.this);
            }
        }
    }

    private void onDown(int index) {
        mRender.setPressedIndex(index);
    }

    private void onUp(boolean followedByLongPress) {
        if (followedByLongPress) {
            // Avoid showing press-up animations for long-press.
            mRender.setPressedIndex(-1);
        } else {
            mRender.setPressedUp();
        }
    }

    private void onSingleTapUp(int thumbnailIndex) {
        if (!mIsActive)
            return;

        if (mSelector.inSelectionMode()) {
            MediaItem item = mAlbumDataLoader.get(thumbnailIndex);
            if (item == null)
                return; // Item not ready yet, ignore the click
            mSelector.toggle(item.getPath());
            mThumbnailView.invalidate();
        } else {
            // Render transition in pressed state
            mRender.setPressedIndex(thumbnailIndex);
            mRender.setPressedUp();
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PICK_PHOTO, thumbnailIndex, 0), FadeTexture.DURATION);
        }
    }

    public void onLongTap(int thumbnailIndex) {
        if (mGetContent)
            return;
        MediaItem item = mAlbumDataLoader.get(thumbnailIndex);
        if (item == null)
            return;
        mSelector.setAutoLeaveSelectionMode(true);
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
        mActivity = (LetoolBaseActivity) getActivity();
        setHasOptionsMenu(true);
    }

    private void initializeViews() {
        mSelector = new SelectionManager(mActivity, false);
        mSelector.setSelectionListener(this);
        mConfig = ViewConfigs.AlbumPage.get(getAndroidContext());
        ThumbnailLayout layout = new ThumbnailContractLayout(mConfig.albumSpec);
        mThumbnailView = new ThumbnailView(this, layout);
        mThumbnailView.setBackgroundColor(LetoolUtils.intColorToFloatARGBArray(getResources().getColor(R.color.default_background)));
        mRender = new ThumbnailRenderer(this, mThumbnailView, mSelector);
        layout.setRenderer(mRender);
        mThumbnailView.setThumbnailRenderer(mRender);
        mRender.setModel(mAlbumDataLoader);
        mRootPane.addComponent(mThumbnailView);
        mThumbnailView.setListener(new ThumbnailView.SimpleListener() {

            @Override
            public void onDown(int index) {
                ThumbnailFragment.this.onDown(index);
            }

            @Override
            public void onUp(boolean followedByLongPress) {
                ThumbnailFragment.this.onUp(followedByLongPress);
            }

            @Override
            public void onSingleTapUp(int slotIndex) {
                ThumbnailFragment.this.onSingleTapUp(slotIndex);
            }

            @Override
            public void onLongTap(int slotIndex) {
                ThumbnailFragment.this.onLongTap(slotIndex);
            }
        });
    }

    private void initializeData() {
        Bundle data = getArguments();
        mIsCamera = data.getBoolean(DataManager.KEY_IS_CAMERA);
        mAlbumTitle = data.getString(DataManager.KEY_ALBUM_TITLE);
        mDataPath = new MediaPath(data.getString(DataManager.KEY_MEDIA_PATH), data.getLong(DataManager.KEY_ALBUM_ID));
        mData = getDataManager().getMediaSet(mDataPath);
        if (mData == null) {
            Utils.fail("MediaSet is null. Path = %s", mDataPath);
        }
        mAlbumDataLoader = new ThumbnailDataLoader(this, mData);
        mAlbumDataLoader.setLoadingListener(new MyLoadingListener());
        mRender.setModel(mAlbumDataLoader);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LLog.i(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.gl_root_view, container, false);
        mGLRootView = (GLRootView) rootView.findViewById(R.id.gl_root_view);
        LLog.i(TAG, "onCreateView:" + mGLRootView);
        initializeViews();
        initializeData();
        mHandler = new SynchronizedHandler(mGLRootView) {

            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_PICK_PHOTO: {
                        //pickPhoto(message.arg1);
                        break;
                    }
                    default:
                        throw new AssertionError(message.what);
                }
            }
        };
        mEyePosition = new EyePosition(getAndroidContext(), this);
        LetoolActionBar actionBar = mActivity.getLetoolActionBar();
        actionBar.setOnActionMode(LetoolActionBar.ACTION_BAR_MODE_BROWSE, this);
        if (mIsCamera) {
            actionBar.setTitleIcon(R.drawable.ic_drawer);
        } else {
            actionBar.setTitleIcon(R.drawable.ic_action_previous_item);

        }
        actionBar.setTitle(mAlbumTitle);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflator) {
        super.onCreateOptionsMenu(menu, inflator);
        LLog.i(TAG, "onCreateOptionsMenu");
        //GalleryActionBar actionBar = mActivity.getGalleryActionBar();

        if (mGetContent) {
            inflator.inflate(R.menu.pickup, menu);
            //int typeBits = mData.getInt(LetoolBaseActivity.KEY_TYPE_BITS,DataManager.INCLUDE_IMAGE);
            //actionBar.setTitle(LetoolUtils.getSelectionModePrompt(typeBits));
        } else {
            inflator.inflate(R.menu.album, menu);
            //actionBar.setTitle(mData.getName());

            /*
             * FilterUtils.setupMenuItems(actionBar, mMediaSetPath, true);
             * menu.findItem(R.id.action_group_by).setVisible(mShowClusterMenu);
             * menu.findItem(R.id.action_camera).setVisible(
             * MediaSetUtils.isCameraSource(mMediaSetPath) &&
             * LetoolUtils.isCameraAvailable(mActivity));
             */

        }
        //actionBar.setSubtitle(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
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
            // Set the reload bit here to prevent it exit this page in clearLoadingBit().
            setLoadingBit(BIT_LOADING_RELOAD);
            mLoadingFailed = false;
            mAlbumDataLoader.resume();
            mRender.resume();
            mRender.setPressedIndex(-1);
            mEyePosition.resume();
            if (!mInitialSynced) {
                setLoadingBit(BIT_LOADING_SYNC);
                //mSyncTask = mData.requestSync(this);
            }
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
            mRender.setThumbnailFilter(null);
            mAlbumDataLoader.pause();
            mRender.pause();
            DetailsHelper.pause();
            mEyePosition.resume();
            if (mSyncTask != null) {
                mSyncTask.cancel();
                mSyncTask = null;
                clearLoadingBit(BIT_LOADING_SYNC);
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_navi) {
            if (!mIsCamera) {
                getActivity().finish();
            } else {
                mActivity.getLetoolSlidingMenu().toggle();
            }
        }
    }

    @Override
    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case SelectionManager.ENTER_SELECTION_MODE: {
                mActivity.getLetoolActionBar().setOnActionMode(LetoolActionBar.ACTION_BAR_MODE_SELECTION, this);
                mActivity.getLetoolActionBar().setSelectionManager(mSelector);
                mRootPane.invalidate();
                break;
            }
            case SelectionManager.LEAVE_SELECTION_MODE: {
                mActivity.getLetoolActionBar().setOnActionMode(LetoolActionBar.ACTION_BAR_MODE_BROWSE, this);
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
        String format = getResources().getQuantityString(R.plurals.number_of_items_selected, count);
        mActivity.getLetoolActionBar().setTitle(String.format(format, count));
        //mActionModeHandler.updateSupportedOperation(path, selected);
    }
}
