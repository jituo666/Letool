package com.xjt.newpic.edit.pipeline;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;

import com.xjt.newpic.edit.cache.BitmapCache;
import com.xjt.newpic.edit.imageshow.ImageManager;

public class Buffer {
    private static final String TAG = Buffer.class.getSimpleName();
    private Bitmap mBitmap;
    private Allocation mAllocation;
    private boolean mUseAllocation = false;
    private ImagePreset mPreset;

    public Buffer(Bitmap bitmap) {
        RenderScript rs = CachingPipeline.getRenderScriptContext();
        if (bitmap != null) {
            BitmapCache cache = ImageManager.getImage().getBitmapCache();
            mBitmap = cache.getBitmapCopy(bitmap, BitmapCache.PREVIEW_CACHE);
        }
        if (mUseAllocation) {
            // TODO: recreate the allocation when the RS context changes
            mAllocation = Allocation.createFromBitmap(rs, mBitmap, Allocation.MipmapControl.MIPMAP_NONE,  Allocation.USAGE_SHARED | Allocation.USAGE_SCRIPT);
        }
    }

    public boolean isSameSize(Bitmap bitmap) {
        if (mBitmap == null || bitmap == null) {
            return false;
        }
        if (mBitmap.getWidth() == bitmap.getWidth() && mBitmap.getHeight() == bitmap.getHeight()) {
            return true;
        }
        return false;
    }

    public synchronized void useBitmap(Bitmap bitmap) {
        Canvas canvas = new Canvas(mBitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    public synchronized Bitmap getBitmap() {
        return mBitmap;
    }

    public Allocation getAllocation() {
        return mAllocation;
    }

    public void sync() {
        if (mUseAllocation) {
            mAllocation.copyTo(mBitmap);
        }
    }

    public ImagePreset getPreset() {
        return mPreset;
    }

    public void setPreset(ImagePreset preset) {
        if ((mPreset == null) || (!mPreset.same(preset))) {
            mPreset = new ImagePreset(preset);
        } else {
            mPreset.updateWith(preset);
        }
    }

    public void remove() {
        BitmapCache cache = ImageManager.getImage().getBitmapCache();
        if (cache.cache(mBitmap)) {
            mBitmap = null;
        }
    }
}

