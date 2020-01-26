package com.zxwl.frame.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zxwl.frame.R;
import com.zxwl.network.bean.ConfBean;

import java.util.List;

/**
 * author：pc-20171125
 * data:2019/5/10 19:24
 * 会议列表
 */
public class ConfAdapter extends BaseQuickAdapter<ConfBean.DataBean, BaseViewHolder> {

    public ConfAdapter(int layoutResId, @Nullable List<ConfBean.DataBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ConfBean.DataBean item) {
        helper.addOnClickListener(R.id.tv_join);

        helper.setImageResource(R.id.iv_img, 3 == item.confStatus ? R.mipmap.ic_conf_3 : R.mipmap.ic_conf_more);

        helper.setText(R.id.tv_conf_name, item.confName);
        helper.setText(R.id.tv_time, item.beginTime);
//        helper.setText(R.id.tv_originator, "发起人:管理员");
//        helper.setText(R.id.tv_originator, "发起人:"+item.creatorUri);
        helper.setText(R.id.tv_originator, "发起人:"+item.creatorName);
        String attendee = getAttendee(item.siteStatusInfoList);
        helper.setText(R.id.tv_attendee, attendee);

        helper.setText(R.id.tv_join_code,item.accessCode);

        TextView tvJoin = helper.getView(R.id.tv_join);
        tvJoin.setVisibility(3 == item.confStatus ? View.VISIBLE : View.GONE);

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
