package com.xjt.newpic.edit.editors;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.xjt.newpic.R;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.edit.NpEditActivity;
import com.xjt.newpic.edit.controller.ColorChooser;
import com.xjt.newpic.edit.filters.FilterImageBorderRepresentation;
import com.xjt.newpic.edit.filters.FilterRepresentation;
import com.xjt.newpic.edit.imageshow.ImageShow;
import com.xjt.newpic.surpport.PopupMenu;
import com.xjt.newpic.surpport.PopupMenuItem;

public class EditorImageBorder extends ParametricEditor {

    private static final String TAG = EditorImageBorder.class.getSimpleName();

    private static final int POP_UP_MENU_ID_CORNER_SIZE = 0;
    private static final int POP_UP_MENU_ID_BODER_SIZE = 1;
    private static final int POP_UP_MENU_ID_BODER_COLOR = 2;
    private static final int POP_UP_MENU_ID_BODER_CLEAR = 3;

    public static final int ID = R.id.editorImageBorder;

    int[] mBasicColors = {
            FilterImageBorderRepresentation.DEFAULT_MENU_COLOR1,
            FilterImageBorderRepresentation.DEFAULT_MENU_COLOR2,
            FilterImageBorderRepresentation.DEFAULT_MENU_COLOR3,
            FilterImageBorderRepresentation.DEFAULT_MENU_COLOR4,
            FilterImageBorderRepresentation.DEFAULT_MENU_COLOR5,
    };

    private String mParameterString;

    public EditorImageBorder() {
        super(ID);
    }

    @Override
    public String calculateUserMessage(Context context, String effectName, Object parameterValue) {
        FilterImageBorderRepresentation rep = getColorBorderRep();
        if (rep == null) {
            return "";
        }
        if (mParameterString == null) {
            mParameterString = "";
        }
        String val = rep.getValueString();
        return mParameterString + val;
    }

    @Override
    public void createEditor(Context context, FrameLayout frameLayout) {
        mView = mImageShow = new ImageShow(context);
        super.createEditor(context, frameLayout);
    }

    @Override
    public void reflectCurrentFilter() {
        super.reflectCurrentFilter();
        FilterRepresentation rep = getLocalRepresentation();
        if (rep != null && getLocalRepresentation() instanceof FilterImageBorderRepresentation) {
            FilterImageBorderRepresentation cbRep = (FilterImageBorderRepresentation) getLocalRepresentation();
            cbRep.setPramMode(FilterImageBorderRepresentation.PARAM_COLOR);
            mParameterString = mContext.getString(R.string.color_border_color);
            if (mEditControl != null) {
                control(cbRep.getCurrentParam(), mEditControl);
            }
        }
    }

    @Override
    public void openUtilityPanel(final LinearLayout accessoryViewList) {
        Button view = (Button) accessoryViewList.findViewById(R.id.applyEffect);
        view.setText(mContext.getString(R.string.color_border_color));
        view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showPopupMenu(accessoryViewList);
            }
        });
    }

    @Override
    public boolean showsSeekBar() {
        return false;
    }

    private void showPopupMenu(LinearLayout accessoryViewList) {
        final Button button = (Button) accessoryViewList.findViewById(R.id.applyEffect);
        if (button == null) {
            return;
        }
        final PopupMenu popupMenu = new PopupMenu(mImageShow.getActivity(), button);
        popupMenu.add(POP_UP_MENU_ID_CORNER_SIZE, R.string.color_border_corner_size);
        popupMenu.add(POP_UP_MENU_ID_BODER_SIZE, R.string.color_border_size);
        popupMenu.add(POP_UP_MENU_ID_BODER_COLOR, R.string.color_border_color);
        popupMenu.add(POP_UP_MENU_ID_BODER_CLEAR, R.string.color_border_clear);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(PopupMenuItem item) {
                selectMenuItem(item);
                return true;
            }
        });
        popupMenu.show();
        ((NpEditActivity) mContext).onShowMenu(popupMenu);
    }

    protected void selectMenuItem(PopupMenuItem item) {
        FilterImageBorderRepresentation rep = getColorBorderRep();
        if (rep == null) {
            return;
        }
        switch (item.getItemId()) {
            case POP_UP_MENU_ID_CORNER_SIZE:
                rep.setPramMode(FilterImageBorderRepresentation.PARAM_RADIUS);
                break;
            case POP_UP_MENU_ID_BODER_SIZE:
                rep.setPramMode(FilterImageBorderRepresentation.PARAM_SIZE);
                break;
            case POP_UP_MENU_ID_BODER_COLOR:
                rep.setPramMode(FilterImageBorderRepresentation.PARAM_COLOR);
                break;
            case POP_UP_MENU_ID_BODER_CLEAR:
                clearFrame();
                break;
        }
        if (item.getItemId() != FilterImageBorderRepresentation.PARAM_CLEAR) {
            mParameterString = item.getTitle().toString();
        }
        if (mControl instanceof ColorChooser) {
            ColorChooser c = (ColorChooser) mControl;
            mBasicColors = c.getColorSet();
        }
        if (mEditControl != null) {
            control(rep.getCurrentParam(), mEditControl);
        }
        if (mControl instanceof ColorChooser) {
            ColorChooser c = (ColorChooser) mControl;
            c.setColorSet(mBasicColors);
        }
        updateText();
        mControl.updateUI();
        mView.invalidate();
    }

    public void clearFrame() {
        commitLocalRepresentation();
    }

    @Override
    public void setUtilityPanelUI(View actionButton, View editControl) {
        super.setUtilityPanelUI(actionButton, editControl);
        return;
    }

    FilterImageBorderRepresentation getColorBorderRep() {
        FilterRepresentation rep = getLocalRepresentation();
        if (rep instanceof FilterImageBorderRepresentation) {
            return (FilterImageBorderRepresentation) rep;
        }
        return null;
    }

}
