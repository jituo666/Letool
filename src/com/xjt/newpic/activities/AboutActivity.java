
package com.xjt.newpic.activities;

import com.xjt.newpic.R;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class AboutActivity extends Activity implements android.view.View.OnClickListener {

    private static final String TAG = AboutActivity.class.getSimpleName();
    private View mBackNavi;
    private TextView mVersion;

    public String getVersion() {
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.app_about);
        mBackNavi = findViewById(R.id.action_navi);
        mBackNavi.setOnClickListener(this);
        mVersion = (TextView) findViewById(R.id.np_version);
        mVersion.setText(getString(R.string.about_version, getVersion()));
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_navi) {
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);

    }
}
