package com.zxwl.frame.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

/**
 * author：pc-20171125
 * data:2019/5/11 14:46
 */
@Entity
public class ChatBean {
    @Id(autoincrement = true)
    public Long id;

    /**
     * 消息类型，跟MultipleItem对应
     */
    public int type;

    public String name;
    public Date time;
    public String textContent;
    public String duration;

    public String sendId;//发送人id
    public String sendName;//发送人name

    public String receiveId;//接收人id
    public String receiveName;//接收人name

    public boolean isSend = false;//true:发出去的消息，false：收到的消息

    public boolean isRead = true;//true:代表消息已读，false：代表消息未读

    public String conversationId;//会话Id
    public String conversationUserName;//对方名称

    public boolean isGroup = false;//true:代表是群消息，false：个人消息

    public ChatBean(int type, String name, Date time, String textContent) {
        this.type = type;
        this.name = name;
        this.time = time;
        this.textContent = textContent;
    }

    public ChatBean(int type, String name, Date time, String textContent, String duration) {
        this.type = type;
        this.name = name;
        this.time = time;
        this.textContent = textContent;
        this.duration = duration;
    }

    /**
     * 转型为ChatBeanLM
     *
     * @return
     */
    public ChatBeanLM toChatBeanLM() {
        return new ChatBeanLM(id, type, name, time, textContent, duration, sendId, sendName, receiveId, receiveName, isSend, isRead, conversationId, conversationUserName, isGroup);
    }

    @Generated(hash = 2011994906)
    public ChatBean(Long id, int type, String name, Date time, String textContent, String duration, String sendId, String sendName, String receiveId, String receiveName,
            boolean isSend, boolean isRead, String conversationId, String conversationUserName, boolean isGroup) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.time = time;
        this.textContent = textContent;
        this.duration = duration;
        this.sendId = sendId;
        this.sendName = sendName;
        this.receiveId = receiveId;
        this.receiveName = receiveName;
        this.isSend = isSend;
        this.isRead = isRead;
        this.conversationId = conversationId;
        this.conversationUserName = conversationUserName;
        this.isGroup = isGroup;
    }

    @Generated(hash = 1872716502)
    public ChatBean() {
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getTime() {
        return this.time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getTextContent() {
        return this.textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getDuration() {
        return this.duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSendId() {
        return this.sendId;
    }

    public void setSendId(String sendId) {
        this.sendId = sendId;
    }

    public String getSendName() {
        return this.sendName;
    }

    public void setSendName(String sendName) {
        this.sendName = sendName;
    }

    public String getReceiveId() {
        return this.receiveId;
    }

    public void setReceiveId(String receiveId) {
        this.receiveId = receiveId;
    }

    public String getReceiveName() {
        return this.receiveName;
    }

    public void setReceiveName(String receiveName) {
        this.receiveName = receiveName;
    }

    public boolean getIsSend() {
        return this.isSend;
    }

    public void setIsSend(boolean isSend) {
        this.isSend = isSend;
    }

    public boolean getIsRead() {
        return this.isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public String getConversationId() {
        return this.conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationUserName() {
        return this.conversationUserName;
    }

    public void setConversationUserName(String conversationUserName) {
        this.conversationUserName = conversationUserName;
    }

    public boolean getIsGroup() {
        return this.isGroup;
    }

    public void setIsGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ChatBean{" +
                "id=" + id +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", time=" + time +
                ", textContent='" + textContent + '\'' +
                ", duration='" + duration + '\'' +
                ", sendId='" + sendId + '\'' +
                ", sendName='" + sendName + '\'' +
                ", receiveId='" + receiveId + '\'' +
                ", receiveName='" + receiveName + '\'' +
                ", isSend=" + isSend +
                ", isRead=" + isRead +
                ", conversationId='" + conversationId + '\'' +
                ", conversationUserName='" + conversationUserName + '\'' +
                ", isGroup=" + isGroup +
                '}';
    }
}
