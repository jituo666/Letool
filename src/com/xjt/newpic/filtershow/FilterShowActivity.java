package com.xjt.newpic.filtershow;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
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
import android.support.v4.app.DialogFragment;
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
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.filtershow.cache.ImageLoader;
import com.xjt.newpic.filtershow.category.Action;
import com.xjt.newpic.filtershow.category.CategoryAdapter;
import com.xjt.newpic.filtershow.category.CategorySelected;
import com.xjt.newpic.filtershow.category.CategoryView;
import com.xjt.newpic.filtershow.category.MainPanel;
import com.xjt.newpic.filtershow.data.UserPresetsManager;
import com.xjt.newpic.filtershow.editors.BasicEditor;
import com.xjt.newpic.filtershow.editors.Editor;
import com.xjt.newpic.filtershow.editors.EditorChanSat;
import com.xjt.newpic.filtershow.editors.EditorColorBorder;
import com.xjt.newpic.filtershow.editors.EditorCrop;
import com.xjt.newpic.filtershow.editors.EditorDraw;
import com.xjt.newpic.filtershow.editors.EditorGrad;
import com.xjt.newpic.filtershow.editors.EditorManager;
import com.xjt.newpic.filtershow.editors.EditorMirror;
import com.xjt.newpic.filtershow.editors.EditorPanel;
import com.xjt.newpic.filtershow.editors.EditorRedEye;
import com.xjt.newpic.filtershow.editors.EditorRotate;
import com.xjt.newpic.filtershow.editors.EditorStraighten;
import com.xjt.newpic.filtershow.editors.EditorTinyPlanet;
import com.xjt.newpic.filtershow.editors.ImageOnlyEditor;
import com.xjt.newpic.filtershow.filters.FilterDrawRepresentation;
import com.xjt.newpic.filtershow.filters.FilterMirrorRepresentation;
import com.xjt.newpic.filtershow.filters.FilterRepresentation;
import com.xjt.newpic.filtershow.filters.FilterRotateRepresentation;
import com.xjt.newpic.filtershow.filters.FilterUserPresetRepresentation;
import com.xjt.newpic.filtershow.filters.FiltersManager;
import com.xjt.newpic.filtershow.filters.ImageFilter;
import com.xjt.newpic.filtershow.history.HistoryItem;
import com.xjt.newpic.filtershow.history.HistoryManager;
import com.xjt.newpic.filtershow.imageshow.ImageShow;
import com.xjt.newpic.filtershow.imageshow.MasterImage;
import com.xjt.newpic.filtershow.imageshow.Spline;
import com.xjt.newpic.filtershow.info.InfoPanel;
import com.xjt.newpic.filtershow.pipeline.CachingPipeline;
import com.xjt.newpic.filtershow.pipeline.ImagePreset;
import com.xjt.newpic.filtershow.pipeline.ProcessingService;
import com.xjt.newpic.filtershow.presets.PresetManagementDialog;
import com.xjt.newpic.filtershow.presets.UserPresetsAdapter;
import com.xjt.newpic.filtershow.provider.SharedImageProvider;
import com.xjt.newpic.filtershow.tools.SaveImage;
import com.xjt.newpic.filtershow.tools.XmpPresets;
import com.xjt.newpic.filtershow.tools.XmpPresets.XMresults;
import com.xjt.newpic.filtershow.ui.FramedTextButton;
import com.xjt.newpic.imagedata.utils.LetoolBitmapPool;
import com.xjt.newpic.metadata.source.LocalAlbum;
import com.xjt.newpic.surpport.PopupMenuItem;
import com.xjt.newpic.surpport.PopupMenu;
import com.xjt.newpic.utils.LetoolUtils;
import com.xjt.newpic.view.LetoolTopBar;
import com.xjt.newpic.view.LetoolTopBar.OnActionModeListener;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Vector;

public class FilterShowActivity extends FragmentActivity implements OnItemClickListener, OnActionModeListener,
        PopupMenu.OnDismissListener, PopupMenu.OnMenuItemClickListener {

    private static final String TAG = FilterShowActivity.class.getSimpleName();

    private static final long LIMIT_SUPPORTS_HIGHRES = 134217728; // 128Mb

    public static final String TINY_PLANET_ACTION = "com.android.camera.action.TINY_PLANET";
    public static final String FILTER_EDIT_ACTION = "com.xjt.newpic.edit";

    public static final boolean RESET_TO_LOADED = false;

    private static final int SELECT_PICTURE = 1;

    private static final int POPUP_MENU_RESET = 0;
    private static final int POPUP_MENU_INFO = 1;
    private static final int POPUP_MENU_PRINT = 2;

    private String mAction = "";
    private MasterImage mMasterImage = null;
    private ImageShow mImageShow = null;

    private WeakReference<ProgressDialog> mSavingProgressDialog;
    private EditorPlaceHolder mEditorPlaceHolder = new EditorPlaceHolder(this);
    private Editor mCurrentEditor = null;

    private boolean mShowingTinyPlanet = false;
    private boolean mShowingImageStatePanel = false;
    private boolean mShowingVersionsPanel = false;

    private final Vector<ImageShow> mImageViews = new Vector<ImageShow>();

    private File mSharedOutputFile = null;
    private boolean mSharingImage = false;

    private Uri mOriginalImageUri = null;
    private ImagePreset mOriginalPreset = null;
    private Uri mSelectedImageUri = null;

    private ArrayList<Action> mActions = new ArrayList<Action>();
    private UserPresetsManager mUserPresetsManager = null;
    private UserPresetsAdapter mUserPresetsAdapter = null;
    private CategoryAdapter mCategoryLooksAdapter = null;
    private CategoryAdapter mCategoryBordersAdapter = null;
    private CategoryAdapter mCategoryGeometryAdapter = null;
    private CategoryAdapter mCategoryFiltersAdapter = null;
    private CategoryAdapter mCategoryVersionsAdapter = null;
    private int mCurrentPanel = MainPanel.LOOKS;
    private Vector<FilterUserPresetRepresentation> mVersions = new Vector<FilterUserPresetRepresentation>();
    private int mVersionsCounter = 0;

    private boolean mHandlingSwipeButton = false;
    private View mHandledSwipeView = null;
    private float mHandledSwipeViewLastDelta = 0;
    private float mSwipeStartX = 0;
    private float mSwipeStartY = 0;

    private boolean mIsBound = false;
    private PopupMenu mCurrentMenu = null;
    private boolean mLoadingVisible = true;
    private LetoolTopBar mTopBar;
    private View mAccept;
    private View mMore;

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
        setContentView(R.layout.filtershow_splashscreen);
    }

    public void updateUIAfterServiceStarted() {
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        ImageFilter.setActivityForMemoryToasts(this);
        mUserPresetsManager = new UserPresetsManager(this);
        mUserPresetsAdapter = new UserPresetsAdapter(this);
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
    }

    public void initActionBar() {
        mTopBar = new LetoolTopBar(this, (ViewGroup) findViewById(R.id.letool_top_bar_container));
        mTopBar.setOnActionMode(LetoolTopBar.ACTION_BAR_MODE_IMAGE_EDIT, this);
        mTopBar.setTitleIcon(R.drawable.ic_action_previous_item);
        View operationPanel = mTopBar.getActionPanel();
        ImageView undo = (ImageView) operationPanel.findViewById(R.id.action_undo);
        ImageView redo = (ImageView) operationPanel.findViewById(R.id.action_redo);
        mAccept = operationPanel.findViewById(R.id.action_accept);
        mMore = (ImageView) operationPanel.findViewById(R.id.action_more);
        mMasterImage.getHistory().setMenuItems(undo, redo, redo);
    }

    private void loadXML() {
        setContentView(R.layout.filtershow_activity);
        initActionBar();
        mImageShow = (ImageShow) findViewById(R.id.imageShow);
        mImageViews.add(mImageShow);
        mImageShow.attach();
        setupEditors();
    }

    public void fillCategories() {
        fillLooks();
        loadUserPresets();
        fillBorders();
        fillTools();
        fillEffects();
        fillVersions();
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
            mCategoryBordersAdapter.add(new Action(this, representation, Action.FULL_VIEW));
        }
    }

    public void loadMainPanel() {
        if (findViewById(R.id.main_panel_container) == null) {
            return;
        }
        MainPanel panel = new MainPanel();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_panel_container, panel, MainPanel.FRAGMENT_TAG);
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
                transaction.remove(getSupportFragmentManager().findFragmentByTag(MainPanel.FRAGMENT_TAG));
                transaction.replace(R.id.main_panel_container, panel, MainPanel.FRAGMENT_TAG);
                transaction.commit();
            }
        };
        Fragment main = getSupportFragmentManager().findFragmentByTag(MainPanel.FRAGMENT_TAG);
        boolean doAnimation = false;
        if (mShowingImageStatePanel && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            doAnimation = true;
        }
        if (doAnimation && main != null && main instanceof MainPanel) {
            //            MainPanel mainPanel = (MainPanel) main;
            //            View container = mainPanel.getView().findViewById(R.id.category_panel_container);
            //            View bottom = mainPanel.getView().findViewById(R.id.bottom_panel);
            //            int panelHeight = container.getHeight() + bottom.getHeight();
            //            ViewPropertyAnimator anim = mainPanel.getView().animate();
            //            anim.translationY(panelHeight).start();
            //            final Handler handler = new Handler();
            //            handler.postDelayed(showEditor, 0);
        } else {
            showEditor.run();
        }
    }

    private void fillVersions() {
        if (mCategoryVersionsAdapter != null) {
            mCategoryVersionsAdapter.clear();
        }
        mCategoryVersionsAdapter = new CategoryAdapter(this);
        mCategoryVersionsAdapter.setShowAddButton(true);
    }

    public void updateVersions() {
        mCategoryVersionsAdapter.clear();
        FilterUserPresetRepresentation originalRep = new FilterUserPresetRepresentation(
                getString(R.string.filtershow_version_original), new ImagePreset(), -1);
        mCategoryVersionsAdapter.add(new Action(this, originalRep, Action.FULL_VIEW));
        ImagePreset current = new ImagePreset(MasterImage.getImage().getPreset());
        FilterUserPresetRepresentation currentRep = new FilterUserPresetRepresentation(
                getString(R.string.filtershow_version_current), current, -1);
        mCategoryVersionsAdapter.add(new Action(this, currentRep, Action.FULL_VIEW));
        if (mVersions.size() > 0) {
            mCategoryVersionsAdapter.add(new Action(this, Action.SPACER));
        }
        for (FilterUserPresetRepresentation rep : mVersions) {
            mCategoryVersionsAdapter.add(new Action(this, rep, Action.FULL_VIEW, true));
        }
        mCategoryVersionsAdapter.notifyDataSetInvalidated();
    }

    public void addCurrentVersion() {
        ImagePreset current = new ImagePreset(MasterImage.getImage().getPreset());
        mVersionsCounter++;
        FilterUserPresetRepresentation rep = new FilterUserPresetRepresentation("" + mVersionsCounter, current, -1);
        mVersions.add(rep);
        updateVersions();
    }

    public void removeVersion(Action action) {
        mVersions.remove(action.getRepresentation());
        updateVersions();
    }

    private void fillLooks() {
        FiltersManager filtersManager = FiltersManager.getManager();
        ArrayList<FilterRepresentation> filtersRepresentations = filtersManager.getLooks();

        if (mCategoryLooksAdapter != null) {
            mCategoryLooksAdapter.clear();
        }
        mCategoryLooksAdapter = new CategoryAdapter(this);
        int verticalItemHeight = (int) getResources().getDimension(R.dimen.action_item_height);
        mCategoryLooksAdapter.setItemHeight(verticalItemHeight);
        for (FilterRepresentation representation : filtersRepresentations) {
            mCategoryLooksAdapter.add(new Action(this, representation, Action.FULL_VIEW));
        }
        if (mUserPresetsManager.getRepresentations() == null || mUserPresetsManager.getRepresentations().size() == 0) {
            mCategoryLooksAdapter.add(new Action(this, Action.ADD_ACTION));
        }

        Fragment panel = getSupportFragmentManager().findFragmentByTag(MainPanel.FRAGMENT_TAG);
        if (panel != null) {
            if (panel instanceof MainPanel) {
                MainPanel mainPanel = (MainPanel) panel;
                mainPanel.loadCategoryLookPanel(true);
            }
        }
    }

    public void removeLook(Action action) {
        FilterUserPresetRepresentation rep = (FilterUserPresetRepresentation) action.getRepresentation();
        if (rep == null) {
            return;
        }
        mUserPresetsManager.delete(rep.getId());
        updateUserPresetsFromManager();
    }

    public void setDefaultPreset() {
        // Default preset (original)
        ImagePreset preset = new ImagePreset(); // empty
        mMasterImage.setPreset(preset, preset.getLastRepresentation(), true);
    }

    public void addNewPreset() {
        DialogFragment dialog = new PresetManagementDialog();
        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    public void updateUserPresetsFromAdapter(UserPresetsAdapter adapter) {
        ArrayList<FilterUserPresetRepresentation> representations = adapter.getDeletedRepresentations();
        for (FilterUserPresetRepresentation representation : representations) {
            deletePreset(representation.getId());
        }
        ArrayList<FilterUserPresetRepresentation> changedRepresentations = adapter.getChangedRepresentations();
        for (FilterUserPresetRepresentation representation : changedRepresentations) {
            updatePreset(representation);
        }
        adapter.clearDeletedRepresentations();
        adapter.clearChangedRepresentations();
        loadUserPresets();
    }

    public void loadUserPresets() {
        mUserPresetsManager.load();
        updateUserPresetsFromManager();
    }

    public void updateUserPresetsFromManager() {
        ArrayList<FilterUserPresetRepresentation> presets = mUserPresetsManager.getRepresentations();
        if (presets == null) {
            return;
        }
        if (mCategoryLooksAdapter != null) {
            fillLooks();
        }
        if (presets.size() > 0) {
            mCategoryLooksAdapter.add(new Action(this, Action.SPACER));
        }
        mUserPresetsAdapter.clear();
        for (int i = 0; i < presets.size(); i++) {
            FilterUserPresetRepresentation representation = presets.get(i);
            mCategoryLooksAdapter.add(new Action(this, representation, Action.FULL_VIEW, true));
            mUserPresetsAdapter.add(new Action(this, representation, Action.FULL_VIEW));
        }
        if (presets.size() > 0) {
            mCategoryLooksAdapter.add(new Action(this, Action.ADD_ACTION));
        }
        mCategoryLooksAdapter.notifyDataSetChanged();
        mCategoryLooksAdapter.notifyDataSetInvalidated();
    }

    public void saveCurrentImagePreset(String name) {
        mUserPresetsManager.save(MasterImage.getImage().getPreset(), name);
    }

    private void deletePreset(int id) {
        mUserPresetsManager.delete(id);
    }

    private void updatePreset(FilterUserPresetRepresentation representation) {
        mUserPresetsManager.update(representation);
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
            mCategoryFiltersAdapter.add(new Action(this, representation));
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
            mCategoryGeometryAdapter.add(new Action(this, representation));
            if (representation instanceof FilterDrawRepresentation) {
                found = true;
            }
        }
        if (!found) {
            FilterRepresentation representation = new FilterDrawRepresentation();
            Action action = new Action(this, representation);
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

    public UserPresetsAdapter getUserPresetsAdapter() {
        return mUserPresetsAdapter;
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

    public boolean isShowingImageStatePanel() {
        return mShowingImageStatePanel;
    }

    public ProcessingService getProcessingService() {
        return mBoundService;
    }

    public boolean isSimpleEditAction() {
        return !FILTER_EDIT_ACTION.equalsIgnoreCase(mAction);
    }

    //pop up menu callback
    private void showPopupMenu() {
        PopupMenu popup = new PopupMenu(this, mMore);
        popup.setOnMenuItemClickListener(this);
        popup.add(POPUP_MENU_RESET, R.string.reset);
        popup.add(POPUP_MENU_INFO, R.string.filtershow_show_info_panel);
        popup.add(POPUP_MENU_PRINT, R.string.print_image);
        popup.show();
        onShowMenu(popup);
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

    public void toggleInformationPanel() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);

        InfoPanel panel = new InfoPanel();
        panel.show(transaction, InfoPanel.FRAGMENT_TAG);
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

    public void registerAction(Action action) {
        if (mActions.contains(action)) {
            return;
        }
        mActions.add(action);
    }

    private void loadActions() {
        for (int i = 0; i < mActions.size(); i++) {
            Action action = mActions.get(i);
            action.setImageFrame(new Rect(0, 0, 96, 96), 0);
        }
    }

    @Override
    protected void onDestroy() {
        if (mLoadBitmapTask != null) {
            mLoadBitmapTask.cancel(false);
        }
        mUserPresetsManager.close();
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
            Uri uri = Uri.withAppendedPath(SharedImageProvider.CONTENT_URI,
                    Uri.encode(mSharedOutputFile.getAbsolutePath()));
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
        if (mAccept != null) {
            mAccept.setEnabled(enable);
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

    public void toggleVersionsPanel() {
        mShowingVersionsPanel = !mShowingVersionsPanel;
        Fragment panel = getSupportFragmentManager().findFragmentByTag(MainPanel.FRAGMENT_TAG);
        if (panel != null && panel instanceof MainPanel) {
            MainPanel mainPanel = (MainPanel) panel;
            mainPanel.loadCategoryVersionsPanel();
        }
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
        Fragment currentPanel = getSupportFragmentManager().findFragmentByTag(MainPanel.FRAGMENT_TAG);
        if (currentPanel instanceof MainPanel) {
            return;
        }
        loadMainPanel();
        showDefaultImageView();
    }

    @Override
    public void onBackPressed() {
        Fragment currentPanel = getSupportFragmentManager().findFragmentByTag(MainPanel.FRAGMENT_TAG);
        if (currentPanel instanceof MainPanel) {
            if (!mImageShow.hasModifications()) {
                done();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.unsaved).setTitle(R.string.save_before_exit);
                builder.setPositiveButton(R.string.save_and_exit, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        saveImage();
                    }
                });
                builder.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        done();
                    }
                });
                builder.show();
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
                    ViewHelper.setTranslationX(mHandledSwipeView, delta);
                    distance = mHandledSwipeView.getWidth();
                } else {
                    ViewHelper.setTranslationY(mHandledSwipeView, delta);
                }
                delta = Math.abs(delta);
                float transparency = Math.min(1, delta / distance);

                ViewHelper.setAlpha(mHandledSwipeView, 1.f - transparency);
                mHandledSwipeViewLastDelta = delta;
            }
            if (ev.getActionMasked() == MotionEvent.ACTION_CANCEL || ev.getActionMasked() == MotionEvent.ACTION_UP) {

                ViewHelper.setTranslationX(mHandledSwipeView, 0);
                ViewHelper.setTranslationY(mHandledSwipeView, 0);
                ViewHelper.setAlpha(mHandledSwipeView, 1.f);
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
            case R.id.action_accept: {
                saveImage();
                break;
            }
            case R.id.action_more: {
                showPopupMenu();
                break;
            }
            case android.R.id.home: {
                saveImage();
                break;
            }
        }
    }

    @Override
    public boolean onMenuItemClick(PopupMenuItem item) {
        switch (item.getItemId()) {
            case POPUP_MENU_RESET: {
                resetHistory();
                return true;
            }
            case POPUP_MENU_INFO: {
                toggleInformationPanel();
                return true;
            }
            case POPUP_MENU_PRINT: {
                print();
                return true;
            }
        }
        return false;
    }

}
