package com.xjt.newpic.edit.controller;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.editors.Editor;

public class ActionSlider extends TitledSlider {
    private static final String TAG = "ActionSlider";
    ImageButton mLeftButton;
    ImageButton mRightButton;
    public ActionSlider() {
        mLayoutID = R.layout.np_edit_control_action_slider;
    }

    @Override
    public void setUp(ViewGroup container, Parameter parameter, Editor editor) {
        super.setUp(container, parameter, editor);
        mLeftButton = (ImageButton) mTopView.findViewById(R.id.leftActionButton);
        mLeftButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ((ParameterActionAndInt) mParameter).fireLeftAction();
            }
        });

        mRightButton = (ImageButton) mTopView.findViewById(R.id.rightActionButton);
        mRightButton.setOnClickListener(new OnClickListener() {

                @Override
            public void onClick(View v) {
                ((ParameterActionAndInt) mParameter).fireRightAction();
            }
        });
        updateUI();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (mLeftButton != null) {
            int iconId = ((ParameterActionAndInt) mParameter).getLeftIcon();
            mLeftButton.setImageResource(iconId);
        }
        if (mRightButton != null) {
            int iconId = ((ParameterActionAndInt) mParameter).getRightIcon();
            mRightButton.setImageResource(iconId);
        }
    }
}
