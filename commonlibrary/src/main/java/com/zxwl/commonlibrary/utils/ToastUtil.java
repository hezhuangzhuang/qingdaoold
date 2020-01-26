package com.zxwl.commonlibrary.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Created by hcc on 16/8/4 21:18
 * 100332338@qq.com
 * <p/>
 * Toast工具类
 */
public class ToastUtil {

    private static String oldMsg;
    private static long time;
    private static Handler sHandler = new Handler(Looper.getMainLooper());

    public static void showShortToast(final Context context, final String msg) {
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                if (msg != null) {
                    if (!msg.equals(oldMsg)) { // 当显示的内容不一样时，即断定为不是同一个Toast
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                        time = System.currentTimeMillis();
                    } else {
                        // 显示内容一样时，只有间隔时间大于Toast.LENGTH_SHORT时才显示
                        if (System.currentTimeMillis() - time > Toast.LENGTH_SHORT) {
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                            time = System.currentTimeMillis();
                        }
                    }
                    oldMsg = msg;
                }
            }
        });
    }

    public static void showLongToast(final Context context, final String msg) {
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!msg.equals(oldMsg)) { // 当显示的内容不一样时，即断定为不是同一个Toast
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    time = System.currentTimeMillis();
                } else {
                    // 显示内容一样时，只有间隔时间大于Toast.LENGTH_SHORT时才显示
                    if (System.currentTimeMillis() - time > Toast.LENGTH_LONG) {
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                        time = System.currentTimeMillis();
                    }
                }
                oldMsg = msg;
            }
        });
    }
}
