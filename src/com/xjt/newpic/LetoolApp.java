
package com.xjt.newpic;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Looper;

import com.xjt.newpic.common.ThreadPool;
import com.xjt.newpic.imagedata.blobcache.BlobCacheService;
import com.xjt.newpic.metadata.DataManager;

/**
 * @Author Jituo.Xuan
 * @Date 9:07:03 PM May 17, 2014
 * @Comments:null
 */
public interface LetoolApp {

    public DataManager getDataManager();

    public BlobCacheService getBlobCacheService();

    public ThreadPool getThreadPool();

    public Context getAppContext();

    public Looper getMainLooper();

    public ContentResolver getContentResolver();

    public Resources getResources();
}
