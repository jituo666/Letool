
package com.xjt.letool.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.umeng.analytics.MobclickAgent;
import com.xjt.letool.LetoolContext;
import com.xjt.letool.R;
import com.xjt.letool.activities.LocalImageBrowseActivity;
import com.xjt.letool.common.EyePosition;
import com.xjt.letool.common.LLog;
import com.xjt.letool.common.SynchronizedHandler;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.metadata.MediaDetails;
import com.xjt.letool.metadata.MediaPath;
import com.xjt.letool.metadata.MediaSet;
import com.xjt.letool.metadata.loader.DataLoadingListener;
import com.xjt.letool.metadata.loader.ThumbnailSetDataLoader;
import com.xjt.letool.metadata.source.LocalAlbumSet;
import com.xjt.letool.selectors.ContractSelectListener;
import com.xjt.letool.selectors.ContractSelector;
import com.xjt.letool.stat.StatConstants;
import com.xjt.letool.utils.LetoolUtils;
import com.xjt.letool.view.BatchDeleteMediaListener;
import com.xjt.letool.view.BatchDeleteMediaListener.DeleteMediaProgressListener;
import com.xjt.letool.view.DetailsHelper;
import com.xjt.letool.view.GLBaseView;
import com.xjt.letool.view.GLController;
import com.xjt.letool.view.LetoolTopBar;
import com.xjt.letool.view.LetoolDialog;
import com.xjt.letool.view.ThumbnailView;
import com.xjt.letool.view.DetailsHelper.CloseListener;
import com.xjt.letool.views.layout.ThumbnailSetContractLayout;
import com.xjt.letool.views.layout.ThumbnailLayout;
import com.xjt.letool.views.layout.ThumbnailLayout.LayoutListener;
import com.xjt.letool.views.opengl.FadeTexture;
import com.xjt.letool.views.opengl.GLESCanvas;
import com.xjt.letool.views.render.ThumbnailSetRenderer;
import com.xjt.letool.views.utils.ViewConfigs;
import com.xjt.letool.LetoolApp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @Author Jituo.Xuan
 * @Date 9:40:26 PM Apr 20, 2014
 * @Comments:null
 */
public class GalleryFragment extends LetoolFragment implements EyePosition.EyePositionListener, ContractSelectListener, LayoutListener {

    private static final String TAG = GalleryFragment.class.getSimpleName();

    public static final String KEY_IS_EMPTY_ALBUM = "empty-album";

    private static final int MSG_LAYOUT_CONFIRMED = 0;
    private static final int MSG_PICK_ALBUM = 1;

    private static final int BIT_LOADING_RELOAD = 1;
    private static final int BIT_LOADING_SYNC = 2;
    private static final int REQUEST_DO_ANIMATION = 1;

    private ViewGroup mNativeButtons;
    private LetoolContext mLetoolContext;
    private GLController mGLController;
    //    private CommonLoadingPanel mLoadingInsie;
    private ThumbnailView mThumbnailView;
    private boolean mIsActive = false;
    private ViewConfigs.AlbumSetPage mConfig;
    private ThumbnailSetRenderer mThumbnailViewRenderer;

    private ContractSelector mSelector;
    private ThumbnailSetDataLoader mThumbnailSetAdapter;
    private MediaSet mMediaSet;

    private DetailsHelper mDetailsHelper;
    private MyDetailsSource mDetailsSource;
    private boolean mShowDetails;
    private EyePosition mEyePosition;
    private WeakReference<Toast> mEmptyAlbumToast = null;
    // The eyes' position of the user, the origin is at the center of the device and the unit is in pixels.
    private float mX;
    private float mY;
    private float mZ;

    private int mLoadingBits = 0;
    private boolean mShowedEmptyToastForSelf = false;
    private Handler mHandler;

    private final GLBaseView mRootPane = new GLBaseView() {

        private final float mMatrix[] = new float[16];

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            mEyePosition.resetPosition();
            LetoolTopBar actionBar = mLetoolContext.getLetoolTopBar();
            int thumbnailViewLeft = left + mConfig.paddingLeft;
            int thumbnailViewRight = right - left - mConfig.paddingRight;
            int thumbnailViewTop = top + mConfig.paddingTop + actionBar.getHeight();
            int thumbnailViewBottom = bottom - top - mConfig.paddingBottom;
            if (mShowDetails) {
                mDetailsHelper.layout(left, thumbnailViewTop, right, bottom);
            } else {
                mThumbnailViewRenderer.setHighlightItemPath(null);
            }

            mThumbnailView.layout(thumbnailViewLeft, thumbnailViewTop, thumbnailViewRight, thumbnailViewBottom);
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

    private class MyLoadingListener implements DataLoadingListener {

        @Override
        public void onLoadingStarted() {
            setLoadingBit(BIT_LOADING_RELOAD);
        }

        @Override
        public void onLoadingFinished(boolean loadingFailed) {
            clearLoadingBit(BIT_LOADING_RELOAD);
        }
    }

    private void setLoadingBit(int loadingBit) {
        mLoadingBits |= loadingBit;
    }

    private void clearLoadingBit(int loadingBit) {
        mLoadingBits &= ~loadingBit;
        if (mLoadingBits == 0 && mIsActive) {
            if (mThumbnailSetAdapter.size() == 0) {
                return;
            }
        }
        // Hide the empty album toast if we are in the root instance of
        // AlbumSetPage and the album is no longer empty (for instance,
        // after a sync is completed and web albums have been synced)
        if (mShowedEmptyToastForSelf) {
            mShowedEmptyToastForSelf = false;
            hideEmptyAlbumToast();
        }
    }

    private void showEmptyAlbumToast(int toastLength) {
        Toast toast;
        if (mEmptyAlbumToast != null) {
            toast = mEmptyAlbumToast.get();
            if (toast != null) {
                toast.show();
                return;
            }
        }
        toast = Toast.makeText(mLetoolContext.getAppContext(), R.string.empty_album, toastLength);
        mEmptyAlbumToast = new WeakReference<Toast>(toast);
        toast.show();
    }

    private void hideEmptyAlbumToast() {
        if (mEmptyAlbumToast != null) {
            Toast toast = mEmptyAlbumToast.get();
            if (toast != null)
                toast.cancel();
        }
    }

    public void onSingleTapUp(int thumbnailIndex) {
        if (!mIsActive)
            return;

        if (mSelector.inSelectionMode()) {
            MediaSet targetSet = mThumbnailSetAdapter.getMediaSet(thumbnailIndex);
            if (targetSet == null)
                return; // Content is dirty, we shall reload soon
            mSelector.toggle(targetSet.getPath());
            mThumbnailView.invalidate();
        } else {
            // Show pressed-up animation for the single-tap.
            mThumbnailViewRenderer.setPressedIndex(thumbnailIndex);
            mThumbnailViewRenderer.setPressedUp();
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PICK_ALBUM, thumbnailIndex, 0), FadeTexture.DURATION);
        }
    }

    private void onDown(int index) {
        mThumbnailViewRenderer.setPressedIndex(index);
    }

    private void onUp(boolean followedByLongPress) {
        if (followedByLongPress) {
            // Avoid showing press-up animations for long-press.
            mThumbnailViewRenderer.setPressedIndex(-1);
        } else {
            mThumbnailViewRenderer.setPressedUp();
        }
    }

    public void onLongTap(int thumbnailIndex) {
        MobclickAgent.onEvent(mLetoolContext.getAppContext(), StatConstants.EVENT_KEY_GALLERY_LONG_PRESSED);
        MediaSet set = mThumbnailSetAdapter.getMediaSet(thumbnailIndex);
        if (set == null)
            return;
        mSelector.toggle(set.getPath());
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
        mLetoolContext = (LetoolContext) getActivity();
        mGLController = mLetoolContext.getGLController();

        //mLoadingInsie = (CommonLoadingPanel) rootView.findViewById(R.id.loading);
        //        mLoadingInsie.setVisibility(View.VISIBLE);
        mHandler = new SynchronizedHandler(mGLController) {

            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_LAYOUT_CONFIRMED: {
                        //                        mLoadingInsie.setVisibility(View.GONE);
                        break;
                    }
                    case MSG_PICK_ALBUM: {
                        pickAlbum(message.arg1);
                        break;
                    }
                    default:
                        throw new AssertionError(message.what);
                }
            }
        };
        initializeViews();
        initializeData();
        mEyePosition = new EyePosition(mLetoolContext.getAppContext(), this);
        //mThumbnailView.startRisingAnimation();
    }

    private void initializeViews() {
        mSelector = new ContractSelector(mLetoolContext, true);
        mSelector.setSelectionListener(this);
        mConfig = ViewConfigs.AlbumSetPage.get(mLetoolContext.getAppContext());
        ThumbnailLayout layout = new ThumbnailSetContractLayout(mConfig.albumSetSpec);
        mThumbnailView = new ThumbnailView(mLetoolContext, layout);
        mThumbnailView.setBackgroundColor(
                LetoolUtils.intColorToFloatARGBArray(getResources().getColor(R.color.default_background_thumbnail))
                );
        mThumbnailViewRenderer = new ThumbnailSetRenderer(mLetoolContext, mThumbnailView, mSelector);
        layout.setRenderer(mThumbnailViewRenderer);
        layout.setLayoutListener(this);
        mThumbnailView.setThumbnailRenderer(mThumbnailViewRenderer);
        mRootPane.addComponent(mThumbnailView);
        mThumbnailView.setListener(new ThumbnailView.SimpleListener() {

            @Override
            public void onDown(int index) {
                GalleryFragment.this.onDown(index);
            }

            @Override
            public void onUp(boolean followedByLongPress) {
                GalleryFragment.this.onUp(followedByLongPress);
            }

            @Override
            public void onSingleTapUp(int thumbnailIndex) {
                GalleryFragment.this.onSingleTapUp(thumbnailIndex);
            }

            @Override
            public void onLongTap(int thumbnailIndex) {
                GalleryFragment.this.onLongTap(thumbnailIndex);
            }
        });
    }

    private void initializeData() {
        mMediaSet = new LocalAlbumSet(new MediaPath(mLetoolContext.getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY), -1000),
                (LetoolApp) getActivity().getApplication());
        mSelector.setSourceMediaSet(mMediaSet);
        mThumbnailSetAdapter = new ThumbnailSetDataLoader(mLetoolContext, mMediaSet);
        mThumbnailSetAdapter.setLoadingListener(new MyLoadingListener());
        mThumbnailViewRenderer.setModel(mThumbnailSetAdapter);
    }

    private void initBars() {
        LetoolTopBar topBar = mLetoolContext.getLetoolTopBar();
        topBar.setOnActionMode(LetoolTopBar.ACTION_BAR_MODE_BROWSE, this);
        topBar.setTitleIcon(R.drawable.ic_drawer);
        topBar.setTitleText(R.string.app_name);
        mNativeButtons = (ViewGroup) topBar.getActionPanel().findViewById(R.id.navi_buttons);
        mNativeButtons.setVisibility(View.VISIBLE);

        TextView naviToGallery = (TextView) mNativeButtons.findViewById(R.id.navi_to_gallery);
        naviToGallery.setEnabled(false);
        TextView naviToPhoto = (TextView) mNativeButtons.findViewById(R.id.navi_to_photo);
        naviToPhoto.setEnabled(true);
        naviToPhoto.setOnClickListener(this);

    }

    private void initSelectionActionBar() {
        LetoolTopBar actionBar = mLetoolContext.getLetoolTopBar();
        actionBar.setOnActionMode(LetoolTopBar.ACTION_BAR_MODE_SELECTION, this);
        actionBar.setContractSelectionManager(mSelector);
        String format = getResources().getQuantityString(R.plurals.number_of_items_selected, 0);
        actionBar.setTitleText(String.format(format, 0));
    }

    private void getThumbnailCenter(int thumbnailIndex, int center[]) {
        Rect offset = new Rect();
        mRootPane.getBoundsOf(mThumbnailView, offset);
        Rect r = mThumbnailView.getThumbnailRect(thumbnailIndex);
        int scrollX = mThumbnailView.getScrollX();
        int scrollY = mThumbnailView.getScrollY();
        center[0] = offset.left + (r.left + r.right) / 2 - scrollX;
        center[1] = offset.top + (r.top + r.bottom) / 2 - scrollY;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LLog.i(TAG, "onCreateView");
        initBars();
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
        LLog.i(TAG, "onResume");
        mGLController.onResume();
        mGLController.lockRenderThread();
        try {
            mIsActive = true;
            mGLController.setContentPane(mRootPane);
            mThumbnailSetAdapter.resume();
            mThumbnailViewRenderer.resume();
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
        mGLController.lockRenderThread();
        try {
            mIsActive = false;
            mThumbnailSetAdapter.pause();
            mThumbnailViewRenderer.pause();
            mEyePosition.pause();
        } finally {
            mGLController.unlockRenderThread();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        mGLController.lockRenderThread();
        try {

            super.onActivityResult(requestCode, resultCode, data);
            if (data != null && data.getBooleanExtra(KEY_IS_EMPTY_ALBUM, false)) {
                showEmptyAlbumToast(Toast.LENGTH_SHORT);
            }
            switch (requestCode) {
                case REQUEST_DO_ANIMATION: {
                    mThumbnailView.startRisingAnimation();
                }
            }
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
        if (mMediaSet != null) {
            mMediaSet.closeCursor();
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

    private void hideDetails() {
        mShowDetails = false;
        mDetailsHelper.hide();
        mThumbnailViewRenderer.setHighlightItemPath(null);
        mThumbnailView.invalidate();
    }

    private void showDetails() {
        mShowDetails = true;
        if (mDetailsHelper == null) {
            mDetailsHelper = new DetailsHelper(mLetoolContext, mRootPane, mDetailsSource);
            mDetailsHelper.setCloseListener(new CloseListener() {

                @Override
                public void onClose() {
                    hideDetails();
                }
            });
        }
        mDetailsHelper.show();
    }

    private class MyDetailsSource implements DetailsHelper.DetailsSource {

        private int mIndex;

        @Override
        public int size() {
            return mThumbnailSetAdapter.size();
        }

        @Override
        public int setIndex() {
            //            Path id = mSelector.getSelected(false).get(0);
            //            mIndex = mThumbnailSetAdapter.findSet(id);
            return mIndex;
        }

        @Override
        public MediaDetails getDetails() {
            //            MediaObject item = mThumbnailSetAdapter.getMediaSet(mIndex);
            //            if (item != null) {
            //                mThumbnailViewRenderer.setHighlightItemPath(item.getPath());
            //                return item.getDetails();
            //            } else {
            return null;
            //            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.action_navi) {
            mLetoolContext.getLetoolSlidingMenu().toggle();
        } else if (v.getId() == R.id.operation_delete) {

            MobclickAgent.onEvent(getActivity(), StatConstants.EVENT_KEY_GALLERY_DELETE);
            int count = mSelector.getSelectedCount();
            if (count <= 0) {
                Toast t = Toast.makeText(getActivity(), R.string.common_selection_tip, Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                return;
            }
            BatchDeleteMediaListener cdl = new BatchDeleteMediaListener(getActivity(), mLetoolContext.getDataManager(),
                    new DeleteMediaProgressListener() {

                        @Override
                        public void onConfirmDialogDismissed(boolean confirmed) {
                            if (confirmed)
                                mSelector.leaveSelectionMode();
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
        } else if (v.getId() == R.id.enter_selection_indicate) {
            MobclickAgent.onEvent(mLetoolContext.getAppContext(), StatConstants.EVENT_KEY_SELECT_OK);
            mSelector.leaveSelectionMode();
        } else if (v.getId() == R.id.navi_to_photo) {
            Fragment f = new PhotoFragment();
            Bundle data = new Bundle();
            data.putString(LocalImageBrowseActivity.KEY_MEDIA_PATH, mLetoolContext.getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
            data.putBoolean(LocalImageBrowseActivity.KEY_IS_PHOTO_ALBUM, true);
            f.setArguments(data);
            mLetoolContext.pushContentFragment(f, this, false);
        }
    }

    private void pickAlbum(int thumbnailIndex) {
        if (!mIsActive)
            return;
        MediaSet targetSet = mThumbnailSetAdapter.getMediaSet(thumbnailIndex);
        if (targetSet == null)
            return; // Content is dirty, we shall reload soon
        if (targetSet.getTotalMediaItemCount() == 0) {
            showEmptyAlbumToast(Toast.LENGTH_SHORT);
            return;
        }
        hideEmptyAlbumToast();
        MediaPath mediaPath = targetSet.getPath();
        Fragment f = new PhotoFragment();
        Bundle data = new Bundle();
        data.putString(LocalImageBrowseActivity.KEY_MEDIA_PATH, mLetoolContext.getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
        data.putString(LocalImageBrowseActivity.KEY_ALBUM_TITLE, targetSet.getName());
        data.putInt(LocalImageBrowseActivity.KEY_ALBUM_ID, mediaPath.getIdentity());
        data.putBoolean(LocalImageBrowseActivity.KEY_IS_PHOTO_ALBUM, false);
        f.setArguments(data);
        mLetoolContext.pushContentFragment(f, this, true);
        /* Intent it = new Intent();
         it.setClass(getActivity(), LocalImageBrowseActivity.class);
         it.putExtra(LocalImageBrowseActivity.KEY_ALBUM_ID, mediaPath.getIdentity());
         it.putExtra(LocalImageBrowseActivity.KEY_MEDIA_PATH, mLetoolContext.getDataManager().getTopSetPath(DataManager.INCLUDE_LOCAL_IMAGE_ONLY));
         it.putExtra(LocalImageBrowseActivity.KEY_ALBUM_TITLE, targetSet.getName());
         int[] center = new int[2];
         getThumbnailCenter(thumbnailIndex, center);
         startActivityForResult(it, LocalImageBrowseActivity.REQUEST_FOR_PHOTO);*/
        return;

    }

    public void onMenuClicked() {
        MobclickAgent.onEvent(mLetoolContext.getAppContext(), StatConstants.EVENT_KEY_SLIDE_MENU_MENU);
        //getLetoolSlidingMenu().toggle();

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

                initBars();
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
        mLetoolContext.getLetoolTopBar().setTitleText(String.format(format, count));
    }

    @Override
    public void onLayoutFinshed(int count) {
        mHandler.obtainMessage(MSG_LAYOUT_CONFIRMED, count, 0).sendToTarget();
    }

}
