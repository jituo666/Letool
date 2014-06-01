
package com.xjt.letool.views.utils;

import android.content.Context;
import android.content.res.Resources;

import com.xjt.letool.R;
import com.xjt.letool.views.layout.ThumbnailLayoutSpec;
import com.xjt.letool.views.render.ThumbnailRenderer;
import com.xjt.letool.views.render.ThumbnailSetRenderer;

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
            albumSetSpec.placeholderColor = r.getColor(R.color.albumset_placeholder);
            albumSetSpec.rowsLand = r.getInteger(R.integer.albumset_rows_land);
            albumSetSpec.rowsPort = r.getInteger(R.integer.albumset_rows_port);
            albumSetSpec.thumbnailGap = r.getDimensionPixelSize(R.dimen.albumset_thumbnail_gap);
            albumSetSpec.labelHeight = r.getDimensionPixelSize(R.dimen.albumset_thumbnail_label_height);

            paddingLeft = r.getDimensionPixelSize(R.dimen.albumset_padding_left);
            paddingTop = r.getDimensionPixelSize(R.dimen.albumset_padding_top);
            paddingRight = r.getDimensionPixelSize(R.dimen.albumset_padding_right);
            paddingBottom = r.getDimensionPixelSize(R.dimen.albumset_padding_bottom);

            labelSpec = new ThumbnailSetRenderer.LabelSpec();
            labelSpec.labelBackgroundHeight = r.getDimensionPixelSize(R.dimen.albumset_label_background_height);
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

        public ThumbnailRenderer.SortTagSpec sortTagSpec;

        public static synchronized AlbumPage get(Context context) {
            if (sInstance == null) {
                sInstance = new AlbumPage(context);
            }
            return sInstance;
        }

        private AlbumPage(Context context) {
            Resources r = context.getResources();

            placeholderColor = r.getColor(R.color.album_placeholder);

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

            sortTagSpec = new ThumbnailRenderer.SortTagSpec(); //分类标签布局定义
            sortTagSpec.titleFontSize = r.getDimensionPixelSize(R.dimen.album_tag_font_size);
            sortTagSpec.countFontSize = r.getDimensionPixelSize(R.dimen.album_count_font_size);
            sortTagSpec.iconSize = r.getDimensionPixelSize(R.dimen.album_icon_size);
        }
    }
}
