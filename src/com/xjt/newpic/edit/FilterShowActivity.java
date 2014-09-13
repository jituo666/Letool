
package com.xjt.newpic.edit;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.print.PrintHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.nineoldandroids.view.ViewHelper;
import com.xjt.newpic.R;
import com.xjt.newpic.common.ApiHelper;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.edit.cache.ImageLoader;
import com.xjt.newpic.edit.category.CategoryAction;
import com.xjt.newpic.edit.category.CategoryAdapter;
import com.xjt.newpic.edit.category.CategoryMainPanel;
import com.xjt.newpic.edit.category.CategorySelected;
import com.xjt.newpic.edit.category.CategoryView;
import com.xjt.newpic.edit.editors.BasicEditor;
import com.xjt.newpic.edit.editors.Editor;
import com.xjt.newpic.edit.editors.EditorChanSat;
import com.xjt.newpic.edit.editors.EditorColorBorder;
import com.xjt.newpic.edit.editors.EditorCrop;
import com.xjt.newpic.edit.editors.EditorDraw;
import com.xjt.newpic.edit.editors.EditorGrad;
import com.xjt.newpic.edit.editors.EditorManager;
import com.xjt.newpic.edit.editors.EditorMirror;
import com.xjt.newpic.edit.editors.EditorPanel;
import com.xjt.newpic.edit.editors.EditorRedEye;
import com.xjt.newpic.edit.editors.EditorRotate;
import com.xjt.newpic.edit.editors.EditorStraighten;
import com.xjt.newpic.edit.editors.EditorTinyPlanet;
import com.xjt.newpic.edit.editors.ImageOnlyEditor;
import com.xjt.newpic.edit.filters.FilterDrawRepresentation;
import com.xjt.newpic.edit.filters.FilterMirrorRepresentation;
import com.xjt.newpic.edit.filters.FilterRepresentation;
import com.xjt.newpic.edit.filters.FilterRotateRepresentation;
import com.xjt.newpic.edit.filters.FilterUserPresetRepresentation;
import com.xjt.newpic.edit.filters.FiltersManager;
import com.xjt.newpic.edit.filters.ImageFilter;
import com.xjt.newpic.edit.history.HistoryItem;
import com.xjt.newpic.edit.history.HistoryManager;
import com.xjt.newpic.edit.imageshow.ImageShow;
import com.xjt.newpic.edit.imageshow.MasterImage;
import com.xjt.newpic.edit.imageshow.Spline;
import com.xjt.newpic.edit.pipeline.CachingPipeline;
import com.xjt.newpic.edit.pipeline.ImagePreset;
import com.xjt.newpic.edit.pipeline.ProcessingService;
import com.xjt.newpic.edit.provider.SharedImageProvider;
import com.xjt.newpic.edit.tools.SaveImage;
import com.xjt.newpic.edit.tools.XmpPresets;
import com.xjt.newpic.edit.tools.XmpPresets.XMresults;
import com.xjt.newpic.edit.ui.FramedTextButton;
import com.xjt.newpic.imagedata.utils.LetoolBitmapPool;
import com.xjt.newpic.metadata.source.LocalAlbum;
import com.xjt.newpic.surpport.PopupMenu;
import com.xjt.newpic.utils.LetoolUtils;
import com.xjt.newpic.view.LetoolDialog;
import com.xjt.newpic.view.LetoolTopBar;
import com.xjt.newpic.view.LetoolTopBar.OnActionModeListener;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Vector;

public class FilterShowActivity extends FragmentActivity implements OnItemClickListener, OnActionModeListener,
        PopupMenu.OnDismissListener {

    private static final String TAG = FilterShowActivity.class.getSimpleName();

    private static final long LIMIT_SUPPORTS_HIGHRES = 134217728; // 128Mb

    public static final String TINY_PLANET_ACTION = "com.android.camera.action.TINY_PLANET";
    public static final String FILTER_EDIT_ACTION = "com.xjt.newpic.edit";

    public static final boolean RESET_TO_LOADED = false;
    private static final int SELECT_PICTURE = 1;

    private String mAction = "";
    private MasterImage mMasterImage = null;
    private ImageShow mImageShow = null;

    private WeakReference<ProgressDialog> mSavingProgressDialog;
    private EditorPlaceHolder mEditorPlaceHolder = new EditorPlaceHolder(this);
    private Editor mCurrentEditor = null;

    private boolean mShowingTinyPlanet = false;

    private final Vector<ImageShow> mImageViews = new Vector<ImageShow>();

    private File mSharedOutputFile = null;
    private boolean mSharingImage = false;

    private Uri mOriginalImageUri = null;
    private ImagePreset mOriginalPreset = null;
    private Uri mSelectedImageUri = null;

    private ArrayList<CategoryAction> mActions = new ArrayList<CategoryAction>();
    private CategoryAdapter mCategoryLooksAdapter = null;
    private CategoryAdapter mCategoryBordersAdapter = null;
    private CategoryAdapter mCategoryGeometryAdapter = null;
    private CategoryAdapter mCategoryFiltersAdapter = null;
    private CategoryAdapter mCategoryVersionsAdapter = null;
    private int mCurrentPanel = CategoryMainPanel.LOOKS;

    private boolean mHandlingSwipeButton = false;
    private View mHandledSwipeView = null;
    private float mHandledSwipeViewLastDelta = 0;
    private float mSwipeStartX = 0;
    private float mSwipeStartY = 0;

    private boolean mIsBound = false;
    private PopupMenu mCurrentMenu = null;
    private boolean mLoadingVisible = true;
    private LetoolTopBar mTopBar;
    private View mReset;
    private View mSave;

    private ProcessingService mBoundService;
    private LoadBitmapTask mLoadBitmapTask;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            /*
             * This is called when the connection with the service has been
             * established, giving us the service object we can use to
             * interact with the service.  Because we have bound to a explicit
             * service that we know is running in our own process, we can
             * cast its IBinder to a concrete class and directly access it.
             */
            mBoundService = ((ProcessingService.LocalBinder) service).getService();
            mBoundService.setFiltershowActivity(FilterShowActivity.this);
            mBoundService.onStart();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            /*
             * This is called when the connection with the service has been
             * unexpectedly disconnected -- that is, its process crashed.
             * Because it is running in our same process, we should never
             * see this happen.
             */
            mBoundService = null;
        }
    };

    void doBindService() {
        /*
         * Establish a connection with the service.  We use an explicit
         * class name because we want a specific service implementation that
         * we know will be running in our own process (and thus won't be
         * supporting component replacement by other applications).
         */
        bindService(new Intent(this, ProcessingService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) { // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clearGalleryBitmapPool();
        doBindService();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.GRAY));
    }

    public void updateUIAfterServiceStarted() {
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        ImageFilter.setActivityForMemoryToasts(this);
        setupMasterImage();
        setDefaultValues();
        fillEditors();
        loadXML();
        fillCategories();
        loadMainPanel();
        extractXMPData();
        processIntent();
    }

    private void fillEditors() {
        mEditorPlaceHolder.addEditor(new EditorChanSat());
        mEditorPlaceHolder.addEditor(new EditorGrad());
        mEditorPlaceHolder.addEditor(new EditorDraw());
        mEditorPlaceHolder.addEditor(new EditorColorBorder());
        mEditorPlaceHolder.addEditor(new BasicEditor());
        mEditorPlaceHolder.addEditor(new ImageOnlyEditor());
        mEditorPlaceHolder.addEditor(new EditorTinyPlanet());
        mEditorPlaceHolder.addEditor(new EditorRedEye());
        mEditorPlaceHolder.addEditor(new EditorCrop());
        mEditorPlaceHolder.addEditor(new EditorMirror());
        mEditorPlaceHolder.addEditor(new EditorRotate());
        mEditorPlaceHolder.addEditor(new EditorStraighten());
        //mEditorPlaceHolder.addEditor(new EditorRedEye());
    }

    public void initActionBar() {
        mTopBar = new LetoolTopBar(this, (ViewGroup) findViewById(R.id.letool_top_bar_container));
        mTopBar.setOnActionMode(LetoolTopBar.ACTION_BAR_MODE_IMAGE_EDIT, this);
        mTopBar.setTitleIcon(R.drawable.ic_action_previous_item);
        View operationPanel = mTopBar.getActionPanel();
        ImageView undo = (ImageView) operationPanel.findViewById(R.id.action_undo);
        ImageView redo = (ImageView) operationPanel.findViewById(R.id.action_redo);
        mReset = operationPanel.findViewById(R.id.action_reset);
        mSave = operationPanel.findViewById(R.id.action_save);
        mMasterImage.getHistory().initMenuItems(undo, redo, mReset);
    }

    private void loadXML() {
        setContentView(R.layout.np_edit_activity);
        initActionBar();
        mImageShow = (ImageShow) findViewById(R.id.imageShow);
        mImageViews.add(mImageShow);
        mImageShow.attach();
        setupEditors();
    }

    public void fillCategories() {
        fillLooks();
        fillBorders();
        fillTools();
        fillEffects();
    }

    private void fillBorders() {
        FiltersManager filtersManager = FiltersManager.getManager();
        ArrayList<FilterRepresentation> borders = filtersManager.getBorders();

        for (int i = 0; i < borders.size(); i++) {
            FilterRepresentation filter = borders.get(i);
            filter.setName(getString(R.string.borders));
            if (i == 0) {
                filter.setName(getString(R.string.none));
            }
        }

        if (mCategoryBordersAdapter != null) {
            mCategoryBordersAdapter.clear();
        }
        mCategoryBordersAdapter = new CategoryAdapter(this);
        for (FilterRepresentation representation : borders) {
            if (representation.getTextId() != 0) {
                representation.setName(getString(representation.getTextId()));
            }
            mCategoryBordersAdapter.add(new CategoryAction(this, representation, CategoryAction.FULL_VIEW));
        }
    }

    public void loadMainPanel() {
        if (findViewById(R.id.main_panel_container) == null) {
            return;
        }
        CategoryMainPanel panel = new CategoryMainPanel();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_panel_container, panel, CategoryMainPanel.FRAGMENT_TAG);
        transaction.commitAllowingStateLoss();
    }

    public void loadEditorPanel(FilterRepresentation representation, final Editor currentEditor) {
        if (representation.getEditorId() == ImageOnlyEditor.ID) {
            currentEditor.reflectCurrentFilter();
            return;
        }
        final int currentId = currentEditor.getID();
        Runnable showEditor = new Runnable() {

            @Override
            public void run() {
                EditorPanel panel = new EditorPanel();
                panel.setEditor(currentId);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.remove(getSupportFragmentManager().findFragmentByTag(CategoryMainPanel.FRAGMENT_TAG));
                transaction.replace(R.id.main_panel_container, panel, CategoryMainPanel.FRAGMENT_TAG);
                transaction.commit();
            }
        };
        showEditor.run();
    }

    private void fillLooks() {
        FiltersManager filtersManager = FiltersManager.getManager();
        ArrayList<FilterRepresentation> filtersRepresentations = filtersManager.getLooks();

        if (mCategoryLooksAdapter != null) {
            mCategoryLooksAdapter.clear();
        }
        mCategoryLooksAdapter = new CategoryAdapter(this);
        for (FilterRepresentation representation : filtersRepresentations) {
            mCategoryLooksAdapter.add(new CategoryAction(this, representation, CategoryAction.FULL_VIEW));
        }

        Fragment panel = getSupportFragmentManager().findFragmentByTag(CategoryMainPanel.FRAGMENT_TAG);
        if (panel != null) {
            if (panel instanceof CategoryMainPanel) {
                CategoryMainPanel mainPanel = (CategoryMainPanel) panel;
                mainPanel.loadCategoryLookPanel(true);
            }
        }
    }

    public void removeLook(CategoryAction action) {
        FilterUserPresetRepresentation rep = (FilterUserPresetRepresentation) action.getRepresentation();
        if (rep == null) {
            return;
        }
    }

    public void setDefaultPreset() {
        // Default preset (original)
        ImagePreset preset = new ImagePreset(); // empty
        mMasterImage.setPreset(preset, preset.getLastRepresentation(), true);
    }

    private void fillEffects() {
        FiltersManager filtersManager = FiltersManager.getManager();
        ArrayList<FilterRepresentation> filtersRepresentations = filtersManager.getEffects();
        if (mCategoryFiltersAdapter != null) {
            mCategoryFiltersAdapter.clear();
        }
        mCategoryFiltersAdapter = new CategoryAdapter(this);
        for (FilterRepresentation representation : filtersRepresentations) {
            if (representation.getTextId() != 0) {
                representation.setName(getString(representation.getTextId()));
            }
            mCategoryFiltersAdapter.add(new CategoryAction(this, representation));
        }
    }

    private void fillTools() {
        FiltersManager filtersManager = FiltersManager.getManager();
        ArrayList<FilterRepresentation> filtersRepresentations = filtersManager.getTools();
        if (mCategoryGeometryAdapter != null) {
            mCategoryGeometryAdapter.clear();
        }
        mCategoryGeometryAdapter = new CategoryAdapter(this);
        boolean found = false;
        for (FilterRepresentation representation : filtersRepresentations) {
            mCategoryGeometryAdapter.add(new CategoryAction(this, representation));
            if (representation instanceof FilterDrawRepresentation) {
                found = true;
            }
        }
        if (!found) {
            FilterRepresentation representation = new FilterDrawRepresentation(0);
            CategoryAction action = new CategoryAction(this, representation);
            action.setIsDoubleAction(true);
            mCategoryGeometryAdapter.add(action);
        }
    }

    private void setupEditors() {
        mEditorPlaceHolder.setContainer((FrameLayout) findViewById(R.id.editorContainer));
        EditorManager.addEditors(mEditorPlaceHolder);
        mEditorPlaceHolder.setOldViews(mImageViews);
        mEditorPlaceHolder.hide();
    }

    private void setDefaultValues() {
        Resources res = getResources();
        // TODO: get those values from XML.
        FramedTextButton.setTextSize((int) getPixelsFromDip(14));
        FramedTextButton.setTrianglePadding((int) getPixelsFromDip(4));
        FramedTextButton.setTriangleSize((int) getPixelsFromDip(10));

        Drawable curveHandle = res.getDrawable(R.drawable.camera_crop);
        int curveHandleSize = (int) res.getDimension(R.dimen.crop_indicator_size);
        Spline.setCurveHandle(curveHandle, curveHandleSize);
        Spline.setCurveWidth((int) getPixelsFromDip(3));

        mOriginalImageUri = null;
    }

    private void processIntent() {
        Intent intent = getIntent();
        mAction = intent.getAction();
        mSelectedImageUri = intent.getData();
        Uri loadUri = mSelectedImageUri;
        if (mOriginalImageUri != null) {
            loadUri = mOriginalImageUri;
        }
        if (loadUri != null) {
            startLoadBitmap(loadUri);
        } else {
            pickImage();
        }
    }

    private void startLoadBitmap(Uri uri) {
        final View imageShow = findViewById(R.id.imageShow);
        imageShow.setVisibility(View.INVISIBLE);
        startLoadingIndicator();
        mShowingTinyPlanet = false;
        mLoadBitmapTask = new LoadBitmapTask();
        mLoadBitmapTask.execute(uri);
    }

    public CategoryAdapter getCategoryLooksAdapter() {
        return mCategoryLooksAdapter;
    }

    public CategoryAdapter getCategoryBordersAdapter() {
        return mCategoryBordersAdapter;
    }

    public CategoryAdapter getCategoryGeometryAdapter() {
        return mCategoryGeometryAdapter;
    }

    public CategoryAdapter getCategoryFiltersAdapter() {
        return mCategoryFiltersAdapter;
    }

    public CategoryAdapter getCategoryVersionsAdapter() {
        return mCategoryVersionsAdapter;
    }

    public void removeFilterRepresentation(FilterRepresentation filterRepresentation) {
        if (filterRepresentation == null) {
            return;
        }
        ImagePreset oldPreset = MasterImage.getImage().getPreset();
        ImagePreset copy = new ImagePreset(oldPreset);
        copy.removeFilter(filterRepresentation);
        MasterImage.getImage().setPreset(copy, copy.getLastRepresentation(), true);
        if (MasterImage.getImage().getCurrentFilterRepresentation() == filterRepresentation) {
            FilterRepresentation lastRepresentation = copy.getLastRepresentation();
            MasterImage.getImage().setCurrentFilterRepresentation(lastRepresentation);
        }
    }

    public void useFilterRepresentation(FilterRepresentation filterRepresentation) {
        if (filterRepresentation == null) {
            return;
        }
        if (!(filterRepresentation instanceof FilterRotateRepresentation)
                && !(filterRepresentation instanceof FilterMirrorRepresentation)
                && MasterImage.getImage().getCurrentFilterRepresentation() == filterRepresentation) {
            return;
        }
        if (filterRepresentation instanceof FilterUserPresetRepresentation
                || filterRepresentation instanceof FilterRotateRepresentation
                || filterRepresentation instanceof FilterMirrorRepresentation) {
            MasterImage.getImage().onNewLook(filterRepresentation);
        }
        ImagePreset oldPreset = MasterImage.getImage().getPreset();
        ImagePreset copy = new ImagePreset(oldPreset);
        FilterRepresentation representation = copy.getRepresentation(filterRepresentation);
        if (representation == null) {
            filterRepresentation = filterRepresentation.copy();
            copy.addFilter(filterRepresentation);
        } else {
            if (filterRepresentation.allowsSingleInstanceOnly()) {
                // Don't just update the filter representation. Centralize the
                // logic in the addFilter(), such that we can keep "None" as null.
                if (!representation.equals(filterRepresentation)) {
                    // Only do this if the filter isn't the same (state panel clicks can lead us here)
                    copy.removeFilter(representation);
                    copy.addFilter(filterRepresentation);
                }
            }
        }
        MasterImage.getImage().setPreset(copy, filterRepresentation, true);
        MasterImage.getImage().setCurrentFilterRepresentation(filterRepresentation);
    }

    public void showRepresentation(FilterRepresentation representation) {
        if (representation == null) {
            return;
        }

        if (representation instanceof FilterRotateRepresentation) {
            FilterRotateRepresentation r = (FilterRotateRepresentation) representation;
            r.rotateCW();
        } else if (representation instanceof FilterMirrorRepresentation) {
            FilterMirrorRepresentation r = (FilterMirrorRepresentation) representation;
            r.cycle();
        }
        if (representation.isBooleanFilter()) {
            ImagePreset preset = MasterImage.getImage().getPreset();
            if (preset.getRepresentation(representation) != null) {
                // remove
                ImagePreset copy = new ImagePreset(preset);
                copy.removeFilter(representation);
                FilterRepresentation filterRepresentation = representation.copy();
                MasterImage.getImage().setPreset(copy, filterRepresentation, true);
                MasterImage.getImage().setCurrentFilterRepresentation(null);
                return;
            }
        }

        useFilterRepresentation(representation);
        // show representation
        if (mCurrentEditor != null) {
            mCurrentEditor.detach();
        }

        LLog.i(TAG, "-------------showEditor3:" + representation.getEditorId() );
        mCurrentEditor = mEditorPlaceHolder.showEditor(representation.getEditorId());
        loadEditorPanel(representation, mCurrentEditor);
    }

    public Editor getEditor(int editorID) {
        return mEditorPlaceHolder.getEditor(editorID);
    }

    public void setCurrentPanel(int currentPanel) {
        mCurrentPanel = currentPanel;
    }

    public int getCurrentPanel() {
        return mCurrentPanel;
    }

    public void updateCategories() {
        if (mMasterImage == null) {
            return;
        }
        ImagePreset preset = mMasterImage.getPreset();
        mCategoryLooksAdapter.reflectImagePreset(preset);
        mCategoryBordersAdapter.reflectImagePreset(preset);
    }

    public View getMainStatePanelContainer(int id) {
        return findViewById(id);
    }

    public ProcessingService getProcessingService() {
        return mBoundService;
    }

    public boolean isSimpleEditAction() {
        return !FILTER_EDIT_ACTION.equalsIgnoreCase(mAction);
    }

    public void onShowMenu(PopupMenu menu) {
        mCurrentMenu = menu;
        menu.setOnDismissListener(this);
    }

    @Override
    public void onDismiss(PopupMenu popupMenu) {
        if (mCurrentMenu == null) {
            return;
        }
        mCurrentMenu.setOnDismissListener(null);
        mCurrentMenu = null;
    }

    private class LoadHighresBitmapTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            MasterImage master = MasterImage.getImage();
            if (master.supportsHighRes()) {
                Rect originalBounds = master.getOriginalBounds();
                int highresPreviewSize = master.getOriginalBitmapLarge().getWidth() * 2;
                if (highresPreviewSize > originalBounds.width()) {
                    highresPreviewSize = originalBounds.width();
                }
                Rect bounds = new Rect();
                Bitmap originalHires = ImageLoader.loadOrientedConstrainedBitmap(master.getUri(),
                        master.getActivity(), highresPreviewSize,
                        master.getOrientation(), bounds);
                LLog.i(TAG, "--------------------------LoadHighresBitmapTask w:" + originalHires.getWidth() + ":" + " h" + originalHires.getHeight()
                        + " size:" + originalHires.getWidth() * 4 * originalHires.getHeight());
                master.setOriginalBounds(bounds);
                master.setOriginalBitmapHighres(originalHires);
                mBoundService.setOriginalBitmapHighres(originalHires);
                master.warnListeners();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Bitmap highresBitmap = MasterImage.getImage().getOriginalBitmapHighres();
            if (highresBitmap != null) {
                float highResPreviewScale = (float) highresBitmap.getWidth() / (float) MasterImage.getImage().getOriginalBounds().width();
                mBoundService.setHighresPreviewScaleFactor(highResPreviewScale);
            }
            MasterImage.getImage().warnListeners();
        }
    }

    public boolean isLoadingVisible() {
        return mLoadingVisible;
    }

    public void startLoadingIndicator() {
        final View loading = findViewById(R.id.loading);
        mLoadingVisible = true;
        loading.setVisibility(View.VISIBLE);
    }

    public void stopLoadingIndicator() {
        final View loading = findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        mLoadingVisible = false;
    }

    private class LoadBitmapTask extends AsyncTask<Uri, Boolean, Boolean> {

        int mBitmapSize;

        public LoadBitmapTask() {
            mBitmapSize = getScreenImageSize();
        }

        @Override
        protected Boolean doInBackground(Uri... params) {
            if (!MasterImage.getImage().loadBitmap(params[0], mBitmapSize)) {
                return false;
            }
            publishProgress(ImageLoader.queryLightCycle360(MasterImage.getImage().getActivity()));
            return true;
        }

        @Override
        protected void onProgressUpdate(Boolean... values) {
            super.onProgressUpdate(values);
            if (isCancelled()) {
                return;
            }
            if (values[0]) {
                mShowingTinyPlanet = true;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            MasterImage.setMaster(mMasterImage);
            if (isCancelled()) {
                return;
            }

            if (!result) {
                if (mOriginalImageUri != null && !mOriginalImageUri.equals(mSelectedImageUri)) {
                    mOriginalImageUri = mSelectedImageUri;
                    mOriginalPreset = null;
                    Toast.makeText(FilterShowActivity.this, R.string.cannot_edit_original, Toast.LENGTH_SHORT).show();
                    startLoadBitmap(mOriginalImageUri);
                } else {
                    cannotLoadImage();
                }
                return;
            }

            if (null == CachingPipeline.getRenderScriptContext()) {
                Log.v(TAG, "RenderScript context destroyed during load");
                return;
            }
            final View imageShow = findViewById(R.id.imageShow);
            imageShow.setVisibility(View.VISIBLE);

            Bitmap largeBitmap = MasterImage.getImage().getOriginalBitmapLarge();
            mBoundService.setOriginalBitmap(largeBitmap);

            float previewScale = (float) largeBitmap.getWidth() / (float) MasterImage.getImage().getOriginalBounds().width();
            mBoundService.setPreviewScaleFactor(previewScale);
            if (!mShowingTinyPlanet) {
                mCategoryFiltersAdapter.removeTinyPlanet();
            }
            mCategoryLooksAdapter.imageLoaded();
            mCategoryBordersAdapter.imageLoaded();
            mCategoryGeometryAdapter.imageLoaded();
            mCategoryFiltersAdapter.imageLoaded();
            mLoadBitmapTask = null;

            MasterImage.getImage().warnListeners();
            loadActions();
            if (mOriginalPreset != null) {
                MasterImage.getImage().setLoadedPreset(mOriginalPreset);
                MasterImage.getImage().setPreset(mOriginalPreset, mOriginalPreset.getLastRepresentation(), true);
                mOriginalPreset = null;
            } else {
                setDefaultPreset();
            }
            MasterImage.getImage().resetGeometryImages(true);
            if (mAction == TINY_PLANET_ACTION) {
                showRepresentation(mCategoryFiltersAdapter.getTinyPlanet());
            }
            LoadHighresBitmapTask highresLoad = new LoadHighresBitmapTask();
            highresLoad.execute();
            MasterImage.getImage().warnListeners();
            super.onPostExecute(result);
        }

    }

    private void clearGalleryBitmapPool() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                LetoolBitmapPool.getInstance().clear();
                return null;
            }
        }.execute();
    }

    public void registerAction(CategoryAction action) {
        if (mActions.contains(action)) {
            return;
        }
        mActions.add(action);
    }

    private void loadActions() {
        for (int i = 0; i < mActions.size(); i++) {
            CategoryAction action = mActions.get(i);
            action.setImageFrame(new Rect(0, 0, 96, 96), 0);
        }
    }

    @Override
    protected void onDestroy() {
        if (mLoadBitmapTask != null) {
            mLoadBitmapTask.cancel(false);
        }
        doUnbindService();
        super.onDestroy();
    }

    // find a more robust way of handling image size selection for high screen densities.
    private int getScreenImageSize() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        return Math.max(outMetrics.heightPixels, outMetrics.widthPixels);
    }

    private void showSavingProgress(String albumName) {
        ProgressDialog progress;
        if (mSavingProgressDialog != null) {
            progress = mSavingProgressDialog.get();
            if (progress != null) {
                progress.show();
                return;
            }
        }
        // Allow cancellation of the saving process
        String progressText;
        if (albumName == null) {
            progressText = getString(R.string.common_save);
        } else {
            progressText = getString(R.string.common_save, albumName);
        }
        progress = ProgressDialog.show(this, "", progressText, true, false);
        mSavingProgressDialog = new WeakReference<ProgressDialog>(progress);
    }

    private void hideSavingProgress() {
        if (mSavingProgressDialog != null) {
            ProgressDialog progress = mSavingProgressDialog.get();
            if (progress != null)
                progress.dismiss();
        }
    }

    public void completeSaveImage(Uri saveUri) {
        if (mSharingImage && mSharedOutputFile != null) {
            // Image saved, we unblock the content provider
            Uri uri = Uri.withAppendedPath(SharedImageProvider.CONTENT_URI, Uri.encode(mSharedOutputFile.getAbsolutePath()));
            ContentValues values = new ContentValues();
            values.put(SharedImageProvider.PREPARE, false);
            getContentResolver().insert(uri, values);
        }
        setResult(RESULT_OK, new Intent().setData(saveUri));
        hideSavingProgress();
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void print() {
        Bitmap bitmap = MasterImage.getImage().getHighresImage();
        PrintHelper printer = new PrintHelper(this);
        printer.printBitmap("ImagePrint", bitmap);
    }

    public void enableSave(boolean enable) {
        if (mSave != null) {
            mSave.setEnabled(enable);
        }
        if (mReset != null) {
            mReset.setEnabled(enable);
        }
    }

    // //////////////////////////////////////////////////////////////////////////////
    // Some utility functions
    // TODO: finish the cleanup.

    public void invalidateViews() {
        for (ImageShow views : mImageViews) {
            views.updateImage();
        }
    }

    public void hideImageViews() {
        for (View view : mImageViews) {
            view.setVisibility(View.GONE);
        }
        mEditorPlaceHolder.hide();
    }

    public void setupMasterImage() {

        HistoryManager historyManager = new HistoryManager();
        MasterImage.reset();
        mMasterImage = MasterImage.getImage();
        mMasterImage.setHistoryManager(historyManager);
        mMasterImage.setActivity(this);

        if (Runtime.getRuntime().maxMemory() > LIMIT_SUPPORTS_HIGHRES) {
            mMasterImage.setSupportsHighRes(true);
        } else {
            mMasterImage.setSupportsHighRes(false);
        }
    }

    void resetHistory() {
        HistoryManager adapter = mMasterImage.getHistory();
        adapter.reset();
        HistoryItem historyItem = adapter.getItem(0);
        ImagePreset original = null;
        if (RESET_TO_LOADED) {
            original = new ImagePreset(historyItem.getImagePreset());
        } else {
            original = new ImagePreset();
        }
        FilterRepresentation rep = null;
        if (historyItem != null) {
            rep = historyItem.getFilterRepresentation();
        }
        mMasterImage.setPreset(original, rep, true);
        invalidateViews();
        backToMain();
    }

    public void showDefaultImageView() {
        mEditorPlaceHolder.hide();
        mImageShow.setVisibility(View.VISIBLE);
        MasterImage.getImage().setCurrentFilter(null);
        MasterImage.getImage().setCurrentFilterRepresentation(null);
    }

    public void backToMain() {
        Fragment currentPanel = getSupportFragmentManager().findFragmentByTag(CategoryMainPanel.FRAGMENT_TAG);
        if (currentPanel instanceof CategoryMainPanel) {
            return;
        }
        loadMainPanel();
        showDefaultImageView();
    }

    @Override
    public void onBackPressed() {
        Fragment currentPanel = getSupportFragmentManager().findFragmentByTag(CategoryMainPanel.FRAGMENT_TAG);
        if (currentPanel instanceof CategoryMainPanel) {
            if (!mImageShow.hasModifications()) {
                done();
            } else {

                View.OnClickListener l = new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (v.getId() == R.id.ok_btn) {
                            saveImage();
                        } else if (v.getId() == R.id.cancel_btn) {
                            done();
                        }
                    }
                };
                final LetoolDialog dlg = new LetoolDialog(this);
                dlg.setTitle(R.string.common_recommend);
                dlg.setOkBtn(R.string.save_and_exit, l, R.drawable.np_common_pressed_left_bg);
                dlg.setCancelBtn(R.string.exit, l, R.drawable.np_common_pressed_right_bg);
                dlg.setMessage(R.string.unsaved);
                dlg.show();
            }
        } else {
            backToMain();
        }
    }

    public void cannotLoadImage() {
        Toast.makeText(this, R.string.cannot_load_image, Toast.LENGTH_SHORT).show();
        finish();
    }

    // //////////////////////////////////////////////////////////////////////////////

    public float getPixelsFromDip(float value) {
        Resources r = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, r.getDisplayMetrics());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mMasterImage.onHistoryItemClick(position);
        invalidateViews();
    }

    public void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), SELECT_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                startLoadBitmap(selectedImageUri);
            }
        }
    }

    public void saveImage() {
        if (mImageShow.hasModifications()) {
            // Get the name of the album, to which the image will be saved
            File saveDir = SaveImage.getFinalSaveDirectory(this, mSelectedImageUri);
            int bucketId = LetoolUtils.getBucketId(saveDir.getPath());
            String albumName = LocalAlbum.getLocalizedName(getResources(), bucketId, null);
            showSavingProgress(albumName);
            mImageShow.saveImage(this, null);
        } else {
            done();
        }
    }

    public void done() {
        hideSavingProgress();
        if (mLoadBitmapTask != null) {
            mLoadBitmapTask.cancel(false);
        }
        finish();
    }

    private void extractXMPData() {
        XMresults res = XmpPresets.extractXMPData(getBaseContext(), mMasterImage, getIntent().getData());
        if (res == null)
            return;
        mOriginalImageUri = res.originalimage;
        mOriginalPreset = res.preset;
    }

    public Uri getSelectedImageUri() {
        return mSelectedImageUri;
    }

    public void setHandlesSwipeForView(View view, float startX, float startY) {
        if (view != null) {
            mHandlingSwipeButton = true;
        } else {
            mHandlingSwipeButton = false;
        }
        mHandledSwipeView = view;
        int[] location = new int[2];
        view.getLocationInWindow(location);
        mSwipeStartX = location[0] + startX;
        mSwipeStartY = location[1] + startY;
    }

    @SuppressLint("NewApi")
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mHandlingSwipeButton) {
            int direction = CategoryView.HORIZONTAL;
            if (mHandledSwipeView instanceof CategoryView) {
                direction = ((CategoryView) mHandledSwipeView).getOrientation();
            }
            if (ev.getActionMasked() == MotionEvent.ACTION_MOVE) {
                float delta = ev.getY() - mSwipeStartY;
                float distance = mHandledSwipeView.getHeight();
                if (direction == CategoryView.VERTICAL) {
                    delta = ev.getX() - mSwipeStartX;
                    if (ApiHelper.AT_LEAST_11) {
                        mHandledSwipeView.setTranslationX(delta);
                    } else {
                        ViewHelper.setTranslationX(mHandledSwipeView, delta);
                    }
                    distance = mHandledSwipeView.getWidth();
                } else {
                    if (ApiHelper.AT_LEAST_11) {
                        mHandledSwipeView.setTranslationY(delta);
                    } else {
                        ViewHelper.setTranslationY(mHandledSwipeView, delta);
                    }
                }
                delta = Math.abs(delta);
                float transparency = Math.min(1, delta / distance);
                if (ApiHelper.AT_LEAST_11) {
                    mHandledSwipeView.setAlpha(1.f - transparency);
                } else {
                    ViewHelper.setAlpha(mHandledSwipeView, 1.f - transparency);
                }
                mHandledSwipeViewLastDelta = delta;
            }
            if (ev.getActionMasked() == MotionEvent.ACTION_CANCEL || ev.getActionMasked() == MotionEvent.ACTION_UP) {

                if (ApiHelper.AT_LEAST_11) {
                    mHandledSwipeView.setTranslationX(0);
                    mHandledSwipeView.setTranslationY(0);
                    mHandledSwipeView.setAlpha(1.f);
                } else {
                    ViewHelper.setTranslationX(mHandledSwipeView, 0);
                    ViewHelper.setTranslationY(mHandledSwipeView, 0);
                    ViewHelper.setAlpha(mHandledSwipeView, 1.f);
                }
                mHandlingSwipeButton = false;
                float distance = mHandledSwipeView.getHeight();
                if (direction == CategoryView.VERTICAL) {
                    distance = mHandledSwipeView.getWidth();
                }
                if (mHandledSwipeViewLastDelta > distance) {
                    ((CategoryView) mHandledSwipeView).delete();
                }
            }
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    public Point mHintTouchPoint = new Point();

    public Point hintTouchPoint(View view) {
        int location[] = new int[2];
        view.getLocationOnScreen(location);
        int x = mHintTouchPoint.x - location[0];
        int y = mHintTouchPoint.y - location[1];
        return new Point(x, y);
    }

    public void startTouchAnimation(View target, float x, float y) {
        final CategorySelected hint = (CategorySelected) findViewById(R.id.categorySelectedIndicator);
        hint.setVisibility(View.VISIBLE);
        int location[] = new int[2];
        target.getLocationOnScreen(location);
        mHintTouchPoint.x = (int) (location[0] + x);
        mHintTouchPoint.y = (int) (location[1] + y);
        hint.setCircleCenter(new Point(location[0] + target.getWidth() / 2, location[1]));
        hint.startWaveAnimator();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_undo: {
                HistoryManager adapter = mMasterImage.getHistory();
                int position = adapter.undo();
                mMasterImage.onHistoryItemClick(position);
                backToMain();
                invalidateViews();
                break;
            }
            case R.id.action_redo: {
                HistoryManager adapter = mMasterImage.getHistory();
                int position = adapter.redo();
                mMasterImage.onHistoryItemClick(position);
                invalidateViews();
                break;
            }
            case R.id.action_reset: {
                resetHistory();
                break;
            }
            case R.id.action_save: {
                saveImage();
                break;
            }
            case android.R.id.home: {
                saveImage();
                break;
            }
            case R.id.action_navi: {
                onBackPressed();
                break;
            }

        }
    }

}
