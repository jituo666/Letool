package com.xjt.letool.common;

public interface FutureListener<T> {
    public void onFutureDone(Future<T> future);
}
