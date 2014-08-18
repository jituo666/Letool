package com.xjt.newpic.filtershow.controller;


public interface Parameter {
    public String getParameterName();

    public String getParameterType();

    public String getValueString();

    public void setController(Control c);

    public void setFilterView(FilterView editor);

    public void copyFrom(Parameter src);
}
