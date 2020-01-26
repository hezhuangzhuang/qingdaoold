package com.zxwl.frame.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.opensdk.commonservice.common.LocContext;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.frame.R;
import com.zxwl.frame.bean.EventMsg;
import com.zxwl.frame.net.Urls;
import com.zxwl.network.api.ImApi;
import com.zxwl.network.bean.BaseData_logicServer;
import com.zxwl.network.callback.RxSubscriber;
import com.zxwl.network.exception.ResponeThrowable;
import com.zxwl.network.http.HttpUtils;

import org.greenrobot.eventbus.EventBus;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EditDialogActivity extends AppCompatActivity {
    private EditText etContent;
    private TextView btCancle;
    private TextView btConfirm;
    private Context context;
    private int groupID;
    private ImageView ivClearText;

    public static void startActivity(Context context, int groupId, String currName) {
        Intent intent = new Intent(LocContext.getContext(), EditDialogActivity.class);
        intent.putExtra("groupId", groupId);
        intent.putExtra("currName", currName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_dialog);

        context = this;

        groupID = getIntent().getIntExtra("groupId", -1);

        findViews();
        setListener();

        String currName = getIntent().getStringExtra("currName");
        etContent.setText(currName);
    }

    private void setListener() {
        View.OnClickListener commonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.bt_confirm:
                        //TODO 调用修改群名称接口
                        if (groupID != -1) {
                            String newName = etContent.getText().toString();

                            if (TextUtils.isEmpty(newName)) {
                                ToastUtil.showLongToast(getApplicationContext(), "群组名称不能为空");
                                return;
                            }
                            renameGroup(groupID, newName);
                        } else {
                            ToastUtil.showShortToast(context, "群组信息存在异常");
                        }

                        break;
                    case R.id.bt_cancle:
                        finish();
                        break;

                    case R.id.iv_clear_text:
                        etContent.setText("");
                        break;
                }
            }
        };
        btCancle.setOnClickListener(commonListener);
        btConfirm.setOnClickListener(commonListener);
        ivClearText.setOnClickListener(commonListener);
    }

    private void findViews() {
        etContent = (EditText) findViewById(R.id.et_content);
        btCancle =  (TextView) findViewById(R.id.bt_cancle);
        btConfirm = (TextView) findViewById(R.id.bt_confirm);

        ivClearText = (ImageView) findViewById(R.id.iv_clear_text);
    }

    private void renameGroup(int groupId, String newName) {
        HttpUtils.getInstance(context)
                .getRetofitClinet()
                .setBaseUrl(Urls.logicServerURL)
                .builder(ImApi.class)
                .reNameGroup(groupId, newName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscriber<BaseData_logicServer>() {
                    @Override
                    protected void onError(ResponeThrowable responeThrowable) {
                        ToastUtil.showShortToast(context, "网络异常");
                    }

                    @Override
                    public void onSuccess(BaseData_logicServer baseData_logicServer) {
                        if (baseData_logicServer.getResponseCode() == 1) {
                            ToastUtil.showShortToast(context, "修改群名称成功");
                            //更新群组名称
                            EventMsg eventMsg = new EventMsg();
                            eventMsg.setMsg(EventMsg.UPDATE_GROUP);
                            EventBus.getDefault().post(eventMsg);

                            //群名称修改成功，返回首页
                            MainActivity.startActivity(context);
                        } else {
                            ToastUtil.showShortToast(context, baseData_logicServer.getMessage());
                        }
                    }
                });
    }
}
