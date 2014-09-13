package com.xjt.newpic.edit.pipeline;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;

public interface PipelineInterface {
    public String getName();
    public Resources getResources();
    public Allocation getInPixelsAllocation();
    public Allocation getOutPixelsAllocation();
    public boolean prepareRenderscriptAllocations(Bitmap bitmap);
    public RenderScript getRSContext();
}
