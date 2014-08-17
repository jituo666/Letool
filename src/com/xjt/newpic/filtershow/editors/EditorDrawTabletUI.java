package com.xjt.newpic.filtershow.editors;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.xjt.newpic.R;
import com.xjt.newpic.filtershow.colorpicker.ColorCompareView;
import com.xjt.newpic.filtershow.colorpicker.ColorHueView;
import com.xjt.newpic.filtershow.colorpicker.ColorListener;
import com.xjt.newpic.filtershow.colorpicker.ColorOpacityView;
import com.xjt.newpic.filtershow.colorpicker.ColorSVRectView;
import com.xjt.newpic.filtershow.controller.BasicParameterInt;
import com.xjt.newpic.filtershow.controller.BasicParameterStyle;
import com.xjt.newpic.filtershow.controller.ParameterColor;
import com.xjt.newpic.filtershow.filters.FilterDrawRepresentation;

import java.util.Arrays;

public class EditorDrawTabletUI {
    private int mSelectedColorButton;
    private int mSelectedStyleButton;
    private FilterDrawRepresentation mRep;
    private Button[] mColorButton;
    private ImageButton[] mStyleButton;

    private int[] mBasColors;
    private int mSelected;
    private int mTransparent;
    private SeekBar mdrawSizeSeekBar;
    private int[] ids = {
            R.id.draw_color_button01,
            R.id.draw_color_button02,
            R.id.draw_color_button03,
            R.id.draw_color_button04,
            R.id.draw_color_button05,
    };

    public void setDrawRepresentation(FilterDrawRepresentation rep) {
        mRep = rep;
        BasicParameterInt size;
        size = (BasicParameterInt) mRep.getParam(FilterDrawRepresentation.PARAM_SIZE);
        mdrawSizeSeekBar.setMax(size.getMaximum() - size.getMinimum());
        mdrawSizeSeekBar.setProgress(size.getValue());

        ParameterColor color;
        color = (ParameterColor) mRep.getParam(FilterDrawRepresentation.PARAM_COLOR);
        color.setValue(mBasColors[mSelectedColorButton]);
        BasicParameterStyle style;
        style = (BasicParameterStyle) mRep.getParam(FilterDrawRepresentation.PARAM_STYLE);
        style.setSelected(mSelectedStyleButton);
    }

    public void resetStyle() {
        for (int i = 0; i < mStyleButton.length; i++) {
            int rid = (i == mSelectedStyleButton) ? R.color.holo_blue_light : android.R.color.transparent;
            mStyleButton[i].setBackgroundResource(rid);

        }
    }
}
