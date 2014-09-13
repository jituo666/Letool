package com.xjt.newpic.edit.filters;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.editors.BasicEditor;
import com.xjt.newpic.edit.filters.ScriptC_convolve3x3;

public class ImageFilterSharpen extends ImageFilterRS {
    private static final String SERIALIZATION_NAME = "SHARPEN";
    private static final String TAG = "ImageFilterSharpen";
    private ScriptC_convolve3x3 mScript;

    private FilterBasicRepresentation mParameters;

    public ImageFilterSharpen() {
        mName = "Sharpen";
    }

    public FilterRepresentation getDefaultRepresentation() {
        FilterRepresentation representation = new FilterBasicRepresentation("Sharpen",0, 0, 0, 100);
        representation.setSerializationName(SERIALIZATION_NAME);
        representation.setShowParameterValue(true);
        representation.setFilterClass(ImageFilterSharpen.class);
        representation.setTextId(R.string.sharpness);
        representation.setOverlayId(R.drawable.filtershow_button_colors_sharpen);
        representation.setEditorId(BasicEditor.ID);
        representation.setSupportsPartialRendering(true);
        representation.setSampleResource(R.drawable.effect_sample_36);
        return representation;
    }

    public void useRepresentation(FilterRepresentation representation) {
        FilterBasicRepresentation parameters = (FilterBasicRepresentation) representation;
        mParameters = parameters;
    }

    @Override
    protected void resetAllocations() {
        // nothing to do
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
        if (mScript == null) {
            mScript = new ScriptC_convolve3x3(getRenderScriptContext(), res, R.raw.convolve3x3);
        }
    }

    private void computeKernel() {
        float scaleFactor = getEnvironment().getScaleFactor();
        float p1 = mParameters.getValue() * scaleFactor;
        float value = p1 / 100.0f;
        float f[] = new float[9];
        float p = value;
        f[0] = -p;
        f[1] = -p;
        f[2] = -p;
        f[3] = -p;
        f[4] = 8 * p + 1;
        f[5] = -p;
        f[6] = -p;
        f[7] = -p;
        f[8] = -p;
        mScript.set_gCoeffs(f);
    }

    @Override
    protected void bindScriptValues() {
        int w = getInPixelsAllocation().getType().getX();
        int h = getInPixelsAllocation().getType().getY();
        mScript.set_gWidth(w);
        mScript.set_gHeight(h);
    }

    @Override
    protected void runFilter() {
        if (mParameters == null) {
            return;
        }
        computeKernel();
        mScript.set_gIn(getInPixelsAllocation());
        mScript.bind_gPixels(getInPixelsAllocation());
        mScript.forEach_root(getInPixelsAllocation(), getOutPixelsAllocation());
    }

}
