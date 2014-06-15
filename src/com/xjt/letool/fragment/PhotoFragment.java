
package com.xjt.letool.fragment;

import com.umeng.analytics.MobclickAgent;
import com.xjt.letool.LetoolApp;
import com.xjt.letool.R;
import com.xjt.letool.activities.FullImageActivity;
import com.xjt.letool.activities.ThumbnailActivity;
import com.xjt.letool.common.EyePosition;
import com.xjt.letool.common.Future;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.SynchronizedHandler;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.metadata.MediaItem;
import com.xjt.letool.metadata.MediaPath;
import com.xjt.letool.metadata.MediaSet;
import com.xjt.letool.metadata.MediaSetUtils;
import com.xjt.letool.metadata.loader.DataLoadingListener;
import com.xjt.letool.metadata.loader.ThumbnailDataLoader;
import com.xjt.letool.metadata.source.LocalAlbum;
import com.xjt.letool.selectors.ContractSelectListener;
import com.xjt.letool.selectors.ContractSelector;
import com.xjt.letool.stat.StatConstants;
import com.xjt.letool.surpport.MenuItem;
import com.xjt.letool.surpport.PopupMenu;
import com.xjt.letool.surpport.PopupMenu.OnMenuItemClickListener;
import com.xjt.letool.utils.LetoolUtils;
import com.xjt.letool.utils.RelativePosition;
import com.xjt.letool.utils.StorageUtils;
import com.xjt.letool.utils.Utils;
import com.xjt.letool.view.BatchDeleteMediaListener;
import com.xjt.letool.view.BatchDeleteMediaListener.DeleteMediaProgressListener;
import com.xjt.letool.view.DetailsHelper;
import com.xjt.letool.view.GLBaseView;
import com.xjt.letool.view.GLController;
import com.xjt.letool.view.GLRootView;
import com.xjt.letool.view.LetoolTopBar;
import com.xjt.letool.view.LetoolDialog;
import com.xjt.letool.view.ThumbnailView;
import com.xjt.letool.views.layout.ThumbnailContractLayout;
import com.xjt.letool.views.layout.ThumbnailLayout;
import com.xjt.letool.views.layout.ThumbnailLayout.LayoutListener;
import com.xjt.letool.views.opengl.FadeTexture;
import com.xjt.letool.views.opengl.GLESCanvas;
import com.xjt.letool.views.render.ThumbnailRenderer;
import com.xjt.letool.views.utils.ViewConfigs;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @Author Jituo.Xuan
 * @Date 9:48:35 AM Apr 19, 2014
 * @Comments:null
 */
public class PhotoFragment extends LetoolFragment implements EyePosition.EyePositionListener, ContractSelectListener,
        LayoutListener, OnMenuItemClickListener {

    private static final String TAG = PhotoFragment.class.getSimpleName();

    public static final String KEY_SET_CENTER = "set-center";
    public static final String KEY_RESUME_ANIMATION = "resume_animation";
    private static final int BIT_LOADING_RELOAD = 1;
    private static final int BIT_LOADING_SYNC = 2;
    private static final int MSG_LAYOUT_CONFIRMED = 0;
    private static final int MSG_PICK_PHOTO = 1;

    private static final int POP_UP_MENU_ITEM_SELECT = 0;
    private static final int POP_UP_MENU_ITEM_CAMERA = 1;

    //photo data
    private MediaPath mDataSetPath;
    private MediaSet mDataSet;
    private ThumbnailDataLoader mAlbumDataSetLoader;
    private int mLoadingBits = 0;
    private Future<Integer> mSyncTask = null; // synchronize data
    private boolean mInitialSynced = false;

    //views
    private TextView mEmptyView;
    private GLRootView mGLRootView;
    private ImageView mMore;
    private ViewConfigs.AlbumPage mConfig;
    private ThumbnailView mThumbnailView;
    private ThumbnailRenderer mRender;
    private RelativePosition mOpenCenter = new RelativePosition();
    private boolean mIsActive = false;

    private String mAlbumTitle;
    private boolean mIsPhotoAlbum = false;
    private boolean mHasSDCard = false;
    private boolean mHasDCIM = false;
    private boolean mGetContent;
    private SynchronizedHandler mHandler;
    protected ContractSelector mSelector;
    private EyePosition mEyePosition; // The eyes' position of the user, the origin is at the center of the device and the unit is in pixels.
    private float mUserDistance; // in pixel
    private float mX;
    private float mY;
    private float mZ;

    private final GLBaseView mRootPane = new GLBaseView() {

        private final float mMatrix[] = new float[16];

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            mEyePosition.resetPosition();
            LetoolTopBar actionBar = getLetoolTopBar();
            int thumbnailViewLeft = left + mConfig.paddingLeft;
            int thumbnailViewRight = right - left - mConfig.paddingRight;
            int thumbnailViewTop = top + mConfig.paddingTop + actionBar.getHeight();
            int thumbnailViewBottom = bottom - top - mConfig.paddingBottom;
            mRender.setHighlightItemPath(null);
            // Set the mThumbnailView as a reference point to the open animation
            mOpenCenter.setReferencePosition(0, thumbnailViewTop);
            mThumbnailView.layout(thumbnailViewLeft, thumbnailViewTop, thumbnailViewRight, thumbnailViewBottom);
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
            if (mAlbumDataSetLoader.size() == 0) {
                Toast.makeText(getActivity(), R.string.empty_album, Toast.LENGTH_LONG).show();
                mGLRootView.setVisibility(View.GONE);
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

    private void onSingleTapUp(int thumbnailIndex) {
        if (!mIsActive)
            return;

        if (mSelector.inSelectionMode()) {
            MediaItem item = mAlbumDataSetLoader.get(thumbnailIndex);
            if (item == null)
                return; // Item not ready yet, ignore the click
            mSelector.toggle(item.getPath());
            mThumbnailView.invalidate();
        } else { // Render transition in pressed state
            mRender.setPressedIndex(thumbnailIndex);
            mRender.setPressedUp();
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PICK_PHOTO, thumbnailIndex, 0), FadeTexture.DURATION);
        }
    }

    public void onLongTap(int thumbnailIndex) {
        MobclickAgent.onEvent(getAndroidContext(), StatConstants.EVENT_KEY_PHOTO_LONG_PRESSED);
        if (mGetContent)
            return;
        MediaItem item = mAlbumDataSetLoader.get(thumbnailIndex);
        if (item == null)
            return;
        mSelector.toggle(item.getPath());
        mThumbnailView.invalidate();
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
    }

    private void initializeData() {
        Bundle data = getArguments();
        mIsPhotoAlbum = data.getBoolean(ThumbnailActivity.KEY_IS_PHOTO_ALBUM);
        if (mIsPhotoAlbum) {
            if (MediaSetUtils.MY_ALBUM_BUCKETS.length > 0) {
                mAlbumTitle = getString(R.string.common_photo);
                mDataSetPath = new MediaPath(data.getString(ThumbnailActivity.KEY_MEDIA_PATH), MediaSetUtils.MY_ALBUM_BUCKETS[0]);
                mDataSet = new LocalAlbum(mDataSetPath, (LetoolApp) getActivity().getApplication(), MediaSetUtils.MY_ALBUM_BUCKETS, true,
                        getString(R.string.common_photo));
                mHasDCIM = true;
            } else {
                mHasDCIM = false;
                return;
            }
        } else {
            mAlbumTitle = data.getString(ThumbnailActivity.KEY_ALBUM_TITLE);
            mDataSetPath = new MediaPath(data.getString(ThumbnailActivity.KEY_MEDIA_PATH), data.getInt(ThumbnailActivity.KEY_ALBUM_ID));
            mDataSet = getDataManager().getMediaSet(mDataSetPath);
            if (mDataSet == null) {
                Utils.fail("MediaSet is null. Path = %s", mDataSetPath);
            }
        }
        mAlbumDataSetLoader = new ThumbnailDataLoader(this, mDataSet);
        mAlbumDataSetLoader.setLoadingListener(new MetaDataLoadingListener());
    }

    private void initializeViews() {
        mSelector = new ContractSelector(this, false);
        mSelector.setSelectionListener(this);
        mConfig = ViewConfigs.AlbumPage.get(getAndroidContext());
        ThumbnailLayout layout;
        layout = new ThumbnailContractLayout(mConfig.albumSpec);
        mThumbnailView = new ThumbnailView(this, layout);
        mThumbnailView.setBackgroundColor(LetoolUtils.intColorToFloatARGBArray(getResources().getColor(R.color.default_background_thumbnail)));
        mThumbnailView.setListener(new ThumbnailView.SimpleListener() {

            @Override
            public void onDown(int index) {
                PhotoFragment.this.onDown(index);
            }

            @Override
            public void onUp(boolean followedByLongPress) {
                PhotoFragment.this.onUp(followedByLongPress);
            }

            @Override
            public void onSingleTapUp(int thumbnailIndex) {
                PhotoFragment.this.onSingleTapUp(thumbnailIndex);
            }

            @Override
            public void onLongTap(int thumbnailIndex) {
                PhotoFragment.this.onLongTap(thumbnailIndex);
            }
        });
        mRender = new ThumbnailRenderer(this, mThumbnailView, mSelector);
        layout.setRenderer(mRender);
        layout.setLayoutListener(this);
        mThumbnailView.setThumbnailRenderer(mRender);
        mRender.setModel(mAlbumDataSetLoader);
        mRootPane.addComponent(mThumbnailView);
    }

    private void initBrowseActionBar() {
        LetoolTopBar actionBar = getLetoolTopBar();
        actionBar.setOnActionMode(LetoolTopBar.ACTION_BAR_MODE_BROWSE, this);

        actionBar.setTitleText(mAlbumTitle);
        mMore = (ImageView) actionBar.getActionPanel().findViewById(R.id.action_more);
        mMore.setVisibility(View.VISIBLE);
        if (mIsPhotoAlbum) {
            actionBar.setTitleIcon(R.drawable.ic_drawer);
            mMore.setImageResource(R.drawable.ic_action_more);
        } else {
            actionBar.setTitleIcon(R.drawable.ic_action_previous_item);
            mMore.setImageResource(R.drawable.ic_action_accept);

        }
    }

    private void initSelectionActionBar() {
        LetoolTopBar actionBar = getLetoolTopBar();
        actionBar.setOnActionMode(LetoolTopBar.ACTION_BAR_MODE_SELECTION, this);
        actionBar.setContractSelectionManager(mSelector);
        String format = getResources().getQuantityString(R.plurals.number_of_items_selected, 0);
        actionBar.setTitleText(String.format(format, 0));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LLog.i(TAG, "onCreateView" + System.currentTimeMillis());
        View rootView = inflater.inflate(R.layout.gl_root_view, container, false);
        mHasSDCard = StorageUtils.externalStorageAvailable();
        mGLRootView = (GLRootView) rootView.findViewById(R.id.gl_root_view);
        initializeData();
        initializeViews();
        initBrowseActionBar();
        mHandler = new SynchronizedHandler(mGLRootView) {

            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_LAYOUT_CONFIRMED: {
                        //mLoadingInsie.setVisibility(View.GONE);
                        break;
                    }
                    case MSG_PICK_PHOTO: {
                        pickPhoto(message.arg1);
                        break;
                    }
                    default:
                        throw new AssertionError(message.what);
                }
            }
        };
        mEyePosition = new EyePosition(getAndroidContext(), this);
        mEmptyView = (TextView) rootView.findViewById(R.id.empty_view);
        if (!mHasSDCard) {
            showEmptyView(R.string.common_error_nosdcard);
            return rootView;
        } else if (mIsPhotoAlbum && !mHasDCIM) {
            showEmptyView(R.string.common_error_nodcim);
            return rootView;
        }
        Bundle data = getArguments();
        if (data != null) {
            int[] center = data.getIntArray(KEY_SET_CENTER);
            if (center != null) {
                mOpenCenter.setAbsolutePosition(center[0], center[1]);
                mThumbnailView.startScatteringAnimation(mOpenCenter);
            } else {

            }
        }
        return rootView;
    }

    private void showEmptyView(int resId) {
        mEmptyView.setText(resId);
        mEmptyView.setVisibility(View.VISIBLE);
        mGLRootView.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        LLog.i(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mHasSDCard || mIsPhotoAlbum && !mHasDCIM) {
            return;
        }
        LLog.i(TAG, "onResume" + System.currentTimeMillis());
        mGLRootView.onResume();
        mGLRootView.lockRenderThread();
        try {
            mIsActive = true;
            mGLRootView.setContentPane(mRootPane);
            // Set the reload bit here to prevent it exit this page in clearLoadingBit().
            setLoadingBit(BIT_LOADING_RELOAD);
            mAlbumDataSetLoader.resume();
            mRender.resume();
            mRender.setPressedIndex(-1);
            mEyePosition.resume();
            if (!mInitialSynced) {
                setLoadingBit(BIT_LOADING_SYNC);
                //mSyncTask = mDataSet.requestSync(this);
            }
        } finally {
            mGLRootView.unlockRenderThread();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LLog.i(TAG, "onPause");
        if (!mHasSDCard || mIsPhotoAlbum && !mHasDCIM) {
            return;
        }
        mGLRootView.onPause();
        mGLRootView.lockRenderThread();
        try {
            mIsActive = false;
            mRender.setThumbnailFilter(null);
            mAlbumDataSetLoader.pause();
            mRender.pause();
            DetailsHelper.pause();
            mEyePosition.resume();
            if (mSyncTask != null) {
                mSyncTask.cancel();
                mSyncTask = null;
                clearLoadingBit(BIT_LOADING_SYNC);
            }
        } finally {
            mGLRootView.unlockRenderThread();
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
        if (mDataSet != null) {
            mDataSet.closeCursor();
        }
        LLog.i(TAG, "onDestroy");
    }

    @Override
    public GLController getGLController() {
        return mGLRootView;
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
    public void onMenuClicked() {
        if (!mIsPhotoAlbum)
            return;
        MobclickAgent.onEvent(getAndroidContext(), StatConstants.EVENT_KEY_SLIDE_MENU_MENU);
        getLetoolSlidingMenu().toggle();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_navi) {
            if (!mIsActive && !(mIsPhotoAlbum && !mHasDCIM)) {
                return;
            }
            if (!mIsPhotoAlbum) {
                getActivity().finish();
            } else {
                getLetoolSlidingMenu().toggle();
            }
            return;
        }
        if (!mIsActive) {
            return;
        }
        if (v.getId() == R.id.operation_delete) {

            MobclickAgent.onEvent(getAndroidContext(), StatConstants.EVENT_KEY_PHOTO_DELETE);
            int count = mSelector.getSelectedCount();
            if (count <= 0) {
                Toast t = Toast.makeText(getActivity(), R.string.common_selection_tip, Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                return;
            }
            BatchDeleteMediaListener cdl = new BatchDeleteMediaListener(getActivity(), getDataManager(),
                    new DeleteMediaProgressListener() {

                        @Override
                        public void onConfirmDialogDismissed(boolean confirmed) {
                            if (confirmed) {
                                mSelector.leaveSelectionMode();
                            }
                        }

                        @Override
                        public ArrayList<MediaPath> onGetDeleteItem() {
                            return mSelector.getSelected(false);
                        }

                    });
            final LetoolDialog dlg = new LetoolDialog(getActivity());
            dlg.setTitle(R.string.common_recommend);
            dlg.setOkBtn(R.string.common_ok, cdl);
            dlg.setCancelBtn(R.string.common_cancel, cdl);
            dlg.setDividerVisible(true);
            dlg.setMessage(R.string.common_delete_tip);
            dlg.setDividerVisible(true);
            dlg.show();

        } else if (v.getId() == R.id.action_more) {
            if (mIsPhotoAlbum) {
                MobclickAgent.onEvent(getAndroidContext(), StatConstants.EVENT_KEY_POPUP_MENU);
                showPopupMenu();
            } else {
                if (mSelector != null)
                    mSelector.enterSelectionMode();
            }
        } else if (v.getId() == R.id.enter_selection_indicate) {
            MobclickAgent.onEvent(getAndroidContext(), StatConstants.EVENT_KEY_SELECT_OK);
            mSelector.leaveSelectionMode();
        }
    }

    public void showPopupMenu() {
        PopupMenu popup = new PopupMenu(this.getActivity());
        popup.setOnItemSelectedListener(this);
        popup.add(POP_UP_MENU_ITEM_SELECT, R.drawable.ic_action_accept, R.string.popup_menu_select_mode);
        popup.add(POP_UP_MENU_ITEM_CAMERA, R.drawable.ic_action_camera, R.string.popup_menu_take_picture);
        popup.show(mMore);

    }

    @Override
    public void onSelectionModeChange(int mode) {
        switch (mode) {
            case ContractSelector.ENTER_SELECTION_MODE: {
                initSelectionActionBar();
                mRootPane.invalidate();
                break;
            }
            case ContractSelector.LEAVE_SELECTION_MODE: {
                initBrowseActionBar();
                mRootPane.invalidate();
                break;
            }
            case ContractSelector.SELECT_ALL_MODE: {
                mRootPane.invalidate();
                break;
            }
        }
    }

    @Override
    public void onSelectionChange(MediaPath path, boolean selected) {
        int count = mSelector.getSelectedCount();
        String format = getResources().getQuantityString(R.plurals.number_of_items_selected, count);
        getLetoolTopBar().setTitleText(String.format(format, count));
    }

    private void pickPhoto(int index) {
        Intent it = new Intent();
        it.setClass(getAndroidContext(), FullImageActivity.class);
        if (mIsPhotoAlbum) {
            it.putExtra(ThumbnailActivity.KEY_MEDIA_PATH, getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
            it.putExtra(ThumbnailActivity.KEY_IS_PHOTO_ALBUM, true);
        } else {
            it.putExtra(ThumbnailActivity.KEY_ALBUM_ID, mDataSet.getPath().getIdentity());
            it.putExtra(ThumbnailActivity.KEY_MEDIA_PATH, getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
            it.putExtra(ThumbnailActivity.KEY_IS_PHOTO_ALBUM, false);
            it.putExtra(ThumbnailActivity.KEY_ALBUM_TITLE, mDataSet.getName());
        }
        it.putExtra(FullImageFragment.KEY_INDEX_HINT, index);
        startActivity(it);
    }

    //-----------------------------------------------details-----------------------------------------------------------------------

    @Override
    public void onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case POP_UP_MENU_ITEM_SELECT:
                MobclickAgent.onEvent(getAndroidContext(), StatConstants.EVENT_KEY_MENU_SELECT);
                if (mSelector != null) {
                    mSelector.enterSelectionMode();
                }
                break;
            case POP_UP_MENU_ITEM_CAMERA:
                MobclickAgent.onEvent(getAndroidContext(), StatConstants.EVENT_KEY_MENU_CAMERA);
                Intent it = new Intent();
                it.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                startActivity(it);
                break;

        }
    }

}
