package com.zxwl.frame.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zxwl.frame.R;
import com.zxwl.network.bean.PeopleBean;

import java.util.List;

/**
 *
 */
public class ConstactsAdapter extends BaseQuickAdapter<PeopleBean, BaseViewHolder> {
    private boolean showCheck = false;

    public void setShowCheck(boolean showCheck) {
        this.showCheck = showCheck;
    }

    public ConstactsAdapter(int layoutResId, @Nullable List<PeopleBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, PeopleBean userBean) {
        helper.setText(R.id.tv_name, userBean.name);
        ImageView ivCheck = helper.getView(R.id.iv_check);

        //TODO:1121添加代码
        helper.setImageResource(R.id.iv_head_portrait, userBean.hearRes);

        ivCheck.setVisibility(showCheck ? View.VISIBLE : View.GONE);

        ivCheck.setImageResource(userBean.isCheck ? R.mipmap.ic_select_true : R.mipmap.ic_no_login_un_select);

    }
}
