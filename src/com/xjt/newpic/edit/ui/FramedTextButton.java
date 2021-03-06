package com.xjt.newpic.edit.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.xjt.newpic.R;

public class FramedTextButton extends ImageButton {

    private static final String TAG = "FramedTextButton";
    private String mText = null;
    private static int mTextSize = 24;
    private static int mTextPadding = 20;
    private static Paint gPaint = new Paint();
    private static Path gPath = new Path();
    private static int mTrianglePadding = 2;
    private static int mTriangleSize = 30;

    public static void setTextSize(int value) {
        mTextSize = value;
    }

    public static void setTextPadding(int value) {
        mTextPadding = value;
    }

    public static void setTrianglePadding(int value) {
        mTrianglePadding = value;
    }

    public static void setTriangleSize(int value) {
        mTriangleSize = value;
    }

    public void setText(String text) {
        mText = text;
        invalidate();
    }

    public void setTextFrom(int itemId) {
        switch (itemId) {
            case 0: {
                setText(getContext().getString(R.string.curves_channel_rgb));
                break;
            }
            case 1: {
                setText(getContext().getString(R.string.curves_channel_red));
                break;
            }
            case 2: {
                setText(getContext().getString(R.string.curves_channel_green));
                break;
            }
            case 3: {
                setText(getContext().getString(R.string.curves_channel_blue));
                break;
            }
        }
        invalidate();
    }

    public FramedTextButton(Context context) {
        this(context, null);
    }

    public FramedTextButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs == null) {
            return;
        }
        TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ImageButtonTitle);

        mText = a.getString(R.styleable.ImageButtonTitle_android_text);
    }

    public String getText() {
        return mText;
    }

    @Override
    public void onDraw(Canvas canvas) {
        gPaint.setARGB(96, 255, 255, 255);
        gPaint.setStrokeWidth(2);
        gPaint.setStyle(Paint.Style.STROKE);
        int w = getWidth();
        int h = getHeight();
        canvas.drawRect(mTextPadding, mTextPadding, w - mTextPadding,
                h - mTextPadding, gPaint);
        gPath.reset();
        gPath.moveTo(w - mTextPadding - mTrianglePadding - mTriangleSize,
                h - mTextPadding - mTrianglePadding);
        gPath.lineTo(w - mTextPadding - mTrianglePadding,
                h - mTextPadding - mTrianglePadding - mTriangleSize);
        gPath.lineTo(w - mTextPadding - mTrianglePadding,
                h - mTextPadding - mTrianglePadding);
        gPath.close();
        gPaint.setARGB(128, 255, 255, 255);
        gPaint.setStrokeWidth(1);
        gPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawPath(gPath, gPaint);
        if (mText != null) {
            gPaint.reset();
            gPaint.setARGB(255, 255, 255, 255);
            gPaint.setTextSize(mTextSize);
            float textWidth = gPaint.measureText(mText);
            Rect bounds = new Rect();
            gPaint.getTextBounds(mText, 0, mText.length(), bounds);
            int x = (int) ((w - textWidth) / 2);
            int y = (h + bounds.height()) / 2;

            canvas.drawText(mText, x, y, gPaint);
        }
    }

}
