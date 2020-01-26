package com.zxwl.frame.activity;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.gson.GsonBuilder;
import com.huawei.opensdk.callmgr.CallConstant;
import com.huawei.opensdk.callmgr.CallInfo;
import com.huawei.opensdk.callmgr.CallMgr;
import com.huawei.opensdk.commonservice.localbroadcast.CustomBroadcastConstants;
import com.huawei.opensdk.commonservice.localbroadcast.LocBroadcast;
import com.huawei.opensdk.commonservice.localbroadcast.LocBroadcastReceiver;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.huawei.opensdk.sdkwrapper.login.LoginCenter;
import com.zxwl.commonlibrary.utils.DateUtil;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.ecsdk.common.UIConstants;
import com.zxwl.ecsdk.logic.CallFunc;
import com.zxwl.ecsdk.utils.IntentConstant;
import com.zxwl.frame.R;
import com.zxwl.frame.adapter.AddSiteAdapter;
import com.zxwl.frame.adapter.item.ChatItem;
import com.zxwl.frame.adapter.item.MultipleItem;
import com.zxwl.frame.bean.ChatBean;
import com.zxwl.frame.bean.EventMsg;
import com.zxwl.frame.bean.MessageBody;
import com.zxwl.frame.bean.MessageReal;
import com.zxwl.frame.bean.respone.ConfBeanRespone;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.service.MsgIOServer;
import com.zxwl.frame.utils.Constant;
import com.zxwl.frame.utils.Constants;
import com.zxwl.frame.utils.NotificationUtils;
import com.zxwl.frame.utils.PreferenceUtil;
import com.zxwl.frame.utils.StatusBarUtils;
import com.zxwl.frame.utils.sharedpreferences.PreferencesHelper;

import org.greenrobot.eventbus.EventBus;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.Date;

import static com.huawei.opensdk.commonservice.common.LocContext.getContext;

public class AudioActivity extends BaseLibActivity implements LocBroadcastReceiver, View.OnClickListener {
    private static final String TAG = "AudioActivity";
    /*会控按钮--*/
    private TextView tvHangUp;
    private TextView tvMic;
    private TextView tvMute;
    /*会控按钮--end*/

    private String[] mActions = new String[]{
            CustomBroadcastConstants.ACTION_CALL_END,
            CustomBroadcastConstants.ADD_LOCAL_VIEW,
            CustomBroadcastConstants.DEL_LOCAL_VIEW
    };

    private CallInfo mCallInfo;
    private int mCallID;
    private Object thisVideoActivity = this;

    private CallMgr mCallMgr;
    private CallFunc mCallFunc;

    private MeetingMgr instance;

    private String confId;

    private String peerNumber;

    private boolean isMic = true; //麦克风是否静音
    private boolean isMute = false;//外放是否静音

    private String smcConfId;//smc的会议id，添加会场使用


    //是否保持通话
    private boolean isHold = true;
    //开始的时间
    private long startTimeLong;

    @Override
    protected void findViews() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tvHangUp = (TextView) findViewById(R.id.tv_hang_up);
        tvMic = (TextView) findViewById(R.id.tv_mic);
        tvMute = (TextView) findViewById(R.id.tv_mute);
    }

    @Override
    protected void initData() {
        StatusBarUtils.setTransparent(this);

        Intent intent = getIntent();
//        mCallInfo = (CallInfo) intent.getSerializableExtra(UIConstants.CALL_INFO);

        mCallInfo = PreferencesHelper.getData(UIConstants.CALL_INFO, CallInfo.class);

        peerNumber = mCallInfo.getPeerNumber();

        this.mCallID = mCallInfo.getCallID();

        mCallMgr = CallMgr.getInstance();
        mCallFunc = CallFunc.getInstance();

        if ((CallConstant.TYPE_LOUD_SPEAKER != CallMgr.getInstance().getCurrentAudioRoute())) {
            CallMgr.getInstance().switchAudioRoute();
        }

        //设置扬声器的状态
        setAudioRouteStatus();

        //是否静音
        setMuteStatus();

        instance = MeetingMgr.getInstance();

        confId = PreferencesHelper.getData(Constants.CONF_ID);

        if (!TextUtils.isEmpty(confId)) {
//            queryConfInfo();
        }

        //是否静音
        setMuteStatus();

        //发送notif
        sendNotif(mCallInfo);

        startTimeLong = System.currentTimeMillis();
    }

    @Override
    protected void setListener() {
        tvHangUp.setOnClickListener(this);
        tvMic.setOnClickListener(this);
        tvMute.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_audio;
    }

    /**
     * 更新扬声器状态
     */
    public void switchAudioRoute() {
        int audioRoute = CallMgr.getInstance().switchAudioRoute();
        setAudioRouteStatus(audioRoute);
    }

    public void setAudioRouteStatus() {
        int audioRoute = CallMgr.getInstance().getCurrentAudioRoute();
        setAudioRouteStatus(audioRoute);
    }

    public void setAudioRouteStatus(int audioRoute) {
        boolean isLoudSpeaker = CallConstant.TYPE_LOUD_SPEAKER == audioRoute;
        tvMute.setCompoundDrawablesWithIntrinsicBounds(0, isLoudSpeaker ? R.mipmap.icon_unmute : R.mipmap.icon_mute, 0, 0);
    }

    /**
     * 麦克风静音
     */
    public void muteMic() {
//        boolean currentMuteStatus = mCallFunc.isMuteStatus();
        boolean currentMuteStatus = isMic;
        if (CallMgr.getInstance().muteMic(mCallID, !currentMuteStatus)) {
            mCallFunc.setMuteStatus(!currentMuteStatus);
//            Toast.makeText(this, "麦克风" + (currentMuteStatus ? "当前静音" : "非静音"), Toast.LENGTH_SHORT).show();
            setMuteStatus();
            isMic = !isMic;
        }
    }

    private void setMuteStatus() {
        boolean currentMuteStatus = mCallFunc.isMuteStatus();
        //更新状态静音按钮状态
        tvMic.setCompoundDrawablesWithIntrinsicBounds(0, currentMuteStatus ? R.mipmap.icon_mic_close : R.mipmap.icon_mic, 0, 0);
    }

    @Override
    public void onReceive(String broadcastName, Object obj) {
        switch (broadcastName) {
            case CustomBroadcastConstants.ACTION_CALL_END:
                EventMsg eventMsg = new EventMsg();
                eventMsg.setMsg(EventMsg.HOLDCALL);
                eventMsg.setBody("hide");
                EventBus.getDefault().post(eventMsg);
                //如果是主叫则发送消息
                if (mCallInfo.isCaller()) {
                    long chatTimeLong = System.currentTimeMillis() - startTimeLong;
                    String chatTime = DateUtil.longToString(chatTimeLong, DateUtil.CHAT_TIME);

                    //发送消息
                    sendTextMsg("通话时长 " + chatTime, mCallInfo.getPeerNumber());

                    //保存本地消息
                    saveLocalMessage("通话时长 " + chatTime, mCallInfo.getPeerNumber());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isHold = false;
                        finish();
                    }
                });
                break;

            case CustomBroadcastConstants.ADD_LOCAL_VIEW:
                break;

            case CustomBroadcastConstants.DEL_LOCAL_VIEW:
                break;

            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mCallInfo.isFocus()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    queryConfInfo();
                }
            }, 3 * 1000);

        }
        LocBroadcast.getInstance().registerBroadcast(this, mActions);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "217-->onPause");
        if (isHold) {
            holdCall(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        cancleNotif();

        LocBroadcast.getInstance().unRegisterBroadcast(this, mActions);
        clearHoldHint();
    }

    private void cancleNotif() {
        NotificationUtils.cancel(NotificationUtils.AUDIO_ID);
    }

    private void clearHoldHint() {
        EventMsg eventMsg = new EventMsg();
        eventMsg.setMsg(EventMsg.HOLDCALL);
        eventMsg.setBody("hide");
        EventBus.getDefault().post(eventMsg);
    }

    @Override
    public void onClick(View v) {
        if (R.id.tv_hang_up == v.getId()) {
            clearHoldHint();
            //结束会议
            mCallMgr.endCall(mCallID);
        } else if (R.id.tv_mute == v.getId()) {
            if (mCallInfo.isFocus()) {
                if (isMute) {
                    setSitesQuietRequest(false);
                } else {
                    setSitesQuietRequest(true);
                }
            } else {
                if (isMuteSpeakStatus()) {
                    huaweiOpenSpeaker();
                } else {
                    huaweiCloseSpeaker();
                }
            }
        } else if (R.id.tv_mic == v.getId()) {
            if (mCallInfo.isFocus()) {
                //处于静音
                if (isMic) {
                    setSiteMuteRequest(false);
                } else {
                    setSiteMuteRequest(true);
                }
            } else {
                muteMic();
            }
        }
    }

    /**
     * true代表静音
     *
     * @return
     */
    private boolean isMuteSpeakStatus() {
        if (0 != mCallID) {
            return CallMgr.getInstance().getMuteSpeakStatus(mCallID);
        } else {
            return false;
        }
    }

    private void huaweiCloseSpeaker() {
        if (0 != mCallID) {
            boolean muteSpeak = CallMgr.getInstance().muteSpeak(mCallID, true);
        }
        setSpeakerStatus();
    }

    private void huaweiOpenSpeaker() {
        if (0 != mCallID) {
            CallMgr.getInstance().muteSpeak(mCallID, false);
        }
        setSpeakerStatus();
    }

    /**
     * 设置扬声器的图片
     */
    private void setSpeakerStatus() {
        tvMute.setCompoundDrawablesWithIntrinsicBounds(0, isMuteSpeakStatus() ? R.mipmap.icon_mute : R.mipmap.icon_unmute, 0, 0);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        holdCall(true);
    }

    /**
     * 保持通话不断切换至后台
     */
    private void holdCall(boolean isBack) {
        moveTaskToBack(true);
        if (isBack) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        EventMsg eventMsg = new EventMsg();
        eventMsg.setMsg(EventMsg.HOLDCALL);
        eventMsg.setBody("audio");
        EventBus.getDefault().post(eventMsg);
    }

    /*添加会场*/
    private BottomSheetDialog mBottomSheetDialog;
    private TextView tvAddCancle;
    private TextView tvAddConfirm;

    private RecyclerView rvAddAttendees;
    private AddSiteAdapter addSiteAdapter;
    /*添加会场--end*/

    private int repeatCount;

    /**
     * 查询会议详情
     */
    private void queryConfInfo() {
        RequestParams params = new RequestParams(Urls.BASE_URL + "conf/queryBySmcConfIdOrAccessCode");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        params.addQueryStringParameter("accessCode", peerNumber);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ConfBeanRespone baseData = new GsonBuilder().create().fromJson(result, ConfBeanRespone.class);
                if (null != baseData && 0 == baseData.code) {

                    //smcconfid
                    smcConfId = baseData.data.smcConfId;

                    for (ConfBeanRespone.DataBean.SiteStatusInfoListBean siteBean : baseData.data.siteStatusInfoList) {
                        if (siteBean.siteUri.equals(getCurrentSiteUri())) {
                            tvMic.setCompoundDrawablesWithIntrinsicBounds(0, 1 == siteBean.microphoneStatus ? R.mipmap.icon_mic : R.mipmap.icon_mic_close, 0, 0);
                            tvMute.setCompoundDrawablesWithIntrinsicBounds(0, 1 == siteBean.loudspeakerStatus ? R.mipmap.icon_unmute : R.mipmap.icon_mute, 0, 0);
                            return;
                        }
                    }
                } else {
                    Log.i(TAG, "查询会议详情失,稍后再试");
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.i(TAG, "查询会议详情失败，错误:" + ex.getCause());
                if (repeatCount < 3) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            repeatCount++;
                            queryConfInfo();
                        }
                    }, 10 * 1000);
                }
            }

            @Override
            public void onCancelled(CancelledException cex) {
//                Toast.makeText(x.app(), "cancelled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * 获取当前账号的号码
     *
     * @return
     */
    private String getCurrentSiteUri() {
        return LoginCenter.getInstance().getSipAccountInfo().getTerminal();
    }


    /**
     * m麦克风闭音
     */
    private void setSiteMuteRequest(boolean isMicParam) {
        RequestParams params = new RequestParams(Urls.BASE_URL + "conf/setSiteMute");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        params.addQueryStringParameter("smcConfId", smcConfId);
        params.addQueryStringParameter("siteUri", LoginCenter.getInstance().getSipAccountInfo().getTerminal());
        params.addQueryStringParameter("isMute", String.valueOf(isMicParam));

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ConfBeanRespone baseData = new GsonBuilder().create().fromJson(result, ConfBeanRespone.class);

                if (null != baseData && 0 == baseData.code) {
                    if (isMicParam) {
                        //静音成功
                        tvMic.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.icon_mic_close, 0, 0);
                    } else {
                        //取消静音成功
                        tvMic.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.icon_mic, 0, 0);
                    }
                    isMic = isMicParam;
                } else {
//                    Toast.makeText(AudioActivity.this, baseData.msg, Toast.LENGTH_SHORT).show();
                    ToastUtil.showShortToast(AudioActivity.this, "数据请求中...");
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
//                Toast.makeText(VideoConfActivity.this, "麦克风静音失败，错误:" + ex.getCause(), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "麦克风静音失败，错误:" + ex.getCause());
            }

            @Override
            public void onCancelled(CancelledException cex) {
//                Toast.makeText(x.app(), "cancelled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * 外放闭音
     */
    private void setSitesQuietRequest(boolean isMuteParam) {
        RequestParams params = new RequestParams(Urls.BASE_URL + "conf/setSitesQuiet");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        params.addQueryStringParameter("smcConfId", smcConfId);
        params.addQueryStringParameter("siteUri", LoginCenter.getInstance().getSipAccountInfo().getTerminal());
        params.addQueryStringParameter("isQuiet", String.valueOf(isMuteParam));

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ConfBeanRespone baseData = new GsonBuilder().create().fromJson(result, ConfBeanRespone.class);

                if (null != baseData && 0 == baseData.code) {
                    if (isMuteParam) {
                        //静音成功
                        tvMute.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.icon_mute, 0, 0);
                    } else {
                        //取消静音成功
                        tvMute.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.icon_unmute, 0, 0);
                    }
                    isMute = isMuteParam;
                } else {
//                    Toast.makeText(AudioActivity.this, baseData.msg, Toast.LENGTH_SHORT).show();
                    ToastUtil.showShortToast(AudioActivity.this, "数据请求中...");
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
//                Toast.makeText(VideoConfActivity.this, "外放静音失败，错误:" + ex.getCause(), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "外放静音失败，错误:" + ex.getCause());
            }

            @Override
            public void onCancelled(CancelledException cex) {
//                Toast.makeText(x.app(), "cancelled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFinished() {
            }
        });
    }


    private static Intent intent = new Intent(IntentConstant.AUDIO_ACTIVITY_ACTION);

    /**
     * 发送notif
     */
    private void sendNotif(CallInfo callInfo) {
        NotificationUtils.notify(NotificationUtils.AUDIO_ID, new NotificationUtils.Func1<Void, NotificationCompat.Builder>() {
            @Override
            public Void call(NotificationCompat.Builder param) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(IntentConstant.DEFAULT_CATEGORY);
                //判断是否是会议
                param.setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.ic_launcher))
                        //设置该通知的优先级
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText((callInfo.isVideoCall() ? "视频" : "语音") + "通话中,点击以继续")
                        .setContentIntent(PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                        .setAutoCancel(false);
                return null;
            }
        });
    }

    /**
     * 发送消息
     *
     * @param textMsg
     * @param peerNumber
     */
    private boolean sendTextMsg(String textMsg,
                                String peerNumber) {
        String account = LoginCenter.getInstance().getAccount();

        String disPlayName = PreferenceUtil.getString(this, Constant.DISPLAY_NAME, "");

        MessageReal messageReal = new MessageReal(textMsg,
                MessageReal.TYPE_VOICE_CALL,
                "");

        MessageBody msg = new MessageBody(
                account,
                disPlayName,
                peerNumber,
                peerNumber,
                messageReal,
                MessageBody.TYPE_PERSONAL
        );
        return MsgIOServer.sendMsg(msg);
    }

    /**
     * 保存消息到数据库
     *
     * @param textMsg
     * @param peerNumber
     */
    private void saveLocalMessage(String textMsg, String peerNumber) {
        String disPlayName = PreferenceUtil.getString(this, Constant.DISPLAY_NAME, "");

        ChatBean sendMsg = new ChatBean(
                MultipleItem.SEND_VOICE_CALL,
                disPlayName,
//                Constant.CurrDisPlayName,
                new Date(),
                textMsg);
        sendMsg.sendId = LoginCenter.getInstance().getAccount();
//        sendMsg.sendName = Constant.CurrDisPlayName;
        sendMsg.sendName = disPlayName;
        sendMsg.receiveId = peerNumber;
        sendMsg.receiveName = peerNumber;
        sendMsg.isSend = true;
        sendMsg.conversationId = peerNumber;//会话id
        sendMsg.conversationUserName = peerNumber;//会话id

        ChatItem chatItem = new ChatItem(sendMsg);

        //更新单个消息
        EventMsg eventMsg = new EventMsg();
        eventMsg.setMessageData(sendMsg);
        eventMsg.setMsg(EventMsg.RECEIVE_SINGLE_MESSAGE);
        EventBus.getDefault().post(eventMsg);

        //更新首页消息
        eventMsg = new EventMsg();
        eventMsg.setMessageData(sendMsg);
        eventMsg.setMsg(EventMsg.UPDATE_HOME);
        EventBus.getDefault().post(eventMsg);

    }

}
