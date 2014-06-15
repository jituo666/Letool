
package com.xjt.letool.metadata;

import android.os.Environment;

import com.xjt.letool.common.LLog;
import com.xjt.letool.utils.LetoolUtils;
import com.xjt.letool.utils.UtilStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public class MediaSetUtils {

    private static final String TAG = MediaSetUtils.class.getSimpleName();
    public static final String IMPORTED = "Imported";
    public static final String DOWNLOAD = "download";
    public static final Comparator<MediaSet> NAME_COMPARATOR = new NameComparator();
    public static int[] MY_ALBUM_BUCKETS = new int[0];

    public static final int DOWNLOAD_BUCKET_ID = LetoolUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() + "/"
                    + DOWNLOAD);
    public static final int IMPORTED_BUCKET_ID = LetoolUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() + "/"
                    + IMPORTED);
    public static final int SNAPSHOT_BUCKET_ID = LetoolUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() +
                    "/Pictures/Screenshots");

    public static void initializeMyAlbumBuckets() {
        //common
        ArrayList<Integer> list = new ArrayList<Integer>();
        if (UtilStorage.getInstance().getExternalSdCardPath() != null) {
            list.addAll(recurseCamerDir(UtilStorage.getInstance().getExternalSdCardPath().toString() + "/DCIM/"));
            list.addAll(recurseCamerDir(UtilStorage.getInstance().getExternalSdCardPath().toString() + "/Camera/"));
            list.addAll(recurseCamerDir(UtilStorage.getInstance().getExternalSdCardPath().toString() + "/Photo/"));
        }
        if (UtilStorage.getInstance().getInnerSdCardPath() != null) {
            list.addAll(recurseCamerDir(UtilStorage.getInstance().getInnerSdCardPath().toString() + "/DCIM/"));
            list.addAll(recurseCamerDir(UtilStorage.getInstance().getInnerSdCardPath().toString() + "/Camera/"));
            list.addAll(recurseCamerDir(UtilStorage.getInstance().getInnerSdCardPath().toString() + "/Photo/"));
        }
        if (list.size() == 0) {
            list.addAll(recurseCamerDir(Environment.getExternalStorageDirectory().toString() + "/DCIM/"));
            list.addAll(recurseCamerDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath().toString()));
        }

        MY_ALBUM_BUCKETS = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            MY_ALBUM_BUCKETS[i] = list.get(i).intValue();
        }
    }

    private static ArrayList<Integer> recurseCamerDir(String dirPath) {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        File f = new File(dirPath);
        if (f != null && f.exists() && f.isDirectory()) {
            File dirs[] = f.listFiles();
            for (File dir : dirs) {
                if (dir.isDirectory()) {
                    LLog.i(TAG, "-------------------" + dir.getAbsolutePath());
                    Integer i = LetoolUtils.getBucketId(dir.getAbsolutePath());
                    if (!ret.contains(i))
                        ret.add(i);

                }
            }
        }
        return ret;
    }

    // Sort MediaSets by name
    public static class NameComparator implements Comparator<MediaSet> {

        public int compare(MediaSet set1, MediaSet set2) {
            int result = set1.getName().compareToIgnoreCase(set2.getName());
            if (result != 0)
                return result;
            return set1.getPath().toString().compareTo(set2.getPath().toString());
        }
    }
}
