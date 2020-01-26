package com.zxwl.network.api;

import com.zxwl.network.bean.BaseData_logicServer;
import com.zxwl.network.bean.GroupBean;
import com.zxwl.network.bean.ImBean;

import okhttp3.MultipartBody;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import rx.Observable;

/**
 * author：pc-20171125
 * data:2019/5/14 10:49
 */
public interface ImApi {
    /**
     * 上传文件
     */
    @Multipart
    @POST("fileAction_uploadFile.action")
    Observable<ImBean> upload(@Part MultipartBody.Part file);

    /**
     * 通过iD查询群组
     */
    @GET("groupAction_queryByUserId.action")
    Observable<GroupBean> queryGroup(@Query("userId") String file);

    /**
     * 查询群主
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("im/mobile/group/judgeIfCreateUser")
    Observable<BaseData_logicServer> isGroupCreater(
            @Query("sipAccount") String sipAccount,
            @Query("groupId") String groupId
    );

    /**
     * 心跳
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("im/mobile/heartMap")
    Observable<BaseData_logicServer> ping(
            @Query("sip") String sip,
            @Query("deviceID") String deviceID
            );

    /**
     * 检查更新
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("im/mobile/version")
    Observable<BaseData_logicServer> checkUpdate();

    /**
     * 绑定手机
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("/im/mobile/account/bindTelNum")
    Observable<BaseData_logicServer> bindPhone(
            @Query("id") int id,
            @Query("telephone") String telephone
    );

    /**
     * 获取绑定手机号码回显
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("/im/mobile/account/checkTelNum")
    Observable<BaseData_logicServer> getBindPhone(
            @Query("id") int id
    );

    /**
     * 修改密码
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("im/mobile/account/updatePwd")
    Observable<BaseData_logicServer> changePWD(
            @Query("id") int id,
            @Query("oldPassword") String oldPassword,
            @Query("newPassword") String newPassword
    );

    /**
     * 修改群名称
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("/im/mobile/group/updateGroupName")
    Observable<BaseData_logicServer> reNameGroup(
            @Query("groupId") int groupId,
            @Query("newName") String newName
    );
}
