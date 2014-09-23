
package com.xjt.newpic.edit.filters;

import com.xjt.newpic.edit.editors.ImageOnlyEditor;

public class FilterImageBorderRepresentation extends FilterRepresentation {

    private int mDrawableResource = 0;

    public FilterImageBorderRepresentation(int drawableResource, int sr) {
        super("ImageBorder", sr);
        setFilterClass(ImageFilterImageBorder.class);
        mDrawableResource = drawableResource;
        setFilterType(FilterRepresentation.TYPE_BORDER);
        setEditorId(ImageOnlyEditor.ID);
        setShowParameterValue(false);
    }

    public String toString() {
        return "FilterBorder: " + getName();
    }

    @Override
    public FilterRepresentation copy() {
        FilterImageBorderRepresentation representation = new FilterImageBorderRepresentation(mDrawableResource, 0);
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
