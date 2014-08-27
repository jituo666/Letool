
package com.xjt.newpic.filtershow.filters;

import com.xjt.newpic.filtershow.editors.ImageOnlyEditor;
import com.xjt.newpic.filtershow.pipeline.ImagePreset;

public class FilterUserPresetRepresentation extends FilterRepresentation {

    private ImagePreset mPreset;
    private int mId;

    public FilterUserPresetRepresentation(String name, int sr, ImagePreset preset, int id) {
        super(name, sr);
        setEditorId(ImageOnlyEditor.ID);
        setFilterType(FilterRepresentation.TYPE_FX);
        setSupportsPartialRendering(true);
        mPreset = preset;
        mId = id;
    }

    public ImagePreset getImagePreset() {
        return mPreset;
    }

    public int getId() {
        return mId;
    }

    public FilterRepresentation copy() {
        FilterRepresentation representation = new FilterUserPresetRepresentation(getName(), 0, new ImagePreset(mPreset), mId);
        return representation;
    }

    @Override
    public boolean allowsSingleInstanceOnly() {
        return true;
    }
}
