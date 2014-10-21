
package com.xjt.newpic.views.render;

import com.xjt.newpic.NpContext;
import com.xjt.newpic.R;
import com.xjt.newpic.adapters.ThumbnailSetDataWindow;
import com.xjt.newpic.metadata.MediaPath;
import com.xjt.newpic.metadata.loader.ThumbnailSetDataLoader;
import com.xjt.newpic.views.ThumbnailView;
import com.xjt.newpic.views.layout.ThumbnailLayoutParam;
import com.xjt.newpic.views.opengl.ColorTexture;
import com.xjt.newpic.views.opengl.GLESCanvas;
import com.xjt.newpic.views.opengl.ResourceTexture;


/**
 * @Author Jituo.Xuan
 * @Date 8:17:16 PM Jul 24, 2014
 * @Comments:null
 */
public abstract class ThumbnailSetRenderer extends ThumbnailBaseRender {

    private static final String TAG = ThumbnailSetRenderer.class.getSimpleName();

    private static final int CACHE_SIZE = 48;

    protected NpContext mLetoolContext;

    protected ColorTexture mDefaulTexture;

    protected ResourceTexture mVideoPlayIcon;
    protected ResourceTexture mVideoOverlay;

    protected ThumbnailView mThumbnailView;
    protected ThumbnailSetDataWindow mDataWindow;

    protected int mPressedIndex = -1;
    protected boolean mAnimatePressedUp;
    protected MediaPath mHighlightItemPath = null;

    protected ThumbnailLabelParam mThumbnailLabelParam;
    protected ThumbnailLayoutParam mThumbnailParam;

    public static class ThumbnailLabelParam {

        public int labelHeight;
        public int backgroundColor;
        public int titleFontSize;
        public int countFontSize;
        public int titleColor;
        public int countColor;
        public int borderSize;
        public int gravity;
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

    protected ThumbnailSetRenderer(NpContext activity, ThumbnailView thumbnailView) {
        super(activity.getActivityContext());
        mLetoolContext = activity;
        mThumbnailView = thumbnailView;
        mDefaulTexture = new ColorTexture(activity.getActivityContext().getResources().getColor(R.color.cp_thumbnail_placehoder));
        mDefaulTexture.setSize(1, 1);
        mVideoPlayIcon = new ResourceTexture(activity.getActivityContext(), R.drawable.ic_video_folder);
        mVideoOverlay = new ResourceTexture(activity.getActivityContext(), R.drawable.ic_video_thumb);
    }

    public void setModel(ThumbnailSetDataLoader model) {
        if (mDataWindow != null) {
            mDataWindow.setListener(null);
            mDataWindow = null;
            mThumbnailView.setThumbnailCount(0);
        }
        if (model != null) {
            mDataWindow = new ThumbnailSetDataWindow(mLetoolContext, model, mThumbnailLabelParam, CACHE_SIZE);
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

    @Override
    public void prepareDrawing() {
    }

    public void pause() {
        mDataWindow.pause();
    }

    public void resume() {
        mDataWindow.resume();
    }

    protected void drawVideoOverlay(GLESCanvas canvas, int width, int height) {
        // Scale the video overlay to the height of the thumbnail and put it  on the left side.
//        ResourceTexture v = mVideoOverlay;
//        float scale = (float) height / v.getHeight();
//        int w = Math.round(scale * v.getWidth());
//        int h = Math.round(scale * v.getHeight());
//        v.draw(canvas, 0, 0, w, h);

        int s = Math.min(width, height) / 6;
        mVideoPlayIcon.draw(canvas, (height - s) / 2, (height - s) / 2, s, s);
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
