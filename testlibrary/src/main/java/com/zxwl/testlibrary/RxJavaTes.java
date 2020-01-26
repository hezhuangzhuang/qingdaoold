package com.zxwl.testlibrary;

import android.graphics.Bitmap;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * author：Administrator
 * data:2020/1/2 20:02
 */
public class RxJavaTes {
    public void map() {
        Observable.just("map")
                .map(new Func1<String, Bitmap>() {
                    @Override
                    public Bitmap call(String s) {
                        return null;
                    }
                })
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {

                    }
                });
    }
}
