package com.zxwl.frame.utils.rom;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.zxwl.frame.App;
import com.zxwl.frame.utils.PreferenceUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * author：Administrator
 * data:2019/12/17 16:34
 */
public class SystemUtil {
    public static final String SYS_EMUI = "sys_emui";
    public static final String SYS_MIUI = "sys_miui";
    public static final String SYS_FLYME = "sys_flyme";
    public static final String SYS_OTHER = "sys_other";
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
    private static final String KEY_EMUI_API_LEVEL = "ro.build.hw_emui_api_level";
    private static final String KEY_EMUI_VERSION = "ro.build.version.emui";
    private static final String KEY_EMUI_CONFIG_HW_SYS_VERSION = "ro.confg.hw_systemversion";

    public static String getSystem() {
        String sysType = PreferenceUtil.getString(App.getContext(), "sysType", "");
        if (TextUtils.isEmpty(sysType)) {
            try {
                sysType = SYS_OTHER;
                Properties prop = new Properties();
                prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
                if (prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                        || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                        || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null) {
                    sysType = SYS_MIUI;//小米
                } else if (prop.getProperty(KEY_EMUI_API_LEVEL, null) != null
                        || prop.getProperty(KEY_EMUI_VERSION, null) != null
                        || prop.getProperty(KEY_EMUI_CONFIG_HW_SYS_VERSION, null) != null) {
                    sysType = SYS_EMUI;//华为
                } else if (getMeizuFlymeOSFlag().toLowerCase().contains("flyme")) {
                    sysType = SYS_FLYME;//魅族
                }
                PreferenceUtil.getString(App.getContext(), "sysType", sysType);
            } catch (IOException e) {
                e.printStackTrace();
                return sysType;
            }
        }
        return sysType;
    }

    public static String getSystemNew() {
        String SYS = "";
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            if (!TextUtils.isEmpty(getSystemProperty(KEY_MIUI_VERSION_CODE, ""))
                    || !TextUtils.isEmpty(getSystemProperty(KEY_MIUI_VERSION_NAME, ""))
                    || !TextUtils.isEmpty(getSystemProperty(KEY_MIUI_INTERNAL_STORAGE, ""))) {
                SYS = SYS_MIUI;//小米
            } else if (!TextUtils.isEmpty(getSystemProperty(KEY_EMUI_API_LEVEL, ""))
                    || !TextUtils.isEmpty(getSystemProperty(KEY_EMUI_VERSION, ""))
                    || !TextUtils.isEmpty(getSystemProperty(KEY_EMUI_CONFIG_HW_SYS_VERSION, ""))) {
                SYS = SYS_EMUI;//华为
            } else if (getMeizuFlymeOSFlag().toLowerCase().contains("flyme")) {
                SYS = SYS_FLYME;//魅族
            }
            return SYS;
        } else {
            try {
                Properties prop = new Properties();
                prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
                if (prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                        || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                        || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null) {
                    SYS = SYS_MIUI;//小米
                } else if (prop.getProperty(KEY_EMUI_API_LEVEL, null) != null
                        || prop.getProperty(KEY_EMUI_VERSION, null) != null
                        || prop.getProperty(KEY_EMUI_CONFIG_HW_SYS_VERSION, null) != null) {
                    SYS = SYS_EMUI;//华为
                } else if (getMeizuFlymeOSFlag().toLowerCase().contains("flyme")) {
                    SYS = SYS_FLYME;//魅族
                }
            } catch (IOException e) {
                e.printStackTrace();
                return SYS;
            } finally {
                return SYS;
            }
        }
    }

    public static String getMeizuFlymeOSFlag() {
        return getSystemProperty("ro.build.display.id", "");
    }

    private static String getSystemProperty(String key, String defaultValue) {
        try {
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method get = clz.getMethod("get", String.class, String.class);
            return (String) get.invoke(clz, key, defaultValue);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    public static boolean isMeizuOrXiaoMiOrHuawei() {
        return SYS_MIUI.equals(getSystemNew()) || SYS_FLYME.equals(getSystemNew()) || SYS_EMUI.equals(getSystemNew());
    }

    public static boolean isMIUI() {
        return SYS_MIUI.equals(getSystemNew()) ;
    }

    /**
     * 判断我们的应用是否在白名单中
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) App.getInstance().getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(App.getInstance().getPackageName());
        }
        return isIgnoring;
    }

    /**
     * 申请白名单权限
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestIgnoreBatteryOptimizations() {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + App.getInstance().getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            App.getInstance().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
