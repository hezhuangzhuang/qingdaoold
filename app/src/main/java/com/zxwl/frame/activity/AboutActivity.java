package com.zxwl.frame.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zxwl.commonlibrary.BaseActivity;
import com.zxwl.frame.R;
import com.zxwl.frame.utils.VersionUtils;

//TODO:1121修改代码
public class AboutActivity extends BaseActivity {
    private TextView tvTopTitle;
    private TextView tvVerCode;
    private ImageView ivBack;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void findViews() {
        tvTopTitle = (TextView) findViewById(R.id.tv_top_title);
        tvVerCode = (TextView) findViewById(R.id.tv_verCode);

        ivBack = (ImageView) findViewById(R.id.iv_back_operate);
        ivBack.setImageResource(R.mipmap.general_back_icon_white);

        ImageView back = (ImageView) findViewById(R.id.iv_back_operate);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void initData() {
        tvTopTitle.setText("关于我们");
        tvVerCode.setText("当前版本：" + VersionUtils.getVerName());
    }

    @Override
    protected void setListener() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_about;
    }
}
