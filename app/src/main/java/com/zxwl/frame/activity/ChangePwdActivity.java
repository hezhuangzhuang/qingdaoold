package com.zxwl.frame.activity;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zxwl.commonlibrary.BaseActivity;
import com.zxwl.commonlibrary.utils.DialogUtils;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.frame.R;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.utils.Constant;
import com.zxwl.network.api.ImApi;
import com.zxwl.network.bean.BaseData_logicServer;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ChangePwdActivity extends BaseActivity {
    private ImageView ivBackOperate;
    private TextView tvTopTitle;
    private TextView tvRightOperate;
    private EditText etOldPwd;
    private EditText etNewPwd;
    private EditText etConfirmPwd;
    private Context context;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ChangePwdActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void findViews() {
        ivBackOperate = (ImageView) findViewById(R.id.iv_back_operate);
        tvTopTitle = (TextView) findViewById(R.id.tv_top_title);
        tvRightOperate = (TextView) findViewById(R.id.tv_right_operate);
        etOldPwd = (EditText) findViewById(R.id.et_old_pwd);
        etNewPwd = (EditText) findViewById(R.id.et_new_pwd);
        etConfirmPwd = (EditText) findViewById(R.id.et_confirm_pwd);
    }

    @Override
    protected void initData() {
        context = this;

        tvTopTitle.setText("修改密码");
        tvRightOperate.setText("确定");
        tvRightOperate.setVisibility(View.VISIBLE);

        ivBackOperate.setImageResource(R.mipmap.general_back_icon_white);
        ivBackOperate.setVisibility(View.VISIBLE);
    }

    @Override
    protected void setListener() {
        ivBackOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvRightOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPwd = etOldPwd.getText().toString().trim();
                if(!checkPwd(oldPwd, "旧密码")){
                    return;
                }

                String newPwd = etNewPwd.getText().toString().trim();
                if (!checkPwd(newPwd, "新密码")){
                    return;
                }

                String confirmPwd = etConfirmPwd.getText().toString().trim();
                if (!checkPwd(confirmPwd, "新密码")){
                    return;
                }

                try {
                    if (!newPwd.equals(confirmPwd)) {
                        ToastUtil.showShortToast(getApplicationContext(),"新密码两次输入不相同");
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                DialogUtils.showProgressDialog(ChangePwdActivity.this, "正在修改...");
                updatePwdRequest();

            }
        });
    }

    private void updatePwdRequest() {
        HttpUtils.getInstance(context)
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ImApi.class)
                .changePWD(Constant.CurrID,etOldPwd.getText().toString(),etNewPwd.getText().toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData_logicServer>() {
                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        ToastUtil.showLongToast(context,"修改密码失败，网络可能存在问题");
                        DialogUtils.dismissProgressDialog(context);
                    }

                    @Override
                    public void onSuccess(BaseData_logicServer baseData_logicServer) {
                        if (baseData_logicServer.getResponseCode() == 1){
                            ToastUtil.showShortToast(context,"密码修改成功");
                            finish();
                        }else {
                            ToastUtil.showLongToast(context,baseData_logicServer.getMessage());
                        }
                        DialogUtils.dismissProgressDialog(context);
                    }
                });
    }

    public boolean checkPwd(String oldPwd, String hintText) {
        if (TextUtils.isEmpty(oldPwd)) {
            ToastUtil.showShortToast(getApplicationContext(),hintText + "不能为空");
            return false;
        }

        if (oldPwd.length() > 20 || oldPwd.length() < 6) {
            ToastUtil.showShortToast(getApplicationContext(),hintText + "长度应为6~20位");
            return false;
        }
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_change_pwd;
    }

}
