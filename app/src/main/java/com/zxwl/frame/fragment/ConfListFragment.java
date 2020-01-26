package com.zxwl.frame.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.huawei.opensdk.sdkwrapper.login.LoginCenter;
import com.zxwl.commonlibrary.BaseLazyFragment;
import com.zxwl.commonlibrary.utils.DialogUtils;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.frame.R;
import com.zxwl.frame.activity.ConvokeConfNewActivity;
import com.zxwl.frame.activity.HistoryConfActivity;
import com.zxwl.frame.adapter.ConfAdapter;
import com.zxwl.frame.inter.HuaweiCallImp;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.utils.Constant;
import com.zxwl.frame.widget.SelectCreateDialog;
import com.zxwl.network.ApiUrls;
import com.zxwl.network.api.ConfApi;
import com.zxwl.network.bean.ConfBean;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.RequestBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 会议列表fragment
 */
public class ConfListFragment extends BaseLazyFragment {
    private RecyclerView rvContent;
    private ConfAdapter confAdapter;
    private ImageView ivRightOperate;
    private ImageView ivBackOperate;
    private TextView tvRightOperate;

    private Timer timer;
    private TimerTask task;
    private List<ConfBean.DataBean> confList;

    public static final String NAME = "NAME";

    public ConfListFragment() {
    }

    public static ConfListFragment newInstance(String name) {
        ConfListFragment fragment = new ConfListFragment();
        Bundle args = new Bundle();
        args.putString(NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    public static ConfListFragment newInstance() {
        ConfListFragment fragment = new ConfListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View inflateContentView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_conf_list, container, false);
    }

    @Override
    protected void findViews(View view) {
        rvContent = (RecyclerView) view.findViewById(R.id.rv_content);
        ivBackOperate = (ImageView) view.findViewById(R.id.iv_back_operate);
        ivBackOperate.setVisibility(View.VISIBLE);

        ivRightOperate = (ImageView) view.findViewById(R.id.iv_right_operate);
        ivRightOperate.setImageResource(R.mipmap.ic_home_more);
        ivRightOperate.setVisibility(View.GONE);

        tvRightOperate = (TextView) view.findViewById(R.id.tv_right_operate);
        tvRightOperate.setVisibility(View.VISIBLE);
        tvRightOperate.setText("历史会议");
        tvRightOperate.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));

        //初始化标题栏
        ((TextView) view.findViewById(R.id.tv_top_title)).setText("我的会议");

        initRecycler();

        update();
    }

    private SelectCreateDialog selectCreateDialog;

    @Override
    protected void addListeners() {
        ivRightOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCreateDialog = new SelectCreateDialog(getActivity());
                selectCreateDialog.setOnClickLis(new SelectCreateDialog.onClickLis() {
                    @Override
                    public void onClick(int pos) {
                        switch (pos) {
                            case SelectCreateDialog.CREATE_GROUP:
//                                CreateGroupActivity.startActivity(getActivity());
                                ConvokeConfNewActivity.startActivity(getActivity(), "创建群组");
                                selectCreateDialog.dismiss();
                                break;

                            case SelectCreateDialog.CREATE_AUDIO:
                                if (Constant.isholdCall) {
                                    ToastUtil.showLongToast(getActivity(), "当前处于会议中，无法召集会议");
                                    return;
                                }
//                                ConvokeConfActivity.startActivity(getContext(), true);
                                ConvokeConfNewActivity.startActivity(getContext(), true);
                                selectCreateDialog.dismiss();
                                break;

                            case SelectCreateDialog.CREATE_VIDEO:
                                if (Constant.isholdCall) {
                                    ToastUtil.showLongToast(getActivity(), "当前处于会议中，无法召集会议");
                                    return;
                                }
//                                ConvokeConfActivity.startActivity(getContext(), false);
                                ConvokeConfNewActivity.startActivity(getContext(), false);
                                selectCreateDialog.dismiss();
                                break;
                        }
                    }
                });
                selectCreateDialog.setBackground(null);
                selectCreateDialog.showPopupWindow(ivRightOperate);
            }
        });

        ivBackOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        tvRightOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HistoryConfActivity.startActivity(getActivity());
//                showConfDetailsDialog();
            }
        });
    }

    public static final int CREATE_CODE = 102;

    public static final String CONF_BEAN = "CONF_BEAN";

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK == resultCode) {
            switch (requestCode) {
                case CREATE_CODE:
                    break;
            }
        }
    }

    @Override
    protected void initData() {
        DialogUtils.showProgressDialog(getActivity(), "正在获取列表....");
        getAllConfs(true);
    }

    public void update() {
        task = new TimerTask() {
            @Override
            public void run() {
                try {
                    getAllConfs(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        timer = new Timer();
        timer.schedule(task, 0, 30000);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        task.cancel();
        timer.cancel();
        timer = null;
        task = null;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            //相当于Fragment的onPause
        } else {
            // 相当于Fragment的onResume
            getAllConfs(false);
        }
    }

    private void getAllConfs(boolean cancel) {
        try {
            String account = LoginCenter.getInstance().getAccount();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("siteUri", account);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            RequestBody body = RequestBody.create(ApiUrls.MEDIA_TYPE, jsonObject.toString());

            //变更初始状态
            View view = getActivity().getLayoutInflater().inflate(R.layout.layout_empty, (ViewGroup) rvContent.getParent(), false);
            confAdapter.setEmptyView(view);

            HttpUtils.getInstance(getActivity())
                    .getRetofitClinet()
//                .setBaseUrl("http://192.168.20.133:8090/videoConf/")
                    .setBaseUrl(Urls.BASE_URL)
                    .builder(ConfApi.class)
                    .getAllConf(body)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new RxSubscriber<ConfBean>() {
                        @Override
                        public void onSuccess(ConfBean baseData) {
                            if (cancel) {
                                DialogUtils.dismissProgressDialog(getActivity());
                            }
                            if ("success".equals(baseData.msg)) {
                                if (baseData.data.size() > 0) {
//                                    initRecycler(baseData.data);
                                    confAdapter.replaceData(baseData.data);
                                } else {
                                    confAdapter.replaceData(new ArrayList<>());
                                    View view = getActivity().getLayoutInflater().inflate(R.layout.layout_empty, (ViewGroup) rvContent.getParent(), false);
                                    confAdapter.setEmptyView(view);
                                }
                            } else {
                                confAdapter.replaceData(new ArrayList<>());
                                View view = getActivity().getLayoutInflater().inflate(R.layout.layout_empty, (ViewGroup) rvContent.getParent(), false);
                                confAdapter.setEmptyView(view);
                            }
                        }

                        @Override
                        protected void onError(ResponeThrowable responeThrowable) {
                            if (cancel) {
                                Toast.makeText(getContext(), "请求失败,error：" + responeThrowable.getCause().toString(), Toast.LENGTH_SHORT).show();
                                DialogUtils.dismissProgressDialog(getActivity());
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initRecycler() {
        confAdapter = new ConfAdapter(R.layout.item_conf, new ArrayList<>());
        confAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                ConfBean.DataBean item = confAdapter.getItem(position);
                HuaweiCallImp.getInstance().joinConf(item.accessCode);
            }
        });

        rvContent.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvContent.setAdapter(confAdapter);
    }


}
