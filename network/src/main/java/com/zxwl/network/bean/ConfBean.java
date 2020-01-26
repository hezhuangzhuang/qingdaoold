package com.zxwl.network.bean;

import java.util.List;

/**
 * author：pc-20171125
 * data:2019/10/30 20:53
 */
public class ConfBean {

    /**
     * code : 0
     * msg : success
     * data : [{"smcConfId":"1786","confName":"周勇测试会议","confStatus":3,"chairUri":null,"beginTime":"2019-10-30 20:40:43","endTime":"2019-10-30 22:40:43","accessCode":"02710035","siteStatusInfoList":[{"siteUri":"027991","siteName":"027991","siteType":0,"siteStatus":3,"microphoneStatus":0,"loudspeakerStatus":0},{"siteUri":"027992","siteName":"027992","siteType":0,"siteStatus":3,"microphoneStatus":0,"loudspeakerStatus":0}]}]
     */

    public int code;
    public String msg;
    public List<DataBean> data;

    public static class DataBean {
        /**
         * smcConfId : 1786
         * confName : 周勇测试会议
         * confStatus : 3
         * chairUri : null
         * beginTime : 2019-10-30 20:40:43
         * endTime : 2019-10-30 22:40:43
         * accessCode : 02710035
         * siteStatusInfoList : [{"siteUri":"027991","siteName":"027991","siteType":0,"siteStatus":3,"microphoneStatus":0,"loudspeakerStatus":0},{"siteUri":"027992","siteName":"027992","siteType":0,"siteStatus":3,"microphoneStatus":0,"loudspeakerStatus":0}]
         */
        public String smcConfId;
        public String confName;
        public int confStatus;
        public Object chairUri;
        public String beginTime;
        public String creatorUri;
        public String creatorName;
        public String endTime;
        public String accessCode;
        public List<SiteStatusInfoListBean> siteStatusInfoList;

        public static class SiteStatusInfoListBean {
            /**
             * siteUri : 027991
             * siteName : 027991
             * siteType : 0
             * siteStatus : 3
             * microphoneStatus : 0
             * loudspeakerStatus : 0
             */
            public String siteUri;
            public String siteName;
            public int siteType;
            public int siteStatus;
            public int microphoneStatus;
            public int loudspeakerStatus;
        }
    }
}
