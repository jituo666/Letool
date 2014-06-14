
package com.xjt.letool.activities;

import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateStatus;
import com.xjt.letool.R;
import com.xjt.letool.common.GlobalConstants;
import com.xjt.letool.common.LLog;
import com.xjt.letool.fragment.GalleryFragment;
import com.xjt.letool.fragment.LetoolFragment;
import com.xjt.letool.fragment.PhotoFragment;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.preference.GlobalPreference;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;

/**
 * @Author Jituo.Xuan
 * @Date 9:54:30 AM Apr 19, 2014
 * @Comments:null
 */
public class MainActivity extends BaseFragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String KEY_DISMISS_KEYGUARD = "dismiss-keyguard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.layout_main);
        super.onCreate(savedInstanceState);
        if (GlobalPreference.getLastUIComponents(this) == LetoolFragment.FRAGMENT_TAG_FOLDER) {
            Fragment fragment = new GalleryFragment();
            Bundle data = new Bundle();
            data.putString(ThumbnailActivity.KEY_MEDIA_PATH, getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_SET_ONLY));
            fragment.setArguments(data);
            getSupportFragmentManager().beginTransaction().add(R.id.root_container, fragment, LetoolFragment.FRAGMENT_TAG_FOLDER).commit();
        } else {
            Fragment fragment = new PhotoFragment();
            Bundle data = new Bundle();
            data.putString(ThumbnailActivity.KEY_MEDIA_PATH, getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
            data.putBoolean(ThumbnailActivity.KEY_IS_PHOTO_ALBUM, true);
            fragment.setArguments(data);
            getSupportFragmentManager().beginTransaction().add(R.id.root_container, fragment, LetoolFragment.FRAGMENT_TAG_PHOTO).commit();

        }

        mIsMainActivity = true;
        long time = GlobalPreference.getLastAppUpdateCheckTime(this);
        // app version check
        if (System.currentTimeMillis() - time > GlobalConstants.APP_VERSION_CHECK_INTERVAL) {
            UmengUpdateAgent.setDefault();
            UmengUpdateAgent.setUpdateUIStyle(UpdateStatus.STYLE_NOTIFICATION);
            UmengUpdateAgent.update(this);
            GlobalPreference.setAppUpdateCheckTime(this, System.currentTimeMillis());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        if (mFragmentManager.findFragmentByTag(LetoolFragment.FRAGMENT_TAG_PHOTO) != null) {
            GlobalPreference.setLastUIComponnents(this, LetoolFragment.FRAGMENT_TAG_PHOTO);
        } else if (mFragmentManager.findFragmentByTag(LetoolFragment.FRAGMENT_TAG_FOLDER) != null) {
            GlobalPreference.setLastUIComponnents(this, LetoolFragment.FRAGMENT_TAG_FOLDER);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
