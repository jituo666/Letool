package com.xjt.letool.share;

import java.util.ArrayList;
import java.util.List;

import com.xjt.letool.R;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

public class ShareManager implements ShareListener {

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

    public static List<ShareTo> getShareToList(Context context) {
        ArrayList<ShareTo> list = new ArrayList<ShareTo>();
        if (isQzoneInstalled(context)) {
            list.add(new ShareTo(AppConstants.SHARE_TO_QQ, "QQ", context.getResources().getDrawable(R.drawable.ic_launcher)));
            list.add(new ShareTo(AppConstants.SHARE_TO_QZONE, "QZone", context.getResources().getDrawable(R.drawable.ic_launcher)));
        }
        if (isWeichatIstalled(context)) {
            list.add(new ShareTo(AppConstants.SHARE_TO_WX, "微信", context.getResources().getDrawable(R.drawable.ic_launcher)));
            list.add(new ShareTo(AppConstants.SHARE_TO_WX_F, "微信朋友圈", context.getResources().getDrawable(R.drawable.ic_launcher)));
        }
        return list;
    }

    private static boolean isQzoneInstalled(Context context) {
        boolean ret = false;
        PackageInfo p = null;
        try {
            p = null;
            p = context.getPackageManager().getPackageInfo("com.tencent.mobileqq", 0);
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

    private static boolean isWeichatIstalled(Context context) {
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
    public void onShareTo(int shareTo, Bundle shareData) {
        switch (shareTo) {
            case AppConstants.SHARE_TO_QQ: {
                break;
            }
            case AppConstants.SHARE_TO_QZONE: {
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
