package com.zxwl.frame.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.huawei.opensdk.commonservice.util.LogUtil;
import com.zxwl.ecsdk.common.UIConstants;
import com.zxwl.frame.activity.LoginDialogActivity;
import com.zxwl.frame.utils.sharedpreferences.PreferencesHelper;

public class NetWorkChangReceiver extends BroadcastReceiver {
    public static final String TAG = "NetWorkChangReceiver";

    /**
     * 获取连接类型
     *
     * @param type
     * @return
     */
    private String getConnectionType(int type) {
        String connType = "";
        if (type == ConnectivityManager.TYPE_MOBILE) {
            connType = "3G网络数据";
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            connType = "WIFI网络";
        }
        return connType;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {// 监听wifi的打开与关闭，与wifi的连接无关
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            Log.e(TAG, "wifiState:" + wifiState);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    break;
            }
        }
        // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            //获取联网状态的NetworkInfo对象
            NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (info != null) {
                //如果当前的网络连接成功并且网络连接可用
                if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                    if (info.getType() == ConnectivityManager.TYPE_WIFI || info.getType() == ConnectivityManager.TYPE_MOBILE) {
                        LogUtil.i(UIConstants.DEMO_TAG, TAG + getConnectionType(info.getType()) + "连接");

                        boolean isLogin = false;
                        boolean isNetWorkDis = false;
                        Integer data = -1;
                        try {
                            isLogin = PreferencesHelper.getData(UIConstants.IS_LOGIN, Boolean.class);
                            isNetWorkDis = PreferencesHelper.getData(UIConstants.DISCONNECT_NETWORK, Boolean.class);
                            data = PreferencesHelper.getData(UIConstants.REGISTER_RESULT, Integer.class);
                        } catch (Exception e) {
                        }


                        //如果已经登录，并且网络断开则重新登录下
                        if (isLogin && 834 == data) {
                            //网络没有断开
                            LoginDialogActivity.startActivity(context);
                        }
                    }
                } else {
                    //网络断开
                    PreferencesHelper.saveData(UIConstants.DISCONNECT_NETWORK, true);
                    LogUtil.i(UIConstants.DEMO_TAG, TAG + getConnectionType(info.getType()) + "断开");
                }
            }
        }
    }
}
