package com.zxwl.frame.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zxwl.frame.bean.ResponeBean;
import com.zxwl.frame.rx.RxBus;
import com.zxwl.frame.utils.Messages;

public class HeadsetReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("state")) {
            if (0 == intent.getIntExtra("state", 0)) {
                ResponeBean responeBean = new ResponeBean();
                responeBean.command = Messages.HEADSET_PLUG;
                responeBean.message = "1";
                RxBus.getInstance().post(responeBean);

//                Toast.makeText(context, "耳机未插入", Toast.LENGTH_SHORT).show();
            } else if (1 == intent.getIntExtra("state", 0)) {

                ResponeBean responeBean = new ResponeBean();
                responeBean.command = Messages.HEADSET_PLUG;
                responeBean.message = "0";
                RxBus.getInstance().post(responeBean);

//                Toast.makeText(context, "耳机已插入", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
