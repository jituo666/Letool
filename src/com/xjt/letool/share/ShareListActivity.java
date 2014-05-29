package com.xjt.letool.share;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;
import com.xjt.letool.R;
import com.xjt.letool.share.ShareManager.ShareTo;

import java.util.List;

public class ShareListActivity extends Activity {

    private static final String TAG = ShareListActivity.class.getSimpleName();
    public static final String SHARE_MEDIA_PATH_LIST = "share_media_path_list";
    private ShareManager mShareManager;
    private ListView mMenusList;
    private List<ShareTo> mData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_list);
        mMenusList = (ListView)findViewById(R.id.share_to_list);
        mShareManager = new ShareManager(this);
        mData = mShareManager.getShareToList();
        mMenusList.setAdapter(new ShareAdapter());
        mMenusList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int postion, long id) {
                mShareManager.onShareTo(ShareListActivity.this, mData.get(postion).shareToType, 
                        getIntent().getStringArrayListExtra((SHARE_MEDIA_PATH_LIST)),l);
                //finish();
            }

        });
    }

    Handler h =  new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    finish();
                }
            }
            super.handleMessage(msg);
        }
      
    };
    
    IUiListener l =  new IUiListener() {

        @Override
        public void onCancel() {
          h.obtainMessage(1).sendToTarget();

        }

        @Override
        public void onError(UiError e) {
            h.obtainMessage(1).sendToTarget();
        }

        @Override
        public void onComplete(Object response) {
            h.obtainMessage(1).sendToTarget();
        }

    };
    private class ShareAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View v;
            if (convertView == null) {
                v = getLayoutInflater().inflate(R.layout.share_list_item, parent, false);
            } else {
                v = convertView;
            }
            TextView textView = (TextView) v.findViewById(R.id.app_name);
            textView.setText(mData.get(position).shareToTitle);
            ImageView imageView = (ImageView) v.findViewById(R.id.app_icon);
            imageView.setImageDrawable(mData.get(position).shareToIcon);
            return v;
        }
    }

}
