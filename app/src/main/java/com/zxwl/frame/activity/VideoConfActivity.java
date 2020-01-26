package com.zxwl.frame.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.GsonBuilder;
import com.huawei.opensdk.callmgr.CallConstant;
import com.huawei.opensdk.callmgr.CallInfo;
import com.huawei.opensdk.callmgr.CallMgr;
import com.huawei.opensdk.callmgr.VideoMgr;
import com.huawei.opensdk.commonservice.localbroadcast.CustomBroadcastConstants;
import com.huawei.opensdk.commonservice.localbroadcast.LocBroadcast;
import com.huawei.opensdk.commonservice.localbroadcast.LocBroadcastReceiver;
import com.huawei.opensdk.commonservice.util.LogUtil;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.huawei.opensdk.demoservice.Member;
import com.huawei.opensdk.sdkwrapper.login.LoginCenter;
import com.huawei.videoengine.ViERenderer;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.ecsdk.common.UIConstants;
import com.zxwl.ecsdk.logic.CallFunc;
import com.zxwl.ecsdk.utils.IntentConstant;
import com.zxwl.frame.R;
import com.zxwl.frame.adapter.AddSiteAdapter;
import com.zxwl.frame.adapter.ConfControlAdapter;
import com.zxwl.frame.adapter.onConfControlClickListener;
import com.zxwl.frame.adapter.onRecyclerClick;
import com.zxwl.frame.bean.EventMsg;
import com.zxwl.frame.bean.respone.ConfBeanRespone;
import com.zxwl.frame.bean.respone.GroupUser;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.service.AudioStateWatchService;
import com.zxwl.frame.utils.AppManager;
import com.zxwl.frame.utils.DensityUtil;
import com.zxwl.frame.utils.DragFrameLayout;
import com.zxwl.frame.utils.NotificationUtils;
import com.zxwl.frame.utils.StatusBarUtils;
import com.zxwl.frame.utils.sharedpreferences.PreferencesHelper;
import com.zxwl.network.api.ConfApi;
import com.zxwl.network.bean.BaseData;
import com.zxwl.network.bean.PeopleBean;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;

import org.greenrobot.eventbus.EventBus;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import me.jessyan.autosize.internal.CancelAdapt;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.huawei.opensdk.commonservice.common.LocContext.getContext;

/**
 * 视频会议界面
 */
public class VideoConfActivity extends BaseLibActivity implements LocBroadcastReceiver, View.OnClickListener, CancelAdapt {
    private TextView tvTopTitle;
    private ImageView ivRightOperate;
    /*顶部按钮--end*/

    /*视频界面--*/
    private FrameLayout mRemoteView;
    private DragFrameLayout mLocalView;
    private FrameLayout mHideView;
    /*视频界面--end*/

    /*会控按钮--*/
    private TextView tvHangUp;
    private TextView tvMic;
    private TextView tvMute;
    /*会控按钮--end*/

    private static final int ADD_LOCAL_VIEW = 101;

    private String[] mActions = new String[]{
            CustomBroadcastConstants.ACTION_CALL_END,
            CustomBroadcastConstants.ADD_LOCAL_VIEW,
            CustomBroadcastConstants.DEL_LOCAL_VIEW
    };

    private CallInfo mCallInfo;
    private int mCallID;
    private Object thisVideoActivity = this;

    private int mCameraIndex = CallConstant.FRONT_CAMERA;

    private CallMgr mCallMgr;
    private CallFunc mCallFunc;
    private MeetingMgr instance;

    /*会控顶部*/
    private ImageView ivBg;
    private RelativeLayout llTopControl;
    private LinearLayout llBottomControl;
    /*会控顶部-end*/

    private ImageView ivSwitchCamera;
    private ImageView ivOneKeyCloseMic;
    private ImageView ivSwitchConfMode;

    private boolean showControl = true;//是否显示控制栏

    private String peerNumber;//会议接入号

    private String confID;//会议id
    private int callID;//会议id
    private String subject;//会议主题
    private String smcConfId;//smc的会议id，添加会场使用
    private String TAG = VideoConfActivity.class.getSimpleName();

    //会议是否是自己创建的
    private boolean isCreate = false;

    //麦克风是否静音
    private boolean isMic = true;
    //外放是否静音
    private boolean isMute = false;

    //是否是主席
    private boolean isChair = false;

    //是否保持通话
    private boolean isHold = true;

    //会议模式：0表示主席模式,选看模式，1表示多画面模式
    //（1）主席模式,选看模式：所有参会会场都观看主席会场，在此模式下，任意会场可以选看任意会场
    //（2）多画面模式：所有参会会场都观看多画面，在此模式下，选看功能无效
    //多画面模式，显示广播按钮
    //选看模式，显示选看按钮
    private int confMode = -1;

    @Override
    protected void findViews() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tvTopTitle = (TextView) findViewById(R.id.tv_top_title);
        ivRightOperate = (ImageView) findViewById(R.id.iv_right_operate);
        mRemoteView = (FrameLayout) findViewById(R.id.conf_share_layout);
        mLocalView = (DragFrameLayout) findViewById(R.id.conf_video_small_logo);
        mHideView = (FrameLayout) findViewById(R.id.hide_video_view);
        tvHangUp = (TextView) findViewById(R.id.tv_hang_up);
        tvMic = (TextView) findViewById(R.id.tv_mic);
        tvMute = (TextView) findViewById(R.id.tv_mute);

        llTopControl = (RelativeLayout) findViewById(R.id.ll_top_control);
        llBottomControl = (LinearLayout) findViewById(R.id.ll_bottom_control);
        ivBg = (ImageView) findViewById(R.id.iv_bg);

        ivSwitchCamera = (ImageView) findViewById(R.id.iv_back_operate);

        ivOneKeyCloseMic = (ImageView) findViewById(R.id.iv_one_key_close_mic);
        ivSwitchConfMode = (ImageView) findViewById(R.id.iv_switch_conf_mode);
    }

    @Override
    protected void initData() {
        //结束掉等待的对话框
        AppManager.getInstance().finishActivity(LoadingActivity.class);

        if (Build.VERSION.SDK_INT < 28) {
            StatusBarUtils.setTransparent(this);
        }

        ivRightOperate.setVisibility(View.VISIBLE);
        //可能需要删除的添加图标
        ivRightOperate.setImageResource(R.mipmap.icon_add);

        ivSwitchCamera.setVisibility(View.GONE);

        Intent intent = getIntent();

        try {
            mCallInfo = PreferencesHelper.getData(UIConstants.CALL_INFO, CallInfo.class);
            isCreate = PreferencesHelper.getData(UIConstants.IS_CREATE, Boolean.class);
        } catch (Exception e) {}

        confID = intent.getStringExtra(UIConstants.CONF_ID);
        callID = intent.getIntExtra(UIConstants.CALL_ID, -1);
        peerNumber = intent.getStringExtra(UIConstants.PEER_NUMBER);

        this.mCallID = mCallInfo.getCallID();

        mCallMgr = CallMgr.getInstance();
        mCallFunc = CallFunc.getInstance();
        instance = MeetingMgr.getInstance();

        //请求主席
        if (isCreate) {
            ivOneKeyCloseMic.setVisibility(View.GONE);
            setRightOperateShow(View.VISIBLE);
            isChair = true;
        }

        sendNotif(mCallInfo);
    }

    private long last = 0;

    @Override
    protected void setListener() {
        tvHangUp.setOnClickListener(this);
        tvMic.setOnClickListener(this);
        tvMute.setOnClickListener(this);

        ivRightOperate.setOnClickListener(this);
        ivBg.setOnClickListener(this);
        ivSwitchCamera.setOnClickListener(this);
        ivOneKeyCloseMic.setOnClickListener(this);
        ivRightOperate.setOnClickListener(this);
        ivSwitchConfMode.setOnClickListener(this);

        mLocalView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        last = System.currentTimeMillis();
                        break;

                    case MotionEvent.ACTION_UP:
                        long s = System.currentTimeMillis() - last;
                        if (s < 100) {
                            changeShowView();
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_conf_new;
    }

    @Override
    protected void onResume() {
        super.onResume();

        showControl = llBottomControl.getVisibility() == View.VISIBLE && llTopControl.getVisibility() == View.VISIBLE;

        LocBroadcast.getInstance().registerBroadcast(this, mActions);
        addSurfaceView(false);

        if (!TextUtils.isEmpty(peerNumber)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //查询会议详情
                    queryConfInfo();
                }
            }, 3 * 1000);
        }

        //是否开启画面自动旋转
        setAutoRotation(this, true, "148");

        //如果不是扬声器则切换成扬声器
        setLoudSpeaker();

        //设置扬声器的状态
//        setSpeakerStatus();
        AudioStateWatchService.suitCurrAudioDevice();

        setLocalView();

        //循环查询当前的状态
        createQueryConfInfoRetryRequest();

//        if (isMic) {
//            tvMic.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.icon_mic_close, 0, 0);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //如果是指定主席則不保持通話
        if (!isAppointChair) {
            if (isHold) {
                holdCall(false);
            }
        }

        //取消查询当前会场的状态
        unQueryConfInfoSubscribe();
    }

    private void setLocalView() {
        int virtualBarHeigh = 0;
        if (Build.VERSION.SDK_INT < 28) {
            virtualBarHeigh = DensityUtil.getNavigationBarHeight(this) + DensityUtil.dip2px(20);
        } else {
            virtualBarHeigh = DensityUtil.dip2px(20);
        }
        //显示
        mLocalView.animate().translationX(0 - virtualBarHeigh).setDuration(100).start();
    }

    @Override
    protected void onDestroy() {
        cancleNotif();

        try {
            PreferencesHelper.saveData(UIConstants.IS_CREATE, false);
        } catch (Exception e) {
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callClosed();
            }
        });
        super.onDestroy();

        LocBroadcast.getInstance().unRegisterBroadcast(this, mActions);
        mHandler.removeCallbacksAndMessages(null);
        setAutoRotation(this, false, "163");

        PreferencesHelper.saveData(UIConstants.IS_AUTO_ANSWER, false);

        MeetingMgr.getInstance().setCurrentConferenceCallID(0);

        reSetRenderer();

        //清理缓存
//        DataCleanManager.cleanApplicationData(LocContext.getContext(), "");
        clearHoldHint();
    }


    /**
     * 设置为扬声器
     */
    public void setLoudSpeaker() {
        //获取扬声器状态
        //如果不是扬声器则切换成扬声器
        if ((CallConstant.TYPE_LOUD_SPEAKER != CallMgr.getInstance().getCurrentAudioRoute())) {
            CallMgr.getInstance().switchAudioRoute();
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ADD_LOCAL_VIEW:
                    addSurfaceView(true);
                    setAutoRotation(thisVideoActivity, true, "184");
                    break;

                default:
                    break;
            }
        }
    };

    /**
     * 更新状态
     */
    public void muteMic() {
        boolean currentMuteStatus = mCallFunc.isMuteStatus();
        if (CallMgr.getInstance().muteMic(mCallID, !currentMuteStatus)) {
            mCallFunc.setMuteStatus(!currentMuteStatus);
            setMicStatus();
        }
    }

    private void setMicStatus() {
        boolean currentMuteStatus = mCallFunc.isMuteStatus();
        //更新状态静音按钮状态
        tvMic.setCompoundDrawablesWithIntrinsicBounds(0, currentMuteStatus ? R.mipmap.icon_mic_close : R.mipmap.icon_mic, 0, 0);
    }

    /**
     * 设置扬声器的图片
     */
    private void setSpeakerStatus() {
        tvMute.setCompoundDrawablesWithIntrinsicBounds(0, isMuteSpeakStatus() ? R.mipmap.icon_mute : R.mipmap.icon_unmute, 0, 0);
    }

    public void videoToAudio() {
        CallMgr.getInstance().delVideo(mCallID);
    }

    public void holdVideo() {
        CallMgr.getInstance().holdVideoCall(mCallID);
    }

    public void videoDestroy() {
        if (null != CallMgr.getInstance().getVideoDevice()) {
            LogUtil.i(UIConstants.DEMO_TAG, "onCallClosed destroy.");
            CallMgr.getInstance().videoDestroy();

            //从会话列表中移除一路会话
            CallMgr.getInstance().removeCallSessionFromMap(callID);
        }
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        try {
            int callID = getCallID();
            mCameraIndex = VideoMgr.getInstance().getCurrentCameraIndex() == CallConstant.FRONT_CAMERA ? CallConstant.BACK_CAMERA : CallConstant.FRONT_CAMERA;
            CallMgr.getInstance().switchCamera(callID, mCameraIndex);
        } catch (Exception e) {

        }
    }

    public void switchCameraStatus(boolean isCameraClose) {
        if (isCameraClose) {
            CallMgr.getInstance().closeCamera(mCallID);
        } else {
            CallMgr.getInstance().openCamera(mCallID);
        }
    }

    public SurfaceView getHideVideoView() {
        return VideoMgr.getInstance().getLocalHideView();
    }

    public SurfaceView getLocalVideoView() {
        return VideoMgr.getInstance().getLocalVideoView();
    }

    public SurfaceView getRemoteVideoView() {
        return VideoMgr.getInstance().getRemoteVideoView();
    }

    public void setAutoRotation(Object object, boolean isOpen, String line) {
//        LogUtil.i(UIConstants.DEMO_TAG, "setAutoRotation-->" + line);
        VideoMgr.getInstance().setAutoRotation(object, isOpen, 1);
//        VideoMgr.getInstance().setAutoRotation(object, false, 1);
    }

    private void addSurfaceView(ViewGroup container, SurfaceView child) {
        if (child == null) {
            return;
        }
        if (child.getParent() != null) {
            ViewGroup vGroup = (ViewGroup) child.getParent();
            vGroup.removeAllViews();
        }
        container.addView(child);
    }

    private void addSurfaceView(boolean onlyLocal) {
        if (!onlyLocal) {
            addSurfaceView(mRemoteView, getRemoteVideoView());
        }
        addSurfaceView(mLocalView, getLocalVideoView());
        addSurfaceView(mHideView, getHideVideoView());
    }

    /**
     * On call closed.
     */
    private void callClosed() {
        LogUtil.i(UIConstants.DEMO_TAG, "onCallClosed enter.");
//        executorShutDown();
        videoDestroy();
    }

    @Override
    public void onReceive(String broadcastName, Object obj) {
        switch (broadcastName) {
            case CustomBroadcastConstants.ACTION_CALL_END:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callClosed();
                        finishActivity("470");
                    }
                });
                clearHoldHint();
                break;

            case CustomBroadcastConstants.ADD_LOCAL_VIEW:
                mHandler.sendEmptyMessage(ADD_LOCAL_VIEW);
                break;

            case CustomBroadcastConstants.DEL_LOCAL_VIEW:
                break;

            default:
                break;
        }
    }

    /**
     * 移除
     */
    private void clearHoldHint() {
        EventMsg eventMsg = new EventMsg();
        eventMsg.setMsg(EventMsg.HOLDCALL);
        eventMsg.setBody("hide");
        EventBus.getDefault().post(eventMsg);
    }

    /**
     * 离开会议
     */
    private void leaveConf(int line) {
        boolean isLeaveResult = false;
        int callID = getCallID();
        if (callID != 0) {
            isLeaveResult = CallMgr.getInstance().endCall(callID);
        }

        int result = MeetingMgr.getInstance().leaveConf();
        if (result != 0) {
            return;
        }
    }

    public void finishActivity(String lineNumber) {
        isHold = false;
        EventMsg eventMsg = new EventMsg();
        eventMsg.setMsg(EventMsg.HOLDCALL);
        eventMsg.setBody("hide");
        EventBus.getDefault().post(eventMsg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
    }

    private void reSetRenderer() {
        Log.e("hme-video", "清空渲染器");
        //清空渲染器信息
        ViERenderer viERenderer = new ViERenderer();
        try {
            Field g_localRendererField = viERenderer.getClass().getDeclaredField("g_localRenderer");
            g_localRendererField.setAccessible(true);
            g_localRendererField.set(viERenderer, null);

            Field g_localRenderField = viERenderer.getClass().getDeclaredField("g_localRender");
            g_localRenderField.setAccessible(true);
            g_localRenderField.set(viERenderer, null);

            Field renderSysLockField = viERenderer.getClass().getDeclaredField("renderSysLock");
            renderSysLockField.setAccessible(true);
            renderSysLockField.set(viERenderer, new ReentrantLock());


            Field g_remoteRenderField = viERenderer.getClass().getDeclaredField("g_remoteRender");
            g_remoteRenderField.setAccessible(true);
            g_remoteRenderField.set(viERenderer, new SurfaceView[16]);

            Field listenThreadField = viERenderer.getClass().getDeclaredField("listenThread");
            listenThreadField.setAccessible(true);
            listenThreadField.set(viERenderer, null);

            g_localRendererField = null;
            g_localRenderField = null;
            renderSysLockField = null;
            g_remoteRenderField = null;
            listenThreadField = null;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            viERenderer = null;
        }
    }

    @Override
    public void onClick(View v) {
        if (R.id.tv_hang_up == v.getId()) {
            //结束会议
            //是主席则弹出对话框
            if (isChair) {
                showExitConfDialog();
            } else {
                leaveConf(0);
            }
        } else if (R.id.tv_mute == v.getId()) {
            //是否静音喇叭
            //true代表静音
            if (TextUtils.isEmpty(smcConfId)) {
                queryConfInfo();
            } else {
                if (isMute) {
                    setSitesQuietRequest(false);
                } else {
                    setSitesQuietRequest(true);
                }
            }
        } else if (R.id.tv_mic == v.getId()) {
            //静音
            if (TextUtils.isEmpty(smcConfId)) {
                queryConfInfo();
            } else {
                //处于静音
                if (isMic) {
                    setSiteMuteRequest(false);
                } else {
                    setSiteMuteRequest(true);
                }
            }
        } else if (R.id.iv_bg == v.getId()) {
            if (showControl) {
                hideControl();
            } else {
                showControl();
            }
        } else if (R.id.iv_back_operate == v.getId()) {
            switchCamera();
        } else if (R.id.iv_right_operate == v.getId()) {
            ToastUtil.showShortToast(VideoConfActivity.this, "正在启动会议控制...");
            queryConfInfo4ConfControl(true);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (confControlDialog != null && confControlDialog.isShowing()) {
                        queryConfInfo4ConfControl(false);
                    }
                }
            }, 0, 5000);
        } else if (R.id.conf_video_small_logo == v.getId()) {
            changeShowView();
        }//一键关闭除自己的麦克风
        else if (R.id.iv_one_key_close_mic == v.getId()) {
            StringBuilder builder = new StringBuilder();
            if (existList.size() > 0) {
                for (ConfBeanRespone.DataBean.SiteStatusInfoListBean statusInfoListBean : existList) {
                    if (!statusInfoListBean.siteUri.equals(getCurrentSiteUri())) {
                        builder.append(statusInfoListBean.siteUri).append(",");
                    }
                }
            }
            //一键关闭所有麦克风
//            oneKeyCloseMic(!isAllMicClose, builder.toString());
            oneKeyCloseMic(isAllMicClose, builder.toString());
        }//切换模式
        else if (R.id.iv_switch_conf_mode == v.getId()) {
            switchConfMode();
        }
    }

    //是否处于一键静音状态
    private boolean isAllMicClose = true;

    /**
     * m麦克风闭音
     */
    private void oneKeyCloseMic(boolean isMicParam, String siteUri) {
        RequestParams params = new RequestParams(Urls.BASE_URL + "conf/setSiteMute");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        params.addQueryStringParameter("smcConfId", smcConfId);
        params.addQueryStringParameter("siteUri", siteUri);
        params.addQueryStringParameter("isMute", String.valueOf(isMicParam));

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ConfBeanRespone baseData = new GsonBuilder().create().fromJson(result, ConfBeanRespone.class);
                if (null != baseData && 0 == baseData.code) {
//                    isAllMicClose = isMicParam;
//                    ivOneKeyCloseMic.setImageResource(isMicParam ? R.mipmap.ic_close_all_mic : R.mipmap.ic_open_all_mic);
                    ToastUtil.showShortToast(getApplicationContext(), isMicParam ? "静音成功" : "取消静音成功");
                } else {
                    if (baseData.msg.contains("不能为null")) {
                        Toast.makeText(VideoConfActivity.this, "请求中...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(VideoConfActivity.this, baseData.msg, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                setRightOperateShow(View.VISIBLE);
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
     * 离开会议的网络请求
     */
    private void leaveConfRequest() {
        RequestParams params = new RequestParams(Urls.BASE_URL + "conf/disconnectSite");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        params.addQueryStringParameter("smcConfId", smcConfId);
        params.addQueryStringParameter("siteUri", LoginCenter.getInstance().getSipAccountInfo().getTerminal());

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ConfBeanRespone baseData = new GsonBuilder().create().fromJson(result, ConfBeanRespone.class);

                if (null != baseData && 0 == baseData.code) {
                    PreferencesHelper.saveData(UIConstants.IS_CREATE, false);
                } else {
                    Toast.makeText(VideoConfActivity.this, "离开会议失败,请稍后再试！", Toast.LENGTH_SHORT).show();
//                    Toast.makeText(VideoConfActivity.this, baseData.msg, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "离开会议失败,稍后再试");
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                setRightOperateShow(View.VISIBLE);
                Toast.makeText(VideoConfActivity.this, "离开会议失败,请稍后再试", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "离开会议失败，错误:" + ex.getCause());
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
                    Toast.makeText(VideoConfActivity.this, baseData.msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                setRightOperateShow(View.VISIBLE);
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
                    Toast.makeText(VideoConfActivity.this, baseData.msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                setRightOperateShow(View.VISIBLE);
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

    /**
     * 切换会议模式
     */
    private void switchConfMode() {
        int mode = 0 == confMode ? 1 : 0;
        RequestParams params = new RequestParams(Urls.BASE_URL + "conf/changeConfMode");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        params.addQueryStringParameter("smcConfId", smcConfId);
        params.addQueryStringParameter("confMode", String.valueOf(mode));

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ConfBeanRespone baseData = new GsonBuilder().create().fromJson(result, ConfBeanRespone.class);
                if (null != baseData && 0 == baseData.code) {
                    ToastUtil.showLongToast(getApplicationContext(), mode == 0 ? "设置选看模式成功" : "设置主席模式成功");
                } else {
                    Toast.makeText(VideoConfActivity.this, baseData.msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                setRightOperateShow(View.VISIBLE);
//                Toast.makeText(VideoConfActivity.this, "外放静音失败，错误:" + ex.getCause(), Toast.LENGTH_SHORT).show();
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
     * true代表静音
     *
     * @return
     */
    private boolean isMuteSpeakStatus() {
        int currentConferenceCallID = getCallID();
        if (0 != currentConferenceCallID) {
            return CallMgr.getInstance().getMuteSpeakStatus(currentConferenceCallID);
        } else {
            return false;
        }
    }

    private int getCallID() {
        if (-1 != callID) {
            return callID;
        } else {
            return MeetingMgr.getInstance().getCurrentConferenceCallID();
        }
    }

    private void showControl() {
        llTopControl.setVisibility(View.VISIBLE);
        getViewAlphaAnimator(llTopControl, 1).start();
        llBottomControl.setVisibility(View.VISIBLE);
        getViewAlphaAnimator(llBottomControl, 1).start();
    }

    private void hideControl() {
        getViewAlphaAnimator(llBottomControl, 0).start();
        getViewAlphaAnimator(llTopControl, 0).start();
    }

    private ViewPropertyAnimator getViewAlphaAnimator(View view, float alpha) {
        ViewPropertyAnimator viewPropertyAnimator = view.animate().alpha(alpha).setDuration(300);
        viewPropertyAnimator.setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(alpha > 0 ? View.VISIBLE : View.INVISIBLE);
                showControl = alpha > 0 ? true : false;
            }
        });
        return viewPropertyAnimator;
    }

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

    private List<ConfBeanRespone.DataBean.SiteStatusInfoListBean> existList = new ArrayList<>();

    //选看会场临时解决方案
    private boolean isFristSet = true;

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

                    //是否是会议主席
                    boolean isConfChair = !TextUtils.isEmpty(baseData.data.chairUri) && baseData.data.chairUri.equals(LoginCenter.getInstance().getAccount());

                    //会议中已存在的人员
                    existList.clear();
                    existList.addAll(baseData.data.siteStatusInfoList);

                    //smcconfid
                    smcConfId = baseData.data.smcConfId;

                    //会议名称
                    subject = baseData.data.confName;

                    //会议模式
                    confMode = baseData.data.confMode;

                    //如果是主席模式
                    if (confMode == 0) {
                        if (TextUtils.isEmpty(baseData.data.chairUri)) {
                            setWatchSite(baseData.data.creatorUri, false);
                        } else {
                            setWatchSite(baseData.data.chairUri, false);
                        }
                    }

                    repeatCount = 0;

//                    if (isFristSet) {
//                        isFristSet = false;
//                        if (baseData.data.chairUri != null) {
//                            setWatchSite(baseData.data.chairUri, false);
//                            watchUri = baseData.data.chairUri;
//                        } else if (baseData.data.creatorUri != null) {
//                            setWatchSite(baseData.data.creatorUri, false);
//                            watchUri = baseData.data.creatorUri;
//                        }
//                    }

                    if (isCreate || isConfChair) {
                        setSiteMuteRequest(false);
                    }

                    for (ConfBeanRespone.DataBean.SiteStatusInfoListBean siteBean : baseData.data.siteStatusInfoList) {
                        if (siteBean.siteUri.equals(getCurrentSiteUri())) {
                            tvMic.setCompoundDrawablesWithIntrinsicBounds(0, 1 == siteBean.microphoneStatus ? R.mipmap.icon_mic : R.mipmap.icon_mic_close, 0, 0);
                            tvMute.setCompoundDrawablesWithIntrinsicBounds(0, 1 == siteBean.loudspeakerStatus ? R.mipmap.icon_unmute : R.mipmap.icon_mute, 0, 0);
                            return;
                        }
                    }

                } else {
                    setRightOperateShow(View.VISIBLE);

                    Log.i(TAG, "查询会议详情失,稍后再试");
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                setRightOperateShow(View.VISIBLE);
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
     * 查询会议详情
     */
    private void queryConfInfo4ConfControl(boolean isDisPlay) {
        RequestParams params = new RequestParams(Urls.BASE_URL + "conf/queryBySmcConfIdOrAccessCode");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        params.addQueryStringParameter("accessCode", peerNumber);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ConfBeanRespone baseData = new GsonBuilder().create().fromJson(result, ConfBeanRespone.class);
                if (null != baseData && 0 == baseData.code) {
                    //会议中已存在的人员
                    existList.clear();

                    //只显示在线的会场
                    existList.addAll(getOnlineSites(baseData.data.siteStatusInfoList));

                    //smcconfid
                    smcConfId = baseData.data.smcConfId;
                    //会议名称
                    subject = baseData.data.confName;

                    //请求主席
                    if (isCreate) {
                        setRightOperateShow(View.VISIBLE);
                    }

                    repeatCount = 0;

                    showConfControlDialog(existList, isDisPlay);
                } else {
                    setRightOperateShow(View.VISIBLE);

                    Log.i(TAG, "查询会议详情失,稍后再试");
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                setRightOperateShow(View.VISIBLE);
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
     * 获取在线的会场
     *
     * @param siteStatusInfoList
     */
    private List<ConfBeanRespone.DataBean.SiteStatusInfoListBean> getOnlineSites(List<ConfBeanRespone.DataBean.SiteStatusInfoListBean> siteStatusInfoList) {
        List<ConfBeanRespone.DataBean.SiteStatusInfoListBean> siteList = new ArrayList<>();
        for (ConfBeanRespone.DataBean.SiteStatusInfoListBean siteBean : siteStatusInfoList) {
            if (2 == siteBean.siteStatus) {
                siteList.add(siteBean);
            }
        }
        return siteList;
    }

    //错误重新请求次数
    private int repeatCount = 0;

    private void setRightOperateShow(int show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isCreate) {
                    ivRightOperate.setVisibility(show);
                }
            }
        });
    }

    private int changeNumber;

    private void changeShowView() {
        mRemoteView.removeAllViews();
        mLocalView.removeAllViews();
        changeNumber++;
        if (changeNumber % 2 == 0) {
            VideoMgr.getInstance().getRemoteVideoView().setZOrderMediaOverlay(false);
            VideoMgr.getInstance().getLocalVideoView().setZOrderMediaOverlay(true);

            addSurfaceView(mRemoteView, VideoMgr.getInstance().getRemoteVideoView());
            addSurfaceView(mLocalView, VideoMgr.getInstance().getLocalVideoView());
        } else {
            VideoMgr.getInstance().getRemoteVideoView().setZOrderMediaOverlay(true);
            VideoMgr.getInstance().getLocalVideoView().setZOrderMediaOverlay(false);

            addSurfaceView(mLocalView, VideoMgr.getInstance().getRemoteVideoView());
            addSurfaceView(mRemoteView, VideoMgr.getInstance().getLocalVideoView());
        }
    }

    /************************添加会场-start*****************************/
    private BottomSheetDialog addAddtendDialog;
    private TextView tvAddCancle;
    private TextView tvAddConfirm;

    private RecyclerView rvAddAttendees;
    private AddSiteAdapter addSiteAdapter;
    /*添加会场--end*/

    /**
     * 显示添加对话框
     *
     * @param data
     */
    private void showAddAddtendDialog(List<GroupUser.DataBean> data) {
        List<GroupUser.DataBean> selectUser = new ArrayList<>();

        WindowManager wm = this.getWindowManager();
        int height = wm.getDefaultDisplay().getHeight();

        //构造函数的第二个参数可以设置BottomSheetDialog的主题样式
        //mBottomSheetDialog = new BottomSheetDialog(this,R.style.MyBottomDialog);
        addAddtendDialog = new BottomSheetDialog(this);
        //导入底部reycler布局
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_site, null, false);

        rvAddAttendees = view.findViewById(R.id.rv_add_attendees);

        addSiteAdapter = new AddSiteAdapter(this, data);
        addSiteAdapter.setRecyclerClick(new onRecyclerClick() {
            @Override
            public void onClick(int position) {
                GroupUser.DataBean userBean = addSiteAdapter.getDatas().get(position);
                //判断是否被选中
                userBean.isCheck = !userBean.isCheck;
                if (userBean.isCheck) {
                    selectUser.add(userBean);
                } else {
                    selectUser.remove(userBean);
                }

                addSiteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDelete(int position) {

            }
        });

        rvAddAttendees.setLayoutManager(new LinearLayoutManager(this));
        rvAddAttendees.setAdapter(addSiteAdapter);

        tvAddCancle = view.findViewById(R.id.tv_add_cancle);
        tvAddConfirm = view.findViewById(R.id.tv_add_confirm);

        View speaceHolder = view.findViewById(R.id.view_speaceHolder);

        //配置点击外部区域消失
        speaceHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAddtendDialog.dismiss();
            }
        });

        tvAddCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAddtendDialog.dismiss();
            }
        });

        tvAddConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectUser.size() <= 0) {
                    Toast.makeText(VideoConfActivity.this, "请选择需要添加的会场", Toast.LENGTH_SHORT).show();
                    return;
                }

                Member member = null;
                StringBuilder siteUri = new StringBuilder();

                for (int i = 0; i < selectUser.size(); i++) {
                    if (i == selectUser.size() - 1) {
                        siteUri.append(selectUser.get(i).sip);
                    } else {
                        siteUri.append(selectUser.get(i).sip + ",");
                    }
                }
                //添加会场
                addSiteToConf(siteUri.toString());
                addAddtendDialog.dismiss();
            }
        });

        addAddtendDialog.setContentView(view);

        try {
            // hack bg color of the BottomSheetDialog
            ViewGroup parent = (ViewGroup) view.getParent();
            parent.setBackgroundColor(ContextCompat.getColor(this, R.color.tran));
//            ViewGroup.LayoutParams layoutParams = parent.getLayoutParams();
//            layoutParams.width = ((int) (0.5 * DensityUtil.getScreenWidth(this)));
//            parent.setLayoutParams(layoutParams);
        } catch (Exception e) {
            e.printStackTrace();
        }

        BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) view.getParent());
        //设置默认弹出高度为屏幕的0.4倍
//        mBehavior.setPeekHeight((int) (0.4 * height));
        mBehavior.setPeekHeight((int) (height));

        //设置点击dialog外部不消失
        addAddtendDialog.setCanceledOnTouchOutside(false);
        addAddtendDialog.setCancelable(false);

        if (!addAddtendDialog.isShowing()) {
            addAddtendDialog.show();
        } else {
            addAddtendDialog.dismiss();
        }
    }

    /**
     * 添加会场
     */
    private void addSiteToConf(String siteUri) {
        RequestParams params = new RequestParams(Urls.BASE_URL + "conf/addSiteToConf");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        params.addQueryStringParameter("smcConfId", smcConfId);
        params.addQueryStringParameter("siteUri", siteUri);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ConfBeanRespone baseData = new GsonBuilder().create().fromJson(result, ConfBeanRespone.class);

                if (null != baseData && 0 == baseData.code) {
                    Toast.makeText(VideoConfActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                    //添加成功则刷新列表
                    queryConfInfo();
                } else {
                    Toast.makeText(VideoConfActivity.this, "添加失败", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {

            }
        });
    }
    /************************添加会场-end*****************************/

    /************************会控对话框-start*****************************/
    /*会控界面-start*/
    private BottomSheetDialog confControlDialog;
    private TextView tvControlCancel;
    private TextView tvControlConfirm;
    private Space speaceHolder;

    private RecyclerView rvControl;
    private ConfControlAdapter confControlAdapter;

    //最后控制的会场
    private int controlPosition = -1;

    //正在查看的会场
    private String watchUri = "";

    /*会控界面--end*/

    /**
     * 显示会控的对话框
     */
    private void showConfControlDialog(List<ConfBeanRespone.DataBean.SiteStatusInfoListBean> siteList, boolean isDisPlay) {
        if(null!=confControlAdapter){
            confControlAdapter.setConfMode(confMode);
        }
        if (isDisPlay) {
            WindowManager wm = this.getWindowManager();
            int height = wm.getDefaultDisplay().getHeight();

            if (null == confControlDialog) {
                //构造函数的第二个参数可以设置BottomSheetDialog的主题样式
                confControlDialog = new BottomSheetDialog(this);
                //导入底部reycler布局
                View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_site, null, false);

                rvControl = view.findViewById(R.id.rv_add_attendees);
                confControlAdapter = new ConfControlAdapter(this, siteList);
                //会控操作
                confControlAdapter.setRecyclerClick(new onConfControlClickListener() {
                    @Override
                    public void onClick(int position) {
                    }

                    @Override
                    public void onHangUp(int position) {
                        controlPosition = position;
                        ConfBeanRespone.DataBean.SiteStatusInfoListBean site = confControlAdapter.getDatas().get(position);
                        if (site.siteStatus == 2) {
                            setSiteDisconnectRequest(site.siteUri);
                        } else {
                            setSiteCallRequest(site.siteUri);
                        }
                    }

                    @Override
                    public void onMic(int position) {
                        controlPosition = position;
                        ConfBeanRespone.DataBean.SiteStatusInfoListBean site = confControlAdapter.getDatas().get(position);

                        if (site.siteStatus == 2) {
//                        setSiteMuteRequest(site.microphoneStatus == 1, site.siteUri, true);
                            setSiteMuteRequest(true, site.siteUri, true);
                        }
                    }

                    @Override
                    public void onBroadcast(int position) {
                        controlPosition = position;
                        ConfBeanRespone.DataBean.SiteStatusInfoListBean site = confControlAdapter.getDatas().get(position);
                        setSiteBroadcastRequest(!(site.broadcastStatus == 1), site.siteUri);
                    }

                    @Override
                    public void onLouder(int position) {
                        controlPosition = position;
                        ConfBeanRespone.DataBean.SiteStatusInfoListBean site = confControlAdapter.getDatas().get(position);
//                    setSiteCallRequest(site.siteUri);

                        if (site.siteStatus == 2) {
                            setOtherSitesQuietRequest(site.loudspeakerStatus == 1, site.siteUri);
                        }
                    }

                    @Override
                    public void onWatchSite(int position) {
                        controlPosition = position;
                        ConfBeanRespone.DataBean.SiteStatusInfoListBean site = confControlAdapter.getDatas().get(position);
                        setWatchSite(site.siteUri, true);
                    }
                });
                rvControl.setLayoutManager(new LinearLayoutManager(this));
                rvControl.setAdapter(confControlAdapter);

                tvControlCancel = view.findViewById(R.id.tv_add_cancle);
                tvControlConfirm = view.findViewById(R.id.tv_add_confirm);
                TextView tvLable = view.findViewById(R.id.tv_lable);
                tvLable.setText("与会列表");

                View speaceHolder = view.findViewById(R.id.view_speaceHolder);

                //配置点击外部区域消失
                speaceHolder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confControlDialog.dismiss();
                    }
                });

                tvControlCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confControlDialog.dismiss();
                    }
                });
                tvControlConfirm.setText("添加会场");

                tvControlConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confControlDialog.dismiss();
                        HttpUtils.getInstance(VideoConfActivity.this)
                                .getRetofitClinet()
                                .setBaseUrl(Urls.logicServerURL)
                                .builder(ConfApi.class)
                                .getAllConstacts()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new RxSubscriber<BaseData<PeopleBean>>() {
                                    @Override
                                    public void onSuccess(BaseData<PeopleBean> baseData) {
                                        List<PeopleBean> list = baseData.data;
                                        List<GroupUser.DataBean> data = new ArrayList<>();
                                        for (PeopleBean temp : list) {
                                            data.add(new GroupUser.DataBean(temp.id, temp.name, temp.sip, 0, false));
                                        }
                                        showAddAddtendDialog(data);
                                    }

                                    @Override
                                    protected void onError(ResponeThrowable responeThrowable) {
                                        ToastUtil.showLongToast(VideoConfActivity.this, "数据加载失败");
                                    }
                                });
                    }
                });
                confControlDialog.setContentView(view);
                try {
                    ViewGroup parent = (ViewGroup) view.getParent();
                    parent.setBackgroundColor(ContextCompat.getColor(this, R.color.tran));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) view.getParent());
                //设置默认弹出高度为屏幕的0.4倍
                //mBehavior.setPeekHeight((int) (0.4 * height));
                mBehavior.setPeekHeight((int) (height));

                //设置点击dialog外部不消失
                confControlDialog.setCanceledOnTouchOutside(false);
                confControlDialog.setCancelable(false);
            } else {
                confControlAdapter.replceData(siteList);
            }

            confControlAdapter.setChair(isChair);

            //设置添添加人员按钮是否显示
            tvControlConfirm.setVisibility(isChair ? View.VISIBLE : View.INVISIBLE);

            if (!confControlDialog.isShowing()) {
                confControlDialog.show();
            } else {
                confControlDialog.dismiss();
            }
        } else {
            confControlAdapter.replceData(siteList);
        }

        List<ConfBeanRespone.DataBean.SiteStatusInfoListBean> data = confControlAdapter.getDatas();
        for (ConfBeanRespone.DataBean.SiteStatusInfoListBean temp : data) {
            if (temp.siteUri.equals(watchUri)) {
                temp.isWatch = true;
            } else {
                temp.isWatch = false;
            }
        }
//      confControlAdapter.getDatas().get(controlPosition).isWatch = true;
        confControlAdapter.notifyDataSetChanged();
    }
    /************************会控对话框-end*****************************/

    /**
     * 麦克风闭音
     *
     * @param isMicParam true：静音
     * @param siteUri    静音的会场号码
     * @param isList     true:通过会控对话框控制
     */
    private void setSiteMuteRequest(boolean isMicParam, String siteUri, boolean isList) {
        RequestParams params = new RequestParams(Urls.BASE_URL + "conf/setSiteMute");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        params.addQueryStringParameter("smcConfId", smcConfId);
        params.addQueryStringParameter("siteUri", siteUri);
        params.addQueryStringParameter("isMute", String.valueOf(isMicParam));

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ConfBeanRespone baseData = new GsonBuilder().create().fromJson(result, ConfBeanRespone.class);

                if (null != baseData && 0 == baseData.code) {
                    //如果是通过列表控制的则刷新列表
                    if (isList) {

                        //如果是通过列表控制的则刷新列表
                        confControlAdapter.getDatas().get(controlPosition).microphoneStatus = isMicParam ? 0 : 1;
                        confControlAdapter.notifyDataSetChanged();
                    }
                    if (getCurrentSiteUri().equals(siteUri)) {
                        if (isMicParam) {
                            //静音成功
                            tvMic.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.icon_mic_close, 0, 0);
                        } else {
                            //取消静音成功
                            tvMic.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.icon_mic, 0, 0);
                        }
                        isMic = isMicParam;
                    }
                } else {
                    Toast.makeText(VideoConfActivity.this, baseData.msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                setRightOperateShow(View.VISIBLE);
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
    private void setOtherSitesQuietRequest(boolean isMuteParam, String siteUri) {
        RequestParams params = new RequestParams(Urls.BASE_URL + "conf/setSitesQuiet");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        params.addQueryStringParameter("smcConfId", smcConfId);
        params.addQueryStringParameter("siteUri", siteUri);
        params.addQueryStringParameter("isQuiet", String.valueOf(isMuteParam));

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ConfBeanRespone baseData = new GsonBuilder().create().fromJson(result, ConfBeanRespone.class);


                if (null != baseData && 0 == baseData.code) {
                    confControlAdapter.getDatas().get(controlPosition).loudspeakerStatus = isMuteParam ? 0 : 1;
                    confControlAdapter.notifyDataSetChanged();
                    if (getCurrentSiteUri().equals(siteUri)) {
                        if (isMuteParam) {
                            //静音成功
                            tvMute.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.icon_mute, 0, 0);
                        } else {
                            //取消静音成功
                            tvMute.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.icon_unmute, 0, 0);
                        }
                        isMute = isMuteParam;
                    }
                } else {
                    Toast.makeText(VideoConfActivity.this, baseData.msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                setRightOperateShow(View.VISIBLE);
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

    /**
     * 广播会场
     *
     * @param isBroadcast true：广播
     * @param siteUri     广播的会场号码
     *                    confAction_setBroadcastSite.action
     */
    private void setSiteBroadcastRequest(boolean isBroadcast, String siteUri) {
        RequestParams params = new RequestParams(Urls.BASE_URL + "conf/setBroadcastSite");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        params.addQueryStringParameter("smcConfId", smcConfId);
        params.addQueryStringParameter("siteUri", siteUri);
        params.addQueryStringParameter("isBroadcast", String.valueOf(isBroadcast));

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ConfBeanRespone baseData = new GsonBuilder().create().fromJson(result, ConfBeanRespone.class);

                if (null != baseData && 0 == baseData.code) {
                    for (int i = 0; i < confControlAdapter.getItemCount(); i++) {
                        confControlAdapter.getDatas().get(i).broadcastStatus = 0;
                    }
                    Toast.makeText(VideoConfActivity.this, isBroadcast ? "广播会场成功" : "取消广播会场成功", Toast.LENGTH_SHORT).show();

                    //如果是通过列表控制的则刷新列表
                    confControlAdapter.getDatas().get(controlPosition).broadcastStatus = isBroadcast ? 1 : 0;
                    confControlAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(VideoConfActivity.this, baseData.msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                setRightOperateShow(View.VISIBLE);
                Log.i(TAG, "麦克风静音失败，错误:" + ex.getCause());
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * 呼叫会场
     *
     * @param siteUri 呼叫的会场号码
     *                connectSite
     */
    private void setSiteCallRequest(String siteUri) {
        RequestParams params = new RequestParams(Urls.BASE_URL + "conf/connectSite");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        params.addQueryStringParameter("smcConfId", smcConfId);
        params.addQueryStringParameter("siteUri", siteUri);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ConfBeanRespone baseData = new GsonBuilder().create().fromJson(result, ConfBeanRespone.class);

                if (null != baseData && 0 == baseData.code) {
                    Toast.makeText(VideoConfActivity.this, "呼叫会场成功", Toast.LENGTH_SHORT).show();

                    //如果是通过列表控制的则刷新列表
                    confControlAdapter.notifyItemChanged(controlPosition);
                } else {
                    Toast.makeText(VideoConfActivity.this, baseData.msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                setRightOperateShow(View.VISIBLE);
//                Toast.makeText(VideoConfActivity.this, "麦克风静音失败，错误:" + ex.getCause(), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "麦克风静音失败，错误:" + ex.getCause());
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * 挂断
     *
     * @param siteUri 呼叫的会场号码
     *                connectSite
     */
    private void setSiteDisconnectRequest(String siteUri) {
        RequestParams params = new RequestParams(Urls.BASE_URL + "conf/disconnectSite");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        params.addQueryStringParameter("smcConfId", smcConfId);
        params.addQueryStringParameter("siteUri", siteUri);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ConfBeanRespone baseData = new GsonBuilder().create().fromJson(result, ConfBeanRespone.class);

                if (null != baseData && 0 == baseData.code) {
                    //如果是通过列表控制的则刷新列表
                    confControlAdapter.getDatas().get(controlPosition).siteStatus = 3;
                    Toast.makeText(VideoConfActivity.this, "挂断会场成功", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                setRightOperateShow(View.VISIBLE);
//                Toast.makeText(VideoConfActivity.this, "麦克风静音失败，错误:" + ex.getCause(), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "麦克风静音失败，错误:" + ex.getCause());
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * 选看会场
     *
     * @param siteUri 选看会场
     *                connectSite
     */
    private void setWatchSite(String siteUri, boolean showToast) {
        if (0 != confMode) {
            return;
        }
        RequestParams params = new RequestParams(Urls.BASE_URL + "conf/setVideoSource");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        params.addQueryStringParameter("smcConfId", smcConfId);
        params.addQueryStringParameter("siteUri", LoginCenter.getInstance().getAccount());
        params.addQueryStringParameter("videoSourceUri", siteUri);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ConfBeanRespone baseData = new GsonBuilder().create().fromJson(result, ConfBeanRespone.class);

                if (null != baseData && 0 == baseData.code) {
                    //如果是通过列表控制的则刷新列表
                    //TODO:选看会场刷新
//                    confControlAdapter.getDatas().get(controlPosition).siteStatus = 3;
//                    confControlAdapter.notifyItemChanged(controlPosition);
                    if (showToast) {
                        ToastUtil.showShortToast(VideoConfActivity.this, "选看会场成功");
                    }

                    //临时解决方案
                    //清空所有标记
                    List<ConfBeanRespone.DataBean.SiteStatusInfoListBean> data = confControlAdapter.getDatas();
                    for (ConfBeanRespone.DataBean.SiteStatusInfoListBean temp : data) {
                        if (temp.siteUri.equals(siteUri)) {
                            temp.isWatch = true;
                        } else {
                            temp.isWatch = false;
                        }
                    }
                    watchUri = siteUri;
                    confControlAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(VideoConfActivity.this, baseData.msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                setRightOperateShow(View.VISIBLE);
//                Toast.makeText(VideoConfActivity.this, "麦克风静音失败，错误:" + ex.getCause(), Toast.LENGTH_SHORT).show();
                Log.i(TAG, "麦克风静音失败，错误:" + ex.getCause());
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * 结束会议
     */
    private void endConfReuqest() {
        RequestParams params = new RequestParams(Urls.BASE_URL + "conf/stopConf");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        params.addQueryStringParameter("smcConfId", smcConfId);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ConfBeanRespone baseData = new GsonBuilder().create().fromJson(result, ConfBeanRespone.class);
                if (null != baseData && 0 == baseData.code) {
                    ToastUtil.showLongToast(getApplicationContext(), "结束会议成功");
                } else {
                    setRightOperateShow(View.VISIBLE);

                    Log.i(TAG, "结束会议失败,稍后再试");
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                setRightOperateShow(View.VISIBLE);
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

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
//        holdCall(true);
    }

    /**
     * 保持通话不断切换至后台
     */
    private void holdCall(boolean isBack) {
//        moveTaskToBack(true);
//        if (isBack) {
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
//        }
//        EventMsg eventMsg = new EventMsg();
//        eventMsg.setMsg(EventMsg.HOLDCALL);
//        eventMsg.setBody("videoConf");
//        EventBus.getDefault().post(eventMsg);
    }

    /**
     * 获取当前账号的号码
     *
     * @return
     */
    private String getCurrentSiteUri() {
        return LoginCenter.getInstance().getSipAccountInfo().getTerminal();
    }

    private MaterialDialog exitConfDialog;

    //是否指定主席
    private boolean isAppointChair = false;

    /**
     * 显示结束会议的对话框
     */
    private void showExitConfDialog() {
        if (null == exitConfDialog) {
            View view = View.inflate(this, R.layout.dialog_exit_conf, null);
            RelativeLayout rlContent = (RelativeLayout) view.findViewById(R.id.rl_content);
            ImageView ivClose = (ImageView) view.findViewById(R.id.ic_close);
            TextView tvEndConf = (TextView) view.findViewById(R.id.tv_end_conf);
            TextView tvLeaveConf = (TextView) view.findViewById(R.id.tv_leave_conf);
            TextView tvAppointChair = (TextView) view.findViewById(R.id.tv_appoint_chair);

            ivClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exitConfDialog.dismiss();
                }
            });

            tvEndConf.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unQueryConfInfoSubscribe();
                    endConfReuqest();
                }
            });

            tvLeaveConf.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    leaveConf(1832);
                    leaveConfRequest();
                }
            });

            tvAppointChair.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appointChair();
                }
            });

            exitConfDialog = new MaterialDialog.Builder(this)
                    .customView(view, false)
                    .build();

            Window window = exitConfDialog.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);  //全屏的flag
            window.setBackgroundDrawableResource(android.R.color.transparent); //设置window背景透明
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.alpha = 0.8f;
            lp.dimAmount = 0.1f; //dimAmount在0.0f和1.0f之间，0.0f完全不暗，1.0f全暗
            window.setAttributes(lp);
        }
        exitConfDialog.show();
    }

    private int REQUEST_CODE = 0;

    /**
     * 指定主席
     */
    private void appointChair() {
        isAppointChair = true;
        //跳转到指定主席界面
        Intent intent = new Intent(this, ChairSelectActivity.class);
        intent.putExtra("peerNumber", peerNumber);
        intent.putExtra("smcConfId", smcConfId);
        startActivityForResult(intent, REQUEST_CODE);
        exitConfDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) {
            if (REQUEST_CODE == requestCode) {
                ToastUtil.showShortToast(getApplicationContext(), data.getStringExtra("name"));
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //设置指定主席的标记为false
//        isAppointChair = false;

    }

    private Subscription queryConfInfoSubscribe;

    private void createQueryConfInfoRetryRequest() {
        unQueryConfInfoSubscribe();

        queryConfInfoSubscribe = Observable.interval(3, TimeUnit.SECONDS)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        intervalQueryConfInfo();
                    }
                });
    }

    private void unQueryConfInfoSubscribe() {
        if (null != queryConfInfoSubscribe) {
            if (!queryConfInfoSubscribe.isUnsubscribed()) {
                queryConfInfoSubscribe.unsubscribe();
                queryConfInfoSubscribe = null;
            }
        }
    }

    /**
     * 循环查询状态
     */
    private void intervalQueryConfInfo() {
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
                    //是否是会议主席
                    boolean isConfChair = !TextUtils.isEmpty(baseData.data.chairUri) && baseData.data.chairUri.equals(LoginCenter.getInstance().getAccount());

                    //会议模式
                    confMode = baseData.data.confMode;

                    //判断是否是主席
                    if (isConfChair || isCreate) {
                        //等于主席
                        isChair = true;
                        ivSwitchConfMode.setVisibility(View.VISIBLE);
                        //0表示主席模式，1表示多画面模式
                        ivSwitchConfMode.setImageResource(0 == confMode ? R.mipmap.ic_discuss_close : R.mipmap.ic_discuss_open);
                    } else {
                        isChair = false;
                        ivSwitchConfMode.setVisibility(View.GONE);
                    }

                    //如果是主席显示一键关闭麦克风按钮
//                    ivOneKeyCloseMic.setVisibility(isChair ? View.VISIBLE : View.GONE);

                    //判断是否是本人
                    for (ConfBeanRespone.DataBean.SiteStatusInfoListBean siteBean : baseData.data.siteStatusInfoList) {
                        if (siteBean.siteUri.equals(getCurrentSiteUri())) {
                            tvMic.setCompoundDrawablesWithIntrinsicBounds(0, 1 == siteBean.microphoneStatus ? R.mipmap.icon_mic : R.mipmap.icon_mic_close, 0, 0);
                            tvMute.setCompoundDrawablesWithIntrinsicBounds(0, 1 == siteBean.loudspeakerStatus ? R.mipmap.icon_unmute : R.mipmap.icon_mute, 0, 0);
                            //得到正在观看的画面
                            watchUri = siteBean.videoSourceUri;
                            break;
                        }
                    }

                    for (ConfBeanRespone.DataBean.SiteStatusInfoListBean temp : baseData.data.siteStatusInfoList) {
                        if (temp.microphoneStatus == 1 && !temp.siteUri.equals(LoginCenter.getInstance().getAccount())) {
                            ivOneKeyCloseMic.setImageResource(R.mipmap.ic_open_all_mic);
                            return;
                        }
                    }
                    ivOneKeyCloseMic.setImageResource(R.mipmap.ic_close_all_mic);
                } else {
//                    Toast.makeText(VideoConfActivity.this, baseData.msg, Toast.LENGTH_SHORT).show();

                    Log.i(TAG, "查询会议详情失,稍后再试");
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                //setRightOperateShow(View.VISIBLE);
                Log.i(TAG, "查询会议详情失败，错误:" + ex.getCause());
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

    private static Intent intent = new Intent(IntentConstant.VIDEO_CONF_ACTIVITY_ACTION);

    /**
     * 发送notif
     */
    private void sendNotif(CallInfo callInfo) {
        NotificationUtils.notify(NotificationUtils.VIDEO_ID, new NotificationUtils.Func1<Void, NotificationCompat.Builder>() {
            @Override
            public Void call(NotificationCompat.Builder param) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(IntentConstant.DEFAULT_CATEGORY);
//                intent.putExtra(UIConstants.CONF_ID, confID);
//                intent.putExtra(UIConstants.CALL_ID, callInfo.getCallID());
//                intent.putExtra(UIConstants.PEER_NUMBER, callInfo.getPeerNumber());

                //判断是否是会议
                param.setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.ic_launcher))
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText((callInfo.isVideoCall() ? "视频" : "语音") + "通话中,点击以继续")
                        .setContentIntent(PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                        //设置该通知的优先级
                        .setPriority(Notification.PRIORITY_HIGH)
                        //让通知右滑是否能能取消通知,默认是false
                        .setOngoing(true)
                        .setAutoCancel(false);
                return null;
            }
        });
    }

    private void cancleNotif() {
        NotificationUtils.cancel(NotificationUtils.VIDEO_ID);
    }

    /**
     * 设置会议模式
     *
     * @param confMode confMode（会议模式：0表示主席模式，1表示多画面模式）
     *                 <p>
     *                 （1）主席模式：所有参会会场都观看主席会场，在此模式下，任意会场可以选看任意会场
     *                 （2）多画面模式：所有参会会场都观看多画面，在此模式下，选看功能无效
     */
    private void setConfMode(String confMode) {
        RequestParams params = new RequestParams(Urls.BASE_URL + "conf/stopConf");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        params.addQueryStringParameter("smcConfId", smcConfId);
        params.addQueryStringParameter("confMode", confMode);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
//                ConfBeanRespone baseData = new GsonBuilder().create().fromJson(result, ConfBeanRespone.class);
//                if (null != baseData && 0 == baseData.code) {
//                    ToastUtil.showLongToast(getApplicationContext(), "结束会议成功");
//                } else {
//                    setRightOperateShow(View.VISIBLE);
//
//                    Log.i(TAG, "结束会议失败,稍后再试");
//                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
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


}

