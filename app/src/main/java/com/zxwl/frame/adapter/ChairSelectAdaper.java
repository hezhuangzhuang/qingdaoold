package com.zxwl.frame.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zxwl.frame.R;
import com.zxwl.frame.bean.respone.ConfBeanRespone;

import java.util.List;

public class ChairSelectAdaper extends BaseQuickAdapter<ConfBeanRespone.DataBean.SiteStatusInfoListBean, BaseViewHolder> {
    public ChairSelectAdaper(int layoutResId, @Nullable List<ConfBeanRespone.DataBean.SiteStatusInfoListBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ConfBeanRespone.DataBean.SiteStatusInfoListBean item) {
        helper.setText(R.id.tv_displayName,item.siteName);
        helper.addOnClickListener(R.id.tv_setChair);
    }
}
