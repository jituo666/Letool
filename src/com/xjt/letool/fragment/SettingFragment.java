
package com.xjt.letool.fragment;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;
import com.xjt.letool.LetoolContext;
import com.xjt.letool.R;
import com.xjt.letool.common.ApiHelper;
import com.xjt.letool.imagedata.blobcache.BlobCacheManager;
import com.xjt.letool.settings.LetoolPreference;
import com.xjt.letool.stat.StatConstants;
import com.xjt.letool.utils.StorageUtils;
import com.xjt.letool.utils.StringUtils;
import com.xjt.letool.view.LetoolTopBar;
import com.xjt.letool.view.LetoolTopBar.OnActionModeListener;

/**
 * @Author Jituo.Xuan
 * @Date 9:47:49 AM Apr 19, 2014
 * @Comments:null
 */
public class SettingFragment extends Fragment implements OnActionModeListener {

    private static final String TAG = SettingFragment.class.getSimpleName();

    private LetoolPreference mClearCache;
    private LetoolPreference mVersionCheck;
    private LetoolPreference mAuthorDesc;
    private LetoolPreference mQQGroup;
    private LetoolContext mLetoolContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLetoolContext = (LetoolContext) this.getActivity();
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
        LetoolTopBar topBar = mLetoolContext.getLetoolTopBar();
        topBar.setOnActionMode(LetoolTopBar.ACTION_BAR_MODE_SETTINGS, this);
        topBar.setTitleIcon(R.drawable.ic_action_previous_item);
        topBar.setTitleText(R.string.common_settings);
        ViewGroup nativeButtons = (ViewGroup) topBar.getActionPanel().findViewById(R.id.navi_buttons);
        nativeButtons.setVisibility(View.GONE);
    }

    private void initViews() {
        String x = getString(R.string.clear_cache_desc, getCacheSize(), StringUtils.formatBytes(StorageUtils.getExternalStorageAvailableSize()));
        mClearCache.setSettingItemText(getString(R.string.clear_cache_title), x);
        mVersionCheck.setSettingItemText(getString(R.string.version_update_check_title), getVersion());
        mAuthorDesc.setSettingItemText(getString(R.string.author_title), getString(R.string.author_desc));
        mQQGroup.setSettingItemText(getString(R.string.app_communication_platfrom), getString(R.string.app_QQ_group));
        mAuthorDesc.setOnClickListener(this);
        mClearCache.setOnClickListener(this);
        mVersionCheck.setOnClickListener(this);
        mQQGroup.setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.app_settings, container, false);
        initBrowseActionBar();
        mClearCache = (LetoolPreference) rootView.findViewById(R.id.clear_cache);
        mVersionCheck = (LetoolPreference) rootView.findViewById(R.id.version_update_check);
        mAuthorDesc = (LetoolPreference) rootView.findViewById(R.id.author_desc);
        mQQGroup = (LetoolPreference) rootView.findViewById(R.id.app_about);
        initViews();
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_navi) {
            getActivity().finish();
        } else if (v.getId() == R.id.clear_cache) {
            MobclickAgent.onEvent(getActivity(), StatConstants.EVENT_KEY_CLEAR_CAHCE);
            new ClearCacheTask().execute();
        } else if (v.getId() == R.id.version_update_check) {

            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            MobclickAgent.onEvent(getActivity(), StatConstants.EVENT_KEY_UPDATE_CHECK);
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
            UmengUpdateAgent.update(context);
            progressDialog.setMessage(mLetoolContext.getAppContext().getString(R.string.common_update_checking));
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog.show();
        } else if (v.getId() == R.id.author_desc) {
            if (ApiHelper.supportVersion(ApiHelper.VERSION_CODES.HONEYCOMB))
                copyQQToClipBoard();
        } else if (v.getId() == R.id.app_about) {
            if (!joinQQGroup("pan68pjSBp1edKE0a6mUIUogCS4U-qZW")) {
                Toast.makeText(mLetoolContext.getAppContext(), R.string.app_QQ_group_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private void copyQQToClipBoard() {
        ClipboardManager cmb = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText("2518545630");
        Toast.makeText(mLetoolContext.getAppContext(), R.string.app_QQ_to_clipboard, Toast.LENGTH_SHORT).show();
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
            BlobCacheManager.clearCachedFiles(mLetoolContext.getAppContext());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (dialog.isShowing()) {
                dialog.dismiss();
                String x = getString(R.string.clear_cache_desc, getCacheSize(), StringUtils.formatBytes(StorageUtils.getExternalStorageAvailableSize()));
                mClearCache.setSettingItemText(getString(R.string.clear_cache_title), x);
            }
            Toast.makeText(getActivity(), R.string.clear_cache_finished, Toast.LENGTH_SHORT).show();
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
