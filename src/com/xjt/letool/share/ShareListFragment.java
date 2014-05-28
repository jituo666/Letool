
package com.xjt.letool.share;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.xjt.letool.R;
import com.xjt.letool.share.ShareManager.ShareTo;

import java.util.List;

public class ShareListFragment extends DialogFragment {

    private static final String TAG = ShareListFragment.class.getSimpleName();
    public static final String SHARE_MEDIA_PATH = "share_media_path";
    private ShareManager mShareManager;
    private ListView mMenusList;
    private List<ShareTo> mData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.share_list, container, false);
        mMenusList = (ListView) rootView.findViewById(R.id.share_to_list);
        mShareManager = new ShareManager(getActivity());
        mData = mShareManager.getShareToList();
        mMenusList.setAdapter(new ShareAdapter());
        mMenusList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int postion, long id) {
                mShareManager.onShareTo(getActivity(), mData.get(postion).shareToType, getArguments());
                ShareListFragment.this.dismiss();
            }

        });
        return rootView;
    }

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
                v = getActivity().getLayoutInflater().inflate(R.layout.share_list_item, parent, false);
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
