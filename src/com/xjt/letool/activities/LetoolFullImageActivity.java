
package com.xjt.letool.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.xjt.letool.R;
import com.xjt.letool.common.LLog;
import com.xjt.letool.data.DataManager;
import com.xjt.letool.fragments.FullImageFragment;

public class LetoolFullImageActivity extends LetoolBaseActivity {

    private static final String TAG = LetoolFullImageActivity.class.getSimpleName();

    public static final int REQUEST_FOR_PHOTO = 100;
    public static final String KEY_MEDIA_SET_PATH = "media_set_path";

    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.layout_main);
        super.onCreate(savedInstanceState);
        mFragmentManager = getSupportFragmentManager();
        Fragment fragment = new FullImageFragment();
        Bundle data = new Bundle();
        String albumTitle = getIntent().getStringExtra(KEY_ALBUM_TITLE);
        long albumId = getIntent().getLongExtra(KEY_ALBUM_ID, 0);
        String albumMediaPath = getIntent().getStringExtra(KEY_MEDIA_PATH);
        //boolean isCamera = getIntent().getBooleanExtra(KEY_MEDIA_PATH, false);
        data.putString(KEY_ALBUM_TITLE, albumTitle);
        data.putLong(KEY_ALBUM_ID, albumId);
        data.putString(KEY_MEDIA_PATH, albumMediaPath);
        data.putBoolean(KEY_IS_CAMERA, false);
        fragment.setArguments(data);
        LLog.i(TAG, " start album id:" + albumId + " albumTitle:" + albumTitle + " albumMediaPath:" + albumMediaPath + " isCamera:");
        mFragmentManager.beginTransaction().add(R.id.root_container, fragment, "PhotoFragment").commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
