package com.zxwl.testlibrary;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.alibaba.android.arouter.facade.annotation.Route;

@Route(path = "/path/seturl")
public class SetUrlActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //查找布局文件
        int id = getResources().getIdentifier("activity_set_url","layout",getPackageName());
        setContentView(R.layout.activity_set_url);

    }
}
