
package com.xjt.letool.views.opengl;

import android.opengl.ETC1Util.ETC1Texture;
import android.opengl.ETC1;
import android.opengl.GLES20;

import com.xjt.letool.common.LLog;

import junit.framework.Assert;


import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public abstract class UploadETC1Texture extends BasicTexture {

    private static final String TAG = UploadETC1Texture.class.getSimpleName();

    @SuppressWarnings("unused")
    private boolean mContentValid = true;
    private boolean mIsUploading = false; // indicate this textures is being uploaded in background
    private boolean mOpaque = true;
    private boolean mThrottled = false;
    private static int sUploadedCount;
    private static final int UPLOAD_LIMIT = 100;

    protected ETC1Texture mETC1Texture;

    protected UploadETC1Texture() {
        this(null);
    }
    
    protected UploadETC1Texture(ETC1Texture texture) {
        super(null, 0, STATE_UNLOADED);
    }

    protected void setIsUploading(boolean uploading) {
        mIsUploading = uploading;
    }

    public boolean isUploading() {
        return mIsUploading;
    }

    protected void setThrottled(boolean throttled) {
        mThrottled = throttled;
    }


    private ETC1Texture getETC1Texture() {
        if (mETC1Texture == null) {
            mETC1Texture = onGetETC1Texture();
            int w = mETC1Texture.getWidth();
            int h = mETC1Texture.getHeight();
            if (mWidth == UNSPECIFIED) {
                setSize(w, h);
            }
        }
        return mETC1Texture;
    }

    private void freeETC1Texture() {
        Assert.assertTrue(mETC1Texture != null);
        onFreeETC1Texture(mETC1Texture);
        mETC1Texture = null;
    }

    @Override
    public int getWidth() {
        if (mWidth == UNSPECIFIED)
            getETC1Texture();
        return mWidth;
    }

    @Override
    public int getHeight() {
        if (mWidth == UNSPECIFIED)
            getETC1Texture();
        return mHeight;
    }

    protected abstract ETC1Texture onGetETC1Texture();

    protected abstract void onFreeETC1Texture(ETC1Texture etc1Texture);

    protected void invalidateContent() {
        if (mETC1Texture != null)
            freeETC1Texture();
        mContentValid = false;
        mWidth = UNSPECIFIED;
        mHeight = UNSPECIFIED;
    }

    /**
     * Whether the content on GPU is valid.
     */
    public boolean isContentValid() {
        return isLoaded() && mContentValid;
    }

    /**
     * Updates the content on GPU's memory.
     * @param canvas
     */
    public void updateContent(GLESCanvas canvas) {
        if (!isLoaded()) {
            if (mThrottled && ++sUploadedCount > UPLOAD_LIMIT) {
                return;
            }
            uploadToCanvas(canvas);
        } else if (!mContentValid) {
            ETC1Texture texture = getETC1Texture();
            GLES20.glCompressedTexImage2D(GL10.GL_TEXTURE_2D, 0, ETC1.ETC1_RGB8_OES, texture.getWidth(), texture.getHeight(), 0, texture.getData().capacity(), texture.getData());
            freeETC1Texture();
            mContentValid = true;
        }
    }

    public static void resetUploadLimit() {
        sUploadedCount = 0;
    }

    public static boolean uploadLimitReached() {
        return sUploadedCount > UPLOAD_LIMIT;
    }

    private void uploadToCanvas(GLESCanvas canvas) {

        ETC1Texture texture = getETC1Texture();
        if (texture != null) {
            try {
                int bWidth = texture.getWidth();
                int bHeight = texture.getHeight();
                int texWidth = getTextureWidth();
                int texHeight = getTextureHeight();
                Assert.assertTrue(bWidth <= texWidth && bHeight <= texHeight);
                // Upload the etc1Texture to a new texture.
                mId = canvas.getGLId().generateTexture();
                canvas.setTextureParameters(this);
                canvas.initializeTexture(this, texture);
            } finally {
                freeETC1Texture();
            }
            // Update texture state.
            setAssociatedCanvas(canvas);
            mState = STATE_LOADED;
            mContentValid = true;
        } else {
            mState = STATE_ERROR;
            throw new RuntimeException("Texture load fail, no etc1Texture");
        }
    }

    @Override
    protected boolean onBind(GLESCanvas canvas) {
        updateContent(canvas);
        return isContentValid();
    }

    @Override
    protected int getTarget() {
        return GL11.GL_TEXTURE_2D;
    }

    public void setOpaque(boolean isOpaque) {
        mOpaque = isOpaque;
    }

    @Override
    public boolean isOpaque() {
        return mOpaque;
    }

    @Override
    public void recycle() {
        super.recycle();
        if (mETC1Texture != null)
            freeETC1Texture();
    }

}
