package com.zxwl.frame.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.zxwl.frame.adapter.item.ChatItem;
import com.zxwl.frame.bean.ChatBean;
import com.zxwl.frame.bean.ChatBeanLM;
import com.zxwl.frame.bean.GroupIDBean;
import com.zxwl.frame.bean.LocalFileBean;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天记录数据库工具类
 */
public class DBUtils {

    /**
     * 初始化GreenDao,进行初始化操作
     */
    public static void initGreenDao(Context context, String dbName) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, dbName + ".db");
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    private static DaoSession daoSession;

    public static DaoSession getDaoSession() {
        return daoSession;
    }


    //处理聊天记录

    /**
     * 聊天记录插入或者替换数据
     *
     * @param list
     */
    public static void recordSave(List<ChatItem> list) {
        try {
            DaoSession daoSession = getDaoSession();
            //启动事务避免同时删除插入数据报错，后续可以把消息结构整体优化
            daoSession.runInTx(new Runnable() {
                @Override
                public void run() {
                    delete(recordQueryInChatBean(list.get(0).chatBean.sendId, list.get(0).chatBean.receiveId));
                    for (ChatItem item : list) {
                        daoSession.insertOrReplace(item.chatBean);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 聊天记录插入单个数据
     */
    public static void recordSaveIten(ChatItem item) {
        try {
            DaoSession daoSession = getDaoSession();
            //启动事务避免同时删除插入数据报错，后续可以把消息结构整体优化
            daoSession.insertOrReplace(item.chatBean);
            List<ChatItem> chatItems = recordQueryInChatItem(item.chatBean.sendId, item.chatBean.receiveId);
            Log.i("recordSaveIten", chatItems.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除数据
     */
    private static void delete(List<ChatBean> list) {
        DaoSession daoSession = getDaoSession();
        for (ChatBean bean : list) {
            daoSession.delete(bean);
        }
    }

    /**
     * 修改数据
     *
     * @param bean
     */
    public static void updata(ChatBean bean) {
        DaoSession daoSession = getDaoSession();
        daoSession.update(bean);
    }

    /**
     * 获取所有聊天记录数据
     *
     * @return
     */
    public static List<ChatItem> recordQueryInChatItem(String sendId, String receiveId) {
        try {
            List<ChatBean> list = recordQueryInChatBean(sendId, receiveId);
            List<ChatItem> result = new ArrayList<>();
            for (ChatBean bean : list) {
                result.add(new ChatItem(bean));
            }
            return result;
        } catch (Exception e) {
            return new ArrayList<ChatItem>();
        }
    }

    /**
     * 获取所有聊天记录数据
     *
     * @return
     */
    public static List<ChatBean> recordQueryInChatBean(String sendId, String receiveId) {
        DaoSession daoSession = getDaoSession();
        QueryBuilder<ChatBean> qb = daoSession.queryBuilder(ChatBean.class);
        try {
            List<ChatBean> list = qb.where(ChatBeanDao.Properties.ConversationId.eq(receiveId)).list();
            return list;
        } catch (Exception e) {
            return new ArrayList<ChatBean>();
        }
//        if (judgeGroup(receiveId)){//群组聊天记录查询
//            try {
//                List<ChatBean> list = qb.where(
//                        ChatBeanDao.Properties.ConversationId.eq(receiveId)
//                ).list();
//                return list;
//            }catch (Exception e){
//                return new ArrayList<ChatBean>();
//            }
//        } else {//个人聊天记录查询
//            try {
//                List<ChatBean> list = qb.whereOr(
//                        qb.and(ChatBeanDao.Properties.ReceiveId.eq(receiveId), ChatBeanDao.Properties.SendId.eq(sendId))
//                        , qb.and(ChatBeanDao.Properties.ReceiveId.eq(sendId), ChatBeanDao.Properties.SendId.eq(receiveId))
//                ).list();
//                return list;
//            }catch (Exception e){
//                return new ArrayList<ChatBean>();
//            }
//        }
    }


    //处理远近端文件地址对应关系

    /**
     * 存放远近端文件对应关系
     *
     * @param bean
     */
    public static void saveFilePath(LocalFileBean bean) {
        DaoSession daoSession = getDaoSession();
        daoSession.insertOrReplace(bean);
    }

    /**
     * 查询本地文件路径
     */
    public static String getFilePath(String remotePath) {
        DaoSession daoSession = getDaoSession();
        try {
            List<LocalFileBean> list = daoSession.queryRaw(LocalFileBean.class, " where REMOTE_PATH = ?", remotePath);
            return list.get(0).getLocalPath();
        } catch (Exception e) {
            return "";
        }
    }

    //处理首页消息列表

    /**
     * 保存所有聊天的最后一条数据
     *
     * @param list
     */
    public static void recordSaveLM(List<ChatItem> list) {
        DaoSession daoSession = getDaoSession();
        for (ChatItem item : list) {
            ChatBeanLM result = item.chatBean.toChatBeanLM();
            if (judgeGroup(result.conversationId)) {
                result.isGroup = true;
            } else {
                result.isGroup = false;
            }
            daoSession.insertOrReplace(result);
        }
    }

    /**
     * 保存最后一条数据
     *
     * @param chatItem
     */
    public static void saveLMItem(ChatItem item) {
        DaoSession daoSession = getDaoSession();
        ChatBeanLM result = item.chatBean.toChatBeanLM();
        if (judgeGroup(result.conversationId)) {
            result.isGroup = true;
        } else {
            result.isGroup = false;
        }
        daoSession.insertOrReplace(result);
    }

    /**
     * 获取所有聊天的最后一条数据
     */
    public static List<ChatItem> recordQueryAllLM() {
        DaoSession daoSession = getDaoSession();
        QueryBuilder<ChatBeanLM> qb = daoSession.queryBuilder(ChatBeanLM.class);
        List<ChatBeanLM> list = qb.orderDesc(ChatBeanLMDao.Properties.Time).list();
        List<ChatItem> result = new ArrayList<>();
        for (ChatBeanLM bean : list) {
            result.add(new ChatItem(bean.toChatBean()));
        }
        return result;
    }

    /**
     * 获取所有聊天的最后一条数据
     */
    public static ChatItem queryByIdLM(String groupId) {
        DaoSession daoSession = getDaoSession();
        QueryBuilder<ChatBeanLM> qb = daoSession.queryBuilder(ChatBeanLM.class);
//        List<ChatBeanLM> queryList = qb.
//                orderDesc(ChatBeanLMDao.Properties.Time)
//                .where(ChatBeanLMDao.Properties.ConversationId.eq(groupId))
//                .list();

        List<ChatBeanLM> queryList = qb.orderDesc(ChatBeanLMDao.Properties.Time)
                .where(ChatBeanLMDao.Properties.ConversationId.eq(groupId))
                .limit(1)
                .list();

        ChatItem chatItem = null;
        if (queryList.size() > 0) {
            chatItem = (new ChatItem(queryList.get(0).toChatBean()));
        }
//        List<ChatItem> result = new ArrayList<>();
//        for (ChatBeanLM bean : queryList) {
//            result.add(new ChatItem(bean.toChatBean()));
//        }
        return chatItem;
    }

    /**
     * 通过id查询聊天的最后一条数据
     *
     * @return
     */
    public static ChatItem queryByIdLMOld(String id) {
        ChatItem chatItem = null;
        List<ChatItem> chatItems = recordQueryAllLM();

        for (ChatItem item : chatItems) {
            if (item.chatBean.conversationId.equals(id)) {
                return item;
            }
        }
        return chatItem;
    }

    /**
     * 解散群组时，删除群组聊天记录
     */
    public static void deleteGroupRecord(String id) {
        daoSession.runInTx(new Runnable() {
            @Override
            public void run() {
                DaoSession daoSession = getDaoSession();
                QueryBuilder<ChatBeanLM> qb = daoSession.queryBuilder(ChatBeanLM.class);
                List<ChatBeanLM> list = qb.where(ChatBeanLMDao.Properties.ConversationId.eq(id)).list();

                //删除
                for (ChatBeanLM temp : list) {
                    daoSession.delete(temp);
                }
            }
        });

    }

    //处理群组关系

    /**
     * 存放群组id
     */
    public static void saveGroupInfo(GroupIDBean bean) {
        DaoSession daoSession = getDaoSession();
        daoSession.insertOrReplace(bean);
    }

    /**
     * 判断是否是群组
     */
    public static boolean judgeGroup(String id) {
        //不在将群组信息存放本地，通过id判断
        if (id.length() >= 9) {
            return false;
        } else {
            return true;
        }
    }
}
