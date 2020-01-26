package com.zxwl.frame.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.huawei.opensdk.sdkwrapper.login.LoginCenter;
import com.zxwl.commonlibrary.BaseActivity;
import com.zxwl.commonlibrary.utils.DialogUtils;
import com.zxwl.frame.R;
import com.zxwl.frame.adapter.GroupAdapter;
import com.zxwl.network.api.ImApi;
import com.zxwl.network.bean.GroupBean;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;
import com.zxwl.network.rxfun.RetryWithDelay;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GroupActivity extends BaseActivity {
    private ImageView ivBackOperate;
    private TextView tvLeftOperate;
    private TextView tvTopTitle;
    private RecyclerView rvContent;
    private TextView tvRightOperate;

    private GroupAdapter adapter;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, GroupActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void findViews() {
        ivBackOperate = (ImageView) findViewById(R.id.iv_back_operate);
        ivBackOperate.setVisibility(View.VISIBLE);
        tvLeftOperate = (TextView) findViewById(R.id.tv_left_operate);
        tvTopTitle = (TextView) findViewById(R.id.tv_top_title);
        rvContent = (RecyclerView) findViewById(R.id.rv_content);
        tvRightOperate = (TextView) findViewById(R.id.tv_right_operate);
    }

    @Override
    protected void initData() {

        tvTopTitle.setText("群组聊天");
        tvTopTitle.setVisibility(View.VISIBLE);
        tvTopTitle.setTextColor(ContextCompat.getColor(this, R.color.color_333));

        tvRightOperate.setVisibility(View.VISIBLE);
        tvRightOperate.setText("创建群组");
        tvRightOperate.setTextColor(ContextCompat.getColor(this, R.color.color_418AD5));

    }

    @Override
    protected void onResume() {
        super.onResume();
        String account = LoginCenter.getInstance().getAccount();
//        String userId = account.substring(account.length() - 1, account.length());
        String userId = account;
        initGroup(userId);
    }

    @Override
    protected void setListener() {
        ivBackOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvRightOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                CreateGroupActivity.startActivity(GroupActivity.this);
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_group;
    }

    private void initGroup(String userId) {

        DialogUtils.showProgressDialog(this, "正在获取列表....");

        HttpUtils.getInstance(this)
                .getRetofitClinet()
                .setBaseUrl(com.zxwl.frame.net.Urls.GUOHANG_BASE_URL)
                .builder(ImApi.class)
                .queryGroup(userId)
                .retryWhen(new RetryWithDelay(3, 500))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<GroupBean>() {
                    @Override
                    public void onSuccess(GroupBean baseData) {
                        if (0 == baseData.code && "success".equals(baseData.msg)) {
                            initRecycler(baseData.data);
                        }
                        DialogUtils.dismissProgressDialog(GroupActivity.this);
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        DialogUtils.dismissProgressDialog(GroupActivity.this);
                        Toast.makeText(GroupActivity.this, "请求失败,error：" + responeThrowable.getCause().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void initRecycler(List<GroupBean.DataBean> data) {
        adapter = new GroupAdapter(R.layout.item_constacts, data);
        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                GroupBean.DataBean dataBean = (GroupBean.DataBean) adapter.getData().get(position);
                ChatActivity.startActivity(GroupActivity.this, dataBean.id + "", dataBean.name, true);
            }
        });
        rvContent.setLayoutManager(new LinearLayoutManager(this));
        rvContent.setAdapter(adapter);
    }
}
