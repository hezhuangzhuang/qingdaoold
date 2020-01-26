package com.zxwl.frame.utils;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import java.lang.reflect.Method;


public class DensityUtil {
    public static int dip2px(float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(float pxValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 得到屏幕的高
     *
     * @param context 实体
     * @return 设备屏幕的高度
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        assert wm != null;
        return wm.getDefaultDisplay().getHeight();
    }

    /**
     * 得到屏幕的宽
     *
     * @param context 实体
     * @return 设备屏幕的宽度
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        assert wm != null;
        return wm.getDefaultDisplay().getWidth();
    }

    //获取虚拟按键的高度
//    public static int getNavigationBarHeight(Context context) {
//        int result = 0;
//        if (hasNavBar(context)) {
//            Resources res = context.getResources();
//            int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
//            if (resourceId > 0) {
//                result = res.getDimensionPixelSize(resourceId);
//            }
//        }
//        return result;
//    }

    /**
     * 检查是否存在虚拟按键栏
     *
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static boolean hasNavBar(Context context) {
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
        if (resourceId != 0) {
            boolean hasNav = res.getBoolean(resourceId);
            // check override flag
            String sNavBarOverride = getNavBarOverride();
            if ("1".equals(sNavBarOverride)) {
                hasNav = false;
            } else if ("0".equals(sNavBarOverride)) {
                hasNav = true;
            }
            return hasNav;
        } else { // fallback
            return !ViewConfiguration.get(context).hasPermanentMenuKey();
        }
    }

    /**
     * 判断虚拟按键栏是否重写
     *
     * @return
     */
    private static String getNavBarOverride() {
        String sNavBarOverride = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Class c = Class.forName("android.os.SystemProperties");
                Method m = c.getDeclaredMethod("get", String.class);
                m.setAccessible(true);
                sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
            } catch (Throwable e) {
            }
        }
        return sNavBarOverride;
    }


    private static int getInternalDimensionSize(Context context, String key) {
        int result = 0;
        try {
            int resourceId = context.getResources().getIdentifier(key, "dimen", "android");
            if (resourceId > 0) {
                result = Math.round(context.getResources().getDimensionPixelSize(resourceId) *
                        Resources.getSystem().getDisplayMetrics().density /
                        context.getResources().getDisplayMetrics().density);
            }
        } catch (Resources.NotFoundException ignored) {
            return 0;
        }
        return result;
    }

    public static int getStatusBarHeight(Context context){
        return getInternalDimensionSize(context, "status_bar_height");
    }

    public static int getNavigationBarHeight(Context context) {
        boolean mInPortrait = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (hasNavBar((Activity) context)) {
                String key;
                if (mInPortrait) {
                    key = "navigation_bar_height";
                } else {
                    key = "navigation_bar_height_landscape";
                }
                return getInternalDimensionSize(context, key);
            }
        }
        return result;
    }

    private static boolean hasNavBar(Activity activity) {
        //判断小米手机是否开启了全面屏,开启了，直接返回false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (Settings.Global.getInt(activity.getContentResolver(), "force_fsg_nav_bar", 0) != 0) {
                return false;
            }
        }
        //其他手机根据屏幕真实高度与显示高度是否相同来判断
        WindowManager windowManager = activity.getWindowManager();
        Display d = windowManager.getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            d.getRealMetrics(realDisplayMetrics);
        }

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
    }
}  