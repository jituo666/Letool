/**
 * @Author Jituo.Xuan
 * @Date Jul 28, 2014 10:54:21 AM
 * @Comments:null
 */

package com.xjt.letool.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import com.xjt.letool.common.LLog;

public class PackageUtils {

    private static final String TAG = PackageUtils.class.getSimpleName();

    //    private final static String[] FILTER_APP_PACKAGENAME = {
    //            "com.mt.mtxx.mtxx",// 美图秀秀
    //            "my.beautyCamera",// 美人相机
    //            "my.PCamera",// POCO相机
    //            "com.ei.hdrphoto",// 好照片
    //            "cn.jingling.motu.photowonder",// 百度魔图
    //            "cn.poco.foodcamera", // POCO美食相机
    //            "jp.naver.linecamera.android",//LINE camera
    //            "powercam.activity", // PowerCam"
    //            "com.huaban.android", //采集到花瓣
    //    };

    private final static String[] SHARED_APP_PACKAGENAME = {
            "com.tencent.mm.ui.tools.ShareImgUI",// 微信好友
            "com.tencent.mm.ui.tools.ShareToTimeLineUI",// 微信朋友圈
            "com.tencent.mobileqq.activity.JumpActivity",// QQ好友
            "com.tencent.mobileqq.activity.qfileJumpActivity",// Q我的电脑
            "com.qzone.ui.operation.QZonePublishMoodActivity", // QQ空间
            "com.sina.weibo.EditActivity", // 新浪微博
            "com.tencent.WBlog.intentproxy.TencentWeiboIntent", //腾讯微博
    };

    private final static String[] SHARED_APP_PACKAGENAME_LABEL = {
            "微信好友",
            "微信朋友圈",
            "QQ好友",
            "我的电脑",
            "QQ空间",
            "新浪微博",
            "腾讯微博"
    };

    public static class AppInfo {

        public String pkgName;
        public String launcherClass;
        /** May be null. */
        public String label;
        /** May be null. */
        public Drawable icon;
        /** May be null. */
        public String versionName;
        /** May be 0. */
        public int versionCode;
    }

    private static List<ResolveInfo> getShareApps(Context context, String mimeType, boolean singleSharing) {
        List<ResolveInfo> apps = new ArrayList<ResolveInfo>();
        String shareAction = singleSharing ? Intent.ACTION_SEND : Intent.ACTION_SEND_MULTIPLE;
        Intent intent = new Intent(shareAction, null);
        intent.setType("image/jpg");
        PackageManager pManager = context.getPackageManager();
        apps = pManager.queryIntentActivities(intent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        return apps;
    }

    public static List<AppInfo> getShareAppList(Context context, String mimeType, boolean singleSharing) {
        List<AppInfo> shareAppInfos = new ArrayList<AppInfo>();
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfos = getShareApps(context, mimeType, singleSharing);
        if (null == resolveInfos) {
            return shareAppInfos;
        } else {
            for (ResolveInfo resolveInfo : resolveInfos) {
                LLog.i(TAG, "--------resolveInfo:" + resolveInfo.activityInfo.name);
                if (isShareList(resolveInfo.activityInfo.name)) {
                    AppInfo appInfo = new AppInfo();
                    appInfo.pkgName = (resolveInfo.activityInfo.packageName);
                    appInfo.launcherClass = (resolveInfo.activityInfo.name);
                    appInfo.icon = (resolveInfo.loadIcon(packageManager));
                    shareAppInfos.add(appInfo);
                }
            }
        }
        List<AppInfo> result = new ArrayList<AppInfo>();
        for (int index = 0; index < SHARED_APP_PACKAGENAME.length; index++) {
            String s = SHARED_APP_PACKAGENAME[index];
            for (AppInfo i : shareAppInfos) {
                if (i.launcherClass.equals(s)) {
                    i.label = SHARED_APP_PACKAGENAME_LABEL[index];
                    result.add(i);
                }
            }
        }
        if (result.size() % 2 != 0) {
            result.add(new AppInfo());
        }
        return result;
    }

    private static boolean isShareList(String componentName) {
        for (String s : SHARED_APP_PACKAGENAME) {
            if (s.equals(componentName)) {
                return true;
            }
        }
        return false;
    }

}
