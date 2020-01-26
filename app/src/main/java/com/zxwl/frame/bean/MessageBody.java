package com.zxwl.frame.bean;

public class MessageBody {
    /**
     * 私聊
     */
    public static final int TYPE_PERSONAL = 1;
    /**
     * 群聊
     */
    public static final int TYPE_COMMON = 2;

    /**
     * 由于sendId与receiveId设计为数字型，导致有可能重复，目前存在以下限制
     * sendId只能为用户ID，不能为群组ID
     */
    private String sendId;   // 发送者ID
    private String sendName; // 发送者名称

    /**
     * 当type = 1时，receiveId为用户ID
     * 当type = 2时，receiveId为群组ID
     */
    private String receiveId; //接收者ID
    private String receiveName; //接收者名称

    private MessageReal real; // 消息内容

    private Integer type; // 1:私聊；2:群聊

    public MessageBody(String sendId, String sendName, String receiveId, String receiveName, MessageReal real, Integer type) {
        this.sendId = sendId;
        this.sendName = sendName;
        this.receiveId = receiveId;
        this.receiveName = receiveName;
        this.real = real;
        this.type = type;
    }

    //=====================================================================
    public String getSendId() {
        return sendId;
    }

    public void setSendId(String sendId) {
        this.sendId = sendId;
    }

    public String getSendName() {
        return sendName;
    }

    public void setSendName(String sendName) {
        this.sendName = sendName;
    }

    public String getReceiveId() {
        return receiveId;
    }

    public void setReceiveId(String receiveId) {
        this.receiveId = receiveId;
    }

    public String getReceiveName() {
        return receiveName;
    }

    public void setReceiveName(String receiveName) {
        this.receiveName = receiveName;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public MessageReal getReal() {
        return real;
    }

    public void setReal(MessageReal real) {
        this.real = real;
    }

    @Override
    public String toString() {
        return "MessageBody{" +
                "sendId='" + sendId + '\'' +
                ", sendName='" + sendName + '\'' +
                ", receiveId='" + receiveId + '\'' +
                ", receiveName='" + receiveName + '\'' +
                ", real=" + real +
                ", type=" + type +
                '}';
    }
}
