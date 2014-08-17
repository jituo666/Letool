
package com.xjt.newpic.filtershow.editors;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.xjt.newpic.R;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.filtershow.controller.Control;
import com.xjt.newpic.filtershow.filters.FilterRepresentation;
import com.xjt.newpic.filtershow.imageshow.ImageShow;
import com.xjt.newpic.filtershow.imageshow.MasterImage;
import com.xjt.newpic.filtershow.pipeline.ImagePreset;
import com.xjt.newpic.surpport.PopupMenu;
import com.xjt.newpic.surpport.PopupMenuItem;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Base class for Editors Must contain a mImageShow and a top level view
 */
public class Editor implements OnSeekBarChangeListener, SwapButton.SwapButtonListener {

    private static final String TAG = Editor.class.getSimpleName();

    protected Context mContext;
    protected View mView;
    protected ImageShow mImageShow;
    protected FrameLayout mFrameLayout;
    protected SeekBar mSeekBar;
    Button mEditTitle;
    protected Button mFilterTitle;
    protected int mID;
    protected boolean mChangesGeometry = false;
    protected FilterRepresentation mLocalRepresentation = null;
    protected byte mShowParameter = SHOW_VALUE_UNDEFINED;
    private Button mButton;
    public static byte SHOW_VALUE_UNDEFINED = -1;
    public static byte SHOW_VALUE_OFF = 0;
    public static byte SHOW_VALUE_INT = 1;

    public static void hackFixStrings(PopupMenu menu) {
        int count = menu.size();
        for (int i = 0; i < count; i++) {
            PopupMenuItem item = menu.getItem(i);
            LLog.i(TAG, "count" + count + " i:" + i + "------------hackFixStrings:" + (item == null));
            item.setTitle(item.getTitle().toString().toUpperCase());
        }
    }

    public String calculateUserMessage(Context context, String effectName, Object parameterValue) {
        return effectName.toUpperCase() + " " + parameterValue;
    }

    protected Editor(int id) {
        mID = id;
    }

    public int getID() {
        return mID;
    }

    public byte showParameterValue() {
        return mShowParameter;
    }

    public boolean showsSeekBar() {
        return true;
    }

    public void setUpEditorUI(View actionButton, View editControl,
            Button editTitle, Button stateButton) {
        mEditTitle = editTitle;
        mFilterTitle = stateButton;
        mButton = editTitle;
        MasterImage.getImage().resetGeometryImages(false);
        setUtilityPanelUI(actionButton, editControl);
    }

    public boolean showsPopupIndicator() {
        return false;
    }

    /**
     * @param actionButton the would be the area for menu etc
     * @param editControl this is the black area for sliders etc
     */
    public void setUtilityPanelUI(View actionButton, View editControl) {

        Context context = editControl.getContext();
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout lp = (LinearLayout) inflater.inflate(
                R.layout.filtershow_seekbar, (ViewGroup) editControl, true);
        mSeekBar = (SeekBar) lp.findViewById(R.id.primarySeekBar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setVisibility(View.GONE);
        if (context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT) {
            if (showsSeekBar()) {
                mSeekBar.setVisibility(View.VISIBLE);
            }
        }

        if (mButton != null) {
            setMenuIcon(showsPopupIndicator());
        }
    }

    @Override
    public void onProgressChanged(SeekBar sbar, int progress, boolean arg2) {

    }

    public void setPanel() {

    }

    public void createEditor(Context context, FrameLayout frameLayout) {
        mContext = context;
        mFrameLayout = frameLayout;
        mLocalRepresentation = null;
    }

    protected void unpack(int viewid, int layoutid) {

        if (mView == null) {
            mView = mFrameLayout.findViewById(viewid);
            if (mView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService
                        (Context.LAYOUT_INFLATER_SERVICE);
                mView = inflater.inflate(layoutid, mFrameLayout, false);
                mFrameLayout.addView(mView, mView.getLayoutParams());
            }
        }
        mImageShow = findImageShow(mView);
    }

    private ImageShow findImageShow(View view) {
        if (view instanceof ImageShow) {
            return (ImageShow) view;
        }
        if (!(view instanceof ViewGroup)) {
            return null;
        }
        ViewGroup vg = (ViewGroup) view;
        int n = vg.getChildCount();
        for (int i = 0; i < n; i++) {
            View v = vg.getChildAt(i);
            if (v instanceof ImageShow) {
                return (ImageShow) v;
            } else if (v instanceof ViewGroup) {
                return findImageShow(v);
            }
        }
        return null;
    }

    public View getTopLevelView() {
        return mView;
    }

    public ImageShow getImageShow() {
        return mImageShow;
    }

    public void setVisibility(int visible) {
        mView.setVisibility(visible);
    }

    public FilterRepresentation getLocalRepresentation() {
        if (mLocalRepresentation == null) {
            ImagePreset preset = MasterImage.getImage().getPreset();
            FilterRepresentation filterRepresentation = MasterImage.getImage().getCurrentFilterRepresentation();
            mLocalRepresentation = preset.getFilterRepresentationCopyFrom(filterRepresentation);
            if (mShowParameter == SHOW_VALUE_UNDEFINED && filterRepresentation != null) {
                boolean show = filterRepresentation.showParameterValue();
                mShowParameter = show ? SHOW_VALUE_INT : SHOW_VALUE_OFF;
            }

        }
        return mLocalRepresentation;
    }

    /**
     * Call this to update the preset in MasterImage with the current representation
     * returned by getLocalRepresentation.  This causes the preview bitmap to be
     * regenerated.
     */
    public void commitLocalRepresentation() {
        commitLocalRepresentation(getLocalRepresentation());
    }

    /**
     * Call this to update the preset in MasterImage with a given representation.
     * This causes the preview bitmap to be regenerated.
     */
    public void commitLocalRepresentation(FilterRepresentation rep) {
        ArrayList<FilterRepresentation> filter = new ArrayList<FilterRepresentation>(1);
        filter.add(rep);
        commitLocalRepresentation(filter);
    }

    /**
     * Call this to update the preset in MasterImage with a collection of FilterRepresentations.
     * This causes the preview bitmap to be regenerated.
     */
    public void commitLocalRepresentation(Collection<FilterRepresentation> reps) {
        ImagePreset preset = MasterImage.getImage().getPreset();
        preset.updateFilterRepresentations(reps);
        if (mButton != null) {
            updateText();
        }
        if (mChangesGeometry) {
            // Regenerate both the filtered and the geometry-only bitmaps
            MasterImage.getImage().resetGeometryImages(true);
        }
        // Regenerate the filtered bitmap.
        MasterImage.getImage().invalidateFiltersOnly();
    }

    /**
     * This is called in response to a click to apply and leave the editor.
     */
    public void finalApplyCalled() {
        commitLocalRepresentation();
    }

    protected void updateText() {
        String s = "";
        if (mLocalRepresentation != null) {
            s = mContext.getString(mLocalRepresentation.getTextId());
        }
        mButton.setText(calculateUserMessage(mContext, s, ""));
    }

    /**
     * called after the filter is set and the select is called
     */
    public void reflectCurrentFilter() {
        mLocalRepresentation = null;
        FilterRepresentation representation = getLocalRepresentation();
        if (representation != null && mFilterTitle != null && representation.getTextId() != 0) {
            String text = mContext.getString(representation.getTextId()).toUpperCase();
            mFilterTitle.setText(text);
            updateText();
        }
    }

    public boolean useUtilityPanel() {
        return true;
    }

    public void openUtilityPanel(LinearLayout mAccessoryViewList) {
        setMenuIcon(showsPopupIndicator());
        if (mImageShow != null) {
            mImageShow.openUtilityPanel(mAccessoryViewList);
        }
    }

    protected void setMenuIcon(boolean on) {
        mEditTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, on ? R.drawable.filtershow_menu_marker_rtl : 0, 0);
    }

    protected void createMenu(int[] strId, View button) {
        PopupMenu pmenu = new PopupMenu(mContext, button);
        for (int i = 0; i < strId.length; i++) {
            pmenu.add(i, strId[i]);
        }
        setMenuIcon(true);

    }

    public Control[] getControls() {
        return null;
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {

    }

    @Override
    public void swapLeft(PopupMenuItem item) {

    }

    @Override
    public void swapRight(PopupMenuItem item) {

    }

    public void detach() {
        if (mImageShow != null) {
            mImageShow.detach();
        }
    }
}
