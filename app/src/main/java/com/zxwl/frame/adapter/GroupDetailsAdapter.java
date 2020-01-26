package com.zxwl.frame.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zxwl.frame.R;
import com.zxwl.network.bean.PeopleBean;

import java.util.List;

public class GroupDetailsAdapter extends BaseQuickAdapter<PeopleBean, BaseViewHolder> {
    public GroupDetailsAdapter(int layoutResId, @Nullable List<PeopleBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, PeopleBean item) {
        helper.setText(R.id.tv_name, item.name);
    }
}
