package com.zxwl.frame.push.huawei;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;
import com.huawei.opensdk.commonservice.util.LogUtil;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.utils.Constant;
import com.zxwl.frame.utils.PreferenceUtil;
import com.zxwl.network.api.ConfApi;
import com.zxwl.network.bean.BaseData_BackServer;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PushHmsMessageService extends HmsMessageService {
    public static final String TAG = "PushHmsMessageService";

    public PushHmsMessageService() {
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        LogUtil.i(TAG, "onNewToken-->" + s);
        saveHuaweiPushToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        LogUtil.i(TAG, "onMessageReceived-->" + remoteMessage.toString());
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();

        LogUtil.i(TAG, "onDeletedMessages-->");
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);

        LogUtil.i(TAG, "onMessageSent-->" + s);
    }

    /**
     * 保存华为的推送token
     */
    private void saveHuaweiPushToken(String token) {
        String sipAccount = PreferenceUtil.getString(this, Constant.SIP_ACCOUNT, "");
        HttpUtils.getInstance(this)
                .getRetofitClinet()
                .setBaseUrl(Urls.BASE_URL)
                .builder(ConfApi.class)
                .saveHuaweiToken(sipAccount, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData_BackServer>() {
                    @Override
                    public void onSuccess(BaseData_BackServer logicData) {
                        LogUtil.i(TAG, "saveHuaweiPushToken-->onSuccess-->" + logicData.toString());
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        ToastUtil.showShortToast(getApplicationContext(), responeThrowable.getMessage());
                    }
                });

    }
}
