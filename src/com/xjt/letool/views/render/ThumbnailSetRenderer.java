
package com.xjt.letool.views.render;

import android.graphics.Color;

import com.xjt.letool.LetoolBaseActivity;
import com.xjt.letool.datas.Path;
import com.xjt.letool.opengl.ColorTexture;
import com.xjt.letool.opengl.FadeInTexture;
import com.xjt.letool.opengl.GLESCanvas;
import com.xjt.letool.opengl.Texture;
import com.xjt.letool.opengl.TiledTexture;
import com.xjt.letool.opengl.UploadedTexture;
import com.xjt.letool.views.ThumbnailView;

public class ThumbnailSetRenderer extends AbstractThumbnailSRender {

    private static final String TAG = "ThumbnailRenderer";

    private static final int CACHE_SIZE = 96;
    private final int mPlaceholderColor;

    private final ColorTexture mWaitLoadingTexture;
    private LetoolBaseActivity mActivity;

    private ThumbnailView mThumbnailView;

    private int mPressedIndex = -1;
    private boolean mAnimatePressedUp;
    private Path mHighlightItemPath = null;
    private boolean mInSelectionMode;

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

    public ThumbnailSetRenderer(LetoolBaseActivity activity, ThumbnailView thumbnailView) {
        super(activity);
        mActivity = activity;
        mThumbnailView = thumbnailView;
        mPlaceholderColor = Color.GRAY;

        mWaitLoadingTexture = new ColorTexture(mPlaceholderColor);
        mWaitLoadingTexture.setSize(1, 1);
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

    public void setHighlightItemPath(Path path) {
        if (mHighlightItemPath == path)
            return;
        mHighlightItemPath = path;
        mThumbnailView.invalidate();
    }

    @Override
    public void prepareDrawing() {
        mInSelectionMode = true;
    }

    @Override
    public void onVisibleRangeChanged(int visibleStart, int visibleEnd) {

    }

    @Override
    public void onThumbnailSizeChanged(int width, int height) {

    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Texture checkContentTexture(Texture texture) {
        return ((texture instanceof TiledTexture) && !((TiledTexture) texture).isReady()) ? null : texture;
    }

    private static Texture checkLabelTexture(Texture texture) {
        return ((texture instanceof UploadedTexture) && ((UploadedTexture) texture).isUploading()) ? null : texture;
    }

    @Override
    public int renderThumbnail(GLESCanvas canvas, int index, int pass, int width, int height) {
        //AlbumSetEntry entry = mDataWindow.get(index);
        int renderRequestFlags = 0;
        renderRequestFlags |= renderContent(canvas, null, width, height);
        renderRequestFlags |= renderLabel(canvas, null, width, height);
        renderRequestFlags |= renderOverlay(canvas, index, null, width, height);
        return renderRequestFlags;
    }

    protected int renderContent(GLESCanvas canvas, Texture texture, int width, int height) {
        int renderRequestFlags = 0;
        Texture content = checkContentTexture(texture);
        if (content == null) {
            content = mWaitLoadingTexture;
        } else {

        }
        drawContent(canvas, content, width, height, 0);
        if ((content instanceof FadeInTexture) && ((FadeInTexture) content).isAnimating()) {
            renderRequestFlags |= ThumbnailView.RENDER_MORE_FRAME;
        }
        return renderRequestFlags;
    }

    protected int renderLabel(GLESCanvas canvas, Texture texture, int width, int height) {
        int renderRequestFlags = 0;
        Texture content = checkLabelTexture(texture);
        if (content == null) {
            content = mWaitLoadingTexture;
        }
        return renderRequestFlags;
    }

    protected int renderOverlay(GLESCanvas canvas, int index, Texture texture, int width, int height) {
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
    public void resume() {
        mThumbnailView.setThumbnailCount(100);
    }

    public void pause() {

    }
}
