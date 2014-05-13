//
//package com.xjt.letool.settings;
//
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import com.xjt.letool.R;
//
//@SuppressWarnings("static-access")
//public class MainSettingsFragment extends DxFragment implements OnClickListener,
//        OnPrefenceChangeListener {
//    private static final int[] sResponses = {
//            R.string.antispam_response_busy,
//            R.string.antispam_response_poweroff,
//            R.string.antispam_response_stop,
//            R.string.antispam_response_empty
//    };
//
//    private LTPreference mPrefWhiteList;
//    private LTPreference mPrefBlackList;
//    private LTPreference mPrefMode;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        mMainView = inflater.inflate(R.layout.antispam_main_settings, container, false);
//        return mMainView;
//    }
//
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        initViews();
//    }
//
//    private void initValues() {
//        if (mPrefAntispamOn == null) {
//            return;
//        }
//        mPrefAntispamOn.setChecked(AntiSpamSettings.isAntiSpamOn());
//        mPrefFloatwinOn.setChecked(AntiSpamSettings.isLocationFloatwinOn());
//        mPrefReportwinOn.setChecked(AntiSpamSettings.isReportwinOn());
//        updateResponseValue(AntiSpamSettings.getResponse());
//        mPrefData.showNewTip(!TextUtils.isEmpty(AntiSpamSettings.getLableUpdateUrl()));
//    }
//
//    private void updateResponseValue(int value) {
//        mPrefResponse.setSummary(sResponses[value]);
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        initValues();
//    }
//
//    private void initViews() {
//        mPrefBlackList = (LTPreference) findViewById(R.id.pref_black_list);
//        mPrefWhiteList = (LTPreference) findViewById(R.id.pref_white_list);
//        mPrefAntispamOn = (LTPreference) findViewById(R.id.pref_antispam_on);
//        mPrefMode = (LTPreference) findViewById(R.id.pref_mode);
//
//    }
//
//    @Override
//    public void onClick(View v) {
//        if (v == mPrefWhiteList) {
//            ((AntiSpamSettingsActivity) getActivity()).loadWhiteListFragment();
//        } else if (v == mPrefSchedule) {
//            ((AntiSpamSettingsActivity) getActivity()).loadScheduleSettingsFragment();
//        } else if (v == mPrefBlackList) {
//            ((AntiSpamSettingsActivity) getActivity()).loadBlackListFragment();
//        }else if (v == mPrefMode) {
//            ((AntiSpamSettingsActivity) getActivity()).loadModeSettingsFragment();
//        }
//    }
//
//    @Override
//    public void onChange(LTPreference pref, Object value) {
//        if (pref == mPrefAntispamOn) {
//            boolean checked = (Boolean) value;
//            AntiSpamSettings.setAntiSpamOn(checked);
//        } else if (pref == mPrefFloatwinOn) {
//            boolean checked = (Boolean) value;
//            AntiSpamSettings.setLocationFloatwinOn(checked);
//        } else if (pref == mPrefReportwinOn) {
//            boolean checked = (Boolean) value;
//            AntiSpamSettings.setReportwinOn(checked);
//        }
//    }
//
//}
