package com.zxwl.frame.utils;

import com.zxwl.frame.R;

/**
 * author：pc-20171125
 * data:2019/4/12 14:47
 */
public class Constant {
    public static final String TAG = "ImDemo";

    public static final String AUTO_LOGIN = "AUTO_LOGIN";//是否自动登录

    //是否设置了权限
    public static final String SETTING_PERMISSION = "SETTING_PERMISSION";

    //最长录音时间
    public static final int DEFAULT_MAX_AUDIO_RECORD_TIME_SECOND = 12;

    //语音存放位置
    public static final String AUDIO_SAVE_DIR = FileUtils.getDir("audio");

    //视频存放位置
    public static final String VIDEO_SAVE_DIR = FileUtils.getDir("video");

    //照片存放位置
    public static final String PHOTO_SAVE_DIR = FileUtils.getDir("photo");

    //socket服务需要用到的字段
    public static final String USER_ID = "USER_ID";

    public static final String HAS_LOGIN = "HAS_LOGIN";//是否已登陆
    public static final String USER_NAME = "USER_NAME";//登录账号
    public static final String PASS_WORD = "PASS_WORD";//登录密码

    public static final String DISPLAY_NAME = "DISPLAY_NAME";//显示的名称
    public static final String CURRENT_ID = "CURRENT_ID";//当前登录账号的id

    public static final String SMC_IP = "SMC_IP";
    public static final String SMC_PORT = "SMC_PORT";

    public static final String SIP_ACCOUNT = "SIP_ACCOUNT";
    public static final String SIP_PASSWORD = "SIP_PASSWORD";


    public static String CurrUserName = "";
    public static String CurrDisPlayName = "";
    public static String CurrPassWrod = "";
    public static int CurrID = -1;

    //是否保持通话
    public static boolean isholdCall = false;


    //TODO:1121添加代码
    public static int[] personalHeads = {
            R.mipmap.ic_personal_head_one,
            R.mipmap.ic_personal_head_two,
            R.mipmap.ic_personal_head_three,
            R.mipmap.ic_personal_head_four
    };

    public static int[] groupHeads = {
            R.mipmap.ic_group_head_one,
            R.mipmap.ic_group_head_two,
            R.mipmap.ic_group_head_three,
            R.mipmap.ic_group_head_four
    };

    /**
     * 生成4以内随机数
     *
     * @return
     */
    public static int getRandon() {
//        return (int) (Math.random() * 4);
        return 0;
    }

    public static int getPersonalHeadRes() {
        return personalHeads[getRandon()];
    }

    public static int getGroupHeadRes() {
        return groupHeads[getRandon()];
    }

}
