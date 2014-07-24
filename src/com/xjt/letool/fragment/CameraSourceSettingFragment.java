
package com.xjt.letool.fragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.xjt.letool.LetoolApp;
import com.xjt.letool.LetoolContext;
import com.xjt.letool.R;
import com.xjt.letool.common.LLog;
import com.xjt.letool.metadata.MediaSet;
import com.xjt.letool.metadata.source.LocalSimpleAlbumSet;
import com.xjt.letool.preference.GlobalPreference;
import com.xjt.letool.view.LetoolTopBar;
import com.xjt.letool.view.LetoolTopBar.OnActionModeListener;

/**
 * @Author Jituo.Xuan
 * @Date 9:47:49 AM Apr 19, 2014
 * @Comments:null
 */
public class CameraSourceSettingFragment extends Fragment implements OnActionModeListener {

    private static final String TAG = CameraSourceSettingFragment.class.getSimpleName();

    protected ImageLoader imageLoader = ImageLoader.getInstance();
    protected AbsListView listView;
    DisplayImageOptions options;
    protected boolean pauseOnScroll = false;
    protected boolean pauseOnFling = true;
    private ArrayList<MediaDir> imageUrls;
    private Button mSave;
    private String mSavePhotodirs;
    private ItemAdapter mItemAdapter;
    private LetoolContext mLetoolContext;
    private LayoutInflater mLayoutInflater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLetoolContext = (LetoolContext) this.getActivity();
        mSavePhotodirs = GlobalPreference.getPhotoDirs(getActivity());
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_launcher)
                .showImageForEmptyUri(R.drawable.ic_launcher)
                .showImageOnFail(R.drawable.ic_launcher)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(20))
                .build();
    }

    public String getVersion() {
        try {
            PackageManager manager = getActivity().getPackageManager();
            PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);
            Date now = new Date(info.lastUpdateTime);
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.CHINESE);
            return getString(R.string.version_update_check_desc, info.versionName, f.format(now));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void initBrowseActionBar() {
        LetoolTopBar topBar = mLetoolContext.getLetoolTopBar();
        topBar.setOnActionMode(LetoolTopBar.ACTION_BAR_MODE_SETTINGS, CameraSourceSettingFragment.this);
        topBar.setTitleIcon(R.drawable.ic_action_previous_item);
        topBar.setTitleText(R.string.camera_source_dirs_title);
        ViewGroup nativeButtons = (ViewGroup) topBar.getActionPanel().findViewById(R.id.navi_buttons);
        nativeButtons.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayoutInflater = inflater;
        View rootView = inflater.inflate(R.layout.camera_source_setting, container, false);
        initBrowseActionBar();
        listView = (ListView) rootView.findViewById(android.R.id.list);
        mSave = (Button) rootView.findViewById(R.id.save);
        mSave.setOnClickListener(this);
        new LoadMeidaTask().execute();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        applyScrollListener();
    }

    private void applyScrollListener() {
        listView.setOnScrollListener(new PauseOnScrollListener(imageLoader, pauseOnScroll, pauseOnFling));
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
                view = mLayoutInflater.inflate(R.layout.camera_source_setting_item, parent, false);
                holder = new ViewHolder();
                holder.textCount = (TextView) view.findViewById(R.id.text_count);
                holder.textPath = (TextView) view.findViewById(R.id.text_path);
                holder.image = (ImageView) view.findViewById(R.id.image);
                holder.checkBox = (CheckBox) view.findViewById(R.id.checked);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            MediaDir m = imageUrls.get(position);
            imageLoader.displayImage("file://" + m.dir, holder.image, options, animateFirstListener);
            File parentFile = new File(m.dir).getParentFile();
            if (parentFile == null) {
                parentFile = new File("/");
            }
            holder.textPath.setText(parentFile.toString());
            holder.textCount.setText(String.valueOf(m.mediaCount));
            holder.checkBox.setChecked(m.isChecked);
            holder.checkBox.setOnClickListener(CameraSourceSettingFragment.this);
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
            mediaCount = c;
            dir = d;
            isChecked = checked;
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
            mDataSet = new LocalSimpleAlbumSet((LetoolApp) mLetoolContext.getActivityContext().getApplicationContext(), true);
            dialog = new ProgressDialog(getActivity());
            dialog.setTitle(getString(R.string.common_recommend));
            dialog.setMessage(getString(R.string.common_loading));
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            ArrayList<MediaSet> r = mDataSet.getAllAlbums();
            for (MediaSet s : r) {
                int count = s.getAllMediaItems();
                String path = s.getCoverMediaItem().getFilePath();
                String dir = path.substring(0, path.lastIndexOf("/"));
                imageUrls.add(new MediaDir(count, path, mSavePhotodirs.contains(dir + "|")));
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
                    // startImagePagerActivity(position);
                }
            });
        }

    }

    @Override
    public void onClick(View v) {
        String result = "";
        if (v.getId() == R.id.save) {
            for (MediaDir m : imageUrls) {
                File parentFile = new File(m.dir).getParentFile();
                if (m.isChecked && parentFile != null) {
                    result += new File(m.dir).getParentFile().toString();
                    result += "|";
                }
            }
            GlobalPreference.setPhotoDirs(getActivity(), result);
            //
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        } else if (v.getId() == R.id.checked) {
            MediaDir m = imageUrls.get(((Integer) v.getTag()));
            m.isChecked = !m.isChecked;
            mItemAdapter.notifyDataSetChanged();
        } else if (v.getId() == R.id.action_navi) {
            mLetoolContext.popContentFragment();
        }
    }

}
