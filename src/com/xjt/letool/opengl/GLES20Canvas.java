package com.xjt.letool.opengl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;

public class GLES20Canvas implements GLESCanvas {

    @Override
    public GLId getGLId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSize(int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearBuffer() {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearBuffer(float[] argb) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAlpha(float alpha) {
        // TODO Auto-generated method stub

    }

    @Override
    public float getAlpha() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void multiplyAlpha(float alpha) {
        // TODO Auto-generated method stub

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

}
