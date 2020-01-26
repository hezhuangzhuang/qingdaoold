package com.zxwl.frame.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zxwl.frame.R;
import com.zxwl.network.bean.ConfBean;
import com.zxwl.network.bean.HistoryConfBean;

import java.util.List;

/**
 * author：pc-20171125
 * data:2019/5/10 19:24
 * 会议列表
 */
public class HistoryConfAdapter extends BaseQuickAdapter<HistoryConfBean.DataBean, BaseViewHolder> {

    public HistoryConfAdapter(int layoutResId, @Nullable List<HistoryConfBean.DataBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, HistoryConfBean.DataBean item) {
        helper.addOnClickListener(R.id.tv_join);

        helper.setImageResource(R.id.iv_img, R.mipmap.ic_conf_3);

        helper.setText(R.id.tv_conf_name, item.confName);
        helper.setText(R.id.tv_time, item.createTime);
        helper.setText(R.id.tv_originator, "发起人:" + item.creatorUriName);
        helper.setText(R.id.tv_attendee, item.sites);

        TextView tvJoin = helper.getView(R.id.tv_join);
        tvJoin.setVisibility(View.GONE);

        helper.addOnClickListener(R.id.tv_join);
    }

    private String getAttendee(List<ConfBean.DataBean.SiteStatusInfoListBean> siteStatusInfoList) {
        StringBuilder attendee = new StringBuilder();
        for (ConfBean.DataBean.SiteStatusInfoListBean item : siteStatusInfoList) {
            attendee.append(item.siteName + "\t");
        }
        return attendee.toString();
    }
}
