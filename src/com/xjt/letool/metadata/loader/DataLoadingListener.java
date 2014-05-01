package com.xjt.letool.metadata.loader;

public interface DataLoadingListener {
    public void onLoadingStarted();
    public void onLoadingFinished(boolean loadFailed);
}
