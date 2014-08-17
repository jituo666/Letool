
package com.xjt.newpic.filtershow.editors;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.xjt.newpic.R;
import com.xjt.newpic.filtershow.FilterShowActivity;
import com.xjt.newpic.filtershow.controller.BasicParameterStyle;
import com.xjt.newpic.filtershow.controller.BitmapCaller;
import com.xjt.newpic.filtershow.controller.FilterView;
import com.xjt.newpic.filtershow.controller.Parameter;
import com.xjt.newpic.filtershow.filters.FilterChanSatRepresentation;
import com.xjt.newpic.filtershow.filters.FilterRepresentation;
import com.xjt.newpic.filtershow.imageshow.MasterImage;
import com.xjt.newpic.filtershow.pipeline.ImagePreset;
import com.xjt.newpic.surpport.PopupMenuItem;
import com.xjt.newpic.surpport.PopupMenu;

public class EditorChanSat extends ParametricEditor implements OnSeekBarChangeListener, FilterView {

    private final String TAG = EditorChanSat.class.getSimpleName();
    public static final int ID = R.id.editorChanSat;

    private static final int POP_UP_MENU_ID_MAIN = 0;
    private static final int POP_UP_MENU_ID_RED = 1;
    private static final int POP_UP_MENU_ID_YELLOW = 2;
    private static final int POP_UP_MENU_ID_GREEN = 3;
    private static final int POP_UP_MENU_ID_CYAN = 4;
    private static final int POP_UP_MENU_ID_BLUE = 5;
    private static final int POP_UP_MENU_ID_MAGENTA = 6;

    private SwapButton mButton;
    private final Handler mHandler = new Handler();

    private SeekBar mMainBar;
    private SeekBar mRedBar;
    private SeekBar mYellowBar;
    private SeekBar mGreenBar;
    private SeekBar mCyanBar;
    private SeekBar mBlueBar;
    private SeekBar mMagentaBar;
    private TextView mMainValue;
    private TextView mRedValue;
    private TextView mYellowValue;
    private TextView mGreenValue;
    private TextView mCyanValue;
    private TextView mBlueValue;
    private TextView mMagentaValue;

    int[] mMenuStrings = {
            R.string.editor_chan_sat_main,
            R.string.editor_chan_sat_red,
            R.string.editor_chan_sat_yellow,
            R.string.editor_chan_sat_green,
            R.string.editor_chan_sat_cyan,
            R.string.editor_chan_sat_blue,
            R.string.editor_chan_sat_magenta
    };

    String mCurrentlyEditing = null;

    public EditorChanSat() {
        super(ID, R.layout.filtershow_default_editor, R.id.basicEditor);
    }

    @Override
    public String calculateUserMessage(Context context, String effectName, Object parameterValue) {
        FilterRepresentation rep = getLocalRepresentation();
        if (rep == null || !(rep instanceof FilterChanSatRepresentation)) {
            return "";
        }
        FilterChanSatRepresentation csrep = (FilterChanSatRepresentation) rep;
        int mode = csrep.getParameterMode();
        String paramString;

        paramString = mContext.getString(mMenuStrings[mode]);

        int val = csrep.getCurrentParameter();
        return paramString + ((val > 0) ? " +" : " ") + val;
    }

    @Override
    public void openUtilityPanel(final LinearLayout accessoryViewList) {
        mButton = (SwapButton) accessoryViewList.findViewById(R.id.applyEffect);
        mButton.setText(mContext.getString(R.string.editor_chan_sat_main));

        if (useCompact(mContext)) {
            final PopupMenu popupMenu = new PopupMenu(mImageShow.getActivity(), mButton);
            popupMenu.add(POP_UP_MENU_ID_MAIN, R.string.editor_chan_sat_main);
            popupMenu.add(POP_UP_MENU_ID_RED, R.string.editor_chan_sat_red);
            popupMenu.add(POP_UP_MENU_ID_YELLOW, R.string.editor_chan_sat_yellow);
            popupMenu.add(POP_UP_MENU_ID_GREEN, R.string.editor_chan_sat_green);
            popupMenu.add(POP_UP_MENU_ID_CYAN, R.string.editor_chan_sat_cyan);
            popupMenu.add(POP_UP_MENU_ID_BLUE, R.string.editor_chan_sat_blue);
            popupMenu.add(POP_UP_MENU_ID_MAGENTA, R.string.editor_chan_sat_magenta);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(PopupMenuItem item) {
                    selectMenuItem(item);
                    return true;
                }
            });
            mButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    popupMenu.show();
                    ((FilterShowActivity) mContext).onShowMenu(popupMenu);
                }
            });
            mButton.setListener(this);

            FilterChanSatRepresentation csrep = getChanSatRep();
            String menuString = mContext.getString(mMenuStrings[0]);
            switchToMode(csrep, FilterChanSatRepresentation.MODE_MASTER, menuString);
        } else {
            mButton.setText(mContext.getString(R.string.saturation));
        }
    }

    @Override
    public void reflectCurrentFilter() {
        if (useCompact(mContext)) {
            super.reflectCurrentFilter();
            updateText();
            return;
        }
        mLocalRepresentation = null;
        if (getLocalRepresentation() != null
                && getLocalRepresentation() instanceof FilterChanSatRepresentation) {
            FilterChanSatRepresentation rep =
                    (FilterChanSatRepresentation) getLocalRepresentation();
            int value = rep.getValue(FilterChanSatRepresentation.MODE_MASTER);
            mMainBar.setProgress(value + 100);
            mMainValue.setText("" + value);
            value = rep.getValue(FilterChanSatRepresentation.MODE_RED);
            mRedBar.setProgress(value + 100);
            mRedValue.setText("" + value);
            value = rep.getValue(FilterChanSatRepresentation.MODE_YELLOW);
            mYellowBar.setProgress(value + 100);
            mYellowValue.setText("" + value);
            value = rep.getValue(FilterChanSatRepresentation.MODE_GREEN);
            mGreenBar.setProgress(value + 100);
            mGreenValue.setText("" + value);
            value = rep.getValue(FilterChanSatRepresentation.MODE_CYAN);
            mCyanBar.setProgress(value + 100);
            mCyanValue.setText("" + value);
            value = rep.getValue(FilterChanSatRepresentation.MODE_BLUE);
            mBlueBar.setProgress(value + 100);
            mBlueValue.setText("" + value);
            value = rep.getValue(FilterChanSatRepresentation.MODE_MAGENTA);
            mMagentaBar.setProgress(value + 100);
            mMagentaValue.setText("" + value);
            String text = mContext.getString(rep.getTextId()).toUpperCase();
            mFilterTitle.setText(text);
            updateText();
        }
    }

    @Override
    public void setUtilityPanelUI(View actionButton, View editControl) {
        if (useCompact(mContext)) {
            super.setUtilityPanelUI(actionButton, editControl);
            return;
        }
    }

    public int getParameterIndex(int id) {
        switch (id) {
            case POP_UP_MENU_ID_MAIN:
                return FilterChanSatRepresentation.MODE_MASTER;
            case POP_UP_MENU_ID_RED:
                return FilterChanSatRepresentation.MODE_RED;
            case POP_UP_MENU_ID_YELLOW:
                return FilterChanSatRepresentation.MODE_YELLOW;
            case POP_UP_MENU_ID_GREEN:
                return FilterChanSatRepresentation.MODE_GREEN;
            case POP_UP_MENU_ID_CYAN:
                return FilterChanSatRepresentation.MODE_CYAN;
            case POP_UP_MENU_ID_BLUE:
                return FilterChanSatRepresentation.MODE_BLUE;
            case POP_UP_MENU_ID_MAGENTA:
                return FilterChanSatRepresentation.MODE_MAGENTA;
        }
        return -1;
    }

    @Override
    public void detach() {
        if (mButton == null) {
            return;
        }
        mButton.setListener(null);
        mButton.setOnClickListener(null);
    }

    private void updateSeekBar(FilterChanSatRepresentation rep) {
        mControl.updateUI();
    }

    @Override
    protected Parameter getParameterToEdit(FilterRepresentation rep) {
        if (rep instanceof FilterChanSatRepresentation) {
            FilterChanSatRepresentation csrep = (FilterChanSatRepresentation) rep;
            Parameter param = csrep.getFilterParameter(csrep.getParameterMode());
            if (param instanceof BasicParameterStyle) {
                param.setFilterView(EditorChanSat.this);
            }
            return param;
        }
        return null;
    }

    private FilterChanSatRepresentation getChanSatRep() {
        FilterRepresentation rep = getLocalRepresentation();
        if (rep != null
                && rep instanceof FilterChanSatRepresentation) {
            FilterChanSatRepresentation csrep = (FilterChanSatRepresentation) rep;
            return csrep;
        }
        return null;
    }

    @Override
    public void computeIcon(int n, BitmapCaller caller) {
        FilterChanSatRepresentation rep = getChanSatRep();
        if (rep == null)
            return;
        rep = (FilterChanSatRepresentation) rep.copy();
        ImagePreset preset = new ImagePreset();
        preset.addFilter(rep);
        Bitmap src = MasterImage.getImage().getThumbnailBitmap();
        caller.available(src);
    }

    protected void selectMenuItem(PopupMenuItem item) {
        if (getLocalRepresentation() != null && getLocalRepresentation() instanceof FilterChanSatRepresentation) {
            FilterChanSatRepresentation csrep = (FilterChanSatRepresentation) getLocalRepresentation();
            switchToMode(csrep, getParameterIndex(item.getItemId()), item.getTitle().toString());

        }
    }

    protected void switchToMode(FilterChanSatRepresentation csrep, int mode, String title) {
        if (csrep == null) {
            return;
        }
        csrep.setParameterMode(mode);
        mCurrentlyEditing = title;
        mButton.setText(mCurrentlyEditing);
        {
            Parameter param = getParameterToEdit(csrep);
            control(param, mEditControl);
        }
        updateSeekBar(csrep);
        mView.invalidate();
    }

    @Override
    public void onProgressChanged(SeekBar sbar, int progress, boolean arg2) {
        FilterChanSatRepresentation rep = getChanSatRep();
        int value = progress - 100;
        rep.setCurrentParameter(value);
        commitLocalRepresentation();
    }

    @Override
    public void swapLeft(PopupMenuItem item) {
        super.swapLeft(item);
        mButton.setTranslationX(0);
        mButton.animate().translationX(mButton.getWidth()).setDuration(SwapButton.ANIM_DURATION);
        Runnable updateButton = new Runnable() {

            @Override
            public void run() {
                mButton.animate().cancel();
                mButton.setTranslationX(0);
            }
        };
        mHandler.postDelayed(updateButton, SwapButton.ANIM_DURATION);
        selectMenuItem(item);
    }

    @Override
    public void swapRight(PopupMenuItem item) {
        super.swapRight(item);
        mButton.setTranslationX(0);
        mButton.animate().translationX(-mButton.getWidth()).setDuration(SwapButton.ANIM_DURATION);
        Runnable updateButton = new Runnable() {

            @Override
            public void run() {
                mButton.animate().cancel();
                mButton.setTranslationX(0);
            }
        };
        mHandler.postDelayed(updateButton, SwapButton.ANIM_DURATION);
        selectMenuItem(item);
    }
}
