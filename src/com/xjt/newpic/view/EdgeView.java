
package com.xjt.newpic.view;

import android.content.Context;
import android.opengl.Matrix;

import com.xjt.newpic.R;
import com.xjt.newpic.views.opengl.GLESCanvas;
import com.xjt.newpic.views.utils.ViewEdgeEffect;
import com.xjt.newpic.views.utils.ViewEdgeEffect.Drawable;

// EdgeView draws EdgeEffect (blue glow) at four sides of the view.
public class EdgeView extends GLView {

    @SuppressWarnings("unused")
    private static final String TAG = "EdgeView";

    public static final int INVALID_DIRECTION = -1;
    public static final int TOP = 0;
    public static final int LEFT = 1;
    public static final int BOTTOM = 2;
    public static final int RIGHT = 3;

    // Each edge effect has a transform matrix, and each matrix has 16 elements.
    // We put all the elements in one array. These constants specify the starting index of each matrix.
    private static final int TOP_M = TOP * 16;
    private static final int LEFT_M = LEFT * 16;
    private static final int BOTTOM_M = BOTTOM * 16;
    private static final int RIGHT_M = RIGHT * 16;

    private ViewEdgeEffect[] mEffect = new ViewEdgeEffect[4];
    private float[] mMatrix = new float[4 * 16];

    private Context mContext;
    Drawable mEdge;
    Drawable mGlow;

    public EdgeView(Context context) {
        mContext = context;
    }

    public void prepareDrawables() {
        mEdge = new Drawable(mContext, R.drawable.overscroll_edge);
        mGlow = new Drawable(mContext, R.drawable.overscroll_glow);
        for (int i = 0; i < 4; i++) {
            mEffect[i] = new ViewEdgeEffect(mContext, mEdge, mGlow);
        }
    }

    public void freeDrawables() {
        if (mEdge != null)
            mEdge.recycle();

        if (mGlow != null)
            mGlow.recycle();
        for (int i = 0; i < 4; i++) {
            mEffect[i] = null;
        }
    }

    @Override
    protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
        if (!changeSize)
            return;

        int w = right - left;
        int h = bottom - top;
        for (int i = 0; i < 4; i++) {
            if ((i & 1) == 0) {  // top or bottom
                mEffect[i].setSize(w, h);
            } else {  // left or right
                mEffect[i].setSize(h, w);
            }
        }

        // Set up transforms for the four edges. Without transforms an
        // EdgeEffect draws the TOP edge from (0, 0) to (w, Y * h) where Y
        // is some factor < 1. For other edges we need to move, rotate, and
        // flip the effects into proper places.
        Matrix.setIdentityM(mMatrix, TOP_M);
        Matrix.setIdentityM(mMatrix, LEFT_M);
        Matrix.setIdentityM(mMatrix, BOTTOM_M);
        Matrix.setIdentityM(mMatrix, RIGHT_M);

        Matrix.rotateM(mMatrix, LEFT_M, 90, 0, 0, 1);
        Matrix.scaleM(mMatrix, LEFT_M, 1, -1, 1);

        Matrix.translateM(mMatrix, BOTTOM_M, 0, h, 0);
        Matrix.scaleM(mMatrix, BOTTOM_M, 1, -1, 1);

        Matrix.translateM(mMatrix, RIGHT_M, w, 0, 0);
        Matrix.rotateM(mMatrix, RIGHT_M, 90, 0, 0, 1);
    }

    @Override
    protected void render(GLESCanvas canvas) {
        super.render(canvas);
        boolean more = false;
        for (int i = 0; i < 4; i++) {
            canvas.save(GLESCanvas.SAVE_FLAG_MATRIX);
            canvas.multiplyMatrix(mMatrix, i * 16);
            if (mEffect[i] != null) {
                more |= mEffect[i].draw(canvas);
            }
            canvas.restore();
        }
        if (more) {
            invalidate();
        }
    }

    // Called when the content is pulled away from the edge.
    // offset is in pixels. direction is one of {TOP, LEFT, BOTTOM, RIGHT}.
    public void onPull(int offset, int direction) {
        int fullLength = ((direction & 1) == 0) ? getWidth() : getHeight();
        if (mEffect[direction] != null) {
            mEffect[direction].onPull((float) offset / fullLength);
            if (!mEffect[direction].isFinished()) {
                invalidate();
            }
        }
    }

    // Call when the object is released after being pulled.
    public void onRelease() {
        boolean more = false;
        for (int i = 0; i < 4; i++) {
            if (mEffect[i] != null) {
                mEffect[i].onRelease();
                more |= !mEffect[i].isFinished();
            }
        }
        if (more) {
            invalidate();
        }
    }

    // Call when the effect absorbs an impact at the given velocity.
    // Used when a fling reaches the scroll boundary. velocity is in pixels
    // per second. direction is one of {TOP, LEFT, BOTTOM, RIGHT}.
    public void onAbsorb(int velocity, int direction) {
        if (mEffect[direction] != null) {
            mEffect[direction].onAbsorb(velocity);
            if (!mEffect[direction].isFinished()) {
                invalidate();
            }
        }
    }
}
