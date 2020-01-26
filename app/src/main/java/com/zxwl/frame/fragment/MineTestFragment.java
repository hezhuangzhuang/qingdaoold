package com.zxwl.frame.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zxwl.commonlibrary.BaseLazyFragment;
import com.zxwl.frame.R;

/**
 * 首页fragment
 */
public class MineTestFragment extends BaseLazyFragment {
    private TextView tvContent;

    public static final String NAME = "NAME";

    public MineTestFragment() {
    }

    public static MineTestFragment newInstance(String name) {
        MineTestFragment fragment = new MineTestFragment();
        Bundle args = new Bundle();
        args.putString(NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    public static MineTestFragment newInstance() {
        MineTestFragment fragment = new MineTestFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View inflateContentView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_study, container, false);
    }

    String name;

    @Override
    protected void findViews(View view) {
        tvContent = (TextView) view.findViewById(R.id.tv_content);

        Bundle arguments = getArguments();
        name = arguments.getString(NAME);
        tvContent.setText(name);
    }

    @Override
    protected void addListeners() {
    }

    @Override
    protected void initData() {
    }

}
