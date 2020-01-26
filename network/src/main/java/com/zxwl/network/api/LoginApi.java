package com.zxwl.network.api;

import com.zxwl.network.bean.BaseData;
import com.zxwl.network.bean.BaseData_logicServer;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

/**
 * author：pc-20171125
 * data:2018/12/18 17:52
 */

public interface LoginApi {
    /**
     * 获取sessionID方法
     */
    @POST
    Observable<BaseData> getSessionId(@Url String url);

    /**
     * 登录
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST
    Observable<BaseData> login(@Url String url,
                               @Body RequestBody body);

    /**
     * 登录
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST
    Observable<BaseData> login(@Url String url,
                               @Query("userName") String name,
                               @Query("password") String pwd,
                               @Query("loginType") String loginType);

    /*
     * 上传访客记录
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST
    Observable<BaseData> checkIn(@Url String url,
                                 @Field("name") String name,
                                 @Field("tel") String tel,
                                 @Field("studentName") String studentName,
                                 @Field("plate") String plate,
                                 @Field("remark") String remark,
                                 @Field("src") String base64);

    /*
     * 上传访客记录
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST
    Observable<BaseData> checkIn(@Url String url,
                                 @Body RequestBody body);

    /**
     * 更改sessionID方法
     */
    @POST
    Observable<BaseData> changeSessionId(@Url String url);


    /**
     * 获取登录参数
     */
    @POST
    Observable<BaseData_logicServer> getLoginInfo(@Url String url,
                                                  @Query("deviceID") String deviceID,
                                                  @Query("userName") String name,
                                                  @Query("userWord") String pwd);

    /**
     * 心跳
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("im/mobile/logOut")
    Observable<BaseData_logicServer> loginout(
            @Query("sip") String sip);

}
