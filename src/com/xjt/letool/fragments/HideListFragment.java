
package com.xjt.letool.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xjt.letool.R;
import com.xjt.letool.activities.LetoolBaseActivity;

public class HideListFragment extends Fragment {

    private LetoolBaseActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (LetoolBaseActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.app_hide_list, container, false);
        mActivity.getLetoolActionBar().setTitle(R.string.sliding_menu_title_hide_list);
        return rootView;
    }
}
