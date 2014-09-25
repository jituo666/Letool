
package com.xjt.newpic.activities;

import com.xjt.newpic.preference.GlobalPreference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class NpStartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (System.currentTimeMillis() - GlobalPreference.getLastGuideTime(this) > NpMediaActivity.SPLASH_INTERVAL) {
            GlobalPreference.setLastGuideTime(this, System.currentTimeMillis());
            Intent intent = new Intent();
            intent.setClass(this, NpGuideActivity.class);
            startActivity(intent);
        } else {
            GlobalPreference.setLastGuideTime(this, System.currentTimeMillis());
            Intent intent = new Intent();
            intent.setClass(this, NpMainActivity.class);
            startActivity(intent);
        }
        finish();
    }

}
