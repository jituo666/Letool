package com.xjt.newpic.edit.editors;

import android.content.Context;
import android.widget.FrameLayout;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.filters.FilterRepresentation;
import com.xjt.newpic.edit.filters.FilterTinyPlanetRepresentation;
import com.xjt.newpic.edit.imageshow.ImageTinyPlanet;

public class EditorTinyPlanet extends BasicEditor {
    public static final int ID = R.id.tinyPlanetEditor;
    private static final String LOGTAG = "EditorTinyPlanet";
    ImageTinyPlanet mImageTinyPlanet;

    public EditorTinyPlanet() {
        super(ID, R.layout.np_edit_tiny_planet_editor, R.id.imageTinyPlanet);
    }

    @Override
    public void createEditor(Context context, FrameLayout frameLayout) {
        super.createEditor(context, frameLayout);
        mImageTinyPlanet = (ImageTinyPlanet) mImageShow;
        mImageTinyPlanet.setEditor(this);
    }

    @Override
    public void reflectCurrentFilter() {
        super.reflectCurrentFilter();
        FilterRepresentation rep = getLocalRepresentation();
        if (rep != null && rep instanceof FilterTinyPlanetRepresentation) {
            FilterTinyPlanetRepresentation drawRep = (FilterTinyPlanetRepresentation) rep;
            mImageTinyPlanet.setRepresentation(drawRep);
        }
    }

    public void updateUI() {
        if (mControl != null) {
            mControl.updateUI();
        }
    }
}
