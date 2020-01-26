package com.zxwl.network.callback;


import com.zxwl.network.exception.ResponeThrowable;

import rx.Subscriber;

/**
 * Copyright 2015 蓝色互动. All rights reserved.
 * author：hw
 * data:2017/4/20 12:45
 * ClassName: ${Class_Name}
 */

public abstract class ErrorSubscriber<T> extends Subscriber<T> {
    @Override
    public void onError(Throwable e) {
        if (e instanceof ResponeThrowable) {
            onError((ResponeThrowable) e);
        } else {
            onError(new ResponeThrowable(e, 1000));
//            onError((ResponeThrowable) e);
        }
    }

    /**
     * 错误的回调
     */
    protected abstract void onError(ResponeThrowable responeThrowable);
}
