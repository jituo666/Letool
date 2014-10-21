package com.xjt.newpic.views;

import java.util.ArrayList;

import com.xjt.newpic.animations.AnimationTime;
import com.xjt.newpic.animations.CanvasAnim;
import com.xjt.newpic.animations.StateTransAnim;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.utils.Utils;
import com.xjt.newpic.views.opengl.GLESCanvas;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

/**
 * @Author Jituo.Xuan
 * @Date 4:30:49 PM Mar 24, 2014
 * @Comments:null
 */
@SuppressLint("WrongCall")
public class GLView {

    private static final String TAG = GLView.class.getSimpleName();

    protected final Rect mBounds = new Rect();
    protected final Rect mPaddings = new Rect();
    protected GLView mParent;
    private ArrayList<GLView> mComponents;
    private GLView mMotionTarget;
    private CanvasAnim mAnimation;
    public static final int VISIBLE = View.VISIBLE;
    public static final int INVISIBLE = View.INVISIBLE;
    private static final int FLAG_INVISIBLE = 1;
    private static final int FLAG_SET_MEASURED_SIZE = 2;
    private static final int FLAG_LAYOUT_REQUESTED = 4;

    private GLController mGLController;
    private int mViewFlags = 0;
    protected int mMeasuredWidth = 0;
    protected int mMeasuredHeight = 0;

    private int mLastWidthSpec = -1;
    private int mLastHeightSpec = -1;

    protected int mScrollY = 0;
    protected int mScrollX = 0;
    protected int mScrollHeight = 0;
    protected int mScrollWidth = 0;

    private float[] mBackgroundColor;
    private StateTransAnim mTransition;

    public boolean getBoundsOf(GLView descendant, Rect out) {
        int xoffset = 0;
        int yoffset = 0;
        GLView view = descendant;
        while (view != this) {
            if (view == null)
                return false;
            Rect bounds = view.mBounds;
            xoffset += bounds.left;
            yoffset += bounds.top;
            view = view.mParent;
        }
        out.set(xoffset, yoffset, xoffset + descendant.getWidth(),
                yoffset + descendant.getHeight());
        return true;
    }

    public Rect getPaddings() {
        return mPaddings;
    }

    public void layout(int left, int top, int right, int bottom) {
        boolean sizeChanged = setBounds(left, top, right, bottom);
        mViewFlags &= ~FLAG_LAYOUT_REQUESTED;
        // We call onLayout no matter sizeChanged is true or not because the
        // orientation may change without changing the size of the View (for
        // example, rotate the device by 180 degrees), and we want to handle
        // orientation change in onLayout.
        onLayout(sizeChanged, left, top, right, bottom);
    }

    private boolean setBounds(int left, int top, int right, int bottom) {
        boolean sizeChanged = (right - left) != (mBounds.right - mBounds.left)
                || (bottom - top) != (mBounds.bottom - mBounds.top);
        mBounds.set(left, top, right, bottom);
        return sizeChanged;
    }

    public void measure(int widthSpec, int heightSpec) {
        if (widthSpec == mLastWidthSpec && heightSpec == mLastHeightSpec
                && (mViewFlags & FLAG_LAYOUT_REQUESTED) == 0) {
            return;
        }

        mLastWidthSpec = widthSpec;
        mLastHeightSpec = heightSpec;

        mViewFlags &= ~FLAG_SET_MEASURED_SIZE;
        onMeasure(widthSpec, heightSpec);
        if ((mViewFlags & FLAG_SET_MEASURED_SIZE) == 0) {
            throw new IllegalStateException(getClass().getName() + " should call setMeasuredSize() in onMeasure()");
        }
    }

    protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
    }

    protected void onMeasure(int widthSpec, int heightSpec) {
    }

    protected void setMeasuredSize(int width, int height) {
        mViewFlags |= FLAG_SET_MEASURED_SIZE;
        mMeasuredWidth = width;
        mMeasuredHeight = height;
    }

    public int getMeasuredWidth() {
        return mMeasuredWidth;
    }

    public int getMeasuredHeight() {
        return mMeasuredHeight;
    }

    public void setVisibility(int visibility) {
        if (visibility == getVisibility())
            return;
        if (visibility == VISIBLE) {
            mViewFlags &= ~FLAG_INVISIBLE;
        } else {
            mViewFlags |= FLAG_INVISIBLE;
        }
        onVisibilityChanged(visibility);
        invalidate();
    }

    public int getVisibility() {
        return (mViewFlags & FLAG_INVISIBLE) == 0 ? VISIBLE : INVISIBLE;
    }

    protected void onVisibilityChanged(int visibility) {
        for (int i = 0, n = getComponentCount(); i < n; ++i) {
            GLView child = getComponent(i);
            if (child.getVisibility() == GLView.VISIBLE) {
                child.onVisibilityChanged(visibility);
            }
        }
    }

    public void attachToRoot(GLController root) {
        Utils.assertTrue(mParent == null && mGLController == null);
        onAttachToRoot(root);
    }

    public void detachFromRoot() {
        Utils.assertTrue(mParent == null && mGLController != null);
        onDetachFromRoot();
    }

    public int getComponentCount() {
        return mComponents == null ? 0 : mComponents.size();
    }

    public GLView getComponent(int index) {
        if (mComponents == null) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return mComponents.get(index);
    }

    public void addComponent(GLView component) {
        if (component.mParent != null)
            throw new IllegalStateException();
        if (mComponents == null) {
            mComponents = new ArrayList<GLView>();
        }
        mComponents.add(component);
        component.mParent = this;

        if (mGLController != null) {
            component.onAttachToRoot(mGLController);
        }
    }

    public boolean removeComponent(GLView component) {
        if (mComponents == null)
            return false;
        if (mComponents.remove(component)) {
            removeOneComponent(component);
            return true;
        }
        return false;
    }

    // Removes all children of this GLView.
    public void removeAllComponents() {
        for (int i = 0, n = mComponents.size(); i < n; ++i) {
            removeOneComponent(mComponents.get(i));
        }
        mComponents.clear();
    }

    private void removeOneComponent(GLView component) {
        if (mMotionTarget == component) {
            long now = SystemClock.uptimeMillis();
            MotionEvent cancelEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0, 0, 0);
            dispatchTouchEvent(cancelEvent);
            cancelEvent.recycle();
        }
        component.onDetachFromRoot();
        component.mParent = null;
    }

    public void invalidate() {
        GLController controller = getGLController();
        if (controller != null)
            controller.requestRender();
    }

    protected void render(GLESCanvas canvas) {

        boolean transitionActive = false;
        if (mTransition != null && mTransition.calculate(AnimationTime.get())) {
            invalidate();
            transitionActive = mTransition.isActive();
        }
        renderBackground(canvas);
        canvas.save();
        if (transitionActive) {
            mTransition.applyContentTransform(this, canvas);
        }
        for (int i = 0, n = getComponentCount(); i < n; ++i) {
            renderChild(canvas, getComponent(i));
        }
        canvas.restore();
        if (transitionActive) {
            mTransition.applyOverlay(this, canvas);
        }
    }

    protected void renderChild(GLESCanvas canvas, GLView component) {
        if (component.getVisibility() != GLView.VISIBLE
                && component.mAnimation == null)
            return;

        int xoffset = component.mBounds.left - mScrollX;
        int yoffset = component.mBounds.top - mScrollY;

        canvas.translate(xoffset, yoffset);

        CanvasAnim anim = component.mAnimation;
        if (anim != null) {
            canvas.save(anim.getCanvasSaveFlags());
            if (anim.calculate(AnimationTime.get())) {
                invalidate();
            } else {
                component.mAnimation = null;
            }
            anim.apply(canvas);
        }
        component.render(canvas);
        if (anim != null)
            canvas.restore();
        canvas.translate(-xoffset, -yoffset);
    }

    protected void renderBackground(GLESCanvas view) {
        if (mBackgroundColor != null) {
            view.clearBuffer(mBackgroundColor);
        }
        if (mTransition != null && mTransition.isActive()) {
            mTransition.applyBackground(this, view);
            return;
        }
    }

    public void setIntroAnimation(StateTransAnim intro) {
        mTransition = intro;
        if (mTransition != null)
            mTransition.start();
    }

    public float[] getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(float[] color) {
        mBackgroundColor = color;
    }

    public Rect bounds() {
        return mBounds;
    }

    public int getWidth() {
        return mBounds.right - mBounds.left;
    }

    public int getHeight() {
        return mBounds.bottom - mBounds.top;
    }

    public GLController getGLController() {
        return mGLController;
    }

    protected void onAttachToRoot(GLController glController) {
        mGLController = glController;
        for (int i = 0, n = getComponentCount(); i < n; ++i) {
            getComponent(i).onAttachToRoot(glController);
        }
    }

    protected void onDetachFromRoot() {
        for (int i = 0, n = getComponentCount(); i < n; ++i) {
            getComponent(i).onDetachFromRoot();
        }
        mGLController = null;
    }

    public void lockRendering() {
        if (mGLController != null) {
            mGLController.lockRenderThread();
        }
    }

    public void unlockRendering() {
        if (mGLController != null) {
            mGLController.unlockRenderThread();
        }
    }

    protected boolean onTouch(MotionEvent event) {
        return false;
    }

    protected boolean dispatchTouchEvent(MotionEvent event, int x, int y, GLView component, boolean checkBounds) {
        Rect rect = component.mBounds;
        int left = rect.left;
        int top = rect.top;
        if (!checkBounds || rect.contains(x, y)) {
            event.offsetLocation(-left, -top);
            if (component.dispatchTouchEvent(event)) {
                event.offsetLocation(left, top);
                return true;
            }
            event.offsetLocation(left, top);
        }
        return false;
    }

    protected boolean dispatchTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int action = event.getAction();
        if (mMotionTarget != null) {
            if (action == MotionEvent.ACTION_DOWN) {
                MotionEvent cancel = MotionEvent.obtain(event);
                cancel.setAction(MotionEvent.ACTION_CANCEL);
                dispatchTouchEvent(cancel, x, y, mMotionTarget, false);
                mMotionTarget = null;
            } else {
                dispatchTouchEvent(event, x, y, mMotionTarget, false);
                if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                    mMotionTarget = null;
                }
                return true;
            }
        }
        if (action == MotionEvent.ACTION_DOWN) {
            // in the reverse rendering order
            for (int i = getComponentCount() - 1; i >= 0; --i) {
                GLView component = getComponent(i);
                if (component.getVisibility() != GLView.VISIBLE)
                    continue;
                if (dispatchTouchEvent(event, x, y, component, true)) {
                    mMotionTarget = component;
                    return true;
                }
            }
        }
        return onTouch(event);
    }
}
