
package com.xjt.letool.utils;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

public class UtilStorage {

    private static final String TAG = UtilStorage.class.getSimpleName();

    private ArrayList<String> cache = new ArrayList<String>();
    private File vold_fastb;
    private final static String HEAD = "dev_mount";
    private static volatile UtilStorage storageUtil;
    private static boolean hasInnerSdCard;
    private static boolean hasExternalSdCard;
    private static String externalSdcard;
    private static String innerSdcard;

    private static final String EXTERNAL_STORAGE = Environment.getExternalStorageDirectory()
            .getAbsolutePath();

    /**
     * 是否有内置sd卡
     */
    public boolean isHasInnerSdCard() {
        return hasInnerSdCard;
    }

    /**
     * 是否有外置sd卡
     */
    public boolean isHasExternalSdCard() {
        return hasExternalSdCard;
    }

    public static synchronized UtilStorage getInstance() {
        if (storageUtil == null) {
            storageUtil = new UtilStorage();
            storageUtil.init();
        }
        return storageUtil;
    }

    /**
     * 获取内置sd卡容量
     */
    public long getInnerSdCardCapacity() {
        if (hasInnerSdCard) {
            return getTotalStorage(innerSdcard);
        } else {
            return 0;
        }
    }

    /**
     * 获取外置sd卡容量
     */
    public long getExternalSdCardCapacity() {
        if (hasExternalSdCard) {
            if (getTotalStorage(externalSdcard) == getTotalStorage(innerSdcard)) {
                return 0;
            }
            return getTotalStorage(externalSdcard);
        } else {
            return 0;
        }
    }

    /**
     * 获取内置sd卡路径
     */
    public String getInnerSdCardPath() {
        if (innerSdcard != null) {
            return innerSdcard;
        } else {
            return null;
        }
    }

    /**
     * 获取外置sd卡路径
     */
    public String getExternalSdCardPath() {
        if (externalSdcard != null) {
            return externalSdcard;
        } else {
            return null;
        }
    }

    /**
     * 获取内置sd卡可用容量
     */
    public long getInnerSdCardAvalible() {
        if (hasInnerSdCard) {
            return getAvailableStorage(innerSdcard);
        } else {
            return 0;
        }
    }

    /**
     * 获取外置sd卡可用容量
     */
    public long getExternalSdCardAvalible() {
        if (hasExternalSdCard) {
            if (getAvailableStorage(externalSdcard) == getAvailableStorage(innerSdcard)) {
                return 0;
            }
            return getAvailableStorage(externalSdcard);
        } else {
            return 0;
        }
    }

    /**
     * 获取内置sd卡已用容量
     */
    public long getInnerSdCardUsed() {
        return getInnerSdCardCapacity() - getInnerSdCardAvalible();
    }

    /**
     * 获取外置sd卡已用容量
     */
    public long getExternalSdCardUsed() {
        return getExternalSdCardCapacity() - getExternalSdCardAvalible();
    }

    // 废弃的原因使用该方法获取不了部分手机的sdcard(华为U9508)
    private void deprecated_init() {
        vold_fastb = new File(Environment.getRootDirectory().getAbsoluteFile() + File.separator
                + "etc" + File.separator + "vold.fstab");
        try {
            // 解析vold.fstab
            initVoldFstabToCache();
        } catch (IOException e) {
            Log.w(TAG, "init vold fstab error.", e);
        }
        for (int i = 0; i < cache.size(); i++) {
            String temp = cache.get(i);
            // dev_mount sdcard /mnt/sdcard 1 /devices/platform/mmci-omap-hs.1/mmc_host/mmc0
            String[] info = temp.split(" ");
            // This will parse Moto path "/mnt/sdcard-ext:none:lun1"
            String[] paths = info[2].split(":");
            String path = paths[0];

            // sdcard, sdcard1, extsdcard
            if (info[1].startsWith("sdcard")) {
                if (!EXTERNAL_STORAGE.equals(info[2])) {
                    hasExternalSdCard = true;
                    externalSdcard = path;
                } else {
                    hasInnerSdCard = true;
                    innerSdcard = path;
                }
            }
        }
        // 若没有明确标示有外置sd卡，且有sdcard，则这个卡的路径为外置sdcard的路径
        if (!hasExternalSdCard && hasInnerSdCard) {
            hasExternalSdCard = true;
            externalSdcard = innerSdcard;
            // 如果配置文件里的路径和api得到的路径不一致，则有内置sd卡
            if (!EXTERNAL_STORAGE.equals(externalSdcard)) {
                hasInnerSdCard = true;
                innerSdcard = Environment.getExternalStorageDirectory().toString();
            } else {
                // 否则，仅有外置sd卡
                hasInnerSdCard = false;
                innerSdcard = null;
            }
        }

        if (hasInnerSdCard) {
            Log.i(TAG,
                    "inner sdcard : " + humanReadableByteCount(getAvailableStorage(innerSdcard), false));
        }
        if (hasExternalSdCard) {
            Log.i(TAG,
                    "external sdcard : "
                            + humanReadableByteCount(getAvailableStorage(externalSdcard), false));
        }
    };

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private void initVoldFstabToCache() throws IOException {
        cache.clear();
        BufferedReader br = new BufferedReader(new FileReader(vold_fastb));
        String tmp = null;
        while ((tmp = br.readLine()) != null) {
            // the words startsWith "dev_mount" are the SD info
            if (tmp.startsWith(HEAD)) {
                cache.add(tmp);
            }
        }
        br.close();
        cache.trimToSize();
    }

    /**
     * 获取存储卡的剩余容量，单位为字节
     * 这个方法用于判断路径下剩余空间，在目录不存在的情况下，会新建目录
     * 存储目录的建立应该放在相机开启时
     * 
     * @param pPath 存储卡路径
     * @return 剩余容量（byte） 为0证明该卡不可访问
     */
    public long getAvailableStorage(String pPath) {
        long availableStorage = 0;
        try {
            // 判断path文件是否存在
            File dir = new File(pPath);
            if (dir.isFile()) {
                return 0;
            }

            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                }
            }

            StatFs stat = new StatFs(pPath);
            availableStorage = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
        } catch (Exception e) {

        }

        return availableStorage;
    }

    private long getTotalStorage(String path) {
        String storageDirectory = path;
        if (path == null) {
            return 0;
        }
        StatFs stat = new StatFs(storageDirectory);
        return (long) stat.getBlockCount() * (long) stat.getBlockSize();
    }

    private void init() {
        hasInnerSdCard = true;
        innerSdcard = Environment.getExternalStorageDirectory().getAbsolutePath();

        HashSet<String> sdcards = getExternalMounts();
        Iterator<String> itSdcards = sdcards.iterator();
        while (itSdcards.hasNext()) {
            String item = itSdcards.next();
            if (item.equals(innerSdcard)) {
                itSdcards.remove();
            }
        }

        // 如果手机具有多个sdcard，仅识别其中一个
        itSdcards = sdcards.iterator();
        if (itSdcards.hasNext()) {
            hasExternalSdCard = true;
            externalSdcard = itSdcards.next();
        }
    }

    private HashSet<String> getExternalMounts() {
        final HashSet<String> out = new HashSet<String>();
        String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
        StringBuffer sb = new StringBuffer();
        try {
            final Process process = new ProcessBuilder().command("mount")
                    .redirectErrorStream(true).start();
            process.waitFor();
            final InputStream is = process.getInputStream();
            final byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1) {
                sb.append(new String(buffer, "UTF-8"));
            }
            is.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // parse output
        final String[] lines = sb.toString().split("\n");
        for (String line : lines) {
            if (!line.toLowerCase(Locale.US).contains("asec")) {
                if (line.matches(reg)) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        if (part.startsWith("/"))
                            if (!part.toLowerCase(Locale.US).contains("vold"))
                                out.add(part);
                    }
                }
            }
        }
        return out;
    }
}
