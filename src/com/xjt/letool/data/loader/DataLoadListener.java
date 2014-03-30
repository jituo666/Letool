package com.xjt.letool.data.loader;

public interface DataLoadListener {
    public void onLoadingStarted();
    public void onLoadingFinished(boolean loadFailed);
}
