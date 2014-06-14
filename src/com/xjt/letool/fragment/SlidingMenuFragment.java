
package com.xjt.letool.fragment;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.xjt.letool.activities.BaseFragmentActivity;
import com.xjt.letool.activities.ThumbnailActivity;
import com.xjt.letool.R;
import com.xjt.letool.common.LLog;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.metadata.MediaSetUtils;
import com.xjt.letool.preference.GlobalPreference;
import com.xjt.letool.stat.StatConstants;
import com.xjt.letool.view.LetoolSlidingMenu;

import java.util.List;

/**
 * @Author Jituo.Xuan
 * @Date 9:48:19 AM Apr 19, 2014
 * @Comments:null
 */
@SuppressWarnings("unused")
public class SlidingMenuFragment extends Fragment {

    private static final String TAG = "SlidingMenuFragment";

    public static final int SLIDING_MENU_PHOTO = 0;
    public static final int SLIDING_MENU_FOLDER = 1;
    public static final int SLIDING_MENU_SETTING = 2;
    public static final int SLIDING_MENU_EXIT = 3;

    private static final SlidingMenuItem[] SLIDING_MENUS = new SlidingMenuItem[] {
            new SlidingMenuItem(SLIDING_MENU_PHOTO, R.drawable.ic_action_photo, R.string.common_photo, true, true),
            new SlidingMenuItem(SLIDING_MENU_FOLDER, R.drawable.ic_action_gallery, R.string.common_gallery, true, true),
            new SlidingMenuItem(SLIDING_MENU_SETTING, R.drawable.ic_action_settings, R.string.common_settings, true, true),
            new SlidingMenuItem(SLIDING_MENU_EXIT, R.drawable.ic_action_exit, R.string.common_exit, true, true),
    };

    private static final Class<?>[] MenuFragmentClasses = new Class<?>[] {
            PhotoFragment.class,
            GalleryFragment.class,
            SettingFragment.class

    };

    private static final String[] MenuFragmentTags = new String[] {
            LetoolFragment.FRAGMENT_TAG_PHOTO,
            LetoolFragment.FRAGMENT_TAG_FOLDER,
            LetoolFragment.FRAGMENT_TAG_SETTINGS
    };

    private static final String[] UMENT_STATS_KEY = new String[] {
            StatConstants.EVENT_KEY_SLIDE_MENU_PHOTO,
            StatConstants.EVENT_KEY_SLIDE_MENU_GALLERY,
            StatConstants.EVENT_KEY_SLIDE_MENU_SETTING
    };
    private ListView mMenusList;
    private ImageView mMenuLogo;
    private BaseFragmentActivity mActivity;
    private FragmentManager mFragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getFragmentManager();
        mActivity = (BaseFragmentActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.sliding_menu, container, false);
        rootView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent m) {
                mActivity.getLetoolSlidingMenu().toggle();
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
                        FragmentTransaction ft = mFragmentManager.beginTransaction();
                        ft.setCustomAnimations(R.anim.alpha_out, 0);
                        ft.remove(mFragmentManager.findFragmentByTag(LetoolFragment.FRAGMENT_TAG_SLIDING_MENU_ALPHA));
                        ft.setCustomAnimations(0, R.anim.slide_left_out);
                        ft.remove(SlidingMenuFragment.this);
                        if (mFragmentManager.findFragmentByTag(MenuFragmentTags[position]) == null) {
                            Fragment f = (Fragment) MenuFragmentClasses[position].newInstance();
                            initFragmentData(f);
                            ft.setCustomAnimations(0, 0);
                            ft.replace(R.id.root_container, f, MenuFragmentTags[position]);
                            MobclickAgent.onEvent(getActivity(), UMENT_STATS_KEY[position]);
                        }
                        ft.commit();
                    } else if (position == MenuFragmentClasses.length) { // exit
                        MobclickAgent.onEvent(getActivity(), StatConstants.EVENT_KEY_SLIDE_MENU_EXIT);
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

    private void initFragmentData(Fragment f) {
        if (f instanceof PhotoFragment) {
            Bundle data = new Bundle();
            data.putString(ThumbnailActivity.KEY_MEDIA_PATH, mActivity.getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
            data.putBoolean(ThumbnailActivity.KEY_IS_PHOTO_ALBUM, true);
            f.setArguments(data);
            GlobalPreference.setLastUIComponnents(this.getActivity(), LetoolFragment.FRAGMENT_TAG_PHOTO);
        } else if (f instanceof GalleryFragment) {
            Bundle data = new Bundle();
            data.putString(ThumbnailActivity.KEY_MEDIA_PATH, mActivity.getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_SET_ONLY));
            f.setArguments(data);
            GlobalPreference.setLastUIComponnents(this.getActivity(), LetoolFragment.FRAGMENT_TAG_FOLDER);
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

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
