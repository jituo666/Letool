
package com.xjt.letool.share;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQAuth;
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
    private QQAuth mQQAuth;
    private Tencent mTencent;
    //private QQShare mQQShare = null;
    private UserInfo mInfo;

    private int mExtarFlag = 0x00;
    private String mTargetUrl = "";

    public boolean ready(Context context) {
        if (mQQAuth == null) {
            return false;
        }
        boolean ready = mQQAuth.isSessionValid() && mQQAuth.getQQToken().getOpenId() != null;
        if (!ready)
            Toast.makeText(context, "login and get openId first, please!", Toast.LENGTH_SHORT).show();
        return ready;
    }


    public void initShare(Context context, String appId) {
        mQQAuth = QQAuth.createInstance(appId, context);
        mTencent = Tencent.createInstance(appId, context);
        mExtarFlag &= (0xFFFFFFFF - QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
        //mQQShare = new QQShare(context, mQQAuth.getQQToken());
    }

    public void commitShareToQQ(Activity activity, String title, String summary, int shareType, String uri) {
        final Bundle params = new Bundle();
        if (shareType != QQShare.SHARE_TO_QQ_TYPE_IMAGE) {
            params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, mTargetUrl);
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
        }
        if (shareType == QQShare.SHARE_TO_QQ_TYPE_IMAGE) {
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, uri.toString());
        } else {
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, uri.toString());
        }
        params.putString(shareType == QQShare.SHARE_TO_QQ_TYPE_IMAGE ? QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL
                : QQShare.SHARE_TO_QQ_IMAGE_URL, uri.toString());
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, activity.getString(R.string.app_name));
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, shareType);
        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, mExtarFlag);
        if (shareType == QQShare.SHARE_TO_QQ_TYPE_AUDIO) {
            params.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, uri.toString());
        }
        if ((mExtarFlag & QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN) != 0) {
            //showToast("在好友选择列表会自动打开分享到qzone的弹窗~~~");
        } else if ((mExtarFlag & QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE) != 0) {
            //showToast("在好友选择列表隐藏了qzone分享选项~~~");
        }
        doShareToQQ(activity, params);
    }

    /**
     * 用异步方式启动分享
     * @param params
     */
    private void doShareToQQ(final Activity activity, final Bundle params) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
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

    public void commitShareToQZone(Activity activity, String title, String summary, int shareType, ArrayList<String> imageUrls) {
        final Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, shareType);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, summary);
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, mTargetUrl);
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
        doShareToQzone(activity, params);
    }

    /**
     * 用异步方式启动分享
     * @param params
     */
    private void doShareToQzone(final Activity activity, final Bundle params) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mTencent.shareToQzone(activity, params, new IUiListener() {

                    @Override
                    public void onCancel() {
                        LLog.i(TAG, "onCancel: ");
                    }

                    @Override
                    public void onError(UiError e) {
                        // TODO Auto-generated method stub
                        LLog.i(TAG, "onError: " + e.errorMessage);
                    }

                    @Override
                    public void onComplete(Object response) {
                        // TODO Auto-generated method stub
                        LLog.i(TAG, "onComplete: " + response.toString());
                    }

                });
            }
        }).start();
    }
}
