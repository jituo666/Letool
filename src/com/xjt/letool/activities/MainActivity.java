
package com.xjt.letool.activities;

import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateStatus;
import com.xjt.letool.R;
import com.xjt.letool.common.LLog;
import com.xjt.letool.fragment.LetoolFragment;
import com.xjt.letool.fragment.PhotoFragment;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.utils.LetoolUtils;
import com.xjt.letool.utils.Utils;

import android.os.Bundle;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.WindowManager;

/**
 * @Author Jituo.Xuan
 * @Date 9:54:30 AM Apr 19, 2014
 * @Comments:null
 */
public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String KEY_DISMISS_KEYGUARD = "dismiss-keyguard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getIntent().getBooleanExtra(KEY_DISMISS_KEYGUARD, false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
        setContentView(R.layout.layout_main);
        super.onCreate(savedInstanceState);
        initializeByIntent();
        mIsMainActivity = true;
        UmengUpdateAgent.setDefault();
        UmengUpdateAgent.setUpdateUIStyle(UpdateStatus.STYLE_NOTIFICATION);
        UmengUpdateAgent.update(this);
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

    private void initializeByIntent() {

        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_GET_CONTENT.equalsIgnoreCase(action)) {
            startGetContentAction(intent);
        } else if (Intent.ACTION_PICK.equalsIgnoreCase(action)) {
            startPickAction(intent);
        } else if (Intent.ACTION_VIEW.equalsIgnoreCase(action)) {
            startViewAction(intent);
        } else {
            startDefaultAction();
        }
    }

    public void startDefaultAction() {
        Fragment fragment = new PhotoFragment();
        Bundle data = new Bundle();
        data.putString(ThumbnailActivity.KEY_MEDIA_PATH, getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
        data.putBoolean(ThumbnailActivity.KEY_IS_PHOTO_ALBUM, true);
        fragment.setArguments(data);
        getSupportFragmentManager().beginTransaction().add(R.id.root_container, fragment, LetoolFragment.FRAGMENT_TAG_THUMBNAIL).commit();
    }

    private void startGetContentAction(Intent intent) {

        Bundle data = intent.getExtras() != null ? new Bundle(intent.getExtras()) : new Bundle();
        data.putBoolean(KEY_GET_CONTENT, true);
        int typeBits = LetoolUtils.determineTypeBits(this, intent);
        data.putInt(KEY_TYPE_BITS, typeBits);
        //
        Fragment fragment = new PhotoFragment();
        data.putString(ThumbnailActivity.KEY_MEDIA_PATH, getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
        data.putBoolean(ThumbnailActivity.KEY_IS_PHOTO_ALBUM, true);
        fragment.setArguments(data);
        getSupportFragmentManager().beginTransaction().add(R.id.root_container, fragment, LetoolFragment.FRAGMENT_TAG_THUMBNAIL).commit();
    }

    /**
     * We do NOT really support the PICK intent. Handle it as the GET_CONTENT. However
     * , we need to translate the type in the intent here.
     * @param intent
     */
    private void startPickAction(Intent intent) {
        Bundle data = intent.getExtras() != null ? new Bundle(intent.getExtras()) : new Bundle();
        LLog.w(TAG, "action PICK is not supported");
        String type = Utils.ensureNotNull(intent.getType());
        if (type.startsWith("vnd.android.cursor.dir/")) {
            if (type.endsWith("/image"))
                intent.setType("image/*");
            if (type.endsWith("/video"))
                intent.setType("video/*");
        }
        data.putBoolean(KEY_GET_CONTENT, true);
        int typeBits = LetoolUtils.determineTypeBits(this, intent);
        data.putInt(KEY_TYPE_BITS, typeBits);
        //
        Fragment fragment = new PhotoFragment();
        data.putString(ThumbnailActivity.KEY_MEDIA_PATH, getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
        data.putBoolean(ThumbnailActivity.KEY_IS_PHOTO_ALBUM, true);
        fragment.setArguments(data);
        getSupportFragmentManager().beginTransaction().add(R.id.root_container, fragment, LetoolFragment.FRAGMENT_TAG_THUMBNAIL).commit();
    }

    private void startViewAction(Intent intent) {

    }

}
