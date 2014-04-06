
package com.xjt.letool.fragments;

import com.xjt.letool.EyePosition;
import com.xjt.letool.Future;
import com.xjt.letool.R;
import com.xjt.letool.TransitionStore;
import com.xjt.letool.activities.LetoolActivity;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.SynchronizedHandler;
import com.xjt.letool.data.DataManager;
import com.xjt.letool.data.MediaDetails;
import com.xjt.letool.data.MediaObject;
import com.xjt.letool.data.MediaPath;
import com.xjt.letool.data.MediaSet;
import com.xjt.letool.data.MediaSetUtils;
import com.xjt.letool.data.loader.DataLoadingListener;
import com.xjt.letool.data.loader.ThumbnailDataLoader;
import com.xjt.letool.opengl.GLESCanvas;
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

public class PhotoFragment extends LetoolFragment implements EyePosition.EyePositionListener {

    private static final String TAG = "PhotoFragment";

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
    private LetoolActivity mActivity;
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
                Toast.makeText(mActivity,
                        R.string.empty_album, Toast.LENGTH_LONG).show();
                //                mActivity.getPageManager().finishPage(AlbumPage.this);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (LetoolActivity) getActivity();
        setHasOptionsMenu(true);
    }

    private void initializeViews() {
        mConfig = ViewConfigs.AlbumPage.get(mActivity);
        ThumbnailLayout layout = new ThumbnailContractLayout(mConfig.albumSpec);
        mThumbnailView = new ThumbnailView(this, layout);
        mThumbnailView.setBackgroundColor(LetoolUtils.intColorToFloatARGBArray(mActivity.getAndroidContext().getResources()
                .getColor(R.color.default_background)));
        mRender = new ThumbnailRenderer(this, mThumbnailView, mSelector);
        layout.setRenderer(mRender);
        mThumbnailView.setThumbnailRenderer(mRender);
        mRender.setModel(mAlbumDataLoader);
        mRootPane.addComponent(mThumbnailView);

    }

    private void initializeData() {
        Bundle data = getArguments();
        LLog.i(TAG, " CAMERA_BUCKET_ID:" + MediaSetUtils.CAMERA_BUCKET_ID);
        mDataPath = new MediaPath(data.getString(DataManager.KEY_MEDIA_PATH), MediaSetUtils.CAMERA_BUCKET_ID);
        mData = mActivity.getDataManager().getMediaSet(mDataPath);
        if (mData == null) {
            Utils.fail("MediaSet is null. Path = %s", mDataPath);
        }
        mAlbumDataLoader = new ThumbnailDataLoader(this, mData);
        mAlbumDataLoader.setLoadingListener(new MyLoadingListener());
        mRender.setModel(mAlbumDataLoader);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        mEyePosition = new EyePosition(mActivity.getAndroidContext(), this);
        LetoolActionBar actionBar =  mActivity.getLetoolActionBar();
        actionBar.setOnActionMode(LetoolActionBar.ACTION_MODE_BROWSE, this);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflator) {
        super.onCreateOptionsMenu(menu, inflator);
        //GalleryActionBar actionBar = mActivity.getGalleryActionBar();

        if (mGetContent) {
            inflator.inflate(R.menu.pickup, menu);
            //int typeBits = mData.getInt(LetoolBaseActivity.KEY_TYPE_BITS,DataManager.INCLUDE_IMAGE);
            //actionBar.setTitle(LetoolUtils.getSelectionModePrompt(typeBits));
        } else {
            inflator.inflate(R.menu.album, menu);
            //actionBar.setTitle(mData.getName());

/*            FilterUtils.setupMenuItems(actionBar, mMediaSetPath, true);

            menu.findItem(R.id.action_group_by).setVisible(mShowClusterMenu);
            menu.findItem(R.id.action_camera).setVisible( MediaSetUtils.isCameraSource(mMediaSetPath)
                    && LetoolUtils.isCameraAvailable(mActivity));*/

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
    }

    @Override
    public void onResume() {
        super.onResume();
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        if (v.getId() == R.id.sliding_menu) {
            mActivity.getLetoolSlidingMenu().toggle();
        }
    }
}
