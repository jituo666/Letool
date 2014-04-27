
package com.xjt.letool.views.render;

import android.graphics.Color;

import com.xjt.letool.adapters.ThumbnailSetDataWindow;
import com.xjt.letool.adapters.ThumbnailSetDataWindow.AlbumSetEntry;
import com.xjt.letool.data.MediaPath;
import com.xjt.letool.data.loader.ThumbnailSetDataLoader;
import com.xjt.letool.view.ThumbnailView;
import com.xjt.letool.views.fragment.LetoolFragment;
import com.xjt.letool.views.opengl.ColorTexture;
import com.xjt.letool.views.opengl.GLESCanvas;
import com.xjt.letool.views.opengl.Texture;
import com.xjt.letool.views.opengl.UploadedTexture;
import com.xjt.letool.views.utils.AlbumLabelMaker;
import com.xjt.letool.views.utils.ViewConfigs;

public class ThumbnailSetRenderer extends AbstractThumbnailRender {

    private static final String TAG = "ThumbnailSetRenderer";

    private static final int CACHE_SIZE = 96;
    private final int mPlaceholderColor;

    private final ColorTexture mWaitLoadingTexture;
    private LetoolFragment mActivity;

    private ThumbnailView mThumbnailView;
    private ThumbnailSetDataWindow mDataWindow;

    private int mPressedIndex = -1;
    private boolean mAnimatePressedUp;
    private MediaPath mHighlightItemPath = null;
    private boolean mInSelectionMode;

    private LabelSpec mLabelSpec;

    public static class LabelSpec {
        public int labelBackgroundHeight;
        public int titleOffset;
        public int countOffset;
        public int titleFontSize;
        public int countFontSize;
        public int leftMargin;
        public int iconSize;
        public int titleRightMargin;
        public int backgroundColor;
        public int titleColor;
        public int countColor;
        public int borderSize;
    }

    private class MyCacheListener implements ThumbnailSetDataWindow.Listener {

        @Override
        public void onSizeChanged(int size) {
            mThumbnailView.setThumbnailCount(size);
        }

        @Override
        public void onContentChanged() {
            mThumbnailView.invalidate();
        }
    }

    public ThumbnailSetRenderer(LetoolFragment activity, ThumbnailView thumbnailView) {
        super(activity.getAndroidContext());
        mActivity = activity;
        mThumbnailView = thumbnailView;
        mPlaceholderColor = Color.GRAY;

        mLabelSpec = ViewConfigs.AlbumSetPage.get(activity.getAndroidContext()).labelSpec;

        mWaitLoadingTexture = new ColorTexture(mPlaceholderColor);
        mWaitLoadingTexture.setSize(1, 1);
    }

    public void setModel(ThumbnailSetDataLoader model) {
        if (mDataWindow != null) {
            mDataWindow.setListener(null);
            mDataWindow = null;
            mThumbnailView.setThumbnailCount(0);
        }
        if (model != null) {
            mDataWindow = new ThumbnailSetDataWindow(mActivity, model, mLabelSpec, CACHE_SIZE);
            mDataWindow.setListener(new MyCacheListener());
            mThumbnailView.setThumbnailCount(mDataWindow.size());
        }
    }

    public void setPressedIndex(int index) {
        if (mPressedIndex == index)
            return;
        mPressedIndex = index;
        mThumbnailView.invalidate();
    }

    public void setPressedUp() {
        if (mPressedIndex == -1)
            return;
        mAnimatePressedUp = true;
        mThumbnailView.invalidate();
    }

    public void setHighlightItemPath(MediaPath path) {
        if (mHighlightItemPath == path)
            return;
        mHighlightItemPath = path;
        mThumbnailView.invalidate();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Texture checkLabelTexture(Texture texture) {
        return ((texture instanceof UploadedTexture) && ((UploadedTexture) texture).isUploading()) ? null : texture;
    }

    @Override
    public int renderThumbnail(GLESCanvas canvas, int index, int pass, int width, int height) {
        AlbumSetEntry entry = mDataWindow.get(index);
        int renderRequestFlags = 0;
        renderRequestFlags |= renderContent(canvas, entry, width, height);
        renderRequestFlags |= renderLabel(canvas, entry, width, height);
        renderRequestFlags |= renderOverlay(canvas, index, width, height);
        return renderRequestFlags;
    }

    protected int renderContent(GLESCanvas canvas, AlbumSetEntry entry, int width, int height) {
        int renderRequestFlags = 0;
        Texture content = entry.bitmapTexture;
        if (content == null) {
            content = mWaitLoadingTexture;
            entry.isWaitLoadingDisplayed = true;
        }
        drawContent(canvas, content, width, height, entry.rotation);
        return renderRequestFlags;
    }

    protected int renderLabel(GLESCanvas canvas, AlbumSetEntry entry, int width, int height) {
        Texture content = checkLabelTexture(entry.labelTexture);
        if (content == null) {
            content = mWaitLoadingTexture;
        }
        int b = AlbumLabelMaker.getBorderSize();
        int h = mLabelSpec.labelBackgroundHeight;
        content.draw(canvas, -b, height - h + b, width + b + b, h);
        return 0;
    }

    protected int renderOverlay(GLESCanvas canvas, int index, int width, int height) {
        int renderRequestFlags = 0;

        if (mPressedIndex == index) {
            if (mAnimatePressedUp) {
                drawPressedUpFrame(canvas, width, height);
                renderRequestFlags |= ThumbnailView.RENDER_MORE_FRAME;
                if (isPressedUpFrameFinished()) {
                    mAnimatePressedUp = false;
                    mPressedIndex = -1;
                }
            } else {
                drawPressedFrame(canvas, width, height);
            }
        } else if ((mHighlightItemPath != null)) {
            drawSelectedFrame(canvas, width, height);
        } else if (mInSelectionMode) {
            drawSelectedFrame(canvas, width, height);
        }
        return renderRequestFlags;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void prepareDrawing() {
        mInSelectionMode = false;
    }

    public void pause() {
        mDataWindow.pause();
    }

    public void resume() {
        mDataWindow.resume();
    }

    @Override
    public void onVisibleRangeChanged(int visibleStart, int visibleEnd) {
        if (mDataWindow != null) {
            mDataWindow.setActiveWindow(visibleStart, visibleEnd);
        }
    }

    @Override
    public void onThumbnailSizeChanged(int width, int height) {
        if (mDataWindow != null) {
            mDataWindow.onThumbnailSizeChanged(width, height);
        }
    }

}
