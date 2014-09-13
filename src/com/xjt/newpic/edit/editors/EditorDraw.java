package com.xjt.newpic.edit.editors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.FilterShowActivity;
import com.xjt.newpic.edit.controller.BitmapCaller;
import com.xjt.newpic.edit.controller.ColorChooser;
import com.xjt.newpic.edit.controller.FilterView;
import com.xjt.newpic.edit.filters.FilterDrawRepresentation;
import com.xjt.newpic.edit.filters.FilterRepresentation;
import com.xjt.newpic.edit.imageshow.ImageDraw;
import com.xjt.newpic.surpport.PopupMenu;
import com.xjt.newpic.surpport.PopupMenuItem;

public class EditorDraw extends ParametricEditor implements FilterView {

    private static final String TAG = EditorDraw.class.getSimpleName();

    private static final int POP_UP_MENU_ID_STYLE = 0;
    private static final int POP_UP_MENU_ID_SIZE = 1;
    private static final int POP_UP_MENU_ID_COLOR = 2;
    private static final int POP_UP_MENU_ID_CLEAR = 3;

    public static final int ID = R.id.editorDraw;
    public ImageDraw mImageDraw;
    int[] brushIcons = {
            R.drawable.brush_flat,
            R.drawable.brush_round,
            R.drawable.brush_gauss,
            R.drawable.brush_marker,
            R.drawable.brush_spatter
    };

    int[] mBasColors = {
            FilterDrawRepresentation.DEFAULT_MENU_COLOR1,
            FilterDrawRepresentation.DEFAULT_MENU_COLOR2,
            FilterDrawRepresentation.DEFAULT_MENU_COLOR3,
            FilterDrawRepresentation.DEFAULT_MENU_COLOR4,
            FilterDrawRepresentation.DEFAULT_MENU_COLOR5,
    };
    private String mParameterString;
    private String mDrawString = null;

    public EditorDraw() {
        super(ID);
    }

    @Override
    public String calculateUserMessage(Context context, String effectName, Object parameterValue) {
        FilterDrawRepresentation rep = getDrawRep();
        if (mDrawString != null) {
            mImageDraw.displayDrawLook();
            return mDrawString;
        }
        if (rep == null) {
            return "";
        }

        if (mParameterString == null) {
            mParameterString = "";
        }
        String val = rep.getValueString();

        mImageDraw.displayDrawLook();
        return mParameterString + val;
    }

    @Override
    public void createEditor(Context context, FrameLayout frameLayout) {
        mView = mImageShow = mImageDraw = new ImageDraw(context);
        super.createEditor(context, frameLayout);
        mImageDraw.setEditor(this);

    }

    @Override
    public void reflectCurrentFilter() {
        super.reflectCurrentFilter();
        FilterRepresentation rep = getLocalRepresentation();
        if (rep != null && getLocalRepresentation() instanceof FilterDrawRepresentation) {
            FilterDrawRepresentation drawRep = (FilterDrawRepresentation) getLocalRepresentation();
            mImageDraw.setFilterDrawRepresentation(drawRep);
            drawRep.getParam(FilterDrawRepresentation.PARAM_STYLE).setFilterView(this);
            drawRep.setPramMode(FilterDrawRepresentation.PARAM_COLOR);
            mParameterString = mContext.getString(R.string.draw_color);
            control(drawRep.getCurrentParam(), mEditControl);
        }
    }

    @Override
    public void openUtilityPanel(final LinearLayout accessoryViewList) {
        Button view = (Button) accessoryViewList.findViewById(R.id.applyEffect);
        view.setText(mContext.getString(R.string.draw_color));
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
        final Button button = (Button) accessoryViewList.findViewById(
                R.id.applyEffect);
        if (button == null) {
            return;
        }
        final PopupMenu popupMenu = new PopupMenu(mImageShow.getActivity(), button);
        popupMenu.add(POP_UP_MENU_ID_STYLE, R.string.draw_style);
        popupMenu.add(POP_UP_MENU_ID_SIZE, R.string.draw_size);
        popupMenu.add(POP_UP_MENU_ID_COLOR, R.string.draw_color);
        popupMenu.add(POP_UP_MENU_ID_CLEAR, R.string.draw_clear);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(PopupMenuItem item) {
                selectMenuItem(item);
                return true;
            }
        });
        popupMenu.show();
        ((FilterShowActivity) mContext).onShowMenu(popupMenu);
    }

    protected void selectMenuItem(PopupMenuItem item) {
        FilterDrawRepresentation rep = getDrawRep();
        if (rep == null) {
            return;
        }

        switch (item.getItemId()) {
            case POP_UP_MENU_ID_STYLE:
                rep.setPramMode(FilterDrawRepresentation.PARAM_STYLE);
                break;
            case POP_UP_MENU_ID_SIZE:
                rep.setPramMode(FilterDrawRepresentation.PARAM_SIZE);
                break;
            case POP_UP_MENU_ID_COLOR:
                rep.setPramMode(FilterDrawRepresentation.PARAM_COLOR);
                break;
            case POP_UP_MENU_ID_CLEAR:
                clearDrawing();
                break;
        }
        if (item.getItemId() != 4) {
            mParameterString = item.getTitle().toString();
            updateText();
        }
        if (mControl instanceof ColorChooser) {
            ColorChooser c = (ColorChooser) mControl;
            mBasColors = c.getColorSet();
        }
        control(rep.getCurrentParam(), mEditControl);
        if (mControl instanceof ColorChooser) {
            ColorChooser c = (ColorChooser) mControl;
            c.setColorSet(mBasColors);
        }
        mControl.updateUI();
        mView.invalidate();
    }

    public void clearDrawing() {
        ImageDraw idraw = (ImageDraw) mImageShow;
        idraw.resetParameter();
        commitLocalRepresentation();
    }

    @Override
    public void setUtilityPanelUI(View actionButton, View editControl) {
        super.setUtilityPanelUI(actionButton, editControl);
    }

    FilterDrawRepresentation getDrawRep() {
        FilterRepresentation rep = getLocalRepresentation();
        if (rep instanceof FilterDrawRepresentation) {
            return (FilterDrawRepresentation) rep;
        }
        return null;
    }

    @Override
    public void computeIcon(int index, BitmapCaller caller) {
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), brushIcons[index]);
        caller.available(bitmap);
    }

    public int getBrushIcon(int type) {
        return brushIcons[type];
    }

}
