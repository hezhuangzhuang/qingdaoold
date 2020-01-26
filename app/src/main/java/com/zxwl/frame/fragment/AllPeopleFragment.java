package com.zxwl.frame.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.huawei.opensdk.sdkwrapper.login.LoginCenter;
import com.zxwl.commonlibrary.BaseLazyFragment;
import com.zxwl.commonlibrary.utils.DialogUtils;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.frame.R;
import com.zxwl.frame.activity.GroupActivity;
import com.zxwl.frame.activity.MemberDetailsActivity;
import com.zxwl.frame.adapter.ConstactsAdapter;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.utils.Constant;
import com.zxwl.network.api.ConfApi;
import com.zxwl.network.bean.BaseData;
import com.zxwl.network.bean.PeopleBean;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;
import com.zxwl.network.rxfun.RetryWithDelay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import qdx.stickyheaderdecoration.NormalDecoration;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * 全部联系人
 */
public class AllPeopleFragment extends BaseLazyFragment {
    private RecyclerView rvContent;
    private ConstactsAdapter allAdapter;

    private LinearLayout llGroup;

    private View errorView;
    private View emptyView;

    public AllPeopleFragment() {
    }

    public static AllPeopleFragment newInstance() {
        AllPeopleFragment fragment = new AllPeopleFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View inflateContentView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_all_people, container, false);
    }

    @Override
    protected void findViews(View view) {
        rvContent = view.findViewById(R.id.rv_content);
        llGroup = view.findViewById(R.id.ll_group);
    }

    private NormalDecoration decoration;

    private void initAdapter() {
        allAdapter = new ConstactsAdapter(R.layout.item_constacts, new ArrayList<>());
        allAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                PeopleBean dataBean = (PeopleBean) adapter.getData().get(position);
                MemberDetailsActivity.startActivity(getActivity(), dataBean.sip, dataBean.name);
            }
        });
        rvContent.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvContent.setAdapter(allAdapter);
    }

    @Override
    protected void addListeners() {
        llGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupActivity.startActivity(getActivity());
            }
        });
    }

    @Override
    protected void initData() {
        errorView = View.inflate(getActivity(), R.layout.error_layout, null);
        errorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAllConstacts();
            }
        });

        emptyView = View.inflate(getActivity(), R.layout.empty_view, null);
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAllConstacts();
            }
        });

        initAdapter();

//        getAllConstacts();
        getConstacts();
    }

    private void getConstacts() {
        DialogUtils.showProgressDialog(getActivity(), "正在获取列表....");
        Observable.zip(
                findGovAccount(),
                findAllConstact(),
                new Func2<BaseData<PeopleBean>,
                        BaseData<PeopleBean>,
                        List<PeopleBean>>() {
                    @Override
                    public List<PeopleBean> call(BaseData<PeopleBean> govAccounts, BaseData<PeopleBean> allConstact) {
                        List<PeopleBean> allList = new ArrayList<>();
                        for (int i = 0, len = govAccounts.data.size(); i < len; i++) {
                            //govAccounts.data.get(i).firstLetter = "标注";
                            govAccounts.data.get(i).sip = govAccounts.data.get(i).sipAccount;
                        }
                        allList.addAll(govAccounts.data);
                        allList.addAll(allConstact.data);
                        return allList;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<List<PeopleBean>>() {
                    @Override
                    public void onSuccess(List<PeopleBean> newsBeans) {
                        DialogUtils.dismissProgressDialog(getActivity());
                        List<PeopleBean> data = newsBeans;
                        Iterator<PeopleBean> iterator = data.iterator();
                        while (iterator.hasNext()) {
                            PeopleBean next = iterator.next();
                            if (next.sip.equals(getCurrentSiteUri())) {
                                iterator.remove();
                            }
                        }
                        if (data.size() > 0) {
                            for (int i = 0; i < data.size(); i++) {
                                data.get(i).hearRes = Constant.getPersonalHeadRes();
                            }
                            decoration = new NormalDecoration() {
                                @Override
                                public String getHeaderName(int pos) {
                                    return data.get(pos).firstLetter;
                                }
                            };
                            rvContent.addItemDecoration(decoration);
                            allAdapter.replaceData(data);
                        } else {
                            allAdapter.setEmptyView(emptyView);
                        }
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        ToastUtil.showShortToast(getContext(),responeThrowable.getMessage());
                        DialogUtils.dismissProgressDialog(getActivity());
                    }
                });
    }

    private Observable<BaseData<PeopleBean>> findGovAccount() {
        return HttpUtils.getInstance(getActivity())
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ConfApi.class)
                .getGovAccounts()
                .retryWhen(new RetryWithDelay(3, 500));
    }

    private Observable<BaseData<PeopleBean>> findAllConstact() {
        return HttpUtils.getInstance(getActivity())
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ConfApi.class)
                .getAllConstacts()
                .retryWhen(new RetryWithDelay(3, 500));
    }

    private void getAllConstacts() {
        DialogUtils.showProgressDialog(getActivity(), "正在获取列表....");
        HttpUtils.getInstance(getActivity())
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
                        DialogUtils.dismissProgressDialog(getActivity());
                        if (BaseData.SUCCESS == baseData.responseCode) {
                            List<PeopleBean> data = baseData.data;
                            if (data.size() > 0) {
                                for (int i = 0; i < data.size(); i++) {
                                    data.get(i).hearRes = Constant.getPersonalHeadRes();
                                }
                                decoration = new NormalDecoration() {
                                    @Override
                                    public String getHeaderName(int pos) {
                                        return data.get(pos).firstLetter;
                                    }
                                };
                                rvContent.addItemDecoration(decoration);
                                allAdapter.replaceData(data);
                            } else {
                                allAdapter.setEmptyView(emptyView);
                            }
                        } else {
//                            ToastUtil.showShortToast(App.getContext(), "获取失败");
                            allAdapter.setEmptyView(errorView);
                        }
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        Toast.makeText(getContext(), "网络异常", Toast.LENGTH_LONG).show();
                        DialogUtils.dismissProgressDialog(getActivity());
                        allAdapter.setEmptyView(errorView);
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

    /**
     * 获取当前账号的号码
     *
     * @return
     */
    private String getCurrentSiteUri() {
        return LoginCenter.getInstance().getSipAccountInfo().getTerminal();
    }
}
