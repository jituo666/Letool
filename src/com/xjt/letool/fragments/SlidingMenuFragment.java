
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

@SuppressWarnings("unused")
public class SlidingMenuFragment extends Fragment {

    private static final String TAG = "SlidingMenuFragment";

    public static final int SLIDING_MENU_HIDE_LIST = 0;
    public static final int SLIDING_MENU_ABOUT = 2;
    public static final int SLIDING_MENU_SETTING = 1;
    public static final int SLIDING_MENU_EXIT = 3;

    private static final SlidingMenuItem[] SLIDING_MENUS = new SlidingMenuItem[] {
            new SlidingMenuItem(SLIDING_MENU_HIDE_LIST, R.drawable.ic_hide_list, R.string.sliding_menu_title_hide_list, true, true),
            new SlidingMenuItem(SLIDING_MENU_ABOUT, R.drawable.ic_app_about, R.string.sliding_menu_title_about, true, true),
            new SlidingMenuItem(SLIDING_MENU_SETTING, R.drawable.ic_app_settings, R.string.sliding_menu_title_settings, true, true),
            new SlidingMenuItem(SLIDING_MENU_SETTING, R.drawable.ic_app_exit, R.string.sliding_menu_title_exit, true, true),
    };

    private static final Class<?>[] MenuFragmentClasses = new Class<?>[] {
            HideListFragment.class,
            AboutFragment.class,
            SettingFragment.class

    };

    private ListView mMenusList;
    private ImageView mMenuLogo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.sliding_menu, container, false);
        rootView.setOnTouchListener(new OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent m) {
                ((LetoolActivity)getActivity()).getLetoolSlidingMenu().toggle();
                return true;
            }

        });
        mMenuLogo = (ImageView)rootView.findViewById(R.id.siding_menu_logo_image);
        mMenuLogo.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {

            }
        });
        mMenusList = (ListView) rootView.findViewById(R.id.sliding_menu_list);
        mMenusList.setAdapter(new SlidingMenuAdapter(inflater));
        mMenusList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                try {
                    if (position < MenuFragmentClasses.length) {
                        Fragment f = (Fragment) MenuFragmentClasses[position].newInstance();
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        if (getFragmentManager().getFragments().size() >= 4) {
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
                    } else { // exit
                        getActivity().finish();
                    }
                } catch (java.lang.InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
        LLog.i(TAG, " getFragmentManager" + this.getFragmentManager());
        return rootView;
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
        private LayoutInflater mLayoutInflater;

        public SlidingMenuAdapter(LayoutInflater inflater) {
            mLayoutInflater = inflater;
        }

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
                v = mLayoutInflater.inflate(R.layout.sliding_menu_item, null);
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
