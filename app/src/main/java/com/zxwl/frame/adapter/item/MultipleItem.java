package com.zxwl.frame.adapter.item;

/**
 * author：pc-20171125
 * data:2019/5/8 18:15
 */
public class MultipleItem {

    public static final int SEND_TEXT = 1;//发送纯文本
    public static final int FORM_TEXT = 2;//收到纯文本
    public static final int SEND_IMG = 3;//发送图片
    public static final int FORM_IMG = 4;//收到图片
    public static final int SEND_VOICE = 5;//发送语音
    public static final int FORM_VOICE = 6;//收到语音
    public static final int SEND_FILE = 7;//发送文件
    public static final int FORM_FILE = 8;//收到文件
    public static final int NOTIFY = 9;//收到通知

    public static final int SEND_VIDEO_CALL = 10;//发出的视频呼叫
    public static final int FORM_VIDEO_CALL = 11;//收到的视频呼叫

    public static final int SEND_VOICE_CALL = 12;//发出的语音呼叫
    public static final int FORM_VOICE_CALL = 13;//收到的语音呼叫

    public static final int TEXT_SPAN_SIZE = 3;
    public static final int IMG_SPAN_SIZE = 1;
    public static final int IMG_TEXT_SPAN_SIZE = 4;
    public static final int IMG_TEXT_SPAN_SIZE_MIN = 2;
}
