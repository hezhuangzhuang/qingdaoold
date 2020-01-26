package com.zxwl.frame.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.opensdk.callmgr.CallMgr;
import com.zxwl.ecsdk.common.UIConstants;
import com.zxwl.frame.R;
import com.zxwl.frame.utils.AppManager;
import com.zxwl.frame.utils.sharedpreferences.PreferencesHelper;

/**
 * 来电显示界面
 */
public class CallerIDActivity extends BaseMediaActivity implements View.OnClickListener {
    private ImageView ivAvatar;
    private TextView tvNumber;
    private TextView tvHangUp;
    private TextView tvAnswer;

    @Override
    protected void findViews() {
        ivAvatar = (ImageView) findViewById(R.id.iv_avatar);
        tvNumber = (TextView) findViewById(R.id.tv_number);
        tvHangUp = (TextView) findViewById(R.id.tv_hang_up);
        tvAnswer = (TextView) findViewById(R.id.tv_answer);

        //是否移动端创建会议填false
        PreferencesHelper.saveData(UIConstants.IS_CREATE, false);
    }

    @Override
    protected void setListener() {
        tvHangUp.setOnClickListener(this);
        tvAnswer.setOnClickListener(this);

        tvNumber.setText(TextUtils.isEmpty(String.valueOf(mCallNumber)) ? "" : String.valueOf(mCallNumber));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_caller_id;
    }

    @Override
    public void onClick(View v) {
        if (R.id.tv_hang_up == v.getId()) {
            hangUp();
        } else if (R.id.tv_answer == v.getId()) {
            answer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean isFristClick = true;

    /**
     * 接听
     */
    private void answer() {
        if (isFristClick){
            CallMgr.getInstance().answerCall(mCallID, mIsVideoCall);
        }
    }

    /**
     * 挂断
     */
    private void hangUp() {
        //结束掉等待的对话框
        AppManager.getInstance().finishActivity(LoadingActivity.class);
        CallMgr.getInstance().endCall(mCallID);
        CallMgr.getInstance().stopPlayRingBackTone();
        CallMgr.getInstance().stopPlayRingingTone();
        finish();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        hangUp();
    }
}
