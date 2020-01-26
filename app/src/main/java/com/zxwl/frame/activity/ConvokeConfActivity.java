package com.zxwl.frame.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.huawei.opensdk.sdkwrapper.login.LoginCenter;
import com.zxwl.commonlibrary.BaseActivity;
import com.zxwl.commonlibrary.utils.DialogUtils;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.frame.App;
import com.zxwl.frame.R;
import com.zxwl.frame.adapter.ConstactsAdapter;
import com.zxwl.frame.inter.HuaweiCallImp;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.utils.Constant;
import com.zxwl.frame.utils.DateUtil;
import com.zxwl.network.ApiUrls;
import com.zxwl.network.api.ConfApi;
import com.zxwl.network.bean.BaseData;
import com.zxwl.network.bean.BaseData_logicServer;
import com.zxwl.network.bean.PeopleBean;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;
import com.zxwl.network.rxfun.RetryWithDelay;

import java.util.ArrayList;
import java.util.List;

import qdx.stickyheaderdecoration.NormalDecoration;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ConvokeConfActivity extends BaseActivity {
    private ImageView ivBackOperate;
    private TextView tvLeftOperate;
    private TextView tvTopTitle;
    private TextView tvRightOperate;
    private EditText etName;

    private EditText etAccessCode;

    private RecyclerView rvContent;
    private ConstactsAdapter adapter;

    private boolean onlyVoice = false;

    public static final String GROUP_ID = "GROUP_ID";
    private int groupId = -1;

    public static final String TITLE = "TITLE";
    private String title;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ConvokeConfActivity.class);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, String title) {
        Intent intent = new Intent(context, ConvokeConfActivity.class);
        intent.putExtra(TITLE, title);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, boolean onlyVoice) {
        Intent intent = new Intent(context, ConvokeConfActivity.class);
        intent.putExtra("onlyVoice", onlyVoice);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, boolean onlyVoice, int groupId) {
        Intent intent = new Intent(context, ConvokeConfActivity.class);
        intent.putExtra("onlyVoice", onlyVoice);
        intent.putExtra(GROUP_ID, groupId);
        context.startActivity(intent);
    }

    @Override
    protected void findViews() {
        ivBackOperate = (ImageView) findViewById(R.id.iv_back_operate);
        tvLeftOperate = (TextView) findViewById(R.id.tv_left_operate);
        tvTopTitle = (TextView) findViewById(R.id.tv_top_title);
        tvRightOperate = (TextView) findViewById(R.id.tv_right_operate);
        rvContent = (RecyclerView) findViewById(R.id.rv_content);
        etName = (EditText) findViewById(R.id.group_name);
        etAccessCode = (EditText) findViewById(R.id.et_access_code);
    }

    @Override
    protected void initData() {
        ivBackOperate.setVisibility(View.GONE);

        groupId = getIntent().getIntExtra(GROUP_ID, -1);

        tvLeftOperate.setVisibility(View.VISIBLE);
        tvLeftOperate.setText("取消");
//        tvLeftOperate.setTextColor(ContextCompat.getColor(this, R.color.color_666666));

        tvTopTitle.setVisibility(View.VISIBLE);
        tvTopTitle.setText("添加与会人");
//        tvTopTitle.setTextColor(ContextCompat.getColor(this, R.color.color_333));

        tvRightOperate.setVisibility(View.VISIBLE);
        tvRightOperate.setText("召集会议");
//        tvRightOperate.setTextColor(ContextCompat.getColor(this, R.color.color_418AD5));

        this.onlyVoice = getIntent().getBooleanExtra("onlyVoice", false);

        if (onlyVoice) {
            tvRightOperate.setVisibility(View.VISIBLE);
            tvRightOperate.setText("召集语音会议");
//            tvRightOperate.setTextColor(ContextCompat.getColor(this, R.color.color_418AD5));
        } else {
            tvRightOperate.setVisibility(View.VISIBLE);
            tvRightOperate.setText("召集视频会议");
//            tvRightOperate.setTextColor(ContextCompat.getColor(this, R.color.color_418AD5));
        }

        title = getIntent().getStringExtra(TITLE);
        if (!TextUtils.isEmpty(title)) {
            tvTopTitle.setVisibility(View.VISIBLE);
            tvTopTitle.setText(title);
//            tvTopTitle.setTextColor(ContextCompat.getColor(this, R.color.color_333));

            tvRightOperate.setVisibility(View.VISIBLE);
            tvRightOperate.setText("创建群组");
//            tvRightOperate.setTextColor(ContextCompat.getColor(this, R.color.color_418AD5));
        }

        DialogUtils.showProgressDialog(this, "正在获取列表....");

        if (-1 == groupId) {
            getAllConstacts();
        } else {
            getByDepIdConstacts(groupId);
        }
    }

    private void getAllConstacts() {
        HttpUtils.getInstance(this)
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ConfApi.class)
                .getAllConstacts()
                .retryWhen(new RetryWithDelay(3, 500))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData<PeopleBean>>() {
                    @Override
                    public void onSuccess(BaseData<PeopleBean> baseData) {
                        DialogUtils.dismissProgressDialog(ConvokeConfActivity.this);
                        if (BaseData.SUCCESS == baseData.responseCode) {
                            if (baseData.data.size() > 0) {
                                initRecycler(removeSelf(baseData.data));
                            }
                        } else {
                            ToastUtil.showShortToast(App.getContext(), "获取失败");
                        }
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        Toast.makeText(getApplication(), "请求失败,error：" + responeThrowable.getCause().toString(), Toast.LENGTH_SHORT).show();
                        DialogUtils.dismissProgressDialog(ConvokeConfActivity.this);
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
                if (TextUtils.isEmpty(etName.getText())) {
                    ToastUtil.showShortToast(ConvokeConfActivity.this, "请输入名称");
                    return;
                }
                if (!TextUtils.isEmpty(title)) {
                    if (selectUser.size() <= 0) {
                        Toast.makeText(ConvokeConfActivity.this, "请选择成员", Toast.LENGTH_SHORT).show();
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

                    HttpUtils.getInstance(ConvokeConfActivity.this)
                            .getRetofitClinet()
                            .setBaseUrl(Urls.logicServerURL)
                            .builder(ConfApi.class)
                            .newcreateGroup(ApiUrls.NEW_CREATE_GROUP, etName.getText().toString(), String.valueOf(Constant.CurrID), stringBuffer.toString() + "," + Constant.CurrID)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new RxSubscriber<BaseData_logicServer>() {
                                @Override
                                public void onSuccess(BaseData_logicServer baseData_logicServer) {
                                    if (baseData_logicServer.getResponseCode() == 1) {
                                        ToastUtil.showShortToast(ConvokeConfActivity.this, "群组创建成功");
                                        finish();
                                    } else {
                                        ToastUtil.showShortToast(ConvokeConfActivity.this, "群组创建失败，服务器报错");
                                    }
                                }

                                @Override
                                protected void onError(ResponeThrowable responeThrowable) {
                                    ToastUtil.showShortToast(ConvokeConfActivity.this, "群组创建失败，服务器异常");
                                }
                            });
                    return;
                }

                if (selectUser.size() <= 0) {
                    Toast.makeText(ConvokeConfActivity.this, "请选择参会列表", Toast.LENGTH_SHORT).show();
                    return;
                }

                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0; i < selectUser.size(); i++) {
                    if (i < selectUser.size() - 1) {
                        stringBuffer.append(selectUser.get(i).sip).append(",");
                    } else {
                        stringBuffer.append(selectUser.get(i).sip);
                    }
                }

                HuaweiCallImp.getInstance().createConferenceNetWork(DateUtil.getCurrentTime(DateUtil.FORMAT_DATE_TIME), "120", stringBuffer.toString() + "," + LoginCenter.getInstance().getAccount(), "1","", onlyVoice ? 0 : 1);
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_convoke_conf;
    }

    private List<PeopleBean> selectUser = new ArrayList<>();

    private NormalDecoration decoration;

    private void initRecycler(List<PeopleBean> dataList) {
        for (int i = 0; i < dataList.size(); i++) {
            dataList.get(i).hearRes = Constant.getPersonalHeadRes();
        }
        adapter = new ConstactsAdapter(R.layout.item_constacts, dataList);
        adapter.setShowCheck(true);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                PeopleBean userBean = (PeopleBean) adapter.getData().get(position);
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

        decoration = new NormalDecoration() {
            @Override
            public String getHeaderName(int pos) {
                return dataList.get(pos).firstLetter;
            }
        };

        rvContent.addItemDecoration(decoration);
        rvContent.setLayoutManager(new LinearLayoutManager(this));
        rvContent.setAdapter(adapter);
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
                        DialogUtils.dismissProgressDialog(ConvokeConfActivity.this);
                        if (BaseData.SUCCESS == baseData.responseCode) {
                            if (baseData.data.size() > 0) {
//                                initRecycler(baseData.data);
                                initRecycler(removeSelf(baseData.data));
                            }
                        } else {
                            ToastUtil.showShortToast(App.getContext(), "获取失败");
                        }
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        Toast.makeText(ConvokeConfActivity.this, "请求失败,error：" + responeThrowable.getCause().toString(), Toast.LENGTH_SHORT).show();
                        DialogUtils.dismissProgressDialog(ConvokeConfActivity.this);
                    }
                });
    }

    private List<PeopleBean> removeSelf(List<PeopleBean> data) {
        int index = -1;
        for (int i = 0; i < data.size(); i++) {
            if (LoginCenter.getInstance().getAccount().equals(data.get(i).sip)) {
                index = i;
            }
        }
        if (index != -1) {
            data.remove(index);
        }
//        PeopleBean self = null;
//        for (PeopleBean temp : data){
//            if (temp.sip.equals(LoginCenter.getInstance().getAccount())){
//                self = temp;
//            }
//        }
//        data.remove(self);
        return data;
    }

}
