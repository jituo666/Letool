
package com.xjt.letool.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.xjt.letool.activities.LetoolActivity;
import com.xjt.letool.R;
import com.xjt.letool.common.LLog;

import java.util.List;

@SuppressWarnings("unused")
public class SlidingMenuFragment extends Fragment {

    private static final String TAG = "SlidingMenuFragment";

    public static final int SLIDING_MENU_HIDE_LIST = 0;
    public static final int SLIDING_MENU_ABOUT = 1;
    public static final int SLIDING_MENU_SETTING = 2;
    public static final int SLIDING_MENU_HOME = 3;
    public static final int SLIDING_MENU_EXIT = 4;

    private static final SlidingMenuItem[] SLIDING_MENUS = new SlidingMenuItem[] {
            new SlidingMenuItem(SLIDING_MENU_HIDE_LIST, R.drawable.ic_action_view_as_list, R.string.sliding_menu_title_hide_list, true, true),
            new SlidingMenuItem(SLIDING_MENU_ABOUT, R.drawable.ic_action_about, R.string.sliding_menu_title_about, true, true),
            new SlidingMenuItem(SLIDING_MENU_SETTING, R.drawable.ic_action_settings, R.string.sliding_menu_title_settings, true, true),
            new SlidingMenuItem(SLIDING_MENU_HOME, R.drawable.ic_action_view_as_grid, R.string.sliding_menu_title_home, true, true),
            new SlidingMenuItem(SLIDING_MENU_EXIT, R.drawable.ic_action_exit, R.string.sliding_menu_title_exit, true, true),
    };

    private static final Class<?>[] MenuFragmentClasses = new Class<?>[] {
            HideListFragment.class,
            AboutFragment.class,
            SettingFragment.class

    };

    private ListView mMenusList;
    private ImageView mMenuLogo;
    private LetoolActivity mActivity;
    private FragmentManager mFragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getFragmentManager();
        mActivity = (LetoolActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.sliding_menu, container, false);
        rootView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent m) {
                ((LetoolActivity) getActivity()).getLetoolSlidingMenu().toggle();
                return true;
            }

        });
        mMenuLogo = (ImageView) rootView.findViewById(R.id.siding_menu_logo_image);
        mMenuLogo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });
        mMenusList = (ListView) rootView.findViewById(R.id.sliding_menu_list);
        mMenusList.setAdapter(new SlidingMenuAdapter());
        mMenusList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                try {
                    if (position < MenuFragmentClasses.length) {
                        Fragment f = (Fragment) MenuFragmentClasses[position].newInstance();
                        FragmentTransaction ft = mFragmentManager.beginTransaction();
                        if (mFragmentManager.getFragments().size() >= 4) {
                            ft.setCustomAnimations(0, R.anim.slide_left_out);
                            ft.remove(SlidingMenuFragment.this);
                            ft.setCustomAnimations(0, 0);
                            ft.replace(R.id.root_container, f);
                        } else {
                            ft.setCustomAnimations(0, R.anim.slide_left_out);
                            ft.remove(SlidingMenuFragment.this);
                            ft.add(R.id.root_container, f);
                        }
                        ft.commit();
                    } else if (position == MenuFragmentClasses.length) { // exit
                        naviToHome();
                    } else if (position - 1 == MenuFragmentClasses.length) {
                        getActivity().finish();
                    }
                } catch (java.lang.InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
        LLog.i(TAG, " mFragmentManager" + mFragmentManager);
        return rootView;
    }

    private void naviToHome() {
        List<Fragment> list = mFragmentManager.getFragments();
        if (list.size() > 2) {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            boolean hasTopFragment = false;
            for (int p = list.size(); p > 2; p--) {
                Fragment f = list.get(p - 1);
                if (f != null) {
                    hasTopFragment = true;
                    if (f instanceof SlidingMenuFragment) {
                        ft.setCustomAnimations(0, R.anim.slide_left_out);
                    } else {
                        ft.setCustomAnimations(0, 0);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                    }
                    ft.remove(f);
                }
            }
            if (hasTopFragment) {
                ft.commit();
                mActivity.getLetoolActionBar().setTitle(R.string.app_name);
            }
        }
    }

    private static class SlidingMenuItem {
        public int menuId;
        public int menuIcon;
        public int menuTitle;
        public boolean menuEnabled;
        public boolean menuVisible;

        public SlidingMenuItem(int id, int icon, int title, boolean enabled) {
            this(id, icon, title, enabled, true);
        }

        public SlidingMenuItem(int id, int icon, int title, boolean enabled, boolean visible) {
            this.menuId = id;
            this.menuIcon = icon;
            this.menuTitle = title;
            this.menuEnabled = enabled;
            this.menuVisible = visible;
        }
    }

    private class SlidingMenuAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return SLIDING_MENUS.length;
        }

        @Override
        public Object getItem(int position) {
            return SLIDING_MENUS[position];
        }

        @Override
        public long getItemId(int position) {
            return SLIDING_MENUS[position].menuId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View v;
            if (convertView == null) {
                v = getActivity().getLayoutInflater().inflate(R.layout.sliding_menu_item, parent, false);
            } else {
                v = convertView;
            }
            TextView textView = (TextView) v.findViewById(R.id.menu_title);
            textView.setText(SLIDING_MENUS[position].menuTitle);
            ImageView imageView = (ImageView) v.findViewById(R.id.menu_icon);
            imageView.setImageDrawable(getActivity().getResources().getDrawable(SLIDING_MENUS[position].menuIcon));
            return v;
        }
    }

}
