package com.xjt.letool.data.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class LetooContent {

    public static final Uri CONTENT_URI = Uri.parse("com.xjt.letool");

    public static final class Thumbnails implements BaseColumns {
        public static final String ORIGINAL_PATH = "orig-path";
        public static final String DATE_TAKEN = "date-taken";
        public static final String MICRO_THUMBS_DATA = "mi_thumb_data";
        public static final String THUMBS_DATA = "thumb-data";
    }
}
