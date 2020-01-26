package com.zxwl.network.bean;

import java.util.List;

/**
 * author：hw
 * data:2017/6/2 15:14
 */
public class BaseData<T> {
    public List<T> data;
    public String error;
    public int success;
    public String message;
    public String result;
    public int responseCode;

    public final  static int SUCCESS = 1;

    @Override
    public String toString() {
        return "BaseData{" +
                "data='" + data + '\'' +
                ", success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
