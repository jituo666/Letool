
package com.xjt.letool.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.xjt.letool.LetoolApp;
import com.xjt.letool.R;
import com.xjt.letool.common.LLog;
import com.xjt.letool.metadata.MediaSet;
import com.xjt.letool.metadata.source.LocalSimpleAlbumSet;
import com.xjt.letool.preference.GlobalPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CameraSourceSettingActivity extends Activity implements View.OnClickListener{

    private final static String TAG = CameraSourceSettingActivity.class.getSimpleName();

    protected ImageLoader imageLoader = ImageLoader.getInstance();
    protected AbsListView listView;
    DisplayImageOptions options;
    protected boolean pauseOnScroll = false;
    protected boolean pauseOnFling = true;
    private ArrayList<MediaDir> imageUrls;
    private Button mSave;
    private String mSavePhotodirs;
    private ItemAdapter mItemAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_source_setting);

        mSavePhotodirs = GlobalPreference.getPhotoDirs(this);
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_launcher)
                .showImageForEmptyUri(R.drawable.ic_launcher)
                .showImageOnFail(R.drawable.ic_launcher)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(20))
                .build();

        listView = (ListView) findViewById(android.R.id.list);
        mSave =  (Button) findViewById(R.id.save);
        mSave.setOnClickListener(this);
        new LoadMeidaTask().execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        applyScrollListener();
    }

    private void applyScrollListener() {
        listView.setOnScrollListener(new PauseOnScrollListener(imageLoader, pauseOnScroll, pauseOnFling));
    }

    @Override
    public void onBackPressed() {
        AnimateFirstDisplayListener.displayedImages.clear();
        super.onBackPressed();
    }

    class ItemAdapter extends BaseAdapter {

        private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

        private class ViewHolder {

            public TextView textPath;
            public TextView textCount;
            public ImageView image;
            public CheckBox checkBox;
        }

        @Override
        public int getCount() {
            return imageUrls.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder holder;
            if (convertView == null) {
                view = getLayoutInflater().inflate(R.layout.camera_source_setting_item, parent, false);
                holder = new ViewHolder();
                holder.textCount = (TextView) view.findViewById(R.id.text_count);
                holder.textPath = (TextView) view.findViewById(R.id.text_path);
                holder.image = (ImageView) view.findViewById(R.id.image);
                holder.checkBox = (CheckBox)view.findViewById(R.id.checked);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            MediaDir m = imageUrls.get(position);
            /*            MediaItem i = m.getCoverMediaItem();
            String path = i.getFilePath();
            imageLoader.displayImage("file://" + path, holder.image, options, animateFirstListener);
            String dir = path.substring(0, path.lastIndexOf("/"));*/
            imageLoader.displayImage("file://" + m.dir, holder.image, options, animateFirstListener);
            String path = m.dir;
            String dir = path.substring(0, path.lastIndexOf("/"));
            holder.textPath.setText(dir);
            holder.textCount.setText(String.valueOf(m.mediaCount));
            holder.checkBox.setChecked(m.isChecked);
            holder.checkBox.setOnClickListener(CameraSourceSettingActivity.this);
            holder.checkBox.setTag(Integer.valueOf(position));
            return view;
        }
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }

    private class MediaDir {
        public MediaDir(int c, String d, boolean checked) {
            mediaCount =c;
            dir =d;
            isChecked =checked;
        }
        int mediaCount;
        String dir;
        boolean isChecked;
        
    }
    private class LoadMeidaTask extends AsyncTask<Void, Void, Void> {

        LocalSimpleAlbumSet mDataSet;
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageUrls = new ArrayList<MediaDir>();
            mDataSet = new LocalSimpleAlbumSet((LetoolApp) getApplication(), true);
            dialog = new ProgressDialog(CameraSourceSettingActivity.this);
            dialog.setTitle(getString(R.string.common_recommend));
            dialog.setMessage(getString(R.string.common_loading));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            ArrayList<MediaSet> r = mDataSet.getAllAlbums();
            for (MediaSet s :r) {
                int count = s.getAllMediaItems();
                String path = s.getCoverMediaItem().getFilePath();
                String dir = path.substring(0, path.lastIndexOf("/"));
                imageUrls.add(new MediaDir(count,path, mSavePhotodirs.contains(dir + "|")));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            LLog.i(TAG, "---------LoadMeidaTask:" + imageUrls.size());
            mItemAdapter = new ItemAdapter();
            ((ListView) listView).setAdapter(mItemAdapter);
            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //startImagePagerActivity(position);
                }
            });
        }

    }

    @Override
    public void onClick(View v) {
        String result = "";
        if (v.getId() == R.id.save) {
            for (MediaDir m: imageUrls) {
                if (m.isChecked) {
                    String path = m.dir;
                    String dir = path.substring(0, path.lastIndexOf("/"));
                    result += dir;
                    result += "|";
                }
            }
            if (result.length() > 0)
                GlobalPreference.setPhotoDirs(this, result);
            Intent it = new Intent();
            it.setClass(this, LocalMediaActivity.class);
            startActivity(it);
            finish();
        } else if (v.getId() == R.id.checked) {
            MediaDir m = imageUrls.get(((Integer)v.getTag()));
            m.isChecked = !m.isChecked;
            mItemAdapter.notifyDataSetChanged();
        }
    }
}
