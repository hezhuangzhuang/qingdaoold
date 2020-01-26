package com.zxwl.frame;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.android.arouter.launcher.ARouter;
import com.huawei.opensdk.callmgr.CallMgr;
import com.huawei.opensdk.commonservice.common.LocContext;
import com.huawei.opensdk.commonservice.util.CrashUtil;
import com.huawei.opensdk.commonservice.util.LogUtil;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.huawei.opensdk.loginmgr.LoginMgr;
import com.huawei.opensdk.servicemgr.ServiceMgr;
import com.huawei.utils.ZipUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreator;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.scwang.smartrefresh.layout.api.DefaultRefreshInitializer;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.tencent.bugly.crashreport.CrashReport;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.zxwl.ecsdk.common.UIConstants;
import com.zxwl.ecsdk.logic.CallFunc;
import com.zxwl.ecsdk.logic.ConfFunc;
import com.zxwl.ecsdk.logic.LoginFunc;
import com.zxwl.ecsdk.utils.FileUtil;
import com.zxwl.frame.activity.MainActivity;
import com.zxwl.frame.inter.HuaweiLoginImp;
import com.zxwl.frame.push.PushConstant;
import com.zxwl.frame.service.MsgIOServer;
import com.zxwl.frame.utils.Constant;
import com.zxwl.frame.utils.DynamicTimeFormat;
import com.zxwl.frame.utils.LogUtils;
import com.zxwl.frame.utils.PreferenceUtil;
import com.zxwl.frame.utils.sharedpreferences.PreferencesHelper;
import com.zxwl.network.ApiUrls;
import com.zxwl.network.config.NetWorkConfiguration;
import com.zxwl.network.http.HttpUtils;
import com.zxy.recovery.core.Recovery;

import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executors;

import me.jessyan.autosize.AutoSizeConfig;


/**
 * author：hw
 * data:2017/4/14 17:17
 */
public class App extends BaseApp {
    static {
        //启用矢量图兼容
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        //设置全局默认配置（优先级最低，会被其他设置覆盖）
        SmartRefreshLayout.setDefaultRefreshInitializer(new DefaultRefreshInitializer() {
            @Override
            public void initialize(@NonNull Context context, @NonNull RefreshLayout layout) {
                //全局设置（优先级最低）
                layout.setEnableAutoLoadMore(true);
                layout.setEnableOverScrollDrag(false);
                layout.setEnableOverScrollBounce(true);
                layout.setEnableLoadMoreWhenContentNotFull(true);
                layout.setEnableScrollContentWhenRefreshed(true);
            }
        });

        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @NonNull
            @Override
            public RefreshHeader createRefreshHeader(@NonNull Context context, @NonNull RefreshLayout layout) {
                //全局设置主题颜色（优先级第二低，可以覆盖 DefaultRefreshInitializer 的配置，与下面的ClassicsHeader绑定）
                return new ClassicsHeader(context).setSpinnerStyle(SpinnerStyle.Translate).setTimeFormat(new DynamicTimeFormat("更新于 %s")).setAccentColor(ContextCompat.getColor(context, R.color.color_999));
            }
        });

        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @NonNull
            @Override
            public RefreshFooter createRefreshFooter(@NonNull Context context, @NonNull RefreshLayout layout) {
                return new ClassicsFooter(context).setSpinnerStyle(SpinnerStyle.Translate);
            }
        });
    }

    private static App mInstance;

    public static App getInstance() {
        return mInstance;
    }

    public static File appDir;

    public static File getFile() {
        return appDir;
    }

    private static final int EXPECTED_FILE_LENGTH = 7;

    //包名需要与应用名一致
    private static final String FRONT_PKG = "com.zxwl.imdemo";

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        // 在主进程初始化调用哈
        if (BuildConfig.DEBUG) {
//            BlockCanary.install(this, new BlockCanaryContext()).start();
        }

        //初始化缓存文件
        File cache = getCacheDir();
        appDir = new File(cache, "ACache");

        //设置网络请求的基本参数
        NetWorkConfiguration configuration = new NetWorkConfiguration(this)
                .baseUrl(ApiUrls.BASE_URL);
        HttpUtils.setConFiguration(configuration);

        //内存泄漏检测
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app include_common_operation this process.
//            return;
//        }
//        LeakCanary.install(this);

        //腾讯bug线上搜集工具
        CrashReport.initCrashReport(getApplicationContext(), "25c10e0723", false);

        initLog();

        //屏幕适配
        configUnits();

        ARouter.openDebug();
        ARouter.init(this);

        MultiDex.install(this);

        PreferencesHelper.init(this);

        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG); // 开启debug会影响性能

        if (initHuawei()) return;

        //bug本地提示
        initcrashHandler();

        //小米推送
        if (sHandler == null) {
            sHandler = new DemoHandler(getApplicationContext());
        }

        //注册小米推送
        MiPushClient.registerPush(this, PushConstant.XIAOMI_APP_ID, PushConstant.XIAOMI_APP_KEY);
    }

    private void initLog() {
        LogUtils.getConfig()
                //保存到文件中
                .setLog2FileSwitch(true);
    }

    private boolean initHuawei() {
        com.huawei.application.BaseApp.setApp(this);
        
        //判断进程是否还在
        if (!isFrontProcess(this, BuildConfig.APPLICATION_ID)) {
            LocContext.init(this);
            CrashUtil.getInstance().init(this);
            Log.i("SDKDemo", "onCreate: PUSH Process.");
            return true;
        }

        String appPath = getApplicationInfo().dataDir + "/lib";
        boolean falg = ServiceMgr.getServiceMgr().startService(this, appPath);

        Log.i(UIConstants.DEMO_TAG, "onCreate: MAIN Process.初始化-->" + falg);

        LoginMgr.getInstance().regLoginEventNotification(LoginFunc.getInstance());
        CallMgr.getInstance().regCallServiceNotification(CallFunc.getInstance());
        MeetingMgr.getInstance().regConfServiceNotification(ConfFunc.getInstance());

        initResourceFile();
        return false;
    }

    /**
     * 异常处理
     */
    private void initcrashHandler() {
//         /本地提示
        Recovery.getInstance()
                .debug(true)
                .recoverInBackground(false)
                .recoverStack(true)
                .mainPage(MainActivity.class)
                .recoverEnabled(true)
//                .callback(new MyCrashCallback())
                .silent(false, Recovery.SilentMode.RECOVER_ACTIVITY_STACK)
//                .skip(TestActivity.class)
                .init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        ServiceMgr.getServiceMgr().stopService();
    }

    /**
     * 华为初始化
     *
     * @param context
     * @param frontPkg
     * @return
     */
    private static boolean isFrontProcess(Context context, String frontPkg) {
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
            if (info.pid == pid) {
                Log.i(UIConstants.DEMO_TAG, "processName-->" + info.processName);
                return frontPkg.equals(info.processName);
            }
        }
        return false;
    }

    /**
     * 华为初始化
     */
    private void initResourceFile() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                initDataConfRes();
            }
        });
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                initDataConfRes();
//            }
//        }).start();
    }

    /**
     * 华为初始化
     */
    private void initDataConfRes() {
        String path =
                getFilesDir() + "/AnnoRes";
        File file = new File(path);
        if (file.exists()) {
            LogUtil.i(UIConstants.DEMO_TAG, file.getAbsolutePath());
            File[] files = file.listFiles();
            if (null != files && EXPECTED_FILE_LENGTH == files.length) {
                return;
            } else {
                FileUtil.deleteFile(file);
            }
        }

        try {
            InputStream inputStream = getAssets().open("AnnoRes.zip");
            ZipUtil.unZipFile(inputStream, path);
        } catch (IOException e) {
//            LogUtil.i(UIConstants.DEMO_TAG, "close...Exception->e" + e.toString());
        }
    }

    //    /**
//     * 注意!!! 布局时的实时预览在开发阶段是一个很重要的环节, 很多情况下 Android Studio 提供的默认预览设备并不能完全展示我们的设计图
//     * 所以我们就需要自己创建模拟设备, 以下链接是给大家的福利, 按照链接中的操作可以让预览效果和设计图完全一致!
//     * @see <a href="https://github.com/JessYanCoding/AndroidAutoSize/blob/master/README-zh.md#preview">dp、pt、in、mm 这四种单位的模拟设备创建方法</a>
//     * <p>
//     * v0.9.0 以后, AndroidAutoSize 强势升级, 将这个方案做到极致, 现在支持5种单位 (dp、sp、pt、in、mm)
//     * {@link UnitsManager} 可以让使用者随意配置自己想使用的单位类型
//     * 其中 dp、sp 这两个是比较常见的单位, 作为 AndroidAutoSize 的主单位, 默认被 AndroidAutoSize 支持
//     * pt、in、mm 这三个是比较少见的单位, 只可以选择其中的一个, 作为 AndroidAutoSize 的副单位, 与 dp、sp 一起被 AndroidAutoSize 支持
//     * 副单位是用于规避修改 {@link DisplayMetrics#density} 所造成的对于其他使用 dp 布局的系统控件或三方库控件的不良影响
//     * 您选择什么单位, 就在 layout 文件中用什么单位布局
//     * <p>
//     * 两个主单位和一个副单位, 可以随时使用 {@link UnitsManager} 的方法关闭和重新开启对它们的支持
//     * 如果您想完全规避修改 {@link DisplayMetrics#density} 所造成的对于其他使用 dp 布局的系统控件或三方库控件的不良影响
//     * 那请调用 {@link UnitsManager#setSupportDP}、{@link UnitsManager#setSupportSP} 都设置为 {@code false}
//     * 停止对两个主单位的支持 (如果开启 sp, 对其他三方库控件影响不大, 也可以不关闭对 sp 的支持)
//     * 并调用 {@link UnitsManager#setSupportSubunits} 从三个冷门单位中选择一个作为副单位
//     * 三个单位的效果都是一样的, 按自己的喜好选择, 比如我就喜欢 mm, 翻译为中文是妹妹的意思
//     * 然后在 layout 文件中只使用这个副单位进行布局, 这样就可以完全规避修改 {@link DisplayMetrics#density} 所造成的不良影响
//     * 因为 dp、sp 这两个单位在其他系统控件或三方库控件中都非常常见, 但三个冷门单位却非常少见
//     */
    private void configUnits() {
        //AndroidAutoSize 默认开启对 dp 的支持, 调用 UnitsManager.setSupportDP(false); 可以关闭对 dp 的支持
        //主单位 dp 和 副单位可以同时开启的原因是, 对于旧项目中已经使用了 dp 进行布局的页面的兼容
        //让开发者的旧项目可以渐进式的从 dp 切换到副单位, 即新页面用副单位进行布局, 然后抽时间逐渐的将旧页面的布局单位从 dp 改为副单位
        //最后将 dp 全部改为副单位后, 再使用 UnitsManager.setSupportDP(false); 将 dp 的支持关闭, 彻底隔离修改 density 所造成的不良影响
        //如果项目完全使用副单位, 则可以直接以像素为单位填写 AndroidManifest 中需要填写的设计图尺寸, 不需再把像素转化为 dp
        AutoSizeConfig.getInstance()
                .getUnitsManager()
                .setSupportDP(true);
        //当使用者想将旧项目从主单位过渡到副单位, 或从副单位过渡到主单位时
        //因为在使用主单位时, 建议在 AndroidManifest 中填写设计图的 dp 尺寸, 比如 360 * 640
        //而副单位有一个特性是可以直接在 AndroidManifest 中填写设计图的 px 尺寸, 比如 1080 * 1920
        //但在 AndroidManifest 中却只能填写一套设计图尺寸, 并且已经填写了主单位的设计图尺寸
        //所以当项目中同时存在副单位和主单位, 并且副单位的设计图尺寸与主单位的设计图尺寸不同时, 可以通过 UnitsManager#setDesignSize() 方法配置
        //如果副单位的设计图尺寸与主单位的设计图尺寸相同, 则不需要调用 UnitsManager#setDesignSize(), 框架会自动使用 AndroidManifest 中填写的设计图尺寸
//                .setDesignSize(2160, 3840)
        //AndroidAutoSize 默认开启对 sp 的支持, 调用 UnitsManager.setSupportSP(false); 可以关闭对 sp 的支持
        //如果关闭对 sp 的支持, 在布局时就应该使用副单位填写字体的尺寸
        //如果开启 sp, 对其他三方库控件影响不大, 也可以不关闭对 sp 的支持, 这里我就继续开启 sp, 请自行斟酌自己的项目是否需要关闭对 sp 的支持
//                .setSupportSP(false)
        //AndroidAutoSize 默认不支持副单位, 调用 UnitsManager#setSupportSubunits() 可选择一个自己心仪的副单位, 并开启对副单位的支持
        //只能在 pt、in、mm 这三个冷门单位中选择一个作为副单位, 三个单位的适配效果其实都是一样的, 您觉的哪个单位看起顺眼就用哪个
        //您选择什么单位就在 layout 文件中用什么单位进行布局, 我选择用 mm 为单位进行布局, 因为 mm 翻译为中文是妹妹的意思
        //如果大家生活中没有妹妹, 那我们就让项目中最不缺的就是妹妹!
//                .setSupportSubunits(Subunits.PT);
    }

    /**
     * 返回登录页面
     */
    public static void relogin(boolean clear) {
        //登出APP,清理数据
        HuaweiLoginImp.getInstance().logOut();

        PreferenceUtil.put(getContext(), Constant.SIP_ACCOUNT, "");
        PreferenceUtil.put(getContext(), Constant.SIP_PASSWORD, "");

        MsgIOServer.unRegedit();
        Intent intent = new Intent(getContext(), MsgIOServer.class);
        getContext().stopService(intent);

        Constant.CurrUserName = "";
        Constant.CurrPassWrod = "";
        Constant.CurrDisPlayName = "";

        if (clear) {
            PreferenceUtil.put(getContext(), "CurrPassWrod", "");
        }
    }

    /**
     * 重复登录，被踢出
     */
    public static void mutilLogin(boolean clear) {
        //登出APP，清理数据
        HuaweiLoginImp.getInstance().logOut();

        PreferenceUtil.put(getContext(), Constant.SIP_ACCOUNT, "");
        PreferenceUtil.put(getContext(), Constant.SIP_PASSWORD, "");

        MsgIOServer.unRegedit();
        Intent intent = new Intent(getContext(), MsgIOServer.class);
        getContext().stopService(intent);

        Constant.CurrUserName = "";
        Constant.CurrPassWrod = "";
        Constant.CurrDisPlayName = "";

        if (clear) {
            PreferenceUtil.put(getContext(), "CurrUserName", "");
            PreferenceUtil.put(getContext(), "CurrPassWrod", "");
        }
    }

    /**
     * 关闭MsgServer
     */
    public static void stopMsgServer() {
        MsgIOServer.unRegedit();
        Intent stopIntent = new Intent(getContext(), MsgIOServer.class);
        getContext().stopService(stopIntent);
    }

    /**
     * 重启消息分发服务
     */
    public static void reStartMsgServer() {
        MsgIOServer.unRegedit();
        Intent stopIntent = new Intent(getContext(), MsgIOServer.class);
        getContext().stopService(stopIntent);


        Intent startIntent = new Intent(getContext(), MsgIOServer.class);
        getContext().startService(startIntent);
    }

    @Override
    public void attachBaseContext(Context context) {
        super.attachBaseContext(context);
    }


    private static DemoHandler sHandler;

    public static DemoHandler getHandler() {
        return sHandler;
    }

    public static class DemoHandler extends Handler {

        private Context context;

        public DemoHandler(Context context) {
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg) {
            String s = (String) msg.obj;
//            if (sMainActivity != null) {
//                sMainActivity.refreshLogInfo();
//            }
            if (!TextUtils.isEmpty(s)) {
//                Toast.makeText(context,"这是消息："+ s, Toast.LENGTH_LONG).show();
            }
        }
    }
}
