package com.zxwl.frame.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huawei.opensdk.commonservice.localbroadcast.CustomBroadcastConstants;
import com.huawei.opensdk.commonservice.localbroadcast.LocBroadcast;
import com.huawei.opensdk.commonservice.localbroadcast.LocBroadcastReceiver;
import com.huawei.opensdk.commonservice.util.LogUtil;
import com.huawei.opensdk.sdkwrapper.login.LoginCenter;
import com.zxwl.commonlibrary.BaseActivity;
import com.zxwl.commonlibrary.utils.DialogUtils;
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
import com.zxwl.frame.utils.sharedpreferences.PreferencesHelper;
import com.zxwl.network.ApiUrls;
import com.zxwl.network.api.LoginApi;
import com.zxwl.network.bean.BaseData_logicServer;
import com.zxwl.network.bean.LoginInfo;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;

import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private EditText etName;
    private EditText etPwd;
    private TextView tvAutoLogin;
    private Button btLogin;
    private TextView tvserver;
    private ImageView ivPwdShow;

    private boolean isAutoLogin = false;//是否自动登录
    private boolean isShowPwd;//密码是否显示

    private Gson gson = new Gson();
    private String TAG = "LoginActivity";

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void findViews() {
        etName = (EditText) findViewById(R.id.et_name);
        etPwd = (EditText) findViewById(R.id.et_pwd);
        tvAutoLogin = (TextView) findViewById(R.id.tv_auto_login);
        btLogin = (Button) findViewById(R.id.bt_login);
        tvserver = (TextView) findViewById(R.id.tv_server);
        ivPwdShow = (ImageView) findViewById(R.id.iv_pwd_show);
    }

    @Override
    protected void initData() {
        if (!isCameraUseable()) {
            Toast.makeText(LoginActivity.this, "检测到摄像头权限未启用，请打开摄像头权限并重启应用", Toast.LENGTH_SHORT).show();
        }

        if (getIntent() != null) {
            if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
                finish();
                return;
            }
        }

        String lastName = PreferenceUtil.getString(getApplicationContext(), Constant.USER_NAME, "");
        String lastPwd = PreferenceUtil.getString(getApplicationContext(), Constant.PASS_WORD, "");

        etName.setText(lastName);
        etPwd.setText(lastPwd);

//        if (!TextUtils.isEmpty(PreferenceUtil.getString(LoginActivity.this, "CurrUserName", ""))) {
//            //显示上次登录成功的账号密码
//            etName.setText(PreferenceUtil.getString(LoginActivity.this, "CurrUserName", ""));
//        }
//        if (!TextUtils.isEmpty(PreferenceUtil.getString(LoginActivity.this, "CurrPassWrod", ""))) {
//            //显示上次登录成功的账号密码
//            etPwd.setText(PreferenceUtil.getString(LoginActivity.this, "CurrPassWrod", ""));
//        }

        tvAutoLogin.setVisibility(View.INVISIBLE);
        setAutoPic();
    }

    @Override
    protected void setListener() {
        tvAutoLogin.setOnClickListener(this);
        btLogin.setOnClickListener(this);
        tvserver.setOnClickListener(this);
        ivPwdShow.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_auto_login:
                isAutoLogin = !isAutoLogin;
                setAutoPic();
                break;

            case R.id.bt_login:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PermissionGen.with(this)
                            .addRequestCode(LoginActivity.REQUEST_IMEI_CODE)
                            .permissions(
                                    Manifest.permission.READ_PHONE_STATE
                            ).request();
                } else {
                    login();
                }

                break;

            case R.id.tv_server:
                Intent intent = new Intent(LoginActivity.this, ServerActivity.class);
                startActivity(intent);
                break;

            case R.id.iv_pwd_show:
                isShowPwd = !isShowPwd;
                if (isShowPwd) {
                    //如果选中，显示密码
                    etPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivPwdShow.setImageResource(R.mipmap.ic_pwd_show);
                } else {
                    //否则隐藏密码
                    etPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivPwdShow.setImageResource(R.mipmap.ic_pwd_hide);
                }
                setSelection(etPwd);
                break;
            default:
                break;
        }
    }


    private void setSelection(EditText editText) {
        //设置光标位置
        CharSequence text = editText.getText();
        if (text instanceof Spannable) {
            Spannable spanText = (Spannable) text;
            Selection.setSelection(spanText, text.length());
        }
    }

    private void setAutoPic() {
        tvAutoLogin.setCompoundDrawablesWithIntrinsicBounds(isAutoLogin ? R.mipmap.ic_autologin_true : R.mipmap.ic_autologin_false, 0, 0, 0);
    }

    private void login() {
        String name = etName.getText().toString().trim();
        String pwd = etPwd.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(pwd)) {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        Urls.logicServerURL = "http://" + PreferenceUtil.getString(LoginActivity.this, "logicServer", Urls.DEF_WEBSOCKET_IP) + ":" + PreferenceUtil.getString(LoginActivity.this, "logicServerPort", Urls.DEF_WEBSOCKET_PORT) + "/";

        String backserverIP = PreferenceUtil.getString(LoginActivity.this, "backserverIP", Urls.DEF_FILE_IP);
        String backserverPort = PreferenceUtil.getString(LoginActivity.this, "backserverPort", Urls.DEF_FILE_PORT);
        String websocketIP = PreferenceUtil.getString(LoginActivity.this, "websocketIP", Urls.DEF_WEBSOCKET_IP);
        String websocketPort = PreferenceUtil.getString(LoginActivity.this, "websocketPort", Urls.DEF_WEBSOCKET_PORT);
        String logicServer = PreferenceUtil.getString(LoginActivity.this, "logicServer", Urls.DEF_WEBSOCKET_IP);
        String logicServerPort = PreferenceUtil.getString(LoginActivity.this, "logicServerPort", Urls.DEF_WEBSOCKET_PORT);

        if (TextUtils.isEmpty(backserverIP)
                || TextUtils.isEmpty(backserverPort)
                || TextUtils.isEmpty(websocketIP)
                || TextUtils.isEmpty(websocketPort)
                || TextUtils.isEmpty(logicServer)
                || TextUtils.isEmpty(logicServerPort)) {
            ToastUtil.showShortToast(LoginActivity.this, "请先配置服务器信息");
            return;
        }

        //登录广播
        LocBroadcast.getInstance().registerBroadcast(loginReceiver, mActions);

        DialogUtils.showProgressDialog(this, "正在登录...");

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
                            LoginInfo info = gson.fromJson(gson.toJson(baseData.getData()), LoginInfo.class);

                            Constant.CurrDisPlayName = info.getName();
                            Constant.CurrID = info.getId();

                            //im的显示名称和id
                            PreferenceUtil.put(LoginActivity.this, Constant.DISPLAY_NAME, info.getName());
                            PreferenceUtil.put(LoginActivity.this, Constant.CURRENT_ID, info.getId());

                            //smc地址和登录账号
                            PreferenceUtil.put(LoginActivity.this, Constant.SMC_IP, info.getScIp());
                            PreferenceUtil.put(LoginActivity.this, Constant.SMC_PORT, info.getScPort());

                            PreferenceUtil.put(LoginActivity.this, Constant.SIP_ACCOUNT, info.getSipAccount());
                            PreferenceUtil.put(LoginActivity.this, Constant.SIP_PASSWORD, info.getSipPassword());

                            loginRequest(info.getSipAccount(), info.getSipPassword());
                        } else if (baseData.getResponseCode() == 2) {
                            ToastUtil.showShortToast(App.getContext(), baseData.getMessage());
                            DialogUtils.dismissProgressDialog(LoginActivity.this);
                        } else {
                            ToastUtil.showShortToast(App.getContext(), "获取AD域鉴权错误，登录失败");
                            DialogUtils.dismissProgressDialog(LoginActivity.this);
                        }
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        ToastUtil.showShortToast(LoginActivity.this, "登录失败，可能存在网络异常");
                        DialogUtils.dismissProgressDialog(LoginActivity.this);
                    }
                });
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
            Log.i(TAG, "loginReceiver-->" + broadcastName);
            switch (broadcastName) {
                case CustomBroadcastConstants.LOGIN_SUCCESS:
                    DialogUtils.dismissProgressDialog(LoginActivity.this);
                    LogUtil.i(UIConstants.DEMO_TAG, "login success");
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();

                    PreferenceUtil.putBoolean(getApplicationContext(), Constant.HAS_LOGIN, true);

                    //是否登录
                    PreferencesHelper.saveData(UIConstants.IS_LOGIN, true);
                    PreferencesHelper.saveData(UIConstants.REGISTER_RESULT, "0");

                    //存放用户信息
                    Constant.CurrUserName = etName.getText().toString().trim();
                    Constant.CurrPassWrod = etPwd.getText().toString().trim();
                    
                    //登录成功记录账号密码到文本中
                    PreferenceUtil.put(LoginActivity.this, "CurrUserName", Constant.CurrUserName);
                    PreferenceUtil.put(LoginActivity.this, "CurrPassWrod", Constant.CurrPassWrod);

                    //PreferenceUtil.putStringProcess(LoginActivity.this, Constant.USER_NAME, etName.getText().toString().trim());
                    //PreferenceUtil.putStringProcess(LoginActivity.this, Constant.PASS_WORD, etPwd.getText().toString().trim());
                    PreferenceUtil.put(LoginActivity.this, Constant.USER_NAME, etName.getText().toString().trim());
                    PreferenceUtil.put(LoginActivity.this, Constant.PASS_WORD, etPwd.getText().toString().trim());

                    startChatService();

                    MainActivity.startActivity(LoginActivity.this);

                    LocBroadcast.getInstance().unRegisterBroadcast(loginReceiver, mActions);
                    finish();
                    break;

                case CustomBroadcastConstants.LOGIN_FAILED:
                    DialogUtils.dismissProgressDialog(LoginActivity.this);
                    String errorMessage = ((String) obj);
                    LogUtil.i(UIConstants.DEMO_TAG, "login failed," + errorMessage);
                    Toast.makeText(LoginActivity.this, "华为平台登录失败：" + errorMessage, Toast.LENGTH_SHORT).show();
                    break;

                case CustomBroadcastConstants.LOGOUT:
                    LogUtil.i(UIConstants.DEMO_TAG, "logout success");
                    break;

                default:
                    break;
            }
        }
    };
    /*华为登录相关end*/

    private void startChatService() {
        Log.e("zxwl_swf", "尝试启动服务");
        String account = LoginCenter.getInstance().getAccount();
        Intent intent = new Intent(this, MsgIOServer.class);
        intent.putExtra(Constant.USER_ID, account);
        startService(intent);

        MyJobService.startJob(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
//        LocBroadcast.getInstance().unRegisterBroadcast(loginReceiver, mActions);
        super.onDestroy();
    }

    private void loginRequest(String name, String pwd) {
        String backserverIP = PreferenceUtil.getString(LoginActivity.this, "backserverIP", Urls.DEF_FILE_IP);
        String backserverPort = PreferenceUtil.getString(LoginActivity.this, "backserverPort", Urls.DEF_FILE_PORT);
        String websocketIP = PreferenceUtil.getString(LoginActivity.this, "websocketIP", Urls.DEF_WEBSOCKET_IP);
        String websocketPort = PreferenceUtil.getString(LoginActivity.this, "websocketPort", Urls.DEF_WEBSOCKET_PORT);
        String logicServer = PreferenceUtil.getString(LoginActivity.this, "logicServer", Urls.DEF_WEBSOCKET_IP);
        String logicServerPort = PreferenceUtil.getString(LoginActivity.this, "logicServerPort", Urls.DEF_WEBSOCKET_PORT);
        String smcIP = PreferenceUtil.getString(LoginActivity.this, Constant.SMC_IP, "120.221.95.141");
        String smcPort = PreferenceUtil.getString(LoginActivity.this, Constant.SMC_PORT, "5060");

        HuaweiLoginImp
                .getInstance()
                .querySiteUri(
                        LoginActivity.this,
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

    /**
     * 检测摄像头可用性
     *
     * @return
     */
    public static boolean isCameraUseable() {
        boolean canUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            // setParameters 是针对魅族MX5。MX5通过Camera.open()拿到的Camera对象不为null
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            canUse = false;
        }
        if (mCamera != null) {
            mCamera.release();
        }
        return canUse;
    }

    public final static int REQUEST_IMEI_CODE = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    /**
     * 获取信息的权限
     */
    @PermissionSuccess(requestCode = LoginActivity.REQUEST_IMEI_CODE)
    public void takePhoto() {
        login();
    }

    /**
     * 得到拍照权限
     */
    @PermissionFail(requestCode = LoginActivity.REQUEST_IMEI_CODE)
    public void take() {
        Toast.makeText(this.getApplicationContext(), "需要获取手机唯一码", Toast.LENGTH_SHORT).show();
    }
}
