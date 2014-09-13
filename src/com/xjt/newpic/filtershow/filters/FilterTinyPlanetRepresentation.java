package com.xjt.newpic.filtershow.filters;

import com.xjt.newpic.R;
import com.xjt.newpic.filtershow.editors.EditorTinyPlanet;

public class FilterTinyPlanetRepresentation extends FilterBasicRepresentation {

    private static final String SERIALIZATION_NAME = "TINYPLANET";
    private static final String TAG = "FilterTinyPlanetRepresentation";
    private static final String SERIAL_ANGLE = "Angle";
    private float mAngle = 0;

    public FilterTinyPlanetRepresentation(int sr) {
        super("TinyPlanet", 0, 50, 100, sr);
        setSerializationName(SERIALIZATION_NAME);
        setShowParameterValue(true);
        setFilterClass(ImageFilterTinyPlanet.class);
        setFilterType(FilterRepresentation.TYPE_TINYPLANET);
        setTextId(R.string.tinyplanet);
        setEditorId(EditorTinyPlanet.ID);
        setMinimum(1);
        setSupportsPartialRendering(false);
    }

    @Override
    public FilterRepresentation copy() {
        FilterTinyPlanetRepresentation representation = new FilterTinyPlanetRepresentation(0);
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
        FilterTinyPlanetRepresentation representation = (FilterTinyPlanetRepresentation) a;
        super.useParametersFrom(a);
        mAngle = representation.mAngle;
        setZoom(representation.getZoom());
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }

    public float getAngle() {
        return mAngle;
    }

    public int getZoom() {
        return getValue();
    }

    public void setZoom(int zoom) {
        setValue(zoom);
    }

    public boolean isNil() {
        // TinyPlanet always has an effect
        return false;
    }

    public boolean equals(FilterRepresentation representation) {
        if (!super.equals(representation)) {
            return false;
        }
        if (mAngle == ((FilterTinyPlanetRepresentation) representation).mAngle) {
            return true;
        }
        return false;
    }

    @Override
    public String[][] serializeRepresentation() {
        String[][] ret = {
                {
                        SERIAL_NAME, getName()
                },
                {
                        SERIAL_VALUE, Integer.toString(getValue())
                },
                {
                        SERIAL_ANGLE, Float.toString(mAngle)
                }
        };
        return ret;
    }

    @Override
    public void deSerializeRepresentation(String[][] rep) {
        super.deSerializeRepresentation(rep);
        for (int i = 0; i < rep.length; i++) {
            if (SERIAL_VALUE.equals(rep[i][0])) {
                setValue(Integer.parseInt(rep[i][1]));
            } else if (SERIAL_ANGLE.equals(rep[i][0])) {
                setAngle(Float.parseFloat(rep[i][1]));
            }
        }
    }
}
