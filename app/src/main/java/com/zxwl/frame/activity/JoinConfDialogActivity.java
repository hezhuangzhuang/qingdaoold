package com.zxwl.frame.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.huawei.opensdk.commonservice.common.LocContext;
import com.zxwl.frame.R;
import com.zxwl.frame.inter.HuaweiCallImp;

public class JoinConfDialogActivity extends AppCompatActivity {
    private EditText etAccessCode;
    private Button btCancle;
    private Button btConfirm;
    private Context context;


    public static void startActivity(Context context) {
//        context.startActivity(new Intent(context, LoginDialogActivity.class));
        Intent intent = new Intent(LocContext.getContext(), JoinConfDialogActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        LocContext.getContext().startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_conf_dialog);

        context = this;

        findViews();

        setListener();
    }

    private void setListener() {
        View.OnClickListener commonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.bt_confirm:
                        HuaweiCallImp.getInstance().joinConf(etAccessCode.getText().toString());
//                        ToastUtil.showShortToast(context, etAccessCode.getText().toString());
                        break;
                    case R.id.bt_cancle:
                        finish();
                        break;
                }
            }
        };
        btCancle.setOnClickListener(commonListener);
        btConfirm.setOnClickListener(commonListener);
    }

    private void findViews() {
        etAccessCode = (EditText) findViewById(R.id.et_accessCode);
        btCancle = (Button) findViewById(R.id.bt_cancle);
        btConfirm = (Button) findViewById(R.id.bt_confirm);
    }

}
