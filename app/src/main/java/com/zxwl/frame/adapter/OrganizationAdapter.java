package com.zxwl.frame.adapter;

import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.zxwl.frame.R;
import com.zxwl.frame.adapter.item.OrganizationItem;
import com.zxwl.frame.utils.Constant;
import com.zxwl.network.bean.PeopleBean;

import java.util.List;

/**
 * author：pc-20171125
 * data:2019/10/30 17:29
 */
public class OrganizationAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    public static final int TYPE_LEVEL_ORGANIZATION = 1;
    public static final int TYPE_LEVEL_PERSON = 2;

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    public OrganizationAdapter(List<MultiItemEntity> data) {
        super(data);

        addItemType(TYPE_LEVEL_ORGANIZATION, R.layout.item_organization);

        addItemType(TYPE_LEVEL_PERSON, R.layout.item_constacts);
    }

    @Override
    protected void convert(BaseViewHolder helper, MultiItemEntity item) {
        switch (helper.getItemViewType()) {
            case TYPE_LEVEL_ORGANIZATION:
                OrganizationItem organizationItem = (OrganizationItem) item;
                String content = organizationItem.organizationBean.depName + "(" + organizationItem.organizationBean.count + ")";
                helper.setText(R.id.tv_content, content)
                        .setImageResource(R.id.iv, organizationItem.isExpanded() ? R.mipmap.arrow_b : R.mipmap.arrow_r);

                helper.setOnClickListener(R.id.ll_root, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (organizationItem.organizationBean.count > 0) {
                            int pos = helper.getAdapterPosition();
                            Log.d(TAG, "Level 1 item pos: " + pos);
//                            if (organizationItem.isExpanded()) {
//                                collapse(pos, false);
//                            } else {
//                                expand(pos, false);
//                            }
                            //展开状态
                            if (organizationItem.isExpanded()) {
                                if (null != childClick) {
                                    childClick.onCollapseClick(pos, organizationItem.organizationBean.depId);
                                }
                            } else {
                                if (null != childClick) {
                                    childClick.onExpandClick(pos, organizationItem.organizationBean.depId);
                                }
                            }
                        }
                    }
                });
                break;

            case TYPE_LEVEL_PERSON:
                PeopleBean peopleBean = (PeopleBean) item;

                //TODO:1121添加代码
                helper.setImageResource(R.id.iv_head_portrait, Constant.getPersonalHeadRes());

                helper.setText(R.id.tv_name, peopleBean.name);
                helper.setOnClickListener(R.id.rl_content, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != childClick) {
                            childClick.onPersonClick(peopleBean);
                        }
                    }
                });
                break;
        }
    }

    public interface onChildClick {
        void onCollapseClick(int pos, int depId);

        void onExpandClick(int pos, int depId);

        void onPersonClick(PeopleBean peopleBean);
    }

    private onChildClick childClick;

    public void setChildClick(onChildClick childClick) {
        this.childClick = childClick;
    }
}
