
package com.xjt.newpic.edit.filters;

import android.util.Log;

import com.xjt.newpic.edit.controller.Control;
import com.xjt.newpic.edit.controller.FilterView;
import com.xjt.newpic.edit.controller.Parameter;
import com.xjt.newpic.edit.controller.ParameterInteger;

public class FilterBasicRepresentation extends FilterRepresentation implements ParameterInteger {

    private static final String LOGTAG = "FilterBasicRep";
    private int mMinimum;
    private int mValue;
    private int mMaximum;
    private int mDefaultValue;
    private int mPreviewValue;
    public static final String SERIAL_NAME = "Name";
    public static final String SERIAL_VALUE = "Value";
    private boolean mLogVerbose = Log.isLoggable(LOGTAG, Log.VERBOSE);

    public FilterBasicRepresentation(String name, int sampleResource, int minimum, int value, int maximum) {
        super(name, sampleResource);
        mMinimum = minimum;
        mMaximum = maximum;
        setValue(value);
    }

    @Override
    public String toString() {
        return getName() + " : " + mMinimum + " < " + mValue + " < " + mMaximum;
    }

    @Override
    public FilterRepresentation copy() {
        FilterBasicRepresentation representation = new FilterBasicRepresentation(getName(), 0, 0, 0, 0);
        copyAllParameters(representation);
        return representation;
    }

    @Override
    protected void copyAllParameters(FilterRepresentation representation) {
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    @Override
    public void useParametersFrom(FilterRepresentation a) {
        if (a instanceof FilterBasicRepresentation) {
            FilterBasicRepresentation representation = (FilterBasicRepresentation) a;
            setMinimum(representation.getMinimum());
            setMaximum(representation.getMaximum());
            setValue(representation.getValue());
            setDefaultValue(representation.getDefaultValue());
            setPreviewValue(representation.getPreviewValue());
        }
    }

    @Override
    public boolean equals(FilterRepresentation representation) {
        if (!super.equals(representation)) {
            return false;
        }
        if (representation instanceof FilterBasicRepresentation) {
            FilterBasicRepresentation basic = (FilterBasicRepresentation) representation;
            if (basic.mMinimum == mMinimum
                    && basic.mMaximum == mMaximum
                    && basic.mValue == mValue
                    && basic.mDefaultValue == mDefaultValue
                    && basic.mPreviewValue == mPreviewValue) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getMinimum() {
        return mMinimum;
    }

    public void setMinimum(int minimum) {
        mMinimum = minimum;
    }

    @Override
    public int getValue() {
        return mValue;
    }

    @Override
    public void setValue(int value) {
        mValue = value;
        if (mValue < mMinimum) {
            mValue = mMinimum;
        }
        if (mValue > mMaximum) {
            mValue = mMaximum;
        }
    }

    @Override
    public int getMaximum() {
        return mMaximum;
    }

    public void setMaximum(int maximum) {
        mMaximum = maximum;
    }

    public void setDefaultValue(int defaultValue) {
        mDefaultValue = defaultValue;
    }

    @Override
    public int getDefaultValue() {
        return mDefaultValue;
    }

    public int getPreviewValue() {
        return mPreviewValue;
    }

    public void setPreviewValue(int previewValue) {
        mPreviewValue = previewValue;
    }

    @Override
    public String getStateRepresentation() {
        int val = getValue();
        return ((val > 0) ? "+" : "") + val;
    }

    @Override
    public String getParameterType() {
        return sParameterType;
    }

    @Override
    public void setController(Control control) {
    }

    @Override
    public String getValueString() {
        return getStateRepresentation();
    }

    @Override
    public String getParameterName() {
        return getName();
    }

    @Override
    public void setFilterView(FilterView editor) {
    }

    @Override
    public void copyFrom(Parameter src) {
        useParametersFrom((FilterBasicRepresentation) src);
    }

    @Override
    public String[][] serializeRepresentation() {
        String[][] ret = {
                {
                        SERIAL_NAME, getName()
                },
                {
                        SERIAL_VALUE, Integer.toString(mValue)
                }
        };
        return ret;
    }

    @Override
    public void deSerializeRepresentation(String[][] rep) {
        super.deSerializeRepresentation(rep);
        for (int i = 0; i < rep.length; i++) {
            if (SERIAL_VALUE.equals(rep[i][0])) {
                mValue = Integer.parseInt(rep[i][1]);
                break;
            }
        }
    }
}
