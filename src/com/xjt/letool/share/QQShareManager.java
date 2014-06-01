
package com.xjt.letool.share;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.xjt.letool.R;
import com.xjt.letool.common.LLog;

public class QQShareManager {

    private static final String TAG = QQShareManager.class.getSimpleName();
    private int shareType = QQShare.SHARE_TO_QQ_TYPE_DEFAULT;
    private Tencent mTencent;
    private int mExtarFlag = 0x00;
    private String mTargetUril = "http://blog.csdn.net/jetoo";

    public QQShareManager(Context context, String appId) {
        mTencent = Tencent.createInstance(appId, context);
    }

    public void shareImageToQQ(Activity activity, String title, String summary, String shareUri) {
        final Bundle params = new Bundle();
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, shareUri);
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, mTargetUril);
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, activity.getString(R.string.app_name));
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, mExtarFlag);
        doShareToQQ(activity, params);
    }

    /**
     * 用异步方式启动分享
     * @param params
     */
    private void doShareToQQ(Activity a, final Bundle params) {
        final Activity activity = a;
        new Thread(new Runnable() {

            @Override
            public void run() {
                mTencent.shareToQQ(activity, params, new IUiListener() {

                    @Override
                    public void onCancel() {
                        if (shareType != QQShare.SHARE_TO_QQ_TYPE_IMAGE) {
                            LLog.i(TAG, "onCancel: ");
                        }
                    }

                    @Override
                    public void onComplete(Object response) {
                        // TODO Auto-generated method stub
                        LLog.i(TAG, "onComplete: " + response.toString());
                    }

                    @Override
                    public void onError(UiError e) {
                        // TODO Auto-generated method stub
                        LLog.i(TAG, "onError: " + e.errorMessage);
                    }

                });
            }
        }).start();
    }

    public void shareImageToQZone(Activity activity, String title, String summary, ArrayList<String> imageUrls) {
        final Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, summary);
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, mTargetUril);
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
        doShareToQzone(activity, params);
    }

    /**
     * 用异步方式启动分享
     * @param params
     */
    private void doShareToQzone(final Activity a, final Bundle params) {
        final Activity activity = a;
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mTencent.shareToQzone(activity, params, new IUiListener() {

                    @Override
                    public void onCancel() {
                        if (shareType != QQShare.SHARE_TO_QQ_TYPE_IMAGE) {
                            LLog.i(TAG, "onCancel: ");
                        }
                    }

                    @Override
                    public void onComplete(Object response) {
                        // TODO Auto-generated method stub
                        LLog.i(TAG, "onComplete: " + response.toString());
                    }

                    @Override
                    public void onError(UiError e) {
                        // TODO Auto-generated method stub
                        LLog.i(TAG, "onError: " + e.errorMessage);
                    }

                });
            }
        }).start();
    }
}
