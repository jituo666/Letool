package com.xjt.newpic.filtershow.filters;

import java.util.Vector;

public abstract class FilterPointRepresentation extends FilterRepresentation {
    private static final String LOGTAG = "FilterPointRepresentation";
    private Vector<FilterPoint> mCandidates = new Vector<FilterPoint>();

    public FilterPointRepresentation(String type, int sr, int textid, int editorID) {
        super(type,sr);
        setFilterClass(ImageFilterRedEye.class);
        setFilterType(FilterRepresentation.TYPE_NORMAL);
        setTextId(textid);
        setEditorId(editorID);
    }

    @Override
    public abstract FilterRepresentation copy();

    @Override
    protected void copyAllParameters(FilterRepresentation representation) {
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    public boolean hasCandidates() {
        return mCandidates != null;
    }

    public Vector<FilterPoint> getCandidates() {
        return mCandidates;
    }

    @Override
    public boolean isNil() {
        if (getCandidates() != null && getCandidates().size() > 0) {
            return false;
        }
        return true;
    }

    public Object getCandidate(int index) {
        return this.mCandidates.get(index);
    }

    public void addCandidate(FilterPoint c) {
        this.mCandidates.add(c);
    }

    @Override
    public void useParametersFrom(FilterRepresentation a) {
        if (a instanceof FilterPointRepresentation) {
            FilterPointRepresentation representation = (FilterPointRepresentation) a;
            mCandidates.clear();
            for (FilterPoint redEyeCandidate : representation.mCandidates) {
                mCandidates.add(redEyeCandidate);
            }
        }
    }

    public void removeCandidate(RedEyeCandidate c) {
        this.mCandidates.remove(c);
    }

    public void clearCandidates() {
        this.mCandidates.clear();
    }

    public int getNumberOfCandidates() {
        return mCandidates.size();
    }
}
