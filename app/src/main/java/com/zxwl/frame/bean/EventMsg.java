package com.zxwl.frame.bean;


/**
 * EventBus消息体
 */
public class EventMsg<T> {
    /**
     * 收到消息
     */
    public static final String RECEIVE_MESSAGE = "RECEIVE_MESSAGE";

    //刷新单个消息
    public static final String RECEIVE_SINGLE_MESSAGE = "RECEIVE_SINGLE_MESSAGE";

    //更新首页消息列表
    public static final String UPDATE_HOME = "UPDATE_HOME";

    //更新首页的消息已读状态
    public static final String UPDATE_HOME_READ = "UPDATE_HOME_READ";

    //更新首页消息列表
    public static final String UPDATE_HOME_NEW = "UPDATE_HOME_NEW";

    //更新群组列表
    public static final String UPDATE_GROUP = "UPDATE_GROUP";

    //更新首页消息提示
    public static final String HOME_HINT = "HOME_HINT";

    //掉线提示
    public static final String DISCONNECT = "DISCONNECT";

    //通话保持
    public static final String HOLDCALL = "HOLDCALL";

    //重新连上提示
    public static final String CONNECT = "CONNECT";

    //消息码
    private String msg;
    //消息体
    private Object body;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getBody() {
        return body;
    }

    /**
     * 优化方法，获取socket的消息体
     *
     * @return
     */
    public MessageBody getMessageBody() {
        return (MessageBody) body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public T t;

    public void setMessageData(T t) {
        this.t = t;
    }

    public T getMessageData() {
        return this.t;
    }

}
