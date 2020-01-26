package com.zxwl.frame.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zxwl.commonlibrary.utils.DateUtil;
import com.zxwl.frame.R;
import com.zxwl.frame.adapter.item.ChatItem;
import com.zxwl.frame.adapter.item.MultipleItem;
import com.zxwl.frame.utils.Constant;

import java.util.List;

/**
 * author：pc-20171125
 * data:2019/5/10 19:24
 */
public class HomeMessageAdapter extends BaseQuickAdapter<ChatItem, BaseViewHolder> {
    public HomeMessageAdapter(int layoutResId, @Nullable List<ChatItem> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ChatItem item) {
        helper.addOnClickListener(R.id.rl_message);
        helper.setText(R.id.tv_title, item.chatBean.conversationUserName);

        helper.setVisible(R.id.iv_un_read, !item.chatBean.isRead);
        helper.setText(R.id.tv_time, DateUtil.getTimeStringAutoShort2(item.chatBean.time, false));

        //TODO:1121添加代码
        if (item.chatBean.isGroup) {
            helper.setImageResource(R.id.iv_head, Constant.getGroupHeadRes());
        } else {
            helper.setImageResource(R.id.iv_head, Constant.getPersonalHeadRes());
        }

        switch (item.getItemType()) {
            //语音
            case MultipleItem.SEND_VOICE:
            case MultipleItem.FORM_VOICE:
                if (item.chatBean.textContent.endsWith(".voice") || item.chatBean.textContent.endsWith(".m4a")) {
                    helper.setText(R.id.tv_message, "[语音]");
                } else {
                    helper.setText(R.id.tv_message, "[文件]");
                }
                break;

            //图片
            case MultipleItem.SEND_IMG:
            case MultipleItem.FORM_IMG:
                helper.setText(R.id.tv_message, "[图片]");
                break;

            //文字
            case MultipleItem.SEND_TEXT:
            case MultipleItem.FORM_TEXT:
                helper.setText(R.id.tv_message, item.chatBean.textContent);
                break;

            //视频通话
            case MultipleItem.SEND_VIDEO_CALL:
            case MultipleItem.FORM_VIDEO_CALL:
                helper.setText(R.id.tv_message, "[视频通话]");
                break;

            //语音通话
            case MultipleItem.SEND_VOICE_CALL:
            case MultipleItem.FORM_VOICE_CALL:
                helper.setText(R.id.tv_message, "[语音通话]");
                break;
        }
    }

}
