package com.zxwl.network.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.Serializable;
import java.util.Objects;

/**
 * author：pc-20171125
 * data:2019/10/30 16:25
 */
public class PeopleBean implements MultiItemEntity, Serializable {

    public String id;
    public String sip;
    public String sipAccount;
    public String name;
    public String firstLetter;

    public boolean isCheck = false;
    public int hearRes;

    @Override
    public int getItemType() {
        return 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeopleBean that = (PeopleBean) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(sip, that.sip) &&
                Objects.equals(name, that.name) &&
                Objects.equals(firstLetter, that.firstLetter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sip, name, firstLetter);
    }
}
