
package com.xjt.letool.views.render;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.xjt.letool.R;
import com.xjt.letool.adapters.ThumbnailDataWindow;
import com.xjt.letool.common.LLog;
import com.xjt.letool.fragment.LetoolFragment;
import com.xjt.letool.metadata.MediaPath;
import com.xjt.letool.metadata.loader.ThumbnailDataLoader;
import com.xjt.letool.selectors.ContractSelector;
import com.xjt.letool.selectors.ExpandSelector;
import com.xjt.letool.view.ThumbnailView;
import com.xjt.letool.views.layout.ThumbnailExpandLayout.TimelineTag;
import com.xjt.letool.views.opengl.BitmapTexture;
import com.xjt.letool.views.opengl.ColorTexture;
import com.xjt.letool.views.opengl.GLESCanvas;
import com.xjt.letool.views.opengl.Texture;
import com.xjt.letool.views.opengl.UploadedBitmapTexture;
import com.xjt.letool.views.utils.AlbumSortTagMaker;

import java.util.ArrayList;

public class ThumbnailRendererWithTag extends AbstractThumbnailRender {

    private static final String TAG = ThumbnailRendererWithTag.class.getSimpleName();

    private ThumbnailDataWindow mDataWindow;
    private ThumbnailView mThumbnailView;
    private MediaPath mHighlightItemPath = null;
    private ThumbnailFilter mThumbnailFilter;

    private final ColorTexture mWaitLoadingTexture;
    private final int mPlaceholderColor = 0xFFE8E8E8;

    private int mPressedIndex = -1;
    private boolean mAnimatePressedUp;
    private ExpandSelector mMediaSelector;
    private boolean mInSelectionMode;
    private LetoolFragment mFragment;

    private final BitmapTexture mTagChecked;
    private final BitmapTexture mTagUnChecked;

    protected SortTagSpec mSortTagSpec;

    public interface ThumbnailFilter {

        public boolean acceptThumbnail(int index);
    }

    public static class SortTagSpec {

        public int titleFontSize;
        public int countFontSize;
        public int iconSize;
    }

    private class MyDataListener implements ThumbnailDataWindow.DataListener {

        @Override
        public void onContentChanged() {
            mThumbnailView.invalidate();
        }

        @Override
        public void onSizeChanged(int size, ArrayList<TimelineTag> tags) {
            mThumbnailView.setThumbnailCount(size, tags);
        }
    }

    public ThumbnailRendererWithTag(LetoolFragment fragment, ThumbnailView slotView, ExpandSelector selector) {
        this(fragment, slotView, selector, null);
    }

    public ThumbnailRendererWithTag(LetoolFragment fragment, ThumbnailView slotView, ExpandSelector selector, SortTagSpec sortTagSpec) {
        super(fragment.getActivity());
        mFragment = fragment;
        mThumbnailView = slotView;
        mMediaSelector = selector;
        mWaitLoadingTexture = new ColorTexture(mPlaceholderColor);
        mWaitLoadingTexture.setSize(1, 1);

        mSortTagSpec = sortTagSpec;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        mTagChecked = new BitmapTexture(BitmapFactory.decodeResource(fragment.getResources(),
                R.drawable.gg_bs_btn_checkbox_dark_on, options));
        mTagUnChecked = new BitmapTexture(BitmapFactory.decodeResource(fragment.getResources(),
                R.drawable.gg_bs_btn_checkbox_dark_off, options));

    }

    public void setModel(ThumbnailDataLoader model) {
        if (mDataWindow != null) {
            mDataWindow.setListener(null);
            mThumbnailView.setThumbnailCount(0);
            mDataWindow = null;
        }
        if (model != null) {
            mDataWindow = new ThumbnailDataWindow(mFragment, model, mSortTagSpec);
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
    public void onVisibleThumbnailRangeChanged(int visibleStart, int visibleEnd) {
        //LLog.i(TAG, "onVisibleRangeChanged visibleStart:" + visibleStart + " visibleEnd:" + visibleEnd);
        if (mDataWindow != null) {
            mDataWindow.setActiveWindow(visibleStart, visibleEnd);
        }
    }

    @Override
    public void onThumbnailSizeChanged(int width, int height) {
        // Do nothing
    }

    private static Texture checkTexture(Texture texture) {
        return (texture instanceof UploadedBitmapTexture) && ((UploadedBitmapTexture) texture).isUploading() ? null : texture;
    }

    @Override
    public int renderThumbnail(GLESCanvas canvas, int index, int pass, int width, int height) {
        if (mThumbnailFilter != null && !mThumbnailFilter.acceptThumbnail(index))
            return 0;
        ThumbnailDataWindow.AlbumEntry entry = mDataWindow.get(index);

        int renderRequestFlags = 0;

        Texture content = entry.compressTexture;
        if (content == null) {
            content = mWaitLoadingTexture;
            entry.isWaitDisplayed = true;
        }

        drawContent(canvas, content, width, height, entry.rotation);
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
        } else if (mInSelectionMode) {
            if (mMediaSelector.isItemSelected(entry.path, index)) {
                drawSelectedFrame(canvas, width, height);
            } else {
                drawPreSelectedFrame(canvas, width, height);
            }
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

    @Override
    public void initSortTagMetrics(int width, int height) {
        if (mDataWindow != null) {
            mDataWindow.setSortTagMetrics(width, height);
        }
    }

    @Override
    public void onVisibleTagRangeChanged(int visibleStart, int visibleEnd) {
        if (mDataWindow != null) {
            mDataWindow.setActiveTagWindow(visibleStart, visibleEnd);
        }
    }

    @Override
    public int renderSortTag(GLESCanvas canvas, int tagIndex, int width, int height) {
        Texture t = mDataWindow.getSortTagEntry(tagIndex).bitmapTexture;
        Texture content = checkTexture(t);
        if (content != null) {
            int b = AlbumSortTagMaker.getBorderSize();
            int h = content.getHeight();
            content.draw(canvas, -b, height - h + b, width + b + b, h);
        }
        if (mInSelectionMode) {
            TimelineTag tag = mDataWindow.getSortTag(tagIndex);
            canvas.translate(tag.pos.right - height, 0);
            if (tag.checked) {
                drawCheckedBox(canvas, height / 2, height / 2);
            } else {
                drawUncheckedBox(canvas, height / 2, height / 2);
            }
        }
        return 0;
    }

    protected void drawCheckedBox(GLESCanvas canvas, int width, int height) {
        drawFrame(canvas, mFramePressed.getPaddings(), mTagChecked, 0, 0, width, height);
    }

    protected void drawUncheckedBox(GLESCanvas canvas, int width, int height) {
        drawFrame(canvas, mFramePressed.getPaddings(), mTagUnChecked, 0, 0, width, height);
    }
}
