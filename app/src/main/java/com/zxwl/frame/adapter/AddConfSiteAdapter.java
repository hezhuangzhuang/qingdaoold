package com.zxwl.frame.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zxwl.frame.R;
import com.zxwl.network.bean.response.GroupUser;

import java.util.List;

/**
 * author：pc-20171125
 * data:2019/4/23 11:07
 */
public class AddConfSiteAdapter extends BaseQuickAdapter<GroupUser.DataBean, BaseViewHolder> {

    public AddConfSiteAdapter(int layoutResId, @Nullable List<GroupUser.DataBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, GroupUser.DataBean userBean) {
        if ("-1".equals(userBean.id)) {
            View view = helper.getView(R.id.holer);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.width = 0;
            view.setLayoutParams(params);

            helper.setGone(R.id.iv_check, false);
            helper.setText(R.id.tv_name, userBean.name);
        } else {
            View view = helper.getView(R.id.holer);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.width = 50;
            view.setLayoutParams(params);

            helper.setVisible(R.id.iv_check,true);
            helper.setText(R.id.tv_name, userBean.name);
            helper.setImageResource(R.id.iv_check, userBean.isCheck ? R.mipmap.ic_autologin_true : R.mipmap.ic_autologin_false);
        }
        helper.addOnClickListener(R.id.rl_content);
    }

//    public AddConfSiteAdapter(Context context, int layoutId, List<GroupUser.DataBean> datas) {
//        super(context, layoutId, datas);
//    }
//
//    @Override
//    protected void convert(ViewHolder holder, GroupUser.DataBean userBean, int position) {
//        holder.setText(R.id.tv_name, userBean.name);
//
//        holder.setVisible(R.id.iv_head_portrait,false);
//
//        holder.setVisible(R.id.iv_check, true);
//        holder.setImageResource(R.id.iv_check, userBean.isCheck ? R.mipmap.ic_no_login_select : R.mipmap.ic_no_login_un_select);
//
//        holder.setBackgroundRes(R.id.view_split, R.color.color_444);
//
//        holder.setTextColorRes(R.id.tv_name, R.color.white);
//
//        //如果是选择模式则有点击效果
//        holder.setOnClickListener(R.id.rl_content, new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (null != recyclerClick) {
//                    recyclerClick.onClick(position);
//                }
//            }
//        });
//    }
//
//    private onRecyclerClick recyclerClick;
//
//    public void setRecyclerClick(onRecyclerClick recyclerClick) {
//        this.recyclerClick = recyclerClick;
//    }
}

