
package com.xjt.newpic.edit.filters;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.xjt.newpic.R;
import com.xjt.newpic.edit.pipeline.ImagePreset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public abstract class BaseFiltersManager implements FiltersManagerInterface {

    private static final String TAG = BaseFiltersManager.class.getSimpleName();
    private static final int FILTER_BODER_DEFAULT_SIZE = 3; // in percent
    private static final int FILTER_BODER_DEFAULT_RADIUS = 3;

    protected HashMap<Class<?>, ImageFilter> mFilters = null;
    protected HashMap<String, FilterRepresentation> mRepresentationLookup = null;

    protected ArrayList<FilterRepresentation> mLooks = new ArrayList<FilterRepresentation>();
    protected ArrayList<FilterRepresentation> mBorders = new ArrayList<FilterRepresentation>();
    protected ArrayList<FilterRepresentation> mTools = new ArrayList<FilterRepresentation>();
    protected ArrayList<FilterRepresentation> mEffects = new ArrayList<FilterRepresentation>();

    protected void init() {
        mFilters = new HashMap<Class<?>, ImageFilter>();
        mRepresentationLookup = new HashMap<String, FilterRepresentation>();
        Vector<Class<?>> filters = new Vector<Class<?>>();
        addFilterClasses(filters);
        for (Class<?> filterClass : filters) {
            try {
                Object filterInstance = filterClass.newInstance();
                if (filterInstance instanceof ImageFilter) {
                    mFilters.put(filterClass, (ImageFilter) filterInstance);
                    FilterRepresentation rep = ((ImageFilter) filterInstance).getDefaultRepresentation();
                    if (rep != null) {
                        addRepresentation(rep);
                    }
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void addRepresentation(FilterRepresentation rep) {
        mRepresentationLookup.put(rep.getSerializationName(), rep);
    }

    public FilterRepresentation createFilterFromName(String name) {
        try {
            return mRepresentationLookup.get(name).copy();
        } catch (Exception e) {
            Log.v(TAG, "unable to generate a filter representation for \"" + name + "\"");
            e.printStackTrace();
        }
        return null;
    }

    public ImageFilter getFilter(Class<?> c) {
        return mFilters.get(c);
    }

    @Override
    public ImageFilter getFilterForRepresentation(FilterRepresentation representation) {
        return mFilters.get(representation.getFilterClass());
    }

    public FilterRepresentation getRepresentation(Class<?> c) {
        ImageFilter filter = mFilters.get(c);
        if (filter != null) {
            return filter.getDefaultRepresentation();
        }
        return null;
    }

    public void freeFilterResources(ImagePreset preset) {
        if (preset == null) {
            return;
        }
        Vector<ImageFilter> usedFilters = preset.getUsedFilters(this);
        for (Class<?> c : mFilters.keySet()) {
            ImageFilter filter = mFilters.get(c);
            if (!usedFilters.contains(filter)) {
                filter.freeResources();
            }
        }
    }

    public void freeRSFilterScripts() {
        for (Class<?> c : mFilters.keySet()) {
            ImageFilter filter = mFilters.get(c);
            if (filter != null && filter instanceof ImageFilterRS) {
                ((ImageFilterRS) filter).resetScripts();
            }
        }
    }

    protected void addFilterClasses(Vector<Class<?>> filters) {
        filters.add(ImageFilterWBalance.class);
        filters.add(ImageFilterExposure.class);
        filters.add(ImageFilterVignette.class);
        filters.add(ImageFilterGrad.class);
        filters.add(ImageFilterContrast.class);
        filters.add(ImageFilterShadows.class);
        filters.add(ImageFilterHighlights.class);
        filters.add(ImageFilterVibrance.class);
        filters.add(ImageFilterSharpen.class);
        filters.add(ImageFilterCurves.class);
        filters.add(ImageFilterHue.class);
        filters.add(ImageFilterChanSat.class);
        filters.add(ImageFilterSaturated.class);
        filters.add(ImageFilterBwFilter.class);
        filters.add(ImageFilterNegative.class);
        filters.add(ImageFilterEdge.class);
        filters.add(ImageFilterKMeans.class);

        filters.add(ImageFilterDraw.class);
        //looks
        filters.add(ImageFilterFx.class);
        //boders
        filters.add(ImageFilterTextureBorder.class);
        filters.add(ImageFilterColorBorder.class);
        filters.add(ImageFilterImageBorder.class);
    }

    public ArrayList<FilterRepresentation> getLooks() {
        return mLooks;
    }

    public ArrayList<FilterRepresentation> getBorders() {
        return mBorders;
    }

    public ArrayList<FilterRepresentation> getTools() {
        return mTools;
    }

    public ArrayList<FilterRepresentation> getEffects() {
        return mEffects;
    }

    public void addLooks(Context context) {
        int[] drawid = {
                R.drawable.filtershow_fx_0000_vintage,
                R.drawable.filtershow_fx_0004_bw_contrast,
                R.drawable.filtershow_fx_0008_washout_color,
                R.drawable.filtershow_fx_0007_washout,
                R.drawable.filtershow_fx_0002_bleach,
                R.drawable.filtershow_fx_0001_instant,
                R.drawable.filtershow_fx_0003_blue_crush,
                R.drawable.filtershow_fx_0005_punch,
                R.drawable.filtershow_fx_0006_x_process
        };

        int[] sampleid = { // [0,  1~9] 10
                R.drawable.effect_sample_2,
                R.drawable.effect_sample_3,
                R.drawable.effect_sample_8,
                R.drawable.effect_sample_6,
                R.drawable.effect_sample_4,
                R.drawable.effect_sample_5,
                R.drawable.effect_sample_7,
                R.drawable.effect_sample_1,
                R.drawable.effect_sample_9
        };

        int[] fxNameid = {
                R.string.ffx_vintage,
                R.string.ffx_bw_contrast,
                R.string.ffx_washout_color,
                R.string.ffx_washout,
                R.string.ffx_bleach,
                R.string.ffx_instant,
                R.string.ffx_blue_crush,
                R.string.ffx_punch,
                R.string.ffx_x_process
        };

        // Do not localize.
        String[] serializationNames = {
                "LUT3D_VINTAGE",
                "LUT3D_BW",
                "LUT3D_WASHOUT_COLOR",
                "LUT3D_WASHOUT",
                "LUT3D_BLEACH",
                "LUT3D_INSTANT",
                "LUT3D_BLUECRUSH",
                "LUT3D_PUNCH",
                "LUT3D_XPROCESS"
        };

        FilterFxRepresentation nullFx = new FilterFxRepresentation(context.getString(R.string.none), R.drawable.effect_sample_0, 0, R.string.none);
        mLooks.add(nullFx);

        for (int i = 0; i < drawid.length; i++) {
            FilterFxRepresentation fx = new FilterFxRepresentation(context.getString(fxNameid[i]), drawid[i], sampleid[i], fxNameid[i]);
            fx.setSerializationName(serializationNames[i]);
            ImagePreset preset = new ImagePreset();
            preset.addFilter(fx);
            FilterUserPresetRepresentation rep = new FilterUserPresetRepresentation(context.getString(fxNameid[i]), sampleid[i], preset, -1);
            mLooks.add(rep);
            addRepresentation(fx);
        }
    }

    public void addBorders(Context context) {

        // Do not localize
        String[] serializationNames = {
                "FRAME_4X5",
                "FRAME_BRUSH",
                "FRAME_GRUNGE",
                "FRAME_SUMI_E",
                "FRAME_TAPE",
                "FRAME_BLACK",
                "FRAME_BLACK_ROUNDED",
                "FRAME_WHITE",
                "FRAME_WHITE_ROUNDED",
                "FRAME_CREAM",
                "FRAME_CREAM_ROUNDED"
        };

        int[] sampleid = { // [0, 11 ~21] 12
                R.drawable.effect_sample_11,
                R.drawable.effect_sample_12,
                R.drawable.effect_sample_13,
                R.drawable.effect_sample_14,
                R.drawable.effect_sample_15,
                R.drawable.effect_sample_16
                //                R.drawable.effect_sample_17,
                //                R.drawable.effect_sample_18,
                //                R.drawable.effect_sample_19,
                //                R.drawable.effect_sample_20,
                //                R.drawable.effect_sample_21
        };

        // The "no border" implementation
        int i = 0;
        FilterRepresentation rep = new FilterImageBorderRepresentation(0, R.drawable.effect_sample_0);
        mBorders.add(rep);

        //        // Regular borders
        ArrayList<FilterRepresentation> borderList = new ArrayList<FilterRepresentation>();
        //
        rep = new FilterImageBorderRepresentation(R.drawable.filtershow_border_4x5, sampleid[0]);
        borderList.add(rep);

        rep = new FilterImageBorderRepresentation(R.drawable.filtershow_border_brush, sampleid[1]);
        borderList.add(rep);

        rep = new FilterImageBorderRepresentation(R.drawable.filtershow_border_grunge, sampleid[2]);
        borderList.add(rep);

        rep = new FilterImageBorderRepresentation(R.drawable.filtershow_border_sumi_e, sampleid[3]);
        borderList.add(rep);

        rep = new FilterTextureBorderRepresentation(FilterTextureBorderRepresentation.DEFAULT_TEXTURE1, FILTER_BODER_DEFAULT_SIZE,
                FILTER_BODER_DEFAULT_RADIUS, sampleid[4]);
        borderList.add(rep);

        rep = new FilterColorBorderRepresentation(FilterColorBorderRepresentation.DEFAULT_COLOR1, FILTER_BODER_DEFAULT_SIZE,
                FILTER_BODER_DEFAULT_RADIUS, sampleid[5]);
        borderList.add(rep);

        for (FilterRepresentation filter : borderList) {
            filter.setSerializationName(serializationNames[i++]);
            addRepresentation(filter);
            mBorders.add(filter);
        }

    }

    public void addTools(Context context) {

        int[] textId = { // [22~26] ,5
                R.string.crop,
                R.string.straighten,
                R.string.rotate,
                R.string.mirror,
                R.string.imageDraw
        };

        int[] overlayId = { // [
                R.drawable.filtershow_button_geometry_crop,
                R.drawable.filtershow_button_geometry_straighten,
                R.drawable.filtershow_button_geometry_rotate,
                R.drawable.filtershow_button_geometry_flip,
                R.drawable.filtershow_drawing
        };

        FilterRepresentation[] geometryFilters = {
                new FilterCropRepresentation(R.drawable.effect_sample_22),
                new FilterStraightenRepresentation(R.drawable.effect_sample_25),
                new FilterRotateRepresentation(R.drawable.effect_sample_24),
                new FilterMirrorRepresentation(R.drawable.effect_sample_23),
                getRepresentation(ImageFilterDraw.class)
        };

        for (int i = 0; i < textId.length; i++) {
            FilterRepresentation geometry = geometryFilters[i];
            geometry.setTextId(textId[i]);
            geometry.setOverlayId(overlayId[i]);
            geometry.setOverlayOnly(true);
            if (geometry.getTextId() != 0) {
                geometry.setName(context.getString(geometry.getTextId()));
            }
            mTools.add(geometry);
        }
    }

    public void addEffects() {
        // sample resource id [28~43], 16
        mEffects.add(getRepresentation(ImageFilterWBalance.class));
        mEffects.add(getRepresentation(ImageFilterExposure.class));
        mEffects.add(getRepresentation(ImageFilterVignette.class));
        mEffects.add(getRepresentation(ImageFilterGrad.class));
        mEffects.add(getRepresentation(ImageFilterContrast.class));
        mEffects.add(getRepresentation(ImageFilterShadows.class));
        mEffects.add(getRepresentation(ImageFilterHighlights.class));
        mEffects.add(getRepresentation(ImageFilterVibrance.class));
        mEffects.add(getRepresentation(ImageFilterSharpen.class));
        mEffects.add(getRepresentation(ImageFilterCurves.class));
        mEffects.add(getRepresentation(ImageFilterHue.class));
        mEffects.add(getRepresentation(ImageFilterChanSat.class));
        mEffects.add(getRepresentation(ImageFilterBwFilter.class));
        mEffects.add(getRepresentation(ImageFilterNegative.class));
        mEffects.add(getRepresentation(ImageFilterEdge.class));
        mEffects.add(getRepresentation(ImageFilterKMeans.class));
    }

    public void removeRepresentation(ArrayList<FilterRepresentation> list, FilterRepresentation representation) {
        for (int i = 0; i < list.size(); i++) {
            FilterRepresentation r = list.get(i);
            if (r.getFilterClass() == representation.getFilterClass()) {
                list.remove(i);
                break;
            }
        }
    }

    public void setFilterResources(Resources resources) {
        ImageFilterImageBorder imageBorder = (ImageFilterImageBorder) getFilter(ImageFilterImageBorder.class);
        imageBorder.setResources(resources);
        ImageFilterTextureBorder colorBorder = (ImageFilterTextureBorder) getFilter(ImageFilterTextureBorder.class);
        colorBorder.setResources(resources);
        ImageFilterFx filterFx = (ImageFilterFx) getFilter(ImageFilterFx.class);
        filterFx.setResources(resources);
    }
}
