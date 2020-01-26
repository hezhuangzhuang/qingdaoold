package com.zxwl.network.bean;

/**
 * author：pc-20171125
 * data:2019/5/14 11:06
 */
public class ImBean {
    /**
     * code : 0
     * msg : success
     * data : {"fileName":"会控.jpg","filePath":"http://113.57.147.178:9021/upload/f4897559-bc1c-40a9-9ca7-c7ac3e807500.jpg"}
     */
    public int code;
    public String msg;
    public DataBean data;

    public static class DataBean {
        /**
         * fileName : 会控.jpg
         * filePath : http://113.57.147.178:9021/upload/f4897559-bc1c-40a9-9ca7-c7ac3e807500.jpg
         */
        public String fileName;
        public String filePath;
    }

//    public int code;
//    public String msg;
//    public String data;
//
    public final static String SUCCESS = "success";


}
