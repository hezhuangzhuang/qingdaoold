package com.zxwl.frame.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huawei.opensdk.commonservice.localbroadcast.CustomBroadcastConstants;
import com.huawei.opensdk.commonservice.localbroadcast.LocBroadcast;
import com.huawei.opensdk.commonservice.localbroadcast.LocBroadcastReceiver;
import com.huawei.opensdk.commonservice.util.LogUtil;
import com.huawei.opensdk.sdkwrapper.login.LoginCenter;
import com.zxwl.commonlibrary.BaseActivity;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.ecsdk.common.UIConstants;
import com.zxwl.frame.App;
import com.zxwl.frame.R;
import com.zxwl.frame.inter.HuaweiLoginImp;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.service.MsgIOServer;
import com.zxwl.frame.service.MyJobService;
import com.zxwl.frame.utils.AppUtils;
import com.zxwl.frame.utils.Constant;
import com.zxwl.frame.utils.PreferenceUtil;
import com.zxwl.frame.utils.StatusBarUtil;
import com.zxwl.frame.utils.sharedpreferences.PreferencesHelper;
import com.zxwl.network.ApiUrls;
import com.zxwl.network.api.LoginApi;
import com.zxwl.network.bean.BaseData_logicServer;
import com.zxwl.network.bean.LoginInfo;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LauncherActivity extends BaseActivity {
    private Context context;
    private ImageView icImg;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, LauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    @Override
    protected void findViews() {
        icImg = (ImageView) findViewById(R.id.ic_img);
    }

    @Override
    protected void initData() {
        if (getIntent() != null) {
            if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
                finish();
                return;
            }
        }
        StatusBarUtil.setTranslucentForImageView(this, 0, null);

        context = this;

        startAnimation();
    }

    private AlphaAnimation alphaAnimation;
    private ScaleAnimation scaleAnimation;

    private void startAnimation() {
        AnimationSet animationSet = new AnimationSet(true);

        scaleAnimation = new ScaleAnimation(0.2f, 1, 0.2f, 1, Animation.RELATIVE_TO_SELF, 0.5f, 1, 0.5f);
        scaleAnimation.setDuration(1000);

        alphaAnimation = new AlphaAnimation(0.2f, 1.0f);
        alphaAnimation.setDuration(1000);

        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);

        icImg.startAnimation(animationSet);

        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startLogin();
            }
        });
    }

    private void startLogin() {
        String name = PreferenceUtil.getString(context, Constant.USER_NAME, "");
        String pwd = PreferenceUtil.getString(context, Constant.PASS_WORD, "");
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    LoginActivity.startActivity(LauncherActivity.this);
                    finish();
                }
            }, 1500);
        } else {
            login(name, pwd);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //登录广播
        LocBroadcast.getInstance().registerBroadcast(loginReceiver, mActions);
    }

    private void login(String name, String pwd) {
        Urls.logicServerURL = "http://" + PreferenceUtil.getString(context, "logicServer", Urls.DEF_WEBSOCKET_IP) + ":" + PreferenceUtil.getString(context, "logicServerPort", Urls.DEF_WEBSOCKET_PORT) + "/";

        String backserverIP = PreferenceUtil.getString(context, "backserverIP", Urls.DEF_FILE_IP);
        String backserverPort = PreferenceUtil.getString(context, "backserverPort", Urls.DEF_FILE_PORT);
        String websocketIP = PreferenceUtil.getString(context, "websocketIP", Urls.DEF_WEBSOCKET_IP);
        String websocketPort = PreferenceUtil.getString(context, "websocketPort", Urls.DEF_WEBSOCKET_PORT);
        String logicServer = PreferenceUtil.getString(context, "logicServer", Urls.DEF_WEBSOCKET_IP);
        String logicServerPort = PreferenceUtil.getString(context, "logicServerPort", Urls.DEF_WEBSOCKET_PORT);

        if (TextUtils.isEmpty(backserverIP)
                || TextUtils.isEmpty(backserverPort)
                || TextUtils.isEmpty(websocketIP)
                || TextUtils.isEmpty(websocketPort)
                || TextUtils.isEmpty(logicServer)
                || TextUtils.isEmpty(logicServerPort)) {
            ToastUtil.showShortToast(context, "请先配置服务器信息");
            LoginActivity.startActivity(LauncherActivity.this);
            return;
        }

        String imei = AppUtils.getDeviceIdIMEI(this);

        HttpUtils.getInstance(this)
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(LoginApi.class)
                .getLoginInfo(ApiUrls.GET_LOGIN_INFO, imei, name, pwd)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData_logicServer>() {
                    @Override
                    public void onSuccess(BaseData_logicServer baseData) {
                        if (baseData.getResponseCode() == 1) {
                            Gson gson = new Gson();
                            LoginInfo info = gson.fromJson(gson.toJson(baseData.getData()), LoginInfo.class);

                            Constant.CurrDisPlayName = info.getName();
                            Constant.CurrID = info.getId();

                            PreferenceUtil.put(context, Constant.SMC_IP, info.getScIp());
                            PreferenceUtil.put(context, Constant.SMC_PORT, info.getScPort());

                            loginRequest(info.getSipAccount(), info.getSipPassword());
                        } else if (baseData.getResponseCode() == 2) {
                            ToastUtil.showShortToast(App.getContext(), baseData.getMessage());
                            LoginActivity.startActivity(LauncherActivity.this);
                            finish();
                        } else {
                            ToastUtil.showShortToast(App.getContext(), "获取AD域鉴权错误，登录失败");
                            LoginActivity.startActivity(LauncherActivity.this);
                            finish();
                        }
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        ToastUtil.showShortToast(context, "登录失败，可能存在网络异常");
                        LoginActivity.startActivity(LauncherActivity.this);
                        finish();
                    }
                });
    }

    private void loginRequest(String name, String pwd) {
        String backserverIP = PreferenceUtil.getString(context, "backserverIP", Urls.DEF_FILE_IP);
        String backserverPort = PreferenceUtil.getString(context, "backserverPort", Urls.DEF_FILE_PORT);
        String websocketIP = PreferenceUtil.getString(context, "websocketIP", Urls.DEF_WEBSOCKET_IP);
        String websocketPort = PreferenceUtil.getString(context, "websocketPort", Urls.DEF_WEBSOCKET_PORT);
        String logicServer = PreferenceUtil.getString(context, "logicServer", Urls.DEF_WEBSOCKET_IP);
        String logicServerPort = PreferenceUtil.getString(context, "logicServerPort", Urls.DEF_WEBSOCKET_PORT);

        String smcIP = PreferenceUtil.getString(context, Constant.SMC_IP, "120.221.95.141");
        String smcPort = PreferenceUtil.getString(context, Constant.SMC_PORT, "5060");

        HuaweiLoginImp
                .getInstance()
                .querySiteUri(
                        this,
                        name,
                        pwd,
                        smcIP,
                        smcPort,
                        backserverIP,
                        backserverPort,
                        backserverIP,
                        backserverPort
                );
        MsgIOServer.websocketIP = websocketIP;
        MsgIOServer.websocketPort = websocketPort;
    }

    @Override
    protected void setListener() {
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_launcher;
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
                    Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show();

                    PreferenceUtil.putBoolean(getApplicationContext(), Constant.HAS_LOGIN, true);

                    //是否登录
                    PreferencesHelper.saveData(UIConstants.IS_LOGIN, true);
                    PreferencesHelper.saveData(UIConstants.REGISTER_RESULT, "0");

                    //存放用户信息
                    String name = PreferenceUtil.getString(context, "CurrUserName", "");
                    String pwd = PreferenceUtil.getString(context, "CurrPassWrod", "");
                    Constant.CurrUserName = name;
                    Constant.CurrPassWrod = pwd;

                    startChatService();

                    MainActivity.startActivity(context);
                    LocBroadcast.getInstance().unRegisterBroadcast(loginReceiver, mActions);
                    finish();
                    break;

                case CustomBroadcastConstants.LOGIN_FAILED:
//                    DialogUtils.dismissProgressDialog(context);
                    String errorMessage = ((String) obj);
                    LogUtil.i(UIConstants.DEMO_TAG, "login failed," + errorMessage);
                    Toast.makeText(context, "华为平台登录失败：" + errorMessage, Toast.LENGTH_SHORT).show();
                    LoginActivity.startActivity(LauncherActivity.this);
                    finish();
                    break;

                case CustomBroadcastConstants.LOGOUT:
                    LogUtil.i(UIConstants.DEMO_TAG, "logout success");
                    break;

                default:
                    break;
            }
        }
    };

    private void startChatService() {
        Log.e("zxwl_swf", "尝试启动服务");
        String account = LoginCenter.getInstance().getAccount();
//        String i = account.substring(account.length() - 1, account.length());
        String i = account;
        Intent intent = new Intent(this, MsgIOServer.class);
        intent.putExtra(Constant.USER_ID, i);
        startService(intent);

        MyJobService.startJob(this);
    }

}
