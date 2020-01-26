package com.zxwl.frame.widget;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.zxwl.frame.R;
import com.zxwl.network.bean.HistoryConfBean;

import razerdp.basepopup.BasePopupWindow;

/**
 * author：Administrator
 * data:2019/12/25 14:49
 */
public class HistoryConfDialog extends BasePopupWindow implements View.OnClickListener {
    private TextView tvClose;
    private TextView tvConfName;
    private TextView tvOriginator;
    private TextView tvTime;
    private TextView tvAttendee;

    private HistoryConfBean.DataBean dataBean;

    public HistoryConfDialog(Context context, int width, int height, HistoryConfBean.DataBean dataBean) {
        super(context, width, height);
        this.dataBean = dataBean;

        tvClose = (TextView) findViewById(R.id.tv_close);
        tvConfName = (TextView) findViewById(R.id.tv_conf_name);
        tvOriginator = (TextView) findViewById(R.id.tv_originator);
        tvTime = (TextView) findViewById(R.id.tv_time);
        tvAttendee = (TextView) findViewById(R.id.tv_attendee);

        this.dataBean = dataBean;

        tvConfName.setText(dataBean.confName);
        tvOriginator.setText(dataBean.creatorUri);
        tvTime.setText(dataBean.createTime);
        tvAttendee.setText(dataBean.sitesName);

        tvClose.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        dismiss();
    }

    @Override
    public View onCreateContentView() {
        return createPopupById(R.layout.bottom_dialog_history_conf);
    }

    public void setData(HistoryConfBean.DataBean dataBean) {
        this.dataBean = dataBean;
        tvConfName.setText(dataBean.confName);
        tvOriginator.setText(dataBean.creatorUriName);
        tvTime.setText(dataBean.createTime);
        tvAttendee.setText(dataBean.sitesName);
    }
}
