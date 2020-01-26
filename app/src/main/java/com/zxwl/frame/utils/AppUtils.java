package com.zxwl.frame.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.zxwl.ecsdk.common.UIConstants;

import java.util.List;

/**
 * author：Administrator
 * data:2019/12/17 16:28
 */
public class AppUtils {
    /**
     * 获取设备的IMEI
     *
     * @param context
     * @return
     */
    @SuppressLint("HardwareIds")
    public static String getDeviceIdIMEI(Context context) {
        String id;
        //android.telephony.TelephonyManager
        TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

//        AndPermission.with(context)
//                .runtime()
//                .permission(Manifest.permission.READ_PHONE_STATE)
//                .onGranted(permissions -> {
//                    Toast.makeText(context, "获取成功", Toast.LENGTH_SHORT).show();
//                })
//                .onDenied(permissions -> {
//                    // Storage permission are not allowed.
//                      Toast.makeText(context, "失败", Toast.LENGTH_SHORT).show();
//                })
//                .start();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        if (mTelephony.getDeviceId() != null) {
            id = mTelephony.getDeviceId();
        } else {
            //android.provider.Settings;
            id = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return id;
    }

    /**
     * 判断是否在应用是否在进程
     *
     * @param context
     * @param frontPkg
     * @return
     */
    public static boolean isFrontProcess(Context context, String frontPkg) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> infos = null;
        if (manager != null) {
            infos = manager.getRunningAppProcesses();
        }
        if (infos == null || infos.isEmpty()) {
            return false;
        }

        final int pid = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            Log.i(UIConstants.DEMO_TAG, "processName-->" + info.processName);
//            if (info.pid == pid) {
////                Log.i(UIConstants.DEMO_TAG, "processName-->" + info.processName);
//                return frontPkg.equals(info.processName);
//            }
            return "com.zxwl.imdemo:local".equals(info.processName);
        }
        return false;
    }
}
