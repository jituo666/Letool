
package com.xjt.newpic.edit.filters;

import com.xjt.newpic.edit.editors.ImageOnlyEditor;

public class FilterFxRepresentation extends FilterRepresentation {

    private static final String TAG = FilterFxRepresentation.class.getSimpleName();
    private int mBitmapResource = 0;
    private int mNameResource = 0;

    public FilterFxRepresentation(String name, int bitmapResource, int sampleResource, int nameResource) {
        super(name,bitmapResource);
        setFilterClass(ImageFilterFx.class);
        mBitmapResource = bitmapResource;
        mNameResource = nameResource;
        setFilterType(FilterRepresentation.TYPE_FX);
        setTextId(nameResource);
        setEditorId(ImageOnlyEditor.ID);
        setShowParameterValue(false);
        setSupportsPartialRendering(true);
    }

    @Override
    public String toString() {
        return "FilterFx: " + hashCode() + " : " + getName() + " bitmap rsc: " + mBitmapResource;
    }

    @Override
    public FilterRepresentation copy() {
        FilterFxRepresentation representation = new FilterFxRepresentation(getName(), 0, 0, 0);
        copyAllParameters(representation);
        return representation;
    }

    @Override
    protected void copyAllParameters(FilterRepresentation representation) {
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    @Override
    public synchronized void useParametersFrom(FilterRepresentation a) {
        if (a instanceof FilterFxRepresentation) {
            FilterFxRepresentation representation = (FilterFxRepresentation) a;
            setName(representation.getName());
            setSerializationName(representation.getSerializationName());
            setBitmapResource(representation.getBitmapResource());
            setNameResource(representation.getNameResource());
        }
    }

    @Override
    public boolean equals(FilterRepresentation representation) {
        if (!super.equals(representation)) {
            return false;
        }
        if (representation instanceof FilterFxRepresentation) {
            FilterFxRepresentation fx = (FilterFxRepresentation) representation;
            if (fx.mNameResource == mNameResource && fx.mBitmapResource == mBitmapResource) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean same(FilterRepresentation representation) {
        if (!super.same(representation)) {
            return false;
        }
        return equals(representation);
    }

    @Override
    public boolean allowsSingleInstanceOnly() {
        return true;
    }

    public int getBitmapResource() {
        return mBitmapResource;
    }

    public void setBitmapResource(int bitmapResource) {
        mBitmapResource = bitmapResource;
    }

    public int getNameResource() {
        return mNameResource;
    }

    public void setNameResource(int nameResource) {
        mNameResource = nameResource;
    }
}
