package com.zxwl.network.bean;

import java.util.List;

/**
 * author：pc-20171125
 * data:2019/5/15 15:43
 */
public class GroupBean {

    /**
     * code : 0
     * msg : success
     * data : [{"id":1,"name":"研发部市场部群组"},{"id":3,"name":"研发部技术部群组"}]
     */

    public int code;
    public String msg;
    public List<DataBean> data;

    public static class DataBean {
        /**
         * id : 1
         * name : 研发部市场部群组
         */
        public int id;
        public String name;
    }

    @Override
    public String toString() {
        return "GroupBean{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
