package com.zxwl.frame.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.GsonBuilder;
import com.huawei.opensdk.sdkwrapper.login.LoginCenter;
import com.zxwl.commonlibrary.BaseActivity;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.ecsdk.common.UIConstants;
import com.zxwl.ecsdk.utils.IntentConstant;
import com.zxwl.frame.R;
import com.zxwl.frame.adapter.ChairSelectAdaper;
import com.zxwl.frame.bean.respone.ConfBeanRespone;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.utils.sharedpreferences.PreferencesHelper;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChairSelectActivity extends BaseActivity {
    private ImageView ivBack;
    private RecyclerView rvList;
    private ChairSelectAdaper adaper;
    private List<ConfBeanRespone.DataBean.SiteStatusInfoListBean> list;
    private String peerNumber;
    private String smcConfId;
    private Context context;

    public static void startActivity(Context context, String peerNumber, String smcConfId) {
        Intent intent = new Intent(context, ChairSelectActivity.class);
        intent.putExtra("peerNumber", peerNumber);
        intent.putExtra("smcConfId", smcConfId);
        context.startActivity(intent);
    }

    @Override
    protected void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back);
        rvList = (RecyclerView) findViewById(R.id.rv_list);
    }

    @Override
    protected void initData() {
        this.context = this;
        peerNumber = getIntent().getStringExtra("peerNumber");
        smcConfId = getIntent().getStringExtra("smcConfId");

        initAdapter();

        /**
         * 获取参会人列表
         */
        getPeopleList();
    }

    private void initAdapter() {
        adaper = new ChairSelectAdaper(R.layout.item_chair_select, new ArrayList<>());
        adaper.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                switch (view.getId()) {
                    case R.id.tv_setChair:
                        ConfBeanRespone.DataBean.SiteStatusInfoListBean statusInfoListBean = (ConfBeanRespone.DataBean.SiteStatusInfoListBean) adapter.getItem(position);
                        requestConfChair(statusInfoListBean);
                        break;
                }
            }
        });
        rvList.setLayoutManager(new LinearLayoutManager(context));
        rvList.setAdapter(adaper);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void setListener() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent videoConf = new Intent(IntentConstant.VIDEO_CONF_ACTIVITY_ACTION);
                videoConf.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(videoConf);
                finish();
            }
        });
    }

    private void getPeopleList() {
        RequestParams params = new RequestParams(Urls.BASE_URL + "conf/queryBySmcConfIdOrAccessCode");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        params.addQueryStringParameter("accessCode", peerNumber);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ConfBeanRespone baseData = new GsonBuilder().create().fromJson(result, ConfBeanRespone.class);
                List<ConfBeanRespone.DataBean.SiteStatusInfoListBean> siteStatusInfoList = baseData.data.siteStatusInfoList;
                //设置显示的list
                setShowList(siteStatusInfoList);
                //刷新适配器
                adaper.replaceData(siteStatusInfoList);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {}

            @Override
            public void onCancelled(CancelledException cex) {}

            @Override
            public void onFinished() {}
        });
    }

    /**
     * 剔除未在线会场和自己
     *
     * @param siteStatusInfoList
     */
    private void setShowList(List<ConfBeanRespone.DataBean.SiteStatusInfoListBean> siteStatusInfoList) {
        Iterator<ConfBeanRespone.DataBean.SiteStatusInfoListBean> iterator = siteStatusInfoList.iterator();
        while (iterator.hasNext()) {
            ConfBeanRespone.DataBean.SiteStatusInfoListBean next = iterator.next();
            if (next.siteUri.equals(getCurrentSiteUri()) || 2 != next.siteStatus) {
                iterator.remove();
            }
        }
    }

    /**
     * 指定主席
     *
     * @param statusInfoListBean
     */
    private void requestConfChair(ConfBeanRespone.DataBean.SiteStatusInfoListBean statusInfoListBean) {
        RequestParams params = new RequestParams(Urls.BASE_URL + "conf/forceSetConfChair");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        params.addQueryStringParameter("smcConfId", smcConfId);
        params.addQueryStringParameter("siteUri", statusInfoListBean.siteUri);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ConfBeanRespone baseData = new GsonBuilder().create().fromJson(result, ConfBeanRespone.class);
                if (null != baseData && 0 == baseData.code) {
                    ToastUtil.showShortToast(getApplicationContext(), "指定主席成功");
                    leaveConfRequest();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ToastUtil.showShortToast(getApplicationContext(), "ex" + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * 离开会议的网络请求
     */
    private void leaveConfRequest() {
        RequestParams params = new RequestParams(Urls.BASE_URL + "conf/disconnectSite");
        params.addHeader("Content-Type", "application/json");
        params.addHeader("Accept", "application/json");

        params.addQueryStringParameter("smcConfId", smcConfId);
        params.addQueryStringParameter("siteUri", getCurrentSiteUri());

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ConfBeanRespone baseData = new GsonBuilder().create().fromJson(result, ConfBeanRespone.class);

                if (null != baseData && 0 == baseData.code) {
                    finish();
                    PreferencesHelper.saveData(UIConstants.IS_CREATE, false);
                } else {
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

            @Override
            public void onCancelled(CancelledException cex) {
//                Toast.makeText(x.app(), "cancelled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFinished() {
            }
        });
    }

    private String getCurrentSiteUri() {
        return LoginCenter.getInstance().getSipAccountInfo().getTerminal();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chair_select;
    }
}
