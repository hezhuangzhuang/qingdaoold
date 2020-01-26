package com.zxwl.network.api;

import com.zxwl.network.bean.BaseData;
import com.zxwl.network.bean.BaseData_BackServer;
import com.zxwl.network.bean.BaseData_logicServer;
import com.zxwl.network.bean.ConfBean;
import com.zxwl.network.bean.CreateGroupBean;
import com.zxwl.network.bean.GroupNewBean;
import com.zxwl.network.bean.HistoryConfBean;
import com.zxwl.network.bean.OrganizationBean;
import com.zxwl.network.bean.PeopleBean;
import com.zxwl.network.bean.response.GroupUser;

import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

/**
 * author：pc-20171125
 * data:2019/5/11 10:35
 */
public interface ConfApi {

    /**
     * 获取用户列表
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST
    Observable<BaseData> getUserList(@Url String url,
                                     @Body RequestBody body);

    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST
    Observable<GroupUser> getUserList(@Url String url,
                                      @Query("discussionGroupId") String name);

    /**
     * 创建群组,弃用
     *
     * @param url
     * @param name
     * @param uri
     * @return
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST
    Observable<CreateGroupBean> createGroup(@Url String url,
                                            @Query("groupName") String name,
                                            @Query("uri") String uri);

    /**
     * 创建群组，通知另一个服务器更新数据
     *
     * @param id
     * @return
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("group/add/")
    Observable<BaseData_BackServer> createGroupNot(
            @Query("id") int id);


    /**
     * 删除群组
     *
     * @param url
     * @param id
     * @return
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST
    Observable<BaseData_BackServer> delGroup(@Url String url,
                                             @Query("groupId") String id);

    /**
     * 入会
     *
     * @param url
     * @param accessCode
     * @return
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST
    Observable<BaseData_BackServer> joinConf(@Url String url,
                                             @Query("accessCode") String accessCode,
                                             @Query("sites") String sites);

    /**
     * 获取用户列表
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("im/mobile/account/findAllByFirstChar")
    Observable<BaseData<PeopleBean>> getAllConstacts();

    /**
     * 获取特别用户列表
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("im/mobile/account/findAllGovAccount")
    Observable<BaseData<PeopleBean>> getGovAccounts();

    /**
     * 获取用户列表
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("im/mobile/account/findAllByFirstChar")
    Observable<BaseData<PeopleBean>> getConstacts(
            @Query("name") String keyword
    );

    /**
     * 获取所有组织
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("im/mobile/department/findAll")
    Observable<BaseData<OrganizationBean>> getAllOrganizations();

    /**
     * 根据组织id获取所属人员
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("im/mobile/account/getByDepId")
    Observable<BaseData<PeopleBean>> getDepIdConstacts(@Query("depId") int depId);

    /**
     * 根据id查询群组人员
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("im/mobile/group/getAccounts")
    Observable<BaseData<PeopleBean>> getGroupIdConstacts(@Query("groupId") int Id);

    /**
     * 根据id查询群组人员
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("im/mobile/group/getAccounts")
    Observable<BaseData<PeopleBean>> SearchGroupIdConstacts(
            @Query("groupId") int Id,
            @Query("name") String keywords
    );

    /**
     * 查询所有群组
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("im/mobile/group/getAllGroups")
    Observable<BaseData<GroupNewBean>> getAllGroups();

    /**
     * 查询所有群组
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("im/mobile/group/getGroupsByAccount")
    Observable<BaseData<GroupNewBean>> getSelfGroups(@Query("id") String id);


    /**
     * 查询会议列表
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("conf/queryBySiteUri")
    Observable<ConfBean> getAllConf(@Body RequestBody body);

    /**
     * 查询会议列表
     * pageNum, pageSize
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @GET("im/confInfo/queryListByMobile")
    Observable<HistoryConfBean> getHistoryList(
            @Query("pageNum") int pageNum,
            @Query("pageSize") int pageSize
    );

    /**
     * 创建群组
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST
    Observable<BaseData_logicServer> newcreateGroup(@Url String url,
                                                    @Query("groupName") String groupName,
                                                    @Query("createId") String createId,
                                                    @Query("ids") String ids);

    /**
     * 加入会议
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("conf/addSiteByAccessCode")
    Observable<BaseData_BackServer> joinConfNew(
            @Query("accessCode") String accessCode,
            @Query("sites") String sites);

    /*
     * 查询华为smc在线状态
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("/site/querySmcSiteByUri")
    Observable<BaseData> querySmcSiteByUri(@Body RequestBody body);

    /*
     * 查询华为smc在线状态
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("site/confAction_querySiteOnlineState")
    Observable<BaseData_BackServer> querySiteOnlineState( @Query("siteUri") String siteUri);

    /*
     *保存华为推送的token
     */
    @Headers({"Content-Type: application/json", "Accept: application/json"})//需要添加头
    @POST("site/confAction_putUriAndToken.action")
    Observable<BaseData_BackServer> saveHuaweiToken(
            @Query("uri") String siteUri,
            @Query("token") String token
    );
}
