
package com.xjt.letool.fragment;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StatFs;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;
import com.xjt.letool.R;
import com.xjt.letool.common.LLog;
import com.xjt.letool.imagedata.blobcache.BlobCacheManager;
import com.xjt.letool.metadata.MediaPath;
import com.xjt.letool.settings.LetoolPreference;
import com.xjt.letool.utils.StorageUtils;
import com.xjt.letool.utils.StringUtils;
import com.xjt.letool.view.GLController;
import com.xjt.letool.view.LetoolActionBar;

import java.io.File;

/**
 * @Author Jituo.Xuan
 * @Date 9:47:49 AM Apr 19, 2014
 * @Comments:null
 */
public class SettingFragment extends LetoolFragment {

    private static final String TAG = SettingFragment.class.getSimpleName();
    private LetoolPreference mAuthorDesc;
    private LetoolPreference mClearCache;
    private LetoolPreference mVersionCheck;
    private LetoolPreference mAbout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initBrowseActionBar() {
        LetoolActionBar actionBar = getLetoolActionBar();
        actionBar.setOnActionMode(LetoolActionBar.ACTION_BAR_MODE_SETTINGS, this);
        actionBar.setTitleIcon(R.drawable.ic_drawer);
        actionBar.setTitleText(R.string.common_settings);

        View tip = getActivity().findViewById(R.id.action_navi_tip);
        int distance = Math.round(getResources().getDimension(R.dimen.letool_action_bar_height) / 12);
        ObjectAnimator.ofFloat(tip, "x", tip.getX() - distance, tip.getX()).setDuration(300).start();
    }

    private void initViews() {
        String x = getString(R.string.clear_cache_desc, getCacheSize(), StringUtils.formatBytes(StorageUtils.getExternalStorageAvailableSize()));
        LLog.i(TAG, "-----------------getCacheSize():" + getCacheSize());
        mClearCache.setSettingItemText(getString(R.string.clear_cache_title), x);
        mVersionCheck.setSettingItemText(getString(R.string.version_update_check_title), getString(R.string.version_update_check_desc));

        mAuthorDesc.setSettingItemText(getString(R.string.author_title), getString(R.string.author_desc));
        mAbout.setSettingItemText(getString(R.string.app_about_title), getString(R.string.app_about_desc));
        mAuthorDesc.setOnClickListener(this);
        mClearCache.setOnClickListener(this);
        mVersionCheck.setOnClickListener(this);
        mAbout.setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.app_settings, container, false);
        initBrowseActionBar();
        mAuthorDesc = (LetoolPreference) rootView.findViewById(R.id.author_desc);
        mClearCache = (LetoolPreference) rootView.findViewById(R.id.clear_cache);
        mVersionCheck = (LetoolPreference) rootView.findViewById(R.id.version_update_check);
        mAbout = (LetoolPreference) rootView.findViewById(R.id.app_about);
        initViews();
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_navi) {
            getLetoolSlidingMenu().toggle();
        } else if (v.getId() == R.id.clear_cache) {
            new ClearCacheTask().execute();
        } else if (v.getId() == R.id.version_update_check) {
            final Context context = getActivity();
            UmengUpdateAgent.setDefault();
            UmengUpdateAgent.setUpdateAutoPopup(false);
            UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {

                @Override
                public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                    switch (updateStatus) {
                        case UpdateStatus.Yes: // has update
                            UmengUpdateAgent.showUpdateDialog(context, updateInfo);
                            break;
                        case UpdateStatus.No: // has no update
                            Toast.makeText(context, "没有更新", Toast.LENGTH_SHORT).show();
                            break;
                        case UpdateStatus.NoneWifi: // none wifi
                            Toast.makeText(context, "没有wifi连接， 只在wifi下更新", Toast.LENGTH_SHORT).show();
                            break;
                        case UpdateStatus.Timeout: // time out
                            Toast.makeText(context, "超时", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
            UmengUpdateAgent.update(context);
        } else if (v.getId() == R.id.author_desc) {
        } else if (v.getId() == R.id.app_about) {
        }
    }

    @Override
    public GLController getGLController() {
        return null;
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
            BlobCacheManager.clearCachedFiles(getAndroidContext());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (dialog.isShowing()) {
                dialog.dismiss();
                String x = getString(R.string.clear_cache_desc, getCacheSize(), StringUtils.formatBytes(StorageUtils.getExternalStorageAvailableSize()));
                LLog.i(TAG, "-----------------getCacheSize():" + getCacheSize());
                mClearCache.setSettingItemText(getString(R.string.clear_cache_title), x);
            }
        }
    }
}
