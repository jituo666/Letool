package com.xjt.newpic.edit.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.colorpicker.ColorBrightnessView;
import com.xjt.newpic.edit.colorpicker.ColorListener;
import com.xjt.newpic.edit.editors.Editor;

public class SliderBrightness implements Control {
    private ColorBrightnessView mColorOpacityView;
    private ParameterBrightness mParameter;
    Editor mEditor;

    @Override
    public void setUp(ViewGroup container, Parameter parameter, Editor editor) {
        container.removeAllViews();
        mEditor = editor;
        Context context = container.getContext();
        mParameter = (ParameterBrightness) parameter;
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout lp = (LinearLayout) inflater.inflate(
                R.layout.np_edit_brightness, container, true);

        mColorOpacityView =   (ColorBrightnessView) lp.findViewById(R.id.brightnessView);
        updateUI();
        mColorOpacityView.addColorListener(new ColorListener() {
            @Override
            public void setColor(float[] hsvo) {
                mParameter.setValue((int)(255* hsvo[3]));
                mEditor.commitLocalRepresentation();
            }
            @Override
            public void addColorListener(ColorListener l) {
            }
        });
    }

    @Override
    public View getTopView() {
        return mColorOpacityView;
    }

    @Override
    public void setPrameter(Parameter parameter) {
        mParameter = (ParameterBrightness) parameter;
        if (mColorOpacityView != null) {
            updateUI();
        }
    }

    @Override
    public void updateUI() {
        mColorOpacityView.setColor(mParameter.getColor());
    }
}
