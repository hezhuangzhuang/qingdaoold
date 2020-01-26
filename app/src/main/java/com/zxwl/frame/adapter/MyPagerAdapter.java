package com.zxwl.frame.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * author：pc-20171125
 * data:2019/5/9 13:41
 */
public class MyPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> mFragments;
    private String[] mTitles;


    public MyPagerAdapter(FragmentManager fm, List<Fragment> mFragments, String[] mTitles) {
        super(fm);
        this.mFragments = mFragments;
        this.mTitles = mTitles;
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

}
