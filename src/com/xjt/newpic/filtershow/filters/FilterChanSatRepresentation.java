package com.xjt.newpic.filtershow.filters;


import com.xjt.newpic.R;
import com.xjt.newpic.filtershow.controller.BasicParameterInt;
import com.xjt.newpic.filtershow.controller.Parameter;
import com.xjt.newpic.filtershow.controller.ParameterSet;
import com.xjt.newpic.filtershow.editors.EditorChanSat;
import com.xjt.newpic.filtershow.imageshow.ControlPoint;
import com.xjt.newpic.filtershow.imageshow.Spline;
import com.xjt.newpic.surpport.JsonReader;
import com.xjt.newpic.surpport.JsonWriter;

import java.io.IOException;
import java.util.Vector;

/**
 * Representation for a filter that has per channel & Master saturation
 */
public class FilterChanSatRepresentation extends FilterRepresentation implements ParameterSet {
    private static final String LOGTAG = "FilterChanSatRepresentation";
    private static final String ARGS = "ARGS";
    private static final String SERIALIZATION_NAME = "channelsaturation";

    public static final int MODE_MASTER = 0;
    public static final int MODE_RED = 1;
    public static final int MODE_YELLOW = 2;
    public static final int MODE_GREEN = 3;
    public static final int MODE_CYAN = 4;
    public static final int MODE_BLUE = 5;
    public static final int MODE_MAGENTA = 6;
    private int mParameterMode = MODE_MASTER;

    private static int MINSAT = -100;
    private static int MAXSAT = 100;
    private BasicParameterInt mParamMaster = new BasicParameterInt(MODE_MASTER, 0, MINSAT, MAXSAT);
    private BasicParameterInt mParamRed = new BasicParameterInt(MODE_RED, 0, MINSAT, MAXSAT);
    private BasicParameterInt mParamYellow = new BasicParameterInt(MODE_YELLOW, 0, MINSAT, MAXSAT);
    private BasicParameterInt mParamGreen = new BasicParameterInt(MODE_GREEN, 0, MINSAT, MAXSAT);
    private BasicParameterInt mParamCyan = new BasicParameterInt(MODE_CYAN, 0, MINSAT, MAXSAT);
    private BasicParameterInt mParamBlue = new BasicParameterInt(MODE_BLUE, 0, MINSAT, MAXSAT);
    private BasicParameterInt mParamMagenta = new BasicParameterInt(MODE_MAGENTA, 0, MINSAT, MAXSAT);

    private BasicParameterInt[] mAllParam = {
            mParamMaster,
            mParamRed,
            mParamYellow,
            mParamGreen,
            mParamCyan,
            mParamBlue,
            mParamMagenta};

    public FilterChanSatRepresentation(int sr) {
        super("ChannelSaturation", sr);
        setTextId(R.string.saturation);
        setFilterType(FilterRepresentation.TYPE_NORMAL);
        setSerializationName(SERIALIZATION_NAME);
        setFilterClass(ImageFilterChanSat.class);
        setEditorId(EditorChanSat.ID);
        setSupportsPartialRendering(true);
    }

    public String toString() {
        return getName() + " : " + mParamRed + ", " + mParamCyan + ", " + mParamRed
                + ", " + mParamGreen + ", " + mParamMaster + ", " + mParamYellow;
    }

    @Override
    public FilterRepresentation copy() {
        FilterChanSatRepresentation representation = new FilterChanSatRepresentation(0);
        copyAllParameters(representation);
        return representation;
    }

    @Override
    protected void copyAllParameters(FilterRepresentation representation) {
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    public void useParametersFrom(FilterRepresentation a) {
        if (a instanceof FilterChanSatRepresentation) {
            FilterChanSatRepresentation representation = (FilterChanSatRepresentation) a;

            for (int i = 0; i < mAllParam.length; i++) {
                mAllParam[i].copyFrom(representation.mAllParam[i]);
            }
        }
    }

    @Override
    public boolean equals(FilterRepresentation representation) {
        if (!super.equals(representation)) {
            return false;
        }
        if (representation instanceof FilterChanSatRepresentation) {
            FilterChanSatRepresentation rep = (FilterChanSatRepresentation) representation;
            for (int i = 0; i < mAllParam.length; i++) {
                if (rep.getValue(i) != getValue(i))
                    return false;
            }
            return true;
        }
        return false;
    }

    public int getValue(int mode) {
        return mAllParam[mode].getValue();
    }

    public void setValue(int mode, int value) {
        mAllParam[mode].setValue(value);
    }

    public int getMinimum() {
        return mParamMaster.getMinimum();
    }

    public int getMaximum() {
        return mParamMaster.getMaximum();
    }

    public int getParameterMode() {
        return mParameterMode;
    }

    public void setParameterMode(int parameterMode) {
        mParameterMode = parameterMode;
    }

    public int getCurrentParameter() {
        return getValue(mParameterMode);
    }

    public void setCurrentParameter(int value) {
        setValue(mParameterMode, value);
    }

    @Override
    public int getNumberOfParameters() {
        return 6;
    }

    @Override
    public Parameter getFilterParameter(int index) {
        return mAllParam[index];
    }

    @Override
    public void serializeRepresentation(JsonWriter writer) throws IOException {
        writer.beginObject();

        writer.name(ARGS);
        writer.beginArray();
        writer.value(getValue(MODE_MASTER));
        writer.value(getValue(MODE_RED));
        writer.value(getValue(MODE_YELLOW));
        writer.value(getValue(MODE_GREEN));
        writer.value(getValue(MODE_CYAN));
        writer.value(getValue(MODE_BLUE));
        writer.value(getValue(MODE_MAGENTA));
        writer.endArray();
        writer.endObject();
    }

    @Override
    public void deSerializeRepresentation(JsonReader sreader) throws IOException {
        sreader.beginObject();

        while (sreader.hasNext()) {
            String name = sreader.nextName();
            if (name.startsWith(ARGS)) {
                sreader.beginArray();
                sreader.hasNext();
                setValue(MODE_MASTER, sreader.nextInt());
                sreader.hasNext();
                setValue(MODE_RED, sreader.nextInt());
                sreader.hasNext();
                setValue(MODE_YELLOW, sreader.nextInt());
                sreader.hasNext();
                setValue(MODE_GREEN, sreader.nextInt());
                sreader.hasNext();
                setValue(MODE_CYAN, sreader.nextInt());
                sreader.hasNext();
                setValue(MODE_BLUE, sreader.nextInt());
                sreader.hasNext();
                setValue(MODE_MAGENTA, sreader.nextInt());
                sreader.hasNext();
                sreader.endArray();
            } else {
                sreader.skipValue();
            }
        }
        sreader.endObject();
    }
}