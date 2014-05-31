
package com.xjt.letool.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.xjt.letool.R;

public class CommonDialog extends Dialog {

    private static final String TAG = CommonDialog.class.getSimpleName();

    public static final int DEFAULT_BTN_LEFT = 1;
    public static final int DEFAULT_BTN_MID = 2;
    public static final int DEFAULT_BTN_RIGHT = 3;

    TextView mTitleView;

    public CommonDialog(Context context) {
        super(context, R.style.MyDialog);
        setContentView(R.layout.common_dialog);
        mTitleView = (TextView) findViewById(R.id.title);

    }

    public void setTitle(int resId) {
        mTitleView.setText(resId);
    }

    public void setTitle(String title) {
        mTitleView.setText(title);
    }

    public ListView setListAdapter(ListAdapter adapter) {
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setVisibility(View.VISIBLE);
        listView.setAdapter(adapter);
        return listView;
    }

    public void setOkBtn(View.OnClickListener clickListener) {
        Button okBtn =(Button) findViewById(R.id.ok_btn);
        okBtn.setVisibility(View.VISIBLE);
        if (clickListener != null) {
            okBtn.setOnClickListener(new ExternalListener(clickListener));
        } else {
            okBtn.setOnClickListener(new CloseListener());
        }
    }

    public void setCancelBtn(View.OnClickListener clickListener) {
        Button cancelBtn = (Button) findViewById(R.id.cancel_btn);
        cancelBtn.setVisibility(View.VISIBLE);

        if (clickListener != null) {
            cancelBtn.setOnClickListener(new ExternalListener(clickListener));
        } else {
            cancelBtn.setOnClickListener(new CloseListener());
        }
    }

    private class CloseListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            cancel();
        }
    }

    private class ExternalListener implements View.OnClickListener {

        private View.OnClickListener mListener;

        public ExternalListener(View.OnClickListener listener) {
            mListener = listener;
        }

        @Override
        public void onClick(View v) {
            CommonDialog.this.dismiss();
            mListener.onClick(v);
        }
    }
}
