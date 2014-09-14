
package com.xjt.newpic.activities;

import android.os.Bundle;

import com.umeng.analytics.AnalyticsConfig;
import com.xjt.newpic.stat.StatConstants;

/**
 * @Author Jituo.Xuan
 * @Date 1:38:59 PM Aug 2, 2014
 * @Comments:null
 */
public class NpMainActivity extends NpMediaActivity {

    private static final String TAG = NpMainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle b) {

        AnalyticsConfig.setAppkey(StatConstants.UMENG_APP_DEBUG_KEY);
        AnalyticsConfig.setChannel(StatConstants.UMENG_TEST_CHANNEL_ID);

        //        AnalyticsConfig.setAppkey(StatConstants.UMENG_APP_RELEASE_KEY);
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_ID_WDJ); // "LETOOL0000001000"; //豌豆夹
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_91); // "LETOOL0000001001"; //91手机助手
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_TENCENT); // "LETOOL0000001002";//腾讯应用宝
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_BAIDU); // "LETOOL0000001003";//百度
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_360); // "LETOOL0000001004";//360手机助手
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_ALI); // "LETOOL0000001005";//阿里淘宝
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_ANZHI); // "LETOOL0000001006";//安智
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_MUMAYI); // "LETOOL0000001007";//木蚂蚁
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_ANZHUO); // "LETOOL0000001008";//安卓
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_XIAOMI); // "LETOOL0000001009";//小米
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_JIFENG);// "LETOOL0000001010";//机峰
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_YINGYONGHUI); // "LETOOL0000001011";//应用汇
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_NDUO); // "LETOOL0000001012";//N多
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_EOE); // "LETOOL0000001013";//EOE
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_UC); // "LETOOL0000001014"; //UC
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_DIANXIN); // "LETOOL0000001015";//电信
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_SUNSUNG); // "LETOOL0000001017";//三星
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_LENOVO); // "LETOOL0000001018";//联想
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_HUAWEI); // "LETOOL0000001019";//华为
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_ZHONGXIN); // "LETOOL0000001020"; //中兴
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_YIDONGMM); // "LETOOL0000001021";//移动MM
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_WOSHOP); // "LETOOL0000001022";//联通沃商店
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_NOKIA); // "LETOOL0000001023";// 诺基亚
        //        AnalyticsConfig.setChannel(StatConstants.UMENG_CHANNEL_OTHERS); // "LETOOL0000001111";//其它
        super.onCreate(b);
    }

}
