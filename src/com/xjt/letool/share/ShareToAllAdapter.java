package com.xjt.letool.share;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xjt.letool.R;
import com.xjt.letool.utils.PackageUtils.AppInfo;

import java.util.List;


public class ShareToAllAdapter extends BaseAdapter {

    final Activity mContext;
    final List<AppInfo> mShareToList;

    public ShareToAllAdapter(Activity cxt,List<AppInfo> data) {
        mShareToList = data;
        mContext = cxt;
    }

    @Override
    public int getCount() {
        return mShareToList.size();
    }

    @Override
    public Object getItem(int position) {
        return mShareToList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View v;
        if (convertView == null) {
            v = mContext.getLayoutInflater().inflate(R.layout.share_list_item, parent, false);
        } else {
            v = convertView;
        }
        if (mShareToList.get(position).icon == null) {
            v.findViewById(R.id.app_name).setVisibility(View.INVISIBLE);
            v.findViewById(R.id.app_icon).setVisibility(View.INVISIBLE);
        } else {
            TextView textView = (TextView) v.findViewById(R.id.app_name);
            textView.setText(mShareToList.get(position).label);
            ImageView imageView = (ImageView) v.findViewById(R.id.app_icon);
            imageView.setImageDrawable(mShareToList.get(position).icon);
        }
        return v;
    }
}
