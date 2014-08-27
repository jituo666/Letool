package com.xjt.newpic.filtershow.filters;

import android.graphics.RectF;

import com.xjt.newpic.R;
import com.xjt.newpic.filtershow.editors.EditorRedEye;

import java.util.Vector;

public class FilterRedEyeRepresentation extends FilterPointRepresentation {
    private static final String LOGTAG = "FilterRedEyeRepresentation";

    public FilterRedEyeRepresentation(int sr) {
        super("RedEye",R.string.redeye,EditorRedEye.ID,sr);
        setSerializationName("REDEYE");
        setFilterClass(ImageFilterRedEye.class);
        setOverlayId(R.drawable.photoeditor_effect_redeye);
        setOverlayOnly(true);
    }

    @Override
    public FilterRepresentation copy() {
        FilterRedEyeRepresentation representation = new FilterRedEyeRepresentation(0);
        copyAllParameters(representation);
        return representation;
    }

    @Override
    protected void copyAllParameters(FilterRepresentation representation) {
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    public void addRect(RectF rect, RectF bounds) {
        Vector<RedEyeCandidate> intersects = new Vector<RedEyeCandidate>();
        for (int i = 0; i < getCandidates().size(); i++) {
            RedEyeCandidate r = (RedEyeCandidate) getCandidate(i);
            if (r.intersect(rect)) {
                intersects.add(r);
            }
        }
        for (int i = 0; i < intersects.size(); i++) {
            RedEyeCandidate r = intersects.elementAt(i);
            rect.union(r.mRect);
            bounds.union(r.mBounds);
            removeCandidate(r);
        }
        addCandidate(new RedEyeCandidate(rect, bounds));
    }

}
