package com.xjt.letool.views.render;

import android.graphics.Color;

import com.xjt.letool.LetoolBaseActivity;
import com.xjt.letool.opengl.ColorTexture;
import com.xjt.letool.opengl.GLESCanvas;
import com.xjt.letool.opengl.Texture;
import com.xjt.letool.views.ThumbnailView;

public class ThumbnailViewRenderer implements ThumbnailView.Render {

    private static final String TAG = "ThumbnailViewRender";
    private static final int CACHE_SIZE = 96;
    private final int mPlaceholderColor;

    private final ColorTexture mWaitLoadingTexture;
    private LetoolBaseActivity mActivity;

    private ThumbnailView mThumbnailView;

    private int mPressedIndex = -1;
    private boolean mAnimatePressedUp;
    private boolean mInSelectionMode;

    public ThumbnailViewRenderer(LetoolBaseActivity activity, ThumbnailView thumbnailView) {
        mActivity = activity;
        mThumbnailView = thumbnailView;
        mPlaceholderColor = Color.GRAY;

        mWaitLoadingTexture = new ColorTexture(mPlaceholderColor);
        mWaitLoadingTexture.setSize(1, 1);
    }

    @Override
    public void prepareDrawing() {

    }

    @Override
    public void onVisibleRangeChanged(int visibleStart, int visibleEnd) {

    }

    @Override
    public void onThumbnailSizeChanged(int width, int height) {

    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int renderThumbnail(GLESCanvas canvas, int index, int pass, int width, int height) {
        return renderContent(canvas, width, height);
    }

    protected int renderContent(GLESCanvas canvas, int width, int height) {
        int renderRequestFlags = 0;
        drawContent(canvas, mWaitLoadingTexture, width, height, 0);
        return renderRequestFlags;
    }

    protected void drawContent(GLESCanvas canvas, Texture content, int width, int height, int rotation) {
        canvas.save(GLESCanvas.SAVE_FLAG_MATRIX);
        // The content is always rendered in to the largest square that fits inside the slot, aligned to the top of the slot.
        width = height = Math.min(width, height);
        if (rotation != 0) {
            canvas.translate(width / 2, height / 2);
            canvas.rotate(rotation, 0, 0, 1);
            canvas.translate(-width / 2, -height / 2);
        }
        // Fit the content into the box
        float scale = Math.min((float) width / content.getWidth(), (float) height / content.getHeight());
        //LLog.i(TAG, "scale:" + scale);
        canvas.scale(scale, scale, 1);
        content.draw(canvas, 0, 0);
        canvas.restore();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void resume() {
        mThumbnailView.setThumbnailCount(100);
    }

    public void pause() {

    }
}
