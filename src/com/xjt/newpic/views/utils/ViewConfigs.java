
package com.xjt.newpic.views.utils;

import android.content.Context;
import android.content.res.Resources;

import com.xjt.newpic.R;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.views.layout.ThumbnailLayoutParam;
import com.xjt.newpic.views.render.ThumbnailSetRenderer;
import com.xjt.newpic.views.render.ThumbnailSetRenderer.ThumbnailLabelParam;
import com.xjt.newpic.views.render.ThumbnailVideoRenderer;

/**
 * @Author Jituo.Xuan
 * @Date 3:28:44 PM Aug 7, 2014
 * @Comments:null
 */
public final class ViewConfigs {

    private static final String TAG = ViewConfigs.class.getSimpleName();

    public static class AlbumSetGridPage {

        private static AlbumSetGridPage sInstance;

        public ThumbnailLayoutParam albumSetGridSpec;
        public ThumbnailSetRenderer.ThumbnailLabelParam labelSpec;
        public int paddingLeft;
        public int paddingTop;
        public int paddingRight;
        public int paddingBottom;

        public static synchronized AlbumSetGridPage get(Context context) {
            if (sInstance == null) {
                sInstance = new AlbumSetGridPage(context);
            }
            return sInstance;
        }

        private AlbumSetGridPage(Context context) {
            Resources r = context.getResources();
            paddingLeft = r.getDimensionPixelSize(R.dimen.albumset_grid_padding_left); // 整个屏幕左边距
            paddingTop = r.getDimensionPixelSize(R.dimen.albumset_grid_padding_top);  // 整个屏幕上边距
            paddingRight = r.getDimensionPixelSize(R.dimen.albumset_grid_padding_right); // 整个屏幕右边距
            paddingBottom = r.getDimensionPixelSize(R.dimen.albumset_grid_padding_bottom); // 整个屏幕下边距

            labelSpec = new ThumbnailSetRenderer.ThumbnailLabelParam();
            labelSpec.gravity = r.getInteger(R.integer.albumset_grid_gravity);
            labelSpec.labelHeight = r.getDimensionPixelSize(R.dimen.albumset_grid_label_height); // 标签的高度
            labelSpec.titleFontSize = r.getDimensionPixelSize(R.dimen.albumset_grid_title_font_size);
            labelSpec.countFontSize = r.getDimensionPixelSize(R.dimen.albumset_grid_count_font_size);
            labelSpec.borderSize = r.getDimensionPixelSize(R.dimen.albumset_grid_border_size);
            labelSpec.backgroundColor = r.getColor(R.color.albumset_label_background);
            labelSpec.titleColor = r.getColor(R.color.albumset_label_title);
            labelSpec.countColor = r.getColor(R.color.albumset_label_count);
            //
            albumSetGridSpec = new ThumbnailLayoutParam();
            albumSetGridSpec.rowsPort = r.getInteger(R.integer.albumset_grid_rows_port); // 每行有多少个缩略图
            albumSetGridSpec.thumbnailGap = r.getDimensionPixelSize(R.dimen.albumset_grid_thumbnail_gap); // 缩略图之间的间隔
            albumSetGridSpec.thumbnailPadding = r.getDimensionPixelSize(R.dimen.albumset_grid_thumbnail_padding); // 标签的高度
            albumSetGridSpec.labelHeight = labelSpec.labelHeight;
            LLog.i(TAG, " ---set grid rowsPort:" + albumSetGridSpec.rowsPort + " labelHeight:" + labelSpec.labelHeight);
        }
    }

    public static class AlbumSetListPage {

        private static AlbumSetListPage sInstance;

        public ThumbnailLayoutParam albumSetListSpec;
        public ThumbnailSetRenderer.ThumbnailLabelParam labelSpec;
        public int paddingLeft;
        public int paddingTop;
        public int paddingRight;
        public int paddingBottom;

        public static synchronized AlbumSetListPage get(Context context) {
            if (sInstance == null) {
                sInstance = new AlbumSetListPage(context);
            }
            return sInstance;
        }

        private AlbumSetListPage(Context context) {
            Resources r = context.getResources();

            paddingLeft = r.getDimensionPixelSize(R.dimen.albumset_list_padding_left);
            paddingTop = r.getDimensionPixelSize(R.dimen.albumset_list_padding_top);
            paddingRight = r.getDimensionPixelSize(R.dimen.albumset_list_padding_right);
            paddingBottom = r.getDimensionPixelSize(R.dimen.albumset_list_padding_bottom);

            labelSpec = new ThumbnailSetRenderer.ThumbnailLabelParam();
            labelSpec.gravity = r.getInteger(R.integer.albumset_list_gravity);
            labelSpec.labelHeight = r.getDimensionPixelSize(R.dimen.albumset_list_label_height);// 标签的高度，这个高度决定了缩略图的绘制范围（宽和高）
            labelSpec.titleFontSize = r.getDimensionPixelSize(R.dimen.albumset_list_title_font_size);
            labelSpec.countFontSize = r.getDimensionPixelSize(R.dimen.albumset_list_count_font_size);
            labelSpec.borderSize = r.getDimensionPixelSize(R.dimen.albumset_list_border_size);
            labelSpec.backgroundColor = r.getColor(R.color.albumset_label_background);
            labelSpec.titleColor = r.getColor(R.color.albumset_label_title);
            labelSpec.countColor = r.getColor(R.color.albumset_label_count);
            //
            albumSetListSpec = new ThumbnailLayoutParam();
            albumSetListSpec.rowsPort = r.getInteger(R.integer.albumset_list_rows_port);
            albumSetListSpec.thumbnailGap = r.getDimensionPixelSize(R.dimen.albumset_list_thumbnail_gap);
            albumSetListSpec.thumbnailPadding = r.getDimensionPixelSize(R.dimen.albumset_list_thumbnail_padding);
            albumSetListSpec.labelHeight = labelSpec.labelHeight;
            LLog.i(TAG, " ---set list rowsPort:" + albumSetListSpec.rowsPort + " labelHeight:" + labelSpec.labelHeight);
        }
    }

    public static class AlbumPage {

        private static AlbumPage sInstance;

        public ThumbnailLayoutParam albumSpec;
        public int placeholderColor;
        public int paddingLeft;
        public int paddingTop;
        public int paddingRight;
        public int paddingBottom;

        public static synchronized AlbumPage get(Context context) {
            if (sInstance == null) {
                sInstance = new AlbumPage(context);
            }
            return sInstance;
        }

        private AlbumPage(Context context) {
            Resources r = context.getResources();

            albumSpec = new ThumbnailLayoutParam();
            albumSpec.rowsPort = r.getInteger(R.integer.album_rows_port);
            albumSpec.thumbnailGap = r.getDimensionPixelSize(R.dimen.album_thumbnail_gap);
            //
            paddingLeft = r.getDimensionPixelSize(R.dimen.album_padding_left);
            paddingTop = r.getDimensionPixelSize(R.dimen.album_padding_top);
            paddingRight = r.getDimensionPixelSize(R.dimen.album_padding_right);
            paddingBottom = r.getDimensionPixelSize(R.dimen.album_padding_bottom);

        }
    }

    public static class VideoSetPage {

        private static VideoSetPage sInstance;

        public ThumbnailLayoutParam videoSetSpec;
        public ThumbnailLabelParam labelSpec;
        public int paddingLeft;
        public int paddingTop;
        public int paddingRight;
        public int paddingBottom;

        public static synchronized VideoSetPage get(Context context) {
            if (sInstance == null) {
                sInstance = new VideoSetPage(context);
            }
            return sInstance;
        }

        private VideoSetPage(Context context) {
            Resources r = context.getResources();
            videoSetSpec = new ThumbnailLayoutParam();
            videoSetSpec.rowsPort = r.getInteger(R.integer.videoset_rows_port);
            videoSetSpec.thumbnailGap = r.getDimensionPixelSize(R.dimen.videoset_thumbnail_gap);
            videoSetSpec.thumbnailPadding = r.getDimensionPixelSize(R.dimen.videoset_label_height);

            paddingLeft = r.getDimensionPixelSize(R.dimen.videoset_padding_left);
            paddingTop = r.getDimensionPixelSize(R.dimen.videoset_padding_top);
            paddingRight = r.getDimensionPixelSize(R.dimen.videoset_padding_right);
            paddingBottom = r.getDimensionPixelSize(R.dimen.videoset_padding_bottom);

            labelSpec = new ThumbnailLabelParam();
            labelSpec.labelHeight = r.getDimensionPixelSize(R.dimen.video_label_height);
            labelSpec.titleFontSize = r.getDimensionPixelSize(R.dimen.videoset_title_font_size);
            labelSpec.countFontSize = r.getDimensionPixelSize(R.dimen.videoset_count_font_size);
            labelSpec.backgroundColor = r.getColor(R.color.albumset_label_background);
            labelSpec.titleColor = r.getColor(R.color.albumset_label_title);
            labelSpec.countColor = r.getColor(R.color.albumset_label_count);
        }
    }

    public static class VideoPage {

        private static VideoPage sInstance;

        public ThumbnailLayoutParam videoSpec;
        public ThumbnailLabelParam labelSpec;
        public int paddingLeft;
        public int paddingTop;
        public int paddingRight;
        public int paddingBottom;

        public static synchronized VideoPage get(Context context) {
            if (sInstance == null) {
                sInstance = new VideoPage(context);
            }
            return sInstance;
        }

        private VideoPage(Context context) {
            Resources r = context.getResources();
            videoSpec = new ThumbnailLayoutParam();
            videoSpec.rowsPort = r.getInteger(R.integer.video_rows_port);
            videoSpec.thumbnailGap = r.getDimensionPixelSize(R.dimen.video_thumbnail_gap);
            videoSpec.thumbnailPadding = r.getDimensionPixelSize(R.dimen.video_label_height) / 4;

            paddingLeft = r.getDimensionPixelSize(R.dimen.video_padding_left);
            paddingTop = r.getDimensionPixelSize(R.dimen.video_padding_top);
            paddingRight = r.getDimensionPixelSize(R.dimen.video_padding_right);
            paddingBottom = r.getDimensionPixelSize(R.dimen.video_padding_bottom);

            labelSpec = new ThumbnailLabelParam();
            labelSpec.labelHeight = r.getDimensionPixelSize(R.dimen.video_label_height);
            labelSpec.titleFontSize = r.getDimensionPixelSize(R.dimen.video_title_font_size);
            labelSpec.countFontSize = r.getDimensionPixelSize(R.dimen.video_count_font_size);
            labelSpec.backgroundColor = r.getColor(R.color.video_label_background);
            labelSpec.titleColor = r.getColor(R.color.video_label_title);
            labelSpec.countColor = r.getColor(R.color.video_label_count);
        }
    }
}
