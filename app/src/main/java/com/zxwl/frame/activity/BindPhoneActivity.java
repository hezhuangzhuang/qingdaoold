package com.zxwl.frame.activity;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.zxwl.commonlibrary.BaseActivity;
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

/**
 * 绑定手机
 */
public class BindPhoneActivity extends BaseActivity {
    private ImageView ivBackOperate;
    private TextView tvTopTitle;
    private TextView tvRightOperate;
    private EditText etPhone;
    private TextView tvGetCode;
    private EditText etCode;
    private Context context;
    private Gson gson;

    @Override
    protected void findViews() {
        ivBackOperate = (ImageView) findViewById(R.id.iv_back_operate);
        tvTopTitle = (TextView) findViewById(R.id.tv_top_title);
        tvRightOperate = (TextView) findViewById(R.id.tv_right_operate);
        etPhone = (EditText) findViewById(R.id.et_phone);
        tvGetCode = (TextView) findViewById(R.id.tv_get_code);
        etCode = (EditText) findViewById(R.id.et_code);
    }

    @Override
    protected void initData() {
        tvTopTitle.setText("绑定手机");
        tvRightOperate.setText("确定");
        tvRightOperate.setVisibility(View.VISIBLE);

        ivBackOperate.setImageResource(R.mipmap.general_back_icon_white);
        ivBackOperate.setVisibility(View.VISIBLE);

        context = this;
        gson = new Gson();

        getBindNumber();

    }

    /**
     * 获取绑定的手机号
     */
    private void getBindNumber() {
        HttpUtils.getInstance(context)
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ImApi.class)
                .getBindPhone(Constant.CurrID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData_logicServer>() {
                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
//                        ToastUtil.showShortToast(context,"网络异常，请稍后重试");
                    }

                    @Override
                    public void onSuccess(BaseData_logicServer baseData_logicServer) {
                        if (baseData_logicServer.getResponseCode() == 1) {
                            JsonObject json = ((JsonObject) gson.toJsonTree(baseData_logicServer.getData()));
                            String telephone = json.get("telephone").getAsString();
                            if (!TextUtils.isEmpty(telephone)) {
//                                etPhone.setHint(telephone);
                                etPhone.setText(telephone);
                            }
                        } else {
//                            ToastUtil.showLongToast(context,baseData_logicServer.getMessage());
                        }
                    }
                });
    }

    @Override
    protected void setListener() {
        ivBackOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvGetCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        tvRightOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = etPhone.getText().toString().trim();
                if (result.length() != 11) {
                    ToastUtil.showLongToast(context, "请输入11位电话号码");
                    return;
                }
                HttpUtils.getInstance(context)
                        .getRetofitClinet()
                        .setBaseUrl(Urls.logicServerURL)
                        .builder(ImApi.class)
                        .bindPhone(Constant.CurrID, result)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new RxSubscriber<BaseData_logicServer>() {
                            @Override
                            protected void onError(ResponeThrowable responeThrowable) {
                                ToastUtil.showShortToast(context, "网络异常，请稍后重试");
                            }

                            @Override
                            public void onSuccess(BaseData_logicServer baseData_logicServer) {
                                if (baseData_logicServer.getResponseCode() == 1) {
                                    ToastUtil.showLongToast(context, "绑定成功");
                                    finish();
                                } else {
                                    ToastUtil.showLongToast(context, baseData_logicServer.getMessage());
                                }
                            }
                        });
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_bind_phone;
    }
}
