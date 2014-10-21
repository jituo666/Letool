//
//package com.xjt.newpic.views.utils;
//
//import com.xjt.newpic.R;
//import com.xjt.newpic.common.ThreadPool;
//import com.xjt.newpic.common.ThreadPool.JobContext;
//import com.xjt.newpic.imagedata.utils.LetoolBitmapPool;
//import com.xjt.newpic.views.render.ThumbnailVideoRenderer.ThumbnailLabelParam;
//
//import android.content.Context;
//import android.graphics.*;
//import android.graphics.Bitmap.Config;
//import android.text.TextPaint;
//import android.text.TextUtils;
//
//public class VideoLabelMaker {
//
//    // We keep a border around the album label to prevent aliasing
//    private static final int BACKGROUND_COLOR = Color.TRANSPARENT; // 为了显示底色,采取透明色
//
//    private final ThumbnailLabelParam mSpec;
//    private final TextPaint mTitlePaint;
//    private final TextPaint mDescPaint;
//
//    private int mLabelWidth;
//    private int mLabelHeight;
//    private int mBorderSize;
//
//    public VideoLabelMaker(Context context, ThumbnailLabelParam spec) {
//        mSpec = spec;
//        mTitlePaint = getTextPaint(spec.titleFontSize, context.getResources().getColor(R.color.np_major_text_color), false);
//        mDescPaint = getTextPaint(spec.countFontSize, context.getResources().getColor(R.color.np_minor_text_color), false);
//        mBorderSize = spec.borderSize;
//    }
//
//    public int getBorderSize() {
//        return mBorderSize;
//    }
//
//    private static TextPaint getTextPaint(int textSize, int color, boolean isBold) {
//        TextPaint paint = new TextPaint();
//        paint.setTextSize(textSize);
//        paint.setAntiAlias(true);
//        paint.setColor(color);
//        if (isBold) {
//            paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
//        }
//        return paint;
//    }
//
//    public synchronized void setLabelWidth(int width) {
//        if (mLabelWidth == width)
//            return;
//        mLabelWidth = width;
//        mLabelHeight = mSpec.labelHeight;
//    }
//
//    public ThreadPool.Job<Bitmap> requestLabel(String title, String count) {
//        return new AlbumLabelJob(title, count);
//    }
//
//    static void drawText(Canvas canvas, int x, int y, String text,
//            int lengthLimit, TextPaint p) {
//        synchronized (p) {
//            text = TextUtils.ellipsize(text, p, lengthLimit, TextUtils.TruncateAt.END).toString();
//            x = (lengthLimit - Math.round(p.measureText(text))) / 2;
//            canvas.drawText(text, x, y - p.getFontMetricsInt().ascent, p);
//        }
//    }
//
//    private class AlbumLabelJob implements ThreadPool.Job<Bitmap> {
//
//        private String mTitle;
//        private String mDesc;
//
//        public AlbumLabelJob(String title, String count) {
//            mTitle = title;
//            mDesc = count;
//        }
//
//        @Override
//        public Bitmap run(JobContext jc) {
//            Bitmap bitmap;
//            int labelWidth;
//
//            synchronized (this) {
//                labelWidth = mLabelWidth;
//                bitmap = LetoolBitmapPool.getInstance().get(mLabelWidth, mLabelHeight);
//            }
//
//            if (bitmap == null) {
//                bitmap = Bitmap.createBitmap(labelWidth, mLabelHeight, Config.ARGB_8888);
//            }
//
//            // 新生成一个位图，在里面绘制标签内容
//            Canvas canvas = new Canvas(bitmap);
//            canvas.clipRect(mBorderSize, mBorderSize, bitmap.getWidth() - mBorderSize, bitmap.getHeight() - mBorderSize);
//            canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.SRC);
//            canvas.translate(mBorderSize, mBorderSize);
//
//            canvas.translate(2 * mBorderSize, (mLabelHeight) / 2 - mBorderSize / 2);
//            mTitlePaint.setTextAlign(Paint.Align.LEFT);
//            mTitle = TextUtils.ellipsize(mTitle, mTitlePaint, mLabelWidth - 4 * mBorderSize, TextUtils.TruncateAt.END).toString();
//            canvas.drawText(mTitle, mSpec.gravity < 0 ? 0 : (mLabelWidth - mTitlePaint.measureText(mTitle)) / 2, 0, mTitlePaint);
//            if (jc.isCancelled())
//                return null;
//            // draw desc
//            canvas.translate(0, (mLabelHeight / 2));
//            mDescPaint.setTextAlign(Paint.Align.LEFT);
//            mDesc = TextUtils.ellipsize(mDesc, mDescPaint, mLabelWidth - 4 * mBorderSize, TextUtils.TruncateAt.END).toString();
//            canvas.drawText(mDesc, mSpec.gravity < 0 ? 0 : (mLabelWidth - mDescPaint.measureText(mDesc)) / 2, 0, mDescPaint);
//            return bitmap;
//        }
//    }
//
//    public void recycleLabel(Bitmap label) {
//        LetoolBitmapPool.getInstance().put(label);
//    }
//}
