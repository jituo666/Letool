package com.xjt.letool.metadata.image;

import android.database.Cursor;

import com.xjt.letool.metadata.MediaDetails;
import com.xjt.letool.metadata.MediaItem;
import com.xjt.letool.metadata.MediaPath;
import com.xjt.letool.utils.LetoolUtils;

import java.text.DateFormat;
import java.util.Date;

//
// LocalMediaItem is an abstract class captures those common fields
// in LocalImage and LocalVideo.
//
public abstract class LocalMediaItem extends MediaItem {

    @SuppressWarnings("unused")
    private static final String TAG = "LocalMediaItem";

    // database fields
    public int id;
    public String caption = "UnKnown";
    public String mimeType;
    public long fileSize;
    public double latitude = INVALID_LATLNG;
    public double longitude = INVALID_LATLNG;
    public long dateTakenInMs;
    public long dateAddedInSec;
    public long dateModifiedInSec;
    public String filePath;
    public int bucketId;
    public int width;
    public int height;

    public LocalMediaItem(MediaPath path, long version) {
        super(path, version);
    }

    @Override
    public long getDateInMs() {
        return dateTakenInMs;
    }

    @Override
    public String getName() {
        return caption;
    }

    @Override
    public void getLatLong(double[] latLong) {
        latLong[0] = latitude;
        latLong[1] = longitude;
    }

    abstract protected boolean updateFromCursor(Cursor cursor);

    public int getBucketId() {
        return bucketId;
    }

    public void updateContent(Cursor cursor) {
        if (updateFromCursor(cursor)) {
            mDataVersion = nextVersionNumber();
        }
    }

    @Override
    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        details.addDetail(MediaDetails.INDEX_PATH, filePath);
        DateFormat formater = DateFormat.getDateTimeInstance();
        details.addDetail(MediaDetails.INDEX_DATETIME, formater.format(new Date(dateTakenInMs)));

        if (LetoolUtils.isValidLocation(latitude, longitude)) {
            details.addDetail(MediaDetails.INDEX_LOCATION, new double[] {latitude, longitude});
        }
        if (fileSize > 0) details.addDetail(MediaDetails.INDEX_SIZE, fileSize);
        return details;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    public long getSize() {
        return fileSize;
    }
}
