package com.xjt.letool.pages;

import java.lang.ref.WeakReference;

import com.xjt.letool.EyePosition;
import com.xjt.letool.LetoolActionBar;
import com.xjt.letool.R;
import com.xjt.letool.common.LLog;
import com.xjt.letool.opengl.GLESCanvas;
import com.xjt.letool.utils.LetoolUtils;
import com.xjt.letool.views.GLImageView;
import com.xjt.letool.views.ThumbnailView;
import com.xjt.letool.views.ViewConfigs;
import com.xjt.letool.views.layout.ThumbnailContractLayout;
import com.xjt.letool.views.render.ThumbnailRenderer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

public class ThumbnailSetPage extends PageState implements EyePosition.EyePositionListener {

    private static final String TAG = "ThumbnailSetPage";

    public static final String KEY_EMPTY_ALBUM = "empty-album";
    private static final int REQUEST_DO_ANIMATION = 1;

    private ThumbnailView mThumbnailView;
    private boolean mIsActive = false;
    private ViewConfigs.AlbumSetPage mConfig;
    private LetoolActionBar mActionBar;
    private ThumbnailRenderer mThumbnailViewRenderer;
    private EyePosition mEyePosition;
    private WeakReference<Toast> mEmptyAlbumToast = null;
    // The eyes' position of the user, the origin is at the center of the device and the unit is in pixels.
    private float mX;
    private float mY;
    private float mZ;

    private final GLImageView mRootPane = new GLImageView() {

        private final float mMatrix[] = new float[16];

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            mEyePosition.resetPosition();
            int slotViewLeft = left + mConfig.paddingLeft;
            int slotViewTop = mActionBar.getHeight() + mConfig.paddingTop;
            int slotViewBottom = bottom - top - mConfig.paddingBottom;
            int slotViewRight = right - left - mConfig.paddingRight;
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
        mThumbnailViewRenderer = new ThumbnailRenderer(mActivity, mThumbnailView);
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
}
