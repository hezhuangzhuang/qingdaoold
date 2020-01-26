package com.zxwl.frame.widget;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.zxwl.frame.R;

import razerdp.basepopup.BasePopupWindow;

/**
 * author：pc-20171125
 * data:2019/10/30 11:38
 */
public class SelectCreateDialog extends
        BasePopupWindow implements View.OnClickListener {
    private TextView tvCreateGroup;
    private TextView tvVideoConf;
    private TextView tvAudioConf;

    public SelectCreateDialog(Context context) {
        super(context);

        tvCreateGroup = (TextView) findViewById(R.id.tv_CreateGroup);
        tvVideoConf = (TextView) findViewById(R.id.tv_VideoConf);
        tvAudioConf = (TextView) findViewById(R.id.tv_AudioConf);

        tvCreateGroup.setOnClickListener(this);
        tvVideoConf.setOnClickListener(this);
        tvAudioConf.setOnClickListener(this);
    }

    @Override
    public View onCreateContentView() {
        return createPopupById(R.layout.pop_home_more);
    }

    public static final int CREATE_GROUP=0;
    public static final int CREATE_AUDIO =1;
    public static final int CREATE_VIDEO =2;

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_CreateGroup:
                if(null!=clickLis){
                    clickLis.onClick(CREATE_GROUP);
                }
                break;

            case R.id.tv_AudioConf:
                if(null!=clickLis){
                    clickLis.onClick(CREATE_AUDIO);
                }
                break;

            case R.id.tv_VideoConf:
                if(null!=clickLis){
                    clickLis.onClick(CREATE_VIDEO);
                }
                break;
        }
    }

    public interface onClickLis{
        void onClick(int pos);
    }

    private onClickLis clickLis;

    public void setOnClickLis(onClickLis onClickLis) {
        this.clickLis = onClickLis;
    }
}
