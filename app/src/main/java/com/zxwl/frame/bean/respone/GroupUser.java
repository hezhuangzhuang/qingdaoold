package com.zxwl.frame.bean.respone;

import java.util.List;
import java.util.Objects;

/**
 * author：pc-20171125
 * data:2019/1/16 13:50
 */
public class GroupUser {
    /**
     * code : 0
     * msg : success
     * data : [{"id":"0271010","name":"颜鹏","online":0},{"id":"0271101","name":"hewei","online":0},{"id":"0271102","name":"测试2","online":0},{"id":"027991","name":"027991","online":2},{"id":"027993","name":"027993","online":2},{"id":"027117","name":"TE10","online":2},{"id":"0271109","name":"0271109","online":2},{"id":"027994","name":"027994","online":0},{"id":"027995","name":"027995","online":0},{"id":"123465","name":"123465","online":2},{"id":"0755007","name":"0755007","online":2},{"id":"0270007","name":"0270007","online":2},{"id":"0271104","name":"测试4","online":2},{"id":"0271105","name":"测试5","online":2},{"id":"0271106","name":"测试6","online":0},{"id":"0271108","name":"测试8","online":0},{"id":"0271107","name":"测试7","online":0},{"id":"027999","name":"027999","online":2},{"id":"027001","name":"CloudIPCC_027001","online":2},{"id":"027002","name":"CloudIPCC_027002","online":2},{"id":"027003","name":"CloudIPCC_027003","online":2},{"id":"027600","name":"深圳TE20","online":2},{"id":"027992","name":"027992","online":2},{"id":"0279901","name":"0279901","online":0},{"id":"0279902","name":"0279902","online":0},{"id":"0279293","name":"0279293","online":2},{"id":"02700101","name":"国电通001","online":2},{"id":"02700102","name":"国电通002","online":2},{"id":"027192","name":"武汉TE20","online":2},{"id":"0271088","name":"罗才智","online":2},{"id":"0271001","name":"郭云霞","online":2},{"id":"0271201","name":"0271201","online":2},{"id":"0271202","name":"IOS测试0271202","online":2},{"id":"0271203","name":"IOS测试0271203","online":2},{"id":"0271204","name":"IOS测试0271204","online":2},{"id":"0271205","name":"IOS测试0271205","online":2},{"id":"0271206","name":"IOS测试0271206","online":2},{"id":"0271207","name":"IOS测试0271207","online":2},{"id":"0271208","name":"0271208","online":2},{"id":"0271209","name":"IOS测试0271209","online":2},{"id":"0271003","name":"徐志文","online":2},{"id":"0271002","name":"王尚文","online":2},{"id":"0271005","name":"颜鹏1","online":2},{"id":"02711101","name":"何伟01","online":0},{"id":"02711102","name":"何伟02","online":2},{"id":"0271103","name":"测试3","online":2},{"id":"0271122","name":"周勇","online":0}]
     */
    public String code;//0为成功
    public String msg;
    public List<DataBean> data;


    public static class DataBean  {
        /**
         * id : 0271010
         * name : 颜鹏
         * online : 0
         */
        public String id;
        public String name;
        public int online;//0代表在线
        public String sip;//呼叫的号码
        public boolean isCheck = false;//是否被选中

        public DataBean(String id, String name,String sip, int online, boolean isCheck) {
            this.id = id;
            this.name = name;
            this.online = online;
            this.isCheck = isCheck;
            this.sip = sip;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DataBean dataBean = (DataBean) o;
            return online == dataBean.online &&
                    Objects.equals(id, dataBean.id) &&
                    Objects.equals(name, dataBean.name);
        }

        @Override
        public int hashCode() {

            return Objects.hash(id, name, online);
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", online=" + online +
                    ", isCheck=" + isCheck +
                    '}';
        }
    }
}
