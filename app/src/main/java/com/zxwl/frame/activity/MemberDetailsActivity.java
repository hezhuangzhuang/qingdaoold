package com.zxwl.frame.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zxwl.commonlibrary.BaseActivity;
import com.zxwl.frame.R;
import com.zxwl.frame.inter.HuaweiCallImp;

/**
 * 成员详情
 */
public class MemberDetailsActivity extends BaseActivity implements View.OnClickListener {
    private TextView tvName;
    private TextView tvNumber;
    private ImageView ivBackOperate;
    private ImageView iv_head;
    private FrameLayout flSendMessage;
    private FrameLayout flSendVideo;
    private FrameLayout flSendAudio;

    private String userId;
    private String userName;

    public static final String USER_ID = "USER_ID";
    public static final String USER_NAME = "USER_NAME";

    private RelativeLayout rlTopTitle;

    public static void startActivity(Context context, String userId, String userName) {
        Intent intent = new Intent(context, MemberDetailsActivity.class);
        intent.putExtra(USER_ID, userId);
        intent.putExtra(USER_NAME, userName);
        context.startActivity(intent);
    }

    @Override
    protected void findViews() {
        ivBackOperate = (ImageView) findViewById(R.id.iv_back_operate);
        iv_head = (ImageView) findViewById(R.id.iv_head);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvNumber = (TextView) findViewById(R.id.tv_number);
        flSendMessage = (FrameLayout) findViewById(R.id.fl_send_message);
        flSendVideo = (FrameLayout) findViewById(R.id.fl_send_video);
        flSendAudio = (FrameLayout) findViewById(R.id.fl_send_audio);
        rlTopTitle = (RelativeLayout) findViewById(R.id.rl_top_title);
    }

    @Override
    protected void initData() {
        userId = getIntent().getStringExtra(USER_ID);
        userName = getIntent().getStringExtra(USER_NAME);

        tvNumber.setText(userId);
        tvName.setText(userName);

        rlTopTitle.setBackgroundColor(ContextCompat.getColor(this,R.color.white));
    }

    @Override
    protected void setListener() {
        ivBackOperate.setOnClickListener(this);
        ivBackOperate.setVisibility(View.VISIBLE);
        //TODO:1121修改代码
        ivBackOperate.setImageResource(R.mipmap.general_back_icon_black);
        flSendMessage.setOnClickListener(this);
        flSendVideo.setOnClickListener(this);
        flSendAudio.setOnClickListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_memberdetails;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back_operate:
                finish();
                break;

            case R.id.fl_send_message:
                ChatActivity.startActivity(MemberDetailsActivity.this, userId, userName, false);
                break;

            case R.id.fl_send_video:
                HuaweiCallImp.getInstance().callSite(userId,true);
                break;

            case R.id.fl_send_audio:
                HuaweiCallImp.getInstance().callSite(userId,false);
                break;
        }
    }
}
