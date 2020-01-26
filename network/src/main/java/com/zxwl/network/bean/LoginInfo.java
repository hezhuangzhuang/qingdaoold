package com.zxwl.network.bean;

public class LoginInfo {
    /**
     * id : 1
     * accountName : admin
     * name : admin
     * password : 123456
     * depId : 3
     * sipAccount : 027991
     * sipPassword : admin123
     * updateTime : 2019-10-29 18:59:53
     * scIp : 192.168.17.95
     * scPort : 5060
     * seesionId : null
     * deviceId : null
     * depName : null
     */

    private int id;
    private String accountName;
    private String name;
    private String password;
    private String depId;
    private String sipAccount;
    private String sipPassword;
    private String updateTime;
    private String scIp;
    private String scPort;
    private Object seesionId;
    private Object deviceId;
    private Object depName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDepId() {
        return depId;
    }

    public void setDepId(String depId) {
        this.depId = depId;
    }

    public String getSipAccount() {
        return sipAccount;
    }

    public void setSipAccount(String sipAccount) {
        this.sipAccount = sipAccount;
    }

    public String getSipPassword() {
        return sipPassword;
    }

    public void setSipPassword(String sipPassword) {
        this.sipPassword = sipPassword;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getScIp() {
        return scIp;
    }

    public void setScIp(String scIp) {
        this.scIp = scIp;
    }

    public String getScPort() {
        return scPort;
    }

    public void setScPort(String scPort) {
        this.scPort = scPort;
    }

    public Object getSeesionId() {
        return seesionId;
    }

    public void setSeesionId(Object seesionId) {
        this.seesionId = seesionId;
    }

    public Object getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Object deviceId) {
        this.deviceId = deviceId;
    }

    public Object getDepName() {
        return depName;
    }

    public void setDepName(Object depName) {
        this.depName = depName;
    }
}
