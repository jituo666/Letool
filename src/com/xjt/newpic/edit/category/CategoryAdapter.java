
package com.xjt.newpic.edit.category;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;

import com.xjt.newpic.R;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.edit.FilterShowActivity;
import com.xjt.newpic.edit.filters.FilterRepresentation;
import com.xjt.newpic.edit.filters.FilterTinyPlanetRepresentation;
import com.xjt.newpic.edit.pipeline.ImagePreset;

public class CategoryAdapter extends ArrayAdapter<CategoryAction> {

    private static final String TAG = CategoryAdapter.class.getSimpleName();

    private View mContainer;
    private int mSelectedPosition;
    private int mCategory;
    private int mOrientation;
    private boolean mShowAddButton = false;
    private String mAddButtonText;
    private int mItemHeight =0;
    private int mItemWidth =0;

    public CategoryAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mItemHeight = context.getResources().getDimensionPixelSize(R.dimen.category_panel_height);
        mItemWidth = context.getResources().getDimensionPixelSize(R.dimen.category_panel_icon_size);
    }

    public CategoryAdapter(Context context) {
        this(context, 0);
    }

    @Override
    public void clear() {
        for (int i = 0; i < getCount(); i++) {
            CategoryAction action = getItem(i);
            action.clearBitmap();
        }
        super.clear();
    }

    @Override
    public void add(CategoryAction action) {
        super.add(action);
        action.setAdapter(this);
    }

    public void initializeSelection(int category) {
        mCategory = category;
        mSelectedPosition = -1;
        if (category == CategoryMainPanel.LOOKS) {
            mSelectedPosition = 0;
            mAddButtonText = getContext().getString(R.string.filtershow_add_button_looks);
        }
        if (category == CategoryMainPanel.BORDERS) {
            mSelectedPosition = 0;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new CategoryView(getContext());
        }
        CategoryView view = (CategoryView) convertView;
        view.setOrientation(mOrientation);
        CategoryAction action = getItem(position);
        view.setAction(action, this);
        view.setLayoutParams(new LayoutParams(mItemWidth, mItemHeight));
        view.setTag(position);
        view.invalidate();
        return view;
    }

    public void setSelected(View v) {
        int old = mSelectedPosition;
        mSelectedPosition = (Integer) v.getTag();
        if (old != -1) {
            invalidateView(old);
        }
        invalidateView(mSelectedPosition);
    }

    public boolean isSelected(View v) {
        return (Integer) v.getTag() == mSelectedPosition;
    }

    private void invalidateView(int position) {
        CategoryListView ct = (CategoryListView) mContainer;
        View child = ct.getChildAt(position);
        if (child != null) {
            child.invalidate();
        }
    }

    public void setContainer(View container) {
        mContainer = container;
    }

    public void imageLoaded() {
        notifyDataSetChanged();
    }

    public FilterRepresentation getTinyPlanet() {
        for (int i = 0; i < getCount(); i++) {
            CategoryAction action = getItem(i);
            if (action.getRepresentation() != null && action.getRepresentation() instanceof FilterTinyPlanetRepresentation) {
                return action.getRepresentation();
            }
        }
        return null;
    }

    public void removeTinyPlanet() {
        for (int i = 0; i < getCount(); i++) {
            CategoryAction action = getItem(i);
            if (action.getRepresentation() != null && action.getRepresentation()
                    instanceof FilterTinyPlanetRepresentation) {
                super.remove(action);
                return;
            }
        }
    }

    @Override
    public void remove(CategoryAction action) {
        if (!(mCategory == CategoryMainPanel.VERSIONS || mCategory == CategoryMainPanel.LOOKS)) {
            return;
        }
        super.remove(action);
        FilterShowActivity activity = (FilterShowActivity) getContext();
        if (mCategory == CategoryMainPanel.LOOKS) {
            activity.removeLook(action);
        }
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void reflectImagePreset(ImagePreset preset) {
        if (preset == null) {
            return;
        }
        int selected = 0; // if nothing found, select "none" (first element)
        FilterRepresentation rep = null;
        if (mCategory == CategoryMainPanel.LOOKS) {
            int pos = preset.getPositionForType(FilterRepresentation.TYPE_FX);
            if (pos != -1) {
                rep = preset.getFilterRepresentation(pos);
            }
        } else if (mCategory == CategoryMainPanel.BORDERS) {
            int pos = preset.getPositionForType(FilterRepresentation.TYPE_BORDER);
            if (pos != -1) {
                rep = preset.getFilterRepresentation(pos);
            }
        }
        if (rep != null) {
            for (int i = 0; i < getCount(); i++) {
                FilterRepresentation itemRep = getItem(i).getRepresentation();
                if (itemRep == null) {
                    continue;
                }
                if (rep.getName().equalsIgnoreCase(itemRep.getName())) {
                    selected = i;
                    break;
                }
            }
        }
        if (mSelectedPosition != selected) {
            mSelectedPosition = selected;
            this.notifyDataSetChanged();
        }
    }

    public boolean showAddButton() {
        return mShowAddButton;
    }

    public void setShowAddButton(boolean showAddButton) {
        mShowAddButton = showAddButton;
    }

    public String getAddButtonText() {
        return mAddButtonText;
    }
}
