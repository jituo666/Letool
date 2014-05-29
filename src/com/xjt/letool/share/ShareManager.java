package com.xjt.letool.share;

import java.util.ArrayList;
import java.util.List;

import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;
import com.xjt.letool.R;
import com.xjt.letool.common.LLog;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;

public class ShareManager implements ShareListener {

    private Context mContext;

    public static class ShareTo {

        int shareToType;
        String shareToTitle;
        Drawable shareToIcon;

        public ShareTo(int type, String title, Drawable icon) {
            shareToType = type;
            shareToTitle = title;
            shareToIcon = icon;
        }
    }

    public ShareManager(Context context) {
        mContext = context;
    }

    public List<ShareTo> getShareToList() {
        ArrayList<ShareTo> list = new ArrayList<ShareTo>();
        if (isQzoneInstalled()) {
            list.add(new ShareTo(AppConstants.SHARE_TO_QQ, "QQ", mContext.getResources().getDrawable(R.drawable.ic_launcher)));
            list.add(new ShareTo(AppConstants.SHARE_TO_QZONE, "QQ空间", mContext.getResources().getDrawable(R.drawable.ic_launcher)));
        }
        if (isWeichatIstalled(mContext)) {
            list.add(new ShareTo(AppConstants.SHARE_TO_WX, "微信", mContext.getResources().getDrawable(R.drawable.ic_launcher)));
            list.add(new ShareTo(AppConstants.SHARE_TO_WX_F, "微信朋友圈", mContext.getResources().getDrawable(R.drawable.ic_launcher)));
        }
        return list;
    }

    private boolean isQzoneInstalled() {
        boolean ret = false;
        PackageInfo p = null;
        try {
            p = null;
            p = mContext.getPackageManager().getPackageInfo("com.tencent.mobileqq", 0);
            if (null != p) {
                if (p.versionCode >= 50) {
                    ret = true;
                }
            } else {
                ret = false;
            }
        } catch (PackageManager.NameNotFoundException e2) {
            ret = false;
        }
        return ret;
    }

    private boolean isWeichatIstalled(Context context) {
        boolean ret = false;
        PackageInfo p = null;
        try {
            p = null;
            p = context.getPackageManager().getPackageInfo("com.tencent.mm", 0);
            if (null != p) {
                if (p.versionCode >= 255) {
                    ret = true;
                }
            } else {
                ret = false;
            }
        } catch (NameNotFoundException e2) {
            ret = false;
        }
        return ret;
    }


    
    @Override
    public void onShareTo(Activity activity, int shareTo, ArrayList<String> shareData,  IUiListener l) {
        switch (shareTo) {
            case AppConstants.SHARE_TO_QQ: {
                QQShareManager m = new QQShareManager(activity.getApplicationContext(), AppConstants.QQ_SHARE_APP_ID);
                m.shareImageToQQ(activity, "分享", "了图分享", shareData.get(0),l);

                break;
            }
            case AppConstants.SHARE_TO_QZONE: {
                QQShareManager m = new QQShareManager(activity.getApplicationContext(), AppConstants.QQ_SHARE_APP_ID);
                m.shareImageToQZone(activity, "分享", "了图分享", shareData, l);
                break;
            }
            case AppConstants.SHARE_TO_WX: {
                break;
            }
            case AppConstants.SHARE_TO_WX_F: {
                break;
            }
        }
    }
}
