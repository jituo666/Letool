package com.xjt.newpic.edit.editors;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.xjt.newpic.R;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.edit.FilterShowActivity;
import com.xjt.newpic.edit.filters.FilterCropRepresentation;
import com.xjt.newpic.edit.filters.FilterRepresentation;
import com.xjt.newpic.edit.imageshow.ImageCrop;
import com.xjt.newpic.edit.imageshow.MasterImage;
import com.xjt.newpic.surpport.PopupMenu;
import com.xjt.newpic.surpport.PopupMenuItem;

public class EditorCrop extends Editor implements EditorInfo {

    public static final String TAG = EditorCrop.class.getSimpleName();
    public static final int ID = R.id.editorCrop;

    private static final int POP_UP_MENU_ID_1_1 = 0;
    private static final int POP_UP_MENU_ID_4_6 = 1;
    private static final int POP_UP_MENU_ID_4_3 = 2;
    private static final int POP_UP_MENU_ID_3_4 = 3;
    private static final int POP_UP_MENU_ID_5_7 = 4;
    private static final int POP_UP_MENU_ID_7_5 = 5;
    private static final int POP_UP_MENU_ID_9_16 = 6;
    private static final int POP_UP_MENU_ID_FREE = 7;
    private static final int POP_UP_MENU_ID_ORIGNAL = 8;

    // Holder for an aspect ratio it's string id
    protected static final class AspectInfo {

        int mAspectX;
        int mAspectY;
        int mStringId;

        AspectInfo(int stringID, int x, int y) {
            mStringId = stringID;
            mAspectX = x;
            mAspectY = y;
        }
    };

    // Mapping from menu id to aspect ratio
    protected static final SparseArray<AspectInfo> sAspects;
    static {
        sAspects = new SparseArray<AspectInfo>();
        sAspects.put(POP_UP_MENU_ID_1_1, new AspectInfo(R.string.aspect1to1_effect, 1, 1));
        sAspects.put(POP_UP_MENU_ID_4_6, new AspectInfo(R.string.aspect4to6_effect, 4, 6));
        sAspects.put(POP_UP_MENU_ID_4_3, new AspectInfo(R.string.aspect4to3_effect, 4, 3));
        sAspects.put(POP_UP_MENU_ID_3_4, new AspectInfo(R.string.aspect3to4_effect, 3, 4));
        sAspects.put(POP_UP_MENU_ID_5_7, new AspectInfo(R.string.aspect5to7_effect, 5, 7));
        sAspects.put(POP_UP_MENU_ID_7_5, new AspectInfo(R.string.aspect7to5_effect, 7, 5));
        sAspects.put(POP_UP_MENU_ID_9_16, new AspectInfo(R.string.aspect9to16_effect, 9, 16));
        sAspects.put(POP_UP_MENU_ID_FREE, new AspectInfo(R.string.aspectNone_effect, 0, 0));
        sAspects.put(POP_UP_MENU_ID_ORIGNAL, new AspectInfo(R.string.aspectOriginal_effect, 0, 0));
    }

    protected ImageCrop mImageCrop;
    private String mAspectString = "";

    public EditorCrop() {
        super(ID);
        mChangesGeometry = true;
    }

    @Override
    public void createEditor(Context context, FrameLayout frameLayout) {
        super.createEditor(context, frameLayout);
        if (mImageCrop == null) {
            mImageCrop = new ImageCrop(context);
        }
        mView = mImageShow = mImageCrop;
        mImageCrop.setEditor(this);
    }

    @Override
    public void reflectCurrentFilter() {
        MasterImage master = MasterImage.getImage();
        master.setCurrentFilterRepresentation(master.getPreset() .getFilterWithSerializationName(FilterCropRepresentation.SERIALIZATION_NAME));
        super.reflectCurrentFilter();
        FilterRepresentation rep = getLocalRepresentation();
        if (rep == null || rep instanceof FilterCropRepresentation) {
            mImageCrop.setFilterCropRepresentation((FilterCropRepresentation) rep);
        } else {
            Log.w(TAG, "Could not reflect current filter, not of type: " + FilterCropRepresentation.class.getSimpleName());
        }
        mImageCrop.invalidate();
    }

    @Override
    public void finalApplyCalled() {
        commitLocalRepresentation(mImageCrop.getFinalRepresentation());
    }

    @Override
    public void openUtilityPanel(final LinearLayout accessoryViewList) {

        Button view = (Button) accessoryViewList.findViewById(R.id.applyEffect);
        view.setText(mContext.getString(R.string.crop));
        view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showPopupMenu(accessoryViewList);
            }
        });
    }

    private void changeCropAspect(int itemId) {
        AspectInfo info = sAspects.get(itemId);
        if (info == null) {
            throw new IllegalArgumentException("Invalid resource ID: " + itemId);
        }
        if (itemId == POP_UP_MENU_ID_ORIGNAL) {
            mImageCrop.applyOriginalAspect();
        } else if (itemId == POP_UP_MENU_ID_FREE) {
            mImageCrop.applyFreeAspect();
        } else {
            mImageCrop.applyAspect(info.mAspectX, info.mAspectY);
        }
        setAspectString(mContext.getString(info.mStringId));
    }

    private void showPopupMenu(LinearLayout accessoryViewList) {
        final Button button = (Button) accessoryViewList.findViewById(R.id.applyEffect);
        final PopupMenu popupMenu = new PopupMenu(mImageShow.getActivity(), button);
        popupMenu.add(POP_UP_MENU_ID_1_1, R.string.aspect1to1_effect);
        popupMenu.add(POP_UP_MENU_ID_4_6, R.string.aspect4to6_effect);
        popupMenu.add(POP_UP_MENU_ID_4_3, R.string.aspect4to3_effect);
        popupMenu.add(POP_UP_MENU_ID_3_4, R.string.aspect3to4_effect);
        popupMenu.add(POP_UP_MENU_ID_5_7, R.string.aspect5to7_effect);
        popupMenu.add(POP_UP_MENU_ID_7_5, R.string.aspect7to5_effect);
        popupMenu.add(POP_UP_MENU_ID_9_16, R.string.aspect9to16_effect);
        popupMenu.add(POP_UP_MENU_ID_FREE, R.string.aspectNone_effect);
        popupMenu.add(POP_UP_MENU_ID_ORIGNAL, R.string.aspectOriginal_effect);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(PopupMenuItem item) {
                changeCropAspect(item.getItemId());
                return true;
            }
        });
        popupMenu.show();
        ((FilterShowActivity) mContext).onShowMenu(popupMenu);
    }

    @Override
    public void setUtilityPanelUI(View actionButton, View editControl) {
        super.setUtilityPanelUI(actionButton, editControl);
        setMenuIcon(true);
    }

    @Override
    public boolean showsSeekBar() {
        return false;
    }

    @Override
    public int getTextId() {
        return R.string.crop;
    }

    @Override
    public int getOverlayId() {
        return R.drawable.filtershow_button_geometry_crop;
    }

    @Override
    public boolean getOverlayOnly() {
        return true;
    }

    private void setAspectString(String s) {
        mAspectString = s;
    }
}
