
package com.xjt.letool.pages;

import java.lang.ref.WeakReference;

import com.xjt.letool.DataManager;
import com.xjt.letool.EyePosition;
import com.xjt.letool.LetoolActionBar;
import com.xjt.letool.R;
import com.xjt.letool.adapters.ThumbnailSetAdapter;
import com.xjt.letool.common.LLog;
import com.xjt.letool.datas.MediaDetails;
import com.xjt.letool.datas.Path;
import com.xjt.letool.opengl.GLESCanvas;
import com.xjt.letool.utils.LetoolUtils;
import com.xjt.letool.views.DetailsHelper;
import com.xjt.letool.views.DetailsHelper.CloseListener;
import com.xjt.letool.views.GLBaseView;
import com.xjt.letool.views.ThumbnailView;
import com.xjt.letool.views.ViewConfigs;
import com.xjt.letool.views.layout.ThumbnailContractLayout;
import com.xjt.letool.views.render.ThumbnailSetRenderer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class ThumbnailSetPage extends PageState implements EyePosition.EyePositionListener {

    private static final String TAG = "ThumbnailSetPage";

    public static final String KEY_EMPTY_ALBUM = "empty-album";
    private static final int REQUEST_DO_ANIMATION = 1;

    private ThumbnailView mThumbnailView;
    private boolean mIsActive = false;
    private ViewConfigs.AlbumSetPage mConfig;
    private LetoolActionBar mActionBar;
    private ThumbnailSetRenderer mThumbnailViewRenderer;

    private ThumbnailSetAdapter mThumbnailSetAdapter;

    private DetailsHelper mDetailsHelper;
    private MyDetailsSource mDetailsSource;
    private boolean mShowDetails;
    private EyePosition mEyePosition;
    private WeakReference<Toast> mEmptyAlbumToast = null;
    // The eyes' position of the user, the origin is at the center of the device and the unit is in pixels.
    private float mX;
    private float mY;
    private float mZ;

    private final GLBaseView mRootPane = new GLBaseView() {

        private final float mMatrix[] = new float[16];

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            mEyePosition.resetPosition();
            int slotViewLeft = left + mConfig.paddingLeft;
            int slotViewTop = mActionBar.getHeight() + mConfig.paddingTop;
            int slotViewBottom = bottom - top - mConfig.paddingBottom;
            int slotViewRight = right - left - mConfig.paddingRight;
            if (mShowDetails) {
                mDetailsHelper.layout(left, slotViewTop, right, bottom);
            } else {
                mThumbnailViewRenderer.setHighlightItemPath(null);
            }

            mThumbnailView.layout(slotViewLeft, slotViewTop, slotViewRight, slotViewBottom);
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

    private void initializeViews() {
        mConfig = ViewConfigs.AlbumSetPage.get(mActivity);
        mThumbnailView = new ThumbnailView(mActivity, new ThumbnailContractLayout(mConfig.albumSetSpec));
        mThumbnailViewRenderer = new ThumbnailSetRenderer(mActivity, mThumbnailView);
        mThumbnailView.setThumbnailRenderer(mThumbnailViewRenderer);
        mRootPane.addComponent(mThumbnailView);
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

    @Override
    public void onCreate(Bundle data, Bundle restoreState) {
        super.onCreate(data, restoreState);
        initializeViews();
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
        mThumbnailViewRenderer.resume();
        mEyePosition.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsActive = false;
        mThumbnailViewRenderer.pause();
        mEyePosition.pause();
    }

    @Override
    protected boolean onItemSelected(MenuItem item) {
        Activity activity = mActivity;
        switch (item.getItemId()) {
/*            case R.id.action_cancel:
                activity.setResult(Activity.RESULT_CANCELED);
                activity.finish();
                return true;
            case R.id.action_select:
                mSelectionManager.setAutoLeaveSelectionMode(false);
                mSelectionManager.enterSelectionMode();
                return true;*/
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
/*            case R.id.action_camera: {
                LetoolUtils.startCameraActivity(activity);
                return true;
            }
            case R.id.action_manage_offline: {
                Bundle data = new Bundle();
                String mediaPath = mActivity.getDataManager().getTopSetPath(DataManager.INCLUDE_ALL);
                data.putString(ThumbnailSetPage.KEY_MEDIA_PATH, mediaPath);
                mActivity.getStateManager().startState(ManageCachePage.class, data);
                return true;
            }
            case R.id.action_settings: {
                activity.startActivity(new Intent(activity, LetoolSettings.class));
                return true;
            }*/
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
            Path id = mSelectionManager.getSelected(false).get(0);
            mIndex = mThumbnailSetAdapter.findSet(id);
            return mIndex;
        }

        @Override
        public MediaDetails getDetails() {
            MediaObject item = mThumbnailSetAdapter.getMediaSet(mIndex);
            if (item != null) {
                mThumbnailViewRenderer.setHighlightItemPath(item.getPath());
                return item.getDetails();
            } else {
                return null;
            }
        }
    }
}
