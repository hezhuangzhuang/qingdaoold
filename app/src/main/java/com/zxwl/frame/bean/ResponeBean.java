package com.zxwl.frame.bean;

/**
 * author：pc-20171125
 * data:2018/7/16 16:51
 */

public class ResponeBean {
    /**
     * command : RESPONSE_NONE
     * dateType : json
     * message : no message
     * receiveTime : 0
     * receiverId : admin
     * sendTime : 1531277455661
     * senderId : SERVER_ID
     * state : success
     */
    public String command;
    public String dateType;
    public String receiveTime;
    public String receiverId;
    public String sendTime;
    public String senderId;
    public String state;

    public String message;

    @Override
    public String toString() {
        return "ResponeBean{" +
                "command='" + command + '\'' +
                ", dateType='" + dateType + '\'' +
                ", receiveTime='" + receiveTime + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", sendTime='" + sendTime + '\'' +
                ", senderId='" + senderId + '\'' +
                ", state='" + state + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
