
package com.xjt.newpic.metadata;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;

import com.xjt.newpic.R;
import com.xjt.newpic.common.LLog;
import com.xjt.newpic.metadata.source.LocalAlbumSet.BucketEntry;
import com.xjt.newpic.preference.GlobalPreference;
import com.xjt.newpic.utils.LetoolUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public class MediaSetUtils {

    private static final String TAG = MediaSetUtils.class.getSimpleName();

    public static final String CAMERA = "Camera";
    public static final String DOWNLOAD = "downloads";
    public static final String IMPORTED = "Imported";
    public static final String SCREENSHOTS = "Screenshots";
    public static final String WEIXIN = "WeiXin";
    public static final String XUNLEI = "ThunderDownload";
    public static final String QQ_RECV = "QQfile_recv";
    public static final String QQ_FAVOR = "QQ_Favorite";
    public static final String QQ_IMAGE = "QQ_Images";
    public static final String SINA_WEIBO = "weibo";
    public static final String PHOTO = "Photos";


    public static final int CAMERA_BUCKET_ID1 = LetoolUtils.getBucketId("/storage/sdcard1" + "/" + "DCIM/Camera");
    public static final int CAMERA_BUCKET_ID0 = LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "DCIM/Camera");
    public static final int PHOTOS_BUCKET_ID = LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "Photos");
    public static final int DOWNLOAD_BUCKET_ID = LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "downloads");
    public static final int IMPORTED_BUCKET_ID = LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "Imported");
    public static final int SNAPSHOT_BUCKET_ID = LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "Pcitures/Screenshots");
    public static final int WEIXIN_BUCKET_ID = LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "WeiXin");
    public static final int XUNLEI_BUCKET_ID = LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "ThunderDownload");

    public static final int QQ_RECEIVE_BUCKET_ID = LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "Tencent/QQfile_recv");
    public static final int QQ_CHAT_BUCKET_ID = LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "tencent/QQ_Favorite");
    public static final int QQ_IMAGE_BUCKET_ID = LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "Tencent/QQ_Images");
    public static final int SINA_WEIBO_BUCKET_ID = LetoolUtils.getBucketId(Environment.getExternalStorageDirectory().toString() + "/" + "sina/weibo/weibo");

    public static final Comparator<MediaSet> NAME_COMPARATOR = new NameComparator();
    private static int[] MY_ALBUM_BUCKETS_ID = new int[0]; // 这个静态的东西是有问题的...
    private static String MY_ALBUM_BUCKETS_DIR = "";

    public static void initializeMyAlbumBuckets(Context context) {
        String saveCameraDirs = GlobalPreference.getPhotoDirs(context);
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
        LLog.i(TAG, "================1:" + name);
        if (MediaSetUtils.SCREENSHOTS.equalsIgnoreCase(name)) {
            name = res.getString(R.string.common_screenshot);
        } else if (MediaSetUtils.DOWNLOAD.equalsIgnoreCase(name)) {
            name = res.getString(R.string.common_download);
        } else if (MediaSetUtils.WEIXIN.equalsIgnoreCase(name)) {
            name = res.getString(R.string.common_weixin);
        } else if (MediaSetUtils.CAMERA.equalsIgnoreCase(name)) {
            name = res.getString(R.string.common_camera_source);
        } else if (MediaSetUtils.SINA_WEIBO.equalsIgnoreCase(name)) {
            name = res.getString(R.string.common_sina_weibo);
        } else if (MediaSetUtils.XUNLEI.equalsIgnoreCase(name)) {
            name = res.getString(R.string.common_xunlei);
        } else if (MediaSetUtils.QQ_RECV.equalsIgnoreCase(name)) {
            name = res.getString(R.string.common_qq_recv);
        } else if (MediaSetUtils.QQ_FAVOR.equalsIgnoreCase(name)) {
            name = res.getString(R.string.common_qq_favor);
        } else if (MediaSetUtils.IMPORTED.equalsIgnoreCase(name)) {
            name = res.getString(R.string.common_import);
        } else if (MediaSetUtils.PHOTO.equalsIgnoreCase(name)) {
            name = res.getString(R.string.common_photo);
        } else if (MediaSetUtils.QQ_IMAGE.equalsIgnoreCase(name)) {
            name = res.getString(R.string.common_qq_images);
        }
        LLog.i(TAG, "================2:" + name);
        return name;
    }

    public static int findBucket(BucketEntry entries[], int bucketId) {
        for (int i = 0, n = entries.length; i < n; ++i) {
            if (entries[i].bucketId == bucketId)
                return i;
        }
        return -1;
    }
}
