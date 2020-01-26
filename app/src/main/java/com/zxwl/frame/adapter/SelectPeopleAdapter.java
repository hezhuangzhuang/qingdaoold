package com.zxwl.frame.adapter;

import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zxwl.frame.R;
import com.zxwl.network.bean.PeopleBean;

import java.util.List;

/**
 *  已选列表
 */
public class SelectPeopleAdapter extends BaseQuickAdapter<PeopleBean, BaseViewHolder> {
    public SelectPeopleAdapter(int layoutResId, @Nullable List<PeopleBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, PeopleBean userBean) {
        helper.setText(R.id.tv_name, userBean.name);
        ImageView ivCheck = helper.getView(R.id.iv_check);

        //TODO:1121添加代码
        helper.setImageResource(R.id.iv_head_portrait, userBean.hearRes);
        helper.addOnClickListener(R.id.iv_delete);
    }
}
