package com.zxwl.frame.activity;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zxwl.commonlibrary.BaseActivity;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.frame.R;
import com.zxwl.frame.net.Urls;
import com.zxwl.frame.utils.Constant;
import com.zxwl.frame.utils.PreferenceUtil;

public class ServerActivity extends BaseActivity {
    private EditText etsmcIP;
    private EditText etsmcPort;
    private EditText etbackserverIP;
    private EditText etbackserverPort;
    private EditText etwebsocketIP;
    private EditText etwebsocketPort;
    private EditText etlogicServer;
    private EditText etlogicServerPort;
    private Button btOK;
    private ImageView back;
    private TextView title;

    private Context context;

    @Override
    protected void findViews() {
        etsmcIP = (EditText) findViewById(R.id.et_smcIP);
        etsmcPort = (EditText) findViewById(R.id.et_smcPort);
        etbackserverIP = (EditText) findViewById(R.id.et_backserverIP);
        etbackserverPort = (EditText) findViewById(R.id.et_backserverPort);
        etwebsocketIP = (EditText) findViewById(R.id.et_websocketIP);
        etwebsocketPort = (EditText) findViewById(R.id.et_websocketPort);
        etlogicServer = (EditText) findViewById(R.id.et_logicServer);
        etlogicServerPort = (EditText) findViewById(R.id.et_logicServerPort);
        btOK = (Button) findViewById(R.id.bt_OK);
        back = (ImageView) findViewById(R.id.iv_back_operate);
        title = (TextView) findViewById(R.id.tv_top_title);
    }

    @Override
    protected void initData() {
        context = this;
        title.setText("服务器设置");
        back.setVisibility(View.VISIBLE);

        etsmcIP.setText(PreferenceUtil.getString(context, Constant.SMC_IP, "120.221.95.141"));
        etsmcPort.setText(PreferenceUtil.getString(context, Constant.SMC_PORT, "5060"));
        //文件服务器
        etbackserverIP.setText(PreferenceUtil.getString(context, "backserverIP", Urls.DEF_FILE_IP));
        etbackserverPort.setText(PreferenceUtil.getString(context, "backserverPort", Urls.DEF_FILE_PORT));
        //im服务器
        etwebsocketIP.setText(PreferenceUtil.getString(context, "websocketIP", Urls.DEF_WEBSOCKET_IP));
        etwebsocketPort.setText(PreferenceUtil.getString(context, "websocketPort", Urls.DEF_WEBSOCKET_PORT));
        //逻辑业务服务器
        etlogicServer.setText(PreferenceUtil.getString(context, "logicServer", Urls.DEF_WEBSOCKET_IP));
        etlogicServerPort.setText(PreferenceUtil.getString(context, "logicServerPort", Urls.DEF_WEBSOCKET_PORT));
    }

    @Override
    protected void setListener() {
        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String smcIP = etsmcIP.getText().toString().trim();
                String smcPort = etsmcPort.getText().toString().trim();
                String backserverIP = etbackserverIP.getText().toString().trim();
                String backserverPort = etbackserverPort.getText().toString().trim();
                String websocketIP = etwebsocketIP.getText().toString().trim();
                String websocketPort = etwebsocketPort.getText().toString().trim();
                String logicServer = websocketIP;
                String logicServerPort = websocketPort;
//                String logicServer = etlogicServer.getText().toString().trim();
//                String logicServerPort = etlogicServerPort.getText().toString().trim();

                if (TextUtils.isEmpty(smcIP) || TextUtils.isEmpty(smcPort) || TextUtils.isEmpty(backserverIP)
                        || TextUtils.isEmpty(backserverPort) || TextUtils.isEmpty(websocketIP) || TextUtils.isEmpty(websocketPort) ||
                        TextUtils.isEmpty(logicServer)) {
                    ToastUtil.showShortToast(context, "服务器参数不能为空");
                    return;
                }

                PreferenceUtil.put(context, Constant.SMC_IP, smcIP);
                PreferenceUtil.put(context, Constant.SMC_PORT, smcPort);
                PreferenceUtil.put(context, "backserverIP", backserverIP);
                PreferenceUtil.put(context, "backserverPort", backserverPort);
                PreferenceUtil.put(context, "websocketIP", websocketIP);
                PreferenceUtil.put(context, "websocketPort", websocketPort);
                PreferenceUtil.put(context, "logicServer", logicServer);
                PreferenceUtil.put(context, "logicServerPort", logicServerPort);

                finish();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_server;
    }
}
