package com.xjt.letool;

public interface FutureListener<T> {
    public void onFutureDone(Future<T> future);
}
