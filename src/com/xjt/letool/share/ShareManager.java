
package com.xjt.letool.share;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.umeng.analytics.MobclickAgent;
import com.xjt.letool.R;
import com.xjt.letool.stat.StatConstants;
import com.xjt.letool.utils.PackageUtils;
import com.xjt.letool.utils.PackageUtils.AppInfo;
import com.xjt.letool.view.LetoolDialog;

import java.util.ArrayList;
import java.util.List;

public class ShareManager {

    public static void showAllShareDialog(final Activity activity, final String mimeType, final ArrayList<Uri> shareUris) {

        if (shareUris.size() == 0)
            return;
        final boolean singleShare = (shareUris.size() == 1);
        final List<AppInfo> shareToList = PackageUtils.getShareAppList(activity, mimeType, singleShare);

        if (shareToList.size() == 0) {
            Intent shareIntent = new Intent(singleShare ? Intent.ACTION_SEND : Intent.ACTION_SEND_MULTIPLE);
            if (singleShare)
                shareIntent.putExtra(Intent.EXTRA_STREAM, shareUris.get(0));
            else
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, shareUris);
            shareIntent.setType(mimeType);
            activity.startActivity(Intent.createChooser(shareIntent, activity.getString(R.string.common_share_to)));
        } else {
            final LetoolDialog dlg = new LetoolDialog(activity);
            dlg.setTitle(R.string.common_share_to);
            dlg.setCancelBtn(R.string.common_cancel, null);

            GridView l = dlg.setGridAdapter(new ShareToAllAdapter(activity, shareToList));
            l.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Intent shareIntent = new Intent(singleShare ? Intent.ACTION_SEND : Intent.ACTION_SEND_MULTIPLE);
                    shareIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    AppInfo appInfo = (AppInfo) shareToList.get(position);
                    if (appInfo.icon != null) {
                        dlg.dismiss();
                        shareIntent.setComponent(new ComponentName(appInfo.pkgName, appInfo.launcherClass));
                        if (singleShare)
                            shareIntent.putExtra(Intent.EXTRA_STREAM, shareUris.get(0));
                        else
                            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, shareUris);
                        shareIntent.setType(mimeType);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        activity.startActivity(shareIntent);
                        MobclickAgent.onEvent(activity, StatConstants.EVENT_KEY_FULL_IMAGE_SHARE_OK);
                    }
                }
            });
            dlg.show();
        }
    }
}
