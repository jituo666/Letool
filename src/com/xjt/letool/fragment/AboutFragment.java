
package com.xjt.letool.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xjt.letool.R;
import com.xjt.letool.activities.BaseFragmentActivity;


/**
 * @Author Jituo.Xuan
 * @Date 9:48:25 AM Apr 19, 2014
 * @Comments:null
 */
public class AboutFragment extends Fragment {

    private BaseFragmentActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (BaseFragmentActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.app_about, container, false);
        //mActivity.getLetoolTopBar().setTitleText(R.string.sliding_menu_title_about);
        return rootView;
    }
}
