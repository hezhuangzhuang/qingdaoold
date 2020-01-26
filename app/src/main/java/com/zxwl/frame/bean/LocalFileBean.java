package com.zxwl.frame.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class LocalFileBean {
    @Unique
    String remotePath;

    String LocalPath;
    @Generated(hash = 545416339)
    public LocalFileBean(String remotePath, String LocalPath) {
        this.remotePath = remotePath;
        this.LocalPath = LocalPath;
    }
    @Generated(hash = 329032171)
    public LocalFileBean() {
    }
    public String getRemotePath() {
        return this.remotePath;
    }
    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }
    public String getLocalPath() {
        return this.LocalPath;
    }
    public void setLocalPath(String LocalPath) {
        this.LocalPath = LocalPath;
    }
}
