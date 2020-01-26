package com.zxwl.frame.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.zxwl.frame.bean.ResponeBean;
import com.zxwl.frame.rx.RxBus;

/**
 * 检测电话状态的广播
 */
public class CallReceiver extends BroadcastReceiver {
    public static final String TAG = "CallReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(CallReceiver.TAG, "PhoneStateReceiver action: " + action);

        String resultData = this.getResultData();
        Log.d(CallReceiver.TAG, "PhoneStateReceiver getResultData: " + resultData);

        if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            // 去电，可以用定时挂断
            // 双卡的手机可能不走这个Action
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d(CallReceiver.TAG, "PhoneStateReceiver EXTRA_PHONE_NUMBER: " + phoneNumber);
        } else {
            // 来电去电都会走
            // 获取当前电话状态
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            Log.d(CallReceiver.TAG, "PhoneStateReceiver onReceive state: " + state);

            //来电响铃
            if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
                Log.d(CallReceiver.TAG, "EXTRA_STATE_RINGING-->");
            }// 来电接通
            else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                Log.d(CallReceiver.TAG, "EXTRA_STATE_OFFHOOK");

                ResponeBean responeBean = new ResponeBean();
                responeBean.command = "EXTRA_STATE_OFFHOOK";
                RxBus.getInstance().post(responeBean);
            } //电话挂断
            else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {
                Log.d(CallReceiver.TAG, "EXTRA_STATE_IDLE");
                ResponeBean responeBean = new ResponeBean();
                responeBean.command = "EXTRA_STATE_IDLE";
                RxBus.getInstance().post(responeBean);
            }
        }
    }
}
