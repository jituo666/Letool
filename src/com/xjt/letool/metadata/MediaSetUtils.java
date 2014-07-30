
package com.xjt.letool.metadata;

import android.content.Context;
import android.os.Environment;

import com.xjt.letool.common.LLog;
import com.xjt.letool.preference.GlobalPreference;
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

    public static final int DOWNLOAD_BUCKET_ID = LetoolUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() + "/" + DOWNLOAD);
    public static final int IMPORTED_BUCKET_ID = LetoolUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() + "/" + IMPORTED);
    public static final int SNAPSHOT_BUCKET_ID = LetoolUtils.getBucketId(
            Environment.getExternalStorageDirectory().toString() + "/Pictures/Screenshots");

    private static int[] MY_ALBUM_BUCKETS_ID = new int[0]; // 这个静态的东西是有问题的...
    private static String MY_ALBUM_BUCKETS_DIR = "";

    public static void initializeMyAlbumBuckets(Context context) {
        String saveCameraDirs = GlobalPreference.getPhotoDirs(context);
        MY_ALBUM_BUCKETS_ID = new int[0];
        LLog.i(TAG, " ------initializeMyAlbumBuckets:" + saveCameraDirs);
        if (saveCameraDirs.length() > 0) {
            String dirs[] = saveCameraDirs.split("[|]");
            if (dirs.length > 0) {
                MY_ALBUM_BUCKETS_ID = new int[dirs.length];
                int i = 0;
                for (String s : dirs) {
                    LLog.i(TAG, " ------initializeMyAlbumBuckets dir:" + s);
                    MY_ALBUM_BUCKETS_ID[i++] = LetoolUtils.getBucketId(s);
                }
            }
        } else {
            ArrayList<Integer> list = new ArrayList<Integer>();
            MY_ALBUM_BUCKETS_DIR = "";
            String outerSdcard = UtilStorage.getInstance().getExternalSdCardPath();
            if (outerSdcard != null) {
                list.addAll(recurseCamerDir(outerSdcard + "/DCIM"));
                list.addAll(recurseCamerDir(outerSdcard + "/Camera"));
                list.addAll(recurseCamerDir(outerSdcard + "/Photo"));
                list.addAll(recurseCamerDir(outerSdcard + "/Photos"));
            }
            String innnerSdCard = UtilStorage.getInstance().getInnerSdCardPath();
            if (innnerSdCard != null) {
                list.addAll(recurseCamerDir(innnerSdCard + "/DCIM"));
                list.addAll(recurseCamerDir(innnerSdCard + "/Camera"));
                list.addAll(recurseCamerDir(innnerSdCard + "/Photo"));
                list.addAll(recurseCamerDir(innnerSdCard + "/Photos"));
            }
            if (list.size() == 0) {
                list.addAll(recurseCamerDir(Environment.getExternalStorageDirectory().toString() + "/DCIM/"));
                list.addAll(recurseCamerDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath().toString()));
            }

            MY_ALBUM_BUCKETS_ID = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                MY_ALBUM_BUCKETS_ID[i] = list.get(i).intValue();
            }
        }
        //common

    }

    public static int[] getBucketsIds() {
        return MY_ALBUM_BUCKETS_ID;
    }

    public static String getBucketsDirs() {
        return MY_ALBUM_BUCKETS_DIR;
    }

    private static ArrayList<Integer> recurseCamerDir(String dirPath) {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        File f = new File(dirPath);
        if (f != null && f.exists() && f.isDirectory()) {
            ret.add(LetoolUtils.getBucketId(dirPath));
            File dirs[] = f.listFiles();
            for (File dir : dirs) {
                if (dir.isDirectory()) {
                    LLog.i(TAG, "-------------------" + dir.getAbsolutePath());
                    Integer i = LetoolUtils.getBucketId(dir.getAbsolutePath());
                    if (!ret.contains(i)) {
                        ret.add(i);
                        MY_ALBUM_BUCKETS_DIR += dir.getAbsolutePath();
                        MY_ALBUM_BUCKETS_DIR += "|";
                    }

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
