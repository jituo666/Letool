package com.xjt.letool.data.loader;

public interface DataLoadingListener {
    public void onLoadingStarted();
    public void onLoadingFinished(boolean loadFailed);
}
