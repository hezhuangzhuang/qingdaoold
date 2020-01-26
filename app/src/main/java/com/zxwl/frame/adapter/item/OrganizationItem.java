package com.zxwl.frame.adapter.item;

import com.chad.library.adapter.base.entity.AbstractExpandableItem;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.zxwl.frame.adapter.OrganizationAdapter;
import com.zxwl.network.bean.OrganizationBean;
import com.zxwl.network.bean.PeopleBean;

/**
 * author：pc-20171125
 * data:2019/10/30 17:28
 */
public class OrganizationItem extends AbstractExpandableItem<PeopleBean> implements MultiItemEntity {
    public OrganizationBean organizationBean;

    public OrganizationItem(OrganizationBean organizationBean) {
        this.organizationBean = organizationBean;
    }

    @Override
    public int getLevel() {
        return OrganizationAdapter.TYPE_LEVEL_ORGANIZATION;
    }

    @Override
    public int getItemType() {
        return OrganizationAdapter.TYPE_LEVEL_ORGANIZATION;
    }
}
