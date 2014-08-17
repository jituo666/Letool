package com.xjt.newpic.filtershow.editors;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.xjt.newpic.R;
import com.xjt.newpic.filtershow.FilterShowActivity;
import com.xjt.newpic.filtershow.controller.BasicParameterInt;
import com.xjt.newpic.filtershow.controller.Parameter;
import com.xjt.newpic.filtershow.filters.FilterRepresentation;
import com.xjt.newpic.filtershow.filters.FilterVignetteRepresentation;
import com.xjt.newpic.filtershow.imageshow.ImageVignette;
import com.xjt.newpic.surpport.PopupMenu;
import com.xjt.newpic.surpport.PopupMenuItem;

public class EditorVignette extends ParametricEditor {

    private static final String TAG = EditorVignette.class.getSimpleName();

    private static final int POP_UP_MENU_ID_VIGNETTE = 0;
    private static final int POP_UP_MENU_ID_FALLOFF = 1;
    private static final int POP_UP_MENU_ID_CONTRAST = 2;
    private static final int POP_UP_MENU_ID_SATURATION = 3;
    private static final int POP_UP_MENU_ID_EXPOSURE = 4;

    public static final int ID = R.id.vignetteEditor;
    ImageVignette mImageVignette;

    private SeekBar mVignetteBar;
    private SeekBar mExposureBar;
    private SeekBar mSaturationBar;
    private SeekBar mContrastBar;
    private SeekBar mFalloffBar;

    private TextView mVignetteValue;
    private TextView mExposureValue;
    private TextView mSaturationValue;
    private TextView mContrastValue;
    private TextView mFalloffValue;

    private SwapButton mButton;
    private final Handler mHandler = new Handler();

    int[] mMenuStrings = {
            R.string.vignette_main,
            R.string.vignette_exposure,
            R.string.vignette_saturation,
            R.string.vignette_contrast,
            R.string.vignette_falloff,
    };

    String mCurrentlyEditing = null;

    public EditorVignette() {
        super(ID, R.layout.filtershow_vignette_editor, R.id.imageVignette);
    }

    @Override
    public void createEditor(Context context, FrameLayout frameLayout) {
        super.createEditor(context, frameLayout);
        mImageVignette = (ImageVignette) mImageShow;
        mImageVignette.setEditor(this);
    }

    @Override
    public void reflectCurrentFilter() {
        if (useCompact(mContext)) {
            super.reflectCurrentFilter();

            FilterRepresentation rep = getLocalRepresentation();
            if (rep != null && getLocalRepresentation() instanceof FilterVignetteRepresentation) {
                FilterVignetteRepresentation drawRep = (FilterVignetteRepresentation) rep;
                mImageVignette.setRepresentation(drawRep);
            }
            updateText();
            return;
        }
        mLocalRepresentation = null;
        if (getLocalRepresentation() != null
                && getLocalRepresentation() instanceof FilterVignetteRepresentation) {
            FilterVignetteRepresentation rep =
                    (FilterVignetteRepresentation) getLocalRepresentation();

            int[] mode = {
                    FilterVignetteRepresentation.MODE_VIGNETTE,
                    FilterVignetteRepresentation.MODE_EXPOSURE,
                    FilterVignetteRepresentation.MODE_SATURATION,
                    FilterVignetteRepresentation.MODE_CONTRAST,
                    FilterVignetteRepresentation.MODE_FALLOFF
            };
            SeekBar[] sliders = {
                    mVignetteBar,
                    mExposureBar,
                    mSaturationBar,
                    mContrastBar,
                    mFalloffBar
            };
            TextView[] label = {
                    mVignetteValue,
                    mExposureValue,
                    mSaturationValue,
                    mContrastValue,
                    mFalloffValue
            };
            for (int i = 0; i < mode.length; i++) {
                BasicParameterInt p = (BasicParameterInt) rep.getFilterParameter(mode[i]);
                int value = p.getValue();
                sliders[i].setMax(p.getMaximum() - p.getMinimum());
                sliders[i].setProgress(value - p.getMinimum());
                label[i].setText("" + value);
            }

            mImageVignette.setRepresentation(rep);
            String text = mContext.getString(rep.getTextId()).toUpperCase();
            mFilterTitle.setText(text);
            updateText();
        }
    }

    @Override
    public String calculateUserMessage(Context context, String effectName, Object parameterValue) {
        FilterRepresentation rep = getLocalRepresentation();
        if (rep == null || !(rep instanceof FilterVignetteRepresentation)) {
            return "";
        }
        FilterVignetteRepresentation csrep = (FilterVignetteRepresentation) rep;
        int mode = csrep.getParameterMode();
        String paramString;

        paramString = mContext.getString(mMenuStrings[mode]);

        int val = csrep.getCurrentParameter();
        return paramString + ((val > 0) ? " +" : " ") + val;
    }

    @Override
    public void openUtilityPanel(final LinearLayout accessoryViewList) {
        mButton = (SwapButton) accessoryViewList.findViewById(R.id.applyEffect);
        mButton.setText(mContext.getString(R.string.vignette_main));

        if (useCompact(mContext)) {
            final PopupMenu popupMenu = new PopupMenu(mImageShow.getActivity(), mButton);
            //popupMenu.getMenuInflater().inflate(R.menu.filtershow_menu_vignette, popupMenu.getMenu());
            popupMenu.add(POP_UP_MENU_ID_VIGNETTE, R.string.vignette_main);
            popupMenu.add(POP_UP_MENU_ID_FALLOFF, R.string.vignette_falloff);
            popupMenu.add(POP_UP_MENU_ID_CONTRAST, R.string.vignette_contrast);
            popupMenu.add(POP_UP_MENU_ID_SATURATION, R.string.vignette_saturation);
            popupMenu.add(POP_UP_MENU_ID_EXPOSURE, R.string.vignette_exposure);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(PopupMenuItem item) {
                    selectMenuItem(item);
                    return true;
                }

            });
            mButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    popupMenu.show();
                    ((FilterShowActivity) mContext).onShowMenu(popupMenu);
                }
            });
            mButton.setListener(this);

            FilterVignetteRepresentation csrep = getVignetteRep();
            String menuString = mContext.getString(mMenuStrings[0]);
            switchToMode(csrep, FilterVignetteRepresentation.MODE_VIGNETTE, menuString);
        } else {
            mButton.setText(mContext.getString(R.string.vignette_main));
        }
    }

    public int getParameterIndex(int id) {
        switch (id) {
            case POP_UP_MENU_ID_VIGNETTE:
                return FilterVignetteRepresentation.MODE_VIGNETTE;
            case POP_UP_MENU_ID_FALLOFF:
                return FilterVignetteRepresentation.MODE_FALLOFF;
            case POP_UP_MENU_ID_CONTRAST:
                return FilterVignetteRepresentation.MODE_CONTRAST;
            case POP_UP_MENU_ID_SATURATION:
                return FilterVignetteRepresentation.MODE_SATURATION;
            case POP_UP_MENU_ID_EXPOSURE:
                return FilterVignetteRepresentation.MODE_EXPOSURE;
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

    private void updateSeekBar(FilterVignetteRepresentation rep) {
        mControl.updateUI();
    }

    @Override
    protected Parameter getParameterToEdit(FilterRepresentation rep) {
        if (rep instanceof FilterVignetteRepresentation) {
            FilterVignetteRepresentation csrep = (FilterVignetteRepresentation) rep;
            Parameter param = csrep.getFilterParameter(csrep.getParameterMode());

            return param;
        }
        return null;
    }

    private FilterVignetteRepresentation getVignetteRep() {
        FilterRepresentation rep = getLocalRepresentation();
        if (rep != null
                && rep instanceof FilterVignetteRepresentation) {
            FilterVignetteRepresentation csrep = (FilterVignetteRepresentation) rep;
            return csrep;
        }
        return null;
    }

    protected void selectMenuItem(PopupMenuItem item) {
        if (getLocalRepresentation() != null
                && getLocalRepresentation() instanceof FilterVignetteRepresentation) {
            FilterVignetteRepresentation csrep =
                    (FilterVignetteRepresentation) getLocalRepresentation();

            switchToMode(csrep, getParameterIndex(item.getItemId()), item.getTitle().toString());
        }
    }

    protected void switchToMode(FilterVignetteRepresentation csrep, int mode, String title) {
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
