
package com.xjt.newpic.metadata;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;

import com.xjt.newpic.R;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.metadata.source.LocalAlbumSet.BucketEntry;
import com.xjt.newpic.utils.LetoolUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public class MediaSetUtils {

    private static final String TAG = MediaSetUtils.class.getSimpleName();

    public static final int[] BUCKETIDS = new int[] {
            LetoolUtils.getBucketId("/storage/sdcard1" + "/" + "DCIM/Camera"),
            LetoolUtils.getBucketId("/storage/extSdCard" + "/" + "DCIM/Camera"),
            LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "DCIM/Camera"),
            LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "Camera"),
            LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "Photos"),
            LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "Pictures/Screenshots"),
            LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "WeiXin"),
            LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "Tencent/QQfile_recv"),
            LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "tencent/QQ_Favorite"),
            LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "Tencent/QQ_Images"),
            LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "sina/weibo/weibo"),
            LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "ThunderDownload"),
            LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "downloads"),
            LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "Imported")
    };

    public static final Comparator<MediaSet> NAME_COMPARATOR = new NameComparator();
    private static int[] MY_ALBUM_BUCKETS_ID = new int[0]; // 这个静态的东西是有问题的...
    private static String MY_ALBUM_BUCKETS_DIR = "";

    public static void initializeMyAlbumBuckets(Context context) {
        MY_ALBUM_BUCKETS_ID = new int[0];
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

    public static String getLocalizedName(Resources res, String name) {
        String[] albumNames = res.getStringArray(R.array.album_names);
        String[] albumAlias = res.getStringArray(R.array.album_alias);
        for (int i = 0; i < albumNames.length; i++) {
            if (albumNames[i].equalsIgnoreCase(name)) {
                return albumAlias[i];
            }
        }
        return name;
    }

    public static int findBucket(BucketEntry entries[], String bucketName) {
        LLog.i(TAG, " -----bucketName e:" + bucketName);
        for (int i = 0, n = entries.length; i < n; ++i) {
            if (entries[i].bucketName.equalsIgnoreCase(bucketName)) {
                LLog.i(TAG, " bucketName r:" + i);
                return i;
            }
        }
        LLog.i(TAG, " -----bucketName r:" + (-1));
        return -1;
    }

    public static int findBucket(BucketEntry entries[], int bucketId) {
        for (int i = 0, n = entries.length; i < n; ++i) {
            if (entries[i].bucketId == bucketId)
                return i;
        }
        return -1;
    }
}
