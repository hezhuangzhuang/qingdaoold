package com.zxwl.frame.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.zxwl.commonlibrary.BaseLazyFragment;
import com.zxwl.commonlibrary.utils.DialogUtils;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.frame.App;
import com.zxwl.frame.R;
import com.zxwl.frame.activity.ChatActivity;
import com.zxwl.frame.adapter.GroupNewAdapter;
import com.zxwl.frame.bean.EventMsg;
import com.zxwl.frame.inter.HuaweiCallImp;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.utils.Constant;
import com.zxwl.frame.utils.DateUtil;
import com.zxwl.network.api.ConfApi;
import com.zxwl.network.bean.BaseData;
import com.zxwl.network.bean.GroupNewBean;
import com.zxwl.network.bean.PeopleBean;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * author：pc-20171125
 * data:2019/10/30 17:08
 */
public class GroupFragment extends BaseLazyFragment {
    private RecyclerView rvContent;

    private GroupNewAdapter groupAdapter;

    public static GroupFragment newInstance() {
        GroupFragment fragment = new GroupFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // 相当于onResume()方法
            getAllGroups();
        } else {
            // 相当于onpause()方法
        }
    }

    /**
     * 主线程中处理返回事件
     *
     * @param messageEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventMain(EventMsg messageEvent) {
        switch (messageEvent.getMsg()) {
            case EventMsg.UPDATE_GROUP:
                getAllGroups();
                break;
        }
    }

    @Override
    protected View inflateContentView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_organization, container, false);
    }

    @Override
    protected void findViews(View view) {
        rvContent = (RecyclerView) view.findViewById(R.id.rv_content);
    }

    private void initAdapter() {
        groupAdapter = new GroupNewAdapter(R.layout.item_constacts, new ArrayList<>());
        groupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                GroupNewBean item = groupAdapter.getItem(position);
                ChatActivity.startActivity(getActivity(), String.valueOf(item.id), item.groupName, true);
            }
        });
        groupAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                GroupNewBean item = groupAdapter.getItem(position);
                oneShoot2CreateConf(item.id);
            }
        });
        rvContent.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvContent.setAdapter(groupAdapter);
    }

    /**
     * 一键召开会议
     *
     * @param id
     */
    private void oneShoot2CreateConf(int id) {
        HttpUtils.getInstance(getActivity())
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ConfApi.class)
                .getGroupIdConstacts(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<BaseData<PeopleBean>, Observable<String>>() {
                    @Override
                    public Observable<String> call(BaseData<PeopleBean> baseData) {
                        StringBuilder builder = new StringBuilder();
                        if (BaseData.SUCCESS == baseData.responseCode) {
                            for (PeopleBean temp : baseData.data) {
                                builder.append(",");
                                builder.append(temp.sip);
                            }
                        } else {
                            ToastUtil.showShortToast(App.getContext(), "一键召集会议失败");
                        }
                        return Observable.just(builder.substring(1));
                    }
                })
                .subscribe(new RxSubscriber<String>() {
                    @Override
                    public void onSuccess(String s) {
                        HuaweiCallImp.getInstance().createConferenceNetWork(DateUtil.getCurrentTime(DateUtil.FORMAT_DATE_TIME), "120", s, String.valueOf(id), "", 1);
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        ToastUtil.showShortToast(App.getContext(), "一键召集会议失败");
                    }
                });
//                .subscribe(new RxSubscriber<BaseData<PeopleBean>>() {
//                    @Override
//                    public void onSuccess(BaseData<PeopleBean> baseData) {
//                        if (BaseData.SUCCESS == baseData.responseCode) {
//                            if (baseData.data.size() > 0) {
////                                initRecycler(baseData.data);
//                            }
//                        } else {
//                            ToastUtil.showShortToast(App.getContext(), "获取失败");
//                        }
//                    }
//
//                    @Override
//                    protected void onError(ResponeThrowable responeThrowable) {
//                        ToastUtil.showShortToast(LocContext.getContext(),"一键召集会议失败");
//                    }
//                });
    }

    @Override
    protected void addListeners() {
    }

    @Override
    protected void initData() {
        initAdapter();

        DialogUtils.showProgressDialog(getActivity(), "正在获取列表....");
        getAllGroups();
        EventBus.getDefault().register(this);
    }

    private void getAllGroups() {
        HttpUtils.getInstance(getActivity())
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ConfApi.class)
                .getSelfGroups(String.valueOf(Constant.CurrID))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData<GroupNewBean>>() {
                    @Override
                    public void onSuccess(BaseData<GroupNewBean> baseData) {
                        DialogUtils.dismissProgressDialog(getActivity());

                        if (BaseData.SUCCESS == baseData.responseCode) {
                            if (baseData.data.size() > 0) {
                                List<GroupNewBean> groupNewBeans = baseData.data;
                                for (int i = 0; i < groupNewBeans.size(); i++) {
                                    groupNewBeans.get(i).headRes = Constant.getGroupHeadRes();
                                }
                                groupAdapter.replaceData(groupNewBeans);
                            } else {
                                List<GroupNewBean> groupNewBeans = new ArrayList<>();
                                groupAdapter.replaceData(groupNewBeans);
                            }
                        } else {
                            ToastUtil.showShortToast(App.getContext(), "获取失败，群组列表可能为空");
                        }
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
//                        Toast.makeText(getContext(), "请求失败,error：" + responeThrowable.getCause().toString(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(getContext(), "网络异常", Toast.LENGTH_LONG).show();
                        DialogUtils.dismissProgressDialog(getActivity());
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
