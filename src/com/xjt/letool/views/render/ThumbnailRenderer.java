package com.xjt.letool.views.render;

import android.graphics.Color;

import com.xjt.letool.adapters.ThumbnailDataWindow;
import com.xjt.letool.common.LLog;
import com.xjt.letool.data.MediaPath;
import com.xjt.letool.data.loader.ThumbnailDataLoader;
import com.xjt.letool.selectors.SelectionManager;
import com.xjt.letool.view.ThumbnailView;
import com.xjt.letool.views.fragment.LetoolFragment;
import com.xjt.letool.views.opengl.ColorTexture;
import com.xjt.letool.views.opengl.FadeInTexture;
import com.xjt.letool.views.opengl.GLESCanvas;
import com.xjt.letool.views.opengl.Texture;
import com.xjt.letool.views.opengl.TiledTexture;

public class ThumbnailRenderer extends AbstractThumbnailRender {

    private static final String TAG = "ThumbnailViewRender";

    private ThumbnailDataWindow mDataWindow;
    private ThumbnailView mThumbnailView;
    private MediaPath mHighlightItemPath = null;
    private ThumbnailFilter mThumbnailFilter;

    private static final int PLACE_HOLDER_COLOR = 0xFF222222;
    private static final int CACHE_SIZE = 96;

    private final ColorTexture mWaitLoadingTexture;
    private final int mPlaceholderColor = Color.GRAY;

    private int mPressedIndex = -1;
    private boolean mAnimatePressedUp;
    private SelectionManager mMediaSelector;
    private boolean mInSelectionMode;
    private LetoolFragment mFragment;

    public interface ThumbnailFilter {
        public boolean acceptThumbnail(int index);
    }

    private class MyDataListener implements ThumbnailDataWindow.DataListener {
        @Override
        public void onContentChanged() {
            mThumbnailView.invalidate();
        }

        @Override
        public void onSizeChanged(int size) {
            mThumbnailView.setThumbnailCount(size);
        }
    }

    public ThumbnailRenderer(LetoolFragment fragment, ThumbnailView slotView, SelectionManager selector) {
        super(fragment.getActivity());
        mFragment = fragment;
        mThumbnailView = slotView;
        mMediaSelector = selector;
        mWaitLoadingTexture = new ColorTexture(mPlaceholderColor);
        mWaitLoadingTexture.setSize(1, 1);
    }

    public void setModel(ThumbnailDataLoader model) {
        if (mDataWindow != null) {
            mDataWindow.setListener(null);
            mThumbnailView.setThumbnailCount(0);
            mDataWindow = null;
        }
        if (model != null) {
            mDataWindow = new ThumbnailDataWindow(mFragment, model, CACHE_SIZE);
            mDataWindow.setListener(new MyDataListener());
            mThumbnailView.setThumbnailCount(model.size());
        }
    }

    public void setHighlightItemPath(MediaPath path) {
        if (mHighlightItemPath == path)
            return;
        mHighlightItemPath = path;
        mThumbnailView.invalidate();
    }

    public void setThumbnailFilter(ThumbnailFilter slotFilter) {
        mThumbnailFilter = slotFilter;
    }

    @Override
    public void prepareDrawing() {
        mInSelectionMode = mMediaSelector.inSelectionMode();
    }

    @Override
    public void onVisibleRangeChanged(int visibleStart, int visibleEnd) {

        //LLog.i(TAG, "onVisibleRangeChanged visibleStart:" + visibleStart + " visibleEnd:" + visibleEnd);
        if (mDataWindow != null) {
            mDataWindow.setActiveWindow(visibleStart, visibleEnd);
        }
    }


    @Override
    public void onThumbnailSizeChanged(int width, int height) {
        // Donothing
    }
    
    private static Texture checkTexture(Texture texture) {
        return (texture instanceof TiledTexture) && !((TiledTexture) texture).isReady() ? null : texture;
    }

    @Override
    public int renderThumbnail(GLESCanvas canvas, int index, int pass, int width, int height) {
        if (mThumbnailFilter != null && !mThumbnailFilter.acceptThumbnail(index))
            return 0;

        ThumbnailDataWindow.AlbumEntry entry = mDataWindow.get(index);

        int renderRequestFlags = 0;

        Texture content = checkTexture(entry.content);
        if (content == null) {
            content = mWaitLoadingTexture;
            entry.isWaitDisplayed = true;
        } else if (entry.isWaitDisplayed) {
            entry.isWaitDisplayed = false;
            content = new FadeInTexture(mPlaceholderColor, entry.bitmapTexture);
            entry.content = content;
        }

        drawContent(canvas, content, width, height, entry.rotation);
        if ((content instanceof FadeInTexture) && ((FadeInTexture) content).isAnimating()) {
            renderRequestFlags |= ThumbnailView.RENDER_MORE_FRAME;
        }
        //
        renderRequestFlags |= renderOverlay(canvas, index, entry, width, height);

        return renderRequestFlags;
    }

    private int renderOverlay(GLESCanvas canvas, int index, ThumbnailDataWindow.AlbumEntry entry, int width, int height) {
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
        } else if ((entry.path != null) && (mHighlightItemPath == entry.path)) {
            drawSelectedFrame(canvas, width, height);
        } else if (mInSelectionMode && mMediaSelector.isItemSelected(entry.path)) {
            drawSelectedFrame(canvas, width, height);
        }
        return renderRequestFlags;
    }

    //
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

    public void resume() {
        mDataWindow.resume();
    }

    public void pause() {
        mDataWindow.pause();
    }
}
