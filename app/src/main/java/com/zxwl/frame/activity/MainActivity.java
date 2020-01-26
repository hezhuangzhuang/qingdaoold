package com.zxwl.frame.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.opensdk.commonservice.localbroadcast.CustomBroadcastConstants;
import com.huawei.opensdk.commonservice.localbroadcast.LocBroadcast;
import com.huawei.opensdk.commonservice.localbroadcast.LocBroadcastReceiver;
import com.huawei.opensdk.commonservice.util.LogUtil;
import com.huawei.opensdk.sdkwrapper.login.LoginCenter;
import com.jaeger.library.StatusBarUtil;
import com.zxwl.commonlibrary.AppManager;
import com.zxwl.commonlibrary.BaseActivity;
import com.zxwl.ecsdk.common.UIConstants;
import com.zxwl.ecsdk.utils.IntentConstant;
import com.zxwl.frame.App;
import com.zxwl.frame.R;
import com.zxwl.frame.adapter.item.ChatItem;
import com.zxwl.frame.adapter.item.MultipleItem;
import com.zxwl.frame.bean.ChatBean;
import com.zxwl.frame.bean.EventMsg;
import com.zxwl.frame.bean.MessageBody;
import com.zxwl.frame.bean.MessageReal;
import com.zxwl.frame.bean.TabEntity;
import com.zxwl.frame.db.DBUtils;
import com.zxwl.frame.fragment.ConfFragment;
import com.zxwl.frame.fragment.ContactsFragment;
import com.zxwl.frame.fragment.HomeFragment;
import com.zxwl.frame.fragment.MineFragment;
import com.zxwl.frame.inter.HuaweiCallImp;
import com.zxwl.frame.inter.HuaweiLoginImp;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.push.PushConstant;
import com.zxwl.frame.service.MsgIOServer;
import com.zxwl.frame.utils.Constant;
import com.zxwl.frame.utils.NetworkUtil;
import com.zxwl.frame.utils.PreferenceUtil;
import com.zxwl.frame.utils.VersionUtils;
import com.zxwl.frame.utils.permisson.PermissionUtils;
import com.zxwl.frame.utils.rom.SystemUtil;
import com.zxwl.frame.utils.sharedpreferences.PreferencesHelper;
import com.zxwl.network.api.ImApi;
import com.zxwl.network.api.LoginApi;
import com.zxwl.network.bean.BaseData_logicServer;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import model.UpdateConfig;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import update.UpdateAppUtils;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(IntentConstant.DEFAULT_CATEGORY);
        context.startActivity(intent);
    }

    private CommonTabLayout tabLayout;

    private String[] mTitles = {
            "消息",
            "会议",
            "通讯录",
            "我的"
    };

    private int[] mIconUnselectIds = {
            R.mipmap.ic_message_false,
            R.mipmap.ic_meeting_false,
            R.mipmap.ic_contacts_false,
            R.mipmap.ic_me_false
    };

    private int[] mIconSelectIds = {
            R.mipmap.ic_message_true,
            R.mipmap.ic_meeting_true,
            R.mipmap.ic_contacts_true,
            R.mipmap.ic_me_true
    };

    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();

    private ArrayList<Fragment> fragments = new ArrayList<>();

    /**
     * 判断是否已经点击过一次回退键
     */
    private boolean isBackPressed = false;

    //掉线的标志
    private TextView tvReconnect;

    @Override
    protected void findViews() {
        tabLayout = (CommonTabLayout) findViewById(R.id.tab_layout);
        tvReconnect = (TextView) findViewById(R.id.tv_reconnect);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void initData() {
        //获取华为推送的token
        getHuaweiToken();

        //是否设置了权限
        boolean setPermission = PreferenceUtil.getBoolean(this, Constant.SETTING_PERMISSION, false);
        if (!setPermission) {
            //判断是否是小米并且似乎否有后台弹出权限
            if (SystemUtil.isMIUI()) {
                showBackroundDialog("后台弹出权限,锁屏显示权限");
            }//判断其他型号手机是否有悬浮窗权限
            else {
                showBackroundDialog("悬浮窗权限,锁屏显示权限");
            }
        }

//        if (!canBackgroundStart(this) && SystemUtil.isMIUI()) {
//            showBackroundDialog("后台弹出权限,锁屏显示权限");
//        }//判断其他型号手机是否有悬浮窗权限
//        else if (!FloatWindowsManager.getInstance().checkPermission(this)) {
//            showBackroundDialog("悬浮窗权限,锁屏显示权限");
//        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            //判断是否在白名单
            if (!SystemUtil.isIgnoringBatteryOptimizations()) {
                SystemUtil.requestIgnoreBatteryOptimizations();
            }
        }

        //初始化线程池
        singleThreadExecutor = Executors.newSingleThreadExecutor();

        DBUtils.initGreenDao(App.getInstance(), LoginCenter.getInstance().getAccount());

        checkUpdate();

        StatusBarUtil.setTranslucentForImageView(this, 0, null);

        setTab();

        EventBus.getDefault().register(this);

        AppManager.getInstance().finishActivity(LoginActivity.class);

        //查询华为登陆状态
        createQueryConfInfoRetryRequest();
    }

    /**
     * 显示是否有后台弹出权限
     */
    private void showBackroundDialog(String permissionName) {
        new MaterialDialog.Builder(this)
                .title("提示")
                .content("为了更好的体验应用,请开启" + permissionName)
                .positiveText("去设置")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PreferenceUtil.putBoolean(getApplicationContext(), Constant.SETTING_PERMISSION, true);
                        if ("后台弹出权限".equals(permissionName)) {
                            PermissionUtils.launchAppPermissionSettings();
                        } else {
                            PermissionUtils.launchAppPermissionSettings();
//                            FloatWindowsManager.getInstance().applyPermission(MainActivity.this);
                        }
                    }
                })
                .show();
    }

    /**
     * 判断小米是否有后台弹出权限
     *
     * @param context
     * @return
     */
    public static boolean canBackgroundStart(Context context) {
        AppOpsManager ops = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        try {
            int op = 10021; // >= 23
            // ops.checkOpNoThrow(op, uid, packageName)
            Method method = ops.getClass().getMethod("checkOpNoThrow", new Class[]{int.class, int.class, String.class});
            Integer result = (Integer) method.invoke(ops, op, android.os.Process.myUid(), context.getPackageName());
            return result == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            // Log.e(TAG, "not support", e);
        }
        return false;
    }

    /**
     * 检查更新
     */
    private void checkUpdate() {
        HttpUtils.getInstance(this)
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ImApi.class)
                .checkUpdate()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData_logicServer>() {
                    @Override
                    public void onSuccess(BaseData_logicServer baseData_logicServer) {
                        if (baseData_logicServer.getResponseCode() == 1) {
                            JsonObject json = ((JsonObject) gson.toJsonTree(baseData_logicServer.getData()));
                            String downPath = json.get("apkUrl").getAsString();
                            int versionCode = json.get("versionNumber").getAsInt();
                            String versionName = json.get("versionval").getAsString();
                            String context = json.get("context").getAsString();

//                            UpdateAppUtils.from(MainActivity.this)
//                                    .serverVersionCode(versionCode)  //服务器versionCode
//                                    .serverVersionName(versionName) //服务器versionName
//                                    .apkPath(donwPath) //最新apk下载地址
//                                    .updateInfo(context)
//                                    .update();

                            if (versionCode > VersionUtils.getVerCode()) {
                                UpdateConfig config = new UpdateConfig();
                                config.setServerVersionCode(versionCode);
                                config.setServerVersionName(versionName);
                                config.setShowDownloadingToast(false);
                                config.setAlwaysShowDownLoadDialog(true);

                                UpdateAppUtils.getInstance().deleteInstalledApk();
                                UpdateAppUtils
                                        .getInstance()
                                        .apkUrl(downPath)
                                        .updateTitle("检测到新版本")
                                        .updateContent(context)
                                        .updateConfig(config)
                                        .update();
                            }
                        }
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        Log.d("zxwl_swf", "检查更新失败");
                    }
                });
    }

    /**
     * 停止聊天服务
     */
    public void stopChatService() {
//        MsgIOServer.unRegedit();
        Intent intent = new Intent(this, MsgIOServer.class);
        stopService(intent);
    }

    @Override
    protected void setListener() {
        tvReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent startIntent = new Intent(MainActivity.this, MsgIOServer.class);
//                MainActivity.this.startService(startIntent);

                //掉线之后重启服务
                MsgIOServer.startService(MainActivity.this);
                tvReconnect.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    /**
     * 保存数据状态
     *
     * @param outState
     */
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    private void setTab() {
        for (int i = 0, count = mTitles.length; i < count; i++) {
            mTabEntities.add(new TabEntity(mTitles[i], mIconSelectIds[i], mIconUnselectIds[i]));
        }

        fragments.add(HomeFragment.newInstance());
//        fragments.add(ConfListFragment.newInstance());
        fragments.add(ConfFragment.newInstance());
        fragments.add(ContactsFragment.newInstance());
        fragments.add(MineFragment.newInstance());

        tabLayout.setTabData(mTabEntities, this, R.id.fl_change, fragments);

        tabLayout.setCurrentTab(0);

        tabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
            }

            @Override
            public void onTabReselect(int position) {
            }
        });
    }

//    private void doublePressBackToast() {
//        if (!isBackPressed) {
//            Log.i("doublePressBackToast", "再次点击返回退出程序");
//            isBackPressed = true;
//            Toast.makeText(this, "再次点击返回退出程序", Toast.LENGTH_SHORT).show();
//        } else {
//            Log.i("doublePressBackToast", "exit");
//            finish();
//            AppManager.getInstance().appExit();
//        }
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                isBackPressed = false;
//            }
//        }, 2000);
//    }
//
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            doublePressBackToast();
//            return true;
//        } else {
//            return super.onKeyUp(keyCode, event);
//        }
//    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        new MaterialDialog.Builder(this)
                .title("提示")
                .content("是否退出登录?")
                .positiveText("确定")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        HuaweiLoginImp.getInstance().logOut();

                        PreferenceUtil.put(MainActivity.this, Constant.SIP_ACCOUNT, "");
                        PreferenceUtil.put(MainActivity.this, Constant.SIP_PASSWORD, "");

                        stopChatService();

                        HttpUtils.getInstance(MainActivity.this)
                                .getRetofitClinet()
                                .setBaseUrl(Urls.logicServerURL)
                                .builder(LoginApi.class)
                                .loginout(LoginCenter.getInstance().getAccount())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new RxSubscriber<BaseData_logicServer>() {
                                    @Override
                                    public void onSuccess(BaseData_logicServer baseData_logicServer) {
                                        AppManager.getInstance().appExit();
                                        System.exit(0);
                                    }

                                    @Override
                                    protected void onError(ResponeThrowable responeThrowable) {
                                        AppManager.getInstance().appExit();
                                        System.exit(0);
                                    }
                                });
                        //若超过1s服务器没有响应，直接退出
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                System.exit(0);
                            }
                        }, 1000);
                    }
                })
                .show();
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
//                    Toast.makeText(MainActivity.this, "华为平台登录成功", Toast.LENGTH_SHORT).show();

//                    LoginCenter.getInstance().getSipAccountInfo().setSiteName(PreferencesHelper.getData(UIConstants.SITE_NAME));
//                    MainActivity.startActivity(MainActivity.this);
//                    finish();
                    break;

                case CustomBroadcastConstants.LOGIN_FAILED:
                    String errorMessage = ((String) obj);
                    LogUtil.i(UIConstants.DEMO_TAG, "login failed," + errorMessage);
//                    Toast.makeText(MainActivity.this, "华为平台登录失败：" + errorMessage, Toast.LENGTH_SHORT).show();
                    break;

                case CustomBroadcastConstants.LOGOUT:
                    LogUtil.i(UIConstants.DEMO_TAG, "logout success");
//                    AppManager.getInstance().appExit();

                    break;

                default:
                    break;
            }
        }
    };
    /*华为登录相关end*/

    @Override
    protected void onResume() {
        super.onResume();

        //登录广播
        LocBroadcast.getInstance().registerBroadcast(loginReceiver, mActions);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);

        LocBroadcast.getInstance().unRegisterBroadcast(loginReceiver, mActions);

        unQueryConfInfoSubscribe();

        stopChatService();
        super.onDestroy();
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
                receiveMessage(messageEvent);
                break;

            case EventMsg.HOME_HINT:
                if ("show".equals(messageEvent.getBody())) {
                    tabLayout.showDot(0);
                } else {
                    tabLayout.hideMsg(0);
                }
                break;

            //收到掉线通知
            case EventMsg.DISCONNECT:
                tvReconnect.setVisibility(View.VISIBLE);
                break;

            //连上通知
            case EventMsg.CONNECT:
                tvReconnect.setVisibility(View.GONE);
                break;

            case EventMsg.HOLDCALL:
                TextView backCall = (TextView) findViewById(R.id.tv_backCall);
                if (messageEvent.getBody().toString().equals("hide")) {
                    backCall.setVisibility(View.GONE);
                    Constant.isholdCall = false;
                } else {
                    Constant.isholdCall = true;
//                    backCall.setVisibility(View.VISIBLE);
                    backCall.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            backVideo(messageEvent.getBody().toString());
                            backCall.setVisibility(View.GONE);
                            Constant.isholdCall = false;
                        }
                    });
                }
                break;
        }
    }

    /**
     * 刷新消息
     *
     * @param messageEvent
     */
    private void receiveMessage(EventMsg messageEvent) {
        //获取消息
        MessageBody messageBody = messageEvent.getMessageBody();
        //获取消息内容
        MessageReal message = messageBody.getReal();

        //消息类型
        Integer type = message.getType();
        ChatBean sendMsg = null;
        switch (type) {
            //文本
            case MessageReal.TYPE_STR:
                sendMsg = new ChatBean(MultipleItem.FORM_TEXT,
                        messageBody.getSendName(),
                        new Date(),
                        message.getMessage());
                sendMsg.sendId = messageBody.getSendId();
                sendMsg.sendName = messageBody.getSendName();

                sendMsg.receiveId = messageBody.getReceiveId();
                sendMsg.receiveName = messageBody.getReceiveName();
                sendMsg.isSend = false;
                sendMsg.isRead = false;

                sendMsg.isGroup = MessageBody.TYPE_COMMON == messageBody.getType();
                if (sendMsg.isGroup) {
                    sendMsg.conversationId = messageBody.getReceiveId();
                    sendMsg.conversationUserName = messageBody.getReceiveName();

                    //更新最后一条消息
                    updateLastMessage(sendMsg);

                    //保存当前人的聊天消息
                    saveChatMessage(messageBody.getReceiveId(), sendMsg);
                } else {
                    sendMsg.conversationId = messageBody.getSendId();
                    sendMsg.conversationUserName = messageBody.getSendName();

                    //更新最后一条消息
                    updateLastMessage(sendMsg);

                    //保存当前人的聊天消息
                    saveChatMessage(messageBody.getSendId(), sendMsg);
                }
                break;

            //图片
            case MessageReal.TYPE_IMG:
                sendMsg = new ChatBean(MultipleItem.FORM_IMG, messageBody.getSendName(), new Date(), message.getImgUrl());
                sendMsg.sendId = messageBody.getSendId();
                sendMsg.sendName = messageBody.getSendName();

                sendMsg.receiveId = messageBody.getReceiveId();
                sendMsg.receiveName = messageBody.getReceiveName();
                sendMsg.isSend = false;
                sendMsg.isRead = false;


                sendMsg.isGroup = MessageBody.TYPE_COMMON == messageBody.getType();
                if (sendMsg.isGroup) {
                    sendMsg.conversationId = messageBody.getReceiveId();
                    sendMsg.conversationUserName = messageBody.getReceiveName();

                    //更新最后一条消息
                    updateLastMessage(sendMsg);

                    //保存当前人的聊天消息
                    saveChatMessage(messageBody.getReceiveId(), sendMsg);
                } else {
                    sendMsg.conversationId = messageBody.getSendId();
                    sendMsg.conversationUserName = messageBody.getSendName();

                    //更新最后一条消息
                    updateLastMessage(sendMsg);

                    //保存当前人的聊天消息
                    saveChatMessage(messageBody.getSendId(), sendMsg);
                }
                break;

            //语音
            case MessageReal.TYPE_APPENDIX:
                sendMsg = new ChatBean(MultipleItem.FORM_VOICE, messageBody.getSendName(), new Date(), message.getImgUrl());
                sendMsg.sendId = messageBody.getSendId();
                sendMsg.sendName = messageBody.getSendName();

                sendMsg.receiveId = messageBody.getReceiveId();
                sendMsg.receiveName = messageBody.getReceiveName();
                sendMsg.isSend = false;
                sendMsg.isRead = false;

                sendMsg.isGroup = MessageBody.TYPE_COMMON == messageBody.getType();
                if (sendMsg.isGroup) {
                    sendMsg.conversationId = messageBody.getReceiveId();
                    sendMsg.conversationUserName = messageBody.getReceiveName();

                    //更新最后一条消息
                    updateLastMessage(sendMsg);

                    //保存当前人的聊天消息
                    saveChatMessage(messageBody.getReceiveId(), sendMsg);
                } else {
                    sendMsg.conversationId = messageBody.getSendId();
                    sendMsg.conversationUserName = messageBody.getSendName();

                    //更新最后一条消息
                    updateLastMessage(sendMsg);

                    //保存当前人的聊天消息
                    saveChatMessage(messageBody.getSendId(), sendMsg);
                }
                break;

            //视频呼叫
            case MessageReal.TYPE_VIDEO_CALL:
                sendMsg = new ChatBean(MultipleItem.FORM_VIDEO_CALL, messageBody.getSendName(), new Date(), message.getMessage());

                sendMsg.sendId = messageBody.getSendId();
                sendMsg.sendName = messageBody.getSendName();

                sendMsg.receiveId = messageBody.getReceiveId();
                sendMsg.receiveName = messageBody.getReceiveName();
                sendMsg.isSend = false;
                sendMsg.isRead = false;

                sendMsg.conversationId = messageBody.getSendId();
                sendMsg.conversationUserName = messageBody.getSendName();
                //更新最后一条消息
                updateLastMessage(sendMsg);

                //保存当前人的聊天消息
                saveChatMessage(messageBody.getSendId(), sendMsg);
                break;

            //语音呼叫
            case MessageReal.TYPE_VOICE_CALL:
                sendMsg = new ChatBean(MultipleItem.FORM_VOICE_CALL, messageBody.getSendName(), new Date(), message.getMessage());

                sendMsg.sendId = messageBody.getSendId();
                sendMsg.sendName = messageBody.getSendName();

                sendMsg.receiveId = messageBody.getReceiveId();
                sendMsg.receiveName = messageBody.getReceiveName();
                sendMsg.isSend = false;
                sendMsg.isRead = false;

                sendMsg.conversationId = messageBody.getSendId();
                sendMsg.conversationUserName = messageBody.getSendName();

                //更新最后一条消息
                updateLastMessage(sendMsg);

                //保存当前人的聊天消息
                saveChatMessage(messageBody.getSendId(), sendMsg);
                break;

            //通知
            case MessageReal.TYPE_NOTIFY:
                sendMsg = new ChatBean(MultipleItem.FORM_TEXT, messageBody.getSendName(), new Date(), message.getImgUrl());
                sendMsg.sendId = messageBody.getSendId();
                sendMsg.sendName = messageBody.getSendName();

                sendMsg.receiveId = messageBody.getReceiveId();
                sendMsg.receiveName = messageBody.getReceiveName();
                sendMsg.isSend = false;
                sendMsg.isRead = false;

                sendMsg.isGroup = MessageBody.TYPE_COMMON == messageBody.getType();
                if (sendMsg.isGroup) {
                    //群组重命名
                    if (message.getMessage().startsWith("群组重命名_")) {
                        saveGroupName(messageBody.getReceiveId(), message.getMessage().substring(6));
                    }

                    //通知首页更新数据
                    EventMsg eventMsg = new EventMsg();
                    eventMsg.setMsg(EventMsg.UPDATE_HOME);
                    EventBus.getDefault().post(eventMsg);
                } else {
                    sendMsg.conversationId = messageBody.getSendId();
                    sendMsg.conversationUserName = messageBody.getSendName();

                    //更新最后一条消息
                    updateLastMessage(sendMsg);

                    //保存当前人的聊天消息
                    saveChatMessage(messageBody.getSendId(), sendMsg);
                }
                break;

            default:
                break;
        }
    }

    /**
     * 返回通话页面
     */
    public void backVideo(String type) {
        switch (type) {
            case "video":
                Intent video = new Intent(IntentConstant.VIDEO_ACTIVITY_ACTION);
                video.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(video);
                break;
            case "videoConf":
                Intent videoConf = new Intent(IntentConstant.VIDEO_CONF_ACTIVITY_ACTION);
                videoConf.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(videoConf);
                break;
            case "audio":
                Intent audio = new Intent(IntentConstant.AUDIO_ACTIVITY_ACTION);
                audio.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(audio);
                break;
        }
    }

    /**
     * 保存消息
     */
    private void saveChatMessage(String receiveId, ChatBean chatBean) {
        Activity activity = AppManager.getInstance().currentActivity();
        ChatItem chatItem = new ChatItem(chatBean);
        List<ChatItem> data = getChatMessage(receiveId);
        data.add(chatItem);
        //TODO:屏蔽了保存的方法
//        DBUtils.recordSave(data);

        DBUtils.recordSaveIten(chatItem);
    }

    /**
     * 保存消息
     */
    private List<ChatItem> getChatMessage(String receiveId) {

        return DBUtils.recordQueryInChatItem(LoginCenter.getInstance().getAccount(), receiveId);
    }

    private ExecutorService singleThreadExecutor;

    /**
     * 更新主页列表消息
     *
     * @param chatBean
     */
    private void updateLastMessage(ChatBean chatBean) {
//        Activity activity = AppManager.getInstance().currentActivity();
        //如果当前正显示在主页则更新消息
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                saveLastMessage(chatBean);
//
//                EventMsg eventMsg = new EventMsg();
//                eventMsg.setMsg(EventMsg.UPDATE_HOME);
//                EventBus.getDefault().post(eventMsg);
//            }
//        }).start();

        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                saveLastMessage(chatBean);

                EventMsg eventMsg = new EventMsg();
                eventMsg.setMsg(EventMsg.UPDATE_HOME);
                EventBus.getDefault().post(eventMsg);
            }
        });
    }

    private Gson gson = new Gson();

    /**
     * 保存最后的消息
     */
    private void saveLastMessage(ChatBean chatBean) {
        List<ChatItem> newList = new ArrayList<>();
        ChatItem newChatItem = new ChatItem(chatBean);
        newList.add(0, newChatItem);
//        DBUtils.recordSaveLM(newList);

        DBUtils.saveLMItem(newChatItem);
    }

    /**
     * 修改群组的名称
     */
    private void saveGroupName(String groupId, String newGroupName) {
        List<ChatItem> chatItems = DBUtils.recordQueryAllLM();
        for (ChatItem item : chatItems) {
            if (item.chatBean.conversationId.equals(groupId)) {
                item.chatBean.conversationUserName = newGroupName;
                break;
            }
        }

        ChatItem chatItem = DBUtils.queryByIdLM(groupId);
        chatItem.chatBean.conversationUserName = newGroupName;

//        DBUtils.recordSaveLM(chatItems);
        DBUtils.saveLMItem(chatItem);

        //更新群组名称
        EventMsg eventMsg = new EventMsg();
        eventMsg.setMsg(EventMsg.UPDATE_GROUP);
        EventBus.getDefault().post(eventMsg);
    }

    private Subscription queryHuaweiRegisterSubscription;

    /**
     * 查询华为登陆状态
     */
    private void createQueryConfInfoRetryRequest() {
        unQueryConfInfoSubscribe();

        queryHuaweiRegisterSubscription = Observable.interval(3, TimeUnit.SECONDS)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if (NetworkUtil.isNetworkAvailable(getApplicationContext())) {
                            String registerStatus = PreferencesHelper.getData(UIConstants.REGISTER_RESULT_TEMP);
                            if (!HuaweiCallImp.LOGIN_STATUS.equals(registerStatus)) {
                                HuaweiLoginImp
                                        .getInstance()
                                        .querySiteUri(
                                                MainActivity.this,
                                                LoginCenter.getInstance().getAccount(),
                                                LoginCenter.getInstance().getPassword(),
                                                LoginCenter.getInstance().getLoginServerAddress(),
                                                LoginCenter.getInstance().getSipPort() + "",
                                                Urls.BASE_URL,
                                                "",
                                                Urls.GUOHANG_BASE_URL,
                                                "",
                                                true);
                            }
                        }
                    }
                });
    }

    private void unQueryConfInfoSubscribe() {
        if (null != queryHuaweiRegisterSubscription) {
            if (!queryHuaweiRegisterSubscription.isUnsubscribed()) {
                queryHuaweiRegisterSubscription.unsubscribe();
                queryHuaweiRegisterSubscription = null;
            }
        }
    }

    /**
     * 获取华为的token
     */
    public void getHuaweiToken() {
        new Thread() {
            @Override
            public void run() {
                try {
                    String getToken = HmsInstanceId.getInstance(getApplicationContext()).getToken(PushConstant.HUAWEI_APP_ID, "HCM");
                    Log.i("MainActivity", "getHuaweiToken-->" + getToken);
                    if (!TextUtils.isEmpty(getToken)) {
                        //TODO: Send token to your app server.
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "getHuaweiToken failed.", e);
                }
            }
        }.start();
    }

}
