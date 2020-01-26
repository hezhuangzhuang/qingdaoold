package com.zxwl.frame.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zxwl.frame.R;
import com.zxwl.network.bean.GroupBean;

import java.util.List;

/**
 */
public class GroupAdapter extends BaseQuickAdapter<GroupBean.DataBean, BaseViewHolder> {
    public GroupAdapter(int layoutResId, @Nullable List<GroupBean.DataBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, GroupBean.DataBean userBean) {
        helper.setText(R.id.tv_name, userBean.name);
        helper.addOnClickListener(R.id.rl_content);
    }
}
