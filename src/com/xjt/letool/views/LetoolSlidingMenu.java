
package com.xjt.letool.views;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.xjt.letool.R;

@SuppressWarnings("unused")
public class LetoolSlidingMenu {

    private View mRootView = null;
    private ListView mMenusList;
    private Context mContext;

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
                v = mInflater.inflate(R.layout.sliding_menu_item, null);
            } else {
                v = convertView;
            }
            TextView textView = (TextView) v.findViewById(R.id.menu_title);
            textView.setText(SLIDING_MENUS[position].menuTitle);
            ImageView imageView = (ImageView)v.findViewById(R.id.menu_icon);
            imageView.setImageDrawable(mContext.getResources().getDrawable(SLIDING_MENUS[position].menuIcon));
            return v;
        }
    }

    private LayoutInflater mInflater;

    public LetoolSlidingMenu(Activity activity, View rootView) {
        mRootView = rootView;
        mMenusList = (ListView) rootView.findViewById(R.id.sliding_menu_list);
        mInflater = activity.getLayoutInflater();
        mContext = activity;
        mMenusList.setAdapter(new SlidingMenuAdapter());
    }

    public void updateVisibility() {
        if (mRootView.getVisibility() == View.GONE) {
            mRootView.setVisibility(View.VISIBLE);
            mRootView.startAnimation(AnimationUtils.loadAnimation(mRootView.getContext(), R.anim.slide_left_in));
        } else if (mRootView.getVisibility() == View.VISIBLE) {
            mRootView.setVisibility(View.GONE);
            mRootView.startAnimation(AnimationUtils.loadAnimation(mRootView.getContext(), R.anim.slide_left_out));
        }
    }

}
