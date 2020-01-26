package com.zxwl.network.http;

import android.content.Context;
import android.util.Log;

import com.zxwl.network.RetrofitClient;
import com.zxwl.network.config.NetWorkConfiguration;
import com.zxwl.network.interceptor.LogInterceptor;
import com.zxwl.network.interceptor.SessionIdInterceptor;
import com.zxwl.network.utils.CommonUtil;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.CacheControl;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Copyright 2015 蓝色互动. All rights reserved.
 * author：hw
 * data:2017/4/20 14:21
 * ClassName: ${Class_Name}
 */

public class HttpUtils {
    //获得HttpUtils实例
    private static HttpUtils mInstance;

    private OkHttpClient okHttpClient;

    private static NetWorkConfiguration netWorkConfiguration;

    private Context context;

    /**
     * 是否加载本地缓存数据，默认为true
     */
    private boolean isLoadDiskCache = true;

    /**
     * -->针对无网络情况
     * 是否加载本地缓存数据
     *
     * @param isCache true为加载，false为不加载
     * @return
     */
    public HttpUtils setLoadDiskCache(boolean isCache) {
        this.isLoadDiskCache = isCache;
        return this;
    }

    /**
     * ---> 针对有网络情况
     * 是否加载内存缓存数据
     * 默认为false
     */
    private boolean isLoadMemoryCache = false;

    /**
     * ---> 针对有网络情况
     * 是否加载内存缓存数据
     *
     * @param isCache true为加载 false不进行加载
     * @return
     */
    public HttpUtils setLoadMemoryCache(boolean isCache) {
        this.isLoadMemoryCache = isCache;
        return this;
    }
    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

    public HttpUtils(Context context) {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(10, TimeUnit.SECONDS);
//            builder.sslSocketFactory(sslSocketFactory);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            this.context = context.getApplicationContext();
            /**
             * 如果配置为空，进行默认配置
             */
            if (null == netWorkConfiguration) {
                netWorkConfiguration = new NetWorkConfiguration(context.getApplicationContext());
            }

            //获得是否缓存
            if (netWorkConfiguration.getIsCache()) {
                okHttpClient = new OkHttpClient.Builder()
                        .sslSocketFactory(sslSocketFactory)
                        .hostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String hostname, SSLSession session) {
                                return true;
                            }
                        })
                        .addInterceptor(new LogInterceptor(LogInterceptor.Level.BODY))
                        .addInterceptor(cacheInterceptor)
                        .addInterceptor(new SessionIdInterceptor())
                        .addNetworkInterceptor(cacheInterceptor)
                        //缓存路径
                        .cache(netWorkConfiguration.getDiskCache())
                        .connectTimeout(netWorkConfiguration.getconnectTimeOut(), TimeUnit.SECONDS)
                        .connectionPool(netWorkConfiguration.getConnectionPool())
                        .retryOnConnectionFailure(true)
                        .build();
            } else {
                okHttpClient = new OkHttpClient.Builder()
//                    .addInterceptor(logInterceptor)
                        .sslSocketFactory(sslSocketFactory)
                        .hostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String hostname, SSLSession session) {
                                return true;
                            }
                        })
                        .cookieJar(new CookieJar() {

                            @Override
                            public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
                                cookieStore.put(httpUrl.host(), list);
                            }

                            @Override
                            public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                                List<Cookie> cookies = cookieStore.get(httpUrl.host());
                                return cookies != null ? cookies : new ArrayList<Cookie>();
                            }
                        })
                        .addInterceptor(new LogInterceptor(LogInterceptor.Level.BODY))
                        .addInterceptor(new SessionIdInterceptor())
                        .connectTimeout(netWorkConfiguration.getconnectTimeOut(), TimeUnit.SECONDS)
                        .connectionPool(netWorkConfiguration.getConnectionPool())
                        .retryOnConnectionFailure(true)
                        .build();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置网络配置参数
     *
     * @param configuration
     */
    public static void setConFiguration(NetWorkConfiguration configuration) {
        if (null == configuration) {
            throw new IllegalArgumentException("ImageLoader configuration can not be initialized with null");
        } else {
            if (null == HttpUtils.netWorkConfiguration) {
                Log.d("HttpUtils", "Initialize NetWorkConfiguration with configuration");
                HttpUtils.netWorkConfiguration = configuration;
            } else {
                Log.e("HttpUtils", "Try to initialize NetWorkConfiguration which had already been initialized before. To re-init NetWorkConfiguration with new configuration ");
            }
            Log.i("HttpUtils", "ConFiguration" + configuration.toString());
        }

    }


    /**
     * 获取请求网络实例
     *
     * @return
     */
    public static HttpUtils getInstance(Context context) {
        if (mInstance == null) {
            synchronized (HttpUtils.class) {
                if (mInstance == null) {
                    mInstance = new HttpUtils(context.getApplicationContext());
                }
            }
        }
        return mInstance;
    }

    public RetrofitClient getRetofitClinet() {
//        Logger.i("configuration:" + netWorkConfiguration.toString());
        return new RetrofitClient(netWorkConfiguration.getBaseUrl(), okHttpClient);
    }

    /**
     * 网络拦截器
     * 进行网络操作的时候进行拦截
     */
    final Interceptor cacheInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            //断网后是否加载本地缓存数据
            if (!CommonUtil.isNetworkAvailable(netWorkConfiguration.context) && isLoadDiskCache) {
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
            }//加载内存缓存数据
            else if (isLoadMemoryCache) {
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
            }//加载网络数据
            else {
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .build();
            }
            Response response = chain.proceed(request);
            //有网进行内存缓存数据
            if (CommonUtil.isNetworkAvailable(netWorkConfiguration.context) && netWorkConfiguration.getIsMemoryCache()) {
                response.newBuilder()
                        //清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, max-age=" + netWorkConfiguration.getmemoryCacheTime())
                        .build();
            } else {
                //进行本地缓存数据
                if (netWorkConfiguration.getIsDiskCache()) {
                    response.newBuilder()
                            //清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                            .removeHeader("Pragma")
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + netWorkConfiguration.getDiskCacheTime())
                            .build();
                }
            }
            return response;
        }
    };

}
