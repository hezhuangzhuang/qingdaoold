package com.zxwl.frame.adapter;

import android.animation.ObjectAnimator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseSectionMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zxwl.commonlibrary.utils.DateUtil;
import com.zxwl.frame.R;
import com.zxwl.frame.adapter.item.ChatItem;
import com.zxwl.frame.adapter.item.MultipleItem;
import com.zxwl.frame.utils.Base64Utils;

import java.util.List;

/**
 * author：pc-20171125
 * data:2019/5/11 14:48
 */
public class ChatAdapter extends BaseSectionMultiItemQuickAdapter<ChatItem, BaseViewHolder> {
    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param sectionHeadResId The section head layout id for each item
     * @param data             A new list is created out of this one to avoid mutable list
     */
    public ChatAdapter(int sectionHeadResId, List<ChatItem> data) {
        super(sectionHeadResId, data);
        addItemType(MultipleItem.SEND_TEXT, R.layout.item_send_msg);
        addItemType(MultipleItem.FORM_TEXT, R.layout.item_from_msg);

        addItemType(MultipleItem.SEND_VOICE, R.layout.item_send_voice);
        addItemType(MultipleItem.FORM_VOICE, R.layout.item_from_voice);

        addItemType(MultipleItem.SEND_IMG, R.layout.item_send_img);
        addItemType(MultipleItem.FORM_IMG, R.layout.item_from_img);

        addItemType(MultipleItem.SEND_VIDEO_CALL, R.layout.item_send_msg);
        addItemType(MultipleItem.FORM_VIDEO_CALL, R.layout.item_from_msg);

        addItemType(MultipleItem.SEND_VOICE_CALL, R.layout.item_send_msg);
        addItemType(MultipleItem.FORM_VOICE_CALL, R.layout.item_from_msg);
    }

    @Override
    protected void convertHead(BaseViewHolder helper, ChatItem item) { }

    @Override
    protected void convert(BaseViewHolder helper, ChatItem item) {
        TextView tvContent = helper.getView(R.id.tv_content);

        switch (item.getItemType()) {
            case MultipleItem.SEND_TEXT:
                helper.setText(R.id.tv_name, item.chatBean.name);
                helper.setText(R.id.tv_time, DateUtil.getTimeStringAutoShort2(item.chatBean.time, true));
                helper.setText(R.id.tv_content, item.chatBean.textContent);
                break;

            case MultipleItem.FORM_TEXT:
                helper.setText(R.id.tv_name, item.chatBean.name);
                helper.setText(R.id.tv_time, DateUtil.getTimeStringAutoShort2(item.chatBean.time, true));
                helper.setText(R.id.tv_content, item.chatBean.textContent);
                break;

            //视频呼叫
            case MultipleItem.SEND_VIDEO_CALL:
                helper.setText(R.id.tv_name, item.chatBean.name);
                helper.setText(R.id.tv_time, DateUtil.getTimeStringAutoShort2(item.chatBean.time, true));
                helper.setText(R.id.tv_content, item.chatBean.textContent);
                tvContent.setCompoundDrawablesRelativeWithIntrinsicBounds(R.mipmap.ic_chat_send_video_call, 0, 0, 0);
                break;

            //视频呼叫
            case MultipleItem.FORM_VIDEO_CALL:
                helper.setText(R.id.tv_name, item.chatBean.name);
                helper.setText(R.id.tv_time, DateUtil.getTimeStringAutoShort2(item.chatBean.time, true));
                helper.setText(R.id.tv_content, item.chatBean.textContent);
                tvContent.setCompoundDrawablesRelativeWithIntrinsicBounds(R.mipmap.ic_chat_form_video_call, 0, 0, 0);
                break;

            //语音呼叫
            case MultipleItem.SEND_VOICE_CALL:
                helper.setText(R.id.tv_name, item.chatBean.name);
                helper.setText(R.id.tv_time, DateUtil.getTimeStringAutoShort2(item.chatBean.time, true));
                helper.setText(R.id.tv_content, item.chatBean.textContent);
                tvContent.setCompoundDrawablesRelativeWithIntrinsicBounds(R.mipmap.ic_chat_send_voice_call, 0, 0, 0);
                break;

            //语音呼叫
            case MultipleItem.FORM_VOICE_CALL:
                helper.setText(R.id.tv_name, item.chatBean.name);
                helper.setText(R.id.tv_time, DateUtil.getTimeStringAutoShort2(item.chatBean.time, true));
                helper.setText(R.id.tv_content, item.chatBean.textContent);
                tvContent.setCompoundDrawablesRelativeWithIntrinsicBounds(R.mipmap.ic_chat_form_voice_call, 0, 0, 0);
                break;

            case MultipleItem.SEND_IMG:
                helper.addOnClickListener(R.id.iv_voice);
                helper.setText(R.id.tv_name, item.chatBean.name);
                helper.setText(R.id.tv_time, DateUtil.getTimeStringAutoShort2(item.chatBean.time, true));
                Glide.with(mContext)
                        .load(item.chatBean.textContent)
                        .into((ImageView) helper.getView(R.id.iv_voice));
                break;

            case MultipleItem.FORM_IMG:
                helper.addOnClickListener(R.id.iv_voice);

                helper.setText(R.id.tv_name, item.chatBean.name);
                helper.setText(R.id.tv_time, DateUtil.getTimeStringAutoShort2(item.chatBean.time, true));
                Glide.with(mContext)
                        .load(item.chatBean.textContent)
                        .into((ImageView) helper.getView(R.id.iv_voice));
//                GlideImageLoader.setAutoSizeIMG((Activity) mContext, item.chatBean.textContent, (ImageView) helper.getView(R.id.iv_voice), 300);
                break;

            case MultipleItem.SEND_VOICE:
                helper.addOnClickListener(R.id.fl_voice);

                if (item.chatBean.textContent.endsWith(".voice") || item.chatBean.textContent.endsWith(".m4a")) {
//                    helper.setText(R.id.tv_duration, item.chatBean.duration);
                    helper.setImageResource(R.id.iv_voice, R.drawable.audio_animation_right_list);
                    helper.setText(R.id.tv_duration, "");
                    helper.setText(R.id.tv_name, item.chatBean.name);
                    helper.setText(R.id.tv_time, DateUtil.getTimeStringAutoShort2(item.chatBean.time, true));
                } else {
                    helper.setImageResource(R.id.iv_voice, R.mipmap.ic_file);
                    helper.setText(R.id.tv_name, item.chatBean.name);
                    String temp = item.chatBean.textContent;
                    String fileName = temp.substring(temp.lastIndexOf("/") + 1);
                    helper.setText(R.id.tv_duration, fileName);
                    helper.setText(R.id.tv_time, DateUtil.getTimeStringAutoShort2(item.chatBean.time, false));
                }
                break;

            case MultipleItem.FORM_VOICE:
                helper.addOnClickListener(R.id.fl_voice);
                if (item.chatBean.textContent.endsWith(".voice") || item.chatBean.textContent.endsWith(".m4a")) {
                    helper.setImageResource(R.id.iv_voice, R.drawable.audio_animation_right_list);
                    helper.setText(R.id.tv_duration, "");
                    helper.setText(R.id.tv_name, item.chatBean.name);
                    helper.setText(R.id.tv_time, DateUtil.getTimeStringAutoShort2(item.chatBean.time, false));
                    ImageView view = helper.getView(R.id.iv_voice);

                    ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", 0f, 180.0f);
                    animator.setDuration(10);
                    animator.start();
                } else {
                    helper.setImageResource(R.id.iv_voice, R.mipmap.ic_file);
                    helper.setText(R.id.tv_name, item.chatBean.name);
                    String temp = item.chatBean.textContent;
                    int endP = temp.lastIndexOf(".");
                    String filetype = temp.substring(endP);
                    String fileName = temp.substring(temp.lastIndexOf("/") + 1, endP);
                    helper.setText(R.id.tv_duration, Base64Utils.decode(fileName) + filetype);
                    helper.setText(R.id.tv_time, DateUtil.getTimeStringAutoShort2(item.chatBean.time, false));
                }
                break;

            default:
                break;
        }
    }
}
