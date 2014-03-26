package com.xjt.letool.pages;

import com.xjt.letool.LetoolActionBar;
import com.xjt.letool.opengl.GLESCanvas;
import com.xjt.letool.utils.LetoolUtils;
import com.xjt.letool.views.GLImageView;
import com.xjt.letool.views.ThumbnailView;
import com.xjt.letool.views.ViewConfigs;
import com.xjt.letool.views.layout.ThumbnailContractLayout;
import com.xjt.letool.views.render.ThumbnailViewRenderer;

import android.os.Bundle;
import android.view.Menu;

public class AlbumSetPage extends PageState {

    private ThumbnailView mThumbnailView;
    private boolean mIsActive = false;
    private ViewConfigs.AlbumSetPage mConfig;
    private LetoolActionBar mActionBar;
    private ThumbnailViewRenderer mThumbnailViewRenderer;

    // The eyes' position of the user, the origin is at the center of the
    // device and the unit is in pixels.
    private float mX;
    private float mY;
    private float mZ;

    private final GLImageView mRootPane = new GLImageView() {
        private final float mMatrix[] = new float[16];

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

            int slotViewTop = mActionBar.getHeight() + mConfig.paddingTop;
            int slotViewBottom = bottom - top - mConfig.paddingBottom;
            int slotViewRight = right - left;

            //            if (mShowDetails) {
            //                mDetailsHelper.layout(left, slotViewTop, right, bottom);
            //            } else {
            //                mAlbumSetView.setHighlightItemPath(null);
            //            }

            mThumbnailView.layout(0, slotViewTop, slotViewRight, slotViewBottom);
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
        mThumbnailViewRenderer = new ThumbnailViewRenderer( mActivity,  mThumbnailView);
        mThumbnailView.setThumbnailRenderer(mThumbnailViewRenderer);
        mRootPane.addComponent(mThumbnailView);
    }

    @Override
    public void onCreate(Bundle data, Bundle restoreState) {
        initializeViews();
        mActionBar = mActivity.getLetoolActionBar();
    }

    @Override
    protected boolean onCreateActionBar(Menu menu) {
        return true;
    }

    @Override
    public void onResume() {
        mIsActive = true;
        setContentPane(mRootPane);
    }

    @Override
    public void onPause() {
        mIsActive = false;
    }

    @Override
    public void onDestroy() {

    }
}
