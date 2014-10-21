
package com.xjt.newpic.views.render;

import android.content.Context;
import android.graphics.Rect;

import com.xjt.newpic.R;
import com.xjt.newpic.views.ThumbnailView;
import com.xjt.newpic.views.opengl.FadeOutTexture;
import com.xjt.newpic.views.opengl.GLESCanvas;
import com.xjt.newpic.views.opengl.NinePatchTexture;
import com.xjt.newpic.views.opengl.Texture;

/**
 * @Author Jituo.Xuan
 * @Date 8:17:02 PM Jul 24, 2014
 * @Comments:null
 */
public abstract class ThumbnailBaseRender implements ThumbnailView.Renderer {

    private static final String TAG = ThumbnailBaseRender.class.getSimpleName();

    protected final NinePatchTexture mFramePressed;
    private FadeOutTexture mFramePressedUp;

    protected ThumbnailBaseRender(Context context) {
        mFramePressed = new NinePatchTexture(context, R.drawable.grid_pressed);
    }

    protected void drawContent(GLESCanvas canvas, Texture content, int width, int height, int rotation, int expandPadding) {
        canvas.save(GLESCanvas.SAVE_FLAG_MATRIX);

        if (rotation != 0) {
            int value = Math.min((width) / 2, (height) / 2); // 纠正绘制时的宽高不等问题
            value = value + expandPadding / 2;
            canvas.translate(value, value);
            canvas.rotate(rotation, 0, 0, 1);
            value = value - expandPadding / 2;
            canvas.translate(-value, -value);
            if (((rotation % 90) & 1) != 0) {
                height = width;
                width = height;
            }
            //Log.i(TAG, "----------drawContent with rotation:" + rotation);
        } else if (expandPadding > 0) {
            canvas.translate(expandPadding / 2, expandPadding / 2);
        }
        // Fit the content into the box
        float scale = Math.min((float) width / content.getWidth(), (float) height / content.getHeight());
        canvas.scale(scale, scale, scale);
        content.draw(canvas, 0, 0);
        canvas.restore();
    }

    protected void drawContent(GLESCanvas canvas, Texture content, int width, int height, int rotation) {
        canvas.save(GLESCanvas.SAVE_FLAG_MATRIX);
        // The content is always rendered in to the largest square that fits inside the thumbnail, aligned to the top of the thumbnail.
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

    protected boolean isPressedUpFrameFinished() {
        if (mFramePressedUp != null) {
            if (mFramePressedUp.isAnimating()) {
                return false;
            } else {
                mFramePressedUp = null;
            }
        }
        return true;
    }

    protected void drawPressedUpFrame(GLESCanvas canvas, int width, int height) {
        if (mFramePressedUp == null) {
            mFramePressedUp = new FadeOutTexture(mFramePressed);
        }
        drawFrame(canvas, mFramePressed.getPaddings(), mFramePressedUp, 0, 0, width, height);
    }

    protected void drawPressedFrame(GLESCanvas canvas, int width, int height) {
        drawFrame(canvas, mFramePressed.getPaddings(), mFramePressed, 0, 0, width, height);
    }

    protected static void drawFrame(GLESCanvas canvas, Rect padding, Texture frame,
            int x, int y, int width, int height) {
        frame.draw(canvas, x - padding.left, y - padding.top, width + padding.left + padding.right,
                height + padding.top + padding.bottom);
    }
}
