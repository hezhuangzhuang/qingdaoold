package com.zxwl.network.bean.request;

/**
 * author：hw
 * data:2017/6/27 09:42
 * 用户信息
 */
public class User {
    public String userName;

    public String password;

    public User(String user, String password) {
        this.userName = user;
        this.password = password;
    }
}
