
package com.xjt.newpic.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xjt.newpic.R;

/**
 * @Author Jituo.Xuan
 * @Date 8:17:56 PM Jul 24, 2014
 * @Comments:null
 */
public class NpEmptyView extends RelativeLayout {

    private ImageView mImageView;
    private TextView mMsgBelowView;

    public NpEmptyView(Context context) {
        super(context);
    }

    public NpEmptyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImageView = (ImageView) findViewById(R.id.empty_image);
        mMsgBelowView = (TextView) findViewById(R.id.empty_message);
    }

    public void updataView(int imageResId, int msgResId) {
        mImageView.setImageResource(imageResId);
        mMsgBelowView.setText(msgResId);
    }

    public void updataView(Drawable d, CharSequence c) {
        mImageView.setImageDrawable(d);
        mMsgBelowView.setText(c);
    }
}
