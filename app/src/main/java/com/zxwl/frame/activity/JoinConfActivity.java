package com.zxwl.frame.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.frame.R;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.utils.Constant;
import com.zxwl.frame.utils.PreferenceUtil;
import com.zxwl.network.ApiUrls;
import com.zxwl.network.api.ConfApi;
import com.zxwl.network.bean.BaseData_BackServer;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 弃用
 */
public class JoinConfActivity extends AppCompatActivity {
    private EditText et_accessCode;
    private Button bt_join;
    private ImageView iv_back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joinconf);

        initView();
        setListener();
    }

    private void initView() {
        et_accessCode = (EditText) findViewById(R.id.et_accessCode);
        bt_join = (Button) findViewById(R.id.bt_join);
        iv_back = (ImageView) findViewById(R.id.iv_back);
    }

    private void setListener() {
        bt_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String accessCode = et_accessCode.getText().toString().trim();
                if (TextUtils.isEmpty(accessCode)) {
                    ToastUtil.showShortToast(getApplicationContext(), "请输入会议接入码");
                    return;
                }

                String userName = PreferenceUtil.getString(getApplicationContext(), Constant.USER_NAME, "");

                HttpUtils.getInstance(getApplicationContext())
                        .getRetofitClinet()
                        .setBaseUrl(Urls.GUOHANG_BASE_URL)
                        .builder(ConfApi.class)
//                        .joinConf(ApiUrls.JOIN_CONF, accessCode, Constant.CurrUserName)
                        .joinConf(ApiUrls.JOIN_CONF, accessCode, userName)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new RxSubscriber<BaseData_BackServer>() {
                            @Override
                            public void onSuccess(BaseData_BackServer baseData_backServer) {
                                if (baseData_backServer.getCode() == 0) {
                                } else {
                                    ToastUtil.showShortToast(getApplicationContext(), "入会失败，参数错误");
                                }
                            }

                            @Override
                            protected void onError(ResponeThrowable responeThrowable) {
                                ToastUtil.showShortToast(getApplicationContext(), "入会失败，请稍后重试");
                            }
                        });

            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
