package com.zxwl.frame.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.zxwl.frame.bean.ChatBeanLM;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "CHAT_BEAN_LM".
*/
public class ChatBeanLMDao extends AbstractDao<ChatBeanLM, Long> {

    public static final String TABLENAME = "CHAT_BEAN_LM";

    /**
     * Properties of entity ChatBeanLM.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Type = new Property(1, int.class, "type", false, "TYPE");
        public final static Property Name = new Property(2, String.class, "name", false, "NAME");
        public final static Property Time = new Property(3, java.util.Date.class, "time", false, "TIME");
        public final static Property TextContent = new Property(4, String.class, "textContent", false, "TEXT_CONTENT");
        public final static Property Duration = new Property(5, String.class, "duration", false, "DURATION");
        public final static Property SendId = new Property(6, String.class, "sendId", false, "SEND_ID");
        public final static Property SendName = new Property(7, String.class, "sendName", false, "SEND_NAME");
        public final static Property ReceiveId = new Property(8, String.class, "receiveId", false, "RECEIVE_ID");
        public final static Property ReceiveName = new Property(9, String.class, "receiveName", false, "RECEIVE_NAME");
        public final static Property IsSend = new Property(10, boolean.class, "isSend", false, "IS_SEND");
        public final static Property IsRead = new Property(11, boolean.class, "isRead", false, "IS_READ");
        public final static Property ConversationId = new Property(12, String.class, "conversationId", false, "CONVERSATION_ID");
        public final static Property ConversationUserName = new Property(13, String.class, "conversationUserName", false, "CONVERSATION_USER_NAME");
        public final static Property IsGroup = new Property(14, boolean.class, "isGroup", false, "IS_GROUP");
    }


    public ChatBeanLMDao(DaoConfig config) {
        super(config);
    }
    
    public ChatBeanLMDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"CHAT_BEAN_LM\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"TYPE\" INTEGER NOT NULL ," + // 1: type
                "\"NAME\" TEXT," + // 2: name
                "\"TIME\" INTEGER," + // 3: time
                "\"TEXT_CONTENT\" TEXT," + // 4: textContent
                "\"DURATION\" TEXT," + // 5: duration
                "\"SEND_ID\" TEXT," + // 6: sendId
                "\"SEND_NAME\" TEXT," + // 7: sendName
                "\"RECEIVE_ID\" TEXT," + // 8: receiveId
                "\"RECEIVE_NAME\" TEXT," + // 9: receiveName
                "\"IS_SEND\" INTEGER NOT NULL ," + // 10: isSend
                "\"IS_READ\" INTEGER NOT NULL ," + // 11: isRead
                "\"CONVERSATION_ID\" TEXT UNIQUE ," + // 12: conversationId
                "\"CONVERSATION_USER_NAME\" TEXT," + // 13: conversationUserName
                "\"IS_GROUP\" INTEGER NOT NULL );"); // 14: isGroup
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"CHAT_BEAN_LM\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, ChatBeanLM entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getType());
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(3, name);
        }
 
        java.util.Date time = entity.getTime();
        if (time != null) {
            stmt.bindLong(4, time.getTime());
        }
 
        String textContent = entity.getTextContent();
        if (textContent != null) {
            stmt.bindString(5, textContent);
        }
 
        String duration = entity.getDuration();
        if (duration != null) {
            stmt.bindString(6, duration);
        }
 
        String sendId = entity.getSendId();
        if (sendId != null) {
            stmt.bindString(7, sendId);
        }
 
        String sendName = entity.getSendName();
        if (sendName != null) {
            stmt.bindString(8, sendName);
        }
 
        String receiveId = entity.getReceiveId();
        if (receiveId != null) {
            stmt.bindString(9, receiveId);
        }
 
        String receiveName = entity.getReceiveName();
        if (receiveName != null) {
            stmt.bindString(10, receiveName);
        }
        stmt.bindLong(11, entity.getIsSend() ? 1L: 0L);
        stmt.bindLong(12, entity.getIsRead() ? 1L: 0L);
 
        String conversationId = entity.getConversationId();
        if (conversationId != null) {
            stmt.bindString(13, conversationId);
        }
 
        String conversationUserName = entity.getConversationUserName();
        if (conversationUserName != null) {
            stmt.bindString(14, conversationUserName);
        }
        stmt.bindLong(15, entity.getIsGroup() ? 1L: 0L);
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, ChatBeanLM entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getType());
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(3, name);
        }
 
        java.util.Date time = entity.getTime();
        if (time != null) {
            stmt.bindLong(4, time.getTime());
        }
 
        String textContent = entity.getTextContent();
        if (textContent != null) {
            stmt.bindString(5, textContent);
        }
 
        String duration = entity.getDuration();
        if (duration != null) {
            stmt.bindString(6, duration);
        }
 
        String sendId = entity.getSendId();
        if (sendId != null) {
            stmt.bindString(7, sendId);
        }
 
        String sendName = entity.getSendName();
        if (sendName != null) {
            stmt.bindString(8, sendName);
        }
 
        String receiveId = entity.getReceiveId();
        if (receiveId != null) {
            stmt.bindString(9, receiveId);
        }
 
        String receiveName = entity.getReceiveName();
        if (receiveName != null) {
            stmt.bindString(10, receiveName);
        }
        stmt.bindLong(11, entity.getIsSend() ? 1L: 0L);
        stmt.bindLong(12, entity.getIsRead() ? 1L: 0L);
 
        String conversationId = entity.getConversationId();
        if (conversationId != null) {
            stmt.bindString(13, conversationId);
        }
 
        String conversationUserName = entity.getConversationUserName();
        if (conversationUserName != null) {
            stmt.bindString(14, conversationUserName);
        }
        stmt.bindLong(15, entity.getIsGroup() ? 1L: 0L);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public ChatBeanLM readEntity(Cursor cursor, int offset) {
        ChatBeanLM entity = new ChatBeanLM( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // type
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // name
            cursor.isNull(offset + 3) ? null : new java.util.Date(cursor.getLong(offset + 3)), // time
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // textContent
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // duration
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // sendId
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // sendName
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // receiveId
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // receiveName
            cursor.getShort(offset + 10) != 0, // isSend
            cursor.getShort(offset + 11) != 0, // isRead
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12), // conversationId
            cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13), // conversationUserName
            cursor.getShort(offset + 14) != 0 // isGroup
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, ChatBeanLM entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setType(cursor.getInt(offset + 1));
        entity.setName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setTime(cursor.isNull(offset + 3) ? null : new java.util.Date(cursor.getLong(offset + 3)));
        entity.setTextContent(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setDuration(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setSendId(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setSendName(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setReceiveId(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setReceiveName(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setIsSend(cursor.getShort(offset + 10) != 0);
        entity.setIsRead(cursor.getShort(offset + 11) != 0);
        entity.setConversationId(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setConversationUserName(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setIsGroup(cursor.getShort(offset + 14) != 0);
     }
    
    @Override
    protected final Long updateKeyAfterInsert(ChatBeanLM entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(ChatBeanLM entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(ChatBeanLM entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
