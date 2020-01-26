package com.zxwl.frame.inter;

import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.huawei.opensdk.callmgr.CallMgr;
import com.huawei.opensdk.commonservice.common.LocContext;
import com.huawei.opensdk.commonservice.util.LogUtil;
import com.huawei.opensdk.demoservice.BookConferenceInfo;
import com.huawei.opensdk.demoservice.ConfConstant;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.huawei.opensdk.demoservice.Member;
import com.huawei.opensdk.sdkwrapper.login.LoginCenter;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.ecsdk.common.UIConstants;
import com.zxwl.ecsdk.utils.DateUtil;
import com.zxwl.frame.activity.LoadingActivity;
import com.zxwl.frame.activity.LoginDialogActivity;
import com.zxwl.frame.bean.respone.ConfRespone;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.utils.AppManager;
import com.zxwl.frame.utils.Constant;
import com.zxwl.frame.utils.NetworkUtil;
import com.zxwl.frame.utils.sharedpreferences.PreferencesHelper;
import com.zxwl.network.api.ConfApi;
import com.zxwl.network.bean.BaseData_BackServer;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * author：pc-20171125
 * data:2019/03/18 10:12
 * 登录
 */
public class HuaweiCallImp {
    //登陆状态为3代表已登陆
    public static final String LOGIN_STATUS = "3";

    public static final int NO_LOGIN_RESULT = 100;

    private static HuaweiCallImp callImp = new HuaweiCallImp();

    public static HuaweiCallImp getInstance() {
        return callImp;
    }

    /**
     * 点对点呼叫
     *
     * @param siteNumber
     * @return
     */
    public int callSite(String siteNumber, boolean isVideoCall) {
        if (!NetworkUtil.isNetworkAvailable(LocContext.getContext())) {
            ToastUtil.showShortToast(LocContext.getContext(), "请检查您的网络");
            return -1;
        }
        if (Constant.isholdCall) {
            ToastUtil.showLongToast(LocContext.getContext(), "你当前处于通话中，无法加入新的会议");
            return -1;
        }
        //获取登陆状态
        String registerState = "";
        try {
            registerState = PreferencesHelper.getData(UIConstants.REGISTER_RESULT_TEMP);
        } catch (Exception e) {
        }

        //判断登陆状态
        if (!LOGIN_STATUS.equals(registerState)) {
            //没有调用登出接口
            //如果网络连接成功
            if (NetworkUtil.isNetworkAvailable(LocContext.getContext())) {
                LoginDialogActivity.startActivity(LocContext.getContext());
                return NO_LOGIN_RESULT;
            }
        }

        LogUtil.i(UIConstants.DEMO_TAG, "LocContext.getPackageName:" + LocContext.getPackageName());
        return CallMgr.getInstance().startCall(siteNumber, isVideoCall);
    }

    /**
     * 创建会议
     *
     * @param confName
     * @param duration
     * @param memberList
     * @param groupId
     */
    public int createConference(String confName,
                                int duration,
                                List<Member> memberList,
                                String groupId) {
        BookConferenceInfo bookConferenceInfo = new BookConferenceInfo();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Date date = new Date();
        String formatStr = dateFormat.format(date);
        bookConferenceInfo.setStartTime(DateUtil.localTimeUtc(formatStr));
        bookConferenceInfo.setMediaType(ConfConstant.ConfMediaType.VIDEO_CONF);
        bookConferenceInfo.setDuration(duration);
        bookConferenceInfo.setSubject("APP_" + groupId + "_" + confName);
        bookConferenceInfo.setPassWord("123456");

        boolean isNull = false;
        if (null != LoginCenter.getInstance() && null != LoginCenter.getInstance().getSipAccountInfo()) {
            isNull = true;
        }

        if (!isNull) {
            return -1;
        }

        //Join the meeting as chairman
        if (null != LoginCenter.getInstance().getSipAccountInfo().getTerminal()) {
            Member chairman = new Member();
            chairman.setNumber(LoginCenter.getInstance().getSipAccountInfo().getTerminal());
            chairman.setAccountId(LoginCenter.getInstance().getAccount());
            chairman.setRole(ConfConstant.ConfRole.CHAIRMAN);
            //Other fields are optional, and can be filled according to need
            memberList.add(chairman);
        }

        bookConferenceInfo.setMemberList(memberList);

        int result = MeetingMgr.getInstance().bookConference(bookConferenceInfo);

        if (result != 0) {
        } else {
            //是否自己创建的会议
            PreferencesHelper.saveData(UIConstants.IS_CREATE, true);

            //是否需要自动接听
            PreferencesHelper.saveData(UIConstants.IS_AUTO_ANSWER, true);

            //显示等待界面
            LoadingActivity.startActivty(LocContext.getContext(), confName);
        }
        return result;
    }

    /**
     * 创建会议，后台接口
     * @param confName
     * @param duration
     * @param memberList
     * @param groupId
     * @param accessCode
     * @param type
     * @return
     */
    public int createConferenceNetWork(String confName,
                                       String duration,
                                       String memberList,
                                       String groupId,
                                       String accessCode,
                                       int type) {
        if (!NetworkUtil.isNetworkAvailable(LocContext.getContext())) {
            ToastUtil.showShortToast(LocContext.getContext(), "请检查您的网络");
            return -1;
        }

        String state = "";
        try {
            state = PreferencesHelper.getData(UIConstants.REGISTER_RESULT_TEMP);
        } catch (Exception e) {
        }

//        Toast.makeText(LocContext.getContext(), "state:" + state, Toast.LENGTH_SHORT).show();

//        if ("4".equals(state) || "0".equals(state) || "2".equals(state)) {
        if (!"3".equals(state)) {
            //没有调用登出接口
            //如果网络连接成功
            if (NetworkUtil.isNetworkAvailable(LocContext.getContext())) {
                LoginDialogActivity.startActivity(LocContext.getContext());
            }
            return NO_LOGIN_RESULT;
        }

//        StringBuilder sites = new StringBuilder();
//        for (int i = 0; i < memberList.size(); i++) {
//            if (i == memberList.size() - 1) {
//                sites.append(memberList.get(i) + ",");
//            } else {
//                sites.append(memberList.get(i));
//            }
//        }


        Toast.makeText(LocContext.getContext(), "正在召集会议....", Toast.LENGTH_SHORT).show();
        RequestParams params = new RequestParams(
//                Urls.BASE_URL
                "http://192.168.20.105:8080/videoConf/"
                        + "conf/scheduleConf");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        JSONObject js_request = new JSONObject();

        startLoading(confName);
        // 根据实际需求添加相应键值对
        try {
            js_request.put("confName", confName);
            js_request.put("duration", duration);
            if (!TextUtils.isEmpty(accessCode)) {
                js_request.put("accessCode", accessCode);
            }
//            js_request.put("sites", memberList + "," + LoginCenter.getInstance().getSipAccountInfo().getTerminal());
            js_request.put("sites", memberList);
            js_request.put("groupId", groupId);
            js_request.put("creatorUri", LoginCenter.getInstance().getSipAccountInfo().getTerminal());
            //type为召开的会议类型：0.语音会议，1.视频会议
            js_request.put("confMediaType", type);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        params.setAsJsonContent(true);
        params.setBodyContent(js_request.toString());

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ConfRespone confRespone = new GsonBuilder().create().fromJson(result, ConfRespone.class);

                if (null != confRespone && 0 == confRespone.code) {
//                    startLoading(confName);
                } else {
                    AppManager.getInstance().finishActivity(LoadingActivity.class);
//                    Toast.makeText(LocContext.getContext(), "召集会议失败,请稍后再试", Toast.LENGTH_SHORT).show();
                    Toast.makeText(LocContext.getContext(), confRespone.msg, Toast.LENGTH_LONG).show();
//                    LocBroadcast.getInstance().sendBroadcast(CustomBroadcastConstants.LOGIN_FAILED, "会场名称请求失败,请稍后再试");
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                AppManager.getInstance().finishActivity(LoadingActivity.class);
//                Toast.makeText(LocContext.getContext(), "召集会议失败,请稍后再试,错误：" + ex.getCause(), Toast.LENGTH_SHORT).show();
                Toast.makeText(LocContext.getContext(), "召集会议失败,请稍后再试" +
                        "" + ex.getCause(), Toast.LENGTH_SHORT).show();
//                LocBroadcast.getInstance().sendBroadcast(CustomBroadcastConstants.LOGIN_FAILED, "会场名称请求失败:" + ex.getReal());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                //Toast.makeText(x.app(), "cancelled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFinished() {
            }
        });

        return -1;
    }

    private void startLoading(String confName) {
        //是否自己创建的会议
        PreferencesHelper.saveData(UIConstants.IS_CREATE, true);

        //是否需要自动接听
        PreferencesHelper.saveData(UIConstants.IS_AUTO_ANSWER, true);

        //显示等待界面
        LoadingActivity.startActivty(LocContext.getContext(), confName);

    }

    /**
     * 加入会议
     * 0：未注册
     * <p>
     * 1：注册中
     * <p>
     * 2：注销中
     * <p>
     * 3：已注册
     * <p>
     * 4：无效状态
     *
     * @param accessCode
     */
    public int joinConf(String accessCode) {
        if (Constant.isholdCall) {
            ToastUtil.showLongToast(LocContext.getContext(), "你当前处于通话中，无法加入新的会议");
            return -1;
        }
//        DataCleanManager.cleanApplicationData(LocContext.getContext(), "");
//        LogUtil.i(UIConstants.DEMO_TAG, "LocContext.getPackageName:" + LocContext.getPackageName());
        String state = "";
        try {
            state = PreferencesHelper.getData(UIConstants.REGISTER_RESULT_TEMP);
        } catch (Exception e) {
        }

//        Toast.makeText(LocContext.getContext(), "state:" + state, Toast.LENGTH_SHORT).show();

//        if ("4".equals(state) || "0".equals(state) || "2".equals(state)) {
        if (!"3".equals(state)) {
            //没有调用登出接口
            //如果网络连接成功
            if (NetworkUtil.isNetworkAvailable(LocContext.getContext())) {
                LoginDialogActivity.startActivity(LocContext.getContext());
            }
            return -1;
        }

        //加入会议
        PreferencesHelper.saveData(UIConstants.JOIN_CONF, true);
        //屏蔽使用原生方法加入会议
//        return CallMgr.getInstance().startCall(accessCode, true);
        joinConfByNetAPI(accessCode);
        return 0;
    }

    private void joinConfByNetAPI(String accessCode) {
        HttpUtils.getInstance(LocContext.getContext())
                .getRetofitClinet()
                .setBaseUrl(Urls.BASE_URL)
                .builder(ConfApi.class)
                .joinConfNew(accessCode, LoginCenter.getInstance().getAccount())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData_BackServer>() {
                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        AppManager.getInstance().finishActivity(LoadingActivity.class);
                        Toast.makeText(LocContext.getContext(), "加入会议失败,请稍后再试,错误：" + responeThrowable.getCause(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(BaseData_BackServer baseData_backServer) {
                        if (baseData_backServer.getCode() == 0) {
                            //是否需要自动接听
                            PreferencesHelper.saveData(UIConstants.IS_AUTO_ANSWER, true);

                            //显示等待界面
                            LoadingActivity.startActivty(LocContext.getContext(), "加入会议中");
                        } else {
                            Toast.makeText(LocContext.getContext(), baseData_backServer.getMsg(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}

