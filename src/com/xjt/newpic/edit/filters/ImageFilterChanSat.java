
package com.xjt.newpic.edit.filters;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.Script.LaunchOptions;
import android.support.v8.renderscript.Type;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.pipeline.FilterEnvironment;
import com.xjt.newpic.edit.filters.ScriptC_saturation;

public class ImageFilterChanSat extends ImageFilterRS {

    private static final String TAG = ImageFilterChanSat.class.getSimpleName();
    private ScriptC_saturation mScript;
    private Bitmap mSourceBitmap;

    private static final int STRIP_SIZE = 64;

    FilterChanSatRepresentation mParameters = new FilterChanSatRepresentation(0);

    public ImageFilterChanSat() {
        mName = "ChannelSat";
    }

    @Override
    public FilterRepresentation getDefaultRepresentation() {
        return new FilterChanSatRepresentation(R.drawable.effect_sample_39);
    }

    @Override
    public void useRepresentation(FilterRepresentation representation) {
        mParameters = (FilterChanSatRepresentation) representation;
    }

    @Override
    protected void resetAllocations() {

    }

    @Override
    public void resetScripts() {
        if (mScript != null) {
            mScript.destroy();
            mScript = null;
        }
    }

    @Override
    protected void createFilter(android.content.res.Resources res, float scaleFactor,
            int quality) {
        createFilter(res, scaleFactor, quality, getInPixelsAllocation());
    }

    @Override
    protected void createFilter(android.content.res.Resources res, float scaleFactor, int quality, Allocation in) {
        RenderScript rsCtx = getRenderScriptContext();

        Type.Builder tb_float = new Type.Builder(rsCtx, Element.F32_4(rsCtx));
        tb_float.setX(in.getType().getX());
        tb_float.setY(in.getType().getY());
        mScript = new ScriptC_saturation(rsCtx, res, R.raw.saturation);
    }

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
        if (SIMPLE_ICONS && FilterEnvironment.QUALITY_ICON == quality) {
            return bitmap;
        }

        mSourceBitmap = bitmap;
        Bitmap ret = super.apply(bitmap, scaleFactor, quality);
        mSourceBitmap = null;

        return ret;
    }

    @Override
    protected void bindScriptValues() {

    }

    @Override
    protected void runFilter() {
        int[] sat = new int[7];
        for (int i = 0; i < sat.length; i++) {
            sat[i] = mParameters.getValue(i);
        }

        int width = getInPixelsAllocation().getType().getX();
        int height = getInPixelsAllocation().getType().getY();
        Matrix m = getOriginalToScreenMatrix(width, height);

        mScript.set_saturation(sat);

        mScript.invoke_setupGradParams();
        runSelectiveAdjust(
                getInPixelsAllocation(), getOutPixelsAllocation());

    }

    private void runSelectiveAdjust(Allocation in, Allocation out) {
        int width = in.getType().getX();
        int height = in.getType().getY();

        LaunchOptions options = new LaunchOptions();
        int ty;
        options.setX(0, width);

        for (ty = 0; ty < height; ty += STRIP_SIZE) {
            int endy = ty + STRIP_SIZE;
            if (endy > height) {
                endy = height;
            }
            options.setY(ty, endy);
            mScript.forEach_selectiveAdjust(in, out, options);
            if (checkStop()) {
                return;
            }
        }
    }

    private boolean checkStop() {
        RenderScript rsCtx = getRenderScriptContext();
        rsCtx.finish();
        if (getEnvironment().needsStop()) {
            return true;
        }
        return false;
    }
}
