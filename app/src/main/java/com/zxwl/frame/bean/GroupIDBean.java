package com.zxwl.frame.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class GroupIDBean {
    @Unique
    String groupid;

    boolean isGrou;

    @Generated(hash = 249228955)
    public GroupIDBean(String groupid, boolean isGrou) {
        this.groupid = groupid;
        this.isGrou = isGrou;
    }

    @Generated(hash = 1637897616)
    public GroupIDBean() {
    }

    public String getGroupid() {
        return this.groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public boolean getIsGrou() {
        return this.isGrou;
    }

    public void setIsGrou(boolean isGrou) {
        this.isGrou = isGrou;
    }
}
