
package com.xjt.letool.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.xjt.letool.R;
import com.xjt.letool.common.LLog;
import com.xjt.letool.fragment.FullImageFragment;

public class FullImageActivity extends BaseActivity {

    private static final String TAG = FullImageActivity.class.getSimpleName();

    public static final int REQUEST_FOR_PHOTO = 100;
    public static final String KEY_MEDIA_SET_PATH = "media_set_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.layout_main);
        super.onCreate(savedInstanceState);
        Fragment fragment = new FullImageFragment();
        Bundle data = new Bundle();
        String albumTitle = getIntent().getStringExtra(KEY_ALBUM_TITLE);
        long albumId = getIntent().getLongExtra(KEY_ALBUM_ID, 0);
        String albumMediaPath = getIntent().getStringExtra(KEY_MEDIA_PATH);
        int currentIndex = getIntent().getIntExtra(FullImageFragment.KEY_INDEX_HINT, 0);
        data.putString(KEY_ALBUM_TITLE, albumTitle);
        data.putLong(KEY_ALBUM_ID, albumId);
        data.putString(KEY_MEDIA_PATH, albumMediaPath);
        data.putBoolean(KEY_IS_CAMERA, false);
        data.putInt(FullImageFragment.KEY_INDEX_HINT, currentIndex);
        fragment.setArguments(data);
        mFragmentManager.beginTransaction().add(R.id.root_container, fragment, "PhotoFragment").commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
