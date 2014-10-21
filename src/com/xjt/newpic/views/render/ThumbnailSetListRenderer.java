
package com.xjt.newpic.views.render;

import com.xjt.newpic.NpContext;
import com.xjt.newpic.R;
import com.xjt.newpic.adapters.ThumbnailSetDataWindow.AlbumSetEntry;
import com.xjt.newpic.selectors.SelectionManager;
import com.xjt.newpic.views.ThumbnailView;
import com.xjt.newpic.views.opengl.ColorTexture;
import com.xjt.newpic.views.opengl.GLESCanvas;
import com.xjt.newpic.views.opengl.Texture;
import com.xjt.newpic.views.opengl.UploadedBitmapTexture;
import com.xjt.newpic.views.utils.ViewConfigs;

public class ThumbnailSetListRenderer extends ThumbnailSetRenderer {

    protected ColorTexture mListBorderTexture;
    protected ColorTexture mListDividerTexture;

    public ThumbnailSetListRenderer(NpContext activity, ThumbnailView thumbnailView, SelectionManager selector) {
        super(activity, thumbnailView);
        mThumbnailLabelParam = ViewConfigs.AlbumSetListPage.get(activity.getActivityContext()).labelSpec;
        mThumbnailParam = ViewConfigs.AlbumSetListPage.get(activity.getActivityContext()).albumSetListSpec;
        if (!mLetoolContext.isImageBrwosing()) {
            mThumbnailParam.thumbnailPadding = 0;
        }
        mListDividerTexture = new ColorTexture(activity.getActivityContext().getResources().getColor(R.color.cp_list_divider_color));
        mListDividerTexture.setSize(1, 1);
        mListBorderTexture = new ColorTexture(activity.getActivityContext().getResources().getColor(R.color.cp_gallery_list_item_border));
        mListBorderTexture.setSize(1, 1);
    }

    protected static Texture checkLabelTexture(Texture texture) {
        return ((texture instanceof UploadedBitmapTexture) && ((UploadedBitmapTexture) texture).isUploading()) ? null : texture;
    }

    @Override
    public int renderThumbnail(GLESCanvas canvas, int index, int pass, int width, int height) {
        AlbumSetEntry entry = mDataWindow.get(index);
        int renderRequestFlags = 0;
        if (entry != null) {
            renderRequestFlags |= renderOverlay(canvas, entry, index, width, height);
            renderRequestFlags |= renderContent(canvas, entry, height, height);
            renderRequestFlags |= renderLabel(canvas, entry, width - height, height);
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
        mListBorderTexture.draw(canvas, 0, 0, width, height);
        drawContent(canvas, content, width - mThumbnailParam.thumbnailPadding, height - mThumbnailParam.thumbnailPadding, entry.rotation,
                mThumbnailParam.thumbnailPadding);
        return renderRequestFlags;
    }

    protected int renderLabel(GLESCanvas canvas, AlbumSetEntry entry, int width, int height) {
        Texture content = checkLabelTexture(entry.labelTexture);
        if (content == null) {
            content = mDefaulTexture;
            return 0;
        }
        content.draw(canvas, height, height / 4, width, height / 2);
        if (!mLetoolContext.isImageBrwosing())
            drawVideoOverlay(canvas, width, height);
        return 0;
    }

    protected int renderOverlay(GLESCanvas canvas, AlbumSetEntry entry, int index, int width, int height) {
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
        }
        mListDividerTexture.draw(canvas, 0, height + mThumbnailLabelParam.borderSize / 3, width, 1);
        return renderRequestFlags;
    }
}
