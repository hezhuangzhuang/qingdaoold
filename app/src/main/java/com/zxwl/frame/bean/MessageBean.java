package com.zxwl.frame.bean;

/**
 * author：pc-20171125
 * data:2019/5/10 19:24
 */
public class MessageBean {
    public String userId;
    public String title;
    public String message;
    public String time;

    public MessageBean(String title, String message, String time) {
        this.title = title;
        this.message = message;
        this.time = time;
    }
}
