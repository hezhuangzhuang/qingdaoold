package com.zxwl.testlibrary;

import android.content.Context;
import android.widget.Toast;

/**
 * author：pc-20171125
 * data:2018/12/19 15:10
 */
public class UtilsTest {
    public boolean falg = false;
    public boolean isRelease = false;
    public boolean isDebug = false;

    public String getString(String name) {
        return "测试的消息:";
    }

    public void sayHello(Context context) {
        Toast.makeText(context, "falg:" + falg + ",isRelease:" + isRelease, Toast.LENGTH_SHORT).show();
    }
}
