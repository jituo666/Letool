
package com.xjt.newpic;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.umeng.analytics.MobclickAgent;
import com.xjt.newpic.activities.NpGuideActivity;
import com.xjt.newpic.activities.NpMainActivity;
import com.xjt.newpic.activities.NpMediaActivity;
import com.xjt.newpic.common.ThreadPool;
import com.xjt.newpic.imagedata.blobcache.BlobCacheService;
import com.xjt.newpic.metadata.DataManager;
import com.xjt.newpic.preference.GlobalPreference;
import com.xjt.newpic.utils.LetoolUtils;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

/**
 * @Author Jituo.Xuan
 * @Date 9:07:11 PM May 17, 2014
 * @Comments:null
 */
public class NpAppImpl extends Application implements NpApp {

    private static final String TAG = NpAppImpl.class.getSimpleName();

    private BlobCacheService mImageCacheService;
    private Object mLock = new Object();
    private DataManager mDataManager;
    private ThreadPool mThreadPool;

    @Override
    public void onCreate() {
        super.onCreate();
        LetoolUtils.initialize(this);
        initImageLoader(this);
        // Umeng
        MobclickAgent.updateOnlineConfig(this);
    }

    @Override
    public synchronized DataManager getDataManager() {
        if (mDataManager == null) {
            mDataManager = new DataManager(this);
            mDataManager.initializeSourceMap();
        }
        return mDataManager;
    }

    @Override
    public synchronized ThreadPool getThreadPool() {
        if (mThreadPool == null) {
            mThreadPool = new ThreadPool();
        }
        return mThreadPool;
    }

    @Override
    public BlobCacheService getBlobCacheService() {

        synchronized (mLock) {
            if (mImageCacheService == null) {
                mImageCacheService = new BlobCacheService(this);
            }
            return mImageCacheService;
        }
    }

    @Override
    public Context getAppContext() {
        return this;
    }

    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                //.writeDebugLogs() // Remove for release app
                .build();
        ImageLoader.getInstance().init(config);
    }
}
