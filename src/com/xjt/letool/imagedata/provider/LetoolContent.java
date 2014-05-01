
package com.xjt.letool.imagedata.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class LetoolContent {

    public static final Uri CONTENT_URI = Uri.parse("content://" + LetoolProvider.AUTHORITY + "/letool");

    public static final class Thumbnails implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + LetoolProvider.AUTHORITY + "/thumbnails");
        public static final String BUKET_ID = "buket_id";
        public static final String PATH_ID = "path_id";
        public static final String DATE_TAKEN = "date_taken";
        public static final String THUMBS_DATA = "thumb_data";
    }
}
