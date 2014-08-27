package com.xjt.newpic.filtershow.filters;

import android.util.Log;

import com.xjt.newpic.R;
import com.xjt.newpic.filtershow.editors.ImageOnlyEditor;
import com.xjt.newpic.surpport.JsonReader;
import com.xjt.newpic.surpport.JsonWriter;

import java.io.IOException;

public class FilterRotateRepresentation extends FilterRepresentation {
    public static final String SERIALIZATION_NAME = "ROTATION";
    public static final String SERIALIZATION_ROTATE_VALUE = "value";
    private static final String TAG = FilterRotateRepresentation.class.getSimpleName();

    Rotation mRotation;

    public enum Rotation {
        ZERO(0), NINETY(90), ONE_EIGHTY(180), TWO_SEVENTY(270);
        private final int mValue;

        private Rotation(int value) {
            mValue = value;
        }

        public int value() {
            return mValue;
        }

        public static Rotation fromValue(int value) {
            switch (value) {
                case 0:
                    return ZERO;
                case 90:
                    return NINETY;
                case 180:
                    return ONE_EIGHTY;
                case 270:
                    return TWO_SEVENTY;
                default:
                    return null;
            }
        }
    }

    public FilterRotateRepresentation(Rotation rotation) {
        super(SERIALIZATION_NAME);
        setSerializationName(SERIALIZATION_NAME);
        setShowParameterValue(false);
        setFilterClass(FilterRotateRepresentation.class);
        setFilterType(FilterRepresentation.TYPE_GEOMETRY);
        setSupportsPartialRendering(true);
        setTextId(R.string.rotate);
        setEditorId(ImageOnlyEditor.ID);
        setRotation(rotation);
    }

    public FilterRotateRepresentation(FilterRotateRepresentation r) {
        this(r.getRotation());
        setName(r.getName());
    }

    public FilterRotateRepresentation() {
        this(getNil());
    }

    public Rotation getRotation() {
        return mRotation;
    }

    public void rotateCW() {
        switch(mRotation) {
            case ZERO:
                mRotation = Rotation.NINETY;
                break;
            case NINETY:
                mRotation = Rotation.ONE_EIGHTY;
                break;
            case ONE_EIGHTY:
                mRotation = Rotation.TWO_SEVENTY;
                break;
            case TWO_SEVENTY:
                mRotation = Rotation.ZERO;
                break;
        }
    }

    public void set(FilterRotateRepresentation r) {
        mRotation = r.mRotation;
    }

    public void setRotation(Rotation rotation) {
        if (rotation == null) {
            throw new IllegalArgumentException("Argument to setRotation is null");
        }
        mRotation = rotation;
    }

    @Override
    public boolean allowsSingleInstanceOnly() {
        return true;
    }

    @Override
    public FilterRepresentation copy() {
        return new FilterRotateRepresentation(this);
    }

    @Override
    protected void copyAllParameters(FilterRepresentation representation) {
        if (!(representation instanceof FilterRotateRepresentation)) {
            throw new IllegalArgumentException("calling copyAllParameters with incompatible types!");
        }
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    @Override
    public void useParametersFrom(FilterRepresentation a) {
        if (!(a instanceof FilterRotateRepresentation)) {
            throw new IllegalArgumentException("calling useParametersFrom with incompatible types!");
        }
        setRotation(((FilterRotateRepresentation) a).getRotation());
    }

    @Override
    public boolean isNil() {
        return mRotation == getNil();
    }

    public static Rotation getNil() {
        return Rotation.ZERO;
    }

    @Override
    public void serializeRepresentation(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name(SERIALIZATION_ROTATE_VALUE).value(mRotation.value());
        writer.endObject();
    }

    @Override
    public boolean equals(FilterRepresentation rep) {
        if (!(rep instanceof FilterRotateRepresentation)) {
            return false;
        }
        FilterRotateRepresentation rotate = (FilterRotateRepresentation) rep;
        if (rotate.mRotation.value() != mRotation.value()) {
            return false;
        }
        return true;
    }

    @Override
    public void deSerializeRepresentation(JsonReader reader) throws IOException {
        boolean unset = true;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (SERIALIZATION_ROTATE_VALUE.equals(name)) {
                Rotation r = Rotation.fromValue(reader.nextInt());
                if (r != null) {
                    setRotation(r);
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
}
