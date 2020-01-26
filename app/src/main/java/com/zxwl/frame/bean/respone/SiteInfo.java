package com.zxwl.frame.bean.respone;

import java.io.Serializable;

/**
 * author：pc-20171125
 * data:2019/1/15 15:03
 */
public class SiteInfo implements Serializable {

    /**
     * code : 0
     * msg : success
     * data : {"siteUri":"0271101","siteName":"0271101"}
     */

    public int code;
    public String msg;
    public DataBean data;

    public static class DataBean implements Serializable {
        /**
         * siteUri : 0271101
         * siteName : 0271101
         */
        public String siteUri;
        public String siteName;
    }
}
