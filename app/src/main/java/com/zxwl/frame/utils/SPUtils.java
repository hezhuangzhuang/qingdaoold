package com.zxwl.frame.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class SPUtils {
    public static final String config = "config";

    public static SharedPreferences getSharedPreferences(Context ctx) {
        return ctx.getSharedPreferences(config, Activity.MODE_PRIVATE);
    }

    public static String getString(Context ctx, String key) {
        return getSharedPreferences(ctx).getString(key, "");
    }

    public static int getInt(Context ctx, String key) {
        return getSharedPreferences(ctx).getInt(key, 0);
    }

    public static float getFloat(Context ctx, String key) {
        return getSharedPreferences(ctx).getFloat(key, 0);
    }

    public static int getInt(Context ctx, String key, int defValue) {
        return getSharedPreferences(ctx).getInt(key, defValue);
    }

    public static boolean getBoolean(Context ctx, String key) {
        return getSharedPreferences(ctx).getBoolean(key, false);
    }

    public static void put(Context ctx, String key, Object object) {
        Editor editor = getSharedPreferences(ctx).edit();
        if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        }
        editor.commit();
    }

    /**
     * 保存List
     *
     * @param tag
     * @param datalist
     */
    public static <T> void setDataList(Context ctx, String tag, List<T> datalist) {
        Editor editor = getSharedPreferences(ctx).edit();
        if (null == datalist || datalist.size() <= 0)
            return;

        Gson gson = new Gson();
        //转换成json数据，再保存
        String strJson = gson.toJson(datalist);
        editor.clear();
        editor.putString(tag, strJson);
        editor.commit();
    }

    /**
     * 获取List
     *
     * @return
     */
    public static <T> List<T> getDataList(Context ctx, String tag) {
        List<T> datalist = new ArrayList<T>();
        String strJson = getSharedPreferences(ctx).getString(tag, null);
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<List<T>>() {
        }.getType());
        return datalist;
    }

    public static <T> List<T> getDataList(Context ctx, String tag,T t) {
        List<T> datalist = new ArrayList<T>();
        String strJson = getSharedPreferences(ctx).getString(tag, null);
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<List<T>>() {
        }.getType());
        return datalist;

    }

}
