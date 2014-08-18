
package com.xjt.newpic.filtershow.editors;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.xjt.newpic.R;
import com.xjt.newpic.filtershow.controller.BasicSlider;
import com.xjt.newpic.filtershow.controller.ColorChooser;
import com.xjt.newpic.filtershow.controller.Control;
import com.xjt.newpic.filtershow.controller.Parameter;
import com.xjt.newpic.filtershow.controller.ParameterBrightness;
import com.xjt.newpic.filtershow.controller.ParameterColor;
import com.xjt.newpic.filtershow.controller.ParameterHue;
import com.xjt.newpic.filtershow.controller.ParameterInteger;
import com.xjt.newpic.filtershow.controller.ParameterOpacity;
import com.xjt.newpic.filtershow.controller.ParameterSaturation;
import com.xjt.newpic.filtershow.controller.ParameterStyles;
import com.xjt.newpic.filtershow.controller.SliderBrightness;
import com.xjt.newpic.filtershow.controller.SliderHue;
import com.xjt.newpic.filtershow.controller.SliderOpacity;
import com.xjt.newpic.filtershow.controller.SliderSaturation;
import com.xjt.newpic.filtershow.controller.StyleChooser;
import com.xjt.newpic.filtershow.filters.FilterBasicRepresentation;
import com.xjt.newpic.filtershow.filters.FilterRepresentation;

import java.lang.reflect.Constructor;
import java.util.HashMap;

public class ParametricEditor extends Editor {

    private final String TAG = ParametricEditor.class.getSimpleName();

    public static final int MINIMUM_WIDTH = 600;
    public static final int MINIMUM_HEIGHT = 800;
    public static int ID = R.id.editorParametric;

    private int mLayoutID;
    private int mViewID;
    protected Control mControl;
    protected View mActionButton;
    protected View mEditControl;
    protected static HashMap<String, Class<?>> portraitMap = new HashMap<String, Class<?>>();

    static {
        portraitMap.put(ParameterSaturation.sParameterType, SliderSaturation.class);
        portraitMap.put(ParameterHue.sParameterType, SliderHue.class);
        portraitMap.put(ParameterOpacity.sParameterType, SliderOpacity.class);
        portraitMap.put(ParameterBrightness.sParameterType, SliderBrightness.class);
        portraitMap.put(ParameterColor.sParameterType, ColorChooser.class);

        portraitMap.put(ParameterInteger.sParameterType, BasicSlider.class);
        //        portraitMap.put(ParameterActionAndInt.sParameterType, ActionSlider.class);
        portraitMap.put(ParameterStyles.sParameterType, StyleChooser.class);
    }

    static Constructor<?> getConstructor(Class<?> cl) {
        try {
            return cl.getConstructor(Context.class, ViewGroup.class);
        } catch (Exception e) {
            return null;
        }
    }

    public ParametricEditor() {
        super(ID);
    }

    protected ParametricEditor(int id) {
        super(id);
    }

    protected ParametricEditor(int id, int layoutID, int viewID) {
        super(id);
        mLayoutID = layoutID;
        mViewID = viewID;
    }

    @Override
    public String calculateUserMessage(Context context, String effectName, Object parameterValue) {

        String apply = "";
        if (mShowParameter == SHOW_VALUE_INT) {
            if (getLocalRepresentation() instanceof FilterBasicRepresentation) {
                FilterBasicRepresentation interval = (FilterBasicRepresentation) getLocalRepresentation();
                apply += " " + effectName.toUpperCase() + " " + interval.getStateRepresentation();
            } else {
                apply += " " + effectName.toUpperCase() + " " + parameterValue;
            }
        } else {
            apply += " " + effectName.toUpperCase();
        }
        return apply;
    }

    @Override
    public void createEditor(Context context, FrameLayout frameLayout) {
        super.createEditor(context, frameLayout);
        unpack(mViewID, mLayoutID);
    }

    @Override
    public void reflectCurrentFilter() {
        super.reflectCurrentFilter();
        if (getLocalRepresentation() != null && getLocalRepresentation() instanceof FilterBasicRepresentation) {
            FilterBasicRepresentation interval = (FilterBasicRepresentation) getLocalRepresentation();
            mControl.setPrameter(interval);
        }
    }

    @Override
    public Control[] getControls() {
        BasicSlider slider = new BasicSlider();
        return new Control[] {
                slider
        };
    }

    protected Parameter getParameterToEdit(FilterRepresentation rep) {
        if (this instanceof Parameter) {
            return (Parameter) this;
        } else if (rep instanceof Parameter) {
            return ((Parameter) rep);
        }
        return null;
    }

    @Override
    public void setUtilityPanelUI(View actionButton, View editControl) {
        mActionButton = actionButton;
        mEditControl = editControl;
        FilterRepresentation rep = getLocalRepresentation();
        Parameter param = getParameterToEdit(rep);
        if (param != null) {
            control(param, editControl);
        } else {
            mSeekBar = new SeekBar(editControl.getContext());
            LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            mSeekBar.setLayoutParams(lp);
            ((LinearLayout) editControl).addView(mSeekBar);
            mSeekBar.setOnSeekBarChangeListener(this);
        }
    }

    protected void control(Parameter p, View editControl) {
        String pType = p.getParameterType();
        Class<?> c = portraitMap.get(pType);

        if (c != null) {
            try {
                mControl = (Control) c.newInstance();
                p.setController(mControl);
                mControl.setUp((ViewGroup) editControl, p, this);
            } catch (Exception e) {
                Log.e(TAG, "Error in loading Control ", e);
            }
        } else {
            Log.e(TAG, "Unable to find class for " + pType);
            for (String string : portraitMap.keySet()) {
                Log.e(TAG, "for " + string + " use " + portraitMap.get(string));
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar sbar, int progress, boolean arg2) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
    }
}
