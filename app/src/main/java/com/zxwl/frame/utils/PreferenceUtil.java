package com.zxwl.frame.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.huawei.opensdk.sdkwrapper.login.LoginCenter;


/**
 * Created by hcc on 16/8/4 21:18
 * 100332338@qq.com
 * <p/>
 * SP缓存工具类
 */
public final class PreferenceUtil {
    public static void reset(final Context context) {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.clear();
        edit.apply();
    }

    private SharedPreferences sharedPreferences;

    public void init(Context context) {
        sharedPreferences = context.getSharedPreferences(context.getPackageName(),
                Context.MODE_PRIVATE);
    }


    public static String getStringWithUserName(Context context, String key, String defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(LoginCenter.getInstance().getAccount()+ "-" + key, defValue);
    }

    public static String getString(Context context, String key, String defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defValue);
    }

    public static long getLong(Context context, String key, long defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, defValue);
    }

    public static float getFloat(Context context, String key, float defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getFloat(key, defValue);
    }

    public static void putWithUserName(Context context, String key, String value) {
        putString(context, LoginCenter.getInstance().getAccount() + "-" + key, value);
    }

    public static void put(Context context, String key, String value) {
        putString(context, key, value);
    }

    public static void put(Context context, String key, int value) {
        putInt(context, key, value);
    }

    public static void put(Context context, String key, float value) {
        putFloat(context, key, value);
    }

    public static void put(Context context, String key, boolean value) {
        putBoolean(context, key, value);
    }

    private static void putFloat(Context context, String key, float value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static int getInt(Context context, String key, int defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defValue);
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defValue);
    }

    public static void putStringProcess(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("preference_mu", Context.MODE_MULTI_PROCESS);
        Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getStringProcess(Context context, String key, String defValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("preference_mu", Context.MODE_MULTI_PROCESS);
        return sharedPreferences.getString(key, defValue);
    }

    public static boolean hasString(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.contains(key);
    }

    private static void putString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void putLong(Context context, String key, long value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private static void putInt(Context context, String key, int value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void remove(Context context, String... keys) {
        if (keys != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                    context);
            Editor editor = sharedPreferences.edit();
            for (String key : keys) {
                editor.remove(key);
            }
            editor.apply();
        }
    }
}
