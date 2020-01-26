package com.zxwl.frame.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.huawei.opensdk.commonservice.localbroadcast.CustomBroadcastConstants;
import com.huawei.opensdk.commonservice.localbroadcast.LocBroadcast;
import com.huawei.opensdk.commonservice.localbroadcast.LocBroadcastReceiver;
import com.huawei.opensdk.commonservice.util.LogUtil;
import com.huawei.opensdk.sdkwrapper.login.LoginCenter;
import com.zxwl.commonlibrary.AppManager;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.ecsdk.common.UIConstants;
import com.zxwl.frame.App;
import com.zxwl.frame.R;
import com.zxwl.frame.activity.ChatActivity;
import com.zxwl.frame.activity.LoginActivity;
import com.zxwl.frame.bean.EventMsg;
import com.zxwl.frame.bean.GroupIDBean;
import com.zxwl.frame.bean.MessageBody;
import com.zxwl.frame.bean.MessageReal;
import com.zxwl.frame.bean.WebSocketResult;
import com.zxwl.frame.db.DBUtils;
import com.zxwl.frame.inter.HuaweiCallImp;
import com.zxwl.frame.inter.HuaweiLoginImp;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.utils.AppUtils;
import com.zxwl.frame.utils.Constant;
import com.zxwl.frame.utils.LogUtils;
import com.zxwl.frame.utils.NetworkUtil;
import com.zxwl.frame.utils.NotificationUtils;
import com.zxwl.frame.utils.PreferenceUtil;
import com.zxwl.frame.utils.sharedpreferences.PreferencesHelper;
import com.zxwl.imdemo.IMyAidlInterface;
import com.zxwl.network.api.ConfApi;
import com.zxwl.network.api.ImApi;
import com.zxwl.network.bean.BaseData_BackServer;
import com.zxwl.network.bean.BaseData_logicServer;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;

import org.greenrobot.eventbus.EventBus;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.huawei.opensdk.commonservice.common.LocContext.getContext;

public class MsgIOServer extends Service {
    private final static String TAG = "zxwl_swf";
    public static String websocketIP = "";
    public static String websocketPort = "";
    private static String websocketWSurl = "";
    public static String websocketUrl = "";
    private static Gson gson;
    private static Timer timer;
    private static TimerTask task;

    private static JWebSocketClient client;

    private String imei;

    private static int reConnCount = 0;

    /**
     * 最大重试次数
     */
    private final static int maxReConn = 1024;

    private String account;

    //socket的状态,0:正常，1:断开，2:正在连接
    private int socketStatus ;

    /**
     * 启动服务
     *
     * @param context
     */
    public static void startService(Context context) {
        Intent startIntent = new Intent(context, MsgIOServer.class);
        context.startService(startIntent);
    }

    /**
     * 启动服务
     *
     * @param context
     */
    public static void startService(Context context, String userId) {
        Intent startIntent = new Intent(context, MsgIOServer.class);
        startIntent.putExtra(Constant.USER_ID, userId);
        context.startService(startIntent);
    }

    /**
     * 停止服务
     *
     * @param context
     */
    public static void stopService(Context context) {
        Intent stopIntent = new Intent(context, MsgIOServer.class);
        context.stopService(stopIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //获取当前的sip
        account = PreferenceUtil.getString(getApplicationContext(), Constant.SIP_ACCOUNT, LoginCenter.getInstance().getAccount());

        //登录广播
        LocBroadcast.getInstance().registerBroadcast(loginReceiver, mActions);

        initData();

        gson = new Gson();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocBroadcast.getInstance().unRegisterBroadcast(loginReceiver, mActions);
        //打印日志
        LogUtils.d("MsgIOServer-->onDestroy");

        //关闭连接
        closeConnect();

        reConnCount = 0;

        unQueryConfInfoSubscribe();
    }

    public MsgIOServer() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //绑定服务
        bindService(new Intent(this, LocalService.class), new MyServiceConnection(), BIND_AUTO_CREATE);

        //获取设备号
        imei = AppUtils.getDeviceIdIMEI(this);

        Log.d(TAG, "分发消息服务已启动");
        //配置websocket状态量
        String userid = "";
        if (null != intent) {
            userid = intent.getStringExtra(Constant.USER_ID);
        }
        websocketWSurl = "ws://" + websocketIP + ":" + websocketPort + "/im/websocket/im/client?sip="+account;
        websocketUrl = "http://" + websocketIP + ":" + websocketPort;

//        if (TextUtils.isEmpty(userid)) {
////            websocketWSurl = websocketWSurl + LoginCenter.getInstance().getAccount();
//            websocketWSurl = websocketWSurl + account;
//        } else {
//            websocketWSurl = websocketWSurl + userid;
//        }

//        websocketWSurl = websocketWSurl + account;

        if (null == client) {
            //创建websocekt对象
            initSocketConnect();
        }

        //轮询查询当前的状态
        createQueryConfInfoRetryRequest();

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 之前启动服务的方法
     */
    private void startTimer() {
        //登录成功后，保持心跳
        timer = new Timer();

        task = new TimerTask() {
            @Override
            public void run() {
                try {
                    if (timer == null && task == null) {
                        cancel();
                        return;
                    }
                    pingServer();

                    client.send("keep alive");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(task, 5000, 5000);
    }

    /**
     * 建立webscoket链接
     */
    private void initSocketConnect() {
        try {
            URI uri = URI.create(websocketWSurl);
            client = new JWebSocketClient(uri) {
                @Override
                public void onMessage(String message) {
                    Log.e("JWebSClientService", "message-->" + message);
                    handleMsg(message);
                }

                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    super.onOpen(handshakedata);
                    //重连次数归零
                    reConnCount = 0;

                    Log.e("JWebSClientService", "onOpen-->" + handshakedata);
                    EventMsg eventMsg = new EventMsg();
                    eventMsg.setMsg(EventMsg.CONNECT);
                    EventBus.getDefault().post(eventMsg);
                }
            };
            client.connectBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理消息
     *
     * @param text
     */
    private void handleMsg(String text) {
        Log.i("handleMsg", "这是消息内容" + text);
        try {
            WebSocketResult result = gson.fromJson(text, WebSocketResult.class);
            if (result.getCode() == -1) {
                //发送消息失败，此时是消息发送的状态响应
            } else {
                switch (result.getEvent()) {
                    case WebSocketResult.OFFLINE_MESSAGE:
                        ArrayList list = (ArrayList) result.getData();
                        for (Object obj : list) {
                            MessageBody body = gson.fromJson(gson.toJson(obj), MessageBody.class);
                            EventMsg eventMsg = new EventMsg();
                            eventMsg.setMsg(EventMsg.RECEIVE_MESSAGE);
                            eventMsg.setBody(body);
                            EventBus.getDefault().post(eventMsg);
                            Log.d(TAG, "OFFLINE_MESSAGE:" + body.toString());
                        }
                        break;

                    case WebSocketResult.CONNECTION_FAIL:
                        String msg_fail = (String) result.getData();
                        Log.d(TAG, "CONNECTION_FAIL --" + msg_fail);
                        break;

                    case WebSocketResult.PROCESS_MESSAGES_FAIL:
                        String msg_fail2 = (String) result.getData();
                        Log.d(TAG, "PROCESS_MESSAGES_FAIL --" + msg_fail2);
                        break;

                    case WebSocketResult.MESSAGE:
                        MessageBody msg_single = null;
                        try {
                            msg_single = (MessageBody) result.getData();
                        } catch (Exception e) {
                            msg_single = gson.fromJson(gson.toJson(result.getData()), MessageBody.class);
                        }
                        //如果是个人则直接发送
                        if (msg_single.getType() == MessageBody.TYPE_PERSONAL) {
                            EventMsg eventMsg = new EventMsg();
                            eventMsg.setMsg(EventMsg.RECEIVE_MESSAGE);
                            eventMsg.setBody(msg_single);
                            EventBus.getDefault().post(eventMsg);

                            //发送notif
                            sendNotif(eventMsg);
                        } else {//群组
                            //存放群组标识
                            DBUtils.saveGroupInfo(new GroupIDBean(msg_single.getReceiveId(), true));

                            //String userId = account.substring(account.length() - 1, account.length());
                            String userId = account;
                            //如果是群组则判断发送人的id跟用户id是否相同，如果相同则不发消息
                            if (!userId.equals(msg_single.getSendId())) {
                                EventMsg eventMsg = new EventMsg();
                                eventMsg.setMsg(EventMsg.RECEIVE_MESSAGE);
                                eventMsg.setBody(msg_single);
                                EventBus.getDefault().post(eventMsg);

                                //发送notif
                                sendNotif(eventMsg);
                            } else if (5 == msg_single.getReal().getType()) {
                                EventMsg eventMsg = new EventMsg();
                                eventMsg.setMsg(EventMsg.RECEIVE_MESSAGE);
                                eventMsg.setBody(msg_single);
                                EventBus.getDefault().post(eventMsg);
                            }
                        }
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "返回数据:" + text);
    }

    /**
     * 心跳接口
     */
    private void pingServer() {
        HttpUtils.getInstance(this)
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ImApi.class)
                .ping(account, imei)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData_logicServer>() {
                    @Override
                    public void onSuccess(BaseData_logicServer logicData) {
                        Log.d("zxwl_ping", "心跳检测onSuccess" + logicData.getData().toString());

                        //打印日志
//                        LogUtils.d(logicData.toString());

                        if (!TextUtils.isEmpty(logicData.toString()) && logicData.getData().toString().contains("isWebSocketOnline=0.0")) {
                            stopself();
                        }

                        if (!TextUtils.isEmpty(logicData.toString()) && logicData.getData().toString().contains("isPhoneRemove=1.0")) {
                            ToastUtil.showLongToast(getContext(), "你当前的账号已在一个设备上登录");

                            unQueryConfInfoSubscribe();

                            stopService(getContext());

                            //是否登录
                            PreferenceUtil.putBoolean(getContext(), Constant.HAS_LOGIN, false);
                            PreferenceUtil.put(getContext(), Constant.PASS_WORD, "");

                            App.mutilLogin(false);
                            AppManager.getInstance().appExit();
                            LoginActivity.startActivity(getContext());
                        }
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        Log.d("zxwl_ping", "心跳检测onError" + responeThrowable.getMessage());
                        stopself();
                    }
                });
    }

    //是否在登录中
    private boolean isLoginIng = false;

    /**
     * 查询华为在线状态
     */
    private void pingHuawei() {
        HttpUtils.getInstance(this)
                .getRetofitClinet()
                .setBaseUrl(Urls.GUOHANG_BASE_URL)
                .builder(ConfApi.class)
                .querySiteOnlineState(account)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData_BackServer>() {
                    @Override
                    public void onSuccess(BaseData_BackServer baseData) {
                        if (BaseData_BackServer.SUCCESS == baseData.getCode()) {
                            if (!baseData.getMsg().contains("SIP状态在线")) {
                                //如果不在登录中则开始登录
                                if (!isLoginIng) {
                                    loginHuawei();
                                }
                            }
                        }
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        //打印日志
                        LogUtils.d("pingHuawei-->responeThrowable" + responeThrowable.getMessage());
                    }
                });
    }

    public void stopself() {
        //重试次数大于
        if (reConnCount >= maxReConn) {
            App.stopMsgServer();
            EventMsg eventMsg = new EventMsg();
            eventMsg.setMsg(EventMsg.DISCONNECT);
            EventBus.getDefault().post(eventMsg);
        } else {
            reconnectWs();
            reConnCount = reConnCount + 1;
        }
    }

    public static boolean sendMsg(MessageBody msg) {
        while (null != client && !ReadyState.OPEN.equals(client.getReadyState())) {
            reconnectWs();
            Log.d(TAG, "连接中···请稍后");
        }
        //websocket发送消息
        try {
            //websocketWSurl 对应的WebSocket已经打开可以这样send,否则报错
            MessageReal real = msg.getReal();
            client.send(gson.toJson(msg));
            Log.d(TAG, "发送数据" + msg.toString());
            return true;
        } catch (Exception e) {
            ToastUtil.showLongToast(App.getContext(), "消息发送失败，您当前可能已经处于离线状态");
            App.reStartMsgServer();
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 断开连接
     */
    private static void closeConnect() {
        try {
            if (null != client) {
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client = null;
        }
    }

    /**
     * 注销
     */
    public static void unRegedit() {
        closeConnect();

        reConnCount = 0;

        if (task != null) {
            task.cancel();
        }
        if (timer != null) {
            timer.cancel();
        }
        task = null;
        timer = null;
    }

    private static ExecutorService singleThreadExecutor;

    /**
     * 开启重连
     */
    private static void reconnectWs() {
        if (null == singleThreadExecutor) {
            singleThreadExecutor = Executors.newSingleThreadExecutor();
        }

        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (null != client) {
                    try {
                        //重连
//                        boolean result = client.reconnectBlocking();
                        boolean result = false;
                        client.reconnect();
                        Log.i(TAG, "reconnectWs--正在重连...结果" + result + ",线程名称");
                    } catch (Exception e) {
                        //打印日志
                        LogUtils.d("重新启动服务报错:" + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    //重新启动服务
                    startService(getContext());

                    //打印日志
                    LogUtils.d("重新启动服务");
                }
            }
        });
    }

    /**
     * 发送notif-start************************************
     */
    private static Intent chatIntent = new Intent(getContext(), ChatActivity.class);

    /**
     * 发送notif
     *
     * @param eventMsg
     */
    private void sendNotif(EventMsg eventMsg) {
        Integer type = eventMsg.getMessageBody().getReal().getType();
        if (MessageReal.TYPE_VIDEO_CALL == type || MessageReal.TYPE_VOICE_CALL == type) {
            return;
        }
        Activity activity = AppManager.getInstance().currentActivity();
        if (activity instanceof ChatActivity) {
//            ToastUtil.showShortToast(getApplicationContext(), "当前是聊天界面");
            return;
        } else {
//            ToastUtil.showShortToast(getApplicationContext(), "当前界面");
        }

        //判断是不是群聊
        boolean isGroup = eventMsg.getMessageBody().getType() == 2;
        //获取notifid
        int notifId = Integer.valueOf(isGroup ? eventMsg.getMessageBody().getReceiveId() : eventMsg.getMessageBody().getSendId());

        //获得消息
        String notifText = getNotifText(eventMsg);
        NotificationUtils.notify(notifId, new NotificationUtils.Func1<Void, NotificationCompat.Builder>() {
            @Override
            public Void call(NotificationCompat.Builder param) {
                //群聊
                if (isGroup) {
                    chatIntent.putExtra(ChatActivity.USER_ID, eventMsg.getMessageBody().getReceiveId());
                    chatIntent.putExtra(ChatActivity.USER_NAME, eventMsg.getMessageBody().getReceiveName());
                    chatIntent.putExtra(ChatActivity.IS_GROUP, true);
                } else {
                    chatIntent.putExtra(ChatActivity.USER_ID, eventMsg.getMessageBody().getSendId());
                    chatIntent.putExtra(ChatActivity.USER_NAME, eventMsg.getMessageBody().getSendName());
                    chatIntent.putExtra(ChatActivity.IS_GROUP, false);
                }
                param.setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(isGroup ? eventMsg.getMessageBody().getReceiveName() : eventMsg.getMessageBody().getSendName())
                        .setContentText(notifText)
                        //使用默认的声音和震动
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                        //设置该通知的优先级
                        .setPriority(Notification.PRIORITY_HIGH)
                        //状态栏的通知
                        .setContentIntent(PendingIntent.getActivity(getContext(), 0, chatIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                        //浮动通知（弹窗式通知）
//                        .setFullScreenIntent(PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT), false)
                        .setAutoCancel(true);
                return null;
            }
        });
    }

    private String getNotifText(EventMsg eventMsg) {
        String content = "";
        /*1:文字; 2:图片; 3:表情； 4：语音*/
        switch (eventMsg.getMessageBody().getReal().getType()) {
            case 1:
                return eventMsg.getMessageBody().getReal().getMessage();

            case 2:
                return "[图片]";

            case 3:
                return "[表情]";

            case 4:
                return "[文件]";

            default:
                return content;
        }
    }

    /**
     * 发送notif-end************************************
     */


    private Subscription queryHuaweiRegisterSubscription;

    /**
     * 查询华为登陆状态
     */
    private void createQueryConfInfoRetryRequest() {
        unQueryConfInfoSubscribe();

        queryHuaweiRegisterSubscription = Observable.interval(5, TimeUnit.SECONDS)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        boolean networkAvailable = NetworkUtil.isNetworkAvailable(getApplicationContext());
                        boolean hasLogin = PreferenceUtil.getBoolean(getApplicationContext(), Constant.HAS_LOGIN, false);
                        if (!hasLogin) {
                            unQueryConfInfoSubscribe();
                            return;
                        }
                        if (networkAvailable) {
                            //判断服务是否还在
                            pingServer();

                            //判断华为是否在线
                            pingHuawei();

                            String registerStatus = PreferencesHelper.getData(UIConstants.REGISTER_RESULT_TEMP);
                            if (!HuaweiCallImp.LOGIN_STATUS.equals(registerStatus)) {
                                //登录华为
//                                loginHuawei();

                                //判断当前状态是否为开启
                                if (null != client && ReadyState.OPEN.equals(client.getReadyState())) {
                                    //发送消息
                                    client.send("keep alive");
                                }
                            }

                            //判断当前状态是否为开启
                            if (null != client) {
                                if (client.isClosed() || !ReadyState.OPEN.equals(client.getReadyState())) {
                                    reconnectWs();
                                }
                            } else {
                                initSocketConnect();
                            }

                            //打印日志
//                            LogUtils.d("createQueryConfInfoRetryRequest-->聊天状态--->" + ReadyState.OPEN.equals(client.getReadyState()) + "，华为云状态--->" + registerStatus);
                        } else {
                            //打印日志
                            LogUtils.d("断网了");
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        //打印日志
                        LogUtils.d("createQueryConfInfoRetryRequest-->轮询出异常了--->" + throwable.getMessage());
                    }
                });
    }

    /**
     * 登录华为平台
     */
    private void loginHuawei() {
        String account = PreferenceUtil.getString(getApplicationContext(), Constant.SIP_ACCOUNT, LoginCenter.getInstance().getAccount());
        String passWord = PreferenceUtil.getString(getApplicationContext(), Constant.SIP_PASSWORD, LoginCenter.getInstance().getPassword());
        String smcIp = PreferenceUtil.getString(getApplicationContext(), Constant.SMC_IP, LoginCenter.getInstance().getLoginServerAddress());
        String smcPort = PreferenceUtil.getString(getApplicationContext(), Constant.SMC_PORT, LoginCenter.getInstance().getSipPort() + "");
        //打印日志
        LogUtils.d("开始重新登陆-->account:" + account + ",passWord:" + passWord + ",smcIp:" + smcIp + ",smcPort:" + smcPort);
        //登陆华为
        HuaweiLoginImp.getInstance().loginRequest(this, account, passWord, smcIp, smcPort);

        isLoginIng = true;
    }

    private void unQueryConfInfoSubscribe() {
        if (null != queryHuaweiRegisterSubscription) {
            if (!queryHuaweiRegisterSubscription.isUnsubscribed()) {
                queryHuaweiRegisterSubscription.unsubscribe();
                queryHuaweiRegisterSubscription = null;
            }
        }
    }

    private MyBinder myBinder;

    class MyBinder extends IMyAidlInterface.Stub {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
                               double aDouble, String aString) throws RemoteException {
        }
    }

    private void initData() {
        myBinder = new MyBinder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("deamon", "deamon",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            if (manager == null)
                return;
            manager.createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this)
                    .setAutoCancel(true)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setOngoing(true)
                    .setPriority(NotificationManager.IMPORTANCE_LOW)
                    .build();
            startForeground(10, notification);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //如果 18 以上的设备 启动一个Service startForeground给相同的id
            //然后结束那个Service
            startForeground(10, new Notification());
            startService(new Intent(this, InnnerService.class));
        } else {
            startForeground(10, new Notification());
        }
    }

    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            startService(new Intent(MsgIOServer.this, LocalService.class));
            bindService(new Intent(MsgIOServer.this, LocalService.class),
                    new MyServiceConnection(), BIND_AUTO_CREATE);
        }
    }

    public static class InnnerService extends Service {
        @Override
        public void onCreate() {
            super.onCreate();
            startForeground(10, new Notification());
            stopSelf();
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

    /*华为登录相关start*/
    public static String[] mActions = new String[]{
            CustomBroadcastConstants.LOGIN_SUCCESS,
            CustomBroadcastConstants.LOGIN_FAILED,
            CustomBroadcastConstants.LOGOUT
    };

    private LocBroadcastReceiver loginReceiver = new LocBroadcastReceiver() {
        @Override
        public void onReceive(String broadcastName, Object obj) {
            switch (broadcastName) {
                case CustomBroadcastConstants.LOGIN_SUCCESS:
                    LogUtil.i(UIConstants.DEMO_TAG, "login success");
                    //登录成功
                    isLoginIng = false;

//                    Toast.makeText(getApplicationContext(), "华为平台登录成功", Toast.LENGTH_SHORT).show();
                    //打印日志
                    LogUtils.d("华为平台登录成功");
                    break;

                case CustomBroadcastConstants.LOGIN_FAILED:
                    //登录失败
                    isLoginIng = false;

                    String errorMessage = ((String) obj);
                    //打印日志
                    LogUtils.d("华为平台登录失败-->" + errorMessage);
                    LogUtil.i(UIConstants.DEMO_TAG, "login failed," + errorMessage);
//                    Toast.makeText(getApplicationContext(), "华为平台登录失败：" + errorMessage, Toast.LENGTH_SHORT).show();
                    break;

                case CustomBroadcastConstants.LOGOUT:
                    LogUtil.i(UIConstants.DEMO_TAG, "logout success");
                    break;

                default:
                    break;
            }
        }
    };

}
