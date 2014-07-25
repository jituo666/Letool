
package com.xjt.letool.fragment;

import com.xjt.letool.LetoolApp;
import com.xjt.letool.LetoolContext;
import com.xjt.letool.R;
import com.xjt.letool.activities.LocalMediaActivity;
import com.xjt.letool.activities.MoviePlayActivity;
import com.xjt.letool.common.EyePosition;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.SynchronizedHandler;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.metadata.MediaDetails;
import com.xjt.letool.metadata.MediaItem;
import com.xjt.letool.metadata.MediaPath;
import com.xjt.letool.metadata.MediaSet;
import com.xjt.letool.metadata.MediaSetUtils;
import com.xjt.letool.metadata.loader.DataLoadingListener;
import com.xjt.letool.metadata.loader.ThumbnailDataLoader;
import com.xjt.letool.metadata.source.LocalAlbum;
import com.xjt.letool.utils.LetoolUtils;
import com.xjt.letool.utils.RelativePosition;
import com.xjt.letool.utils.StorageUtils;
import com.xjt.letool.utils.Utils;
import com.xjt.letool.view.SingleDeleteMediaListener.SingleDeleteMediaProgressListener;
import com.xjt.letool.view.DetailsHelper.CloseListener;
import com.xjt.letool.view.DetailsHelper.DetailsSource;
import com.xjt.letool.view.DetailsHelper;
import com.xjt.letool.view.GLBaseView;
import com.xjt.letool.view.GLController;
import com.xjt.letool.view.LetoolBottomBar;
import com.xjt.letool.view.LetoolTopBar;
import com.xjt.letool.view.LetoolDialog;
import com.xjt.letool.view.SingleDeleteMediaListener;
import com.xjt.letool.view.LetoolTopBar.OnActionModeListener;
import com.xjt.letool.view.ThumbnailView;
import com.xjt.letool.views.layout.ThumbnailContractLayout;
import com.xjt.letool.views.layout.ThumbnailLayout;
import com.xjt.letool.views.layout.ThumbnailLayout.LayoutListener;
import com.xjt.letool.views.opengl.FadeTexture;
import com.xjt.letool.views.opengl.GLESCanvas;
import com.xjt.letool.views.render.ThumbnailVideoRenderer;
import com.xjt.letool.views.utils.ActionItem;
import com.xjt.letool.views.utils.ViewConfigs;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @Author Jituo.Xuan
 * @Date 9:48:35 AM Apr 19, 2014
 * @Comments:null
 */
public class VideoFragment extends Fragment implements EyePosition.EyePositionListener, OnActionModeListener,
        LayoutListener {

    private static final String TAG = VideoFragment.class.getSimpleName();

    private static final int BIT_LOADING_RELOAD = 1;
    private static final int MSG_LAYOUT_CONFIRMED = 0;
    private static final int MSG_PICK_PHOTO = 1;

    private static final int POP_UP_MENU_ITEM_DETAIL = 0;
    private static final int POP_UP_MENU_ITEM_DELETE = 1;

    private LetoolContext mLetoolContext;

    // photo data
    private MediaPath mVideoDataPath;
    private MediaSet mVideoData;
    private ThumbnailDataLoader mVideoDataLoader;
    private int mLoadingBits = 0;

    // views
    private GLController mGLController;
    private ViewConfigs.VideoPage mConfig;
    private ThumbnailView mThumbnailView;
    private ThumbnailVideoRenderer mRender;
    private RelativePosition mOpenCenter = new RelativePosition();
    private boolean mIsActive = false;

    private String mAlbumTitle;
    private boolean mIsCameraSource = false;
    private boolean mIsSDCardMountedCorreclty = false;
    private boolean mHasDefaultDCIMDirectory = false;
    private SynchronizedHandler mHandler;
    private EyePosition mEyePosition; // The eyes' position of the user, the origin is at the center of the device and the unit is in pixels.
    private float mUserDistance; // in pixel
    private float mX;
    private float mY;
    private float mZ;

    private boolean mShowDetails;
    private DetailsHelper mDetailsHelper;
    private int mLongPressedIndex = 0;

    private final GLBaseView mRootPane = new GLBaseView() {

        private final float mMatrix[] = new float[16];

        @Override
        protected void onLayout(boolean changed, int left, int top, int right,
                int bottom) {
            mEyePosition.resetPosition();
            LetoolTopBar actionBar = mLetoolContext.getLetoolTopBar();
            int thumbnailViewLeft = left + mConfig.paddingLeft;
            int thumbnailViewRight = right - left - mConfig.paddingRight;
            int thumbnailViewTop = top + mConfig.paddingTop + actionBar.getHeight();
            int thumbnailViewBottom = bottom - top - mConfig.paddingBottom;
            mRender.setHighlightItemPath(null);
            // Set the mThumbnailView as a reference point to the open animation
            mOpenCenter.setReferencePosition(0, thumbnailViewTop);
            mThumbnailView.layout(thumbnailViewLeft, thumbnailViewTop, thumbnailViewRight, thumbnailViewBottom);
            if (mShowDetails) {
                mDetailsHelper.layout(thumbnailViewLeft, thumbnailViewTop, thumbnailViewRight, thumbnailViewBottom);
            }
            LetoolUtils.setViewPointMatrix(mMatrix, (right - left) / 2, (bottom - top) / 2, -mUserDistance);
        }

        @Override
        protected void render(GLESCanvas canvas) {
            canvas.save(GLESCanvas.SAVE_FLAG_MATRIX);
            LetoolUtils.setViewPointMatrix(mMatrix, getWidth() / 2 + mX, getHeight() / 2 + mY, mZ);
            canvas.multiplyMatrix(mMatrix, 0);
            super.render(canvas);
            canvas.restore();
        }
    };

    private class MetaDataLoadingListener implements DataLoadingListener {

        @Override
        public void onLoadingStarted() {
            setLoadingBit(BIT_LOADING_RELOAD);
        }

        @Override
        public void onLoadingFinished(boolean loadFailed) {
            clearLoadingBit(BIT_LOADING_RELOAD);
        }
    }

    private void setLoadingBit(int loadTaskBit) {
        mLoadingBits |= loadTaskBit;
    }

    private void clearLoadingBit(int loadTaskBit) {
        mLoadingBits &= ~loadTaskBit;
        if (mLoadingBits == 0 && mIsActive) {
            if (mVideoDataLoader.size() == 0) {
                mLetoolContext.showEmptyView(mIsCameraSource ?R.string.common_error_no_movie:R.string.common_error_no_video);
            } else {
                mLetoolContext.hideEmptyView();
            }
        }
    }

    @Override
    public void onLayoutFinshed(int count) {
        mHandler.obtainMessage(MSG_LAYOUT_CONFIRMED, count, 0).sendToTarget();
    }

    private void onDown(int index) {
        mRender.setPressedIndex(index);
    }

    private void onUp(boolean followedByLongPress) {
        if (followedByLongPress) {
            mRender.setPressedIndex(-1); // Avoid showing press-up animations for long-press.
        } else {
            mRender.setPressedUp();
        }
    }

    private void onSingleTapUp(int videoIndex) {
        if (!mIsActive)
            return;


            mRender.setPressedIndex(videoIndex);
            mRender.setPressedUp();
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PICK_PHOTO, videoIndex, 0), FadeTexture.DURATION);

    }

    public void onLongTap(int videoIndex) {
        MediaItem item = mVideoDataLoader.get(videoIndex);
        if (item == null)
            return;
        mLongPressedIndex = videoIndex;
        List<ActionItem> items = new ArrayList<ActionItem>();
        addMenuItem(items, POP_UP_MENU_ITEM_DETAIL, R.string.common_detail);
        addMenuItem(items, POP_UP_MENU_ITEM_DELETE, R.string.common_delete);
        final LetoolDialog dlg = new LetoolDialog(getActivity());
        dlg.setTitle(item.getName());
        ListView listView = dlg.setListAdapter(new MenuItemAdapter(getActivity(), items));
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (position == POP_UP_MENU_ITEM_DETAIL) {
                    showDetails();
                } else if (position == POP_UP_MENU_ITEM_DELETE) {
                    delete();
                }
                dlg.dismiss();
            }
        });
        dlg.setCanceledOnTouchOutside(true);
        dlg.show();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LLog.i(TAG, "onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LLog.i(TAG, "onCreate");
        mLetoolContext = (LetoolContext) getActivity();
        mGLController = mLetoolContext.getGLController();

        initializeData();
        initializeViews();
        mHandler = new SynchronizedHandler(mGLController) {

            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_LAYOUT_CONFIRMED: {
                        // mLoadingInsie.setVisibility(View.GONE);
                        break;
                    }
                    case MSG_PICK_PHOTO: {
                        playVideo(message.arg1);
                        break;
                    }
                    default:
                        throw new AssertionError(message.what);
                }
            }
        };
        mEyePosition = new EyePosition(mLetoolContext.getActivityContext(), this);
    }

    private void initializeData() {
        Bundle data = getArguments();
        mIsCameraSource = data.getBoolean(LocalMediaActivity.KEY_IS_CAMERA_SOURCE);
        if (mIsCameraSource) {
            if (MediaSetUtils.getBucketsIds().length > 0) {
                mAlbumTitle = getString(R.string.common_photo);
                mVideoDataPath = new MediaPath(data.getString(LocalMediaActivity.KEY_MEDIA_PATH), MediaSetUtils.getBucketsIds()[0]);
                mVideoData = new LocalAlbum(mVideoDataPath, (LetoolApp) getActivity().getApplication(), MediaSetUtils.getBucketsIds(),
                        mLetoolContext.isImageBrwosing(),
                        getString(R.string.common_photo));
                mHasDefaultDCIMDirectory = true;
            } else {
                mHasDefaultDCIMDirectory = false;
                return;
            }
        } else {
            mAlbumTitle = data.getString(LocalMediaActivity.KEY_ALBUM_TITLE);
            mVideoDataPath = new MediaPath(data.getString(LocalMediaActivity.KEY_MEDIA_PATH), data.getInt(LocalMediaActivity.KEY_ALBUM_ID));
            mVideoData = mLetoolContext.getDataManager().getMediaSet(mVideoDataPath);
            if (mVideoData == null) {
                Utils.fail("MediaSet is null. Path = %s", mVideoDataPath);
            }
        }
        mVideoDataLoader = new ThumbnailDataLoader(mLetoolContext, mVideoData);
        mVideoDataLoader.setLoadingListener(new MetaDataLoadingListener());
    }

    private void initializeViews() {
        mConfig = ViewConfigs.VideoPage.get(mLetoolContext.getActivityContext());
        ThumbnailLayout layout;
        layout = new ThumbnailContractLayout(mConfig.videoSpec);
        mThumbnailView = new ThumbnailView(mLetoolContext, layout);
        mThumbnailView.setBackgroundColor(LetoolUtils.intColorToFloatARGBArray(getResources().getColor(R.color.default_background_thumbnail)));
        mThumbnailView.setListener(new ThumbnailView.SimpleListener() {

            @Override
            public void onDown(int index) {
                VideoFragment.this.onDown(index);
            }

            @Override
            public void onUp(boolean followedByLongPress) {
                VideoFragment.this.onUp(followedByLongPress);
            }

            @Override
            public void onSingleTapUp(int videoIndex) {
                VideoFragment.this.onSingleTapUp(videoIndex);
            }

            @Override
            public void onLongTap(int videoIndex) {
                VideoFragment.this.onLongTap(videoIndex);
            }
        });
        mRender = new ThumbnailVideoRenderer(mLetoolContext, mThumbnailView);
        layout.setRenderer(mRender);
        layout.setLayoutListener(this);
        mThumbnailView.setThumbnailRenderer(mRender);
        mRender.setModel(mVideoDataLoader);
        mRootPane.addComponent(mThumbnailView);
    }

    private void initBars() {
        LetoolTopBar topBar = mLetoolContext.getLetoolTopBar();
        topBar.setOnActionMode(LetoolTopBar.ACTION_BAR_MODE_BROWSE, this);
        topBar.setVisible(View.VISIBLE, false);
        ViewGroup nativeButtons = (ViewGroup) topBar.getActionPanel().findViewById(R.id.navi_buttons);
        if (mIsCameraSource) {
            topBar.setTitleText(R.string.app_name);
            nativeButtons.setVisibility(View.VISIBLE);
            topBar.setTitleIcon(R.drawable.ic_drawer);
            TextView naviToPhoto = (TextView) nativeButtons.findViewById(R.id.navi_to_photo);
            naviToPhoto.setText(R.string.common_video);
            naviToPhoto.setEnabled(false);
            TextView naviToGallery = (TextView) nativeButtons.findViewById(R.id.navi_to_gallery);
            naviToGallery.setText(R.string.common_movies);
            naviToGallery.setEnabled(true);
            naviToGallery.setOnClickListener(this);
        } else {
            topBar.setTitleText(mAlbumTitle);
            nativeButtons.setVisibility(View.GONE);
            topBar.setTitleIcon(R.drawable.ic_action_previous_item);
        }
        LetoolBottomBar bottomBar = mLetoolContext.getLetoolBottomBar();
        bottomBar.setVisible(View.GONE, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LLog.i(TAG, "onCreateView" + System.currentTimeMillis());
        initBars();
        mIsSDCardMountedCorreclty = StorageUtils.externalStorageAvailable();
        mHasDefaultDCIMDirectory = MediaSetUtils.getBucketsIds().length > 0;
        if (!mIsSDCardMountedCorreclty) {
            mLetoolContext.showEmptyView(R.string.common_error_nosdcard);
        } else if (mIsCameraSource && !mHasDefaultDCIMDirectory) {
            mLetoolContext.showEmptyView(R.string.common_error_nodcim_video);
        } else {
            mLetoolContext.hideEmptyView();
        }
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        LLog.i(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mIsSDCardMountedCorreclty || mIsCameraSource && !mHasDefaultDCIMDirectory) {
            return;
        }
        LLog.i(TAG, "onResume" + System.currentTimeMillis());

        mGLController.setContentPane(mRootPane);
        mGLController.onResume();
        mGLController.lockRenderThread();
        try {
            mIsActive = true;
            // Set the reload bit here to prevent it exit this page in clearLoadingBit().
            setLoadingBit(BIT_LOADING_RELOAD);
            mVideoDataLoader.resume();
            mRender.resume();
            mRender.setPressedIndex(-1);
            mEyePosition.resume();

        } finally {
            mGLController.unlockRenderThread();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LLog.i(TAG, "onPause");
        mGLController.onPause();
        if (!mIsSDCardMountedCorreclty || mIsCameraSource && !mHasDefaultDCIMDirectory) {
            return;
        }
        mGLController.lockRenderThread();
        try {
            mIsActive = false;
            mVideoDataLoader.pause();
            mRender.pause();
            if (mShowDetails)
                hideDetails();
            DetailsHelper.pause();
            mEyePosition.pause();

        } finally {
            mGLController.unlockRenderThread();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        LLog.i(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LLog.i(TAG, "onDestroyView");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LLog.i(TAG, "onDetach");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mVideoData != null) {
            mVideoData.closeCursor();
        }
        LLog.i(TAG, "onDestroy");
    }

    @Override
    public void onEyePositionChanged(float x, float y, float z) {
        mRootPane.lockRendering();
        mX = x;
        mY = y;
        mZ = z;
        mRootPane.unlockRendering();
        mRootPane.invalidate();
    }

    @Override
    public void onClick(View v) {
        if (!mIsSDCardMountedCorreclty)
            return;
        if (v.getId() == R.id.action_navi) {
            if (mIsCameraSource) {
                mLetoolContext.getLetoolSlidingMenu().toggle();
            } else {
                mLetoolContext.popContentFragment();
            }
        } else if (v.getId() == R.id.navi_to_gallery) {
            GalleryFragment f = new GalleryFragment();
            Bundle data = new Bundle();
            data.putString(LocalMediaActivity.KEY_MEDIA_PATH, mLetoolContext.getDataManager()
                    .getTopSetPath(mLetoolContext.isImageBrwosing() ? DataManager.INCLUDE_LOCAL_IMAGE_SET_ONLY : DataManager.INCLUDE_LOCAL_VIDEO_SET_ONLY));
            f.setArguments(data);
            mLetoolContext.pushContentFragment(f, this, false);
        }
    }

    private void playVideo(int videoIndex) {
        MediaItem item = mVideoDataLoader.get(videoIndex);
        if (item == null)
            return;
        Context c = mLetoolContext.getActivityContext();
        try {
            Intent intent = new Intent();
            intent.setClass(c, MoviePlayActivity.class);
            intent.setDataAndType(Uri.parse(item.getFilePath()), "video/*");
            intent.putExtra(Intent.EXTRA_TITLE, "");
            c.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(c, c.getString(R.string.app_name), Toast.LENGTH_SHORT).show();
        }
    }

    // -----------------------------------------------details-----------------------------------------------------------------------

    
    private void delete() {
        MediaItem item = mVideoDataLoader.get(mLongPressedIndex);
        SingleDeleteMediaListener cdl = new SingleDeleteMediaListener(getActivity(), item.getPath(), mLetoolContext.getDataManager(),
                new SingleDeleteMediaProgressListener() {
                    @Override
                    public void onConfirmDialogDismissed(boolean confirmed) {
                        if (confirmed) {
//                            mTotalCount = mMediaSet.getMediaCount();
//                            if (mTotalCount > 0) {
//                                updateActionBarMessage(getString(R.string.full_image_browse, Math.min(mLongPressedIndex + 1,mTotalCount),mTotalCount));
//                            } else {
//                                // not medias
//                                Toast.makeText(getActivity(),
//                                        R.string.full_image_browse_empty,
//                                        Toast.LENGTH_SHORT).show();
//                                getActivity().finish();
//                                return;
//                            }
                        }
                    }

                });

        final LetoolDialog dlg = new LetoolDialog(getActivity());
        dlg.setTitle(R.string.common_recommend);
        dlg.setOkBtn(R.string.common_ok, cdl);
        dlg.setCancelBtn(R.string.common_cancel, cdl);
        dlg.setMessage(R.string.common_delete_cur_tip);
        dlg.show();
    }
    private class MyDetailsSource implements DetailsSource {

        public MyDetailsSource( ) {
        }
        @Override
        public MediaDetails getDetails() {
            MediaItem item = mVideoDataLoader.get(mLongPressedIndex);
                return item.getDetails();
        }

        @Override
        public int size() {
            return mVideoData != null ? mVideoData.getAllMediaItems() : 1;
        }

        @Override
        public int setIndex() {
            return mLongPressedIndex;
        }
    }

    private void showDetails( ) {
        mShowDetails = true;
        if (mDetailsHelper == null) {
            mDetailsHelper = new DetailsHelper(mLetoolContext, mRootPane,new MyDetailsSource());
            mDetailsHelper.setCloseListener(new CloseListener() {
                @Override
                public void onClose() {
                    hideDetails();
                }
            });
        }
        mDetailsHelper.show();
    }

    private void hideDetails() {
        mShowDetails = false;
        mDetailsHelper.hide();
    }

    public void addMenuItem(List<ActionItem> items, int itemId, int titleRes) {
        ActionItem item = new ActionItem();
        item.setItemId(itemId);
        item.setTitle(getString(titleRes));
        items.add(item);
    }

    static class ViewHolder {
        TextView title;
    }

    private class MenuItemAdapter extends ArrayAdapter<ActionItem> {

        public MenuItemAdapter(Context context, List<ActionItem> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.common_action_item, null);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ActionItem item = getItem(position);
            holder.title.setText(item.getTitle());
            return convertView;
        }
    }
}
