package com.zxwl.frame.activity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.zxwl.commonlibrary.AppManager;
import com.zxwl.ecsdk.common.UIConstants;
import com.zxwl.frame.R;
import com.zxwl.frame.utils.sharedpreferences.PreferencesHelper;

/**
 * 召集会议
 */
public class LoadingActivity extends BaseMediaActivity {
    public static final String CONF_NAME = "CONF_NAME";

    private TextView tvName;
    private ImageView ivLoading;

    public static void startActivty(Context context, String confName) {
        Intent intent = new Intent(context, LoadingActivity.class);
        intent.putExtra(CONF_NAME, confName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }

    @Override
    protected void findViews() {
        tvName = (TextView) findViewById(R.id.tv_name);
        ivLoading = (ImageView) findViewById(R.id.iv_loading);
    }

    private ObjectAnimator ra = null;

    @Override
    protected void initData() {
        String confName = getIntent().getStringExtra(CONF_NAME);
        tvName.setText(confName);

        ra = ObjectAnimator.ofFloat(ivLoading, "rotation", 0f, 360f);
        ra.setDuration(1500);
        ra.setRepeatCount(ObjectAnimator.INFINITE);
        ra.setInterpolator(new LinearInterpolator());
        ra.start();

        new Handler()
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 15 * 1000);

        AppManager.getInstance().finishActivity(ConvokeConfNewActivity.class);
    }

    @Override
    protected void setListener() {
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_loading;
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (null != ra) {
            ra.cancel();
        }

        //自动接听改为false
        PreferencesHelper.saveData(UIConstants.IS_AUTO_ANSWER, false);
        PreferencesHelper.saveData(UIConstants.JOIN_CONF, false);
    }
}
