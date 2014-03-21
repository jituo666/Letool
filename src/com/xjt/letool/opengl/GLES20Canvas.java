package com.xjt.letool.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.xjt.letool.utils.IntArray;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class GLES20Canvas implements GLESCanvas {

    private static final String TAG = GLES20Canvas.class.getSimpleName();
    private static final int INITIAL_RESTORE_STATE_SIZE = 8;
    private static final int MATRIX_SIZE = 16;
    private static final int FLOAT_SIZE = Float.SIZE / Byte.SIZE;
    // Keep track of restore state
    private float[] mMatrices = new float[INITIAL_RESTORE_STATE_SIZE * MATRIX_SIZE];
    private float[] mAlphas = new float[INITIAL_RESTORE_STATE_SIZE];
    private IntArray mSaveFlags = new IntArray();

    private int mCurrentAlphaIndex = 0;
    private int mCurrentMatrixIndex = 0;
    private int mWidth;
    private int mHeight;

    // Projection matrix
    private float[] mProjectionMatrix = new float[MATRIX_SIZE];
    // Screen size for when we aren't bound to a texture
    private int mScreenWidth;
    private int mScreenHeight;

    // GL programs
    private int mDrawProgram;
    private int mTextureProgram;
    private int mOesTextureProgram;
    private int mMeshProgram;

    // GL buffer containing BOX_COORDINATES
    private int mBoxCoordinates;
    // Bound textures.
    private ArrayList<Texture> mTargetTextures = new ArrayList<Texture>();

    // Temporary variables used within calculations
    private final float[] mTempMatrix = new float[32];
    private final float[] mTempColor = new float[4];
    private final RectF mTempSourceRect = new RectF();
    private final RectF mTempTargetRect = new RectF();
    private final float[] mTempTextureMatrix = new float[MATRIX_SIZE];
    private final int[] mTempIntArray = new int[1];

    private static final float[] BOX_COORDINATES = {
            0, 0, // Fill rectangle
            1, 0,
            0, 1,
            1, 1,
            0, 0, // Draw line
            1, 1,
            0, 0, // Draw rectangle outline
            0, 1,
            1, 1,
            1, 0,
    };

    private abstract static class ShaderParameter {
        public int handle;
        protected final String mName;

        public ShaderParameter(String name) {
            mName = name;
        }

        public abstract void loadHandle(int program);
    }

    private static class UniformShaderParameter extends ShaderParameter {
        public UniformShaderParameter(String name) {
            super(name);
        }

        @Override
        public void loadHandle(int program) {
            handle = GLES20.glGetUniformLocation(program, mName);
            checkError();
        }
    }

    private static class AttributeShaderParameter extends ShaderParameter {
        public AttributeShaderParameter(String name) {
            super(name);
        }

        @Override
        public void loadHandle(int program) {
            handle = GLES20.glGetAttribLocation(program, mName);
            checkError();
        }
    }

    ShaderParameter[] mDrawParameters = {
            new AttributeShaderParameter(POSITION_ATTRIBUTE), // INDEX_POSITION
            new UniformShaderParameter(MATRIX_UNIFORM), // INDEX_MATRIX
            new UniformShaderParameter(COLOR_UNIFORM), // INDEX_COLOR
    };
    ShaderParameter[] mTextureParameters = {
            new AttributeShaderParameter(POSITION_ATTRIBUTE), // INDEX_POSITION
            new UniformShaderParameter(MATRIX_UNIFORM), // INDEX_MATRIX
            new UniformShaderParameter(TEXTURE_MATRIX_UNIFORM), // INDEX_TEXTURE_MATRIX
            new UniformShaderParameter(TEXTURE_SAMPLER_UNIFORM), // INDEX_TEXTURE_SAMPLER
            new UniformShaderParameter(ALPHA_UNIFORM), // INDEX_ALPHA
    };
    ShaderParameter[] mOesTextureParameters = {
            new AttributeShaderParameter(POSITION_ATTRIBUTE), // INDEX_POSITION
            new UniformShaderParameter(MATRIX_UNIFORM), // INDEX_MATRIX
            new UniformShaderParameter(TEXTURE_MATRIX_UNIFORM), // INDEX_TEXTURE_MATRIX
            new UniformShaderParameter(TEXTURE_SAMPLER_UNIFORM), // INDEX_TEXTURE_SAMPLER
            new UniformShaderParameter(ALPHA_UNIFORM), // INDEX_ALPHA
    };
    ShaderParameter[] mMeshParameters = {
            new AttributeShaderParameter(POSITION_ATTRIBUTE), // INDEX_POSITION
            new UniformShaderParameter(MATRIX_UNIFORM), // INDEX_MATRIX
            new AttributeShaderParameter(TEXTURE_COORD_ATTRIBUTE), // INDEX_TEXTURE_COORD
            new UniformShaderParameter(TEXTURE_SAMPLER_UNIFORM), // INDEX_TEXTURE_SAMPLER
            new UniformShaderParameter(ALPHA_UNIFORM), // INDEX_ALPHA
    };

    private static final String POSITION_ATTRIBUTE = "aPosition";
    private static final String COLOR_UNIFORM = "uColor";
    private static final String MATRIX_UNIFORM = "uMatrix";
    private static final String TEXTURE_MATRIX_UNIFORM = "uTextureMatrix";
    private static final String TEXTURE_SAMPLER_UNIFORM = "uTextureSampler";
    private static final String ALPHA_UNIFORM = "uAlpha";
    private static final String TEXTURE_COORD_ATTRIBUTE = "aTextureCoordinate";

    private static final String DRAW_VERTEX_SHADER = ""
            + "uniform mat4 " + MATRIX_UNIFORM + ";\n"
            + "attribute vec2 " + POSITION_ATTRIBUTE + ";\n"
            + "void main() {\n"
            + "  vec4 pos = vec4(" + POSITION_ATTRIBUTE + ", 0.0, 1.0);\n"
            + "  gl_Position = " + MATRIX_UNIFORM + " * pos;\n"
            + "}\n";

    private static final String DRAW_FRAGMENT_SHADER = ""
            + "precision mediump float;\n"
            + "uniform vec4 " + COLOR_UNIFORM + ";\n"
            + "void main() {\n"
            + "  gl_FragColor = " + COLOR_UNIFORM + ";\n"
            + "}\n";

    private static final String TEXTURE_VERTEX_SHADER = ""
            + "uniform mat4 " + MATRIX_UNIFORM + ";\n"
            + "uniform mat4 " + TEXTURE_MATRIX_UNIFORM + ";\n"
            + "attribute vec2 " + POSITION_ATTRIBUTE + ";\n"
            + "varying vec2 vTextureCoord;\n"
            + "void main() {\n"
            + "  vec4 pos = vec4(" + POSITION_ATTRIBUTE + ", 0.0, 1.0);\n"
            + "  gl_Position = " + MATRIX_UNIFORM + " * pos;\n"
            + "  vTextureCoord = (" + TEXTURE_MATRIX_UNIFORM + " * pos).xy;\n"
            + "}\n";

    private static final String MESH_VERTEX_SHADER = ""
            + "uniform mat4 " + MATRIX_UNIFORM + ";\n"
            + "attribute vec2 " + POSITION_ATTRIBUTE + ";\n"
            + "attribute vec2 " + TEXTURE_COORD_ATTRIBUTE + ";\n"
            + "varying vec2 vTextureCoord;\n"
            + "void main() {\n"
            + "  vec4 pos = vec4(" + POSITION_ATTRIBUTE + ", 0.0, 1.0);\n"
            + "  gl_Position = " + MATRIX_UNIFORM + " * pos;\n"
            + "  vTextureCoord = " + TEXTURE_COORD_ATTRIBUTE + ";\n"
            + "}\n";

    private static final String TEXTURE_FRAGMENT_SHADER = ""
            + "precision mediump float;\n"
            + "varying vec2 vTextureCoord;\n"
            + "uniform float " + ALPHA_UNIFORM + ";\n"
            + "uniform sampler2D " + TEXTURE_SAMPLER_UNIFORM + ";\n"
            + "void main() {\n"
            + "  gl_FragColor = texture2D(" + TEXTURE_SAMPLER_UNIFORM + ", vTextureCoord);\n"
            + "  gl_FragColor *= " + ALPHA_UNIFORM + ";\n"
            + "}\n";

    private static final String OES_TEXTURE_FRAGMENT_SHADER = ""
            + "#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n"
            + "varying vec2 vTextureCoord;\n"
            + "uniform float " + ALPHA_UNIFORM + ";\n"
            + "uniform samplerExternalOES " + TEXTURE_SAMPLER_UNIFORM + ";\n"
            + "void main() {\n"
            + "  gl_FragColor = texture2D(" + TEXTURE_SAMPLER_UNIFORM + ", vTextureCoord);\n"
            + "  gl_FragColor *= " + ALPHA_UNIFORM + ";\n"
            + "}\n";

    private static int loadShader(int type, String shaderCode) {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        checkError();
        GLES20.glCompileShader(shader);
        checkError();

        return shader;
    }

    private static FloatBuffer createBuffer(float[] values) {
        // First create an nio buffer, then create a VBO from it.
        int size = values.length * FLOAT_SIZE;
        FloatBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(values, 0, values.length).position(0);
        return buffer;
    }

    private int assembleProgram(int vertexShader, int fragmentShader, ShaderParameter[] params) {
        int program = GLES20.glCreateProgram();
        checkError();
        if (program == 0) {
            throw new RuntimeException("Cannot create GL program: " + GLES20.glGetError());
        }
        GLES20.glAttachShader(program, vertexShader);
        checkError();
        GLES20.glAttachShader(program, fragmentShader);
        checkError();
        GLES20.glLinkProgram(program);
        checkError();
        int[] mLinkStatus = mTempIntArray;
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, mLinkStatus, 0);
        if (mLinkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: ");
            Log.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        for (int i = 0; i < params.length; i++) {
            params[i].loadHandle(program);
        }
        return program;
    }

    private Texture getTargetTexture() {
        return mTargetTextures.get(mTargetTextures.size() - 1);
    }

    public GLES20Canvas() {
        Matrix.setIdentityM(mTempTextureMatrix, 0);
        Matrix.setIdentityM(mMatrices, mCurrentMatrixIndex);
        mAlphas[mCurrentAlphaIndex] = 1f;
        mTargetTextures.add(null);

        FloatBuffer boxBuffer = createBuffer(BOX_COORDINATES);
        mBoxCoordinates = uploadBuffer(boxBuffer);

        int drawVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, DRAW_VERTEX_SHADER);
        int textureVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, TEXTURE_VERTEX_SHADER);
        int meshVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, MESH_VERTEX_SHADER);
        int drawFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, DRAW_FRAGMENT_SHADER);
        int textureFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, TEXTURE_FRAGMENT_SHADER);
        int oesTextureFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, OES_TEXTURE_FRAGMENT_SHADER);

        mDrawProgram = assembleProgram(drawVertexShader, drawFragmentShader, mDrawParameters);
        mTextureProgram = assembleProgram(textureVertexShader, textureFragmentShader, mTextureParameters);
        mOesTextureProgram = assembleProgram(textureVertexShader, oesTextureFragmentShader, mOesTextureParameters);
        mMeshProgram = assembleProgram(meshVertexShader, textureFragmentShader, mMeshParameters);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        checkError();
    }

    @Override
    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
        GLES20.glViewport(0, 0, mWidth, mHeight);
        checkError();
        Matrix.setIdentityM(mMatrices, mCurrentMatrixIndex);
        Matrix.orthoM(mProjectionMatrix, 0, 0, width, 0, height, -1, 1);
        if (getTargetTexture() == null) {
            mScreenWidth = width;
            mScreenHeight = height;
            Matrix.translateM(mMatrices, mCurrentMatrixIndex, 0, height, 0);
            Matrix.scaleM(mMatrices, mCurrentMatrixIndex, 1, -1, 1);
        }
    }

    @Override
    public void clearBuffer() {
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        checkError();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        checkError();
    }

    @Override
    public void clearBuffer(float[] argb) {
        GLES20.glClearColor(argb[1], argb[2], argb[3], argb[0]);
        checkError();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        checkError();
    }

    @Override
    public float getAlpha() {
        return mAlphas[mCurrentAlphaIndex];
    }

    @Override
    public void setAlpha(float alpha) {
        mAlphas[mCurrentAlphaIndex] = alpha;
    }

    @Override
    public void multiplyAlpha(float alpha) {
        setAlpha(getAlpha() * alpha);
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
    public void multiplyMatrix(float[] mMatrix, int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void save() {
        // TODO Auto-generated method stub

    }

    @Override
    public void save(int saveFlags) {
        // TODO Auto-generated method stub

    }

    @Override
    public void restore() {
        // TODO Auto-generated method stub

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

    public static void checkError() {
        int error = GLES20.glGetError();
        if (error != 0) {
            Throwable t = new Throwable();
            Log.e(TAG, "GL error: " + error, t);
        }
    }

    @Override
    public GLId getGLId() {
        // TODO Auto-generated method stub
        return null;
    }
}
