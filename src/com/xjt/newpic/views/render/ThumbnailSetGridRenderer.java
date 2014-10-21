
package com.xjt.newpic.views.render;

import com.xjt.newpic.NpContext;
import com.xjt.newpic.R;
import com.xjt.newpic.adapters.ThumbnailSetDataWindow.AlbumSetEntry;
import com.xjt.newpic.selectors.SelectionManager;
import com.xjt.newpic.views.ThumbnailView;
import com.xjt.newpic.views.opengl.GLESCanvas;
import com.xjt.newpic.views.opengl.ResourceTexture;
import com.xjt.newpic.views.opengl.Texture;
import com.xjt.newpic.views.opengl.UploadedBitmapTexture;
import com.xjt.newpic.views.utils.ViewConfigs;

public class ThumbnailSetGridRenderer extends ThumbnailSetRenderer {

    private ResourceTexture mBorderTexture;
    private int mPadding = 0;

    public ThumbnailSetGridRenderer(NpContext activity, ThumbnailView thumbnailView, SelectionManager selector) {
        super(activity, thumbnailView);
        mThumbnailLabelParam = ViewConfigs.AlbumSetGridPage.get(activity.getActivityContext()).labelSpec;
        mThumbnailParam = ViewConfigs.AlbumSetGridPage.get(activity.getActivityContext()).albumSetGridSpec;
        mPadding = mThumbnailParam.thumbnailPadding;
        mBorderTexture = new ResourceTexture(activity.getActivityContext(), R.drawable.ic_gallery_border);
    }

    protected static Texture checkLabelTexture(Texture texture) {
        return ((texture instanceof UploadedBitmapTexture) && ((UploadedBitmapTexture) texture).isUploading()) ? null : texture;
    }

    @Override
    public int renderThumbnail(GLESCanvas canvas, int index, int pass, int width, int height) {
        AlbumSetEntry entry = mDataWindow.get(index);
        int renderRequestFlags = 0;
        if (entry != null) {
            // 缩小绘制整体padding大小
            canvas.translate(mThumbnailLabelParam.labelHeight / 4 + mThumbnailLabelParam.labelHeight / 6, mThumbnailLabelParam.labelHeight / 4);
            width = width - mThumbnailLabelParam.labelHeight / 2 - mThumbnailLabelParam.labelHeight / 3;
            //
            height = height - mThumbnailLabelParam.labelHeight / 2;
            //开始绘制
            mBorderTexture.draw(canvas, 0, 0, width, height - mThumbnailLabelParam.labelHeight); // 相框
            renderRequestFlags |= renderContent(canvas, entry, width, height - mThumbnailLabelParam.labelHeight); // 图片内容
            renderRequestFlags |= renderLabel(canvas, entry, width, height); // 标签
            renderRequestFlags |= renderOverlay(canvas, entry, index, width, height - mThumbnailLabelParam.labelHeight); // 按下效果
        }
        return renderRequestFlags;
    }

    protected int renderContent(GLESCanvas canvas, AlbumSetEntry entry, int width, int height) {
        int renderRequestFlags = 0;
        Texture content = entry.bitmapTexture;
        if (content == null) {
            content = mDefaulTexture;
            entry.isWaitLoadingDisplayed = true;
        }
        drawContent(canvas, content, width - mPadding, height - mPadding, entry.rotation,
                mPadding);
        return renderRequestFlags;
    }

    protected int renderLabel(GLESCanvas canvas, AlbumSetEntry entry, int width, int height) {
        Texture content = checkLabelTexture(entry.labelTexture);
        if (content == null) {
            content = mDefaulTexture;
            return 0;
        }
        int h = mThumbnailLabelParam.labelHeight;
        content.draw(canvas, 0, height - h, width, h);
        return 0;
    }

    protected int renderOverlay(GLESCanvas canvas, AlbumSetEntry entry, int index, int width, int height) {
        int renderRequestFlags = 0;
        if (mPressedIndex == index) {
            canvas.translate(mPadding / 2, mPadding / 2);
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
            canvas.translate(-mPadding / 2, -mPadding / 2);
        }
        if (!mLetoolContext.isImageBrwosing())
            drawVideoOverlay(canvas, width, height);
        return renderRequestFlags;
    }

}
