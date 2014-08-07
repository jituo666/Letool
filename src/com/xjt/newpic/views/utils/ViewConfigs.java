
package com.xjt.newpic.views.utils;

import android.content.Context;
import android.content.res.Resources;

import com.xjt.newpic.R;
import com.xjt.newpic.views.layout.ThumbnailLayoutSpec;
import com.xjt.newpic.views.render.ThumbnailSetRenderer;
import com.xjt.newpic.views.render.ThumbnailVideoRenderer;


/**
 * @Author Jituo.Xuan
 * @Date 3:28:44 PM Aug 7, 2014
 * @Comments:null
 */
public final class ViewConfigs {

    public static class AlbumSetPage {

        private static AlbumSetPage sInstance;

        public ThumbnailLayoutSpec albumSetSpec;
        public ThumbnailSetRenderer.LabelSpec labelSpec;
        public int paddingLeft;
        public int paddingTop;
        public int paddingRight;
        public int paddingBottom;

        public static synchronized AlbumSetPage get(Context context) {
            if (sInstance == null) {
                sInstance = new AlbumSetPage(context);
            }
            return sInstance;
        }

        private AlbumSetPage(Context context) {
            Resources r = context.getResources();
            albumSetSpec = new ThumbnailLayoutSpec();
            albumSetSpec.rowsLand = r.getInteger(R.integer.albumset_rows_land);
            albumSetSpec.rowsPort = r.getInteger(R.integer.albumset_rows_port);
            albumSetSpec.thumbnailGap = r.getDimensionPixelSize(R.dimen.albumset_thumbnail_gap);
            albumSetSpec.labelHeight = r.getDimensionPixelSize(R.dimen.albumset_label_height);

            paddingLeft = r.getDimensionPixelSize(R.dimen.albumset_padding_left);
            paddingTop = r.getDimensionPixelSize(R.dimen.albumset_padding_top);
            paddingRight = r.getDimensionPixelSize(R.dimen.albumset_padding_right);
            paddingBottom = r.getDimensionPixelSize(R.dimen.albumset_padding_bottom);

            labelSpec = new ThumbnailSetRenderer.LabelSpec();
            labelSpec.labelHeight = r.getDimensionPixelSize(R.dimen.albumset_label_height);
            labelSpec.titleOffset = r.getDimensionPixelSize(R.dimen.albumset_title_offset);
            labelSpec.countOffset = r.getDimensionPixelSize(R.dimen.albumset_count_offset);
            labelSpec.titleFontSize = r.getDimensionPixelSize(R.dimen.albumset_title_font_size);
            labelSpec.countFontSize = r.getDimensionPixelSize(R.dimen.albumset_count_font_size);
            labelSpec.leftMargin = r.getDimensionPixelSize(R.dimen.albumset_left_margin);
            labelSpec.titleRightMargin = r.getDimensionPixelSize(R.dimen.albumset_title_right_margin);
            labelSpec.iconSize = r.getDimensionPixelSize(R.dimen.albumset_icon_size);
            labelSpec.backgroundColor = r.getColor(R.color.albumset_label_background);
            labelSpec.titleColor = r.getColor(R.color.albumset_label_title);
            labelSpec.countColor = r.getColor(R.color.albumset_label_count);
        }
    }

    public static class AlbumPage {

        private static AlbumPage sInstance;

        public ThumbnailLayoutSpec albumSpec;
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

            albumSpec = new ThumbnailLayoutSpec();
            albumSpec.rowsLand = r.getInteger(R.integer.album_rows_land);
            albumSpec.rowsPort = r.getInteger(R.integer.album_rows_port);
            albumSpec.thumbnailGap = r.getDimensionPixelSize(R.dimen.album_thumbnail_gap);
            albumSpec.tagHeight = r.getDimensionPixelSize(R.dimen.album_label_height);
            albumSpec.tagWidth = r.getDimensionPixelSize(R.dimen.album_label_width);
            //
            paddingLeft = r.getDimensionPixelSize(R.dimen.album_padding_left);
            paddingTop = r.getDimensionPixelSize(R.dimen.album_padding_top);
            paddingRight = r.getDimensionPixelSize(R.dimen.album_padding_right);
            paddingBottom = r.getDimensionPixelSize(R.dimen.album_padding_bottom);

        }
    }

    public static class VideoSetPage {

        private static VideoSetPage sInstance;

        public ThumbnailLayoutSpec videoSetSpec;
        public ThumbnailVideoRenderer.LabelSpec labelSpec;
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
            videoSetSpec = new ThumbnailLayoutSpec();
            videoSetSpec.rowsLand = r.getInteger(R.integer.videoset_rows_land);
            videoSetSpec.rowsPort = r.getInteger(R.integer.videoset_rows_port);
            videoSetSpec.thumbnailGap = r.getDimensionPixelSize(R.dimen.videoset_thumbnail_gap);
            videoSetSpec.labelHeight = r.getDimensionPixelSize(R.dimen.videoset_label_height);

            paddingLeft = r.getDimensionPixelSize(R.dimen.videoset_padding_left);
            paddingTop = r.getDimensionPixelSize(R.dimen.videoset_padding_top);
            paddingRight = r.getDimensionPixelSize(R.dimen.videoset_padding_right);
            paddingBottom = r.getDimensionPixelSize(R.dimen.videoset_padding_bottom);

            labelSpec = new ThumbnailVideoRenderer.LabelSpec();
            labelSpec.labelHeight = r.getDimensionPixelSize(R.dimen.video_label_height);
            labelSpec.titleOffset = r.getDimensionPixelSize(R.dimen.videoset_title_offset);
            labelSpec.countOffset = r.getDimensionPixelSize(R.dimen.videoset_count_offset);
            labelSpec.titleFontSize = r.getDimensionPixelSize(R.dimen.videoset_title_font_size);
            labelSpec.countFontSize = r.getDimensionPixelSize(R.dimen.videoset_count_font_size);
            labelSpec.leftMargin = r.getDimensionPixelSize(R.dimen.videoset_left_margin);
            labelSpec.titleRightMargin = r.getDimensionPixelSize(R.dimen.videoset_title_right_margin);
            labelSpec.iconSize = r.getDimensionPixelSize(R.dimen.videoset_icon_size);
            labelSpec.backgroundColor = r.getColor(R.color.albumset_label_background);
            labelSpec.titleColor = r.getColor(R.color.albumset_label_title);
            labelSpec.countColor = r.getColor(R.color.albumset_label_count);
        }
    }

    public static class VideoPage {

        private static VideoPage sInstance;

        public ThumbnailLayoutSpec videoSpec;
        public ThumbnailVideoRenderer.LabelSpec labelSpec;
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
            videoSpec = new ThumbnailLayoutSpec();
            videoSpec.rowsLand = r.getInteger(R.integer.video_rows_land);
            videoSpec.rowsPort = r.getInteger(R.integer.video_rows_port);
            videoSpec.thumbnailGap = r.getDimensionPixelSize(R.dimen.video_thumbnail_gap);
            videoSpec.labelHeight = r.getDimensionPixelSize(R.dimen.video_label_height);

            paddingLeft = r.getDimensionPixelSize(R.dimen.video_padding_left);
            paddingTop = r.getDimensionPixelSize(R.dimen.video_padding_top);
            paddingRight = r.getDimensionPixelSize(R.dimen.video_padding_right);
            paddingBottom = r.getDimensionPixelSize(R.dimen.video_padding_bottom);

            labelSpec = new ThumbnailVideoRenderer.LabelSpec();
            labelSpec.labelHeight = r.getDimensionPixelSize(R.dimen.video_label_height);
            labelSpec.titleOffset = r.getDimensionPixelSize(R.dimen.video_title_offset);
            labelSpec.countOffset = r.getDimensionPixelSize(R.dimen.video_count_offset);
            labelSpec.titleFontSize = r.getDimensionPixelSize(R.dimen.video_title_font_size);
            labelSpec.countFontSize = r.getDimensionPixelSize(R.dimen.video_count_font_size);
            labelSpec.leftMargin = r.getDimensionPixelSize(R.dimen.video_left_margin);
            labelSpec.titleRightMargin = r.getDimensionPixelSize(R.dimen.video_title_right_margin);
            labelSpec.iconSize = r.getDimensionPixelSize(R.dimen.video_icon_size);
            labelSpec.backgroundColor = r.getColor(R.color.video_label_background);
            labelSpec.titleColor = r.getColor(R.color.video_label_title);
            labelSpec.countColor = r.getColor(R.color.video_label_count);
        }
    }
}
