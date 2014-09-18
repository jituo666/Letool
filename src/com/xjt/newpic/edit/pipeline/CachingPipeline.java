package com.xjt.newpic.edit.pipeline;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;

import com.xjt.newpic.common.LLog;
import com.xjt.newpic.edit.cache.BitmapCache;
import com.xjt.newpic.edit.cache.ImageLoader;
import com.xjt.newpic.edit.filters.FilterRepresentation;
import com.xjt.newpic.edit.filters.FiltersManager;
import com.xjt.newpic.edit.imageshow.GeometryMathUtils;
import com.xjt.newpic.edit.imageshow.ImageManager;

import java.util.Vector;

public class CachingPipeline implements PipelineInterface {

    private boolean DEBUG = true;

    private static final String TAG = CachingPipeline.class.getSimpleName();

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;

    private static volatile RenderScript sRS = null;

    private FiltersManager mFiltersManager = null;
    private volatile Bitmap mOriginalBitmap = null;
    private volatile Bitmap mResizedOriginalBitmap = null;

    private FilterEnvironment mEnvironment = new FilterEnvironment();
    private CacheProcessing mCachedProcessing = new CacheProcessing();

    private volatile Allocation mOriginalAllocation = null;
    private volatile Allocation mFiltersOnlyOriginalAllocation = null;

    protected volatile Allocation mInPixelsAllocation;
    protected volatile Allocation mOutPixelsAllocation;
    private volatile int mWidth = 0;
    private volatile int mHeight = 0;

    private volatile float mPreviewScaleFactor = 1.0f;
    private volatile float mHighResPreviewScaleFactor = 1.0f;
    private volatile String mName = "";

    public CachingPipeline(FiltersManager filtersManager, String name) {
        mFiltersManager = filtersManager;
        mName = name;
    }

    public static synchronized RenderScript getRenderScriptContext() {
        return sRS;
    }

    public static synchronized void createRenderscriptContext(Context context) {
        if (sRS != null) {
            LLog.w(TAG, "A prior RS context exists when calling setRenderScriptContext");
            destroyRenderScriptContext();
        }
        sRS = RenderScript.create(context);
    }

    public static synchronized void destroyRenderScriptContext() {
        if (sRS != null) {
            sRS.destroy();
        }
        sRS = null;
    }

    public void stop() {
        mEnvironment.setStop(true);
    }

    public synchronized void reset() {
        synchronized (CachingPipeline.class) {
            if (getRenderScriptContext() == null) {
                return;
            }
            mOriginalBitmap = null; // just a reference to the bitmap in ImageLoader
            if (mResizedOriginalBitmap != null) {
                mResizedOriginalBitmap.recycle();
                mResizedOriginalBitmap = null;
            }
            if (mOriginalAllocation != null) {
                mOriginalAllocation.destroy();
                mOriginalAllocation = null;
            }
            if (mFiltersOnlyOriginalAllocation != null) {
                mFiltersOnlyOriginalAllocation.destroy();
                mFiltersOnlyOriginalAllocation = null;
            }
            mPreviewScaleFactor = 1.0f;
            mHighResPreviewScaleFactor = 1.0f;

            destroyPixelAllocations();
        }
    }

    public Resources getResources() {
        return sRS.getApplicationContext().getResources();
    }

    private synchronized void destroyPixelAllocations() {
        if (DEBUG) {
            LLog.v(TAG, "destroyPixelAllocations in " + getName());
        }
        if (mInPixelsAllocation != null) {
            mInPixelsAllocation.destroy();
            mInPixelsAllocation = null;
        }
        if (mOutPixelsAllocation != null) {
            mOutPixelsAllocation.destroy();
            mOutPixelsAllocation = null;
        }
        mWidth = 0;
        mHeight = 0;
    }

    private String getType(RenderingRequest request) {
        if (request.getType() == RenderingRequest.ICON_RENDERING) {
            return "ICON_RENDERING";
        }
        if (request.getType() == RenderingRequest.FILTERS_RENDERING) {
            return "FILTERS_RENDERING";
        }
        if (request.getType() == RenderingRequest.FULL_RENDERING) {
            return "FULL_RENDERING";
        }
        if (request.getType() == RenderingRequest.GEOMETRY_RENDERING) {
            return "GEOMETRY_RENDERING";
        }
        if (request.getType() == RenderingRequest.PARTIAL_RENDERING) {
            return "PARTIAL_RENDERING";
        }
        if (request.getType() == RenderingRequest.HIGHRES_RENDERING) {
            return "HIGHRES_RENDERING";
        }
        return "UNKNOWN TYPE!";
    }

    private void setupEnvironment(ImagePreset preset, boolean highResPreview) {
        mEnvironment.setPipeline(this);
        mEnvironment.setFiltersManager(mFiltersManager);
        mEnvironment.setBitmapCache(ImageManager.getImage().getBitmapCache());
        if (highResPreview) {
            mEnvironment.setScaleFactor(mHighResPreviewScaleFactor);
        } else {
            mEnvironment.setScaleFactor(mPreviewScaleFactor);
        }
        mEnvironment.setQuality(FilterEnvironment.QUALITY_PREVIEW);
        mEnvironment.setImagePreset(preset);
        mEnvironment.setStop(false);
    }

    public void setOriginal(Bitmap bitmap) {
        mOriginalBitmap = bitmap;
        LLog.v(TAG, "setOriginal, size " + bitmap.getWidth() + " x " + bitmap.getHeight());
        ImagePreset preset = ImageManager.getImage().getPreset();
        setupEnvironment(preset, false);
        updateOriginalAllocation(preset);
    }

    private synchronized boolean updateOriginalAllocation(ImagePreset preset) {
        if (preset == null) {
            return false;
        }
        Bitmap originalBitmap = mOriginalBitmap;

        if (originalBitmap == null) {
            return false;
        }

        RenderScript RS = getRenderScriptContext();

        Allocation filtersOnlyOriginalAllocation = mFiltersOnlyOriginalAllocation;
        mFiltersOnlyOriginalAllocation = Allocation.createFromBitmap(RS, originalBitmap,Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        if (filtersOnlyOriginalAllocation != null) {
            filtersOnlyOriginalAllocation.destroy();
        }

        Allocation originalAllocation = mOriginalAllocation;
        mResizedOriginalBitmap = preset.applyGeometry(originalBitmap, mEnvironment);
        LLog.i(TAG, "+++++ccbitmap+++++mResizedOriginalBitmap:" + mResizedOriginalBitmap.getRowBytes() * mResizedOriginalBitmap.getHeight()
                + "::::::" + mResizedOriginalBitmap);
        mOriginalAllocation = Allocation.createFromBitmap(RS, mResizedOriginalBitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        if (originalAllocation != null) {
            originalAllocation.destroy();
        }

        return true;
    }

    public void renderHighres(RenderingRequest request) {
        synchronized (CachingPipeline.class) {
            if (getRenderScriptContext() == null) {
                return;
            }
            ImagePreset preset = request.getImagePreset();
            setupEnvironment(preset, false);
            Bitmap bitmap = ImageManager.getImage().getOriginalBitmapHighres();//
            if (bitmap == null) {
                return;
            }
            bitmap = mEnvironment.getBitmapCopy(bitmap, BitmapCache.HIGHRES);
            bitmap = preset.applyGeometry(bitmap, mEnvironment);

            mEnvironment.setQuality(FilterEnvironment.QUALITY_PREVIEW);
            Bitmap bmp = preset.apply(bitmap, mEnvironment);
            if (!mEnvironment.needsStop()) {
                request.setBitmap(bmp);
            } else {
                mEnvironment.cache(bmp);
            }
            mFiltersManager.freeFilterResources(preset);
        }
    }

    public void renderGeometry(RenderingRequest request) {
        synchronized (CachingPipeline.class) {
            if (getRenderScriptContext() == null) {
                return;
            }
            ImagePreset preset = request.getImagePreset();
            setupEnvironment(preset, false);
            Bitmap bitmap = ImageManager.getImage().getOriginalBitmapHighres(); //
            if (bitmap == null) {
                return;
            }
            bitmap = mEnvironment.getBitmapCopy(bitmap, BitmapCache.GEOMETRY);
            bitmap = preset.applyGeometry(bitmap, mEnvironment);
            if (!mEnvironment.needsStop()) {
                request.setBitmap(bitmap);
            } else {
                mEnvironment.cache(bitmap);
            }
            mFiltersManager.freeFilterResources(preset);
        }
    }

    public void renderFilters(RenderingRequest request) {
        synchronized (CachingPipeline.class) {
            if (getRenderScriptContext() == null) {
                return;
            }
            ImagePreset preset = request.getImagePreset();
            setupEnvironment(preset, false);
            Bitmap bitmap = ImageManager.getImage().getOriginalBitmapHighres(); //
            if (bitmap == null) {
                return;
            }
            bitmap = mEnvironment.getBitmapCopy(bitmap, BitmapCache.FILTERS);
            bitmap = preset.apply(bitmap, mEnvironment);
            if (!mEnvironment.needsStop()) {
                request.setBitmap(bitmap);
            } else {
                mEnvironment.cache(bitmap);
            }
            mFiltersManager.freeFilterResources(preset);
        }
    }

    public synchronized void render(RenderingRequest request) {
        // TODO: cleanup/remove GEOMETRY / FILTERS paths
        synchronized (CachingPipeline.class) {
            if (getRenderScriptContext() == null) {
                return;
            }
            if ((request.getType() != RenderingRequest.PARTIAL_RENDERING
                    && request.getType() != RenderingRequest.ICON_RENDERING
                    && request.getBitmap() == null) || request.getImagePreset() == null) {
                return;
            }

            LLog.v(TAG, "render image of type " + getType(request));

            Bitmap bitmap = request.getBitmap();
            ImagePreset preset = request.getImagePreset();
            setupEnvironment(preset, true);
            mFiltersManager.freeFilterResources(preset);

            if (request.getType() == RenderingRequest.PARTIAL_RENDERING) {
                ImageManager master = ImageManager.getImage();
                bitmap = ImageLoader.getScaleOneImageForPreset(master.getActivity(),
                        mEnvironment.getBimapCache(), master.getUri(), request.getBounds(), request.getDestination());
                if (bitmap == null) {
                    LLog.w(TAG, "could not get bitmap for: " + getType(request));
                    return;
                }
            }

            if (request.getType() == RenderingRequest.FULL_RENDERING
                    || request.getType() == RenderingRequest.GEOMETRY_RENDERING
                    || request.getType() == RenderingRequest.FILTERS_RENDERING) {
                updateOriginalAllocation(preset);
            }

            if (DEBUG && bitmap != null && mResizedOriginalBitmap != null) {
                LLog.v(TAG, "after update, req bitmap (" + bitmap.getWidth() + "x" + bitmap.getHeight()
                        + " ? resizeOriginal (" + mResizedOriginalBitmap.getWidth() + "x"
                        + mResizedOriginalBitmap.getHeight());
            }

            if (request.getType() == RenderingRequest.FULL_RENDERING || request.getType() == RenderingRequest.GEOMETRY_RENDERING) {
                mOriginalAllocation.copyTo(bitmap);
            } else if (request.getType() == RenderingRequest.FILTERS_RENDERING) {
                mFiltersOnlyOriginalAllocation.copyTo(bitmap);
            }

            if (request.getType() == RenderingRequest.FULL_RENDERING
                    || request.getType() == RenderingRequest.FILTERS_RENDERING
                    || request.getType() == RenderingRequest.ICON_RENDERING
                    || request.getType() == RenderingRequest.PARTIAL_RENDERING
                    || request.getType() == RenderingRequest.STYLE_ICON_RENDERING) {

                if (request.getType() == RenderingRequest.ICON_RENDERING) {
                    mEnvironment.setQuality(FilterEnvironment.QUALITY_ICON);
                } else {
                    mEnvironment.setQuality(FilterEnvironment.QUALITY_PREVIEW);
                }

                if (request.getType() == RenderingRequest.ICON_RENDERING) {
                    Rect iconBounds = request.getIconBounds();
                    Bitmap source = ImageManager.getImage().getThumbnailBitmap(); //
                    if (iconBounds.width() > source.getWidth() * 2) {
                        source = ImageManager.getImage().getLargeThumbnailBitmap(); //
                    }
                    bitmap = mEnvironment.getBitmap(iconBounds.width(), iconBounds.height(), BitmapCache.ICON);
                    Canvas canvas = new Canvas(bitmap);
                    Matrix m = new Matrix();
                    float minSize = Math.min(source.getWidth(), source.getHeight());
                    float maxSize = Math.max(iconBounds.width(), iconBounds.height());
                    float scale = maxSize / minSize;
                    m.setScale(scale, scale);
                    float dx = (iconBounds.width() - (source.getWidth() * scale)) / 2.0f;
                    float dy = (iconBounds.height() - (source.getHeight() * scale)) / 2.0f;
                    m.postTranslate(dx, dy);
                    canvas.drawBitmap(source, m, new Paint(Paint.FILTER_BITMAP_FLAG));
                }
                Bitmap bmp = preset.apply(bitmap, mEnvironment);
                if (!mEnvironment.needsStop()) {
                    request.setBitmap(bmp);
                }
                mFiltersManager.freeFilterResources(preset);
            }
        }
    }

    public synchronized void renderImage(ImagePreset preset, Allocation in, Allocation out) {
        synchronized (CachingPipeline.class) {
            if (getRenderScriptContext() == null) {
                return;
            }
            setupEnvironment(preset, false);
            mFiltersManager.freeFilterResources(preset);
            preset.applyFilters(-1, -1, in, out, mEnvironment);
            boolean copyOut = false;
            if (preset.nbFilters() > 0) {
                copyOut = true;
            }
            preset.applyBorder(in, out, copyOut, mEnvironment);
        }
    }

    //
    public synchronized Bitmap renderFinalImage(Bitmap bitmap, ImagePreset preset) {
        synchronized (CachingPipeline.class) {
            if (getRenderScriptContext() == null) {
                return bitmap;
            }
            setupEnvironment(preset, false);
            mEnvironment.setQuality(FilterEnvironment.QUALITY_FINAL);
            mEnvironment.setScaleFactor(1.0f);
            mFiltersManager.freeFilterResources(preset);
            bitmap = preset.applyGeometry(bitmap, mEnvironment);
            bitmap = preset.apply(bitmap, mEnvironment);
            return bitmap;
        }
    }

    public Bitmap renderGeometryIcon(Bitmap bitmap, ImagePreset preset) {
        return GeometryMathUtils.applyGeometryRepresentations(preset.getGeometryFilters(), bitmap);
    }

    public void compute(SharedBuffer buffer, ImagePreset preset, int type) {
        if (getRenderScriptContext() == null) {
            return;
        }
        setupEnvironment(preset, false);
        Vector<FilterRepresentation> filters = preset.getFilters();
        Bitmap result = mCachedProcessing.process(mOriginalBitmap, filters, mEnvironment);
        buffer.setProducer(result);
        mEnvironment.cache(result);
    }

    public boolean needsRepaint() {
        SharedBuffer buffer = ImageManager.getImage().getPreviewBuffer();
        return buffer.checkRepaintNeeded();
    }

    public void setPreviewScaleFactor(float previewScaleFactor) {
        mPreviewScaleFactor = previewScaleFactor;
    }

    public void setHighResPreviewScaleFactor(float highResPreviewScaleFactor) {
        mHighResPreviewScaleFactor = highResPreviewScaleFactor;
    }

    public synchronized boolean isInitialized() {
        return getRenderScriptContext() != null && mOriginalBitmap != null;
    }

    public boolean prepareRenderscriptAllocations(Bitmap bitmap) {
        RenderScript RS = getRenderScriptContext();
        boolean needsUpdate = false;
        if (mOutPixelsAllocation == null || mInPixelsAllocation == null || bitmap.getWidth() != mWidth || bitmap.getHeight() != mHeight) {
            destroyPixelAllocations();
            Bitmap bitmapBuffer = bitmap;
            if (bitmap.getConfig() == null || bitmap.getConfig() != BITMAP_CONFIG) {
                bitmapBuffer = bitmap.copy(BITMAP_CONFIG, true);
            }
            mOutPixelsAllocation = Allocation.createFromBitmap(RS, bitmapBuffer, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
            mInPixelsAllocation = Allocation.createTyped(RS, mOutPixelsAllocation.getType());
            needsUpdate = true;
        }
        if (RS != null) {
            mInPixelsAllocation.copyFrom(bitmap);
        }
        if (bitmap.getWidth() != mWidth || bitmap.getHeight() != mHeight) {
            mWidth = bitmap.getWidth();
            mHeight = bitmap.getHeight();
            needsUpdate = true;
        }
        if (DEBUG) {
            LLog.v(TAG, "prepareRenderscriptAllocations: " + needsUpdate + " in " + getName());
        }
        return needsUpdate;
    }

    public synchronized Allocation getInPixelsAllocation() {
        return mInPixelsAllocation;
    }

    public synchronized Allocation getOutPixelsAllocation() {
        return mOutPixelsAllocation;
    }

    public String getName() {
        return mName;
    }

    public RenderScript getRSContext() {
        return CachingPipeline.getRenderScriptContext();
    }
}
