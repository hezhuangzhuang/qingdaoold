package com.zxwl.frame.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.zxwl.commonlibrary.utils.DialogUtils;
import com.zxwl.frame.R;
import com.zxwl.frame.adapter.HistoryConfAdapter;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.utils.DensityUtil;
import com.zxwl.frame.widget.HistoryConfDialog;
import com.zxwl.network.api.ConfApi;
import com.zxwl.network.bean.HistoryConfBean;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;

import java.util.ArrayList;

import razerdp.basepopup.BasePopupWindow;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 历史会议
 */
public class HistoryConfActivity extends BaseLibActivity {
    private ImageView ivBackOperate;
    private TextView tvTopTitle;
    private SmartRefreshLayout refreshLayout;
    private RecyclerView rvList;

    private HistoryConfAdapter historyConfAdapter;

    private int pageNum;
    public static final int PAGE_SIZE = 10;

    private View emptyView;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, HistoryConfActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void findViews() {
        ivBackOperate = (ImageView) findViewById(R.id.iv_back_operate);
        tvTopTitle = (TextView) findViewById(R.id.tv_top_title);
        refreshLayout = (SmartRefreshLayout) findViewById(R.id.refresh_layout);
        rvList = (RecyclerView) findViewById(R.id.rv_list);
        emptyView = getLayoutInflater().inflate(R.layout.layout_empty, (ViewGroup) rvList.getParent(), false);

    }

    @Override
    protected void initData() {
        tvTopTitle.setText("历史会议");
        tvTopTitle.setVisibility(View.VISIBLE);

        ivBackOperate.setVisibility(View.VISIBLE);

        initAdapter();

    }

    @Override
    protected void setListener() {
        ivBackOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                getData(1);
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                getData(pageNum + 1);
            }
        });
        refreshLayout.autoRefresh();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_history_conf;
    }

    private void initAdapter() {
        historyConfAdapter = new HistoryConfAdapter(R.layout.item_history_conf, new ArrayList<>());
        historyConfAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
//                showConfDetailsDialog(historyConfAdapter.getItem(position));
                showHistoryDialog(historyConfAdapter.getItem(position));
            }
        });
        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.setAdapter(historyConfAdapter);
    }

    private void getData(int pageNum) {
        HttpUtils.getInstance(this)
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ConfApi.class)
                .getHistoryList(pageNum, PAGE_SIZE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<HistoryConfBean>() {
                    @Override
                    public void onSuccess(HistoryConfBean baseData) {
                        if (1 == baseData.responseCode) {
                            if (baseData.data.size() > 0) {
                                if (1 == pageNum) {
                                    setPageNum(1);
                                } else {
                                    setPageNum(pageNum);
                                }
                                if (1 == pageNum) {
                                    //刷新加载更多状态
                                    refreshLayout.resetNoMoreData();
                                    historyConfAdapter.replaceData(baseData.data);
                                } else {
                                    historyConfAdapter.addData(baseData.data);
                                }
                            } else {
                                if (1 == pageNum) {
                                    historyConfAdapter.replaceData(new ArrayList<>());
                                    historyConfAdapter.setEmptyView(emptyView);
                                } else {
                                    refreshLayout.finishLoadMoreWithNoMoreData();//设置之后，将不会再触发加载事件
                                }
                            }
                            //结束刷新
                            finishRefresh(pageNum, true);
                        } else {
                            historyConfAdapter.replaceData(new ArrayList<>());
                            historyConfAdapter.setEmptyView(emptyView);
                            //结束刷新
                            finishRefresh(pageNum, false);
                        }
                    }

                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        Toast.makeText(getApplicationContext(), "请求失败,error：" + responeThrowable.getCause().toString(), Toast.LENGTH_SHORT).show();
                        DialogUtils.dismissProgressDialog(HistoryConfActivity.this);
                    }
                });
    }

    /**
     * 设置页数
     *
     * @param pageNum
     */
    private void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    /**
     * 关闭动画
     *
     * @param pageNum
     */
    private void finishRefresh(int pageNum, boolean success) {
        if (1 == pageNum) {
            refreshLayout.finishRefresh(success);
            refreshLayout.setEnableFooterFollowWhenNoMoreData(true);
        } else {
            refreshLayout.finishLoadMore(success);
        }

        if (1 == pageNum && !success) {
            refreshLayout.setEnableLoadMore(false);
        }
    }

    /**
     * 会议详情的对话框
     */
    private BottomSheetDialog confDetailsDialog;
    private TextView tvClose;
    private TextView tvConfName;
    private TextView tvOriginator;
    private TextView tvTime;
    private TextView tvAttendee;

    private HistoryConfDialog historyConfDialog;

    private void showHistoryDialog(HistoryConfBean.DataBean dataBean) {
        if (null == historyConfDialog) {
            historyConfDialog = new HistoryConfDialog(
                    this,
                    DensityUtil.getScreenWidth(this),
                    BasePopupWindow.WRAP_CONTENT,
                    dataBean);
        } else {
            historyConfDialog.setData(dataBean);
        }

        historyConfDialog.setPopupGravity(Gravity.BOTTOM);
        historyConfDialog.showPopupWindow();
    }

    /**
     * 显示会议详情对话框
     */
    private void showConfDetailsDialog(HistoryConfBean.DataBean dataBean) {

        if (null == confDetailsDialog) {
            View view = null;
            confDetailsDialog = new BottomSheetDialog(this);
            //导入底部reycler布局
            view = LayoutInflater.from(this).inflate(R.layout.bottom_dialog_history_conf, null, false);
            confDetailsDialog.setContentView(view);
            tvClose = (TextView) view.findViewById(R.id.tv_close);
            tvConfName = (TextView) view.findViewById(R.id.tv_conf_name);
            tvOriginator = (TextView) view.findViewById(R.id.tv_originator);
            tvTime = (TextView) view.findViewById(R.id.tv_time);
            tvAttendee = (TextView) view.findViewById(R.id.tv_attendee);
        }

        tvConfName.setText(dataBean.confName);
        tvOriginator.setText(dataBean.creatorUri);
        tvTime.setText(dataBean.createTime);
        tvAttendee.setText(dataBean.sites);

        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != confDetailsDialog) {
                    confDetailsDialog.dismiss();
                }
            }
        });

        tvConfName.setText("会议名称");

//        BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) view.getParent());
//        //设置默认弹出高度为屏幕的0.4倍
////        mBehavior.setPeekHeight((int) (0.4 * height));
//        mBehavior.setPeekHeight((int) (height));

        if (!confDetailsDialog.isShowing()) {
            confDetailsDialog.show();
        } else {
            confDetailsDialog.dismiss();
        }
    }

}
