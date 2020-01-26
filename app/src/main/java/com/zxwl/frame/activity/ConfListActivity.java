package com.zxwl.frame.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;

import com.zxwl.commonlibrary.BaseActivity;
import com.zxwl.frame.R;
import com.zxwl.frame.fragment.ConfListFragment;

public class ConfListActivity extends BaseActivity {
    private FrameLayout contain;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ConfListActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void findViews() {
        contain = (FrameLayout) findViewById(R.id.contain);
    }

    @Override
    protected void initData() {
        ConfListFragment conflist = ConfListFragment.newInstance();

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(R.id.contain, conflist, "ConfListFragment");
        ft.commit();
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_conf_list;
    }

}
