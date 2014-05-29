package com.xjt.letool.share;

import java.util.ArrayList;

import com.tencent.tauth.IUiListener;

import android.app.Activity;
import android.os.Bundle;

public interface ShareListener {

    public void onShareTo(Activity activity, int shareTo, ArrayList<String> data);
}
