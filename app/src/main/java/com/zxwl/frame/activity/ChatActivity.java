package com.zxwl.frame.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.huawei.opensdk.commonservice.common.LocContext;
import com.huawei.opensdk.sdkwrapper.login.LoginCenter;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.lqr.audio.AudioPlayManager;
import com.lqr.audio.AudioRecordManager;
import com.lqr.audio.IAudioPlayListener;
import com.lqr.audio.IAudioRecordListener;
import com.zxwl.commonlibrary.BaseActivity;
import com.zxwl.commonlibrary.utils.DialogUtils;
import com.zxwl.commonlibrary.utils.KeyBoardUtil;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.frame.App;
import com.zxwl.frame.R;
import com.zxwl.frame.adapter.ChatAdapter;
import com.zxwl.frame.adapter.item.ChatItem;
import com.zxwl.frame.adapter.item.MultipleItem;
import com.zxwl.frame.bean.ChatBean;
import com.zxwl.frame.bean.EventMsg;
import com.zxwl.frame.bean.LocalFileBean;
import com.zxwl.frame.bean.MessageBody;
import com.zxwl.frame.bean.MessageReal;
import com.zxwl.frame.db.DBUtils;
import com.zxwl.frame.inter.HuaweiCallImp;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.service.MsgIOServer;
import com.zxwl.frame.utils.Constant;
import com.zxwl.frame.utils.DateUtil;
import com.zxwl.frame.utils.FileUtils;
import com.zxwl.frame.utils.LQRPhotoSelectUtils;
import com.zxwl.frame.utils.NotificationUtils;
import com.zxwl.frame.utils.PreferenceUtil;
import com.zxwl.frame.widget.ButtomSelectDialog;
import com.zxwl.network.ApiUrls;
import com.zxwl.network.api.ConfApi;
import com.zxwl.network.api.ImApi;
import com.zxwl.network.bean.BaseData;
import com.zxwl.network.bean.BaseData_BackServer;
import com.zxwl.network.bean.BaseData_logicServer;
import com.zxwl.network.bean.ImBean;
import com.zxwl.network.bean.PeopleBean;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

import static org.xutils.common.util.FileUtil.getFileOrDirSize;

public class ChatActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout llRoot;//跟布局
    private ImageView ivBackOperate;
    private TextView tvTopTitle;
    private RecyclerView rvMsg;//消息
    private ImageView ivAudioSwitch;//语音文本切换按钮
    private EditText etContent;//输入框
    private Button btAudio;//语音
    private ImageView ivEmo;//表情
    private ImageView ivMore;//更多
    private Button btSend;//文本消息发送按钮

    private FrameLayout flEmotionView;//弹出界面的跟布局

    private LinearLayout llMore;//更多的跟布局
    private TextView tvPhotoAlbum;//相册
    private TextView tvPhotograph;//拍照
    private TextView tvFile;//拍照
    private TextView tvVideo;//视频
    private TextView tvAudio;//音频
    private TextView tvRightOperate;//解散群组
    private ImageView ivRightOperate;//查看群成员

    private ChatAdapter chatAdapter;
    private List<ChatBean> chatBeans = new ArrayList<>();

    private String url;
    private String receiveId;//收件人id
    private String receiveName;//收件人name
    private String message;//发送的数据
    private String type;//发送的数据类型

    private LQRPhotoSelectUtils lqrPhotoSelectUtils;
    private String base64;

    private Gson gson = new Gson();

    public static final String USER_ID = "USER_ID";
    public static final String USER_NAME = "USER_NAME";
    public static final String IS_GROUP = "IS_GROUP";
    private String userId;

    private boolean isGroup;//是否是群聊

    //聊天默认显示的名称
    private String disPlayName;

    private LinearLayoutManager layoutManager;

    public static void startActivity(Context context, String userId, String userName) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(USER_ID, userId);
        intent.putExtra(USER_NAME, userName);
        intent.putExtra(IS_GROUP, false);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, String userId, String userName, boolean isGroup) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(USER_ID, userId);
        intent.putExtra(USER_NAME, userName);
        intent.putExtra(IS_GROUP, isGroup);
        context.startActivity(intent);
    }

    @Override
    protected void findViews() {
        llRoot = (LinearLayout) findViewById(R.id.ll_root);
        ivBackOperate = (ImageView) findViewById(R.id.iv_back_operate);
        ivBackOperate.setVisibility(View.VISIBLE);
        tvTopTitle = (TextView) findViewById(R.id.tv_top_title);
        rvMsg = (RecyclerView) findViewById(R.id.rvMsg);
        ivAudioSwitch = (ImageView) findViewById(R.id.iv_audio_switch);
        etContent = (EditText) findViewById(R.id.etContent);
        btAudio = (Button) findViewById(R.id.bt_audio);
        ivEmo = (ImageView) findViewById(R.id.iv_emo);
        ivMore = (ImageView) findViewById(R.id.iv_more);
        btSend = (Button) findViewById(R.id.bt_send);
        flEmotionView = (FrameLayout) findViewById(R.id.flEmotionView);
        llMore = (LinearLayout) findViewById(R.id.ll_more);
        tvPhotoAlbum = (TextView) findViewById(R.id.tv_photo_album);
        tvPhotograph = (TextView) findViewById(R.id.tv_photograph);
        tvVideo = (TextView) findViewById(R.id.tv_video);
        tvAudio = (TextView) findViewById(R.id.tv_audio);
        tvRightOperate = (TextView) findViewById(R.id.tv_right_operate);
        ivRightOperate = (ImageView) findViewById(R.id.iv_right_operate);
        tvFile = (TextView) findViewById(R.id.tv_file);

        findViewById(R.id.rl_top_title).setPadding(0, 0, 0, 0);
    }

    @Override
    protected void initData() {
        FileDownloader.setup(this);
        String account = LoginCenter.getInstance().getAccount();
        userId = account;

        //初始化线程池
        singleThreadExecutor = Executors.newSingleThreadExecutor();

        disPlayName = PreferenceUtil.getString(this, Constant.DISPLAY_NAME, "");

        receiveId = getIntent().getStringExtra(USER_ID);
        receiveName = getIntent().getStringExtra(USER_NAME);
        isGroup = getIntent().getBooleanExtra(IS_GROUP, false);
        tvTopTitle.setText(receiveName);
        tvTopTitle.setVisibility(View.VISIBLE);

        //取消notif
        NotificationUtils.cancel(Integer.valueOf(receiveId));

        initAudioRecordManager();

        initRecycler();

        EventBus.getDefault().register(this);

        //照片选择器
        initLqrPhotoSelectUtils();

        ivEmo.setVisibility(View.GONE);

        List<ChatItem> chatMessage = getChatMessage(receiveId);
        chatAdapter.replaceData(chatMessage);

        if (chatAdapter.getItemCount() > 6) {
            layoutManager.setStackFromEnd(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isGroup) {
            isGroupCreater();
            ivRightOperate.setVisibility(View.VISIBLE);
            ivRightOperate.setImageResource(R.mipmap.ic_group_details);
        }
    }

    private void isGroupCreater() {
        HttpUtils.getInstance(this)
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ImApi.class)
                .isGroupCreater(LoginCenter.getInstance().getAccount(), receiveId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData_logicServer>() {
                    @Override
                    public void onSuccess(BaseData_logicServer bean) {
                        if (bean.getResponseCode() == 1) {
                            //群组是否存在，1存在，0不存在
                            if (bean.getData().toString().contains("ifGroupExist=1.0")) {
                                findViewById(R.id.ll_bottom).setVisibility(View.GONE);
                                ivRightOperate.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {

                    }
                });
    }

    private String getPath() {
        String path = Environment.getExternalStorageDirectory() + "/Luban/image/";
        File file = new File(path);
        if (file.mkdirs()) {
            return path;
        }
        return path;
    }

    /**
     * 主线程中处理返回事件
     *
     * @param messageEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void eventMain(EventMsg messageEvent) {
        switch (messageEvent.getMsg()) {
            case EventMsg.RECEIVE_MESSAGE:
                //获取消息
                MessageBody messageBody = messageEvent.getMessageBody();

                //获取消息内容
                MessageReal message = messageBody.getReal();

                if (DBUtils.judgeGroup(messageBody.getReceiveId())) {
                    if (receiveId.equals(messageBody.getReceiveId())) {
                        handlerMsg(messageBody, message);
                    } else {
//                        updateHome(messageBody, message);
                    }
                } else {
                    if (receiveId.equals(messageBody.getSendId())) {
                        handlerMsg(messageBody, message);
                    } else {
//                      updateHome(messageBody, message);
                    }
                }
                break;

            //刷新单个消息
            case EventMsg.RECEIVE_SINGLE_MESSAGE:
                ChatBean sendBean = (ChatBean) messageEvent.getMessageData();
                addMessageAdapter(sendBean,false);
                break;
        }
    }

    private ExecutorService singleThreadExecutor;

    private void save() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                //保存消息
//                //TODO：屏蔽了保存消息的方法
//                //saveChatMessage(receiveId);
//
//                //保存最后的消息
//                saveLastMessage();
//
//                EventMsg eventMsg = new EventMsg();
//                eventMsg.setMsg(EventMsg.UPDATE_HOME);
//                EventBus.getDefault().post(eventMsg);
//            }
//        }).start();

        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                //保存消息
                //TODO：屏蔽了保存消息的方法
                //saveChatMessage(receiveId);

                //保存最后的消息
                saveLastMessage();

                EventMsg eventMsg = new EventMsg();
                eventMsg.setMsg(EventMsg.UPDATE_HOME);
                EventBus.getDefault().post(eventMsg);
            }
        });
    }

    public void handlerMsg(MessageBody messageBody, MessageReal message) {
        //消息类型
        Integer type = message.getType();
        ChatBean formMsg = null;
        switch (type) {
            //文本
            case MessageReal.TYPE_STR:
                formMsg = new ChatBean(MultipleItem.FORM_TEXT, messageBody.getSendName(), new Date(), message.getMessage());
                formMsg.sendId = messageBody.getReceiveId();
                formMsg.sendName = messageBody.getReceiveName();
                formMsg.receiveId = messageBody.getSendId();
                formMsg.receiveName = messageBody.getSendName();
                formMsg.isSend = false;
                formMsg.conversationId = receiveId;//会话id
                formMsg.conversationUserName = messageBody.getReceiveName();//会话id

                formMsg.isGroup = MessageBody.TYPE_COMMON == messageBody.getType();

                addMessageAdapter(formMsg, false);
                break;

            //图片
            case MessageReal.TYPE_IMG:
                formMsg = new ChatBean(MultipleItem.FORM_IMG, messageBody.getSendName(), new Date(), message.getImgUrl());
                formMsg.sendId = messageBody.getReceiveId();
                formMsg.sendName = messageBody.getReceiveName();
                formMsg.receiveId = messageBody.getSendId();
                formMsg.receiveName = messageBody.getSendName();
                formMsg.isSend = false;
                formMsg.conversationId = receiveId;//会话id
                formMsg.conversationUserName = messageBody.getReceiveName();//会话id

                formMsg.isGroup = MessageBody.TYPE_COMMON == messageBody.getType();

                addMessageAdapter(formMsg, false);
                break;

            //语音
            case MessageReal.TYPE_APPENDIX:
                downVoice(messageBody);
                break;

            //通知
            case MessageReal.TYPE_NOTIFY:
                //群组重命名
                if (message.getMessage().startsWith("群组重命名_")) {
                    String newName = message.getMessage().substring(6);
                    receiveName = newName;
                    tvTopTitle.setText(newName);
                }
                break;

            //视频呼叫
            case MessageReal.TYPE_VIDEO_CALL:
                formMsg = new ChatBean(MultipleItem.FORM_VIDEO_CALL, messageBody.getSendName(), new Date(), message.getMessage());
                formMsg.sendId = messageBody.getReceiveId();
                formMsg.sendName = messageBody.getReceiveName();
                formMsg.receiveId = messageBody.getSendId();
                formMsg.receiveName = messageBody.getSendName();
                formMsg.isSend = false;
                formMsg.conversationId = receiveId;//会话id
                formMsg.conversationUserName = messageBody.getReceiveName();//会话id

                formMsg.isGroup = MessageBody.TYPE_COMMON == messageBody.getType();

                addMessageAdapter(formMsg, false);
                break;

            //视频呼叫
            case MessageReal.TYPE_VOICE_CALL:
                formMsg = new ChatBean(MultipleItem.FORM_VOICE_CALL, messageBody.getSendName(), new Date(), message.getMessage());
                formMsg.sendId = messageBody.getReceiveId();
                formMsg.sendName = messageBody.getReceiveName();
                formMsg.receiveId = messageBody.getSendId();
                formMsg.receiveName = messageBody.getSendName();
                formMsg.isSend = false;
                formMsg.conversationId = receiveId;//会话id
                formMsg.conversationUserName = messageBody.getReceiveName();//会话id

                formMsg.isGroup = MessageBody.TYPE_COMMON == messageBody.getType();

                addMessageAdapter(formMsg, false);
                break;
            default:
                break;
        }
    }

    /**
     * 子线程中处理返回事件
     *
     * @param messageEvent
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND, sticky = true)
    public void eventBackGroup(EventMsg messageEvent) {

    }

    /**
     * 发送文本
     *
     * @param textMsg
     */
    private boolean sendTextMsg(String textMsg) {
        String account = LoginCenter.getInstance().getAccount();

        MessageReal messageReal = new MessageReal(textMsg, MessageReal.TYPE_STR, "");
        MessageBody msg = new MessageBody(
                account,
                //Constant.CurrDisPlayName,
                disPlayName,
                receiveId,
                receiveName,
                messageReal,
                isGroup ? MessageBody.TYPE_COMMON : MessageBody.TYPE_PERSONAL
        );
        return MsgIOServer.sendMsg(msg);
    }

    /**
     * 发送图片
     *
     * @param imgUrl
     */
    private boolean sendImg(String imgUrl) {
        String account = LoginCenter.getInstance().getAccount();
        String i = account;

        MessageReal messageReal = new MessageReal("", MessageReal.TYPE_IMG, imgUrl);
        MessageBody msg = new MessageBody(
                i,
                //Constant.CurrDisPlayName,
                disPlayName,
                receiveId,
                receiveName,
                messageReal,
                isGroup ? MessageBody.TYPE_COMMON : MessageBody.TYPE_PERSONAL
        );
        return MsgIOServer.sendMsg(msg);
    }

    /**
     * 发送音频
     *
     * @param imgUrl
     */
    private boolean sendVoice(String imgUrl) {
        String account = LoginCenter.getInstance().getAccount();
        String i = account;

        MessageReal messageReal = new MessageReal("", MessageReal.TYPE_APPENDIX, imgUrl);
        MessageBody msg = new MessageBody(
                i,
                //Constant.CurrDisPlayName,
                disPlayName,
                receiveId,
                receiveName,
                messageReal,
                isGroup ? MessageBody.TYPE_COMMON : MessageBody.TYPE_PERSONAL
        );

        return MsgIOServer.sendMsg(msg);
    }

    @Override
    protected void onDestroy() {
        //取消notif
        NotificationUtils.cancel(Integer.valueOf(receiveId));

        EventMsg eventMsg = new EventMsg();
        eventMsg.setMessageData(receiveName);
        eventMsg.setMsg(EventMsg.UPDATE_HOME_READ);
        EventBus.getDefault().post(eventMsg);

        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    private void initRecycler() {
        Log.d("zxwl_swf_Thread", "initRecycler" + Thread.currentThread().getName());
        chatAdapter = new ChatAdapter(0, new ArrayList<>());
        chatAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                ChatItem chatItem = (ChatItem) adapter.getData().get(position);

                switch (view.getId()) {
                    //语音
                    case R.id.fl_voice:
                        switch (chatItem.getItemType()) {
                            case MultipleItem.FORM_IMG:
                            case MultipleItem.SEND_IMG:
                                ChatItem chatItem1 = chatAdapter.getData().get(position);
                                PhotoViewActivity.startActivity(ChatActivity.this, chatItem1.chatBean.textContent);
                                break;

                            case MultipleItem.FORM_VOICE:
                                String path = chatAdapter.getData().get(position).chatBean.textContent;
                                File item = new File(path);
                                if (!TextUtils.isEmpty(path)) {
                                    if (path.startsWith("http") || path.startsWith("https")) {
//                                        String localPath = PreferenceUtil.getStringWithUserName(ChatActivity.this,path,"");
                                        String localPath = DBUtils.getFilePath(path);
                                        if (!TextUtils.isEmpty(localPath)) {
                                            item = new File(localPath);
                                        }
                                    }
                                }
                                if (item.getPath().endsWith(".voice") || item.getPath().endsWith(".m4a")) {
                                    //语音附件
                                    if (!item.exists()) {
                                        downVoice(chatItem, position);
                                    } else {
                                        playAudio(view, position);
                                    }
                                } else {
                                    //其他附件
                                    if (item.exists()) {
                                        openFile(item.getPath());
                                    } else {
                                        //文件不存在，需要重新下载
                                        ToastUtil.showShortToast(ChatActivity.this, "文件正在下载...");
                                        downVoice(chatItem, position);
                                    }
                                }
                                break;

                            case MultipleItem.SEND_VOICE:
                                if (chatAdapter.getData().get(position).chatBean.textContent.endsWith(".voice") || chatAdapter.getData().get(position).chatBean.textContent.endsWith(".m4a")) {
                                    playAudio(view, position);
                                } else {
                                    openFile(chatAdapter.getData().get(position).chatBean.textContent);
                                }
                                break;
                        }
                        break;

                    //图片
                    case R.id.iv_voice:
                        switch (chatItem.getItemType()) {
                            case MultipleItem.FORM_IMG:
                            case MultipleItem.SEND_IMG:
                                ChatItem chatItem1 = chatAdapter.getData().get(position);
                                PhotoViewActivity.startActivity(ChatActivity.this, chatItem1.chatBean.textContent);
                                break;

                            case MultipleItem.FORM_VOICE:
                                playAudio(view, position);
                                break;

                            case MultipleItem.SEND_VOICE:
                                playAudio(view, position);
                                break;
                        }
                        break;
                }


            }
        });

        layoutManager = new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.VERTICAL, false);

        rvMsg.setLayoutManager(layoutManager);
        rvMsg.setAdapter(chatAdapter);
    }

    private void openFile(String path) {
        Intent intent = new Intent();
        File file = new File(path);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//设置标记
        intent.setAction(Intent.ACTION_VIEW);//动作，查看
        intent.setDataAndType(Uri.fromFile(file), FileUtils.getMIMEType(file));//设置类型
        startActivity(intent);
    }

    /**
     * 播放语音
     *
     * @param view
     * @param position
     */
    private void playAudio(View view, int position) {
        AudioPlayManager.getInstance().stopPlay();

        File item = new File(chatAdapter.getData().get(position).chatBean.textContent);

//        final ImageView ivAudio = view.findViewById(R.id.iv_voice);
        Uri audioUri = Uri.fromFile(item);
        Log.e("LQR", audioUri.toString());
        AudioPlayManager.getInstance().startPlay(ChatActivity.this, audioUri, new IAudioPlayListener() {
            @Override
            public void onStart(Uri var1) {
//                if (ivAudio != null && ivAudio.getBackground() instanceof AnimationDrawable) {
//                    AnimationDrawable animation = (AnimationDrawable) ivAudio.getBackground();
//                    animation.start();
//                }
            }

            @Override
            public void onStop(Uri var1) {
//                if (ivAudio != null && ivAudio.getBackground() instanceof AnimationDrawable) {
//                    AnimationDrawable animation = (AnimationDrawable) ivAudio.getBackground();
//                    animation.stop();
//                    animation.selectDrawable(0);
//                }
            }

            @Override
            public void onComplete(Uri var1) {
//                if (ivAudio != null && ivAudio.getBackground() instanceof AnimationDrawable) {
//                    AnimationDrawable animation = (AnimationDrawable) ivAudio.getBackground();
//                    animation.stop();
//                    animation.selectDrawable(0);
//                }
            }
        });
    }

    @Override
    protected void setListener() {
        ivBackOperate.setOnClickListener(this);
        ivAudioSwitch.setOnClickListener(this);//语音文本切换按钮
        ivEmo.setOnClickListener(this);//表情
        ivMore.setOnClickListener(this);//更多
        tvPhotoAlbum.setOnClickListener(this);//相册
        tvPhotograph.setOnClickListener(this);//拍照
        tvVideo.setOnClickListener(this);//视频
        tvAudio.setOnClickListener(this);//音频
        btSend.setOnClickListener(this);
        tvFile.setOnClickListener(this);

        btAudio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btAudio.setText("松开结束");
                        Log.i(Constant.TAG, "ACTION_DOWN");
                        AudioRecordManager.getInstance(ChatActivity.this).startRecord();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        Log.i(Constant.TAG, "ACTION_MOVE");
                        if (isCancelled(v, event)) {
                            AudioRecordManager.getInstance(ChatActivity.this).willCancelRecord();
                        } else {
                            AudioRecordManager.getInstance(ChatActivity.this).continueRecord();
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        btAudio.setText("按住说话");
                    case MotionEvent.ACTION_CANCEL:
                        Log.i(Constant.TAG, "ACTION_UP");
                        AudioRecordManager.getInstance(ChatActivity.this).stopRecord();
                        AudioRecordManager.getInstance(ChatActivity.this).destroyRecord();
                        break;
                }
                return false;
            }
        });

        etContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    flEmotionView.setVisibility(View.GONE);
                    llMore.setVisibility(View.GONE);
                }
            }
        });

        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    btSend.setVisibility(View.VISIBLE);
                    ivMore.setVisibility(View.GONE);
                } else {
                    btSend.setVisibility(View.GONE);
                    ivMore.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        rvMsg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                flEmotionView.setVisibility(View.GONE);
                llMore.setVisibility(View.GONE);
                KeyBoardUtil.closeKeybord(etContent, ChatActivity.this);
                return false;
            }
        });

        tvRightOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpUtils.getInstance(ChatActivity.this)
                        .getRetofitClinet()
                        .setBaseUrl(Urls.logicServerURL)
                        .builder(ConfApi.class)
                        .delGroup(ApiUrls.DEL_GROUP, receiveId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new RxSubscriber<BaseData_BackServer>() {

                            @Override
                            protected void onError(ResponeThrowable responeThrowable) {
                                Toast.makeText(ChatActivity.this, "解散失败，请稍后重试", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess(BaseData_BackServer baseData_backServer) {
                                if (baseData_backServer.getCode() == 0) {
                                    Toast.makeText(ChatActivity.this, "解散群组成功", Toast.LENGTH_SHORT).show();
                                    DBUtils.deleteGroupRecord(receiveId);
                                    updateGroupList();
                                    finish();
                                }
                            }
                        });
            }
        });

        ivRightOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupDetailsActivity.startActivity(LocContext.getContext(), Integer.parseInt(receiveId), receiveName);
            }
        });
    }

    public void updateGroupList() {
        try {
            EventMsg eventMsg = new EventMsg();
            eventMsg.setMsg(EventMsg.UPDATE_GROUP);
            EventBus.getDefault().post(eventMsg);
        } catch (Exception e) {

        }
    }

    private boolean isCancelled(View view, MotionEvent event) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        if (event.getRawX() < location[0] || event.getRawX() > location[0] + view.getWidth()
                || event.getRawY() < location[1] - 40) {
            return true;
        }

        return false;
    }

    private File audioDir;

    /**
     * 初始化音频管理
     */
    private void initAudioRecordManager() {
        //设置最长录音时间
        AudioRecordManager.getInstance(this).setMaxVoiceDuration(Constant.DEFAULT_MAX_AUDIO_RECORD_TIME_SECOND);
        audioDir = new File(Constant.AUDIO_SAVE_DIR);
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }
        AudioRecordManager.getInstance(this).setAudioSavePath(audioDir.getAbsolutePath());

        //设置语言监听
        AudioRecordManager.getInstance(this).setAudioRecordListener(new IAudioRecordListener() {
            private TextView mTimerTV;
            private TextView mStateTV;
            private ImageView mStateIV;
            private PopupWindow mRecordWindow;

            @Override
            public void initTipView() {
                Log.i(getString(R.string.Record_TAG), "initTipView");

                View view = View.inflate(ChatActivity.this, R.layout.popup_audio_wi_vo, null);
                mStateIV = (ImageView) view.findViewById(R.id.rc_audio_state_image);
                mStateTV = (TextView) view.findViewById(R.id.rc_audio_state_text);
                mTimerTV = (TextView) view.findViewById(R.id.rc_audio_timer);
                mRecordWindow = new PopupWindow(view, -1, -1);
                mRecordWindow.showAtLocation(llRoot, 17, 0, 0);
                mRecordWindow.setFocusable(true);
                mRecordWindow.setOutsideTouchable(false);
                mRecordWindow.setTouchable(false);
            }

            @Override
            public void setTimeoutTipView(int counter) {
                Log.i(getString(R.string.Record_TAG), "setTimeoutTipView");

                if (null != this.mRecordWindow) {
                    this.mStateIV.setVisibility(View.GONE);
                    this.mStateTV.setVisibility(View.VISIBLE);
                    this.mStateTV.setText(R.string.voice_rec);
                    this.mStateTV.setBackgroundResource(R.drawable.bg_voice_popup);
                    this.mTimerTV.setText(String.format("%s", new Object[]{Integer.valueOf(counter)}));
                    this.mTimerTV.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void setRecordingTipView() {
                Log.i(getString(R.string.Record_TAG), "setRecordingTipView");
                if (this.mRecordWindow != null) {
                    this.mStateIV.setVisibility(View.VISIBLE);
                    this.mStateIV.setImageResource(R.mipmap.ic_volume_1);
                    this.mStateTV.setVisibility(View.VISIBLE);
                    this.mStateTV.setText(R.string.voice_rec);
                    this.mStateTV.setBackgroundResource(R.drawable.bg_voice_popup);
                    this.mTimerTV.setVisibility(View.GONE);
                }
            }

            @Override
            public void setAudioShortTipView() {
                Log.i(getString(R.string.Record_TAG), "setAudioShortTipView");
                if (this.mRecordWindow != null) {
                    mStateIV.setImageResource(R.mipmap.ic_volume_wraning);
                    mStateTV.setText(R.string.voice_short);
                }
            }

            @Override
            public void setCancelTipView() {
                Log.i(getString(R.string.Record_TAG), "setCancelTipView");
                if (this.mRecordWindow != null) {
                    this.mTimerTV.setVisibility(View.GONE);
                    this.mStateIV.setVisibility(View.VISIBLE);
                    this.mStateIV.setImageResource(R.mipmap.ic_volume_cancel);
                    this.mStateTV.setVisibility(View.VISIBLE);
                    this.mStateTV.setText(R.string.voice_cancel);
                    this.mStateTV.setBackgroundResource(R.drawable.shape_corner_voice);

                    btAudio.setText("松开取消");
                }
            }

            @Override
            public void destroyTipView() {
                try {
                    Log.i(getString(R.string.Record_TAG), "destroyTipView");
                    if (this.mRecordWindow != null) {
                        this.mRecordWindow.dismiss();
                        this.mRecordWindow = null;
                        this.mStateIV = null;
                        this.mStateTV = null;
                        this.mTimerTV = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //开始录音
            @Override
            public void onStartRecord() {
                Log.i(getString(R.string.Record_TAG), "onStartRecord");
            }

            //录音完成
            @Override
            public void onFinish(Uri audioPath, int duration) {
                Log.i(getString(R.string.Record_TAG), "finish");
                //发送文件
                File newFile = new File(audioPath.getPath());

                if (newFile.exists()) {
                    //上传语音
                    uploadVoice(newFile, duration);
                } else {
                    Toast.makeText(ChatActivity.this, "发送语音失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAudioDBChanged(int db) {
                Log.i("Record", "onAudioDBChanged");

                switch (db / 5) {
                    case 0:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_1);
                        break;
                    case 1:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_2);
                        break;
                    case 2:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_3);
                        break;
                    case 3:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_4);
                        break;
                    case 4:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_5);
                        break;
                    case 5:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_6);
                        break;
                    case 6:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_7);
                        break;
                    default:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_8);
                }
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back_operate:
                finish();
                break;

            case R.id.iv_audio_switch:
                //输入框显示
                if (View.VISIBLE == etContent.getVisibility()) {

                    btAudio.setText("按住说话");

                    //显示语音按钮
                    btAudio.setVisibility(View.VISIBLE);

                    //隐藏输入框
                    etContent.setVisibility(View.GONE);

                    //隐藏更多
                    llMore.setVisibility(View.GONE);
                    flEmotionView.setVisibility(View.GONE);

                    KeyBoardUtil.closeKeybord(etContent, ChatActivity.this);
                } else {
                    etContent.setVisibility(View.VISIBLE);
                    etContent.requestFocus();

                    KeyBoardUtil.openKeybord(etContent, ChatActivity.this);

                    btAudio.setVisibility(View.GONE);
                }
                break;

            case R.id.bt_send:
                String content = etContent.getText().toString();
                etContent.setText("");

                //发送数据
                if (!sendTextMsg(content)) {
                    return;
                }

//                ChatBean sendMsg = new ChatBean(MultipleItem.SEND_TEXT, Constant.CurrDisPlayName, new Date(), content);
                ChatBean sendMsg = new ChatBean(MultipleItem.SEND_TEXT, disPlayName, new Date(), content);
                sendMsg.sendId = userId;
//                sendMsg.sendName = Constant.CurrDisPlayName;
                sendMsg.sendName = disPlayName;
                sendMsg.receiveId = receiveId;
                sendMsg.receiveName = receiveName;
                sendMsg.isSend = true;
                sendMsg.conversationId = receiveId;//会话id
                sendMsg.conversationUserName = receiveName;//会话id

                sendMsg.isGroup = false;

                addMessageAdapter(sendMsg, true);
                break;

            case R.id.iv_emo:
                break;

            case R.id.iv_more:
                //更多显示
                if (llMore.isShown()) {
                    //隐藏更多
                    llMore.setVisibility(View.GONE);
                    flEmotionView.setVisibility(View.GONE);
                    //输入框获取焦点
                    etContent.requestFocus();
                    //弹出键盘
                    KeyBoardUtil.openKeybord(etContent, ChatActivity.this);
                } else {
                    //弹出键盘
                    KeyBoardUtil.closeKeybord(etContent, ChatActivity.this);
//                    KeyBoardUtil.hideKeyboard(ChatActivity.this);
                    //隐藏语音按钮
                    btAudio.setVisibility(View.GONE);

                    //显示输入框
                    etContent.setVisibility(View.VISIBLE);
                    //输入框失去焦点
                    etContent.clearFocus();

                    //显示更多布局
                    flEmotionView.setVisibility(View.VISIBLE);
                    llMore.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.tv_photo_album:
                lqrPhotoSelectUtils.selectPhoto();
                break;

            case R.id.tv_photograph:
                //拍照
                takePhotoPermission();
                break;
            case R.id.tv_file:
                //附件
                openFileSelect();
                break;

            case R.id.tv_video:
                if (!isGroup) {
                    int result = HuaweiCallImp.getInstance().callSite(receiveId, true);
                    //判断是否登陆
                    if (result == HuaweiCallImp.NO_LOGIN_RESULT) {

                    }
                } else {
                    showBottomSelectDialog(false);
                }
                break;

            case R.id.tv_audio:
                if (!isGroup) {
                    int result = HuaweiCallImp.getInstance().callSite(receiveId, false);
                    //判断是否登陆
                    if (result == HuaweiCallImp.NO_LOGIN_RESULT) {

                    }
                } else {
                    showBottomSelectDialog(true);
                }
                break;
        }
    }

    private void openFileSelect() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 10);
    }


    /**
     * 添加消息到
     *
     * @param chatBean
     * @param isSaveMsg
     */
    private void addMessageAdapter(ChatBean chatBean, boolean isSaveMsg) {
        ChatItem chatItem = new ChatItem(chatBean);
        chatAdapter.getData().add(chatItem);
        chatAdapter.notifyDataSetChanged();
        rvMsg.smoothScrollToPosition(chatAdapter.getData().size() - 1);

        if (isSaveMsg) {
            //保存消息
            DBUtils.recordSaveIten(chatItem);
        }

        save();
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == 10) {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        String path = FileUtils.getPath(this, uri);
                        if (path != null) {
                            File file = new File(path);
                            if (getFileOrDirSize(file) / 1024 / 1024 >= 10) {
                                ToastUtil.showShortToast(ChatActivity.this, "仅支持传输10M以下文件");
                                return;
                            }
                            if (file.exists()) {
                                uploadVoice(file, 0);
                            }
                        }
                    }
                }
                return;
            }
        } catch (Exception e) {
            ToastUtil.showShortToast(ChatActivity.this, "暂不支持此文件传输");
        }

        super.onActivityResult(requestCode, resultCode, data);
        // 2、在Activity中的onActivityResult()方法里与LQRPhotoSelectUtils关联
        lqrPhotoSelectUtils.attachToActivityForResult(requestCode, resultCode, data);
    }

    private void initLqrPhotoSelectUtils() {
        // 1、创建LQRPhotoSelectUtils（一个Activity对应一个LQRPhotoSelectUtils）
        lqrPhotoSelectUtils = new LQRPhotoSelectUtils(this, new LQRPhotoSelectUtils.PhotoSelectListener() {
            @Override
            public void onFinish(File outputFile, Uri outputUri) {
                Luban.with(ChatActivity.this)
                        .load(outputFile)
                        .ignoreBy(100)
                        .setTargetDir(getPath())
                        .filter(new CompressionPredicate() {
                            @Override
                            public boolean apply(String path) {
                                return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                            }
                        })
                        .setCompressListener(new OnCompressListener() {
                            @Override
                            public void onStart() {
                                // TODO 压缩开始前调用，可以在方法内启动 loading UI
                                //                                Toast.makeText(CheckInActivity.this, "开始压缩", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess(File file) {
                                // TODO 压缩成功后调用，返回压缩后的图片文件
                                //                                Toast.makeText(CheckInActivity.this, "压缩成功", Toast.LENGTH_SHORT).show();
                                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                                if (bitmap == null) {
                                    ToastUtil.showLongToast(LocContext.getContext(), "图片选择失败，可能是不支持此类图片");
                                    return;
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] bytes = baos.toByteArray();
                                base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);

                                //上传图片
                                uploadPhoto(file);

                                // 4、当拍照或从图库选取图片成功后回调
                                //                                Glide.with(ChatActivity.this)
                                //                                        .load(file)
                                //                                        .apply(RequestOptions.bitmapTransform(new RotateTransformation()))
                                //                                        .into(ivPreview);
                            }

                            @Override
                            public void onError(Throwable e) {
                                // TODO 当压缩过程出现问题时调用
                                //                                Toast.makeText(CheckInActivity.this, "压缩失败", Toast.LENGTH_SHORT).show();
                            }
                        }).launch();
            }
        }, false);//true裁剪，false不裁剪
    }

    /**
     * 上传图片
     *
     * @param file
     */
    private void uploadPhoto(File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("uploadFile", file.getName(), requestFile);

        HttpUtils.getInstance(ChatActivity.this)
                .getRetofitClinet()
                .setBaseUrl(com.zxwl.frame.net.Urls.GUOHANG_BASE_URL)
                .builder(ImApi.class)
                .upload(filePart)
                //获得sessionId的处理
                .flatMap(new Func1<ImBean, Observable<ChatBean>>() {
                    @Override
                    public Observable<ChatBean> call(ImBean imBean) {
                        if (ImBean.SUCCESS.equals(imBean.msg)) {
//                            ChatBean sendBean = new ChatBean(MultipleItem.SEND_IMG, Constant.CurrDisPlayName, new Date(), imBean.data.filePath);
                            ChatBean sendBean = new ChatBean(MultipleItem.SEND_IMG, disPlayName, new Date(), imBean.data.filePath);
                            sendBean.sendId = userId;
//                            sendBean.sendName = Constant.CurrDisPlayName;
                            sendBean.sendName = disPlayName;
                            sendBean.receiveId = receiveId;
                            sendBean.receiveName = receiveName;
                            sendBean.isSend = true;
                            sendBean.conversationId = receiveId;//会话id
                            sendBean.conversationUserName = receiveName;//会话id

                            sendBean.isGroup = false;

                            return Observable.just(sendBean);
                        } else {
//                            ChatBean sendBean = new ChatBean(MultipleItem.SEND_IMG, Constant.CurrDisPlayName, new Date(), file.getAbsolutePath());
                            ChatBean sendBean = new ChatBean(MultipleItem.SEND_IMG, disPlayName, new Date(), file.getAbsolutePath());
                            sendBean.sendId = userId;
//                            sendBean.sendName = Constant.CurrDisPlayName;
                            sendBean.sendName = disPlayName;
                            sendBean.receiveId = receiveId;
                            sendBean.receiveName = receiveName;
                            sendBean.isSend = true;
                            sendBean.conversationId = receiveId;//会话id
                            sendBean.conversationUserName = receiveName;//会话id

                            sendBean.isGroup = false;

                            ResponeThrowable throwable = new ResponeThrowable(gson.toJson(sendBean).toString(), gson.toJson(sendBean).toString());
                            return Observable.error(throwable);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<ChatBean>() {
                    @Override
                    public void onSuccess(ChatBean chatBean) {
                        if (!sendImg(chatBean.textContent)) {
                            return;
                        }
                        addMessageAdapter(chatBean, true);
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
//                        ChatBean sendBean = new ChatBean(MultipleItem.SEND_IMG, LoginCenter.getInstance().getAccountName(), new Date(), file.getAbsolutePath());
//                        sendBean.sendId = userId;
//                        sendBean.sendName = LoginCenter.getInstance().getAccountName();
//                        sendBean.receiveId = receiveId;
//                        sendBean.receiveName = receiveName;
//                        sendBean.isSend = true;
//                        sendBean.conversationId = receiveId;//会话id
//                        sendBean.conversationUserName = receiveName;//会话id
//
//                        sendBean.isGroup = false;
//
//                        addMessageAdapter(sendBean);
                        Toast.makeText(ChatActivity.this, "图片发送失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 上传语音文件
     *
     * @param file
     */
    private void uploadVoice(File file, int duration) {
        if (file != null && !file.getName().endsWith("voice")) {
            DialogUtils.showProgressDialog(ChatActivity.this, "文件上传中...");
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("uploadFile", file.getName(), requestFile);

        HttpUtils.getInstance(ChatActivity.this)
                .getRetofitClinet()
                .setBaseUrl(com.zxwl.frame.net.Urls.GUOHANG_BASE_URL)
                .builder(ImApi.class)
                .upload(filePart)
                //获得sessionId的处理
                .flatMap(new Func1<ImBean, Observable<ChatBean>>() {
                    @Override
                    public Observable<ChatBean> call(ImBean imBean) {
                        if (ImBean.SUCCESS.equals(imBean.msg)) {
//                            ChatBean sendBean = new ChatBean(MultipleItem.SEND_VOICE, Constant.CurrDisPlayName, new Date(), imBean.data.filePath, duration + "");
                            ChatBean sendBean = new ChatBean(MultipleItem.SEND_VOICE, disPlayName, new Date(), imBean.data.filePath, duration + "");
                            sendBean.sendId = userId;
//                            sendBean.sendName = Constant.CurrDisPlayName;
                            sendBean.sendName = disPlayName;
                            sendBean.receiveId = receiveId;
                            sendBean.receiveName = receiveName;
                            sendBean.isSend = true;
                            sendBean.conversationId = receiveId;//会话id
                            sendBean.conversationUserName = receiveName;//会话id

                            sendBean.isGroup = false;

                            return Observable.just(sendBean);
                        } else {
//                            ChatBean sendBean = new ChatBean(MultipleItem.SEND_VOICE, Constant.CurrDisPlayName, new Date(), file.getAbsolutePath(), duration + "");
                            ChatBean sendBean = new ChatBean(MultipleItem.SEND_VOICE, disPlayName, new Date(), file.getAbsolutePath(), duration + "");
                            sendBean.sendId = userId;
//                            sendBean.sendName = Constant.CurrDisPlayName;
                            sendBean.sendName = disPlayName;
                            sendBean.receiveId = receiveId;
                            sendBean.receiveName = receiveName;
                            sendBean.isSend = true;
                            sendBean.conversationId = receiveId;//会话id
                            sendBean.conversationUserName = receiveName;//会话id

                            sendBean.isGroup = false;

                            ResponeThrowable throwable = new ResponeThrowable(gson.toJson(sendBean).toString(), gson.toJson(sendBean).toString());
                            return Observable.error(throwable);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<ChatBean>() {
                    @Override
                    public void onSuccess(ChatBean chatBean) {
                        DialogUtils.dismissProgressDialog(ChatActivity.this);
                        if (!sendVoice(chatBean.textContent)) {
                            return;
                        }
                        chatBean.textContent = file.getAbsolutePath();
                        addMessageAdapter(chatBean, true);
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        ToastUtil.showShortToast(ChatActivity.this, "文件传输失败");
//                        ChatBean sendBean = new ChatBean(MultipleItem.SEND_IMG,  Constant.CurrDisPlayName, new Date(), file.getAbsolutePath(), duration + "");
//                        sendBean.sendId = userId;
//                        sendBean.sendName =  Constant.CurrDisPlayName;
//                        sendBean.receiveId = receiveId;
//                        sendBean.receiveName = receiveName;
//                        sendBean.isSend = true;
//                        sendBean.conversationId = receiveId;//会话id
//                        sendBean.conversationUserName = receiveName;//会话id
//
//                        sendBean.isGroup = false;
//
//                        addMessageAdapter(sendBean);

                        DialogUtils.dismissProgressDialog(ChatActivity.this);
                    }
                });
    }

    /**
     * 下载语音文件
     *
     * @param messageBody
     */
    private void downVoice(MessageBody messageBody) {
        //获取消息
        //获取消息内容
        MessageReal message = messageBody.getReal();

        int i = message.getImgUrl().lastIndexOf("/");
        String voiceName = message.getImgUrl().substring(i);
        FileDownloader.getImpl().create(message.getImgUrl())
                .setPath(Constant.AUDIO_SAVE_DIR + voiceName)
                .setForceReDownload(true)
                .setListener(new FileDownloadListener() {
                    //等待
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    }

                    //下载进度回调
                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    }

                    //完成下载
                    @Override
                    protected void completed(BaseDownloadTask task) {
                        ChatBean formMsg = new ChatBean(MultipleItem.FORM_VOICE, messageBody.getSendName(), new Date(), task.getPath());
                        formMsg.sendId = messageBody.getReceiveId();
                        formMsg.sendName = messageBody.getReceiveName();
                        formMsg.receiveId = messageBody.getSendId();
                        formMsg.receiveName = messageBody.getSendName();
                        formMsg.isSend = false;
                        formMsg.conversationId = receiveId;//会话id
                        formMsg.conversationUserName = receiveName;//会话id

                        formMsg.isGroup = MessageBody.TYPE_COMMON == messageBody.getType();

                        addMessageAdapter(formMsg, true);
                    }

                    //暂停
                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    //下载出错
                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        Toast.makeText(ChatActivity.this, "下载出错", Toast.LENGTH_SHORT).show();
                    }

                    //已存在相同下载
                    @Override
                    protected void warn(BaseDownloadTask task) {
                    }
                }).start();
    }

    /**
     * 下载语音文件
     *
     * @param position
     */
    private void downVoice(ChatItem chatItem, int position) {
        //获取消息
        int i = chatItem.chatBean.textContent.lastIndexOf("/");
        String voiceName = chatItem.chatBean.textContent.substring(i + 1);
        FileDownloader.getImpl().create(chatItem.chatBean.textContent)
                .setPath(Constant.AUDIO_SAVE_DIR + voiceName)
                .setForceReDownload(true)
                .setListener(new FileDownloadListener() {
                    //等待
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    }

                    //下载进度回调
                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    }

                    //完成下载
                    @Override
                    protected void completed(BaseDownloadTask task) {
                        Toast.makeText(ChatActivity.this, "完成下载", Toast.LENGTH_SHORT).show();
                        //存放对应的url和本地路径映射关系
//                        PreferenceUtil.putWithUserName(ChatActivity.this,chatItem.chatBean.textContent,Constant.AUDIO_SAVE_DIR + voiceName);
                        DBUtils.saveFilePath(new LocalFileBean(chatItem.chatBean.textContent, Constant.AUDIO_SAVE_DIR + voiceName));

                        chatItem.chatBean.textContent = Constant.AUDIO_SAVE_DIR + voiceName;
                        if (chatItem.chatBean.textContent.endsWith(".voice") || chatItem.chatBean.textContent.endsWith(".m4a")) {
                            View childAt = rvMsg.getChildAt(position);
                            playAudio(childAt, position);
                        } else {
                            ToastUtil.showLongToast(ChatActivity.this, Constant.AUDIO_SAVE_DIR + voiceName + "已完成下载");
//                            openFile(Constant.AUDIO_SAVE_DIR + voiceName);
                        }
                    }

                    //暂停
                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    //下载出错
                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        Toast.makeText(ChatActivity.this, "下载出错", Toast.LENGTH_SHORT).show();
                    }

                    //已存在相同下载
                    @Override
                    protected void warn(BaseDownloadTask task) {
                    }
                }).start();
    }

    /**
     * 请求拍照权限
     */
    private void takePhotoPermission() {
        // 3、调用拍照方法
        PermissionGen.with(ChatActivity.this)
                .addRequestCode(LQRPhotoSelectUtils.REQ_TAKE_PHOTO)
                .permissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                ).request();
    }

    /**
     * 得到拍照权限
     */
    @PermissionSuccess(requestCode = LQRPhotoSelectUtils.REQ_TAKE_PHOTO)
    private void takePhoto() {
        lqrPhotoSelectUtils.takePhoto();
    }

    @PermissionFail(requestCode = LQRPhotoSelectUtils.REQ_TAKE_PHOTO)
    private void showTip1() {
        //Toast.makeText(getApplicationContext(), "不给我权限是吧，那就别玩了", Toast.LENGTH_SHORT).show();
        showDialog();
    }

    /**
     * 显示权限对话框
     */
    public void showDialog() {
        //创建对话框创建器
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //设置对话框显示小图标
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        //设置标题
        builder.setTitle("权限申请");
        //设置正文
        builder.setMessage("在设置-应用-权限 中开启相机、存储权限，才能正常使用拍照或图片选择功能");

        //添加确定按钮点击事件
        builder.setPositiveButton("去设置", new DialogInterface.OnClickListener() {//点击完确定后，触发这个事件

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //这里用来跳到手机设置页，方便用户开启权限
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + ChatActivity.this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        //添加取消按钮点击事件
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        //使用构建器创建出对话框对象
        AlertDialog dialog = builder.create();
        dialog.show();//显示对话框
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //        super.onConfigurationChanged(newConfig);
    }

    /**
     * 保存消息
     */
    private void saveChatMessage(String receiveId) {
        Log.d("zxwl_swf_Thread", "saveChatMessage" + Thread.currentThread().getName());
        List<ChatItem> data = chatAdapter.getData();
        DBUtils.recordSave(data);
    }

    /**
     * 获取消息
     */
    private List<ChatItem> getChatMessage(String receiveId) {
        Log.d("zxwl_swf_Thread", "getChatMessage" + Thread.currentThread().getName());
        List<ChatItem> chatItems = DBUtils.recordQueryInChatItem(LoginCenter.getInstance().getAccount(), receiveId);
        List<ChatItem> newList = new ArrayList<>();

        for (ChatItem item : chatItems) {
            //如果消息不等于通知则显示在列表中
            if (item.chatBean.type != MultipleItem.NOTIFY) {
                newList.add(item);
            }
        }
        return newList;
    }

    /**
     * 保存最后的消息
     */
    private void saveLastMessage() {
        List<ChatItem> data = chatAdapter.getData();
        //如果有发消息
        if (data.size() > 0) {
            ChatItem chatItem = data.get(data.size() - 1);

            chatItem.chatBean.conversationId = receiveId;
            chatItem.chatBean.conversationUserName = receiveName;

            List<ChatItem> newList = new ArrayList<>();
            newList.add(0, chatItem);

            //保存最后一条消息
//            DBUtils.recordSaveLM(newList);

            DBUtils.saveLMItem(chatItem);
        }
    }

    /**
     * 召集会议显示人员选择框
     ***********************************************************/
    private ButtomSelectDialog buttomSelectDialog;

    /**
     * 显示选择对话框
     */
    private void showBottomSelectDialog(boolean isVoice) {
        buttomSelectDialog = new ButtomSelectDialog(this, R.style.CustomDialogStyle);
        buttomSelectDialog.setClickListener(new ButtomSelectDialog.onItemClickListener() {
            @Override
            public void selectClick(int type) {
                switch (type) {
                    case ButtomSelectDialog.TYPE_ONE:
                        oneShoot2CreateConf(Integer.valueOf(receiveId), isVoice);
                        buttomSelectDialog.dismiss();
                        break;

                    case ButtomSelectDialog.TYPE_TWO:
                        ConvokeConfNewActivity.startActivity(ChatActivity.this, isVoice, Integer.parseInt(receiveId));
                        buttomSelectDialog.dismiss();
                        break;
                }
            }
        });
        buttomSelectDialog.show();
    }

    /**
     * 一键召开会议
     *
     * @param id
     */
    private void oneShoot2CreateConf(int id, boolean isVoice) {
        HttpUtils.getInstance(this)
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ConfApi.class)
                .getGroupIdConstacts(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<BaseData<PeopleBean>, Observable<String>>() {
                    @Override
                    public Observable<String> call(BaseData<PeopleBean> baseData) {
                        StringBuilder builder = new StringBuilder();
                        if (BaseData.SUCCESS == baseData.responseCode) {
                            for (PeopleBean temp : baseData.data) {
                                builder.append(",");
                                builder.append(temp.sip);
                            }
                        } else {
                            ToastUtil.showShortToast(App.getContext(), "一键召集会议失败");
                        }
                        return Observable.just(builder.substring(1));
                    }
                })
                .subscribe(new RxSubscriber<String>() {
                    @Override
                    public void onSuccess(String s) {
                        //HuaweiCallImp.getInstance().createConferenceNetWork(TimeUtil.parseDateTime(System.currentTimeMillis()), "120", s, String.valueOf(id), isVoice ? 0 : 1);
                        HuaweiCallImp.getInstance().createConferenceNetWork(DateUtil.getCurrentTime(DateUtil.FORMAT_DATE_TIME), "120", s, String.valueOf(id), "", isVoice ? 0 : 1);
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        ToastUtil.showShortToast(App.getContext(), "一键召集会议失败");
                    }
                });
//                .subscribe(new RxSubscriber<BaseData<PeopleBean>>() {
//                    @Override
//                    public void onSuccess(BaseData<PeopleBean> baseData) {
//                        if (BaseData.SUCCESS == baseData.responseCode) {
//                            if (baseData.data.size() > 0) {
////                                initRecycler(baseData.data);
//                            }
//                        } else {
//                            ToastUtil.showShortToast(App.getContext(), "获取失败");
//                        }
//                    }
//
//                    @Override
//                    protected void onError(ResponeThrowable responeThrowable) {
//                        ToastUtil.showShortToast(LocContext.getContext(),"一键召集会议失败");
//                    }
//                });
    }

}
