package com.zxwl.frame.bean;

public class MessageReal {
    /**
     * 文字
     */
    public static final int TYPE_STR = 1;

    /**
     * 图片
     */
    public static final int TYPE_IMG = 2;

    /**
     * 表情
     */
    public static final int TYPE_EMOJI = 3;

    /**
     * 附件
     */
    public static final int TYPE_APPENDIX = 4;

    /**
     * 通知
     */
    public static final int TYPE_NOTIFY = 5;

    /**
     * 视频呼叫
     */
    public static final int TYPE_VIDEO_CALL =6;

    /**
     * 语音呼叫
     */
    public static final int TYPE_VOICE_CALL =7;

    /**
     * 当type = 1，表示文本消息
     * 当type = 2，表示缩略图地址
     * 当type = 3，表示表情标签
     * 当type = 4，表情语音文件地址
     */
    private String message;

    /*1:文字; 2:图片; 3:表情； 4：语音*/
    private Integer type;

    /*
     * 完整图片地址
     * 当type = 2时，此参数才有值
     * */
    private String imgUrl;

    public MessageReal(String message, Integer type, String imgUrl) {
        this.message = message;
        this.type = type;
        this.imgUrl = imgUrl;
    }

    //=====================================================================
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    @Override
    public String toString() {
        return "MessageReal{" +
                "message='" + message + '\'' +
                ", type=" + type +
                ", imgUrl='" + imgUrl + '\'' +
                '}';
    }
}
