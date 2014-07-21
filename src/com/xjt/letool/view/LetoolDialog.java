package com.xjt.letool.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.xjt.letool.R;

public class LetoolDialog extends Dialog {

    private static final String TAG = LetoolDialog.class.getSimpleName();

    public static final int DEFAULT_BTN_LEFT = 1;
    public static final int DEFAULT_BTN_MID = 2;
    public static final int DEFAULT_BTN_RIGHT = 3;

    TextView mTitleView;
    TextView mMessage;
    View mButtonPanel;

    public LetoolDialog(Context context) {
        super(context, R.style.MyDialog);
        setContentView(R.layout.common_dialog);
        mTitleView = (TextView) findViewById(R.id.title);
        mButtonPanel = findViewById(R.id.btn_panel);
        setCanceledOnTouchOutside(false);

    }

    public void setTitle(String title) {
        mTitleView.setText(title);
    }

    public void setTitle(int titleResId) {
        mTitleView.setText(titleResId);
    }

    public void setMessage(int msgResId) {
        mMessage = (TextView) findViewById(R.id.message);
        mMessage.setText(msgResId);
        mMessage.setVisibility(View.VISIBLE);
    }

    public void setMessage(String message) {
        mMessage = (TextView) findViewById(R.id.message);
        mMessage.setText(message);
        mMessage.setVisibility(View.VISIBLE);
    }

    private void setDividerVisible(boolean visible) {
        View v = findViewById(R.id.btn_divider);
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public ListView setListAdapter(ListAdapter adapter) {
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setVisibility(View.VISIBLE);
        listView.setAdapter(adapter);
        return listView;
    }

    public void setOkBtn(int title, View.OnClickListener clickListener) {
        mButtonPanel.setVisibility(View.VISIBLE);
        TextView okBtn = (TextView) findViewById(R.id.ok_btn);
        okBtn.setVisibility(View.VISIBLE);
        if (clickListener != null) {
            okBtn.setOnClickListener(new ExternalListener(clickListener));
        } else {
            okBtn.setOnClickListener(new CloseListener());
        }
        okBtn.setText(title);
    }

    public void setCancelBtn(int title, View.OnClickListener clickListener) {
        setDividerVisible(true);
        mButtonPanel.setVisibility(View.VISIBLE);
        TextView cancelBtn = (TextView) findViewById(R.id.cancel_btn);
        cancelBtn.setVisibility(View.VISIBLE);
        cancelBtn.setText(title);
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
            dismiss();
            mListener.onClick(v);
        }
    }
}