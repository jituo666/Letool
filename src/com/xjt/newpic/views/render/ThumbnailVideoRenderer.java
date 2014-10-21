
package com.xjt.newpic.views.render;

import com.xjt.newpic.NpContext;
import com.xjt.newpic.R;
import com.xjt.newpic.adapters.ThumbnailVideoDataWindow;
import com.xjt.newpic.adapters.ThumbnailVideoDataWindow.VideoEntry;
import com.xjt.newpic.metadata.MediaPath;
import com.xjt.newpic.metadata.loader.ThumbnailDataLoader;
import com.xjt.newpic.views.ThumbnailView;
import com.xjt.newpic.views.layout.ThumbnailLayoutParam;
import com.xjt.newpic.views.opengl.ColorTexture;
import com.xjt.newpic.views.opengl.GLESCanvas;
import com.xjt.newpic.views.opengl.ResourceTexture;
import com.xjt.newpic.views.opengl.Texture;
import com.xjt.newpic.views.opengl.UploadedBitmapTexture;
import com.xjt.newpic.views.render.ThumbnailSetRenderer.ThumbnailLabelParam;
import com.xjt.newpic.views.utils.ViewConfigs;

/**
 * @Author Jituo.Xuan
 * @Date 8:17:12 PM Jul 24, 2014
 * @Comments:null
 */
public class ThumbnailVideoRenderer extends AbstractThumbnailRender {

    private static final String TAG = ThumbnailVideoRenderer.class.getSimpleName();

    private static final int CACHE_SIZE = 48;

    private final ColorTexture mDefaulTexture;
    private final ResourceTexture mVideoOverlay;
    protected final ResourceTexture mVideoPlayIcon;
    protected final ResourceTexture mCameraPlayIcon;
    private NpContext mActivity;
    private boolean mIsCameraSource = false;

    private ThumbnailView mThumbnailView;
    private ThumbnailVideoDataWindow mDataWindow;
    private int mPadding = 0;
    protected ThumbnailLayoutParam mThumbnailParam;
    private int mPressedIndex = -1;
    private boolean mAnimatePressedUp;
    private MediaPath mHighlightItemPath = null;

    private ThumbnailLabelParam mLabelSpec;

    private class MyCacheListener implements ThumbnailVideoDataWindow.Listener {

        @Override
        public void onSizeChanged(int size) {
            mThumbnailView.setThumbnailCount(size);
        }

        @Override
        public void onContentChanged() {
            mThumbnailView.invalidate();
        }
    }

    public ThumbnailVideoRenderer(NpContext context, ThumbnailView thumbnailView, boolean isCameraSource) {
        super(context.getActivityContext());
        mActivity = context;
        mIsCameraSource = isCameraSource;
        mThumbnailView = thumbnailView;
        mThumbnailParam = ViewConfigs.VideoPage.get(context.getActivityContext()).videoSpec;
        mLabelSpec = ViewConfigs.VideoPage.get(context.getActivityContext()).labelSpec;
        mPadding = mThumbnailParam.thumbnailPadding;
        mDefaulTexture = new ColorTexture(context.getActivityContext().getResources().getColor(R.color.thumbnail_placehoder));
        mDefaulTexture.setSize(1, 1);
        mVideoOverlay = new ResourceTexture(context.getActivityContext(), R.drawable.ic_video_thumb);
        mVideoPlayIcon = new ResourceTexture(context.getActivityContext(), R.drawable.ic_movie_play);
        mCameraPlayIcon = new ResourceTexture(context.getActivityContext(), R.drawable.ic_video_play);
    }

    public void setModel(ThumbnailDataLoader model) {
        if (mDataWindow != null) {
            mDataWindow.setListener(null);
            mDataWindow = null;
            mThumbnailView.setThumbnailCount(0);
        }
        if (model != null) {
            mDataWindow = new ThumbnailVideoDataWindow(mActivity, model, mLabelSpec, CACHE_SIZE);
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
        return ((texture instanceof UploadedBitmapTexture) && ((UploadedBitmapTexture) texture).isUploading()) ? null : texture;
    }

    @Override
    public int renderThumbnail(GLESCanvas canvas, int index, int pass, int width, int height) {
        VideoEntry entry = mDataWindow.get(index);
        int renderRequestFlags = 0;
        if (entry != null) {
            // 缩小绘制整体padding大小
            canvas.translate(mLabelSpec.labelHeight / 4 + mLabelSpec.labelHeight / 6, mLabelSpec.labelHeight / 4);
            width = width - mLabelSpec.labelHeight / 2 - mLabelSpec.labelHeight / 3;
            //
            height = height - mLabelSpec.labelHeight / 2;
            //开始绘制
            renderRequestFlags |= renderContent(canvas, entry, width, width); // 图片内容
            renderRequestFlags |= renderLabel(canvas, entry, width - 2 * mPadding, height); // 标签
            renderRequestFlags |= renderOverlay(canvas, entry, index, width, width); // 按下效果
        }
        return renderRequestFlags;
    }

    protected int renderContent(GLESCanvas canvas, VideoEntry entry, int width, int height) {
        int renderRequestFlags = 0;
        Texture content = entry.bitmapTexture;
        if (content == null) {
            content = mDefaulTexture;
            entry.isWaitLoadingDisplayed = true;
        }
        drawContent(canvas, content, width - mPadding, height - mPadding, entry.rotation, mPadding);
        return renderRequestFlags;
    }

    protected int renderLabel(GLESCanvas canvas, VideoEntry entry, int width, int height) {
        canvas.translate(mPadding / 2, mPadding * 2);
        Texture content = checkLabelTexture(entry.labelTexture);
        if (content == null) {
            content = mDefaulTexture;
            return 0;
        }
        int h = mLabelSpec.labelHeight;
        content.draw(canvas, 0, height - h, width, h);
        canvas.translate(-mPadding / 2, -mPadding * 2);
        return 0;
    }

    protected int renderOverlay(GLESCanvas canvas, VideoEntry entry, int index, int width, int height) {
        int renderRequestFlags = 0;
        canvas.translate(mPadding / 2, mPadding / 2);
        if (mPressedIndex == index) {
            if (mAnimatePressedUp) {
                drawPressedUpFrame(canvas, width - mPadding, height - mPadding);
                renderRequestFlags |= ThumbnailView.RENDER_MORE_FRAME;
                if (isPressedUpFrameFinished()) {
                    mAnimatePressedUp = false;
                    mPressedIndex = -1;
                }
            } else {
                drawPressedFrame(canvas, width - mPadding, height - mPadding);
            }
        }
        canvas.translate(-mPadding / 2, -mPadding / 2);
        drawVideoOverlay(canvas, width, height);
        return renderRequestFlags;
    }

    protected void drawVideoOverlay(GLESCanvas canvas, int width, int height) {
        // Scale the video overlay to the height of the thumbnail and put it  on the left side.

        ResourceTexture v = mVideoOverlay;
        float scale = (float) height / v.getHeight();
        int w = Math.round(scale * v.getWidth());
        int h = Math.round(scale * v.getHeight());
        v.draw(canvas, 0, 0, w, height);

        int s = Math.min(width, height) / 6;
        if (mIsCameraSource) {
            mCameraPlayIcon.draw(canvas, (width - s) / 2, (height - s) / 2, s, s);
        } else {
            mVideoPlayIcon.draw(canvas, (width - s) / 2, (height - s) / 2, s, s);
        }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void prepareDrawing() {
    }

    public void pause() {
        mDataWindow.pause();
    }

    public void resume() {
        mDataWindow.resume();
    }

    @Override
    public void onVisibleThumbnailRangeChanged(int visibleStart, int visibleEnd) {
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
