package com.xjt.newpic.filtershow.category;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.xjt.newpic.R;
import com.xjt.newpic.filtershow.FilterShowActivity;

public class CategoryPanel extends Fragment implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "CategoryPanel";
    private static final String PARAMETER_TAG = "currentPanel";

    private int mCurrentAdapter = CategoryMainPanel.LOOKS;
    private CategoryAdapter mAdapter;
    private CategoryIconView mAddButton;

    public void setAdapter(int value) {
        mCurrentAdapter = value;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        loadAdapter(mCurrentAdapter);
    }

    private void loadAdapter(int adapter) {
        FilterShowActivity activity = (FilterShowActivity) getActivity();
        switch (adapter) {
            case CategoryMainPanel.LOOKS: {
                mAdapter = activity.getCategoryLooksAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(CategoryMainPanel.LOOKS);
                }
                activity.updateCategories();
                break;
            }
            case CategoryMainPanel.BORDERS: {
                mAdapter = activity.getCategoryBordersAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(CategoryMainPanel.BORDERS);
                }
                activity.updateCategories();
                break;
            }
            case CategoryMainPanel.GEOMETRY: {
                mAdapter = activity.getCategoryGeometryAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(CategoryMainPanel.GEOMETRY);
                }
                break;
            }
            case CategoryMainPanel.FILTERS: {
                mAdapter = activity.getCategoryFiltersAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(CategoryMainPanel.FILTERS);
                }
                break;
            }
            case CategoryMainPanel.VERSIONS: {
                mAdapter = activity.getCategoryVersionsAdapter();
                if (mAdapter != null) {
                    mAdapter.initializeSelection(CategoryMainPanel.VERSIONS);
                }
                break;
            }
        }
        updateAddButtonVisibility();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt(PARAMETER_TAG, mCurrentAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout main = (LinearLayout) inflater.inflate(R.layout.filtershow_category_panel, container, false);

        if (savedInstanceState != null) {
            int selectedPanel = savedInstanceState.getInt(PARAMETER_TAG);
            loadAdapter(selectedPanel);
        }
        CategoryListView panel = (CategoryListView) main.findViewById(R.id.listItems);
        if (mAdapter != null) {
            mAdapter.setOrientation(CategoryView.HORIZONTAL);
            panel.setAdapter(mAdapter);
            mAdapter.setContainer(panel);
        }
        mAddButton = (CategoryIconView) main.findViewById(R.id.addButton);
        if (mAddButton != null) {
            mAddButton.setOnClickListener(this);
            updateAddButtonVisibility();
        }
        return main;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addButton:
                break;
        }
    }

    public void updateAddButtonVisibility() {
        if (mAddButton == null) {
            return;
        }
        if (mAdapter.showAddButton()) {
            mAddButton.setVisibility(View.VISIBLE);
            if (mAdapter != null) {
                mAddButton.setText(mAdapter.getAddButtonText());
            }
        } else {
            mAddButton.setVisibility(View.GONE);
        }
    }
}
