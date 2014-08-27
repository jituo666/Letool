
package com.xjt.newpic.filtershow.imageshow;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;

import com.android.gallery3d.exif.ExifTag;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.filtershow.FilterShowActivity;
import com.xjt.newpic.filtershow.cache.BitmapCache;
import com.xjt.newpic.filtershow.cache.ImageLoader;
import com.xjt.newpic.filtershow.filters.FilterMirrorRepresentation;
import com.xjt.newpic.filtershow.filters.FilterRepresentation;
import com.xjt.newpic.filtershow.filters.FilterRotateRepresentation;
import com.xjt.newpic.filtershow.filters.FilterUserPresetRepresentation;
import com.xjt.newpic.filtershow.filters.ImageFilter;
import com.xjt.newpic.filtershow.history.HistoryItem;
import com.xjt.newpic.filtershow.history.HistoryManager;
import com.xjt.newpic.filtershow.pipeline.Buffer;
import com.xjt.newpic.filtershow.pipeline.ImagePreset;
import com.xjt.newpic.filtershow.pipeline.RenderingRequest;
import com.xjt.newpic.filtershow.pipeline.RenderingRequestCaller;
import com.xjt.newpic.filtershow.pipeline.SharedBuffer;
import com.xjt.newpic.filtershow.pipeline.SharedPreset;

import java.util.List;
import java.util.Vector;

public class MasterImage implements RenderingRequestCaller {

    private static final String TAG = MasterImage.class.getSimpleName();

    private static final boolean DISABLEZOOM = false;
    public static final int SMALL_BITMAP_DIM = 160;
    public static final int MAX_BITMAP_DIM = 900;

    public static final int CIRCLE_ANIMATION = 1;
    public static final int ROTATE_ANIMATION = 2;
    public static final int MIRROR_ANIMATION = 3;

    private static MasterImage sMasterImage = null;

    private FilterShowActivity mActivity = null;

    private ImageFilter mCurrentFilter = null;
    private ImagePreset mPreset = null;
    private ImagePreset mGeometryOnlyPreset = null;
    private ImagePreset mFiltersOnlyPreset = null;
    private ImagePreset mLoadedPreset = null;

    private SharedBuffer mPreviewBuffer = new SharedBuffer();
    private SharedPreset mPreviewPreset = new SharedPreset();

    private boolean mSupportsHighRes = false;

    private Bitmap mOriginalBitmapSmall = null;
    private Bitmap mOriginalBitmapLarge = null;
    private Bitmap mOriginalBitmapHighres = null;

    private Uri mUri = null;
    private int mOrientation;
    private Rect mOriginalBounds;
    private int mZoomOrientation = ImageLoader.ORI_NORMAL;
    private final Vector<ImageShow> mLoadListeners = new Vector<ImageShow>();

    private Bitmap mGeometryOnlyBitmap = null;
    private Bitmap mFiltersOnlyBitmap = null;
    private Bitmap mPartialBitmap = null;
    private Bitmap mHighresBitmap = null;
    private Bitmap mPreviousImage = null;
    private int mShadowMargin = 15; // not scaled, fixed in the asset
    private Rect mPartialBounds = new Rect();

    private ValueAnimator mAnimator = null;
    private float mMaskScale = 1;
    private boolean mOnGoingNewLookAnimation = false;
    private float mAnimRotationValue = 0;
    private float mCurrentAnimRotationStartValue = 0;
    private float mAnimFraction = 0;
    private int mCurrentLookAnimation = 0;

    private HistoryManager mHistory = null;
    private Vector<ImageShow> mObservers = new Vector<ImageShow>();
    private FilterRepresentation mCurrentFilterRepresentation;

    private float mScaleFactor = 1.0f;
    private float mMaxScaleFactor = 3.0f; // TODO: base this on the current view / image
    private Point mTranslation = new Point();
    private Point mOriginalTranslation = new Point();

    private Point mImageShowSize = new Point();

    private boolean mShowsOriginal;
    private List<ExifTag> mEXIF;
    private BitmapCache mBitmapCache = new BitmapCache();

    private Runnable mWarnListenersRunnable = new Runnable() {

        @Override
        public void run() {
            for (int i = 0; i < mLoadListeners.size(); i++) {
                ImageShow imageShow = mLoadListeners.elementAt(i);
                imageShow.imageLoaded();
            }
            invalidatePreview();
        }
    };

    private MasterImage() {

    }

    public static void setMaster(MasterImage master) {
        sMasterImage = master;
    }

    public static MasterImage getImage() {
        if (sMasterImage == null) {
            sMasterImage = new MasterImage();
        }
        return sMasterImage;
    }

    public boolean loadBitmap(Uri uri, int size) {
        setUri(uri);
        mEXIF = ImageLoader.getExif(getActivity(), uri);
        mOrientation = ImageLoader.getMetadataOrientation(mActivity, uri);
        Rect originalBounds = new Rect();
        mOriginalBitmapLarge = ImageLoader.loadOrientedConstrainedBitmap(uri, mActivity, Math.min(MAX_BITMAP_DIM, size), mOrientation, originalBounds);
        LLog.i(TAG, "+++++ccbitmap+++++loading mOriginalBitmapLarge:" + originalBounds
                + " w:" + mOriginalBitmapLarge.getWidth() + " h:" + mOriginalBitmapLarge.getHeight()
                + " size:" + mOriginalBitmapLarge.getWidth() * 4 * mOriginalBitmapLarge.getHeight()
                + "::::::" + mOriginalBitmapLarge);
        setOriginalBounds(originalBounds);
        if (mOriginalBitmapLarge == null) {
            return false;
        }
        int sw = SMALL_BITMAP_DIM;
        int sh = (int) (SMALL_BITMAP_DIM * (float) mOriginalBitmapLarge.getHeight() / mOriginalBitmapLarge.getWidth());
        mOriginalBitmapSmall = Bitmap.createScaledBitmap(mOriginalBitmapLarge, sw, sh, true);
        LLog.i(TAG, "+++++ccbitmap+++++loading  mOriginalBitmapSmall:" + " w:" + mOriginalBitmapSmall.getWidth()
                + " h:" + mOriginalBitmapSmall.getHeight()
                + " size:" + mOriginalBitmapSmall.getWidth() * 4 * mOriginalBitmapSmall.getHeight()
                + "::::::" + mOriginalBitmapSmall);
        mZoomOrientation = mOrientation;
        warnListeners();
        return true;
    }

    public void onNewLook(FilterRepresentation newRepresentation) {
        if (getFilteredImage() == null) {
            return;
        }
        if (mAnimator != null) {
            mAnimator.cancel();
            if (mCurrentLookAnimation == ROTATE_ANIMATION) {
                mCurrentAnimRotationStartValue += 90;
            }
        } else {
            resetAnimBitmap();
            mPreviousImage = mBitmapCache.getBitmapCopy(getFilteredImage(), BitmapCache.NEW_LOOK);
            LLog.i(TAG, "+++++ccbitmap+++++mPreviousImage:" + mPreviousImage.getRowBytes() * mPreviousImage.getHeight()
                    + "::::::" + mPreviousImage);
        }
        if (newRepresentation instanceof FilterUserPresetRepresentation) {
            mCurrentLookAnimation = CIRCLE_ANIMATION;
            mAnimator = ValueAnimator.ofFloat(0, 1);
            mAnimator.setDuration(650);
        } else if (newRepresentation instanceof FilterRotateRepresentation) {
            mCurrentLookAnimation = ROTATE_ANIMATION;
            mAnimator = ValueAnimator.ofFloat(0, 90);
            mAnimator.setDuration(500);
        } else if (newRepresentation instanceof FilterMirrorRepresentation) {
            mCurrentLookAnimation = MIRROR_ANIMATION;
            mAnimator = ValueAnimator.ofFloat(1, 0, -1);
            mAnimator.setDuration(500);
        }
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mCurrentLookAnimation == CIRCLE_ANIMATION) {
                    setMaskScale((Float) animation.getAnimatedValue());
                } else if (mCurrentLookAnimation == ROTATE_ANIMATION
                        || mCurrentLookAnimation == MIRROR_ANIMATION) {
                    setAnimRotation((Float) animation.getAnimatedValue());
                    setAnimFraction(animation.getAnimatedFraction());
                }
            }
        });
        mAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                mOnGoingNewLookAnimation = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mOnGoingNewLookAnimation = false;
                mCurrentAnimRotationStartValue = 0;
                mAnimator = null;
                notifyObservers();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
        notifyObservers();
    }

    public void warnListeners() {
        mActivity.runOnUiThread(mWarnListenersRunnable);
    }

    @Override
    public void available(RenderingRequest request) {
        if (request.getBitmap() == null) {
            return;
        }
        boolean needsCheckModification = false;
        if (request.getType() == RenderingRequest.GEOMETRY_RENDERING) {
            mBitmapCache.cache(mGeometryOnlyBitmap);
            mGeometryOnlyBitmap = request.getBitmap();
            needsCheckModification = true;
            LLog.i(TAG, "+++++ccbitmap+++++GEOMETRY_RENDERING:" + mGeometryOnlyBitmap.getRowBytes() * mGeometryOnlyBitmap.getHeight()
                    + "::::::" + mGeometryOnlyBitmap);
        } else if (request.getType() == RenderingRequest.FILTERS_RENDERING) {
            mBitmapCache.cache(mFiltersOnlyBitmap);
            mFiltersOnlyBitmap = request.getBitmap();
            notifyObservers();
            needsCheckModification = true;
            LLog.i(TAG, "+++++ccbitmap+++++FILTERS_RENDERING:" + mFiltersOnlyBitmap.getRowBytes() * mFiltersOnlyBitmap.getHeight()
                    + "::::::" + mFiltersOnlyBitmap);
        } else if (request.getType() == RenderingRequest.PARTIAL_RENDERING && request.getScaleFactor() == getScaleFactor()) {
            mBitmapCache.cache(mPartialBitmap);
            mPartialBitmap = request.getBitmap();
            mPartialBounds.set(request.getBounds());
            notifyObservers();
            needsCheckModification = true;
            LLog.i(TAG, "+++++ccbitmap+++++PARTIAL_RENDERING:" + mPartialBitmap.getRowBytes() * mPartialBitmap.getHeight()
                    + "::::::" + mPartialBitmap);
        } else if (request.getType() == RenderingRequest.HIGHRES_RENDERING) {
            mBitmapCache.cache(mHighresBitmap);
            mHighresBitmap = request.getBitmap();
            notifyObservers();
            needsCheckModification = true;
            LLog.i(TAG, "+++++ccbitmap+++++HIGHRES_RENDERING:" + mHighresBitmap.getRowBytes() * mHighresBitmap.getHeight()
                    + "::::::" + mHighresBitmap);
        }
        if (needsCheckModification) {
            mActivity.enableSave(hasModifications());
        }
    }

    public Bitmap getOriginalBitmapSmall() {
        return mOriginalBitmapSmall;
    }

    public Bitmap getOriginalBitmapLarge() {
        return mOriginalBitmapLarge;
    }

    public Bitmap getOriginalBitmapHighres() {
        if (mOriginalBitmapHighres == null) {
            return mOriginalBitmapLarge;
        }
        return mOriginalBitmapHighres;
    }

    public void setOriginalBitmapHighres(Bitmap originalBitmapHighres) {
        mOriginalBitmapHighres = originalBitmapHighres;
        LLog.i(TAG,
                "+++++ccbitmap+++++setOriginalBitmapHighres:" + " w:" + mOriginalBitmapHighres.getWidth() + " h:" + mOriginalBitmapHighres.getHeight()
                        + " size:" + mOriginalBitmapHighres.getWidth() * 4 * mOriginalBitmapHighres.getHeight()
                        + ":" + mOriginalBitmapHighres);
    }

    public int getOrientation() {
        return mOrientation;
    }

    public int getZoomOrientation() {
        return mZoomOrientation;
    }

    public void addListener(ImageShow imageShow) {
        if (!mLoadListeners.contains(imageShow)) {
            mLoadListeners.add(imageShow);
        }
    }

    public void addObserver(ImageShow observer) {
        if (mObservers.contains(observer)) {
            return;
        }
        mObservers.add(observer);
    }

    public void removeObserver(ImageShow observer) {
        mObservers.remove(observer);
    }

    public Rect getOriginalBounds() {
        return mOriginalBounds;
    }

    public void setOriginalBounds(Rect r) {
        mOriginalBounds = r;
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(Uri uri) {
        mUri = uri;
    }

    public void setActivity(FilterShowActivity activity) {
        mActivity = activity;
    }

    public FilterShowActivity getActivity() {
        return mActivity;
    }

    public synchronized void setPreset(ImagePreset preset, FilterRepresentation change, boolean addToHistory) {
        preset.showFilters();
        mPreset = preset;
        if (addToHistory) {
            HistoryItem historyItem = new HistoryItem(mPreset, change);
            mHistory.addHistoryItem(historyItem);
        }
        updatePresets(true);
        resetGeometryImages(false);
        mActivity.updateCategories();
    }

    public void setLoadedPreset(ImagePreset preset) {
        mLoadedPreset = preset;
    }

    public void resetGeometryImages(boolean force) {
        if (mPreset == null) {
            return;
        }
        ImagePreset newPresetGeometryOnly = new ImagePreset(mPreset);
        newPresetGeometryOnly.setDoApplyFilters(false);
        newPresetGeometryOnly.setDoApplyGeometry(true);
        if (force || mGeometryOnlyPreset == null || !newPresetGeometryOnly.equals(mGeometryOnlyPreset)) {
            mGeometryOnlyPreset = newPresetGeometryOnly;
            RenderingRequest.post(mActivity, null, mGeometryOnlyPreset, RenderingRequest.GEOMETRY_RENDERING, this);
        }
        ImagePreset newPresetFiltersOnly = new ImagePreset(mPreset);
        newPresetFiltersOnly.setDoApplyFilters(true);
        newPresetFiltersOnly.setDoApplyGeometry(false);
        if (force || mFiltersOnlyPreset == null || !newPresetFiltersOnly.same(mFiltersOnlyPreset)) {
            mFiltersOnlyPreset = newPresetFiltersOnly;
            RenderingRequest.post(mActivity, null, mFiltersOnlyPreset, RenderingRequest.FILTERS_RENDERING, this);
        }
    }

    public void onHistoryItemClick(int position) {
        HistoryItem historyItem = mHistory.getItem(position);
        // We need a copy from the history
        ImagePreset newPreset = new ImagePreset(historyItem.getImagePreset());
        // don't need to add it to the history
        setPreset(newPreset, historyItem.getFilterRepresentation(), false);
        mHistory.setCurrentPreset(position);
    }

    public HistoryManager getHistory() {
        return mHistory;
    }

    public void setHistoryManager(HistoryManager adapter) {
        mHistory = adapter;
    }

    public void setCurrentFilter(ImageFilter filter) {
        mCurrentFilter = filter;
    }

    public ImageFilter getCurrentFilter() {
        return mCurrentFilter;
    }

    public synchronized boolean hasModifications() {
        // TODO: We need to have a better same effects check to see if two
        // presets are functionally the same. Right now, we are relying on a stricter check as equals().
        ImagePreset loadedPreset = getLoadedPreset();
        if (mPreset == null) {
            if (loadedPreset == null) {
                return false;
            } else {
                return loadedPreset.hasModifications();
            }
        } else {
            if (loadedPreset == null) {
                return mPreset.hasModifications();
            } else {
                return !mPreset.equals(loadedPreset);
            }
        }
    }

    public SharedBuffer getPreviewBuffer() {
        return mPreviewBuffer;
    }

    public ImagePreset getCurrentPreset() {
        return getPreviewBuffer().getConsumer().getPreset();
    }

    public synchronized ImagePreset getPreset() {
        return mPreset;
    }

    public ImagePreset getLoadedPreset() {
        return mLoadedPreset;
    }

    public synchronized ImagePreset getGeometryPreset() {
        return mGeometryOnlyPreset;
    }

    public synchronized ImagePreset getFiltersOnlyPreset() {
        return mFiltersOnlyPreset;
    }

    public SharedPreset getPreviewPreset() {
        return mPreviewPreset;
    }


    public Bitmap getFilteredImage() {
        mPreviewBuffer.swapConsumerIfNeeded(); // get latest bitmap
        Buffer consumer = mPreviewBuffer.getConsumer();
        if (consumer != null) {
            return consumer.getBitmap();
        }
        return null;
    }
    
    public Bitmap getFiltersOnlyImage() {
        return mFiltersOnlyBitmap;
    }

    public Bitmap getGeometryOnlyImage() {
        return mGeometryOnlyBitmap;
    }

    public Bitmap getPartialImage() {
        return mPartialBitmap;
    }

    public Bitmap getHighresImage() {
        if (mHighresBitmap == null) {
            return getFilteredImage();
        }
        return mHighresBitmap;
    }

    public Bitmap getPreviousImage() {
        return mPreviousImage;
    }

    public Rect getPartialBounds() {
        return mPartialBounds;
    }

    public boolean onGoingNewLookAnimation() {
        return mOnGoingNewLookAnimation;
    }

    public int getCurrentLookAnimation() {
        return mCurrentLookAnimation;
    }

    public void resetAnimBitmap() {
        mBitmapCache.cache(mPreviousImage);
        mPreviousImage = null;
    }

    public void notifyObservers() {
        for (ImageShow observer : mObservers) {
            observer.invalidate();
        }
    }

    public void updatePresets(boolean force) {
        invalidatePreview();
    }

    public void invalidateFiltersOnly() {
        mFiltersOnlyPreset = null;
        invalidatePreview();
    }

    public void invalidatePartialPreview() {
        if (mPartialBitmap != null) {
            mBitmapCache.cache(mPartialBitmap);
            mPartialBitmap = null;
            notifyObservers();
        }
    }

    public void invalidateHighresPreview() {
        if (mHighresBitmap != null) {
            mBitmapCache.cache(mHighresBitmap);
            mHighresBitmap = null;
            notifyObservers();
        }
    }

    public void invalidatePreview() {
        if (mPreset == null) {
            return;
        }
        LLog.i(TAG, "-------------------------------invalidatePreview :");
        mPreviewPreset.enqueuePreset(mPreset);
        mPreviewBuffer.invalidate();
        invalidatePartialPreview();
        invalidateHighresPreview();
        needsUpdatePartialPreview();
        needsUpdateHighResPreview();
        mActivity.getProcessingService().updatePreviewBuffer();
    }

    public void setImageShowSize(int w, int h) {
        if (mImageShowSize.x != w || mImageShowSize.y != h) {
            mImageShowSize.set(w, h);
            LLog.i(TAG, "-------------------------------setImageShowSize :");
            float maxWidth = mOriginalBounds.width() / (float) w;
            float maxHeight = mOriginalBounds.height() / (float) h;
            mMaxScaleFactor = Math.max(3.f, Math.max(maxWidth, maxHeight));
            needsUpdatePartialPreview();
            needsUpdateHighResPreview();
        }
    }

    public Matrix originalImageToScreen() {
        return computeImageToScreen(null, 0, true);
    }

    public Matrix computeImageToScreen(Bitmap bitmapToDraw, float rotate, boolean applyGeometry) {
        if (getOriginalBounds() == null || mImageShowSize.x == 0 || mImageShowSize.y == 0) {
            return null;
        }

        Matrix m = null;
        float scale = 1f;
        float translateX = 0;
        float translateY = 0;

        if (applyGeometry) {
            GeometryMathUtils.GeometryHolder holder = GeometryMathUtils.unpackGeometry(mPreset.getGeometryFilters());
            m = GeometryMathUtils.getCropSelectionToScreenMatrix(null, holder,
                    getOriginalBounds().width(), getOriginalBounds().height(), mImageShowSize.x, mImageShowSize.y);
        } else if (bitmapToDraw != null) {
            m = new Matrix();
            RectF size = new RectF(0, 0, bitmapToDraw.getWidth(), bitmapToDraw.getHeight());
            scale = mImageShowSize.x / size.width();
            if (size.width() < size.height()) {
                scale = mImageShowSize.y / size.height();
            }
            translateX = (mImageShowSize.x - (size.width() * scale)) / 2.0f;
            translateY = (mImageShowSize.y - (size.height() * scale)) / 2.0f;
        } else {
            return null;
        }

        Point translation = getTranslation();
        m.postScale(scale, scale);
        m.postRotate(rotate, mImageShowSize.x / 2.0f, mImageShowSize.y / 2.0f);
        m.postTranslate(translateX, translateY);
        m.postTranslate(mShadowMargin, mShadowMargin);
        m.postScale(getScaleFactor(), getScaleFactor(), mImageShowSize.x / 2.0f, mImageShowSize.y / 2.0f);
        m.postTranslate(translation.x * getScaleFactor(), translation.y * getScaleFactor());
        return m;
    }

    public FilterRepresentation getCurrentFilterRepresentation() {
        return mCurrentFilterRepresentation;
    }

    public void setCurrentFilterRepresentation(FilterRepresentation currentFilterRepresentation) {
        mCurrentFilterRepresentation = currentFilterRepresentation;
    }

    private Matrix getImageToScreenMatrix(boolean reflectRotation) {
        if (getOriginalBounds() == null || mImageShowSize.x == 0 || mImageShowSize.y == 0) {
            return new Matrix();
        }
        Matrix m = GeometryMathUtils.getImageToScreenMatrix(mPreset.getGeometryFilters(),
                reflectRotation, getOriginalBounds(), mImageShowSize.x, mImageShowSize.y);
        if (m == null) {
            m = new Matrix();
            m.reset();
            return m;
        }
        Point translate = getTranslation();
        float scaleFactor = getScaleFactor();
        m.postTranslate(translate.x, translate.y);
        m.postScale(scaleFactor, scaleFactor, mImageShowSize.x / 2.0f, mImageShowSize.y / 2.0f);
        return m;
    }

    private Matrix getScreenToImageMatrix(boolean reflectRotation) {
        Matrix m = getImageToScreenMatrix(reflectRotation);
        Matrix invert = new Matrix();
        m.invert(invert);
        return invert;
    }

    public void needsUpdateHighResPreview() {
        if (!mSupportsHighRes || mActivity.getProcessingService() == null || mPreset == null) {
            return;
        }
        LLog.i(TAG, "-------------------------------postHighresRenderingRequest scle factor :" + getScaleFactor());
        mActivity.getProcessingService().postHighresRenderingRequest(mPreset, getScaleFactor(), this);
        invalidateHighresPreview();
    }

    public void needsUpdatePartialPreview() {
        if (mPreset == null) {
            return;
        }
        if (!mPreset.canDoPartialRendering()) {
            invalidatePartialPreview();
            return;
        }
        Matrix originalToScreen = MasterImage.getImage().originalImageToScreen();
        if (originalToScreen == null) {
            return;
        }
        Matrix screenToOriginal = new Matrix();
        originalToScreen.invert(screenToOriginal);
        RectF bounds = new RectF(0, 0, mImageShowSize.x + 2 * mShadowMargin, mImageShowSize.y + 2 * mShadowMargin);
        screenToOriginal.mapRect(bounds);
        Rect rBounds = new Rect();
        bounds.roundOut(rBounds);
        LLog.i(TAG, "-------------------------------postFullresRenderingRequest scle factor :" + getScaleFactor());
        mActivity.getProcessingService().postFullresRenderingRequest(mPreset, getScaleFactor(), rBounds,
                new Rect(0, 0, mImageShowSize.x, mImageShowSize.y), this);
        invalidatePartialPreview();
    }

    public static void reset() {
        sMasterImage = null;
    }

    public float getMaskScale() {
        return mMaskScale;
    }

    public void setMaskScale(float scale) {
        mMaskScale = scale;
        notifyObservers();
    }

    public float getAnimRotationValue() {
        return mAnimRotationValue;
    }

    public void setAnimRotation(float rotation) {
        mAnimRotationValue = mCurrentAnimRotationStartValue + rotation;
        notifyObservers();
    }

    public void setAnimFraction(float fraction) {
        mAnimFraction = fraction;
    }

    public float getAnimFraction() {
        return mAnimFraction;
    }

    public Point getTranslation() {
        return mTranslation;
    }

    public void setTranslation(Point translation) {
        if (DISABLEZOOM) {
            mTranslation.x = 0;
            mTranslation.y = 0;
            return;
        }
        LLog.i(TAG, "-------------------------------setTranslation :");
        mTranslation.x = translation.x;
        mTranslation.y = translation.y;
        needsUpdatePartialPreview();
    }

    public void resetTranslation() {
        LLog.i(TAG, "-------------------------------resetTranslation :");
        mTranslation.x = 0;
        mTranslation.y = 0;
        needsUpdatePartialPreview();
    }

    public Point getOriginalTranslation() {
        return mOriginalTranslation;
    }

    public void setOriginalTranslation(Point originalTranslation) {
        if (DISABLEZOOM) {
            return;
        }
        mOriginalTranslation.x = originalTranslation.x;
        mOriginalTranslation.y = originalTranslation.y;
    }

    public float getScaleFactor() {
        return mScaleFactor;
    }

    public void setScaleFactor(float scaleFactor) {
        if (DISABLEZOOM || scaleFactor == mScaleFactor) {
            return;
        }
        mScaleFactor = scaleFactor;
        invalidatePartialPreview();
    }

    public Bitmap getThumbnailBitmap() {
        return getOriginalBitmapSmall();
    }

    public Bitmap getLargeThumbnailBitmap() {
        return getOriginalBitmapLarge();
    }

    public float getMaxScaleFactor() {
        if (DISABLEZOOM) {
            return 1;
        }
        return mMaxScaleFactor;
    }

    public void setMaxScaleFactor(float maxScaleFactor) {
        mMaxScaleFactor = maxScaleFactor;
    }

    public void setSupportsHighRes(boolean value) {
        mSupportsHighRes = value;
    }

    public boolean supportsHighRes() {
        return mSupportsHighRes;
    }

    public void setShowsOriginal(boolean value) {
        mShowsOriginal = value;
        notifyObservers();
    }

    public boolean showsOriginal() {
        return mShowsOriginal;
    }

    public List<ExifTag> getEXIF() {
        return mEXIF;
    }

    public BitmapCache getBitmapCache() {
        return mBitmapCache;
    }

    public boolean hasTinyPlanet() {
        if (mPreset != null)
            return mPreset.contains(FilterRepresentation.TYPE_TINYPLANET);
        else
            return false;
    }
}
