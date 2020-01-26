package com.zxwl.frame.adapter.item;

import com.chad.library.adapter.base.entity.SectionMultiEntity;
import com.zxwl.commonlibrary.widget.banner.BannerEntity;
import com.zxwl.frame.bean.HomeInfoBean;

import java.util.List;

/**
 * author：pc-20171125
 * data:2019/5/8 18:11
 */
public class HomeListItem extends SectionMultiEntity<HomeInfoBean> {
    public HomeInfoBean homeInfoBean;
    public List<BannerEntity> list;

    public HomeListItem(boolean isHeader, String header) {
        super(isHeader, header);
    }

    public HomeListItem(boolean isHeader, String header, List<BannerEntity> list) {
        super(isHeader, header);
        this.list = list;
    }

    public HomeListItem(HomeInfoBean homeInfoBean) {
        super(homeInfoBean);
        this.homeInfoBean = homeInfoBean;
    }

    @Override
    public int getItemType() {
        return homeInfoBean.type;
    }
}
