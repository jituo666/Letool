//package com.xjt.newpic.filtershow.controller;
//
//import android.content.Context;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.SeekBar;
//import android.widget.SeekBar.OnSeekBarChangeListener;
//import android.widget.TextView;
//
//import com.xjt.newpic.R;
//import com.xjt.newpic.filtershow.editors.Editor;
//
//public class TitledSlider implements Control {
//    private final String LOGTAG = "ParametricEditor";
//    private SeekBar mSeekBar;
//    private TextView mControlName;
//    private TextView mControlValue;
//    protected ParameterInteger mParameter;
//    Editor mEditor;
//    View mTopView;
//    protected int mLayoutID = R.layout.filtershow_control_title_slider;
//
//    @Override
//    public void setUp(ViewGroup container, Parameter parameter, Editor editor) {
//        container.removeAllViews();
//        mEditor = editor;
//        Context context = container.getContext();
//        mParameter = (ParameterInteger) parameter;
//        LayoutInflater inflater =
//                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        mTopView = inflater.inflate(mLayoutID, container, true);
//        mTopView.setVisibility(View.VISIBLE);
//        mSeekBar = (SeekBar) mTopView.findViewById(R.id.controlValueSeekBar);
//        mControlName = (TextView) mTopView.findViewById(R.id.controlName);
//        mControlValue = (TextView) mTopView.findViewById(R.id.controlValue);
//        updateUI();
//        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (mParameter != null) {
//                    mParameter.setValue(progress + mParameter.getMinimum());
//                    if (mControlName != null) {
//                        mControlName.setText(mParameter.getParameterName());
//                    }
//                    if (mControlValue != null) {
//                        mControlValue.setText(Integer.toString(mParameter.getValue()));
//                    }
//                    mEditor.commitLocalRepresentation();
//                }
//            }
//        });
//    }
//
//    @Override
//    public void setPrameter(Parameter parameter) {
//        mParameter = (ParameterInteger) parameter;
//        if (mSeekBar != null)
//            updateUI();
//    }
//
//    @Override
//    public void updateUI() {
//        if (mControlName != null && mParameter.getParameterName() != null) {
//            mControlName.setText(mParameter.getParameterName().toUpperCase());
//        }
//        if (mControlValue != null) {
//            mControlValue.setText(
//                    Integer.toString(mParameter.getValue()));
//        }
//        mSeekBar.setMax(mParameter.getMaximum() - mParameter.getMinimum());
//        mSeekBar.setProgress(mParameter.getValue() - mParameter.getMinimum());
//        mEditor.commitLocalRepresentation();
//    }
//
//    @Override
//    public View getTopView() {
//        return mTopView;
//    }
//}
