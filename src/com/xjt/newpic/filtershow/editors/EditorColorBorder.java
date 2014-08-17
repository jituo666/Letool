
package com.xjt.newpic.filtershow.editors;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.xjt.newpic.R;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.filtershow.FilterShowActivity;
import com.xjt.newpic.filtershow.controller.ColorChooser;
import com.xjt.newpic.filtershow.filters.FilterColorBorderRepresentation;
import com.xjt.newpic.filtershow.filters.FilterRepresentation;
import com.xjt.newpic.filtershow.imageshow.ImageShow;
import com.xjt.newpic.surpport.PopupMenu;
import com.xjt.newpic.surpport.PopupMenuItem;

public class EditorColorBorder extends ParametricEditor {

    private static final String TAG = EditorColorBorder.class.getSimpleName();

    private static final int POP_UP_MENU_ID_CORNER_SIZE = 0;
    private static final int POP_UP_MENU_ID_BODER_SIZE = 1;
    private static final int POP_UP_MENU_ID_BODER_COLOR = 2;
    private static final int POP_UP_MENU_ID_BODER_CLEAR = 3;

    public static final int ID = R.id.editorColorBorder;

    int[] mBasColors = {
            FilterColorBorderRepresentation.DEFAULT_MENU_COLOR1,
            FilterColorBorderRepresentation.DEFAULT_MENU_COLOR2,
            FilterColorBorderRepresentation.DEFAULT_MENU_COLOR3,
            FilterColorBorderRepresentation.DEFAULT_MENU_COLOR4,
            FilterColorBorderRepresentation.DEFAULT_MENU_COLOR5,
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
            cbRep.setPramMode(FilterColorBorderRepresentation.PARAM_SIZE);
            mParameterString = mContext.getString(R.string.color_border_size);
            if (mEditControl != null) {
                control(cbRep.getCurrentParam(), mEditControl);
            }
        }
    }

    @Override
    public void openUtilityPanel(final LinearLayout accessoryViewList) {
        Button view = (Button) accessoryViewList.findViewById(R.id.applyEffect);
        view.setText(mContext.getString(R.string.color_border_size));
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
        LLog.i(TAG, " -------------------------------====showPopupMenu excuted !:");
        ((FilterShowActivity) mContext).onShowMenu(popupMenu);
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
        if (item.getItemId() != 3) {
            mParameterString = item.getTitle().toString();
        }
        if (mControl instanceof ColorChooser) {
            ColorChooser c = (ColorChooser) mControl;
            mBasColors = c.getColorSet();
        }
        if (mEditControl != null) {
            control(rep.getCurrentParam(), mEditControl);
        }
        if (mControl instanceof ColorChooser) {
            ColorChooser c = (ColorChooser) mControl;
            c.setColorSet(mBasColors);
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
        if (ParametricEditor.useCompact(mContext)) {
            super.setUtilityPanelUI(actionButton, editControl);
            return;
        }
        mSeekBar = (SeekBar) editControl.findViewById(R.id.primarySeekBar);
        if (mSeekBar != null) {
            mSeekBar.setVisibility(View.GONE);
        }


    }

    FilterColorBorderRepresentation getColorBorderRep() {
        FilterRepresentation rep = getLocalRepresentation();
        if (rep instanceof FilterColorBorderRepresentation) {
            return (FilterColorBorderRepresentation) rep;
        }
        return null;
    }

}
