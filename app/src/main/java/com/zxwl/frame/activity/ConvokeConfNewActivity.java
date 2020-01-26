package com.zxwl.frame.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import qdx.stickyheaderdecoration.NormalDecoration;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ConvokeConfNewActivity extends BaseActivity {
    private ImageView ivBackOperate;
    private TextView tvLeftOperate;
    private TextView tvTopTitle;
    private TextView tvRightOperate;
    private TextView tvName;
    private EditText etName;
    private EditText etSearch;
    private RadioGroup rgConfMode;

    private LinearLayout llAccessCode;
    private EditText etAccessCode;
    private RecyclerView rvContent;
    private ConstactsAdapter adapter;

    private boolean onlyVoice = false;

    public static final String GROUP_ID = "GROUP_ID";
    private int groupId = -1;

    public static final String TITLE = "TITLE";
    private String title;

    private TextView tvAllSelect;
    private TextView tvSelectNumber;
    private Context context;

    private View emptyView;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ConvokeConfNewActivity.class);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, String title) {
        Intent intent = new Intent(context, ConvokeConfNewActivity.class);
        intent.putExtra(TITLE, title);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, boolean onlyVoice) {
        Intent intent = new Intent(context, ConvokeConfNewActivity.class);
        intent.putExtra("onlyVoice", onlyVoice);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, boolean onlyVoice, int groupId) {
        Intent intent = new Intent(context, ConvokeConfNewActivity.class);
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

        tvAllSelect = (TextView) findViewById(R.id.tv_all_select);
        tvSelectNumber = (TextView) findViewById(R.id.tv_select_number);
        tvName = (TextView) findViewById(R.id.tv_name);
        rgConfMode = (RadioGroup) findViewById(R.id.rg_confMode);
        etSearch = (EditText) findViewById(R.id.et_search);

        emptyView = View.inflate(this, R.layout.layout_empty, null);


        llAccessCode = (LinearLayout) findViewById(R.id.ll_access_code);
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
//            tvRightOperate.setText("召集语音会议");
            tvRightOperate.setText("召集会议");
            rgConfMode.check(R.id.rb_voice);
//            tvRightOperate.setTextColor(ContextCompat.getColor(this, R.color.color_418AD5));
        } else {
            tvRightOperate.setVisibility(View.VISIBLE);
//            tvRightOperate.setText("召集视频会议");
            tvRightOperate.setText("召集会议");
            rgConfMode.check(R.id.rb_video);

//            tvRightOperate.setTextColor(ContextCompat.getColor(this, R.color.color_418AD5));
        }

        title = getIntent().getStringExtra(TITLE);
        if (!TextUtils.isEmpty(title)) {
            tvTopTitle.setVisibility(View.VISIBLE);
            tvTopTitle.setText(title);
//            tvTopTitle.setTextColor(ContextCompat.getColor(this, R.color.color_333));

            tvRightOperate.setVisibility(View.VISIBLE);
            tvRightOperate.setText("创建群组");
            tvName.setText("群组名称");
            rgConfMode.setVisibility(View.GONE);
            llAccessCode.setVisibility(View.GONE);
//            tvRightOperate.setTextColor(ContextCompat.getColor(this, R.color.color_418AD5));
        } else {
            llAccessCode.setVisibility(View.VISIBLE);
//            etName.setHint(DateUtil.getCurrentTime(DateUtil.FORMAT_DATE_TIME)+"(默认显示)");
            etName.setHint(DateUtil.getCurrentTime(DateUtil.FORMAT_DATE_TIME));
        }

        initRecycler(new ArrayList<>());

        DialogUtils.showProgressDialog(this, "正在获取列表....");

        search("");
//        if (-1 == groupId) {
//            getAllConstacts();
//        } else {
//            getByDepIdConstacts(groupId);
//        }
    }

    //list是否为空
    private boolean isListEmpty = false;

    private void getAllConstacts(String keyword) {
        HttpUtils.getInstance(this)
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ConfApi.class)
                .getConstacts(keyword)
                .retryWhen(new RetryWithDelay(3, 500))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData<PeopleBean>>() {
                    @Override
                    public void onSuccess(BaseData<PeopleBean> baseData) {
                        DialogUtils.dismissProgressDialog(ConvokeConfNewActivity.this);
                        if (BaseData.SUCCESS == baseData.responseCode) {
                            if (null != adapter) {
                                for (int i = 0; i < baseData.data.size(); i++) {
                                    baseData.data.get(i).hearRes = Constant.getPersonalHeadRes();
                                }
                                //同步勾选状态
                                List<PeopleBean> removeList = new ArrayList<>();
                                for (PeopleBean select : selectUser) {
                                    for (PeopleBean data : baseData.data) {
                                        if (select.sip.equals(data.sip)) {
                                            data.isCheck = true;
                                        }
                                    }
                                }
                                adapter.replaceData(removeSelf(baseData.data));

                                if (null != decoration) {
                                    //移除之前的decoration
                                    decoration.onDestory();
                                    rvContent.removeItemDecoration(decoration);
                                }

                                if (baseData.data.size() > 0) {
                                    isListEmpty = false;
                                    decoration = new NormalDecoration() {
                                        @Override
                                        public String getHeaderName(int pos) {
                                            return adapter.getItem(pos).firstLetter;
                                        }
                                    };
                                    rvContent.addItemDecoration(decoration);
                                } else {
                                    isListEmpty = true;
                                    adapter.setEmptyView(emptyView);
                                }
                            } else {
                                initRecycler(removeSelf(baseData.data));
                            }
                        } else {
                            ToastUtil.showShortToast(App.getContext(), "获取失败");
                        }
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
//                        Toast.makeText(getApplication(), "请求失败,error：" + responeThrowable.getCause().toString(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplication(), "网络异常" + responeThrowable.getMessage(), Toast.LENGTH_SHORT).show();
                        DialogUtils.dismissProgressDialog(ConvokeConfNewActivity.this);
                    }
                });
    }

    @Override
    protected void setListener() {
        rgConfMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                etName.setHint(DateUtil.getCurrentTime(DateUtil.FORMAT_DATE_TIME));
            }
        });
        tvAllSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isListEmpty) {
                    return;
                }
                isAllSelect = !isAllSelect;
                //设置列表的状态
                setListCheckStatus();
                //设置已选会场的数量
                setSelcetNumber();
                adapter.notifyDataSetChanged();
            }
        });

        tvSelectNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectPeopleActivity.startActivity(ConvokeConfNewActivity.this, selectUser);
            }
        });
        tvLeftOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvRightOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etName.getText()) && tvName.getText().toString().equals("群组名称")) {
                    ToastUtil.showShortToast(ConvokeConfNewActivity.this, "请输入名称");
                    return;
                }
                //创建群组
                if (!TextUtils.isEmpty(title)) {
                    if (selectUser.size() <= 0) {
                        Toast.makeText(ConvokeConfNewActivity.this, "请选择成员", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    StringBuffer stringBuffer = new StringBuffer();

                    getSelectUserSipOrId(stringBuffer, true);

                    HttpUtils.getInstance(ConvokeConfNewActivity.this)
                            .getRetofitClinet()
                            .setBaseUrl(Urls.logicServerURL)
                            .builder(ConfApi.class)
                            .newcreateGroup(ApiUrls.NEW_CREATE_GROUP, etName.getText().toString(), String.valueOf(Constant.CurrID), stringBuffer.toString() + Constant.CurrID)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new RxSubscriber<BaseData_logicServer>() {
                                @Override
                                public void onSuccess(BaseData_logicServer baseData_logicServer) {
                                    if (baseData_logicServer.getResponseCode() == 1) {
                                        ToastUtil.showShortToast(ConvokeConfNewActivity.this, "群组创建成功");
                                        finish();
                                    } else {
                                        ToastUtil.showShortToast(ConvokeConfNewActivity.this, "群组创建失败，服务器报错");
                                    }
                                }

                                @Override
                                protected void onError(ResponeThrowable responeThrowable) {
                                    ToastUtil.showShortToast(ConvokeConfNewActivity.this, "群组创建失败，服务器异常");
                                }
                            });
                    return;
                }

                if (selectUser.size() <= 0) {
                    Toast.makeText(ConvokeConfNewActivity.this, "请选择参会列表", Toast.LENGTH_SHORT).show();
                    return;
                }

                StringBuffer stringBuffer = new StringBuffer();

                getSelectUserSipOrId(stringBuffer, false);

                if (rgConfMode.getCheckedRadioButtonId() == R.id.rb_voice) {
                    onlyVoice = true;
                } else {
                    onlyVoice = false;
                }
                String confName = etName.getText().toString().trim();
                if (TextUtils.isEmpty(confName)) {
                    confName = DateUtil.getCurrentTime(DateUtil.FORMAT_DATE_TIME);
                }
                HuaweiCallImp.getInstance().createConferenceNetWork(
                        confName,
                        "120",
                        stringBuffer.toString() + LoginCenter.getInstance().getAccount(),
                        "1",
                        etAccessCode.getText().toString().trim(),
                        onlyVoice ? 0 : 1);
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d("tagtag_swf", "beforeTextChanged--" + s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("tagtag_swf", "onTextChanged--" + s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("tagtag_swf", "afterTextChanged--" + s);
                search(s.toString());
            }
        });
    }

    /**
     * 关键字搜索
     *
     * @param keywords
     */
    private void search(String keywords) {
        if (-1 == groupId) {
            getAllConstacts(keywords);
        } else {
            getByDepIdConstacts(groupId, keywords);
        }
    }

    /**
     * 获取已选会场的字段
     *
     * @param stringBuffer
     * @param isCreateGroup true创建群组取id，false创建会议取sip
     */
    private void getSelectUserSipOrId(StringBuffer stringBuffer, boolean isCreateGroup) {
        for (PeopleBean p : selectUser) {
            stringBuffer.append(isCreateGroup ? p.id : p.sip).append(",");
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_convoke_conf;
    }

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

                setSelcetNumber();

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

    private void getByDepIdConstacts(int id, String keywords) {
        HttpUtils.getInstance(this)
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ConfApi.class)
                .SearchGroupIdConstacts(id, keywords)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData<PeopleBean>>() {
                    @Override
                    public void onSuccess(BaseData<PeopleBean> baseData) {
                        DialogUtils.dismissProgressDialog(ConvokeConfNewActivity.this);
                        if (BaseData.SUCCESS == baseData.responseCode) {
                            if (null != adapter) {
                                for (int i = 0; i < baseData.data.size(); i++) {
                                    baseData.data.get(i).hearRes = Constant.getPersonalHeadRes();
                                }
                                //同步勾选状态
                                List<PeopleBean> removeList = new ArrayList<>();
                                for (PeopleBean select : selectUser) {
                                    for (PeopleBean data : baseData.data) {
                                        if (select.sip.equals(data.sip)) {
                                            data.isCheck = true;
                                        }
                                    }
                                }
                                if (null != decoration) {
                                    //移除之前的decoration
                                    decoration.onDestory();
                                    rvContent.removeItemDecoration(decoration);
                                }
                                adapter.replaceData(removeSelf(baseData.data));

                                decoration = new NormalDecoration() {
                                    @Override
                                    public String getHeaderName(int pos) {
                                        return adapter.getItem(pos).firstLetter;
                                    }
                                };
                                rvContent.addItemDecoration(decoration);
                            } else {
//                                initRecycler(baseData.data);
                                initRecycler(removeSelf(baseData.data));
                            }
                        } else {
                            ToastUtil.showShortToast(App.getContext(), "获取失败");
                        }
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
//                        Toast.makeText(ConvokeConfNewActivity.this, "请求失败,error：" + responeThrowable.getCause().toString(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(ConvokeConfNewActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                        DialogUtils.dismissProgressDialog(ConvokeConfNewActivity.this);
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
        return data;
    }

    //已选的会场
    private Set<PeopleBean> selectUser = new HashSet<>();

    //是否全选
    private boolean isAllSelect = false;

    /**
     * 设置列表的选择状态
     */
    private void setListCheckStatus() {
        for (int i = 0, len = adapter.getItemCount(); i < len; i++) {
            PeopleBean item = adapter.getItem(i);
            item.isCheck = isAllSelect;
            if (isAllSelect) {
                selectUser.add(item);
            } else {
                selectUser.remove(item);
            }
        }
    }

    /**
     * 设置已选会场的数量
     */
    private void setSelcetNumber() {
        tvSelectNumber.setText("已选择：" + selectUser.size() + "人");

        //判断是否全选
        isAllSelect = selectUser.size() == adapter.getItemCount();
        tvAllSelect.setCompoundDrawablesRelativeWithIntrinsicBounds(isAllSelect ? R.mipmap.ic_no_login_select : R.mipmap.ic_no_login_un_select, 0, 0, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            selectUser = (Set<PeopleBean>) data.getSerializableExtra(SelectPeopleActivity.SELECT_PEOPLE);
            for (PeopleBean peopleBean : adapter.getData()) {
                if (selectUser.contains(peopleBean)) {
                    peopleBean.isCheck = true;
                } else {
                    peopleBean.isCheck = false;
                }
            }
            //设置列表的状态
            setSelcetNumber();
            adapter.notifyDataSetChanged();
        }
    }
}
