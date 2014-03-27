package com.xjt.letool.views;

import android.content.Context;
import android.content.res.Resources;

import com.xjt.letool.R;
import com.xjt.letool.views.layout.ThumbnailLayoutSpec;

public final class ViewConfigs {
    public static class AlbumSetPage {
        private static AlbumSetPage sInstance;

        public ThumbnailLayoutSpec albumSetSpec;
        //public AlbumSetSlotRenderer.LabelSpec labelSpec;
        public int paddingTop;
        public int paddingBottom;
        public int placeholderColor;

        public static synchronized AlbumSetPage get(Context context) {
            if (sInstance == null) {
                sInstance = new AlbumSetPage(context);
            }
            return sInstance;
        }

        private AlbumSetPage(Context context) {
            Resources r = context.getResources();

            placeholderColor = r.getColor(R.color.albumset_placeholder);

            albumSetSpec = new ThumbnailLayoutSpec();
            albumSetSpec.rowsLand = r.getInteger(R.integer.albumset_rows_land);
            albumSetSpec.rowsPort = r.getInteger(R.integer.albumset_rows_port);
            albumSetSpec.thumbnailGap = r.getDimensionPixelSize(R.dimen.albumset_slot_gap);
            albumSetSpec.thumbnailHeightAdditional = 0;

            paddingTop = r.getDimensionPixelSize(R.dimen.albumset_padding_top);
            paddingBottom = r.getDimensionPixelSize(R.dimen.albumset_padding_bottom);

//            labelSpec = new AlbumSetSlotRenderer.LabelSpec();
//            labelSpec.labelBackgroundHeight = r.getDimensionPixelSize(
//                    R.dimen.albumset_label_background_height);
//            labelSpec.titleOffset = r.getDimensionPixelSize(
//                    R.dimen.albumset_title_offset);
//            labelSpec.countOffset = r.getDimensionPixelSize(
//                    R.dimen.albumset_count_offset);
//            labelSpec.titleFontSize = r.getDimensionPixelSize(
//                    R.dimen.albumset_title_font_size);
//            labelSpec.countFontSize = r.getDimensionPixelSize(
//                    R.dimen.albumset_count_font_size);
//            labelSpec.leftMargin = r.getDimensionPixelSize(
//                    R.dimen.albumset_left_margin);
//            labelSpec.titleRightMargin = r.getDimensionPixelSize(
//                    R.dimen.albumset_title_right_margin);
//            labelSpec.iconSize = r.getDimensionPixelSize(
//                    R.dimen.albumset_icon_size);
//            labelSpec.backgroundColor = r.getColor(
//                    R.color.albumset_label_background);
//            labelSpec.titleColor = r.getColor(R.color.albumset_label_title);
//            labelSpec.countColor = r.getColor(R.color.albumset_label_count);
        }
    }

    public static class AlbumPage {
        private static AlbumPage sInstance;

        public ThumbnailLayoutSpec albumSetSpec;
        public int placeholderColor;

        public static synchronized AlbumPage get(Context context) {
            if (sInstance == null) {
                sInstance = new AlbumPage(context);
            }
            return sInstance;
        }

        private AlbumPage(Context context) {
            Resources r = context.getResources();

            placeholderColor = r.getColor(R.color.album_placeholder);

            albumSetSpec = new ThumbnailLayoutSpec();
            albumSetSpec.rowsLand = r.getInteger(R.integer.album_rows_land);
            albumSetSpec.rowsPort = r.getInteger(R.integer.album_rows_port);
            albumSetSpec.thumbnailGap = r.getDimensionPixelSize(R.dimen.album_slot_gap);
        }
    }

    public static class ManageCachePage extends AlbumSetPage {
        private static ManageCachePage sInstance;

        public final int cachePinSize;
        public final int cachePinMargin;

        public static synchronized ManageCachePage get(Context context) {
            if (sInstance == null) {
                sInstance = new ManageCachePage(context);
            }
            return sInstance;
        }

        public ManageCachePage(Context context) {
            super(context);
            Resources r = context.getResources();
            cachePinSize = r.getDimensionPixelSize(R.dimen.cache_pin_size);
            cachePinMargin = r.getDimensionPixelSize(R.dimen.cache_pin_margin);
        }
    }
}

