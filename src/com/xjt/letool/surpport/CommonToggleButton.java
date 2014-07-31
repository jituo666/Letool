package com.xjt.letool.surpport;

import com.xjt.letool.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;


/**
 * @Author Jituo.Xuan
 * @Date 5:56:20 AM Aug 1, 2014
 * @Comments:null
 */
public class CommonToggleButton extends TextView {
    private static final int NO_ALPHA = 0xFF;

    private Context mContext;
    private boolean mChecked = false;
    private CharSequence mTextOn;
    private CharSequence mTextOff;
    private float mDisabledAlpha;

    private static final int[] CHECKED_STATE_SET = {
        android.R.attr.state_checked
    };

    public CommonToggleButton(Context context) {
        super(context);
        init(context);
    }

    public CommonToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CommonToggleButton);
        mTextOn = a.getText(R.styleable.CommonToggleButton_textOn);
        mTextOff = a.getText(R.styleable.CommonToggleButton_textOff);
        mDisabledAlpha = a.getFloat(R.styleable.CommonToggleButton_disabledAlpha, 0.5f);
        a.recycle();

        init(context);
    }

    private void init(Context cxt) {
        mContext = cxt;

        setGravity(Gravity.CENTER);
        setTextColor(cxt.getResources().getColorStateList(R.color.common_toggle_button_text));
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        if (mTextOn == null) {
            mTextOn = mContext.getText(R.string.common_state_on);
        }
        if (mTextOff == null) {
            mTextOff = mContext.getText(R.string.common_state_off);
        }

        syncText();
        syncBackground();
    }

    private void syncText() {
        if (mChecked) {
            setText(mTextOn != null ? mTextOn : "");
        } else {
            setText(mTextOff != null ? mTextOff : "");
        }
    }

    private void syncBackground() {
        if (mChecked) {
            setBackgroundResource(R.drawable.common_toggle_button_on);
        } else {
            setBackgroundResource(R.drawable.common_toggle_button_off);
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable bkgDrawable = getBackground();
        if (bkgDrawable != null) {
            bkgDrawable.setAlpha(isEnabled() ? NO_ALPHA : (int) (NO_ALPHA * mDisabledAlpha));
        }
    }

    @Override
    public boolean performClick() {
        if (!super.performClick()) {
            // If no OnClickListener setup, toggle this button as default behavior
            toggle();
            return false;
        }
        return true;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        if (mChecked == checked) {
            return; // no change
        }
        mChecked = checked;
        refreshDrawableState();
        syncText();
        syncBackground();
    }

    public void toggle() {
        setChecked(!mChecked);
    }

    public void setToggleText(int textOnResId, int textOffResId) {
        mTextOn = mContext.getText(textOnResId);
        mTextOff = mContext.getText(textOffResId);
        syncText();
    }

    public void setToggleText(CharSequence textOn, CharSequence textOff) {
        mTextOn = textOn;
        mTextOff = textOff;
        syncText();
    }
}
