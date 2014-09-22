
package com.xjt.newpic.edit.filters;

import android.graphics.Color;
import android.graphics.Path;
import android.util.Log;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.controller.BasicParameterInt;
import com.xjt.newpic.edit.controller.Parameter;
import com.xjt.newpic.edit.controller.ParameterColor;
import com.xjt.newpic.edit.controller.ParameterStyle;
import com.xjt.newpic.edit.editors.EditorDraw;
import com.xjt.newpic.surpport.JsonReader;
import com.xjt.newpic.surpport.JsonWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

public class FilterDrawRepresentation extends FilterRepresentation {

    private static final String TAG = FilterDrawRepresentation.class.getSimpleName();

    public static final int PARAM_SIZE = 0;
    public static final int PARAM_STYLE = 1;
    public static final int PARAM_COLOR = 2;

    public static int DEFAULT_STYLE1 = R.drawable.brush_flat;
    public static int DEFAULT_STYLE2 = R.drawable.brush_round;
    public static int DEFAULT_STYLE3 = R.drawable.brush_gauss;
    public static int DEFAULT_STYLE4 = R.drawable.brush_marker;
    public static int DEFAULT_STYLE5 = R.drawable.brush_spatter;
    
    public static int DEFAULT_COLOR1 = Color.RED & 0x80FFFFFF;
    public static int DEFAULT_COLOR2 = Color.GREEN & 0x80FFFFFF;
    public static int DEFAULT_COLOR3 = Color.BLUE & 0x80FFFFFF;
    public static int DEFAULT_COLOR4 = Color.BLACK & 0x80FFFFFF;
    public static int DEFAULT_COLOR5 = Color.WHITE & 0x80FFFFFF;

    private static final String SERIAL_COLOR = "color";
    private static final String SERIAL_RADIUS = "radius";
    private static final String SERIAL_TYPE = "type";
    private static final String SERIAL_POINTS_COUNT = "point_count";
    private static final String SERIAL_POINTS = "points";
    private static final String SERIAL_PATH = "path";

    private ParameterColor mParamColor = new ParameterColor(PARAM_COLOR, DEFAULT_COLOR1);
    private BasicParameterInt mParamSize = new BasicParameterInt(PARAM_SIZE, 30, 2, 300);
    private ParameterStyle mParamStyle = new ParameterStyle(PARAM_STYLE, R.drawable.brush_flat);
    private int mParamMode;

    private Vector<StrokeData> mAllDrawings = new Vector<StrokeData>();
    private StrokeData mCurrent; // used in the currently drawing style

    private Parameter[] mAllParam = {
            mParamSize,
            mParamStyle,
            mParamColor
    };

    public FilterDrawRepresentation(int sr) {
        super("Draw", sr);
        setFilterClass(ImageFilterDraw.class);
        setSerializationName("DRAW");
        setFilterType(FilterRepresentation.TYPE_VIGNETTE);
        setTextId(R.string.imageDraw);
        setEditorId(EditorDraw.ID);
        setOverlayId(R.drawable.filtershow_drawing);
        setOverlayOnly(true);
    }

    public void setPramMode(int mode) {
        mParamMode = mode;
    }

    public int getParamMode() {
        return mParamMode;
    }

    public Parameter getCurrentParam() {
        return mAllParam[mParamMode];
    }

    public Parameter getParam(int type) {
        return mAllParam[type];
    }

    public Vector<StrokeData> getDrawing() {
        return mAllDrawings;
    }

    public StrokeData getCurrentDrawing() {
        return mCurrent;
    }

    private int computeCurrentColor() {
        return mParamColor.getValue();
    }

    public void fillStrokeParameters(StrokeData sd) {
        int type = mParamStyle.getValue();
        int color = computeCurrentColor();
        float size = mParamSize.getValue();
        sd.mColor = color;
        sd.mRadius = size;
        sd.mType = type;
    }

    public void startNewSection(float x, float y) {
        mCurrent = new StrokeData();
        fillStrokeParameters(mCurrent);
        mCurrent.mPath = new Path();
        mCurrent.mPath.moveTo(x, y);
        mCurrent.mPoints[0] = x;
        mCurrent.mPoints[1] = y;
        mCurrent.noPoints = 1;
    }

    public void addPoint(float x, float y) {
        int len = mCurrent.noPoints * 2;
        mCurrent.mPath.lineTo(x, y);
        if ((len + 2) > mCurrent.mPoints.length) {
            mCurrent.mPoints = Arrays.copyOf(mCurrent.mPoints, mCurrent.mPoints.length * 2);
        }
        mCurrent.mPoints[len] = x;
        mCurrent.mPoints[len + 1] = y;
        mCurrent.noPoints++;
    }

    public void endSection(float x, float y) {
        addPoint(x, y);
        mAllDrawings.add(mCurrent);
        mCurrent = null;
    }

    public void clearCurrentSection() {
        mCurrent = null;
    }

    public void clear() {
        mCurrent = null;
        mAllDrawings.clear();
    }

    static String colorHexString(int val) {
        String str = "00000000" + Integer.toHexString(val);
        str = "0x" + str.substring(str.length() - 8);
        return str;
    }

    public String getValueString() {
        int val;
        switch (mParamMode) {
            case PARAM_COLOR:
                val = ((ParameterColor) mAllParam[mParamMode]).getValue();
                return "";
            case PARAM_SIZE:
                val = ((BasicParameterInt) mAllParam[mParamMode]).getValue();
                return ((val > 0) ? " +" : " ") + val;
            case PARAM_STYLE:
                return "";
        }
        return "";
    }

    @Override
    public String toString() {
        return getName() + " : strokes=" + mAllDrawings.size() + ((mCurrent == null) ? " no current " : ("draw=" + mCurrent.mType + " " + mCurrent.noPoints));
    }

    @Override
    public FilterRepresentation copy() {
        FilterDrawRepresentation representation = new FilterDrawRepresentation(0);
        copyAllParameters(representation);
        return representation;
    }

    @Override
    protected void copyAllParameters(FilterRepresentation representation) {
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    @Override
    public boolean isNil() {
        return getDrawing().isEmpty();
    }

    @Override
    public void useParametersFrom(FilterRepresentation a) {
        if (a instanceof FilterDrawRepresentation) {
            FilterDrawRepresentation representation = (FilterDrawRepresentation) a;
            mParamColor.copyPalletFrom(representation.mParamColor);
            try {
                if (representation.mCurrent != null) {
                    mCurrent = (StrokeData) representation.mCurrent.clone();
                } else {
                    mCurrent = null;
                }
                if (representation.mAllDrawings != null) {
                    mAllDrawings = new Vector<StrokeData>();
                    for (Iterator<StrokeData> elem = representation.mAllDrawings.iterator(); elem.hasNext();) {
                        StrokeData next = elem.next();
                        mAllDrawings.add(new StrokeData(next));
                    }
                } else {
                    mAllDrawings = null;
                }

            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        } else {
            Log.v(TAG, "cannot use parameters from " + a);
        }
    }

    @Override
    public boolean equals(FilterRepresentation representation) {
        if (!super.equals(representation)) {
            return false;
        }
        if (representation instanceof FilterDrawRepresentation) {
            FilterDrawRepresentation fdRep = (FilterDrawRepresentation) representation;
            if (fdRep.mAllDrawings.size() != mAllDrawings.size())
                return false;
            if (fdRep.mCurrent == null ^ (mCurrent == null || mCurrent.mPath == null)) {
                return false;
            }

            if (fdRep.mCurrent != null && mCurrent != null && mCurrent.mPath != null) {
                if (fdRep.mCurrent.noPoints == mCurrent.noPoints) {
                    return true;
                }
                return false;
            }

            int n = mAllDrawings.size();
            for (int i = 0; i < n; i++) {
                StrokeData a = mAllDrawings.get(i);
                StrokeData b = mAllDrawings.get(i);
                if (!a.equals(b)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void serializeRepresentation(JsonWriter writer) throws IOException {
        writer.beginObject();
        int len = mAllDrawings.size();

        for (int i = 0; i < len; i++) {
            writer.name(SERIAL_PATH + i);
            writer.beginObject();
            StrokeData data = mAllDrawings.get(i);
            writer.name(SERIAL_COLOR).value(data.mColor);
            writer.name(SERIAL_RADIUS).value(data.mRadius);
            writer.name(SERIAL_TYPE).value(data.mType);
            writer.name(SERIAL_POINTS_COUNT).value(data.noPoints);
            writer.name(SERIAL_POINTS);

            writer.beginArray();
            int npoints = data.noPoints * 2;
            for (int j = 0; j < npoints; j++) {
                writer.value(data.mPoints[j]);
            }
            writer.endArray();
            writer.endObject();
        }
        writer.endObject();
    }

    @Override
    public void deSerializeRepresentation(JsonReader sreader) throws IOException {
        sreader.beginObject();
        Vector<StrokeData> strokes = new Vector<StrokeData>();

        while (sreader.hasNext()) {
            sreader.nextName();
            sreader.beginObject();
            StrokeData stroke = new StrokeData();

            while (sreader.hasNext()) {
                String name = sreader.nextName();
                if (name.equals(SERIAL_COLOR)) {
                    stroke.mColor = sreader.nextInt();
                } else if (name.equals(SERIAL_RADIUS)) {
                    stroke.mRadius = (float) sreader.nextDouble();
                } else if (name.equals(SERIAL_TYPE)) {
                    stroke.mType = sreader.nextInt();
                } else if (name.equals(SERIAL_POINTS_COUNT)) {
                    stroke.noPoints = sreader.nextInt();
                } else if (name.equals(SERIAL_POINTS)) {

                    int count = 0;
                    sreader.beginArray();
                    while (sreader.hasNext()) {
                        if ((count + 1) > stroke.mPoints.length) {
                            stroke.mPoints = Arrays.copyOf(stroke.mPoints, count * 2);
                        }
                        stroke.mPoints[count++] = (float) sreader.nextDouble();
                    }
                    stroke.mPath = new Path();
                    stroke.mPath.moveTo(stroke.mPoints[0], stroke.mPoints[1]);
                    for (int i = 0; i < count; i += 2) {
                        stroke.mPath.lineTo(stroke.mPoints[i], stroke.mPoints[i + 1]);
                    }
                    sreader.endArray();
                    strokes.add(stroke);
                } else {
                    sreader.skipValue();
                }
            }
            sreader.endObject();
        }

        mAllDrawings = strokes;

        sreader.endObject();
    }

    public static class StrokeData implements Cloneable {

        public int mType;
        public Path mPath;
        public float mRadius;
        public int mColor;
        public int noPoints = 0;
        public float[] mPoints = new float[20];

        public StrokeData() {
        }

        public StrokeData(StrokeData copy) {
            mType = copy.mType;
            mPath = new Path(copy.mPath);
            mRadius = copy.mRadius;
            mColor = copy.mColor;
            noPoints = copy.noPoints;
            mPoints = Arrays.copyOf(copy.mPoints, copy.mPoints.length);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof StrokeData)) {
                return false;
            }
            StrokeData sd = (StrokeData) o;
            if (mType != sd.mType || mRadius != sd.mRadius || noPoints != sd.noPoints || mColor != sd.mColor) {
                return false;
            }
            return mPath.equals(sd.mPath);
        }

        @Override
        public String toString() {
            return "stroke(" + mType + ", path(" + (mPath) + "), " + mRadius + " , " + Integer.toHexString(mColor) + ")";
        }

        @Override
        public StrokeData clone() throws CloneNotSupportedException {
            return (StrokeData) super.clone();
        }
    }
}
