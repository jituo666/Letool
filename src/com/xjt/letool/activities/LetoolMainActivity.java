
package com.xjt.letool.activities;

import android.os.Bundle;

import com.umeng.analytics.AnalyticsConfig;
import com.xjt.letool.stat.StatConstants;

/**
 * @Author Jituo.Xuan
 * @Date 1:38:59 PM Aug 2, 2014
 * @Comments:null
 */
public class LetoolMainActivity extends LocalMediaActivity {

    @Override
    protected void onCreate(Bundle b) {
        AnalyticsConfig.setAppkey(StatConstants.UMENG_APP_DEBUG_KEY);
        AnalyticsConfig.setChannel(StatConstants.UMENG_TEST_CHANNEL_ID);
        super.onCreate(b);
    }

}
