package com.zxwl.frame.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zxwl.frame.R;
import com.zxwl.network.bean.GroupNewBean;

import java.util.List;

/**
 * 所有群组适配器
 */
public class GroupNewAdapter extends BaseQuickAdapter<GroupNewBean, BaseViewHolder> {
    public GroupNewAdapter(int layoutResId, @Nullable List<GroupNewBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, GroupNewBean groupBean) {
        helper.setText(R.id.tv_name, groupBean.groupName);

        //TODO:1121添加代码
        helper.setImageResource(R.id.iv_head_portrait, groupBean.headRes);
        helper.setVisible(R.id.iv_createConf, true);
        helper.addOnClickListener(R.id.iv_createConf);
    }
}
