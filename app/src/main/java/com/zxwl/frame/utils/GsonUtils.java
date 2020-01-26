package com.zxwl.frame.utils;

import com.google.gson.Gson;

public class GsonUtils {
    private static Gson gson = new Gson();

    /**
     * json解析
     *
     * @param json
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> T jsonResolve(String json, Class<T> tClass) {
        return gson.fromJson(json, tClass);
    }

    /**
     * 对象型json解析
     *
     * @param obj
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> T jsonOBJ_Resolve(Object obj, Class<T> tClass) {
        return gson.fromJson(gson.toJson(obj), tClass);
    }

    /**
     * javaBean转型为JSON字符串
     *
     * @param obj
     * @return
     */
    public static String Bean2Json(Object obj) {
        return gson.toJson(obj);
    }
}
