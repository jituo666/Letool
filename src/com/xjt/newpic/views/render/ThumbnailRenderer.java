
package com.xjt.newpic.views.render;

import com.xjt.newpic.NpContext;
import com.xjt.newpic.R;
import com.xjt.newpic.adapters.ThumbnailDataWindow;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.metadata.MediaPath;
import com.xjt.newpic.metadata.loader.ThumbnailDataLoader;
import com.xjt.newpic.selectors.SelectionManager;
import com.xjt.newpic.view.ThumbnailView;
import com.xjt.newpic.views.opengl.ColorTexture;
import com.xjt.newpic.views.opengl.GLESCanvas;
import com.xjt.newpic.views.opengl.ResourceTexture;
import com.xjt.newpic.views.opengl.Texture;

/**
 * @Author Jituo.Xuan
 * @Date 8:17:20 PM Jul 24, 2014
 * @Comments:null
 */
public class ThumbnailRenderer extends AbstractThumbnailRender {

    private static final String TAG = ThumbnailRenderer.class.getSimpleName();

    private ThumbnailDataWindow mDataWindow;
    private ThumbnailView mThumbnailView;
    private MediaPath mHighlightItemPath = null;
    private ThumbnailFilter mThumbnailFilter;

    private final ColorTexture mWaitLoadingTexture;
    protected final ResourceTexture mFramePreSelected;
    protected final ResourceTexture mFrameSelected;
    protected final ColorTexture mHalfTransparentLevel;

    private int mPressedIndex = -1;
    private boolean mAnimatePressedUp;
    private SelectionManager mMediaSelector;
    private boolean mInSelectionMode;
    private NpContext mFragment;

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

    public ThumbnailRenderer(NpContext context, ThumbnailView slotView, SelectionManager selector) {
        super(context.getActivityContext());
        mFragment = context;
        mThumbnailView = slotView;
        mMediaSelector = selector;
        mWaitLoadingTexture = new ColorTexture(context.getActivityContext().getResources().getColor(R.color.thumbnail_placehoder));
        mWaitLoadingTexture.setSize(1, 1);
        mFramePreSelected = new ResourceTexture(context.getActivityContext(), R.drawable.common_check_off);
        mFrameSelected = new ResourceTexture(context.getActivityContext(), R.drawable.common_check_on);
        mHalfTransparentLevel = new ColorTexture(0x80000000);
    }

    public void setModel(ThumbnailDataLoader model) {
        if (mDataWindow != null) {
            mDataWindow.setListener(null);
            mThumbnailView.setThumbnailCount(0);
            mDataWindow = null;
        }
        if (model != null) {
            mDataWindow = new ThumbnailDataWindow(mFragment, model);
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
        if (mDataWindow != null) {
            mDataWindow.setActiveWindow(visibleStart, visibleEnd);
        }
    }

    @Override
    public void onThumbnailSizeChanged(int width, int height) {
        // Do nothing
    }

    @Override
    public int renderThumbnail(GLESCanvas canvas, int index, int pass, int width, int height) {
        if (mThumbnailFilter != null && !mThumbnailFilter.acceptThumbnail(index))
            return 0;
        if (!mDataWindow.isActiveThumbnail(index))
            return 0;
        ThumbnailDataWindow.AlbumEntry entry = mDataWindow.get(index);

        int renderRequestFlags = 0;
        if (entry != null) {
            Texture content = entry.bitmapTexture;
            if (content == null) {
                content = mWaitLoadingTexture;
                entry.isWaitDisplayed = true;
            }
            drawContent(canvas, content, width, height, entry.rotation);
            renderRequestFlags |= renderOverlay(canvas, index, entry, width, height);
        }
        return renderRequestFlags;
    }

    private int renderOverlay(GLESCanvas canvas, int index, ThumbnailDataWindow.AlbumEntry entry, int width, int height) {
        int renderRequestFlags = 0;
        if (mPressedIndex == index) {
            if (mAnimatePressedUp) {
                drawPressedUpFrame(canvas, width, height);
                if (isPressedUpFrameFinished()) {
                    mAnimatePressedUp = false;
                    mPressedIndex = -1;
                    renderRequestFlags = 0;
                } else {
                    renderRequestFlags |= ThumbnailView.RENDER_MORE_FRAME;
                }
            } else {
                drawPressedFrame(canvas, width, height);
            }
        } else if ((entry.path != null) && (mHighlightItemPath == entry.path)) {
            drawSelectedFrame(canvas, width, height);
        } else if (mInSelectionMode) {
            if (mMediaSelector.isItemSelected(entry.path)) {
                drawSelectedFrame(canvas, width, height);
            } else {
                drawPreSelectedFrame(canvas, width, height);
            }
        }
        return renderRequestFlags;
    }

    protected void drawPreSelectedFrame(GLESCanvas canvas, int width, int height) {
        ColorTexture v = mHalfTransparentLevel;
        v.draw(canvas, 0, 0, width, height);
        int s = Math.min(width, height) / 3;
        mFramePreSelected.draw(canvas, width - s, height - s, s, s);
    }

    protected void drawSelectedFrame(GLESCanvas canvas, int width, int height) {
        ColorTexture v = mHalfTransparentLevel;
        v.draw(canvas, 0, 0, width, height);
        int s = Math.min(width, height) / 3;
        mFrameSelected.draw(canvas, width - s, height - s, s, s);
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
