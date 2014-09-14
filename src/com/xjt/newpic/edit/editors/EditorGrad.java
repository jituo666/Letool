
package com.xjt.newpic.edit.editors;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.FilterShowActivity;
import com.xjt.newpic.edit.controller.Control;
import com.xjt.newpic.edit.controller.FilterView;
import com.xjt.newpic.edit.controller.Parameter;
import com.xjt.newpic.edit.controller.ParameterActionAndInt;
import com.xjt.newpic.edit.filters.FilterGradRepresentation;
import com.xjt.newpic.edit.filters.FilterRepresentation;
import com.xjt.newpic.edit.imageshow.ImageGrad;
import com.xjt.newpic.edit.imageshow.MasterImage;
import com.xjt.newpic.surpport.PopupMenu;
import com.xjt.newpic.surpport.PopupMenuItem;

public class EditorGrad extends ParametricEditor implements OnSeekBarChangeListener, ParameterActionAndInt {

    private static final String TAG = EditorGrad.class.getSimpleName();

    public static final int ID = R.id.editorGrad;

    private PopupMenu mPopupMenu;
    private String mEffectName = "";
    private static final int MODE_BRIGHTNESS = FilterGradRepresentation.PARAM_BRIGHTNESS;
    private static final int MODE_SATURATION = FilterGradRepresentation.PARAM_SATURATION;
    private static final int MODE_CONTRAST = FilterGradRepresentation.PARAM_CONTRAST;
    private static final int ADD_ICON = R.drawable.ic_grad_add;
    private static final int DEL_ICON = R.drawable.ic_grad_del;

    private static final int POP_UP_MENU_ID_BRIGHTNESS = 0;
    private static final int POP_UP_MENU_ID_SATURATION = 1;
    private static final int POP_UP_MENU_ID_CONTRAST = 2;

    private int mSliderMode = MODE_BRIGHTNESS;
    private ImageGrad mImageGrad;
    private ParamAdapter[] mAdapters = new ParamAdapter[3];

    public EditorGrad() {
        super(ID, R.layout.np_edit_grad_editor, R.id.gradEditor);
    }

    @Override
    public void createEditor(Context context, FrameLayout frameLayout) {
        super.createEditor(context, frameLayout);
        mImageGrad = (ImageGrad) mImageShow;
        mImageGrad.setEditor(this);

    }

    @Override
    public void reflectCurrentFilter() {
        super.reflectCurrentFilter();
        FilterRepresentation tmpRep = getLocalRepresentation();
        if (tmpRep instanceof FilterGradRepresentation) {
            FilterGradRepresentation rep = (FilterGradRepresentation) tmpRep;
            mImageGrad.setRepresentation(rep);
        }
    }

    public void updateSeekBar(FilterGradRepresentation rep) {
        mControl.updateUI();
    }

    @Override
    public void onProgressChanged(SeekBar sbar, int progress, boolean arg2) {
        FilterRepresentation tmpRep = getLocalRepresentation();
        if (tmpRep instanceof FilterGradRepresentation) {
            FilterGradRepresentation rep = (FilterGradRepresentation) tmpRep;
            int min = rep.getParameterMin(mSliderMode);
            int value = progress + min;
            rep.setParameter(mSliderMode, value);
            mView.invalidate();
            commitLocalRepresentation();
        }
    }

    @Override
    public void openUtilityPanel(final LinearLayout accessoryViewList) {
        Button view = (Button) accessoryViewList.findViewById(R.id.applyEffect);
        view.setText(mContext.getString(R.string.editor_grad_brightness));
        view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showPopupMenu(accessoryViewList);
            }
        });

        setUpPopupMenu(view);
        setEffectName();

    }

    public void setEffectName() {
        if (mPopupMenu != null) {
            PopupMenuItem item = mPopupMenu.getItem(0);
            mEffectName = item.getTitle().toString();
        }
    }

    @Override
    public void setUtilityPanelUI(View actionButton, View editControl) {
        super.setUtilityPanelUI(actionButton, editControl);
    }

    public void updateParameters() {
        FilterGradRepresentation rep = getGradRepresentation();
        for (int i = 0; i < mAdapters.length; i++) {
            mAdapters[i].updateValues(rep);
        }
    }

    private class ParamAdapter implements OnSeekBarChangeListener {

        SeekBar mSlider;
        TextView mTextView;
        int mMin = -100;
        int mMax = 100;
        int mMode;

        public ParamAdapter(int seekId, int textId, LinearLayout layout, int mode) {
            mSlider = (SeekBar) layout.findViewById(seekId);
            mTextView = (TextView) layout.findViewById(textId);
            mSlider.setMax(mMax - mMin);
            mMode = mode;
            FilterGradRepresentation rep = getGradRepresentation();
            if (rep != null) {
                updateValues(rep);
            }
            mSlider.setOnSeekBarChangeListener(this);
        }

        public void updateValues(FilterGradRepresentation rep) {
            int value = rep.getParameter(mMode);
            mTextView.setText(Integer.toString(value));
            mSlider.setProgress(value - mMin);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            FilterGradRepresentation rep = getGradRepresentation();
            int value = progress + mMin;
            rep.setParameter(mMode, value);
            if (mSliderMode != mMode) {
                mSliderMode = mMode;
                mEffectName = mContext.getResources().getString(getModeNameid(mMode));
                mEffectName = mEffectName.toUpperCase();
            }
            mTextView.setText(Integer.toString(value));
            mView.invalidate();
            commitLocalRepresentation();
        }

        private int getModeNameid(int mode) {
            switch (mode) {
                case MODE_CONTRAST:
                    return R.string.editor_grad_contrast;
                case MODE_BRIGHTNESS:
                    return R.string.editor_grad_brightness;
                case MODE_SATURATION:
                    return R.string.editor_grad_saturation;
            }
            return 0;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    private void showPopupMenu(LinearLayout accessoryViewList) {
        Button button = (Button) accessoryViewList.findViewById(R.id.applyEffect);
        if (button == null) {
            return;
        }

        if (mPopupMenu == null) {
            setUpPopupMenu(button);
        }
        mPopupMenu.show();
        ((FilterShowActivity) mContext).onShowMenu(mPopupMenu);
    }

    private void setUpPopupMenu(Button button) {
        mPopupMenu = new PopupMenu(mImageShow.getActivity(), button);
        mPopupMenu.add(POP_UP_MENU_ID_BRIGHTNESS, R.string.editor_grad_brightness);
        mPopupMenu.add(POP_UP_MENU_ID_SATURATION, R.string.editor_grad_saturation);
        mPopupMenu.add(POP_UP_MENU_ID_CONTRAST, R.string.editor_grad_contrast);
        FilterGradRepresentation rep = (FilterGradRepresentation) getLocalRepresentation();
        if (rep == null) {
            return;
        }
        hackFixStrings(mPopupMenu);
        setEffectName();
        updateText();

        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(PopupMenuItem item) {
                FilterRepresentation tmpRep = getLocalRepresentation();

                if (tmpRep instanceof FilterGradRepresentation) {
                    FilterGradRepresentation rep = (FilterGradRepresentation) tmpRep;
                    int cmdID = item.getItemId();
                    switch (cmdID) {
                        case POP_UP_MENU_ID_BRIGHTNESS:
                            mSliderMode = MODE_BRIGHTNESS;
                            mEffectName = item.getTitle().toString();
                            break;
                        case POP_UP_MENU_ID_SATURATION:
                            mSliderMode = MODE_SATURATION;
                            mEffectName = item.getTitle().toString();
                            break;
                        case POP_UP_MENU_ID_CONTRAST:
                            mSliderMode = MODE_CONTRAST;
                            mEffectName = item.getTitle().toString();
                            break;
                    }
                    updateSeekBar(rep);

                    commitLocalRepresentation();
                    mView.invalidate();
                }
                return true;
            }

        });
    }

    @Override
    public String calculateUserMessage(Context context, String effectName, Object parameterValue) {
        FilterGradRepresentation rep = getGradRepresentation();
        if (rep == null) {
            return mEffectName;
        }
        int val = rep.getParameter(mSliderMode);
        return mEffectName.toUpperCase() + ((val > 0) ? " +" : " ") + val;
    }

    private FilterGradRepresentation getGradRepresentation() {
        FilterRepresentation tmpRep = getLocalRepresentation();
        if (tmpRep instanceof FilterGradRepresentation) {
            return (FilterGradRepresentation) tmpRep;
        }
        return null;
    }

    @Override
    public int getMaximum() {
        FilterGradRepresentation rep = getGradRepresentation();
        if (rep == null) {
            return 0;
        }
        return rep.getParameterMax(mSliderMode);
    }

    @Override
    public int getMinimum() {
        FilterGradRepresentation rep = getGradRepresentation();
        if (rep == null) {
            return 0;
        }
        return rep.getParameterMin(mSliderMode);
    }

    @Override
    public int getDefaultValue() {
        return 0;
    }

    @Override
    public int getValue() {
        FilterGradRepresentation rep = getGradRepresentation();
        if (rep == null) {
            return 0;
        }
        return rep.getParameter(mSliderMode);
    }

    @Override
    public String getValueString() {
        return null;
    }

    @Override
    public void setValue(int value) {
        FilterGradRepresentation rep = getGradRepresentation();
        if (rep == null) {
            return;
        }
        rep.setParameter(mSliderMode, value);
    }

    @Override
    public String getParameterName() {
        return mEffectName;
    }

    @Override
    public String getParameterType() {
        return sParameterType;
    }

    @Override
    public void setController(Control c) {

    }

    @Override
    public void fireLeftAction() {
        FilterGradRepresentation rep = getGradRepresentation();
        if (rep == null) {
            return;
        }
        rep.addBand(MasterImage.getImage().getOriginalBounds());
        updateSeekBar(rep);

        commitLocalRepresentation();
        mView.invalidate();
    }

    @Override
    public int getLeftIcon() {
        return ADD_ICON;
    }

    @Override
    public void fireRightAction() {
        FilterGradRepresentation rep = getGradRepresentation();
        if (rep == null) {
            return;
        }
        rep.deleteCurrentBand();

        updateSeekBar(rep);
        commitLocalRepresentation();
        mView.invalidate();
    }

    @Override
    public int getRightIcon() {
        return DEL_ICON;
    }

    @Override
    public void setFilterView(FilterView editor) {

    }

    @Override
    public void copyFrom(Parameter src) {

    }
}
