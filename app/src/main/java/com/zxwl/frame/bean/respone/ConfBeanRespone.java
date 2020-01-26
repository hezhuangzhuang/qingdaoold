package com.zxwl.frame.bean.respone;

import java.util.List;
import java.util.Objects;

/**
 * author：pc-20171125
 * data:2019/1/7 14:50
 */
public class ConfBeanRespone {

    /**
     * code : 0
     * msg : success
     * data : {"smcConfId":"3710","confName":"APP_1_android测试会议","confStatus":3,"chairUri":null,"beginTime":"2019-01-27 13:57:11","endTime":"2019-01-27 15:57:11","accessCode":"02712620","siteStatusInfoList":[{"siteUri":"0271101@113.57.147.173","siteName":"hewei","siteType":0,"siteStatus":2,"microphoneStatus":1,"loudspeakerStatus":1},{"siteUri":"02711102@113.57.147.173","siteName":"02711102","siteType":0,"siteStatus":2,"microphoneStatus":1,"loudspeakerStatus":1}]}
     */
    public int code;
    public String msg;
    public DataBean data;

    public static class DataBean {
        /**
         * smcConfId : 3710
         * confName : APP_1_android测试会议
         * confStatus : 3
         * chairUri : null
         * beginTime : 2019-01-27 13:57:11
         * endTime : 2019-01-27 15:57:11
         * accessCode : 02712620
         * siteStatusInfoList : [{"siteUri":"0271101@113.57.147.173","siteName":"hewei","siteType":0,"siteStatus":2,"microphoneStatus":1,"loudspeakerStatus":1},{"siteUri":"02711102@113.57.147.173","siteName":"02711102","siteType":0,"siteStatus":2,"microphoneStatus":1,"loudspeakerStatus":1}]
         */
        public String smcConfId;
        public String confName;
        public int confStatus;
//        public Object chairUri;
        public String chairUri;
        public String creatorUri;
        public String beginTime;
        public String endTime;
        public String accessCode;
        public int confMode;//会议模式：0表示主席模式，1表示多画面模式
        public List<SiteStatusInfoListBean> siteStatusInfoList;

        public static class SiteStatusInfoListBean {
            /**
             * siteUri : 0271101@113.57.147.173
             * siteName : hewei
             * siteType : 0
             * siteStatus : 2
             * microphoneStatus : 1
             * loudspeakerStatus : 1
             */
            public String siteUri;
            public String siteName;
            public int siteType;
            public int siteStatus;//2:在线
            public int microphoneStatus;//1：打开
            public int loudspeakerStatus;//1:打开
            public int broadcastStatus;//1:正被广播
            public boolean isWatch;//正在观看

            public String videoSourceUri;//正在观看的会场id

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                SiteStatusInfoListBean that = (SiteStatusInfoListBean) o;
                return siteType == that.siteType &&
                        siteStatus == that.siteStatus &&
                        microphoneStatus == that.microphoneStatus &&
                        loudspeakerStatus == that.loudspeakerStatus &&
                        broadcastStatus == that.broadcastStatus &&
                        Objects.equals(siteUri, that.siteUri) &&
                        Objects.equals(siteName, that.siteName);
            }

            @Override
            public int hashCode() {
                return Objects.hash(siteUri, siteName, siteType, siteStatus, microphoneStatus, loudspeakerStatus, broadcastStatus);
            }
        }
    }
}
