package com.zxwl.frame.utils;

/**
 * author：pc-20171125
 * data:2018/7/12 13:57
 * <p>
 * 心跳返回的消息
 */
public class Messages {
    /**
     * 登录
     */
    public static final String LOGIN_R = "Login_R";

    /**
     * 登出
     */
    public static final String LOGOUT_R = "LogOut_R";

    /**
     * 查询部门
     */
    public static final String DEPARTMENT_R = "Department_R";

    /**
     * 查询人员
     */
    public static final String ADDRESS_R = "Address_R";

    /**
     * 保存会议
     */
    public static final String TIMELY_CONF_R = "TimelyConf_R";

    /**
     * 华为起会接口调用失败
     * 会议启动失败的时候增加了一个事件推送
     */
    public static final String START_CONF_FAILED_R = "StartConfFailed_R";

    /**
     * 弹出会议邀请
     */
    public static final String START_CONF_PUSH_R = "StartConfPush_R";

    /**
     * 查询列表
     */
    public static final String QUERY_CONF_R = "QueryConf_R";
    /**
     * 查询会议详情
     */
    public static final String QUERY_CONF_BY_ID_R = "QueryConfById_R";

    /**
     * 删除与会人
     */
    public static final String DEL_PARTICIPANT_R = "DelParticipant_R";

    /**
     * 添加与会人
     */
    public static final String ADD_PARTICIPANT_R = "AddParticipant_R";

    /**
     * 延长会议
     */
    public static final String DURATION_R = "Duration_R";

    /**
     * 结束会议
     */
    public static final String END_CONF_R = "EndConf_R";

    /**
     * 参会人列表
     */
    public static final String CONF_PARTICIPANT_R = "ConfParticipant_R";

    /**
     * 加入会议
     */
    public static final String JOIN_CONF_R = "JoinConf_R";

    /**
     * 拒绝加入会议
     */
    public static final String REFUSE_CONF_R = "RefuseConf_R";

    /**
     * 离开会议
     */
    public static final String LEAVE_CONF_R = "LeaveConf_R";


    /**
     * 修改信息
     */
    public static final String UPDATE_EMP_R = "UpdateEmp_R";

    /**
     * 检查更新
     */
    public static final String VERSION_R = "Version_R";

    /**
     * 登录华为失败
     */
    public static final String LOGIN_FAILED_R = "LoginFailed_R";


    /***********************自己添加的***************************/

    /**
     * 华为成功加入会议的标识
     */
    public static final String HUAWEI_CALL_GOING = "HUAWEI_CALL_GOING";

    /**
     * 创建会议时，添加参会人
     */
    public static final String ADD_ADDRESS = "ADD_ADDRESS";

    /**
     * 登录成功后更新列表
     */
    public static final String UPDATE_CONF_LIST = "UPDATE_CONF_LIST";

    /**
     * 来电接通
     */
    public static final String EXTRA_STATE_OFFHOOK = "EXTRA_STATE_OFFHOOK";

    /**
     * 电话挂断
     */
    public static final String EXTRA_STATE_IDLE = "EXTRA_STATE_IDLE";

    /**
     * 耳机状态改变
     */
    public static final String HEADSET_PLUG = "HEADSET_PLUG";

}
