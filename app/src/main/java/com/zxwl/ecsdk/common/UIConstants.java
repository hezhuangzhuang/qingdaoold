package com.zxwl.ecsdk.common;


public final class UIConstants {

    public static final String DEMO_TAG = "SDK_DEMO";

    public static final String CONF_ID = "conf_id";
    public static final String CALL_ID = "call_id";
    public static final String PEER_NUMBER = "peer_number";
    public static final String CALL_INFO = "call_info";
    public static final String CONTACT_INDEX = "contact_index";
    public static final String CONTACT_ID = "contact_id";
    public static final String IS_MEETING = "is_meeting";//是否是会议

    public static final String IS_CREATE = "IS_CREATE";//是否是自己创建

    public static final String IS_AUTO_ANSWER = "IS_AUTO_ANSWER";//是否需要自动接听

    public static final String SITE_NAME = "SITE_NAME";//通过接口查询到的会场名称

    public static final String JOIN_CONF = "JOIN_CONF";//加入会议

    public static final String IS_LOGIN = "IS_LOGIN";//是否登录

    public static final String DISCONNECT_NETWORK = "DISCONNECT_NETWORK";//网络是否断开

    public static final String REGISTER_RESULT = "REGISTER_RESULT";//注册码，834代表网络断开

    public static final String IS_LOGOUT = "IS_LOGOUT";//是否调用了登出接口

    public static final String REGISTER_RESULT_TEMP = "REGISTER_RESULT_TEMP";//注册码，临时调试用

    /**
     * The local contacts constant
     */
    public static final int CONTACT_REQUEST_LIST = 100;
    public static final int CONTACT_REQUEST_ITEM = 101;
    public static final int CONTACT_RESULT_CHANGE = 102;
    public static final int CONTACT_REQUEST_INFO = 103;
    public static final int CONTACT_RESULT_INFO = 104;

    public static final int REFRESH_CALL_RECORDS_LIST = 105;

    public static final int ENTER_ADDRESS_BOOK_CONTACTS_LIST = 106;
    public static final int ENTER_ADDRESS_BOOK_CONTACTS_NULL = 107;

    public static final String ENTER_ADDRESS_BOOK_CONTACT_INFO = "ldap_contact_info";

}
