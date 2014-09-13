package com.xjt.newpic.edit.editors;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.controller.Control;
import com.xjt.newpic.edit.controller.FilterView;
import com.xjt.newpic.edit.controller.Parameter;
import com.xjt.newpic.edit.controller.ParameterInteger;
import com.xjt.newpic.edit.filters.FilterBasicRepresentation;
import com.xjt.newpic.edit.filters.FilterRepresentation;

/**
 * The basic editor that all the one parameter filters
 */
public class BasicEditor extends ParametricEditor implements ParameterInteger {

    private final String TAG = BasicEditor.class.getSimpleName();

    public static int ID = R.id.basicEditor;

    public BasicEditor() {
        super(ID, R.layout.np_edit_default_editor, R.id.basicEditor);
    }

    protected BasicEditor(int id) {
        super(id, R.layout.np_edit_default_editor, R.id.basicEditor);
    }

    protected BasicEditor(int id, int layoutID, int viewID) {
        super(id, layoutID, viewID);
    }

    @Override
    public void reflectCurrentFilter() {
        super.reflectCurrentFilter();
        if (getLocalRepresentation() != null && getLocalRepresentation() instanceof FilterBasicRepresentation) {
            updateText();
        }
    }

    private FilterBasicRepresentation getBasicRepresentation() {
        FilterRepresentation tmpRep = getLocalRepresentation();
        if (tmpRep != null && tmpRep instanceof FilterBasicRepresentation) {
            return (FilterBasicRepresentation) tmpRep;

        }
        return null;
    }

    @Override
    public int getMaximum() {
        FilterBasicRepresentation rep = getBasicRepresentation();
        if (rep == null) {
            return 0;
        }
        return rep.getMaximum();
    }

    @Override
    public int getMinimum() {
        FilterBasicRepresentation rep = getBasicRepresentation();
        if (rep == null) {
            return 0;
        }
        return rep.getMinimum();
    }

    @Override
    public int getDefaultValue() {
        return 0;
    }

    @Override
    public int getValue() {
        FilterBasicRepresentation rep = getBasicRepresentation();
        if (rep == null) {
            return 0;
        }
        return rep.getValue();
    }

    @Override
    public String getValueString() {
        return null;
    }

    @Override
    public void setValue(int value) {
        FilterBasicRepresentation rep = getBasicRepresentation();
        if (rep == null) {
            return;
        }
        rep.setValue(value);
        commitLocalRepresentation();
    }

    @Override
    public String getParameterName() {
        FilterBasicRepresentation rep = getBasicRepresentation();
        return mContext.getString(rep.getTextId());
    }

    @Override
    public String getParameterType() {
        return sParameterType;
    }

    @Override
    public void setController(Control c) {
    }

    @Override
    public void setFilterView(FilterView editor) {

    }

    @Override
    public void copyFrom(Parameter src) {

    }
}
