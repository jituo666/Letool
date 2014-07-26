
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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

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
import com.xjt.letool.utils.StorageUtils;
import com.xjt.letool.view.LetoolLoadingView;
import com.xjt.letool.view.LetoolEmptyView;
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
    protected ListView mListView;
    DisplayImageOptions options;
    protected boolean pauseOnScroll = false;
    protected boolean pauseOnFling = true;
    private ArrayList<MediaDir> mMediaDirList = new ArrayList<MediaDir>();;
    private Button mSave;
    private String mSavePhotodirs;
    private ItemAdapter mItemAdapter;
    private LetoolContext mLetoolContext;
    private LayoutInflater mLayoutInflater;
    private LetoolLoadingView mLoadingPanel;
    private LetoolEmptyView mEmptyView;
    private boolean mHasSdCard = true;

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
                .displayer(new RoundedBitmapDisplayer(2))
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
        final View rootView = inflater.inflate(R.layout.camera_source_setting, container, false);
        initBrowseActionBar();
        mListView = (ListView) rootView.findViewById(R.id.camera_source_list);
        mItemAdapter = new ItemAdapter();
        mListView.setAdapter(mItemAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File parentFile = new File(mItemAdapter.getItem(position).filePath).getParentFile();
                if (parentFile == null) {
                    parentFile = new File("/");
                }
                Toast t = Toast.makeText(getActivity(), parentFile.toString(), Toast.LENGTH_LONG);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
            }
        });
        AnimationSet set = new AnimationSet(true);
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(50);
        set.addAnimation(animation);
        animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
                );
        animation.setDuration(60);
        set.addAnimation(animation);
        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
        mListView.setLayoutAnimation(controller);
        mListView.setLayoutAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationEnd(Animation a) {
                View v = rootView.findViewById(R.id.btn_panel);
                v.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_bottom_in));
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation a) {
            }

            @Override
            public void onAnimationStart(Animation a) {
            }
        });

        mLoadingPanel = (LetoolLoadingView) rootView.findViewById(R.id.loading);
        mSave = (Button) rootView.findViewById(R.id.save);
        mSave.setOnClickListener(this);
        mHasSdCard = StorageUtils.externalStorageAvailable();
        if (!mHasSdCard) {
            mEmptyView = (LetoolEmptyView) rootView.findViewById(R.id.empty_view);
            mEmptyView.updataView(R.drawable.ic_launcher, R.string.common_error_nosdcard);
            mEmptyView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
            return rootView;
        }
        new LoadMeidaTask().execute();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        applyScrollListener();
    }

    private void applyScrollListener() {
        mListView.setOnScrollListener(new PauseOnScrollListener(imageLoader, pauseOnScroll, pauseOnFling));
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
            return mMediaDirList.size();
        }

        @Override
        public MediaDir getItem(int position) {
            return mMediaDirList.get(position);
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

            MediaDir m = mMediaDirList.get(position);
            imageLoader.displayImage("file://" + m.filePath, holder.image, options, animateFirstListener);
            File parentFile = new File(m.filePath).getParentFile();
            if (parentFile == null) {
                parentFile = new File("/");
            }
            holder.textPath.setText(parentFile.toString());
            String format = getResources().getQuantityString(R.plurals.number_of_items, m.mediaCount);
            holder.textCount.setText(String.format(format, m.mediaCount));
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

        public MediaDir(int c, String p, boolean checked) {
            mediaCount = c;
            filePath = p;
            isChecked = checked;
        }

        int mediaCount;
        String filePath;
        boolean isChecked;

    }

    private class LoadMeidaTask extends AsyncTask<Void, Void, Void> {

        LocalSimpleAlbumSet mDataSet;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mMediaDirList = new ArrayList<MediaDir>();
            mDataSet = new LocalSimpleAlbumSet((LetoolApp) mLetoolContext.getActivityContext().getApplicationContext(), true);
            mLoadingPanel.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            ArrayList<MediaSet> r = mDataSet.getAllAlbums();
            for (MediaSet s : r) {
                int count = s.getAllMediaItems();
                String path = s.getCoverMediaItem().getFilePath();
                String filePath = path.substring(0, path.lastIndexOf("/"));
                mMediaDirList.add(new MediaDir(count, path, mSavePhotodirs.contains(filePath + "|")));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            if (mMediaDirList.size() == 0) {
                mEmptyView.updataView(R.drawable.ic_launcher, R.string.common_error_no_video);
                mEmptyView.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
                return;
            }
            mLoadingPanel.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            mItemAdapter.notifyDataSetChanged();
            LLog.i(TAG, "---------LoadMeidaTask:" + mMediaDirList.size());
        }

    }

    @Override
    public void onClick(View v) {
        String result = "";
        if (v.getId() == R.id.save) {
            if (!mHasSdCard)
                return;
            for (MediaDir m : mMediaDirList) {
                File parentFile = new File(m.filePath).getParentFile();
                if (m.isChecked && parentFile != null) {
                    result += new File(m.filePath).getParentFile().toString();
                    result += "|";
                }
            }
            GlobalPreference.setPhotoDirs(getActivity(), result);
            //
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        } else if (v.getId() == R.id.checked) {
            MediaDir m = mMediaDirList.get(((Integer) v.getTag()));
            m.isChecked = !m.isChecked;
            mItemAdapter.notifyDataSetChanged();
        } else if (v.getId() == R.id.action_navi) {
            mLetoolContext.popContentFragment();
        }
    }

}
