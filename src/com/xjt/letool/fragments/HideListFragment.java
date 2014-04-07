
package com.xjt.letool.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xjt.letool.R;
import com.xjt.letool.activities.LetoolActivity;

public class HideListFragment extends Fragment {

    private LetoolActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (LetoolActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.app_hide_list, container, false);
        TextView actionBarNaviText = (TextView)mActivity.getLetoolActionBar().getActionPanel().findViewById(R.id.navi_text);
        actionBarNaviText.setText(R.string.sliding_menu_title_hide_list);
        return rootView;
    }
}
