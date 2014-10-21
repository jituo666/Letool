
package com.xjt.newpic.activities;

import com.umeng.analytics.MobclickAgent;
import com.xjt.newpic.R;
import com.xjt.newpic.preference.GlobalPreference;
import com.xjt.newpic.utils.PackageUtils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class NpGuideActivity extends FragmentActivity {

    ViewPager mPager;
    PictureAdapter mAdapter;
    private static final int[] NP_GUIDE_PAGE_RES = new int[] {
            R.drawable.np_guide_1, R.drawable.np_guide_2, R.drawable.np_guide_3, R.drawable.np_guide_4
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.np_guide_activity);
        mPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new PictureAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        MobclickAgent.onResume(this);
        super.onResume();
    }

    class PictureAdapter extends FragmentPagerAdapter {

        public PictureAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int index) {
            if (index == NP_GUIDE_PAGE_RES.length - 1) {
                return GuideFragmentEnter.newInstance(NP_GUIDE_PAGE_RES[index]);
            } else {

                return GuideFragment.newInstance(NP_GUIDE_PAGE_RES[index]);
            }
        }

        @Override
        public int getCount() {
            return NP_GUIDE_PAGE_RES.length;
        }

    }

    static class GuideFragment extends Fragment {

        private int mResId;

        public static GuideFragment newInstance(int rid) {
            GuideFragment fragment = new GuideFragment(rid);
            return fragment;
        }

        public GuideFragment(int rid) {
            mResId = rid;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            ImageView iv = new ImageView(getActivity());
            iv.setImageResource(mResId);
            iv.setScaleType(ScaleType.FIT_XY);
            return iv;
        }
    }

    static class GuideFragmentEnter extends Fragment {

        private int mResId;

        public static GuideFragmentEnter newInstance(int rid) {
            GuideFragmentEnter fragment = new GuideFragmentEnter(rid);
            return fragment;
        }

        public GuideFragmentEnter(int rid) {
            mResId = rid;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.np_guide_enter_fragment, container, false);
            ImageView iv = (ImageView) rootView.findViewById(R.id.guide_pic);
            iv.setImageResource(mResId);

            ImageView ib = (ImageView) rootView.findViewById(R.id.guide_endter);
            ib.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    getActivity().finish();
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), NpMainActivity.class);
                    startActivity(intent);
                    GlobalPreference.setLastGuideCode(getActivity(), PackageUtils.getVersionCode(getActivity()));
                }
            });
            return rootView;
        }
    }
}
