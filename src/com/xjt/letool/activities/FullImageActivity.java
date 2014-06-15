
package com.xjt.letool.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.umeng.analytics.MobclickAgent;
import com.xjt.letool.R;
import com.xjt.letool.fragment.FullImageFragment;

public class FullImageActivity extends BaseFragmentActivity {

    private static final String TAG = FullImageActivity.class.getSimpleName();

    public static final int REQUEST_FOR_PHOTO = 100;
    public static final String KEY_MEDIA_SET_PATH = "media_set_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.layout_main);
        super.onCreate(savedInstanceState);
        Fragment fragment = new FullImageFragment();
        Bundle data = new Bundle();
        String albumTitle = getIntent().getStringExtra(ThumbnailActivity.KEY_ALBUM_TITLE);
        int albumId = getIntent().getIntExtra(ThumbnailActivity.KEY_ALBUM_ID, 0);
        String albumMediaPath = getIntent().getStringExtra(ThumbnailActivity.KEY_MEDIA_PATH);
        int currentIndex = getIntent().getIntExtra(FullImageFragment.KEY_INDEX_HINT, 0);
        data.putString(ThumbnailActivity.KEY_ALBUM_TITLE, albumTitle);
        data.putInt(ThumbnailActivity.KEY_ALBUM_ID, albumId);
        data.putString(ThumbnailActivity.KEY_MEDIA_PATH, albumMediaPath);
        data.putBoolean(ThumbnailActivity.KEY_IS_PHOTO_ALBUM, getIntent().getBooleanExtra(ThumbnailActivity.KEY_IS_PHOTO_ALBUM, false));
        data.putInt(FullImageFragment.KEY_INDEX_HINT, currentIndex);
        fragment.setArguments(data);
        mFragmentManager.beginTransaction().add(R.id.root_container, fragment, "FullImageFragment").commit();
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
    protected void onDestroy() {
        super.onDestroy();
    }

}
