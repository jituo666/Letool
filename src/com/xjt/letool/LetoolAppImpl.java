
package com.xjt.letool;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.umeng.analytics.MobclickAgent;
import com.xjt.letool.common.ThreadPool;
import com.xjt.letool.imagedata.blobcache.BlobCacheService;
import com.xjt.letool.metadata.DataManager;
import com.xjt.letool.utils.LetoolUtils;

import android.app.Application;
import android.content.Context;


/**
 * @Author Jituo.Xuan
 * @Date 9:07:11 PM May 17, 2014
 * @Comments:null
 */
public class LetoolAppImpl extends Application implements LetoolApp {

    private static final String TAG = LetoolAppImpl.class.getSimpleName();

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
