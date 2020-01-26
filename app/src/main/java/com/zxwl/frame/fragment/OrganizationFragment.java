package com.zxwl.frame.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.zxwl.commonlibrary.BaseLazyFragment;
import com.zxwl.commonlibrary.utils.DialogUtils;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.frame.App;
import com.zxwl.frame.R;
import com.zxwl.frame.activity.MemberDetailsActivity;
import com.zxwl.frame.adapter.OrganizationAdapter;
import com.zxwl.frame.adapter.item.OrganizationItem;
import com.zxwl.frame.net.Urls;
import com.zxwl.network.api.ConfApi;
import com.zxwl.network.bean.BaseData;
import com.zxwl.network.bean.OrganizationBean;
import com.zxwl.network.bean.PeopleBean;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;
import com.zxwl.network.rxfun.RetryWithDelay;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * author：pc-20171125
 * data:2019/10/30 17:08
 */
public class OrganizationFragment extends BaseLazyFragment {
    private RecyclerView rvContent;

    private OrganizationAdapter organizationAdapter;

    public static OrganizationFragment newInstance() {
        OrganizationFragment fragment = new OrganizationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
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
        organizationAdapter = new OrganizationAdapter(new ArrayList<>());
        organizationAdapter.setChildClick(new OrganizationAdapter.onChildClick() {
            @Override
            public void onCollapseClick(int pos, int depId) {
                organizationAdapter.collapse(pos);
            }

            @Override
            public void onExpandClick(int pos, int depId) {
                getByDepIdConstacts(pos, depId);
            }

            @Override
            public void onPersonClick(PeopleBean peopleBean) {
                MemberDetailsActivity.startActivity(getActivity(), peopleBean.sip, peopleBean.name);
            }
        });
        rvContent.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvContent.setAdapter(organizationAdapter);
    }

    @Override
    protected void addListeners() {
    }

    @Override
    protected void initData() {
        initAdapter();

        DialogUtils.showProgressDialog(getActivity(), "正在获取列表....");
        getAllOrganizations();
    }

    private void getAllOrganizations() {
        HttpUtils.getInstance(getActivity())
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ConfApi.class)
                .getAllOrganizations()
                .retryWhen(new RetryWithDelay(3, 500))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData<OrganizationBean>>() {
                    @Override
                    public void onSuccess(BaseData<OrganizationBean> baseData) {
                        DialogUtils.dismissProgressDialog(getActivity());

                        if (BaseData.SUCCESS == baseData.responseCode) {
                            if (baseData.data.size() > 0) {
                                List<OrganizationBean> organizationBeanList = baseData.data;

                                ArrayList<MultiItemEntity> res = new ArrayList<>();

                                OrganizationItem organizationItem = null;
                                for (int i = 0; i < organizationBeanList.size(); i++) {
                                    organizationItem = new OrganizationItem(organizationBeanList.get(i));
                                    res.add(organizationItem);
                                }

                                organizationAdapter.replaceData(res);
                            }
                        } else {
                            ToastUtil.showShortToast(App.getContext(), "获取失败");
                        }
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
//                        Toast.makeText(getContext(), "请求失败,error：" + responeThrowable.getCause().toString(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(getContext(),"网络异常",Toast.LENGTH_LONG).show();
                        DialogUtils.dismissProgressDialog(getActivity());
                    }
                });
    }


    private void getByDepIdConstacts(int pos, int depId) {
        HttpUtils.getInstance(getActivity())
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ConfApi.class)
                .getDepIdConstacts(depId)
                .retryWhen(new RetryWithDelay(3, 500))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData<PeopleBean>>() {
                    @Override
                    public void onSuccess(BaseData<PeopleBean> baseData) {
                        DialogUtils.dismissProgressDialog(getActivity());
                        if (BaseData.SUCCESS == baseData.responseCode) {
                            if (baseData.data.size() > 0) {
                                List<PeopleBean> data = baseData.data;
                                OrganizationItem item = (OrganizationItem) organizationAdapter.getItem(pos);
                                if (null == item.getSubItems()) {
                                    for (int i = 0; i < data.size(); i++) {
                                        item.addSubItem(data.get(i));
                                    }
                                }
                                organizationAdapter.notifyDataSetChanged();
                                organizationAdapter.expand(pos);
                            }
                        } else {
                            ToastUtil.showShortToast(App.getContext(), "获取失败");
                        }
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
//                        Toast.makeText(getContext(), "请求失败,error：" + responeThrowable.getCause().toString(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(getContext(),"网络异常",Toast.LENGTH_LONG).show();
                        DialogUtils.dismissProgressDialog(getActivity());
                    }
                });
    }

}
