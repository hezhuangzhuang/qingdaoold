package com.zxwl.frame.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.opensdk.commonservice.localbroadcast.CustomBroadcastConstants;
import com.huawei.opensdk.commonservice.localbroadcast.LocBroadcast;
import com.huawei.opensdk.commonservice.localbroadcast.LocBroadcastReceiver;
import com.huawei.opensdk.commonservice.util.LogUtil;
import com.huawei.opensdk.sdkwrapper.login.LoginCenter;
import com.zxwl.ecsdk.common.UIConstants;
import com.zxwl.frame.R;
import com.zxwl.frame.inter.HuaweiLoginImp;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.utils.sharedpreferences.PreferencesHelper;

public class LoginDialogActivity extends AppCompatActivity {

    public static void startActivity(Context context) {
//        Intent intent = new Intent(LocContext.getContext(), LoginDialogActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        LocContext.getContext().startActivity(intent);
    }

    private void setListener() {
        findViewById(R.id.bt_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(LoginDialogActivity.this, "登录超时,请稍后再试", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }, 60 * 1000);
                login();
            }
        });

        findViewById(R.id.bt_cancle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HuaweiLoginImp.getInstance().logOut();
                finish();
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_dialog);

        setListener();

        login();
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

                    LoginCenter.getInstance().getSipAccountInfo().setSiteName(PreferencesHelper.getData(UIConstants.SITE_NAME));
                    //是否登录
                    PreferencesHelper.saveData(UIConstants.IS_LOGIN, true);
                    PreferencesHelper.saveData(UIConstants.REGISTER_RESULT, "0");
                    Toast.makeText(LoginDialogActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    dismissDialog();
                    finish();
                    break;

                case CustomBroadcastConstants.LOGIN_FAILED:
                    String errorMessage = ((String) obj);
                    LogUtil.i(UIConstants.DEMO_TAG, "login failed," + errorMessage);

//                    if (errorMessage.contains("error code:480") || errorMessage.contains("Timeout error")) {
                    login();
//                    }
                    break;

                case CustomBroadcastConstants.LOGOUT:
                    LogUtil.i(UIConstants.DEMO_TAG, "logout success");
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        //登录广播
        LocBroadcast.getInstance().registerBroadcast(loginReceiver, mActions);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //登录广播
        LocBroadcast.getInstance().unRegisterBroadcast(loginReceiver, mActions);
    }

    private void login() {
//        showProgressDialog();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LogUtil.i(UIConstants.DEMO_TAG, "开始登录");

                HuaweiLoginImp.getInstance().logOut();

                HuaweiLoginImp
                        .getInstance()
                        .querySiteUri(
                                LoginDialogActivity.this,
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
        }, 1 * 1000);
    }

    private ProgressDialog dialog;

    private void showProgressDialog() {
        showProgressDialog(this, "");
    }

    private void dismissDialog() {
        dismissProgressDialog(this);
    }

    private Dialog progressDialog;

    public void showProgressDialog(Context context, String content) {
        if (context instanceof Activity) {
            if (((Activity) context).isFinishing()) {
                return;
            }
        }

        if (null != progressDialog && progressDialog.isShowing()) {
            return;
        }

        if (null == progressDialog) {
            progressDialog = new Dialog(context, R.style.CustomDialogStyle);
        }

        View dialogView = View.inflate(context, R.layout.dialog_progress, null);
        TextView tvContent = dialogView.findViewById(R.id.tv_content);
        if (TextUtils.isEmpty(content)) {
            tvContent.setVisibility(View.GONE);
        } else {
            tvContent.setText(content);
            tvContent.setVisibility(View.VISIBLE);
        }
        progressDialog.setContentView(dialogView);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        try {
            progressDialog.show();
        } catch (WindowManager.BadTokenException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * 隐藏对话框
     *
     * @return
     */
    public void dismissProgressDialog(Context context) {
        if (context instanceof Activity) {
            if (((Activity) context).isFinishing()) {
                progressDialog = null;
                return;
            }
        }

        if (progressDialog != null && progressDialog.isShowing()) {
            Context loadContext = progressDialog.getContext();
            if (loadContext != null && loadContext instanceof Activity) {
                if (((Activity) loadContext).isFinishing()) {
                    progressDialog = null;
                    return;
                }
            }
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

}
