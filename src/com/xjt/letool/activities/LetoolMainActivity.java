
package com.xjt.letool.activities;

import com.xjt.letool.R;
import com.xjt.letool.common.LLog;
import com.xjt.letool.data.DataManager;
import com.xjt.letool.data.MediaSetUtils;
import com.xjt.letool.utils.LetoolUtils;
import com.xjt.letool.utils.Utils;
import com.xjt.letool.views.fragments.LetoolFragment;
import com.xjt.letool.views.fragments.ThumbnailFragment;

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
public class LetoolMainActivity extends LetoolBaseActivity {

    private static final String TAG = LetoolMainActivity.class.getSimpleName();
    public static final String KEY_DISMISS_KEYGUARD = "dismiss-keyguard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getIntent().getBooleanExtra(KEY_DISMISS_KEYGUARD, false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
        setContentView(R.layout.layout_main);
        super.onCreate(savedInstanceState);
        initializeByIntent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
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
        Fragment fragment = new ThumbnailFragment();
        Bundle data = new Bundle();
        data.putLong(KEY_ALBUM_ID, MediaSetUtils.CAMERA_BUCKET_ID);
        data.putString(KEY_MEDIA_PATH, getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
        data.putBoolean(KEY_IS_CAMERA, true);
        data.putString(KEY_ALBUM_TITLE, getString(R.string.common_photo));
        fragment.setArguments(data);
        getSupportFragmentManager().beginTransaction().add(R.id.root_container, fragment, LetoolFragment.FRAGMENT_TAG_THUMBNAIL).commit();
    }

    private void startGetContentAction(Intent intent) {

        Bundle data = intent.getExtras() != null ? new Bundle(intent.getExtras()) : new Bundle();
        data.putBoolean(KEY_GET_CONTENT, true);
        int typeBits = LetoolUtils.determineTypeBits(this, intent);
        data.putInt(KEY_TYPE_BITS, typeBits);
        //
        Fragment fragment = new ThumbnailFragment();
        data.putLong(KEY_ALBUM_ID, MediaSetUtils.CAMERA_BUCKET_ID);
        data.putString(KEY_MEDIA_PATH, getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
        data.putBoolean(KEY_IS_CAMERA, true);
        data.putString(KEY_ALBUM_TITLE, getString(R.string.common_photo));
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
        Fragment fragment = new ThumbnailFragment();
        data.putLong(KEY_ALBUM_ID, MediaSetUtils.CAMERA_BUCKET_ID);
        data.putString(KEY_MEDIA_PATH, getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
        data.putBoolean(KEY_IS_CAMERA, true);
        data.putString(KEY_ALBUM_TITLE, getString(R.string.common_photo));
        fragment.setArguments(data);
        getSupportFragmentManager().beginTransaction().add(R.id.root_container, fragment, LetoolFragment.FRAGMENT_TAG_THUMBNAIL).commit();
    }

    private void startViewAction(Intent intent) {

    }

}
