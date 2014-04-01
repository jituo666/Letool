
package com.xjt.letool.pages;

import java.lang.ref.WeakReference;

import com.xjt.letool.EyePosition;
import com.xjt.letool.LetoolActionBar;
import com.xjt.letool.R;
import com.xjt.letool.data.DataManager;
import com.xjt.letool.data.MediaDetails;
import com.xjt.letool.data.MediaSet;
import com.xjt.letool.data.loader.DataLoadingListener;
import com.xjt.letool.data.loader.ThumbnailSetDataLoader;
import com.xjt.letool.opengl.GLESCanvas;
import com.xjt.letool.selectors.SelectionManager;
import com.xjt.letool.utils.LetoolUtils;
import com.xjt.letool.views.DetailsHelper;
import com.xjt.letool.views.DetailsHelper.CloseListener;
import com.xjt.letool.views.GLBaseView;
import com.xjt.letool.views.ThumbnailView;
import com.xjt.letool.views.ViewConfigs;
import com.xjt.letool.views.layout.ThumbnailContractLayout;
import com.xjt.letool.views.layout.ThumbnailLayout;
import com.xjt.letool.views.render.ThumbnailSetRenderer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ThumbnailSetPage extends PageState implements EyePosition.EyePositionListener {

    private static final String TAG = "ThumbnailSetPage";

    public static final String KEY_EMPTY_ALBUM = "empty-album";
    private static final int DATA_CACHE_SIZE = 256;

    private static final int BIT_LOADING_RELOAD = 1;
    private static final int BIT_LOADING_SYNC = 2;
    private static final int REQUEST_DO_ANIMATION = 1;

    private ThumbnailView mThumbnailView;
    private boolean mIsActive = false;
    private ViewConfigs.AlbumSetPage mConfig;
    private LetoolActionBar mActionBar;
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
    private Button mCameraButton;
    private boolean mShowedEmptyToastForSelf = false;

    private final GLBaseView mRootPane = new GLBaseView() {

        private final float mMatrix[] = new float[16];

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            mEyePosition.resetPosition();
            int thumbnailViewLeft = left + mConfig.paddingLeft;
            int thumbnailViewTop = mActionBar.getHeight() + mConfig.paddingTop;
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
                if (mActivity.getPageManager().getStateCount() > 1) {
                    Intent result = new Intent();
                    result.putExtra(KEY_EMPTY_ALBUM, true);
                    setStateResult(Activity.RESULT_OK, result);
                    mActivity.getPageManager().finishState(this);
                } else {
                    mShowedEmptyToastForSelf = true;
                    showEmptyAlbumToast(Toast.LENGTH_LONG);
                    mThumbnailView.invalidate();
                    showCameraButton();
                }
                return;
            }
        }
        // Hide the empty album toast if we are in the root instance of
        // AlbumSetPage and the album is no longer empty (for instance,
        // after a sync is completed and web albums have been synced)
        if (mShowedEmptyToastForSelf) {
            mShowedEmptyToastForSelf = false;
            hideEmptyAlbumToast();
            hideCameraButton();
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
        toast = Toast.makeText(mActivity, R.string.empty_album, toastLength);
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

    private boolean setupCameraButton() {
        if (!LetoolUtils.isCameraAvailable(mActivity))
            return false;
        RelativeLayout galleryRoot = (RelativeLayout) ((Activity) mActivity).findViewById(R.id.gallery_root);
        if (galleryRoot == null)
            return false;

        mCameraButton = new Button(mActivity);
        mCameraButton.setText(R.string.camera_label);
        mCameraButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.frame_overlay_gallery_camera, 0, 0);
        mCameraButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {LetoolUtils.startCameraActivity(mActivity);
            }
        });
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        galleryRoot.addView(mCameraButton, lp);
        return true;
    }

    private void cleanupCameraButton() {
        if (mCameraButton == null)
            return;
        RelativeLayout galleryRoot = (RelativeLayout) ((Activity) mActivity)
                .findViewById(R.id.gallery_root);
        if (galleryRoot == null)
            return;
        galleryRoot.removeView(mCameraButton);
        mCameraButton = null;
    }

    private void showCameraButton() {
        if (mCameraButton == null && !setupCameraButton())
            return;
        mCameraButton.setVisibility(View.VISIBLE);
    }

    private void hideCameraButton() {
        if (mCameraButton == null)
            return;
        mCameraButton.setVisibility(View.GONE);
    }

    private void initializeViews() {
        mConfig = ViewConfigs.AlbumSetPage.get(mActivity);
        ThumbnailLayout layout = new ThumbnailContractLayout(mConfig.albumSetSpec);
        mThumbnailView = new ThumbnailView(mActivity, layout);
        mThumbnailViewRenderer = new ThumbnailSetRenderer(mActivity, mThumbnailView);
        layout.setRenderer(mThumbnailViewRenderer);
        mThumbnailView.setThumbnailRenderer(mThumbnailViewRenderer);
        mRootPane.addComponent(mThumbnailView);
    }

    private void initializeData(Bundle data) {
        String mediaPath = data.getString(DataManager.KEY_MEDIA_PATH);
        mMediaSet = mActivity.getDataManager().getMediaSet(mediaPath, -1000);
        //mSelectionManager.setSourceMediaSet(mMediaSet);
        mThumbnailSetAdapter = new ThumbnailSetDataLoader(mActivity, mMediaSet, DATA_CACHE_SIZE);
        mThumbnailSetAdapter.setLoadingListener(new MyLoadingListener());
        mThumbnailViewRenderer.setModel(mThumbnailSetAdapter);
    }

    @Override
    public void onCreate(Bundle data, Bundle restoreState) {
        super.onCreate(data, restoreState);
        initializeViews();
        initializeData(data);
        mActionBar = mActivity.getLetoolActionBar();
        mEyePosition = new EyePosition(mActivity.getAndroidContext(), this);
    }

    @Override
    protected boolean onCreateActionBar(Menu menu) {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsActive = true;
        setContentPane(mRootPane);
        mThumbnailSetAdapter.resume();
        mThumbnailViewRenderer.resume();
        mEyePosition.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsActive = false;
        mThumbnailSetAdapter.pause();
        mThumbnailViewRenderer.pause();
        mEyePosition.pause();
    }

    @Override
    protected boolean onItemSelected(MenuItem item) {
        Activity activity = mActivity;
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
                    Toast.makeText(activity, activity.getText(R.string.no_albums_alert), Toast.LENGTH_SHORT).show();
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
    protected void onStateResult(int requestCode, int resultCode, Intent data) {
        if (data != null && data.getBooleanExtra(KEY_EMPTY_ALBUM, false)) {
            showEmptyAlbumToast(Toast.LENGTH_SHORT);
        }
        switch (requestCode) {
            case REQUEST_DO_ANIMATION: {
                mThumbnailView.startRisingAnimation();
            }
        }
    }

    @Override
    public void onDestroy() {

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
}
