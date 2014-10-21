
package com.xjt.newpic.share;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.umeng.analytics.MobclickAgent;
import com.xjt.newpic.R;
import com.xjt.newpic.stat.StatConstants;
import com.xjt.newpic.utils.PackageUtils;
import com.xjt.newpic.utils.PackageUtils.AppInfo;
import com.xjt.newpic.views.NpDialog;

import java.util.ArrayList;
import java.util.List;

public class ShareManager {

    public static interface ShareListener {
        public void shareTriggered();
    }

    public static void showAllShareDialog(final Activity activity, final String mimeType, final ArrayList<Uri> shareUris,
            final ShareListener sl) {

        if (shareUris.size() == 0)
            return;
        final boolean singleShare = (shareUris.size() == 1);
        final List<AppInfo> shareToList = PackageUtils.getShareAppList(activity, mimeType, singleShare);
        final String action = singleShare ? Intent.ACTION_SEND : Intent.ACTION_SEND_MULTIPLE;
        final Intent shareIntent = new Intent(action);
        shareIntent.setType(mimeType);
        if (shareToList.size() == 0) {
            if (singleShare)
                shareIntent.putExtra(Intent.EXTRA_STREAM, shareUris.get(0));
            else
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, shareUris);
            activity.startActivity(Intent.createChooser(shareIntent, activity.getString(R.string.common_share_to)));
        } else {
            final NpDialog dlg = new NpDialog(activity);
            dlg.setTitle(R.string.common_share_to);
            dlg.setCancelBtn(R.string.common_cancel, null,R.drawable.np_common_pressed_bottom_bg);

            GridView g = dlg.setGridAdapter(new ShareToAllAdapter(activity, shareToList));
            g.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    AppInfo appInfo = (AppInfo) shareToList.get(position);
                    if (appInfo.icon != null) {
                        dlg.dismiss();
                        if (sl != null) {
                            sl.shareTriggered();
                        }
                        shareIntent.setComponent(new ComponentName(appInfo.pkgName, appInfo.launcherClass));
                        shareIntent.setType(mimeType);
                        if (singleShare)
                            shareIntent.putExtra(Intent.EXTRA_STREAM, shareUris.get(0));
                        else
                            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, shareUris);
                        activity.startActivity(shareIntent);
                        MobclickAgent.onEvent(activity, StatConstants.EVENT_KEY_FULL_IMAGE_SHARE_OK);
                    }
                }
            });
            dlg.show();
        }
    }
}
