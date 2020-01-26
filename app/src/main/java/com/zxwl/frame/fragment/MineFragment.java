package com.zxwl.frame.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.huawei.opensdk.sdkwrapper.login.LoginCenter;
import com.zxwl.commonlibrary.BaseFragment;
import com.zxwl.frame.App;
import com.zxwl.frame.R;
import com.zxwl.frame.activity.AboutActivity;
import com.zxwl.frame.activity.BindPhoneActivity;
import com.zxwl.frame.activity.ChangePwdActivity;
import com.zxwl.frame.activity.LoginActivity;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.service.MsgIOServer;
import com.zxwl.frame.utils.Constant;
import com.zxwl.frame.utils.PreferenceUtil;
import com.zxwl.network.api.LoginApi;
import com.zxwl.network.bean.BaseData_logicServer;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MineFragment extends BaseFragment {
    private TextView tvName;

    public MineFragment() {
    }

    public static MineFragment newInstance() {
        MineFragment fragment = new MineFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View inflateContentView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_mine, container, false);
    }

    @Override
    protected void findViews(View view) {
        tvName = (TextView) view.findViewById(R.id.tv_name);

        view.findViewById(R.id.bt_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(getActivity())
                        .title("提示")
                        .content("是否退出登录?")
                        .positiveText("确定")
                        .negativeText("取消")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                logOut();
                            }
                        })
                        .show();
            }
        });

        view.findViewById(R.id.tv_funding).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutActivity.startActivity(getActivity());
            }
        });

        TextView bind = view.findViewById(R.id.tv_bind);
        bind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BindPhoneActivity.class);
                startActivity(intent);
            }
        });

        TextView changePassWord = view.findViewById(R.id.tv_changePassWord);
        changePassWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChangePwdActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void init() {
//        tvName.setText(LoginCenter.getInstance().getAccountName());
        String disPlayName = PreferenceUtil.getString(getContext(), Constant.DISPLAY_NAME, "");
        tvName.setText(disPlayName);
    }

    @Override
    protected void addListeners() {

    }

    /**
     * 退出登陆
     */
    private void logOut() {
        HttpUtils.getInstance(getContext())
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(LoginApi.class)
                .loginout(LoginCenter.getInstance().getAccount())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData_logicServer>() {
                    @Override
                    public void onSuccess(BaseData_logicServer baseData_logicServer) {
                        App.relogin(true);

                        //停止服务
                        MsgIOServer.stopService(getContext());
                        //
                        PreferenceUtil.put(getContext(), Constant.PASS_WORD, "");
                        PreferenceUtil.putBoolean(getContext(), Constant.HAS_LOGIN, false);

                        PreferenceUtil.put(getContext(), Constant.DISPLAY_NAME, "");

                        LoginActivity.startActivity(getContext());
                        getActivity().finish();
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {

                    }
                });
    }
}
