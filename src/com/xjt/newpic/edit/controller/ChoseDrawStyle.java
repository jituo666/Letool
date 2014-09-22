
package com.xjt.newpic.edit.controller;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.editors.Editor;

public class ChoseDrawStyle implements Control {

    private final String TAG = ChoseDrawStyle.class.getSimpleName();

    private final static int SELECTED_BITMAP_SIZE = 64;
    private final static int SELECTED_BORD_SIZE = 2; //dp

    protected ParameterStyle mParameter;
    protected LinearLayout mLinearLayout;
    protected Editor mEditor;
    private View mTopView;
    protected int mLayoutID = R.layout.np_edit_control_style_chooser;
    private int mSelectedButton = 0;
    private Resources mRes;

    private int[] mStyleID = {
            R.id.draw_style01,
            R.id.draw_style02,
            R.id.draw_style03,
            R.id.draw_style04,
            R.id.draw_style05
    };

    private ImageView[] mStyles = new ImageView[mStyleID.length];

    @Override
    public void setUp(ViewGroup container, Parameter parameter, Editor editor) {
        container.removeAllViews();
        mEditor = editor;
        Context context = container.getContext();
        mRes = container.getContext().getResources();
        mParameter = (ParameterStyle) parameter;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mTopView = inflater.inflate(mLayoutID, container, true);
        mLinearLayout = (LinearLayout) mTopView.findViewById(R.id.listStyles);
        mTopView.setVisibility(View.VISIBLE);
        int[] palette = mParameter.getStylePalette();
        for (int i = 0; i < mStyleID.length; i++) {
            final ImageView button = (ImageView) mTopView.findViewById(mStyleID[i]);
            mStyles[i] = button;
            if (mSelectedButton == i) {
                button.setImageBitmap(createSelectedBitmap(palette[i]));
            } else {
                button.setImageResource(palette[i]);
            }
            final int textureNo = i;
            button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    selectStyle(v, textureNo);
                }
            });
        }
    }

    private void resetBorders() {
        int[] palette = mParameter.getStylePalette();
        for (int i = 0; i < mStyleID.length; i++) {
            final ImageView button = mStyles[i];
            button.setImageResource(palette[i]);
            if (mSelectedButton == i) {
                button.setImageBitmap(createSelectedBitmap(palette[i]));
            } else {
                button.setImageResource(palette[i]);
            }
        }
    }

    public void selectStyle(View button, int textureNo) {
        mSelectedButton = textureNo;
        int[] palette = mParameter.getStylePalette();
        mParameter.setValue(palette[mSelectedButton]);
        resetBorders();
        mEditor.commitLocalRepresentation();
    }

    @Override
    public View getTopView() {
        return mTopView;
    }

    @Override
    public void setPrameter(Parameter parameter) {
        mParameter = (ParameterStyle) parameter;
        updateUI();
    }

    @Override
    public void updateUI() {
        if (mParameter == null) {
            return;
        }
    }

    public void setDrawStyleSet(int[] basStyles) {
        int[] palette = mParameter.getStylePalette();
        for (int i = 0; i < palette.length; i++) {
            palette[i] = basStyles[i];
            mStyles[i].setTag(palette[i]);
            mStyles[i].setImageResource(palette[i]);
        }

    }

    public int[] getDrawStyleSet() {
        return mParameter.getStylePalette();
    }

    private Bitmap createSelectedBitmap(int resid) {
        Bitmap org = BitmapFactory.decodeResource(mRes, resid);
        int padding = Math.round(mRes.getDisplayMetrics().density * SELECTED_BORD_SIZE);
        int size = SELECTED_BITMAP_SIZE + padding;
        Bitmap result = Bitmap.createBitmap(size, size, Config.ARGB_8888);
        Canvas c = new Canvas(result);
        Drawable d = mRes.getDrawable(R.drawable.grid_pressed);
        d.setBounds(new Rect(0, 0, size, size));
        d.draw(c);
        c.drawBitmap(org, new Rect(0, 0, org.getWidth(), org.getHeight()), new Rect(padding, padding,
                result.getWidth() - padding, result.getHeight() - padding), null);
        return result;
    }
}
