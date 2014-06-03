
package com.xjt.letool.views.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;

import com.xjt.letool.common.LLog;
import com.xjt.letool.common.ThreadPool;
import com.xjt.letool.common.ThreadPool.JobContext;
import com.xjt.letool.imagedata.utils.LetoolBitmapPool;
import com.xjt.letool.views.render.ThumbnailRendererWithTag;

public class AlbumSortTagMaker {

    private static final String TAG = AlbumSortTagMaker.class.getSimpleName();
    private static final int FONT_COLOR_TITLE = Color.BLACK;
    private static final int FONT_COLOR_COUNT = Color.BLACK;
    // We keep a border around the album sort tag to prevent aliasing
    private static final int BORDER_SIZE = 1;
    private static final int BACKGROUND_COLOR = Color.WHITE;
    private final ThumbnailRendererWithTag.SortTagSpec mSpec;
    private final TextPaint mTagNamePaint; // 标签名字
    private final TextPaint mCountPaint; // 非类下的数量
    private int mSortTagWidth;
    private int mSortTagHeight;

    public AlbumSortTagMaker(ThumbnailRendererWithTag.SortTagSpec spec) {
        mSpec = spec;
        mTagNamePaint = getTextPaint(spec.titleFontSize, FONT_COLOR_TITLE, false);
        mCountPaint = getTextPaint(spec.countFontSize, FONT_COLOR_COUNT, false);
    }

    public static int getBorderSize() {
        return BORDER_SIZE;
    }

    private static TextPaint getTextPaint(int textSize, int color, boolean isBold) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setFilterBitmap(true);
        // paint.setShadowLayer(2f, 0f, 0f, Color.BLACK);
        if (isBold) {
            paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
        return paint;
    }

    public synchronized void setSortTagMetrics(int width, int height) {
        if (mSortTagWidth == width)
            return;
        mSortTagWidth = width;
        mSortTagHeight = height;
    }

    public ThreadPool.Job<Bitmap> requestTag(String name, String count) {
        return new AlbumSortTagJob(name, count);
    }

    static void drawText(Canvas canvas, int x, int y, String text, int lengthLimit, TextPaint p) {
        // The TextPaint cannot be used concurrently
        synchronized (p) {
            text = TextUtils.ellipsize(text, p, lengthLimit, TextUtils.TruncateAt.END).toString();
            canvas.drawText(text, x, y + 4 * BORDER_SIZE, p);
        }
    }

    private class AlbumSortTagJob implements ThreadPool.Job<Bitmap> {

        private final String mTagName;
        private final String mCount;

        public AlbumSortTagJob(String name, String count) {
            mTagName = name;
            mCount = count;
        }

        @Override
        public Bitmap run(JobContext jc) {
            ThumbnailRendererWithTag.SortTagSpec s = mSpec;
            String name = mTagName;
            String count = mCount;
            //            Bitmap icon = getOverlayAlbumIcon();
            Bitmap bitmap;
            int sortTagWidth;
            int sortTagHeight;
            synchronized (this) {
                sortTagWidth = mSortTagWidth;
                sortTagHeight = mSortTagHeight;
                bitmap = LetoolBitmapPool.getInstance().get(mSortTagWidth, mSortTagHeight);
            }
            if (bitmap == null) {
                int borders = 2 * BORDER_SIZE;
                bitmap = Bitmap.createBitmap(sortTagWidth + borders, sortTagHeight + borders, Config.ARGB_8888);
            }
            Canvas canvas = new Canvas(bitmap);
            canvas.clipRect(BORDER_SIZE, BORDER_SIZE, bitmap.getWidth() - BORDER_SIZE, bitmap.getHeight() - BORDER_SIZE);
            canvas.translate(BORDER_SIZE, BORDER_SIZE);
            canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC);
            int x = 0;
            int y = 0;
            if (jc.isCancelled())
                return null;
            /* if (icon != null)
                 x += s.iconSize;*/
            // draw count
            if (jc.isCancelled())
                return null;
            FontMetricsInt fi = mCountPaint.getFontMetricsInt();
            //x = sortTagWidth - Math.round(mCountPaint.measureText(count));
            x = 0;
            y = (mSortTagHeight - (fi.descent - fi.ascent)) / 2 + fi.leading - fi.ascent;
            drawText(canvas, x, y, count, Math.round(mCountPaint.measureText(count)), mCountPaint);
            // draw the date
            fi = mTagNamePaint.getFontMetricsInt();
            x = Math.round(mCountPaint.measureText(count)) + 8;
            y = (mSortTagHeight - (fi.descent - mCountPaint.getFontMetricsInt().ascent)) / 2 + fi.leading - fi.ascent;
            drawText(canvas, x, y, name.substring(0, 10), sortTagWidth - Math.round(mCountPaint.measureText(count)),
                    mTagNamePaint);
            //
            y += (mSortTagHeight / 2) - 12;
            drawText(canvas, x, y, name.substring(11), sortTagWidth - Math.round(mCountPaint.measureText(count)),
                    mTagNamePaint);
            return bitmap;
        }
    }

    public void recycleSortTag(Bitmap sortTag) {
        LetoolBitmapPool.getInstance().put(sortTag);
    }

}
