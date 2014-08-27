package com.xjt.newpic.filtershow.imageshow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.xjt.newpic.filtershow.editors.EditorMirror;
import com.xjt.newpic.filtershow.filters.FilterMirrorRepresentation;
import com.xjt.newpic.filtershow.imageshow.GeometryMathUtils.GeometryHolder;

public class ImageMirror extends ImageShow {
    private static final String TAG = ImageMirror.class.getSimpleName();
    private EditorMirror mEditorMirror;
    private FilterMirrorRepresentation mLocalRep = new FilterMirrorRepresentation(0);
    private GeometryHolder mDrawHolder = new GeometryHolder();

    public ImageMirror(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageMirror(Context context) {
        super(context);
    }

    public void setFilterMirrorRepresentation(FilterMirrorRepresentation rep) {
        mLocalRep = (rep == null) ? new FilterMirrorRepresentation(0) : rep;
    }

    public void flip() {
        mLocalRep.cycle();
        invalidate();
    }

    public FilterMirrorRepresentation getFinalRepresentation() {
        return mLocalRep;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Treat event as handled.
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        MasterImage master = MasterImage.getImage();
        Bitmap image = master.getFiltersOnlyImage();
        if (image == null) {
            return;
        }
        GeometryMathUtils.initializeHolder(mDrawHolder, mLocalRep);
        GeometryMathUtils.drawTransformedCropped(mDrawHolder, canvas, image, getWidth(),
                getHeight());
    }

    public void setEditor(EditorMirror editorFlip) {
        mEditorMirror = editorFlip;
    }

}
