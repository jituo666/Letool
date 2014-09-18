package com.xjt.newpic.edit.imageshow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.xjt.newpic.edit.editors.EditorRotate;
import com.xjt.newpic.edit.filters.FilterRotateRepresentation;
import com.xjt.newpic.edit.imageshow.GeometryMathUtils.GeometryHolder;

public class ImageRotate extends ImageShow {
    private EditorRotate mEditorRotate;
    private static final String TAG = ImageRotate.class.getSimpleName();
    private FilterRotateRepresentation mLocalRep = new FilterRotateRepresentation(0);
    private GeometryHolder mDrawHolder = new GeometryHolder();

    public ImageRotate(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageRotate(Context context) {
        super(context);
    }

    public void setFilterRotateRepresentation(FilterRotateRepresentation rep) {
        mLocalRep = (rep == null) ? new FilterRotateRepresentation(0) : rep;
    }

    public void rotate() {
        mLocalRep.rotateCW();
        invalidate();
    }

    public FilterRotateRepresentation getFinalRepresentation() {
        return mLocalRep;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Treat event as handled.
        return true;
    }

    public int getLocalValue() {
        return mLocalRep.getRotation().value();
    }

    @Override
    public void onDraw(Canvas canvas) {
        ImageManager master = ImageManager.getImage();
        Bitmap image = master.getFiltersOnlyImage();
        if (image == null) {
            return;
        }
        GeometryMathUtils.initializeHolder(mDrawHolder, mLocalRep);
        GeometryMathUtils.drawTransformedCropped(mDrawHolder, canvas, image, canvas.getWidth(),
                canvas.getHeight());
    }

    public void setEditor(EditorRotate editorRotate) {
        mEditorRotate = editorRotate;
    }
}
