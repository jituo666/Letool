
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
import com.xjt.newpic.edit.controller.ChoseBorderColor;
import com.xjt.newpic.edit.filters.FilterColorBorderRepresentation;
import com.xjt.newpic.edit.filters.FilterRepresentation;
import com.xjt.newpic.edit.imageshow.ImageShow;
import com.xjt.newpic.surpport.PopupMenu;
import com.xjt.newpic.surpport.PopupMenuItem;

public class EditorColorBorder extends ParametricEditor {

    private static final String TAG = EditorColorBorder.class.getSimpleName();

    private static final int POP_UP_MENU_ID_CORNER_SIZE = 0;
    private static final int POP_UP_MENU_ID_BODER_SIZE = 1;
    private static final int POP_UP_MENU_ID_BODER_COLOR = 2;
    private static final int POP_UP_MENU_ID_BODER_CLEAR = 3;

    public static final int ID = R.id.editorColorBorder;

    int[] mBasicColors = {
            FilterColorBorderRepresentation.DEFAULT_COLOR1,
            FilterColorBorderRepresentation.DEFAULT_COLOR2,
            FilterColorBorderRepresentation.DEFAULT_COLOR3,
            FilterColorBorderRepresentation.DEFAULT_COLOR4,
            FilterColorBorderRepresentation.DEFAULT_COLOR5,
    };

    private String mParameterString;

    public EditorColorBorder() {
        super(ID);
    }

    @Override
    public String calculateUserMessage(Context context, String effectName, Object parameterValue) {
        FilterColorBorderRepresentation rep = getColorBorderRep();
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
        if (rep != null && getLocalRepresentation() instanceof FilterColorBorderRepresentation) {
            FilterColorBorderRepresentation cbRep = (FilterColorBorderRepresentation) getLocalRepresentation();
            cbRep.setPramMode(FilterColorBorderRepresentation.PARAM_COLOR);
            mParameterString = mContext.getString(R.string.border_color);
            if (mEditControl != null) {
                control(cbRep.getCurrentParam(), mEditControl);
            }
        }
    }

    @Override
    public void openUtilityPanel(final LinearLayout accessoryViewList) {
        Button view = (Button) accessoryViewList.findViewById(R.id.applyEffect);
        view.setText(mContext.getString(R.string.border_color));
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
        popupMenu.add(POP_UP_MENU_ID_CORNER_SIZE, R.string.border_corner_size);
        popupMenu.add(POP_UP_MENU_ID_BODER_SIZE, R.string.border_size);
        popupMenu.add(POP_UP_MENU_ID_BODER_COLOR, R.string.border_color);
        popupMenu.add(POP_UP_MENU_ID_BODER_CLEAR, R.string.border_clear);
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
        FilterColorBorderRepresentation rep = getColorBorderRep();
        if (rep == null) {
            return;
        }
        switch (item.getItemId()) {
            case POP_UP_MENU_ID_CORNER_SIZE:
                rep.setPramMode(FilterColorBorderRepresentation.PARAM_RADIUS);
                break;
            case POP_UP_MENU_ID_BODER_SIZE:
                rep.setPramMode(FilterColorBorderRepresentation.PARAM_SIZE);
                break;
            case POP_UP_MENU_ID_BODER_COLOR:
                rep.setPramMode(FilterColorBorderRepresentation.PARAM_COLOR);
                break;
            case POP_UP_MENU_ID_BODER_CLEAR:
                clearFrame();
                break;
        }
        if (item.getItemId() != FilterColorBorderRepresentation.PARAM_CLEAR) {
            mParameterString = item.getTitle().toString();
        }
        if (mControl instanceof ChoseBorderColor) {
            ChoseBorderColor c = (ChoseBorderColor) mControl;
            mBasicColors = c.getColorSet();
        }
        if (mEditControl != null) {
            control(rep.getCurrentParam(), mEditControl);
        }
        if (mControl instanceof ChoseBorderColor) {
            ChoseBorderColor c = (ChoseBorderColor) mControl;
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

    FilterColorBorderRepresentation getColorBorderRep() {
        FilterRepresentation rep = getLocalRepresentation();
        if (rep instanceof FilterColorBorderRepresentation) {
            return (FilterColorBorderRepresentation) rep;
        }
        return null;
    }

}
