
package com.xjt.newpic.views;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.xjt.newpic.R;

/**
 * @Author Jituo.Xuan
 * @Date 8:17:51 PM Jul 24, 2014
 * @Comments:null
 */
public class NpDialog extends Dialog {

    private static final String TAG = NpDialog.class.getSimpleName();

    public static final int DEFAULT_BTN_LEFT = 1;
    public static final int DEFAULT_BTN_MID = 2;
    public static final int DEFAULT_BTN_RIGHT = 3;

    TextView mTitleView;
    TextView mMessage;
    View mButtonPanel;

    public NpDialog(Context context) {
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

    public void setMessage(CharSequence message) {
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

    public GridView setGridAdapter(ListAdapter adapter) {
        GridView gridView = (GridView) findViewById(R.id.grid);
        gridView.setVisibility(View.VISIBLE);
        gridView.setAdapter(adapter);
        return gridView;
    }

    public void setOkBtn(int title, final View.OnClickListener clickListener, int background) {
        mButtonPanel.setVisibility(View.VISIBLE);
        final Button okBtn = (Button) findViewById(R.id.ok_btn);
        okBtn.setVisibility(View.VISIBLE);
        okBtn.setText(title);
        okBtn.setBackgroundResource(background);
        okBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onClick(okBtn);
                }
                dismiss();
            }
        });
    }

    public void setCancelBtn(int title, final View.OnClickListener clickListener, int background) {

        if (findViewById(R.id.ok_btn).getVisibility() == View.VISIBLE) {
            setDividerVisible(true);
        }
        mButtonPanel.setVisibility(View.VISIBLE);
        final Button cancelBtn = (Button) findViewById(R.id.cancel_btn);
        cancelBtn.setVisibility(View.VISIBLE);
        cancelBtn.setText(title);
        cancelBtn.setBackgroundResource(background);
        cancelBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onClick(cancelBtn);
                }
                dismiss();
            }
        });
    }

    @Override
    public void show() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        LayoutParams p = getWindow().getAttributes();
        p.width = (int) (metrics.widthPixels * 0.85);
        getWindow().setAttributes(p);
        getWindow().setGravity(Gravity.CENTER);
        super.show();
    }

}
