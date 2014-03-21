package com.xjt.letool.opengl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import com.xjt.letool.utils.Utils;

import junit.framework.Assert;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLU;
import android.opengl.Matrix;

public class GLES11Canvas implements GLESCanvas {

    private static final String TAG = GLES20Canvas.class.getSimpleName();

    private static final float OPAQUE_ALPHA = 0.95f;

    private GL11 mGL;
    private GLState mGLState;
    private float mAlpha;
    private final ArrayList<ConfigState> mRestoreStack = new ArrayList<ConfigState>();
    private ConfigState mRecycledRestoreAction;
    private final float mMatrixValues[] = new float[16];
    private final float mTextureMatrixValues[] = new float[16];
    private final float mTempMatrix[] = new float[32];

    public GLES11Canvas(GL11 gl) {
        mGL = gl;
        mGLState = new GLState(gl);

        gl.glVertexPointer(2, GL11.GL_FLOAT, 0, 0);
        gl.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);

        // Enable the texture coordinate array for Texture 1
        gl.glClientActiveTexture(GL11.GL_TEXTURE1);
        gl.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
        gl.glClientActiveTexture(GL11.GL_TEXTURE0);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    @Override
    public void setSize(int width, int height) {
        Assert.assertTrue(width >= 0 && height >= 0);
        GL11 gl = mGL;
        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL11.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluOrtho2D(gl, 0, width, 0, height);

        gl.glMatrixMode(GL11.GL_MODELVIEW);
        gl.glLoadIdentity();

        float matrix[] = mMatrixValues;
        Matrix.setIdentityM(matrix, 0);
    }

    @Override
    public void clearBuffer() {
        clearBuffer(null);
    }

    @Override
    public void clearBuffer(float[] argb) {
        if (argb != null && argb.length == 4) {
            mGL.glClearColor(argb[1], argb[2], argb[3], argb[0]);
        } else {
            mGL.glClearColor(0, 0, 0, 1);
        }
        mGL.glClear(GL10.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void setAlpha(float alpha) {
        Assert.assertTrue(alpha >= 0 && alpha <= 1);
        mAlpha = alpha;
    }

    @Override
    public float getAlpha() {
        return mAlpha;
    }

    @Override
    public void multiplyAlpha(float alpha) {
        Assert.assertTrue(alpha >= 0 && alpha <= 1);
        mAlpha *= alpha;
    }

    @Override
    public void translate(float x, float y, float z) {
        // TODO Auto-generated method stub

    }

    @Override
    public void translate(float x, float y) {
        // TODO Auto-generated method stub

    }

    @Override
    public void scale(float sx, float sy, float sz) {
        // TODO Auto-generated method stub

    }

    @Override
    public void rotate(float angle, float x, float y, float z) {
        // TODO Auto-generated method stub

    }

    @Override
    public void multiplyMatrix(float[] matrix, int offset) {
        float[] temp = mTempMatrix;
        Matrix.multiplyMM(temp, 0, mMatrixValues, 0, matrix, offset);
        System.arraycopy(temp, 0, mMatrixValues, 0, 16);
    }

    @Override
    public void save() {
        save(SAVE_FLAG_ALL);
    }

    @Override
    public void save(int saveFlags) {
        ConfigState config = obtainRestoreConfig();

        if ((saveFlags & SAVE_FLAG_ALPHA) != 0) {
            config.mAlpha = mAlpha;
        } else {
            config.mAlpha = -1;
        }

        if ((saveFlags & SAVE_FLAG_MATRIX) != 0) {
            System.arraycopy(mMatrixValues, 0, config.mMatrix, 0, 16);
        } else {
            config.mMatrix[0] = Float.NEGATIVE_INFINITY;
        }

        mRestoreStack.add(config);
    }

    @Override
    public void restore() {
        if (mRestoreStack.isEmpty())
            throw new IllegalStateException();
        ConfigState config = mRestoreStack.remove(mRestoreStack.size() - 1);
        config.restore(this);
        freeRestoreConfig(config);
    }

    @Override
    public void drawLine(float x1, float y1, float x2, float y2, GLPaint paint) {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawRect(float x1, float y1, float x2, float y2, GLPaint paint) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fillRect(float x, float y, float width, float height, int color) {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawTexture(Texture texture, int x, int y, int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawMesh(Texture tex, int x, int y, int xyBuffer, int uvBuffer, int indexBuffer, int indexCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawTexture(Texture texture, RectF source, RectF target) {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawTexture(Texture texture, float[] mTextureTransform, int x, int y, int w, int h) {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawMixed(Texture from, int toColor, float ratio, int x, int y, int w, int h) {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawMixed(Texture from, int toColor, float ratio, RectF src, RectF target) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean unloadTexture(Texture texture) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void deleteBuffer(int bufferId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteRecycledResources() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dumpStatisticsAndClear() {
        // TODO Auto-generated method stub

    }

    @Override
    public void beginRenderTarget(Texture texture) {
        // TODO Auto-generated method stub

    }

    @Override
    public void endRenderTarget() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTextureParameters(Texture texture) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initializeTextureSize(Texture texture, int format, int type) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initializeTexture(Texture texture, Bitmap bitmap) {
        // TODO Auto-generated method stub

    }

    @Override
    public void texSubImage2D(Texture texture, int xOffset, int yOffset, Bitmap bitmap, int format, int type) {
        // TODO Auto-generated method stub

    }

    @Override
    public int uploadBuffer(FloatBuffer buffer) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int uploadBuffer(ByteBuffer buffer) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void recoverFromLightCycle() {
        // TODO Auto-generated method stub

    }

    @Override
    public void getBounds(Rect bounds, int x, int y, int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public GLId getGLId() {
        return null;
    }

    private ConfigState obtainRestoreConfig() {
        if (mRecycledRestoreAction != null) {
            ConfigState result = mRecycledRestoreAction;
            mRecycledRestoreAction = result.mNextFree;
            return result;
        }
        return new ConfigState();
    }

    private void freeRestoreConfig(ConfigState action) {
        action.mNextFree = mRecycledRestoreAction;
        mRecycledRestoreAction = action;
    }

    private static class GLState {

        private final GL11 mGL;

        private int mTexEnvMode = GL11.GL_REPLACE;
        private float mTextureAlpha = 1.0f;
        private int mTextureTarget = GL11.GL_TEXTURE_2D;
        private boolean mBlendEnabled = true;
        private float mLineWidth = 1.0f;

        public GLState(GL11 gl) {
            mGL = gl;

            gl.glDisable(GL11.GL_LIGHTING);
            gl.glEnable(GL11.GL_DITHER);

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            gl.glEnable(GL11.GL_TEXTURE_2D);

            gl.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);

            // Set the background color
            gl.glClearColor(0f, 0f, 0f, 0f);

            gl.glEnable(GL11.GL_BLEND);
            gl.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

            // We use 565 or 8888 format, so set the alignment to 2 bytes/pixel.
            gl.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 2);
        }

        public void setTexEnvMode(int mode) {
            if (mTexEnvMode == mode)
                return;
            mTexEnvMode = mode;
            mGL.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, mode);
        }

        public void setLineWidth(float width) {
            if (mLineWidth == width)
                return;
            mLineWidth = width;
            mGL.glLineWidth(width);
        }

        public void setTextureAlpha(float alpha) {
            if (mTextureAlpha == alpha)
                return;
            mTextureAlpha = alpha;
            if (alpha >= OPAQUE_ALPHA) {
                // The alpha is need for those texture without alpha channel
                mGL.glColor4f(1, 1, 1, 1);
                setTexEnvMode(GL11.GL_REPLACE);
            } else {
                mGL.glColor4f(alpha, alpha, alpha, alpha);
                setTexEnvMode(GL11.GL_MODULATE);
            }
        }

        public void setColorMode(int color, float alpha) {
            setBlendEnabled(!Utils.isOpaque(color) || alpha < OPAQUE_ALPHA);

            // Set mTextureAlpha to an invalid value, so that it will reset
            // again in setTextureAlpha(float) later.
            mTextureAlpha = -1.0f;

            setTextureTarget(0);

            float prealpha = (color >>> 24) * alpha * 65535f / 255f / 255f;
            mGL.glColor4x(
                    Math.round(((color >> 16) & 0xFF) * prealpha),
                    Math.round(((color >> 8) & 0xFF) * prealpha),
                    Math.round((color & 0xFF) * prealpha),
                    Math.round(255 * prealpha));
        }

        // target is a value like GL_TEXTURE_2D. If target = 0, texturing is disabled.
        public void setTextureTarget(int target) {
            if (mTextureTarget == target)
                return;
            if (mTextureTarget != 0) {
                mGL.glDisable(mTextureTarget);
            }
            mTextureTarget = target;
            if (mTextureTarget != 0) {
                mGL.glEnable(mTextureTarget);
            }
        }

        public void setBlendEnabled(boolean enabled) {
            if (mBlendEnabled == enabled)
                return;
            mBlendEnabled = enabled;
            if (enabled) {
                mGL.glEnable(GL11.GL_BLEND);
            } else {
                mGL.glDisable(GL11.GL_BLEND);
            }
        }
    }

    private static class ConfigState {
        float mAlpha;
        float mMatrix[] = new float[16];
        ConfigState mNextFree;

        public void restore(GLES11Canvas canvas) {
            if (mAlpha >= 0)
                canvas.setAlpha(mAlpha);
            if (mMatrix[0] != Float.NEGATIVE_INFINITY) {
                System.arraycopy(mMatrix, 0, canvas.mMatrixValues, 0, 16);
            }
        }
    }
}
