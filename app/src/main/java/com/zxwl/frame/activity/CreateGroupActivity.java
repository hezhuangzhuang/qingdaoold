package com.zxwl.frame.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.zxwl.commonlibrary.BaseActivity;
import com.zxwl.commonlibrary.utils.DialogUtils;
import com.zxwl.frame.R;
import com.zxwl.frame.adapter.AddConfSiteAdapter;
import com.zxwl.frame.service.MsgIOServer;
import com.zxwl.network.ApiUrls;
import com.zxwl.network.api.ConfApi;
import com.zxwl.network.bean.BaseData_BackServer;
import com.zxwl.network.bean.CreateGroupBean;
import com.zxwl.network.bean.response.GroupUser;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;
import com.zxwl.network.rxfun.RetryWithDelay;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.RequestBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class CreateGroupActivity extends BaseActivity {
    private ImageView ivBackOperate;
    private TextView tvLeftOperate;
    private TextView tvTopTitle;
    private TextView tvRightOperate;
    private EditText et_groupName;

    private RecyclerView rvContent;
    private AddConfSiteAdapter adapter;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, CreateGroupActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void findViews() {
        ivBackOperate = (ImageView) findViewById(R.id.iv_back_operate);
        tvLeftOperate = (TextView) findViewById(R.id.tv_left_operate);
        tvTopTitle = (TextView) findViewById(R.id.tv_top_title);
        tvRightOperate = (TextView) findViewById(R.id.tv_right_operate);
        rvContent = (RecyclerView) findViewById(R.id.rv_content);
        et_groupName = (EditText) findViewById(R.id.group_name);
    }

    @Override
    protected void initData() {
        ivBackOperate.setVisibility(View.GONE);

        tvLeftOperate.setVisibility(View.VISIBLE);
        tvLeftOperate.setText("取消");
        tvLeftOperate.setTextColor(ContextCompat.getColor(this, R.color.color_666666));

        tvTopTitle.setVisibility(View.VISIBLE);
        tvTopTitle.setText("创建群组");
        tvTopTitle.setTextColor(ContextCompat.getColor(this, R.color.color_333));

        tvRightOperate.setVisibility(View.VISIBLE);
        tvRightOperate.setText("确认");
        tvRightOperate.setTextColor(ContextCompat.getColor(this, R.color.color_418AD5));

        DialogUtils.showProgressDialog(this, "正在获取列表....");
        initConstacts("1");
    }

    /**
     * 获取用户列表
     */
    private void initConstacts(String groupId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("discussionGroupId", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(ApiUrls.MEDIA_TYPE, jsonObject.toString());

        HttpUtils.getInstance(this)
                .getRetofitClinet()
                .setBaseUrl(com.zxwl.frame.net.Urls.GUOHANG_BASE_URL)
                .builder(ConfApi.class)
                .getUserList(ApiUrls.GROUP_USER_LIST, "1")
                .retryWhen(new RetryWithDelay(3, 500))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<GroupUser>() {
                    @Override
                    public void onSuccess(GroupUser baseData) {

//                        Gson gson = new Gson();
//                        String str = "{\"code\":0,\"msg\":\"success\",\"data\":[{\"id\":\"-1\",\"name\":\"Root\",\"online\":0},{\"id\":\"0271001\",\"name\":\"颜鹏\",\"online\":0},{\"id\":\"0271101\",\"name\":\"0271101\",\"online\":1},{\"id\":\"0271102\",\"name\":\"0271102\",\"online\":1},{\"id\":\"02711101\",\"name\":\"02711101\",\"online\":1},{\"id\":\"02711102\",\"name\":\"02711102\",\"online\":1},{\"id\":\"027247\",\"name\":\"武汉TE30\",\"online\":0},{\"id\":\"0271108\",\"name\":\"0271108\",\"online\":1},{\"id\":\"0271122\",\"name\":\"周勇\",\"online\":1},{\"id\":\"027991\",\"name\":\"027991\",\"online\":1},{\"id\":\"027993\",\"name\":\"027993\",\"online\":1},{\"id\":\"0271002\",\"name\":\"0271002\",\"online\":1},{\"id\":\"0271003\",\"name\":\"0271003\",\"online\":1},{\"id\":\"0271004\",\"name\":\"0271004\",\"online\":1},{\"id\":\"0271005\",\"name\":\"0271005\",\"online\":1},{\"id\":\"0271006\",\"name\":\"0271006\",\"online\":1},{\"id\":\"0271008\",\"name\":\"0271008\",\"online\":0},{\"id\":\"0271007\",\"name\":\"0271007\",\"online\":1},{\"id\":\"0271009\",\"name\":\"0271009\",\"online\":1},{\"id\":\"0271010\",\"name\":\"0271010\",\"online\":1},{\"id\":\"0271011\",\"name\":\"0271011\",\"online\":1},{\"id\":\"027992\",\"name\":\"027992\",\"online\":0},{\"id\":\"0271013\",\"name\":\"语音监听\",\"online\":0},{\"id\":\"027951\",\"name\":\"027951\",\"online\":0},{\"id\":\"027952\",\"name\":\"027952\",\"online\":1},{\"id\":\"-1\",\"name\":\"湖北\",\"online\":0},{\"id\":\"0271012\",\"name\":\"武汉\",\"online\":1},{\"id\":\"-1\",\"name\":\"武汉\",\"online\":0},{\"id\":\"027999\",\"name\":\"027999\",\"online\":1},{\"id\":\"-1\",\"name\":\"北京\",\"online\":0}]}";
//                        baseData = gson.fromJson(str, GroupUser.class);

                        DialogUtils.dismissProgressDialog(CreateGroupActivity.this);
                        if ("0".equals(baseData.code) && "success".equals(baseData.msg)) {
                            initRecycler(baseData.data);
                            DialogUtils.dismissProgressDialog(CreateGroupActivity.this);
                        }
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        DialogUtils.dismissProgressDialog(CreateGroupActivity.this);
                        Toast.makeText(CreateGroupActivity.this, "请求失败,error：" + responeThrowable.getCause().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void setListener() {
        tvLeftOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvRightOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectUser.size() <= 0) {
                    Toast.makeText(CreateGroupActivity.this, "请选择群组成员", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(et_groupName.getText().toString().trim())) {
                    Toast.makeText(CreateGroupActivity.this, "请输入群组名称", Toast.LENGTH_SHORT).show();
                    return;
                }

                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0; i < selectUser.size(); i++) {
                    if (i < selectUser.size() - 1) {
                        stringBuffer.append(selectUser.get(i).id).append(",");
                    } else {
                        stringBuffer.append(selectUser.get(i).id);
                    }
                }

//                HuaweiCallImp.getInstance().createConferenceNetWork("测试会议", "120", stringBuffer.toString(), "1");
                HttpUtils.getInstance(CreateGroupActivity.this)
                        .getRetofitClinet()
                        .setBaseUrl(com.zxwl.frame.net.Urls.GUOHANG_BASE_URL)
                        .builder(ConfApi.class)
                        .createGroup(ApiUrls.CREATE_GROUP, et_groupName.getText().toString().trim(), stringBuffer.toString())
                        .flatMap(new Func1<CreateGroupBean, Observable<BaseData_BackServer>>() {
                            @Override
                            public Observable<BaseData_BackServer> call(CreateGroupBean baseData) {
                                if (baseData.getCode() == 0) {
//                                    Toast.makeText(CreateGroupActivity.this, "群组创建成功", Toast.LENGTH_SHORT).show();
//                                    return null;
                                    return HttpUtils.getInstance(CreateGroupActivity.this)
                                            .getRetofitClinet()
//                                            .setBaseUrl(MsgIOServer.websocketUrl + ":9022/")
                                            .setBaseUrl(MsgIOServer.websocketUrl)
                                            .builder(ConfApi.class)
                                            .createGroupNot(baseData.getData().getId());
                                } else {
                                    return null;
                                }
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new RxSubscriber<BaseData_BackServer>() {
                            @Override
                            public void onSuccess(BaseData_BackServer baseData) {
                                Toast.makeText(CreateGroupActivity.this, "群组创建成功", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            protected void onError(ResponeThrowable responeThrowable) {
                                Toast.makeText(CreateGroupActivity.this, responeThrowable.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_convoke_conf;
    }

    private List<GroupUser.DataBean> selectUser = new ArrayList<>();

    private void initRecycler(List<GroupUser.DataBean> data) {
        adapter = new AddConfSiteAdapter(R.layout.item_add_address, data);
        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                GroupUser.DataBean userBean = (GroupUser.DataBean) adapter.getData().get(position);
                if ("-1".equals(userBean.id)){
                    return;
                }
                //判断是否被选中
                userBean.isCheck = !userBean.isCheck;
                if (userBean.isCheck) {
                    selectUser.add(userBean);
                } else {
                    selectUser.remove(userBean);
                }
                adapter.notifyDataSetChanged();
            }
        });
        rvContent.setLayoutManager(new LinearLayoutManager(this));
        rvContent.setAdapter(adapter);
    }


}
