package com.xjt.newpic.edit.editors;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.filters.FilterRepresentation;
import com.xjt.newpic.edit.filters.FilterStraightenRepresentation;
import com.xjt.newpic.edit.imageshow.ImageStraighten;
import com.xjt.newpic.edit.imageshow.ImageManager;

public class EditorStraighten extends Editor implements EditorInfo {
    public static final String TAG = EditorStraighten.class.getSimpleName();
    public static final int ID = R.id.editorStraighten;
    ImageStraighten mImageStraighten;

    public EditorStraighten() {
        super(ID);
        mShowParameter = SHOW_VALUE_INT;
        mChangesGeometry = true;
    }

    @Override
    public String calculateUserMessage(Context context, String effectName, Object parameterValue) {
        String apply = context.getString(R.string.apply_effect);
        apply += " " + effectName;
        return apply.toUpperCase();
    }

    @Override
    public void createEditor(Context context, FrameLayout frameLayout) {
        super.createEditor(context, frameLayout);
        if (mImageStraighten == null) {
            mImageStraighten = new ImageStraighten(context);
        }
        mView = mImageShow = mImageStraighten;
        mImageStraighten.setEditor(this);
    }

    @Override
    public void reflectCurrentFilter() {
        ImageManager master = ImageManager.getImage();
        master.setCurrentFilterRepresentation(master.getPreset().getFilterWithSerializationName(
                FilterStraightenRepresentation.SERIALIZATION_NAME));
        super.reflectCurrentFilter();
        FilterRepresentation rep = getLocalRepresentation();
        if (rep == null || rep instanceof FilterStraightenRepresentation) {
            mImageStraighten
                    .setFilterStraightenRepresentation((FilterStraightenRepresentation) rep);
        } else {
            Log.w(TAG, "Could not reflect current filter, not of type: "
                    + FilterStraightenRepresentation.class.getSimpleName());
        }
        mImageStraighten.invalidate();
    }

    @Override
    public void finalApplyCalled() {
        commitLocalRepresentation(mImageStraighten.getFinalRepresentation());
    }

    @Override
    public int getTextId() {
        return R.string.straighten;
    }

    @Override
    public int getOverlayId() {
        return R.drawable.filtershow_button_geometry_straighten;
    }

    @Override
    public boolean getOverlayOnly() {
        return true;
    }

    @Override
    public boolean showsSeekBar() {
        return false;
    }

    @Override
    public boolean showsPopupIndicator() {
        return false;
    }
}
