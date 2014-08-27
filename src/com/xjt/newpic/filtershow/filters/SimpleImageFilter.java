package com.xjt.newpic.filtershow.filters;

public class SimpleImageFilter extends ImageFilter {

    private FilterBasicRepresentation mParameters;

    public FilterRepresentation getDefaultRepresentation() {
        FilterRepresentation representation = new FilterBasicRepresentation("Default", 0, 50, 100);
        representation.setShowParameterValue(true);
        return representation;
    }

    public void useRepresentation(FilterRepresentation representation) {
        FilterBasicRepresentation parameters = (FilterBasicRepresentation) representation;
        mParameters = parameters;
    }

    public FilterBasicRepresentation getParameters() {
        return mParameters;
    }
}
