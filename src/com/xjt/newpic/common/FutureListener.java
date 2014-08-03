package com.xjt.newpic.common;

public interface FutureListener<T> {
    public void onFutureDone(Future<T> future);
}
