package com.zxwl.network.bean;

public class BaseData_logicServer {
    //isPhoneRemove    :     0，正常  1，退出登录（其他地方有登录的）
    //licenseStatus    :     1许可证认证成功，其他失败（测试环境不需要认证）
    //isWebSocketOnline:     0，websocket离线 1，在线
    /**
     * responseCode : 1
     * message : 登录成功！
     * data : {}
     */

    private int responseCode;
    private String message;
    private Object data;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
