package com.xjt.letool.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xjt.letool.R;

@SuppressWarnings("static-access")
public class LetoolLoadingView extends RelativeLayout {
    private TextView mMsgInsizeView;
    private TextView mMsgBelowView;

    public LetoolLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    private void initViews(Context cxt) {
        LayoutInflater inflater = LayoutInflater.from(cxt);
        inflater.inflate(R.layout.loading_inside, this, true);

        mMsgInsizeView = (TextView) findViewById(R.id.msg_inside);
        mMsgBelowView = (TextView) findViewById(R.id.msg_below);
    }

    public void updateProgress(int percent) {
        mMsgInsizeView.setText(String.valueOf(percent) + "%");
    }

    public void updateInsideMessage(CharSequence msg) {
        mMsgInsizeView.setText(msg);
    }

    public void updateInsideMessage(int resId) {
        mMsgInsizeView.setText(resId);
    }


    public void updateBelowMessage(CharSequence msg) {
        mMsgBelowView.setText(msg);
    }

    public void updateBelowMessage(int resId) {
        mMsgBelowView.setText(resId);
    }
}
