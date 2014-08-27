package com.xjt.newpic.filtershow.filters;

import com.xjt.newpic.R;
import com.xjt.newpic.filtershow.editors.ImageOnlyEditor;

public class FilterImageBorderRepresentation extends FilterRepresentation {
    private int mDrawableResource = 0;

    public FilterImageBorderRepresentation(int drawableResource) {
        super("ImageBorder");
        setFilterClass(ImageFilterBorder.class);
        mDrawableResource = drawableResource;
        setFilterType(FilterRepresentation.TYPE_BORDER);
        setTextId(R.string.borders);
        setEditorId(ImageOnlyEditor.ID);
        setShowParameterValue(false);
    }

    public String toString() {
        return "FilterBorder: " + getName();
    }

    @Override
    public FilterRepresentation copy() {
        FilterImageBorderRepresentation representation =
                new FilterImageBorderRepresentation(mDrawableResource);
        copyAllParameters(representation);
        return representation;
    }

    @Override
    protected void copyAllParameters(FilterRepresentation representation) {
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    public void useParametersFrom(FilterRepresentation a) {
        if (a instanceof FilterImageBorderRepresentation) {
            FilterImageBorderRepresentation representation = (FilterImageBorderRepresentation) a;
            setName(representation.getName());
            setDrawableResource(representation.getDrawableResource());
        }
    }

    @Override
    public boolean equals(FilterRepresentation representation) {
        if (!super.equals(representation)) {
            return false;
        }
        if (representation instanceof FilterImageBorderRepresentation) {
            FilterImageBorderRepresentation border = (FilterImageBorderRepresentation) representation;
            if (border.mDrawableResource == mDrawableResource) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getTextId() {
        return R.string.none;
    }

    public boolean allowsSingleInstanceOnly() {
        return true;
    }

    public int getDrawableResource() {
        return mDrawableResource;
    }

    public void setDrawableResource(int drawableResource) {
        mDrawableResource = drawableResource;
    }
}
