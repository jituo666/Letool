package com.xjt.newpic.views.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;

import com.xjt.newpic.R;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.common.ThreadPool;
import com.xjt.newpic.common.ThreadPool.JobContext;
import com.xjt.newpic.imagedata.utils.LetoolBitmapPool;
import com.xjt.newpic.views.render.ThumbnailSetRenderer;

public class AlbumLabelMaker {

    private static final String TAG = AlbumLabelMaker.class.getSimpleName();
    // We keep a border around the album label to prevent aliasing
    private final ThumbnailSetRenderer.ThumbnailLabelParam mSpec;
    private final TextPaint mTitlePaint;
    private final TextPaint mDescPaint;

    private int mBorderSize;
    private int mLabelWidth;
    private int mLabelHeight;

    public AlbumLabelMaker(Context context, ThumbnailSetRenderer.ThumbnailLabelParam spec) {
        mSpec = spec;
        mTitlePaint = getTextPaint(spec.titleFontSize, context.getResources().getColor(R.color.cp_text_major_color), false);
        mDescPaint = getTextPaint(spec.countFontSize, context.getResources().getColor(R.color.cp_text_minor_color), false);
        mBorderSize = spec.borderSize;
    }

    public int getBorderSize() {
        return mBorderSize;
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

    public ThreadPool.Job<Bitmap> requestLabel(String title, String desc) {
        return new AlbumLabelJob(title, desc);
    }

    static void drawText(Canvas canvas, int x, int y, String text, int lengthLimit, TextPaint p) {
        synchronized (p) {
            text = TextUtils.ellipsize(text, p, lengthLimit, TextUtils.TruncateAt.END).toString();
            x = (lengthLimit - Math.round(p.measureText(text))) / 2;
            canvas.drawText(text, x, y - p.getFontMetricsInt().ascent, p);
        }
    }

    private class AlbumLabelJob implements ThreadPool.Job<Bitmap> {

        private String mTitle;
        private String mDesc;

        public AlbumLabelJob(String title, String count) {
            mTitle = title;
            mDesc = count;
        }

        @Override
        public Bitmap run(JobContext jc) {
            int labelWidth;
            labelWidth = mLabelWidth;
            Bitmap bitmap;
            synchronized (this) {
                labelWidth = mLabelWidth;
                bitmap = LetoolBitmapPool.getInstance().get(mLabelWidth, mLabelHeight);
            }

            if (bitmap == null) {
                LLog.i(TAG, "--------AlbumLabelJob:createBitmap" + mTitle + " labelWidth:" + labelWidth + " mLabelHeight:" + mLabelHeight);
                bitmap = Bitmap.createBitmap(labelWidth, mLabelHeight, Config.ARGB_8888);
            }

            // 新生成一个位图，在里面绘制标签内容
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(mSpec.backgroundColor, PorterDuff.Mode.SRC);

            // draw title
            if (jc.isCancelled())
                return null;

            canvas.translate(2 * mBorderSize, (mLabelHeight) / 2 - mBorderSize/2);
            mTitlePaint.setTextAlign(Paint.Align.LEFT);
            mTitle = TextUtils.ellipsize(mTitle, mTitlePaint, mLabelWidth - 4 * mBorderSize, TextUtils.TruncateAt.END).toString();
            canvas.drawText(mTitle, mSpec.gravity < 0 ? 0 : (mLabelWidth - mTitlePaint.measureText(mTitle)) / 2, 0, mTitlePaint);
            if (jc.isCancelled())
                return null;
            // draw desc
            canvas.translate(0, (mLabelHeight / 2));
            mDescPaint.setTextAlign(Paint.Align.LEFT);
            mDesc = TextUtils.ellipsize(mDesc, mDescPaint, mLabelWidth - 4 * mBorderSize, TextUtils.TruncateAt.END).toString();
            canvas.drawText(mDesc, mSpec.gravity < 0 ? 0 : (mLabelWidth - mDescPaint.measureText(mDesc)) / 2, 0, mDescPaint);
            return bitmap;
        }
    }

    public void recycleLabel(Bitmap label) {
        LetoolBitmapPool.getInstance().put(label);
    }
}
