package com.zxwl.network.bean;

/**
 * author：hw
 * data:2017/6/2 15:14
 */
public class BaseData_BackServer {
    private int code;
    private String msg;
    private Object data;

    //消息成功的回调
    public static final int SUCCESS = 0;

    public BaseData_BackServer() {
    }

    public BaseData_BackServer(int code, String msg, String data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BaseData{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
