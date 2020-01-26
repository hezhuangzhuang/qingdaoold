package com.zxwl.frame.inter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huawei.opensdk.commonservice.common.LocContext;
import com.huawei.opensdk.commonservice.localbroadcast.CustomBroadcastConstants;
import com.huawei.opensdk.commonservice.localbroadcast.LocBroadcast;
import com.huawei.opensdk.commonservice.util.LogUtil;
import com.huawei.opensdk.loginmgr.LoginConstant;
import com.huawei.opensdk.loginmgr.LoginMgr;
import com.huawei.opensdk.loginmgr.LoginParam;
import com.huawei.utils.DeviceManager;
import com.zxwl.ecsdk.common.UIConstants;
import com.zxwl.ecsdk.utils.FileUtil;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.service.AudioStateWatchService;
import com.zxwl.frame.utils.sharedpreferences.PreferencesHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;

/**
 * author：pc-20171125
 * data:2019/1/3 11:11
 * 登录
 */
public class HuaweiLoginImp {
    private static HuaweiLoginImp loginImp = new HuaweiLoginImp();
    private String TAG = HuaweiLoginImp.class.getSimpleName();

    private Gson gson = new Gson();

    public static HuaweiLoginImp getInstance() {
        return loginImp;
    }

    /**
     * 登录
     *
     * @param activity
     * @param userName
     * @param password
     * @param smcRegisterServer
     * @param smcRegisterPort
     * @param otherServer
     * @param otherPort
     * @param guohangServer
     * @param guohangPort
     * @param isDisNetword      是否是断网重连
     */
    public void querySiteUri(Context activity,
                             String userName,
                             String password,
                             String smcRegisterServer,
                             String smcRegisterPort,
                             String otherServer,
                             String otherPort,
                             String guohangServer,
                             String guohangPort,
                             boolean isDisNetword) {

        if (TextUtils.isEmpty(otherServer)) {
            Toast.makeText(activity, "服务器地址不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        activity.startService(new Intent(activity, AudioStateWatchService.class));

        //如果是断网重连
        if (isDisNetword) {
            Urls.BASE_URL = otherServer;
        } else {
            if (TextUtils.isEmpty(otherPort)) {
                Urls.BASE_URL = "http://" + otherServer + "/";
            } else {
                Urls.BASE_URL = "http://" + otherServer + ":" + otherPort + "/";
            }
        }

        if (TextUtils.isEmpty(guohangServer)) {
            Toast.makeText(activity, "服务器地址不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isDisNetword) {
            Urls.GUOHANG_BASE_URL = guohangServer;
        } else {
            if (TextUtils.isEmpty(guohangPort)) {
                Urls.GUOHANG_BASE_URL = "http://" + guohangServer + "/";
            } else {
                Urls.GUOHANG_BASE_URL = "http://" + guohangServer + ":" + guohangPort + "/";
            }
        }

        loginRequest(
                activity,
                userName,
                password,
                smcRegisterServer,
                smcRegisterPort);
    }

    public void querySiteUri(Activity activity,
                             String userName,
                             String password,
                             String smcRegisterServer,
                             String smcRegisterPort,
                             String otherServer,
                             String otherPort,
                             String guohangServer,
                             String guohangPort) {
        querySiteUri(activity, userName, password, smcRegisterServer, smcRegisterPort, otherServer, otherPort, guohangServer, guohangPort, false);
    }

    public void loginRequest(Context activity,
                             String userName,
                             String password,
                             String smcRegisterServer,
                             String smcRegisterPort) {
        if (!DeviceManager.isNetworkAvailable(activity)) {
            return;
        }

        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)) {
            return;
        }

        String regServerAddress = smcRegisterServer;
        String serverPort = smcRegisterPort;

        if (TextUtils.isEmpty(regServerAddress)) {
            return;
        }

        if (TextUtils.isEmpty(serverPort)) {
            return;
        }

        if (null == Looper.myLooper()) {
            Looper.prepare();
        }

        LoginParam loginParam = new LoginParam();

        loginParam.setServerPort(Integer.parseInt(serverPort));
        loginParam.setProxyPort(Integer.parseInt(serverPort));
        loginParam.setServerUrl(regServerAddress);
        loginParam.setProxyUrl(regServerAddress);
        loginParam.setUserName(userName);
        loginParam.setPassword(password);

        loginParam.setVPN(false);

        loginParam.setSrtpMode(0);
        int mode = "5061".equals(serverPort) ? 1 : 0;

        //TLS:5061
        //UDP:5060
        //TCP:5060
        loginParam.setSipTransportMode(mode);//UDP:0,TLS:1,TCP:2
        loginParam.setServerType(2);

        final int login = LoginMgr.getInstance().login(loginParam);

        importFile(activity);
    }
    /*华为登录相关end*/

    /**
     * 登出
     */
    public void logOut() {
        PreferencesHelper.saveData(UIConstants.IS_LOGOUT, true);
        String state = "";
        try {
            state = PreferencesHelper.getData(UIConstants.REGISTER_RESULT_TEMP);
        } catch (Exception e) {
        }
        if (!"3".equals(state)) {
            //没有调用登出接口
            //如果网络连接成功
            LocBroadcast.getInstance().sendBroadcast(CustomBroadcastConstants.LOGOUT, null);
        } else {
            LoginMgr.getInstance().logout();
        }
        LocContext.getContext().stopService(new Intent(LocContext.getContext(), AudioStateWatchService.class));
    }

    /**
     * import file.
     *
     * @param activity
     */
    private static void importFile(Context activity) {
        LogUtil.i(UIConstants.DEMO_TAG, "import media file!~");
        Executors.newFixedThreadPool(LoginConstant.FIXED_NUMBER).execute(new Runnable() {
            @Override
            public void run() {
                importMediaFile(activity);
                importBmpFile(activity);
                importAnnotFile(activity);
            }
        });
    }

    private static void importBmpFile(Context activity) {
        if (FileUtil.isSdCardExist()) {
            try {
                String bmpPath = Environment.getExternalStorageDirectory() + File.separator + Urls.BMP_FILE;
                InputStream bmpInputStream = activity.getAssets().open(Urls.BMP_FILE);
                FileUtil.copyFile(bmpInputStream, bmpPath);
            } catch (IOException e) {
                LogUtil.e(UIConstants.DEMO_TAG, e.getMessage());
            }
        }
    }

    private static void importAnnotFile(Context activity) {
        if (FileUtil.isSdCardExist()) {
            try {
                String bmpPath = Environment.getExternalStorageDirectory() + File.separator + Urls.ANNOT_FILE;
                File file = new File(bmpPath);
                if (!file.exists()) {
                    file.mkdir();
                }

                String[] bmpNames = new String[]{"check.bmp", "xcheck.bmp", "lpointer.bmp",
                        "rpointer.bmp", "upointer.bmp", "dpointer.bmp", "lp.bmp"};
                String[] paths = new String[bmpNames.length];

                for (int list = 0; list < paths.length; ++list) {
                    paths[list] = bmpPath + File.separator + bmpNames[list];
                    InputStream bmpInputStream = activity.getAssets().open(bmpNames[list]);
                    FileUtil.copyFile(bmpInputStream, paths[list]);
                }

            } catch (IOException e) {
                LogUtil.e(UIConstants.DEMO_TAG, e.getMessage());
            }
        }
    }

    private static void importMediaFile(Context activity) {
        if (FileUtil.isSdCardExist()) {
            try {
                String mediaPath = Environment.getExternalStorageDirectory() + File.separator + Urls.RINGING_FILE;
                InputStream mediaInputStream = activity.getAssets().open(Urls.RINGING_FILE);
                FileUtil.copyFile(mediaInputStream, mediaPath);

                String ringBackPath = Environment.getExternalStorageDirectory() + File.separator + Urls.RING_BACK_FILE;
                InputStream ringBackInputStream = activity.getAssets().open(Urls.RING_BACK_FILE);
                FileUtil.copyFile(ringBackInputStream, ringBackPath);
            } catch (IOException e) {
                LogUtil.e(UIConstants.DEMO_TAG, e.getMessage());
            }
        }
    }

}
