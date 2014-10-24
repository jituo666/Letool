package com.xjt.newpic.activities;

import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;
import com.xjt.newpic.common.LLog;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * @Author Jituo.Xuan
 * @Date 1:38:59 PM Aug 2, 2014
 * @Comments:null
 */
public class NpMainActivity extends NpMediaActivity {

    private static final String TAG = NpMainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle b) {
        FeedbackAgent agent = new FeedbackAgent(this);
        agent.sync();
        UmengUpdateAgent.silentUpdate(this);
        super.onCreate(b);
    }

    public static String getDeviceInfo(Context context) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            String device_id = tm.getDeviceId();

            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            String mac = wifi.getConnectionInfo().getMacAddress();
            json.put("mac", mac);

            if (TextUtils.isEmpty(device_id)) {
                device_id = mac;
            }

            if (TextUtils.isEmpty(device_id)) {
                device_id = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            }

            json.put("device_id", device_id);

            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
