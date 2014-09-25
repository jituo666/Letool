
package com.xjt.newpic.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;
import com.xjt.newpic.NpContext;
import com.xjt.newpic.R;
import com.xjt.newpic.common.ApiHelper;
import com.xjt.newpic.imagedata.blobcache.BlobCacheManager;
import com.xjt.newpic.preference.GlobalPreference;
import com.xjt.newpic.settings.NpPreference;
import com.xjt.newpic.stat.StatConstants;
import com.xjt.newpic.utils.StorageUtils;
import com.xjt.newpic.utils.StringUtils;
import com.xjt.newpic.view.NpTopBar;
import com.xjt.newpic.view.NpTopBar.OnActionModeListener;

/**
 * @Author Jituo.Xuan
 * @Date 9:47:49 AM Apr 19, 2014
 * @Comments:null
 */
public class SettingFragment extends Fragment implements OnActionModeListener {

    private static final String TAG = SettingFragment.class.getSimpleName();

    private NpPreference mCameraSource;
    private NpPreference mAnimSwitch;
    private NpPreference mRememberUISwitch;
    private NpPreference mClearCache;
    private NpPreference mVersionCheck;
    private NpPreference mAuthorDesc;
    private NpPreference mQQGroup;
    private NpContext mLetoolContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLetoolContext = (NpContext) this.getActivity();
    }

    public String getVersion() {
        try {
            PackageManager manager = getActivity().getPackageManager();
            PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
            Date now = new Date(info.lastUpdateTime);
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.CHINESE);
            return getString(R.string.version_update_check_desc, info.versionName, f.format(now));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void initBrowseActionBar() {
        NpTopBar topBar = mLetoolContext.getLetoolTopBar();
        topBar.setOnActionMode(NpTopBar.ACTION_BAR_MODE_SETTINGS, this);
        topBar.setTitleIcon(R.drawable.ic_action_previous_item);
        topBar.setTitleText(R.string.common_settings);
        ViewGroup nativeButtons = (ViewGroup) topBar.getActionPanel().findViewById(R.id.navi_buttons);
        nativeButtons.setVisibility(View.GONE);
    }

    private void initViews() {
        String x = getString(R.string.clear_cache_desc, getCacheSize(), StringUtils.formatBytes(StorageUtils.getExternalStorageAvailableSize()));

        mCameraSource.setSettingItemText(getString(R.string.camera_source_dirs), getString(R.string.camera_source_dirs_desc), false);
        mAnimSwitch.setSettingItemText(getString(R.string.anim_switch_title), getString(R.string.anim_switch_desc), true);
        mAnimSwitch.setChecked(GlobalPreference.isAnimationOpen(getActivity()));
        mRememberUISwitch.setSettingItemText(getString(R.string.remember_ui_switch_title), getString(R.string.remember_ui_switch_desc), true);
        mRememberUISwitch.setChecked(GlobalPreference.rememberLastUI(getActivity()));
        mClearCache.setSettingItemText(getString(R.string.clear_cache_title), x, false);
        mVersionCheck.setSettingItemText(getString(R.string.version_update_check_title), getVersion(), false);

        mCameraSource.setOnClickListener(this);
        mAnimSwitch.setOnClickListener(this);
        mRememberUISwitch.setOnClickListener(this);
        mClearCache.setOnClickListener(this);
        mVersionCheck.setOnClickListener(this);
        //mAuthorDesc.setOnClickListener(this);
        //mQQGroup.setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.app_settings_list, container, false);
        initBrowseActionBar();
        mCameraSource = (NpPreference) rootView.findViewById(R.id.camera_source);
        mAnimSwitch = (NpPreference) rootView.findViewById(R.id.anim_switch);
        mRememberUISwitch = (NpPreference) rootView.findViewById(R.id.remember_ui_switch);
        mClearCache = (NpPreference) rootView.findViewById(R.id.clear_cache);
        mVersionCheck = (NpPreference) rootView.findViewById(R.id.version_update_check);
        initViews();
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.camera_source) {
            MobclickAgent.onEvent(getActivity(), StatConstants.EVENT_KEY_CAMERA_SRC_SETTING);
            CameraSourceSettingFragment f = new CameraSourceSettingFragment();
            mLetoolContext.pushContentFragment(f, this, true);
        } else if (v.getId() == R.id.anim_switch) {
            mAnimSwitch.setChecked(!mAnimSwitch.isChecked());
            GlobalPreference.setAnimationOpen(getActivity(), mAnimSwitch.isChecked());
        } else if (v.getId() == R.id.remember_ui_switch) {
            mRememberUISwitch.setChecked(!mRememberUISwitch.isChecked());
            GlobalPreference.setRememberLastUI(getActivity(), mRememberUISwitch.isChecked());
            GlobalPreference.setLastUI(getActivity(), "");
        } else if (v.getId() == R.id.action_navi) {
            mLetoolContext.popContentFragment();
        } else if (v.getId() == R.id.clear_cache) {
            if (StorageUtils.externalStorageAvailable()) {
                MobclickAgent.onEvent(getActivity(), StatConstants.EVENT_KEY_CLEAR_CAHCE);
                new ClearCacheTask().execute();
            }
        } else if (v.getId() == R.id.version_update_check) {
            MobclickAgent.onEvent(getActivity(), StatConstants.EVENT_KEY_UPDATE_CHECK);
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            final Context context = getActivity();
            UmengUpdateAgent.setDefault();
            UmengUpdateAgent.setUpdateAutoPopup(false);
            UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {

                @Override
                public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    switch (updateStatus) {
                        case UpdateStatus.Yes: // has update
                            UmengUpdateAgent.showUpdateDialog(context, updateInfo);
                            break;
                        case UpdateStatus.No: // has no update
                            Toast.makeText(context, R.string.app_no_update, Toast.LENGTH_SHORT).show();
                            break;
                        case UpdateStatus.NoneWifi: // none wifi
                            Toast.makeText(context, R.string.app_update_only_wifi, Toast.LENGTH_SHORT).show();
                            break;
                        case UpdateStatus.Timeout: // time out
                            Toast.makeText(context, R.string.common_connect_timeout, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
            UmengUpdateAgent.setUpdateOnlyWifi(false);
            UmengUpdateAgent.update(context);
            progressDialog.setMessage(mLetoolContext.getActivityContext().getString(R.string.common_update_checking));
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private void copyQQToClipBoard() {
        ClipboardManager cmb = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText("2518545630");
        Toast.makeText(mLetoolContext.getActivityContext(), R.string.app_QQ_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    /****************
    *
    * 发起添加群流程。群号：了图交流群(248706772) 的 key 为： pan68pjSBp1edKE0a6mUIUogCS4U-qZW
    * 调用 joinQQGroup(pan68pjSBp1edKE0a6mUIUogCS4U-qZW) 即可发起手Q客户端申请加群 了图交流群(248706772)
    *
    * @param key 由官网生成的key
    * @return 返回true表示呼起手Q成功，返回false表示呼起失败
    ******************/
    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }

    private String getCacheSize() {
        try {
            return StringUtils.formatBytes(getFolderSize(getActivity().getApplication().getExternalCacheDir()));
        } catch (Exception e) {
            return "0B";
        }
    }

    public static long getFolderSize(java.io.File file) throws Exception {
        long size = 0;
        java.io.File[] fileList = file.listFiles();
        for (int i = 0; i < fileList.length; i++)
        {
            if (fileList[i].isDirectory())
            {
                size = size + getFolderSize(fileList[i]);
            } else
            {
                size = size + fileList[i].length();
            }
        }
        return size;
    }

    private class ClearCacheTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(getActivity());
            dialog.setTitle(getActivity().getString(R.string.common_clear_cache));
            dialog.setMessage(getActivity().getString(R.string.common_clear_cache_waitting));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            BlobCacheManager.clearCachedFiles(mLetoolContext.getActivityContext());
            ImageLoader.getInstance().clearDiscCache();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (dialog.isShowing()) {
                dialog.dismiss();
                String x = getString(R.string.clear_cache_desc, getCacheSize(), StringUtils.formatBytes(StorageUtils.getExternalStorageAvailableSize()));
                mClearCache.setSettingItemText(getString(R.string.clear_cache_title), x, false);
            }
            Toast t = Toast.makeText(getActivity(), R.string.clear_cache_finished, Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
    }

}
