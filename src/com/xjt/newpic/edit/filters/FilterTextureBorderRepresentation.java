
package com.xjt.newpic.edit.filters;

import java.io.IOException;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.controller.BasicParameterInt;
import com.xjt.newpic.edit.controller.Parameter;
import com.xjt.newpic.edit.controller.ParameterTexture;
import com.xjt.newpic.edit.editors.EditorTextureBorder;
import com.xjt.newpic.surpport.JsonReader;
import com.xjt.newpic.surpport.JsonWriter;

public class FilterTextureBorderRepresentation extends FilterRepresentation {

    private static final String TAG = FilterTextureBorderRepresentation.class.getSimpleName();

    private static final String SERIALIZATION_NAME = "TEXTUREBODER";
    public static final int PARAM_SIZE = 0;
    public static final int PARAM_RADIUS = 1;
    public static final int PARAM_TEXTURE = 2;
    public static final int PARAM_CLEAR = 3;
    public static int DEFAULT_TEXTURE1 = R.drawable.edit_border_tile1;
    public static int DEFAULT_TEXTURE2 = R.drawable.edit_border_tile17;
    public static int DEFAULT_TEXTURE3 = R.drawable.edit_border_tile134;
    public static int DEFAULT_TEXTURE4 = R.drawable.edit_border_tile127;
    public static int DEFAULT_TEXTURE5 = R.drawable.edit_border_tile12;

    private BasicParameterInt mParamSize = new BasicParameterInt(PARAM_SIZE, 3, 2, 30);
    private BasicParameterInt mParamRadius = new BasicParameterInt(PARAM_RADIUS, 2, 0, 100);
    private ParameterTexture mParamTexture = new ParameterTexture(PARAM_TEXTURE, DEFAULT_TEXTURE1);

    private Parameter[] mAllParam = {
            mParamSize,
            mParamRadius,
            mParamTexture
    };
    private int mPramMode;

    public FilterTextureBorderRepresentation(int texture, int size, int radius, int sr) {
        super(SERIALIZATION_NAME, sr);
        setSerializationName(SERIALIZATION_NAME);
        setFilterType(FilterRepresentation.TYPE_BORDER);
        setTextId(R.string.texure_border);
        setEditorId(EditorTextureBorder.ID);
        setShowParameterValue(false);
        setFilterClass(ImageFilterTextureBorder.class);
        mParamTexture.setValue(texture);
        mParamSize.setValue(size);
        mParamRadius.setValue(radius);
        mParamTexture.setTexturePalette(new int[] {
                DEFAULT_TEXTURE1,
                DEFAULT_TEXTURE2,
                DEFAULT_TEXTURE3,
                DEFAULT_TEXTURE4,
                DEFAULT_TEXTURE5
        });
    }

    public String toString() {
        return "FilterBorder: " + getName();
    }

    @Override
    public FilterRepresentation copy() {
        FilterTextureBorderRepresentation representation = new FilterTextureBorderRepresentation(0, 0, 0, 0);
        copyAllParameters(representation);
        return representation;
    }

    @Override
    protected void copyAllParameters(FilterRepresentation representation) {
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    public void useParametersFrom(FilterRepresentation a) {
        if (a instanceof FilterTextureBorderRepresentation) {
            FilterTextureBorderRepresentation representation = (FilterTextureBorderRepresentation) a;
            setName(representation.getName());
            setTexture(representation.getTexture());
            mParamTexture.copyPalletFrom(representation.mParamTexture);
            setBorderSize(representation.getBorderSize());
            setBorderRadius(representation.getBorderRadius());
        }
    }

    @Override
    public boolean equals(FilterRepresentation representation) {
        if (!super.equals(representation)) {
            return false;
        }
        if (representation instanceof FilterTextureBorderRepresentation) {
            FilterTextureBorderRepresentation border = (FilterTextureBorderRepresentation) representation;
            if (border.mParamTexture.getValue() == mParamTexture.getValue()
                    && border.mParamRadius.getValue() == mParamRadius.getValue()
                    && border.mParamSize.getValue() == mParamSize.getValue()) {

                return true;
            }
        }
        return false;
    }

    public boolean allowsSingleInstanceOnly() {
        return true;
    }

    public Parameter getParam(int mode) {
        return mAllParam[mode];
    }

    @Override
    public int getTextId() {
        if (super.getTextId() == 0) {
            return R.string.borders;
        }
        return super.getTextId();
    }

    public int getTexture() {
        return mParamTexture.getValue();
    }

    public void setTexture(int texture) {
        mParamTexture.setValue(texture);
    }

    public int getBorderSize() {
        return mParamSize.getValue();
    }

    public void setBorderSize(int borderSize) {
        mParamSize.setValue(borderSize);
    }

    public int getBorderRadius() {
        return mParamRadius.getValue();
    }

    public void setBorderRadius(int borderRadius) {
        mParamRadius.setValue(borderRadius);
    }

    public void setPramMode(int pramMode) {
        this.mPramMode = pramMode;
    }

    public Parameter getCurrentParam() {
        return mAllParam[mPramMode];
    }

    public String getValueString() {
        return "";
    }

    public void serializeRepresentation(JsonWriter writer) throws IOException {
        writer.beginObject();
        {
            writer.name("size");
            writer.value(mParamSize.getValue());
            writer.name("radius");
            writer.value(mParamRadius.getValue());
            writer.name("texture");
            writer.value(mParamTexture.getValue());
        }
        writer.endObject();
    }

    public void deSerializeRepresentation(JsonReader reader) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equalsIgnoreCase("size")) {
                mParamSize.setValue(reader.nextInt());
            } else if (name.equalsIgnoreCase("radius")) {
                mParamRadius.setValue(reader.nextInt());
            } else if (name.equalsIgnoreCase("texture")) {
                mParamTexture.setValue(reader.nextInt());
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }
}
