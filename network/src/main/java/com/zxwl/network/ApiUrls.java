package com.zxwl.network;

import okhttp3.MediaType;

/**
 * author：pc-20171125
 * data:2018/12/18 15:15
 */
public class ApiUrls {

    public static final int SUCCESS = 1;

    public static String acCSRFToken = "";

    //    public static final String BASE_URL = "http://192.168.20.127:8080/czfk/";//李聪地址
//    public static final String BASE_URL = "http://192.168.20.134:8080/czfk/";//王鑫地址
    public static final String BASE_URL = "http://192.168.16.239:8098/czfk/";//测试地址

    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    /**
     * 获取sessionID方法名
     */
    public static final String getSessionId = "WEB_RequestSessionIDAPI";
    public static final String GET_SESSION_ID = "action.cgi?ActionID=WEB_RequestSessionIDAPI";

    /**
     * 请求认证
     */
    public static final String LOGIN = "loginAction.action";
    public static final String CHECK_IN = "visitorAction_savePadEntity.action";

    /**
     * 更换会话id
     */
    public static final String ChangeSession = "WEB_ChangeSessionIDAPI";
    public static final String CHANGE_SESSIONID = "action.cgi?ActionID=WEB_ChangeSessionIDAPI";


//    public static final String GUOHANG_BASE_URL = "http://113.57.147.178:9021/";

    /**
     * 获取用户列表
     */
//    public static final String GROUP_USER_LIST = "snap-app-im/api/groupuser/groupUserList";
    public static final String GROUP_USER_LIST = "siteAction_querySmcSites.action";

    /**
     * 聊天的根目录
     */
    public static final String IM_BASE_URL = "http://113.57.147.178:9021/";

    /**
     * 聊天上传文件
     */
    public static final String UPLOAD_FILE = "fileAction_uploadFile.action";

    /**
     * 查询群组
     */
    public static final String QUERY_ALL = "groupAction_queryAll.action";

    /**
     * 创建群组
     */
    public static final String CREATE_GROUP = "groupAction_addGroup.action";

    /**
     * 通知创建群组
     */
    public static final String CREATE_GROUP_Not = "group/add";

//    /**
//     * 解散群组
//     */
//    public static final String DEL_GROUP = "groupAction_delGroup.action";

    /**
     * 解散群组
     */
    public static final String DEL_GROUP = "/im/mobile/group/delete";

    /**
     * 入会
     */
    public static final String JOIN_CONF = "confAction_addSiteByAccessCode.action";

    /**
     * 登录鉴权
     */
    public static final String GET_LOGIN_INFO = "im/mobile/login";

    /**
     * 查看所有人员
     */
    public static final String FIND_ALL_BY_FIRST_CHAR = "/im/mobile/account/findAllByFirstChar";

    /**
     * 创建群组
     */
    public static final String NEW_CREATE_GROUP = "/im/mobile/group/createGroup";

    /**
     * 查找最上面的人员
     */
    public static final String FIND_ALL_GOV_ACCOUNT = "/im/mobile/account/findAllGovAccount";


}
