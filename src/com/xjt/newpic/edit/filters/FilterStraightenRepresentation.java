package com.xjt.newpic.edit.filters;

import android.util.Log;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.editors.EditorStraighten;
import com.xjt.newpic.surpport.JsonReader;
import com.xjt.newpic.surpport.JsonWriter;

import java.io.IOException;

public class FilterStraightenRepresentation extends FilterRepresentation {
    public static final String SERIALIZATION_NAME = "STRAIGHTEN";
    public static final String SERIALIZATION_STRAIGHTEN_VALUE = "value";
    private static final String TAG = FilterStraightenRepresentation.class.getSimpleName();
    public static final int MAX_STRAIGHTEN_ANGLE = 45;
    public static final int MIN_STRAIGHTEN_ANGLE = -45;

    float mStraighten;

    public FilterStraightenRepresentation(float straighten, int sr) {
        super(SERIALIZATION_NAME, sr);
        setSerializationName(SERIALIZATION_NAME);
        setShowParameterValue(true);
        setFilterClass(FilterStraightenRepresentation.class);
        setFilterType(FilterRepresentation.TYPE_GEOMETRY);
        setSupportsPartialRendering(true);
        setTextId(R.string.straighten);
        setEditorId(EditorStraighten.ID);
        setStraighten(straighten);
        setSampleResource(R.drawable.effect_sample_25);
    }

    public FilterStraightenRepresentation(FilterStraightenRepresentation s) {
        this(s.getStraighten(),s.getSampleResource());
        setName(s.getName());
    }

    public FilterStraightenRepresentation(int sr) {
        this(getNil(),sr);
    }

    public void set(FilterStraightenRepresentation r) {
        mStraighten = r.mStraighten;
    }

    @Override
    public boolean equals(FilterRepresentation rep) {
        if (!(rep instanceof FilterStraightenRepresentation)) {
            return false;
        }
        FilterStraightenRepresentation straighten = (FilterStraightenRepresentation) rep;
        if (straighten.mStraighten != mStraighten) {
            return false;
        }
        return true;
    }

    public float getStraighten() {
        return mStraighten;
    }

    public void setStraighten(float straighten) {
        if (!rangeCheck(straighten)) {
            straighten = Math.min(Math.max(straighten, MIN_STRAIGHTEN_ANGLE), MAX_STRAIGHTEN_ANGLE);
        }
        mStraighten = straighten;
    }

    @Override
    public boolean allowsSingleInstanceOnly() {
        return true;
    }

    @Override
    public FilterRepresentation copy() {
        return new FilterStraightenRepresentation(this);
    }

    @Override
    protected void copyAllParameters(FilterRepresentation representation) {
        if (!(representation instanceof FilterStraightenRepresentation)) {
            throw new IllegalArgumentException("calling copyAllParameters with incompatible types!");
        }
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    @Override
    public void useParametersFrom(FilterRepresentation a) {
        if (!(a instanceof FilterStraightenRepresentation)) {
            throw new IllegalArgumentException("calling useParametersFrom with incompatible types!");
        }
        setStraighten(((FilterStraightenRepresentation) a).getStraighten());
    }

    @Override
    public boolean isNil() {
        return mStraighten == getNil();
    }

    public static float getNil() {
        return 0;
    }

    @Override
    public void serializeRepresentation(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name(SERIALIZATION_STRAIGHTEN_VALUE).value(mStraighten);
        writer.endObject();
    }

    @Override
    public void deSerializeRepresentation(JsonReader reader) throws IOException {
        boolean unset = true;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (SERIALIZATION_STRAIGHTEN_VALUE.equals(name)) {
                float s = (float) reader.nextDouble();
                if (rangeCheck(s)) {
                    setStraighten(s);
                    unset = false;
                }
            } else {
                reader.skipValue();
            }
        }
        if (unset) {
            Log.w(TAG, "WARNING: bad value when deserializing " + SERIALIZATION_NAME);
        }
        reader.endObject();
    }

    private boolean rangeCheck(double s) {
        if (s < -45 || s > 45) {
            return false;
        }
        return true;
    }
}
