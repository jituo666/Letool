package com.xjt.newpic.filtershow.editors;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.xjt.newpic.R;
import com.xjt.newpic.filtershow.filters.FilterRepresentation;
import com.xjt.newpic.filtershow.filters.FilterRotateRepresentation;
import com.xjt.newpic.filtershow.imageshow.ImageRotate;
import com.xjt.newpic.filtershow.imageshow.MasterImage;

public class EditorRotate extends Editor implements EditorInfo {
    public static final String TAG = EditorRotate.class.getSimpleName();
    public static final int ID = R.id.editorRotate;
    ImageRotate mImageRotate;

    public EditorRotate() {
        super(ID);
        mChangesGeometry = true;
    }

    @Override
    public void createEditor(Context context, FrameLayout frameLayout) {
        super.createEditor(context, frameLayout);
        if (mImageRotate == null) {
            mImageRotate = new ImageRotate(context);
        }
        mView = mImageShow = mImageRotate;
        mImageRotate.setEditor(this);
    }

    @Override
    public void reflectCurrentFilter() {
        MasterImage master = MasterImage.getImage();
        master.setCurrentFilterRepresentation(master.getPreset()
                .getFilterWithSerializationName(FilterRotateRepresentation.SERIALIZATION_NAME));
        super.reflectCurrentFilter();
        FilterRepresentation rep = getLocalRepresentation();
        if (rep == null || rep instanceof FilterRotateRepresentation) {
            mImageRotate.setFilterRotateRepresentation((FilterRotateRepresentation) rep);
        } else {
            Log.w(TAG, "Could not reflect current filter, not of type: "
                    + FilterRotateRepresentation.class.getSimpleName());
        }
        mImageRotate.invalidate();
    }

    @Override
    public void openUtilityPanel(final LinearLayout accessoryViewList) {
        final Button button = (Button) accessoryViewList.findViewById(R.id.applyEffect);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mImageRotate.rotate();
                String displayVal = mContext.getString(getTextId()) + " "
                        + mImageRotate.getLocalValue();
                button.setText(displayVal);
            }
        });
    }

    @Override
    public void finalApplyCalled() {
        commitLocalRepresentation(mImageRotate.getFinalRepresentation());
    }

    @Override
    public int getTextId() {
        return R.string.rotate;
    }

    @Override
    public int getOverlayId() {
        return R.drawable.filtershow_button_geometry_rotate;
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
