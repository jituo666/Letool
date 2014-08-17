
package com.xjt.newpic.filtershow.editors;

import com.xjt.newpic.surpport.PopupMenuItem;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;

public class SwapButton extends Button implements GestureDetector.OnGestureListener {

    public static int ANIM_DURATION = 200;

    public interface SwapButtonListener {
        public void swapLeft(PopupMenuItem item);
        public void swapRight(PopupMenuItem item);
    }

    private GestureDetector mDetector;
    private SwapButtonListener mListener;
//    private Menu mMenu;
    private int mCurrentMenuIndex;

    public SwapButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDetector = new GestureDetector(context, this);
    }

    public SwapButtonListener getListener() {
        return mListener;
    }

    public void setListener(SwapButtonListener listener) {  
        mListener = listener;
    }

    public boolean onTouchEvent(MotionEvent me) {
        if (!mDetector.onTouchEvent(me)) {
            return super.onTouchEvent(me);
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        //callOnClick();
        this.performClick();
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//        if (mMenu == null) {
//            return false;
//        }
//        PopupMenu m;
//        if (e1.getX() - e2.getX() > 0) {
//            // right to left
//            mCurrentMenuIndex++;
//            if (mCurrentMenuIndex == mMenu.size()) {
//                mCurrentMenuIndex = 0;
//            }
//            if (mListener != null) {
//                mListener.swapRight(mCurrentMenuIndex);
//            }
//        } else {
//            // left to right
//            mCurrentMenuIndex--;
//            if (mCurrentMenuIndex < 0) {
//                mCurrentMenuIndex = mMenu.size() - 1;
//            }
//            if (mListener != null) {
//                mListener.swapLeft(mCurrentMenuIndex);
//            }
//        }
        return true;
    }
}
