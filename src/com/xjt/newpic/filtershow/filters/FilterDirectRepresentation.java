
package com.xjt.newpic.filtershow.filters;

public class FilterDirectRepresentation extends FilterRepresentation {

    @Override
    public FilterRepresentation copy() {
        FilterDirectRepresentation representation = new FilterDirectRepresentation(getName(), 0);
        copyAllParameters(representation);
        return representation;
    }

    @Override
    protected void copyAllParameters(FilterRepresentation representation) {
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    public FilterDirectRepresentation(String name, int sr) {
        super(name, sr);
    }

}
