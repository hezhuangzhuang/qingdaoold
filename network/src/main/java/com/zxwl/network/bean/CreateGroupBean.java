package com.zxwl.network.bean;

import java.util.List;

public class CreateGroupBean {
    /**
     * code : 0
     * msg : success
     * data : {"id":6,"name":"周勇群组","userList":[{"id":1,"name":"田虎","number":"0271101","deptId":1},{"id":2,"name":"彭宇奇","number":"0271102","deptId":1},{"id":3,"name":"李聪","number":"027503","deptId":1}]}
     */

    private int code;
    private String msg;
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * id : 6
         * name : 周勇群组
         * userList : [{"id":1,"name":"田虎","number":"0271101","deptId":1},{"id":2,"name":"彭宇奇","number":"0271102","deptId":1},{"id":3,"name":"李聪","number":"027503","deptId":1}]
         */

        private int id;
        private String name;
        private List<UserListBean> userList;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<UserListBean> getUserList() {
            return userList;
        }

        public void setUserList(List<UserListBean> userList) {
            this.userList = userList;
        }

        public static class UserListBean {
            /**
             * id : 1
             * name : 田虎
             * number : 0271101
             * deptId : 1
             */

            private int id;
            private String name;
            private String number;
            private int deptId;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getNumber() {
                return number;
            }

            public void setNumber(String number) {
                this.number = number;
            }

            public int getDeptId() {
                return deptId;
            }

            public void setDeptId(int deptId) {
                this.deptId = deptId;
            }
        }
    }
}
