
package com.xjt.letool;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Looper;

import com.xjt.letool.common.ThreadPool;
import com.xjt.letool.imagedata.blobcache.BlobCacheService;
import com.xjt.letool.metadata.DataManager;


/**
 * @Author Jituo.Xuan
 * @Date 9:07:03 PM May 17, 2014
 * @Comments:null
 */
public interface LetoolApp {

    public DataManager getDataManager();

    public BlobCacheService getBolbCacheService();

    public ThreadPool getThreadPool();

    public Context getActivityContext();

    public Looper getMainLooper();

    public ContentResolver getContentResolver();

    public Resources getResources();
}
