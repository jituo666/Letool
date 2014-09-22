
package com.xjt.newpic.edit.filters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import com.xjt.newpic.R;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.edit.filters.FilterDrawRepresentation.StrokeData;
import com.xjt.newpic.edit.imageshow.ImageManager;
import com.xjt.newpic.edit.pipeline.FilterEnvironment;

import java.util.Vector;

public class ImageFilterDraw extends ImageFilter {

    private static final String TAG = ImageFilterDraw.class.getSimpleName();

    private Bitmap mOverlayBitmap; // this accelerates interaction
    private int mCachedStrokes = -1;
    private int mCurrentStyle = 0;

    FilterDrawRepresentation mRepresentation = new FilterDrawRepresentation(0);

    public ImageFilterDraw() {
        mName = "Image Draw";
    }

    @Override
    public FilterRepresentation getDefaultRepresentation() {
        return new FilterDrawRepresentation(R.drawable.effect_sample_26);
    }

    @Override
    public void useRepresentation(FilterRepresentation representation) {
        FilterDrawRepresentation parameters = (FilterDrawRepresentation) representation;
        mRepresentation = parameters;
    }

    public void setStyle(int style) {
        mCurrentStyle = style;
    }

    public int getStyle() {
        return mCurrentStyle;
    }

    @Override
    public Bitmap apply(Bitmap bitmap, float scaleFactor, int quality) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix m = getOriginalToScreenMatrix(w, h);
        drawData(new Canvas(bitmap), m, quality);
        return bitmap;
    }

    public void drawData(Canvas canvas, Matrix originalRotateToScreen, int quality) {
        Paint paint = new Paint();
        if (quality == FilterEnvironment.QUALITY_FINAL) {
            paint.setAntiAlias(true);
        }
        paint.setStyle(Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(40);

        if (mRepresentation.getDrawing().isEmpty() && mRepresentation.getCurrentDrawing() == null) {
            mOverlayBitmap = null;
            mCachedStrokes = -1;
            LLog.i(TAG, " -------------------------null rep----------------------");
            return;
        }
        if (quality == FilterEnvironment.QUALITY_FINAL) {
            for (FilterDrawRepresentation.StrokeData strokeData : mRepresentation.getDrawing()) {
                paint(strokeData, canvas, originalRotateToScreen, quality);
            }
            return;
        } else {

            if (mOverlayBitmap == null || mOverlayBitmap.getWidth() != canvas.getWidth() ||
                    mOverlayBitmap.getHeight() != canvas.getHeight() || mRepresentation.getDrawing().size() < mCachedStrokes) {
                mOverlayBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
                mCachedStrokes = 0;
            }

            if (mCachedStrokes < mRepresentation.getDrawing().size()) {
                Canvas drawCache = new Canvas(mOverlayBitmap);
                Vector<FilterDrawRepresentation.StrokeData> v = mRepresentation.getDrawing();
                int n = v.size();

                for (int i = mCachedStrokes; i < n; i++) {
                    paint(v.get(i), drawCache, originalRotateToScreen, FilterEnvironment.QUALITY_PREVIEW);
                }
                mCachedStrokes = n;
            }
            canvas.drawBitmap(mOverlayBitmap, 0, 0, paint);

            StrokeData stroke = mRepresentation.getCurrentDrawing();
            if (stroke != null) {
                paint(stroke, canvas, originalRotateToScreen, quality);
            }
        }
    }

    void paint(FilterDrawRepresentation.StrokeData sd, Canvas canvas, Matrix toScrMatrix, int quality) {
        new Brush(sd.mType).paint(sd, canvas, toScrMatrix, quality);
    }

    public static interface DrawStyle {

        public void setBrushId(int brushId);

        public void paint(FilterDrawRepresentation.StrokeData sd, Canvas canvas, Matrix toScrMatrix, int quality);
    }

    //    class SimpleDraw implements DrawStyle {
    //
    //        int brushId;
    //        int mode;
    //
    //        public SimpleDraw(int m) {
    //            mode = m;
    //        }
    //
    //        @Override
    //        public void setBrushId(int id) {
    //            brushId = id;
    //        }
    //
    //        @Override
    //        public void paint(FilterDrawRepresentation.StrokeData sd, Canvas canvas, Matrix toScrMatrix, int quality) {
    //            if (sd == null) {
    //                return;
    //            }
    //            if (sd.mPath == null) {
    //                return;
    //            }
    //            Paint paint = new Paint();
    //
    //            paint.setStyle(Style.STROKE);
    //            if (mode == 0) {
    //                paint.setStrokeCap(Paint.Cap.SQUARE);
    //            } else {
    //                paint.setStrokeCap(Paint.Cap.ROUND);
    //            }
    //            paint.setAntiAlias(true);
    //            paint.setColor(sd.mColor);
    //            paint.setStrokeWidth(toScrMatrix.mapRadius(sd.mRadius));
    //
    //            // done this way because of a bug in path.transform(matrix)
    //            Path mCacheTransPath = new Path();
    //            mCacheTransPath.addPath(sd.mPath, toScrMatrix);
    //            canvas.drawPath(mCacheTransPath, paint);
    //        }
    //    }

    class Brush implements DrawStyle {

        int brushID;
        Bitmap brush;

        public Brush(int id) {
            brushID = id;
        }

        public Bitmap getBrush() {
            if (brush == null) {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inPreferredConfig = Bitmap.Config.ALPHA_8;
                LLog.i(TAG, " -------------------Brush brushID:" + brushID);
                brush = BitmapFactory.decodeResource(ImageManager.getImage().getActivity().getResources(), brushID, opt);
                brush = brush.extractAlpha();
            }
            return brush;
        }

        @Override
        public void paint(FilterDrawRepresentation.StrokeData sd, Canvas canvas, Matrix toScrMatrix, int quality) {
            if (sd == null || sd.mPath == null) {
                return;
            }
            Paint paint = new Paint();
            paint.setStyle(Style.STROKE);
            paint.setAntiAlias(true);
            Path mCacheTransPath = new Path();
            mCacheTransPath.addPath(sd.mPath, toScrMatrix);
            draw(canvas, paint, sd.mColor, toScrMatrix.mapRadius(sd.mRadius) * 2, mCacheTransPath);
        }

        public Bitmap createScaledBitmap(Bitmap src, int dstWidth, int dstHeight, boolean filter) {
            Matrix m = new Matrix();
            m.setScale(dstWidth / (float) src.getWidth(), dstHeight / (float) src.getHeight());
            Bitmap result = Bitmap.createBitmap(dstWidth, dstHeight, src.getConfig());
            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setFilterBitmap(filter);
            canvas.drawBitmap(src, m, paint);
            return result;

        }

        void draw(Canvas canvas, Paint paint, int color, float size, Path path) {
            PathMeasure pathMeasure = new PathMeasure();
            float[] mPosition = new float[2];
            float[] mTan = new float[2];

            pathMeasure.setPath(path, false);

            paint.setAntiAlias(true);
            paint.setColor(color);

            paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
            Bitmap brush;
            // done this way because of a bug in Bitmap.createScaledBitmap(getBrush(),(int) size,(int) size,true);
            brush = createScaledBitmap(getBrush(), (int) size, (int) size, true);
            float len = pathMeasure.getLength();
            float s2 = size / 2;
            float step = s2 / 8;
            for (float i = 0; i < len; i += step) {
                pathMeasure.getPosTan(i, mPosition, mTan);
                // canvas.drawCircle(pos[0], pos[1], size, paint);
                canvas.drawBitmap(brush, mPosition[0] - s2, mPosition[1] - s2, paint);
            }
        }

        @Override
        public void setBrushId(int id) {
            brushID = id;
        }
    }
}
