package com.zxwl.frame.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.internal.LinkedTreeMap;
import com.huawei.opensdk.sdkwrapper.login.LoginCenter;
import com.zxwl.commonlibrary.BaseActivity;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.frame.App;
import com.zxwl.frame.R;
import com.zxwl.frame.adapter.GroupDetailsAdapter;
import com.zxwl.frame.bean.EventMsg;
import com.zxwl.frame.db.DBUtils;
import com.zxwl.frame.net.Urls;
import com.zxwl.network.ApiUrls;
import com.zxwl.network.api.ConfApi;
import com.zxwl.network.api.ImApi;
import com.zxwl.network.bean.BaseData;
import com.zxwl.network.bean.BaseData_BackServer;
import com.zxwl.network.bean.BaseData_logicServer;
import com.zxwl.network.bean.PeopleBean;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GroupDetailsActivity extends BaseActivity {
    private RecyclerView rvList;
    private TextView tvDismissGroup;
    private TextView tvTopTitle;
    private ImageView ivBackOperate;

    private LinearLayout llEditName;
    private TextView tvGroupName;

    private GroupDetailsAdapter adapter;
    private List<PeopleBean> list;
    private Context context;

    private int id;
    private String groupName;

    //是否是自己创建
    public static final String IF_CREATE_USER = "ifCreateUser";

    //群主是否存在
    public static final String IF_GROUP_EXIST = "ifGroupExist";

    public static void startActivity(Context context, int id, String GroupName) {
        Intent intent = new Intent(context, GroupDetailsActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("GroupName", GroupName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void findViews() {
        rvList = (RecyclerView) findViewById(R.id.rv_list);
        tvDismissGroup = (TextView) findViewById(R.id.tv_dismissGroup);
        ivBackOperate = (ImageView) findViewById(R.id.iv_back_operate);
        tvTopTitle = (TextView) findViewById(R.id.tv_top_title);
        llEditName = (LinearLayout) findViewById(R.id.ll_edit_name);
        tvGroupName = (TextView) findViewById(R.id.tv_group_name);
    }

    @Override
    protected void initData() {
        this.context = this;

        id = getIntent().getIntExtra("id", -1);
        groupName = getIntent().getStringExtra("GroupName");

        ivBackOperate.setVisibility(View.VISIBLE);
        tvTopTitle.setText("群聊信息");

        tvGroupName.setText(groupName);

        initAdapter();

        //查询群组人员
        getByDepIdConstacts(id);

        //查询群主
        getGroupCreatePeople();
    }

    private void initAdapter() {
        list = new ArrayList<>();
        adapter = new GroupDetailsAdapter(R.layout.item_group_detail, list);
        rvList.setLayoutManager(new GridLayoutManager(context, 5));
        rvList.setAdapter(adapter);
    }

    private void getByDepIdConstacts(int id) {
        HttpUtils.getInstance(this)
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ConfApi.class)
                .getGroupIdConstacts(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData<PeopleBean>>() {
                    @Override
                    public void onSuccess(BaseData<PeopleBean> baseData) {
                        if (BaseData.SUCCESS == baseData.responseCode) {
                            list.clear();
                            list.addAll(baseData.data);
                            adapter.notifyDataSetChanged();
                        } else {
                            ToastUtil.showShortToast(App.getContext(), "获取失败");
                        }
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
//                        Toast.makeText(ConvokeConfNewActivity.this, "请求失败,error：" + responeThrowable.getCause().toString(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 查询群主
     */
    private void getGroupCreatePeople() {
        HttpUtils.getInstance(this)
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ImApi.class)
                .isGroupCreater(LoginCenter.getInstance().getAccount(), String.valueOf(id))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData_logicServer>() {
                    @Override
                    public void onSuccess(BaseData_logicServer bean) {
                        if (bean.getResponseCode() == 1) {
//                            if (bean.getData().toString().contains("ifCreateUser=true")){
//                                tvRightOperate.setText("解散");
////                                tvRightOperate.setTextColor(ContextCompat.getColor(ChatActivity.this, R.color.color_418AD5));
//                                tvRightOperate.setVisibility(View.VISIBLE);
//                            }
                            //群组是否存在，1存在，0不存在
//                            "ifGroupExist" -> "0.0"
                            //是否是自己创建
//                            "ifCreateUser" -> "true"
                            LinkedTreeMap<String, Object> treeMap = (LinkedTreeMap<String, Object>) bean.getData();
                            //是否是自己创建的
                            boolean isCreate = treeMap.containsKey(IF_CREATE_USER) && ((Boolean) treeMap.get(IF_CREATE_USER));

                            //群是否还在
                            boolean isExist = treeMap.containsKey(IF_GROUP_EXIST) && ((Double) treeMap.get(IF_GROUP_EXIST) == 0);

                            if (isCreate) {
                                llEditName.setVisibility(View.VISIBLE);
                                tvDismissGroup.setVisibility(View.VISIBLE);
                            } else {
                                llEditName.setVisibility(View.GONE);
                                tvDismissGroup.setVisibility(View.GONE);
                            }
                            //群组是否存在，1存在，0不存在
//                            if (bean.getData().toString().contains("ifGroupExist=1.0")) {
//                            }
                        }
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {

                    }
                });
    }

    @Override
    protected void setListener() {
        tvDismissGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissGroup();
            }
        });
        ivBackOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvGroupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditDialogActivity.startActivity(context, id, tvGroupName.getText().toString());
            }
        });
    }

    /**
     * 解散群组
     */
    private void dismissGroup() {
        HttpUtils.getInstance(context)
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ConfApi.class)
                .delGroup(ApiUrls.DEL_GROUP, String.valueOf(id))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData_BackServer>() {

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        Toast.makeText(context, "解散失败，请稍后重试", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(BaseData_BackServer baseData_backServer) {
                        if (baseData_backServer.getCode() == 0) {
                            Toast.makeText(context, "解散群组成功", Toast.LENGTH_SHORT).show();
                            DBUtils.deleteGroupRecord(String.valueOf(id));
                            updateGroupList();
                            finish();
                        }
                    }
                });
    }

    public void updateGroupList() {
        try {
            EventMsg eventMsg = new EventMsg();
            eventMsg.setMsg(EventMsg.UPDATE_GROUP);
            EventBus.getDefault().post(eventMsg);
        } catch (Exception e) {

        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_group_details;
    }
}
