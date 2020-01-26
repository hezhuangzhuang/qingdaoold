package com.zxwl.frame.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.zxwl.commonlibrary.BaseActivity;
import com.zxwl.frame.R;
import com.zxwl.frame.adapter.SelectPeopleAdapter;
import com.zxwl.frame.utils.Constant;
import com.zxwl.network.bean.PeopleBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SelectPeopleActivity extends BaseActivity {
    private TextView tvLeftOperate;
    private TextView tvTopTitle;
    private TextView tvRightOperate;
    private RecyclerView rvContent;

    public static final String SELECT_PEOPLE = "SELECT_PEOPLE";

    private SelectPeopleAdapter adapter;
    private Set<PeopleBean> selectPeople;

    public static void startActivity(Activity context, Set<PeopleBean> selectPeople) {
        Intent intent = new Intent(context, SelectPeopleActivity.class);
        intent.putExtra(SELECT_PEOPLE, (Serializable) selectPeople);
        context.startActivityForResult(intent, 0);
    }

    @Override
    protected void findViews() {
        tvLeftOperate = (TextView) findViewById(R.id.tv_left_operate);
        tvTopTitle = (TextView) findViewById(R.id.tv_top_title);
        tvRightOperate = (TextView) findViewById(R.id.tv_right_operate);
        rvContent = (RecyclerView) findViewById(R.id.rv_content);
    }

    @Override
    protected void initData() {
        tvLeftOperate.setVisibility(View.VISIBLE);
        tvLeftOperate.setText("取消");

        tvTopTitle.setVisibility(View.VISIBLE);
        tvTopTitle.setText("已选择");

        tvRightOperate.setVisibility(View.VISIBLE);
        tvRightOperate.setText("确定");

        selectPeople = (Set<PeopleBean>) getIntent().getSerializableExtra(SELECT_PEOPLE);

        initRecycler();
    }

    @Override
    protected void setListener() {
        tvLeftOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvRightOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(SELECT_PEOPLE, new HashSet<>(adapter.getData()));
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_select_people;
    }

    private void initRecycler() {
        for (PeopleBean p : selectPeople) {
            p.hearRes = Constant.getPersonalHeadRes();
        }

        adapter = new SelectPeopleAdapter(R.layout.item_select_people, new ArrayList<>(selectPeople));
        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (view.getId() == R.id.iv_delete) {
                    adapter.remove(position);
                }
            }
        });
        rvContent.setLayoutManager(new LinearLayoutManager(this));
        rvContent.setAdapter(adapter);
    }

}
