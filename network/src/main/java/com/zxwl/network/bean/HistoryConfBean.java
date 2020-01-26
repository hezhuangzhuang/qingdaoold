package com.zxwl.network.bean;

import java.util.List;

/**
 * author：pc-20171125
 * data:2019/12/25 14:01
 */
public class HistoryConfBean {
    /**
     * responseCode : 1
     * message : null
     * data : [{"smcConfId":"2875","confName":"00325563","accessCode":"900002710187","duration":"30","confMediaType":null,"creatorUri":"001","creatorUriName":null,"createTime":"2019-11-27 15:31:29","sites":"002,003","sitesName":null,"ifSuccess":1},{"smcConfId":null,"confName":"会场名称","accessCode":null,"duration":"30","confMediaType":null,"creatorUri":"0000020000","creatorUriName":"李聪","createTime":null,"sites":"0000020000,0000020003,0000020008,0000020010","sitesName":"李聪,王恒,李露,吴珊","ifSuccess":null}]
     */

    public int responseCode;
    public String message;
    public List<DataBean> data;

    public static class DataBean {
        /**
         * smcConfId : 2875
         * confName : 00325563
         * accessCode : 900002710187
         * duration : 30
         * confMediaType : null
         * creatorUri : 001
         * creatorUriName : null
         * createTime : 2019-11-27 15:31:29
         * sites : 002,003
         * sitesName : null
         * ifSuccess : 1
         */
        public String smcConfId;
        public String creatorUriName;
        public String confName;
        public String accessCode;
        public String duration;
        public Object confMediaType;
        public String creatorUri;
        public String createTime;
        public String sites;
        public String sitesName;
        public int ifSuccess;
    }
}
