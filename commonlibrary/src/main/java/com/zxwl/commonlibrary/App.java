package com.zxwl.commonlibrary;

import android.app.Application;

/**
 * author：pc-20171125
 * data:2018/12/18 15:48
 */

public class App extends Application {

    public static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        init();
    }

    private void init() {
    }

    public static App getInstance() {
        return mInstance;
    }
}
