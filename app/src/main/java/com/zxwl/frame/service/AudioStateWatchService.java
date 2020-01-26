package com.zxwl.frame.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;

import com.huawei.opensdk.commonservice.common.LocContext;
import com.huawei.opensdk.commonservice.util.LogUtil;
import com.zxwl.ecsdk.common.UIConstants;


public class AudioStateWatchService extends Service {

    IntentFilter intentFilter = new IntentFilter();

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    //拔出耳机，切换至扬声器模式
//                    TupMgr.getInstance().getCallManagerIns().setMobileAudioRoute(TupCallParam.CALL_E_MOBILE_AUIDO_ROUTE.CALL_MOBILE_AUDIO_ROUTE_LOUDSPEAKER);
                    useSpeaker();
                    LogUtil.d(UIConstants.DEMO_TAG, "耳机设备拔出");
                    break;
                case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                    //蓝牙耳机状态变化
                    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

                    if (BluetoothProfile.STATE_CONNECTING == adapter.getProfileConnectionState(BluetoothProfile.HEADSET)) {
                        //蓝牙耳机连接
                        LogUtil.d(UIConstants.DEMO_TAG, "接入蓝牙耳机中");
//                        TupMgr.getInstance().getCallManagerIns().setMobileAudioRoute(TupCallParam.CALL_E_MOBILE_AUIDO_ROUTE.CALL_MOBILE_AUDIO_ROUTE_BLUETOOTH);
                        useBltHS();
                    }

                    break;
                case Intent.ACTION_HEADSET_PLUG:
                    //有线耳机状态变化
                    if (intent.hasExtra("state")){
                        if (intent.getIntExtra("state", 0) == 1){
                            //有线耳机连接
                            LogUtil.d(UIConstants.DEMO_TAG, "接入有线耳机");
//                            TupMgr.getInstance().getCallManagerIns().setMobileAudioRoute(TupCallParam.CALL_E_MOBILE_AUIDO_ROUTE.CALL_MOBILE_AUDIO_ROUTE_HEADSET);
                            useWireHS();
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(UIConstants.DEMO_TAG, "音频设备状态变化广播启动");
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    /**
     * 切换声道至蓝牙耳机
     */
    private static void useBltHS(){
        AudioManager am = (AudioManager) LocContext.getContext().getSystemService(Context.AUDIO_SERVICE);
        am.setBluetoothScoOn(true);
        am.setMicrophoneMute(false);
        am.setSpeakerphoneOn(false);
        am.setWiredHeadsetOn(false);
        am.setMode(AudioManager.MODE_NORMAL);
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    /**
     * 使用有线耳机
     */
    private static void useWireHS(){
        AudioManager am = (AudioManager) LocContext.getContext().getSystemService(Context.AUDIO_SERVICE);
        am.setBluetoothScoOn(false);
        am.setMicrophoneMute(false);
        am.setSpeakerphoneOn(false);
        am.setWiredHeadsetOn(true);
        am.setMode(AudioManager.MODE_NORMAL);
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    /**
     * 使用扬声器
     */
    private static void useSpeaker(){
        AudioManager am = (AudioManager) LocContext.getContext().getSystemService(Context.AUDIO_SERVICE);
        am.setBluetoothScoOn(false);
        am.setMicrophoneMute(false);
        am.setSpeakerphoneOn(true);
        am.setWiredHeadsetOn(false);
        am.setMode(AudioManager.MODE_NORMAL);
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    /**
     * 获取当前耳机接入情况
     * @return
     */
    private static int getCurSysAudioSate() {
        AudioManager mAudioManager = (AudioManager) LocContext.getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        //获取当前使用的麦克风，设置媒体播放麦克风
        if (isBluetoothHeadsetConnected()) {
            //蓝牙耳机
            return 2;
        } else if (mAudioManager.isWiredHeadsetOn()){
            //有线耳机
            return 1;
        } else {
            //扬声器
            return 0;
        }
    }

    /**
     * 判断蓝牙耳机是否连接
     * @return
     */
    private static boolean isBluetoothHeadsetConnected() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (BluetoothProfile.STATE_CONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEADSET)) {
            return true;
        }
        return false;
    }

    public static void suitCurrAudioDevice(){
        switch (getCurSysAudioSate()){
            case 0://没有任何耳机设备
//                callManager.setMobileAudioRoute(TupCallParam.CALL_E_MOBILE_AUIDO_ROUTE.CALL_MOBILE_AUDIO_ROUTE_LOUDSPEAKER);
                useSpeaker();
                break;
            case 1:
//                callManager.setMobileAudioRoute(TupCallParam.CALL_E_MOBILE_AUIDO_ROUTE.CALL_MBOILE_AUDIO_ROUTE_DEFAULT);
                useWireHS();
                break;
            case 2:
//                callManager.setMobileAudioRoute(TupCallParam.CALL_E_MOBILE_AUIDO_ROUTE.CALL_MBOILE_AUDIO_ROUTE_DEFAULT);
                useBltHS();
                break;
        }
    }
}
