package com.xjt.newpic.views.utils;

import com.xjt.newpic.R;
import com.xjt.newpic.common.ThreadPool;
import com.xjt.newpic.common.ThreadPool.JobContext;
import com.xjt.newpic.imagedata.utils.LetoolBitmapPool;
import com.xjt.newpic.views.render.ThumbnailVideoRenderer;

import android.content.Context;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.text.TextPaint;
import android.text.TextUtils;

public class VideoLabelMaker {

    // We keep a border around the album label to prevent aliasing
    private static final int BORDER_SIZE = 1;
    private static final int BACKGROUND_COLOR = Color.TRANSPARENT; // 为了显示底色,采取透明色

    private final ThumbnailVideoRenderer.LabelSpec mSpec;
    private final TextPaint mTitlePaint;
    private final TextPaint mCountPaint;

    private int mLabelWidth;
    private int mLabelHeight;

    public VideoLabelMaker(Context context, ThumbnailVideoRenderer.LabelSpec spec) {
        mSpec = spec;
        mTitlePaint = getTextPaint(spec.titleFontSize, context.getResources().getColor(R.color.np_major_text_color), false);
        mCountPaint = getTextPaint(spec.countFontSize, context.getResources().getColor(R.color.np_minor_text_color), false);

    }

    public static int getBorderSize() {
        return BORDER_SIZE;
    }

    private static TextPaint getTextPaint(int textSize, int color, boolean isBold) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setColor(color);
        if (isBold) {
            paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
        return paint;
    }

    public synchronized void setLabelWidth(int width) {
        if (mLabelWidth == width)
            return;
        mLabelWidth = width;
        mLabelHeight = mSpec.labelHeight;
    }

    public ThreadPool.Job<Bitmap> requestLabel(String title, String count) {
        return new AlbumLabelJob(title, count);
    }

    static void drawText(Canvas canvas, int x, int y, String text,
            int lengthLimit, TextPaint p) {
        synchronized (p) {
            text = TextUtils.ellipsize(text, p, lengthLimit, TextUtils.TruncateAt.END).toString();
            x = (lengthLimit - Math.round(p.measureText(text))) / 2;
            canvas.drawText(text, x, y - p.getFontMetricsInt().ascent, p);
        }
    }

    private class AlbumLabelJob implements ThreadPool.Job<Bitmap> {

        private String mTitle;
        private String mCount;

        public AlbumLabelJob(String title, String count) {
            mTitle = title;
            mCount = count;
        }

        @Override
        public Bitmap run(JobContext jc) {
            Bitmap bitmap;
            int labelWidth;

            synchronized (this) {
                labelWidth = mLabelWidth;
                bitmap = LetoolBitmapPool.getInstance().get(mLabelWidth, mLabelHeight);
            }

            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(labelWidth, mLabelHeight, Config.ARGB_8888);
            }

            // 新生成一个位图，在里面绘制标签内容
            Canvas canvas = new Canvas(bitmap);
            canvas.clipRect(BORDER_SIZE, BORDER_SIZE, bitmap.getWidth() - BORDER_SIZE, bitmap.getHeight() - BORDER_SIZE);
            canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC);
            canvas.translate(BORDER_SIZE, BORDER_SIZE);

            // draw title
            if (jc.isCancelled())
                return null;
            int x = (mLabelWidth - mSpec.titleOffset) / 2 + BORDER_SIZE;
            int y = mLabelHeight - mSpec.labelHeight * 7 / 12 + 2;
            mTitlePaint.setTextAlign(Paint.Align.CENTER);
            mTitle = TextUtils.ellipsize(mTitle, mTitlePaint, mLabelWidth - 2 * BORDER_SIZE, TextUtils.TruncateAt.END).toString();
            canvas.drawText(mTitle, x, y, mTitlePaint);
            if (jc.isCancelled())
                return null;
            // draw count
            x = (mLabelWidth - mSpec.countOffset) / 2 + BORDER_SIZE;
            y = (mLabelHeight) / 2 + +mSpec.countOffset * 3 / 2;
            mCountPaint.setTextAlign(Paint.Align.CENTER);
            mCount = TextUtils.ellipsize(mCount, mCountPaint, mLabelWidth - 2 * BORDER_SIZE, TextUtils.TruncateAt.END).toString();
            canvas.drawText(mCount, x, y, mCountPaint);
            return bitmap;
        }
    }

    public void recycleLabel(Bitmap label) {
        LetoolBitmapPool.getInstance().put(label);
    }
}
