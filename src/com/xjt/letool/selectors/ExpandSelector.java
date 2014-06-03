
package com.xjt.letool.selectors;

import com.xjt.letool.metadata.MediaPath;
import com.xjt.letool.metadata.MediaSet;
import com.xjt.letool.views.layout.ThumbnailExpandLayout.SortTag;
import com.xjt.letool.views.layout.ThumbnailExpandLayout.ThumbnailPos;

import java.util.ArrayList;

public class ExpandSelector {

    @SuppressWarnings("unused")
    private static final String TAG = "PhotoSelector";
    public static final int ENTER_SELECTION_MODE = 1;
    public static final int LEAVE_SELECTION_MODE = 2;
    public static final int SELECT_ALL_MODE = 3;

    private ArrayList<SortTag> mTags;
    private ArrayList<ThumbnailPos> mSlotPos;
    private MediaSet mSourceMediaSet;
    private ExpandSelectListener mListener;
    private boolean mInSelectionMode;
    private boolean mAutoLeave = true;

    public void setTagsAndSlotPos(ArrayList<SortTag> tags, ArrayList<ThumbnailPos> slotPos) {
        mTags = tags;
        mSlotPos = slotPos;
    }

    public void setAutoLeaveSelectionMode(boolean enable) {
        mAutoLeave = enable;
    }

    public void setSelectionListener(ExpandSelectListener listener) {
        mListener = listener;
    }

    public boolean inSelectionMode() {
        return mInSelectionMode;
    }

    public void enterSelectionMode() {
        if (mInSelectionMode)
            return;
        mInSelectionMode = true;
        if (mListener != null)
            mListener.onSelectionModeChange(ENTER_SELECTION_MODE);
    }

    public void leaveSelectionMode() {
        if (!mInSelectionMode)
            return;
        mInSelectionMode = false;
        resetAllSCheckSedtate();
        if (mListener != null)
            mListener.onSelectionModeChange(LEAVE_SELECTION_MODE);
    }

    public boolean isItemSelected(MediaPath itemId, int slotIndex) {
        return mSlotPos.get(slotIndex).isChecked;
    }

    public int getSelectedCount() {
        if (mSlotPos == null)
            return 0;
        int total = 0;
        for (ThumbnailPos pos : mSlotPos) {
            if (pos.isChecked)
                total++;
        }
        return total;
    }

    public void toggle(int slotIndex) {
        boolean checked = !mSlotPos.get(slotIndex).isChecked;
        mSlotPos.get(slotIndex).isChecked = checked;
        modifyTagCheckedState(slotIndex);
        if (mListener != null)
            mListener.onSelectionChange();
        if (checked) {
            enterSelectionMode();
        } else {
            int count = getSelectedCount();
            if (count == 0 && mAutoLeave) {
                leaveSelectionMode();
            }
        }
    }

    public void toggleTag(int tagIndex, boolean checked) {
        SortTag tag = mTags.get(tagIndex);
        for (int i = tag.index; i < (tag.index + tag.count); i++) {
            mSlotPos.get(i).isChecked = checked;
        }
        if (mListener != null)
            mListener.onSelectionChange();
        if (checked) {
            enterSelectionMode();
        } else {
            int count = getSelectedCount();
            if (count == 0 && mAutoLeave) {
                leaveSelectionMode();
            }
        }
    }

    public void resetAllSCheckSedtate() {
        for (ThumbnailPos pos : mSlotPos) {
            pos.isChecked = false;
        }
        for (SortTag tag : mTags) {
            tag.checked = false;
        }
    }

    public void modifyTagCheckedState(int slotIndex) {
        SortTag tag = findTag(slotIndex);
        for (int i = (tag.index + tag.count - 1); i >= tag.index; i--) {
            if (!mSlotPos.get(i).isChecked) {
                tag.checked = false;
                return;
            }
        }
        tag.checked = true;
    }

    private SortTag findTag(int slotIndex) {
        SortTag tag = null;
        for (int index = 0; index < mTags.size(); index++) {
            if (index == mTags.size() - 1) {
                tag = mTags.get(index);
            } else if (slotIndex < mTags.get(index).index) {
                tag = mTags.get(index - 1);
            }
            if (tag != null) {
                break;
            }
        }
        return tag;
    }

    public ArrayList<MediaPath> getSelectedPaths() {
        return mSourceMediaSet.getMediaItem(mSlotPos, getSelectedCount());
    }

    public void setSourceMediaSet(MediaSet set) {
        mSourceMediaSet = set;
    }
}
