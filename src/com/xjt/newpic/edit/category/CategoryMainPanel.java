package com.xjt.newpic.edit.category;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.NpEditActivity;
import com.xjt.newpic.edit.imageshow.ImageManager;

public class CategoryMainPanel extends Fragment {

    private static final String TAG = CategoryMainPanel.class.getSimpleName();

    private LinearLayout mMainView;
    private View looksButton;
    private View bordersButton;
    private View geometryButton;
    private View filtersButton;

    public static final String FRAGMENT_TAG = TAG;
    public static final int LOOKS = 0;
    public static final int BORDERS = 1;
    public static final int GEOMETRY = 2;
    public static final int FILTERS = 3;
    public static final int VERSIONS = 4;

    private int mCurrentSelected = -1;

    private void selection(int position, boolean value) {
        if (value) {
            NpEditActivity activity = (NpEditActivity) getActivity();
            activity.setCurrentPanel(position);
        }
        switch (position) {
            case LOOKS: {
                looksButton.setSelected(value);
                break;
            }
            case BORDERS: {
                bordersButton.setSelected(value);
                break;
            }
            case GEOMETRY: {
                geometryButton.setSelected(value);
                break;
            }
            case FILTERS: {
                filtersButton.setSelected(value);
                break;
            }
        }
    }

    private void showPanel(int currentPanel) {
        switch (currentPanel) {
            case LOOKS: {
                loadCategoryLookPanel(false);
                break;
            }
            case BORDERS: {
                loadCategoryBorderPanel();
                break;
            }
            case GEOMETRY: {
                loadCategoryGeometryPanel();
                break;
            }
            case FILTERS: {
                loadCategoryFiltersPanel();
                break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMainView = (LinearLayout) inflater.inflate(R.layout.np_edit_main_panel, null, false);
        looksButton = (View) mMainView.findViewById(R.id.fxButton);
        bordersButton = (View) mMainView.findViewById(R.id.borderButton);
        geometryButton = (View) mMainView.findViewById(R.id.geometryButton);
        filtersButton = (View) mMainView.findViewById(R.id.colorsButton);
        looksButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showPanel(LOOKS);
            }
        });
        bordersButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showPanel(BORDERS);
            }
        });
        geometryButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showPanel(GEOMETRY);
            }
        });
        filtersButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showPanel(FILTERS);
            }
        });

        // 显示初始的category panel
        NpEditActivity activity = (NpEditActivity) getActivity();
        showPanel(activity.getCurrentPanel());
        return mMainView;
    }

    private boolean isRightAnimation(int newPos) {
        if (newPos < mCurrentSelected) {
            return false;
        }
        return true;
    }

    private void setCategoryFragment(CategoryPanel category, boolean fromRight) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (fromRight) {
            transaction.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out);
        } else {
            transaction.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_right_out);
        }
        transaction.replace(R.id.category_panel_container, category, CategoryPanel.FRAGMENT_TAG);
        transaction.commitAllowingStateLoss();
    }

    private void loadCategoryLookPanel(boolean force) {
        if (!force && mCurrentSelected == LOOKS) {
            return;
        }
        boolean fromRight = isRightAnimation(LOOKS);
        selection(mCurrentSelected, false);
        CategoryPanel categoryPanel = new CategoryPanel();
        categoryPanel.setAdapter(LOOKS);
        setCategoryFragment(categoryPanel, fromRight);
        mCurrentSelected = LOOKS;
        selection(mCurrentSelected, true);
    }

    private void loadCategoryBorderPanel() {
        if (mCurrentSelected == BORDERS) {
            return;
        }
        boolean fromRight = isRightAnimation(BORDERS);
        selection(mCurrentSelected, false);
        CategoryPanel categoryPanel = new CategoryPanel();
        categoryPanel.setAdapter(BORDERS);
        setCategoryFragment(categoryPanel, fromRight);
        mCurrentSelected = BORDERS;
        selection(mCurrentSelected, true);
    }

    private void loadCategoryGeometryPanel() {
        if (mCurrentSelected == GEOMETRY) {
            return;
        }
        if (ImageManager.getImage().hasTinyPlanet()) {
            return;
        }
        boolean fromRight = isRightAnimation(GEOMETRY);
        selection(mCurrentSelected, false);
        CategoryPanel categoryPanel = new CategoryPanel();
        categoryPanel.setAdapter(GEOMETRY);
        setCategoryFragment(categoryPanel, fromRight);
        mCurrentSelected = GEOMETRY;
        selection(mCurrentSelected, true);
    }

    private void loadCategoryFiltersPanel() {
        if (mCurrentSelected == FILTERS) {
            return;
        }
        boolean fromRight = isRightAnimation(FILTERS);
        selection(mCurrentSelected, false);
        CategoryPanel categoryPanel = new CategoryPanel();
        categoryPanel.setAdapter(FILTERS);
        setCategoryFragment(categoryPanel, fromRight);
        mCurrentSelected = FILTERS;
        selection(mCurrentSelected, true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMainView != null) {
            if (mMainView.getParent() != null) {
                ViewGroup parent = (ViewGroup) mMainView.getParent();
                parent.removeView(mMainView);
            }
        }
    }

}
