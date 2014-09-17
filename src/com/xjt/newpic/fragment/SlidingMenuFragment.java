
package com.xjt.newpic.fragment;

import android.animation.ObjectAnimator;
import android.content.Intent;
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
import com.xjt.newpic.NpContext;
import com.xjt.newpic.R;
import com.xjt.newpic.activities.NpMediaActivity;
import com.xjt.newpic.activities.NpSettingsActivity;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.metadata.DataManager;
import com.xjt.newpic.metadata.MediaSetUtils;
import com.xjt.newpic.preference.GlobalPreference;
import com.xjt.newpic.stat.StatConstants;
import com.xjt.newpic.view.NpSlidingMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Jituo.Xuan
 * @Date 9:48:19 AM Apr 19, 2014
 * @Comments:null
 */
@SuppressWarnings("unused")
public class SlidingMenuFragment extends Fragment {

    private static final String TAG = SlidingMenuFragment.class.getSimpleName();

    public static final int SLIDING_MENU_PHOTO = 0;
    public static final int SLIDING_MENU_FOLDER = 1;
    public static final int SLIDING_MENU_SETTING = 2;
    public static final int SLIDING_MENU_EXIT = 3;

    private static final SlidingMenuItem[] SLIDING_MENUS = new SlidingMenuItem[] {
            new SlidingMenuItem(SLIDING_MENU_PHOTO, R.drawable.ic_action_picture, R.string.common_picture, true, true),
            new SlidingMenuItem(SLIDING_MENU_FOLDER, R.drawable.ic_action_video, R.string.common_movies, true, true),
            new SlidingMenuItem(SLIDING_MENU_SETTING, R.drawable.ic_action_settings, R.string.common_settings, true, true),
            new SlidingMenuItem(SLIDING_MENU_EXIT, R.drawable.ic_action_exit, R.string.common_exit, true, true)
    };

    private List<Intent> mIntents;
    private ListView mMenusList;
    private ImageView mMenuLogo;
    private NpContext mLetoolContext;
    private FragmentManager mFragmentManager;

    private void initIntentDatas() {
        mIntents = new ArrayList<Intent>();
        Intent itImage = new Intent(getActivity(), NpMediaActivity.class);
        itImage.putExtra(NpMediaActivity.KEY_IS_IMAGE, true);
        mIntents.add(itImage);
        Intent itVideo = new Intent(getActivity(), NpMediaActivity.class);
        itVideo.putExtra(NpMediaActivity.KEY_IS_IMAGE, false);
        mIntents.add(itVideo);
        Intent itSetting = new Intent(getActivity(), NpSettingsActivity.class);
        itSetting.putExtra(NpSettingsActivity.KEY_FROM_TIP, false);
        mIntents.add(itSetting);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getFragmentManager();
        mLetoolContext = (NpContext) getActivity();
        initIntentDatas();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.sliding_menu, container, false);
        rootView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent m) {
                mLetoolContext.getSlidingMenu().toggle();
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

                if (position < 2) {
                    mLetoolContext.getSlidingMenu().toggle();
                    if (mIntents.get(position).hasExtra(NpMediaActivity.KEY_IS_IMAGE)
                            && mLetoolContext.isImageBrwosing() == mIntents.get(position).getBooleanExtra(NpMediaActivity.KEY_IS_IMAGE, true)) {
                        return;
                    }
                    getActivity().startActivity(mIntents.get(position));
                    getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
                    getActivity().finish();
                    if (position == 0) {
                        MobclickAgent.onEvent(mLetoolContext.getActivityContext(), StatConstants.EVENT_KEY_SLIDE_MENU_PICTURE);
                    } else if (position == 1) {
                        MobclickAgent.onEvent(mLetoolContext.getActivityContext(), StatConstants.EVENT_KEY_SLIDE_MENU_VIDEO);
                    }
                } else if (position == 2) {
                    MobclickAgent.onEvent(mLetoolContext.getActivityContext(), StatConstants.EVENT_KEY_SLIDE_MENU_SETTING);
                    mLetoolContext.getSlidingMenu().toggle();
                    getActivity().startActivityForResult(mIntents.get(position), NpMediaActivity.REQUEST_CODE_SETTINGS);
                    getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
                } else if (position == 3) {
                    MobclickAgent.onEvent(mLetoolContext.getActivityContext(), StatConstants.EVENT_KEY_SLIDE_MENU_EXIT);
                    getActivity().finish();
                }

            }
        });
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
        MobclickAgent.onPageStart(TAG);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageEnd(TAG);
    }

}
