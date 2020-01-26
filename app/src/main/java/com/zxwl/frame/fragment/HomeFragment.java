package com.zxwl.frame.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.zxwl.commonlibrary.BaseLazyFragment;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.frame.R;
import com.zxwl.frame.activity.ChatActivity;
import com.zxwl.frame.activity.ConvokeConfNewActivity;
import com.zxwl.frame.activity.JoinConfActivity;
import com.zxwl.frame.adapter.HomeMessageAdapter;
import com.zxwl.frame.adapter.item.ChatItem;
import com.zxwl.frame.bean.EventMsg;
import com.zxwl.frame.db.DBUtils;
import com.zxwl.frame.utils.Constant;
import com.zxwl.frame.widget.SelectCreateDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页fragment
 */
public class HomeFragment extends BaseLazyFragment {
    private RecyclerView rvList;
    private HomeMessageAdapter listAdapter;
    private List<ChatItem> listBeans;

    private ImageView ivMore;
    private TextView tvjoinConf;
    private FrameLayout fl_content;

    private SelectCreateDialog selectCreateDialog;

    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View inflateContentView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    protected void findViews(View view) {
        rvList = view.findViewById(R.id.rv_list);
        ivMore = view.findViewById(R.id.iv_right_operate);
        tvjoinConf = view.findViewById(R.id.tv_joinConf);
        fl_content = view.findViewById(R.id.fl_content);

        //初始化标题栏
        ((TextView) view.findViewById(R.id.tv_top_title)).setText("消息");
        ivMore.setVisibility(View.VISIBLE);
        ivMore.setImageResource(R.mipmap.ic_home_more);
    }

    @Override
    protected void initData() {
        initMessageBeans();

        initListAdapter();

        EventBus.getDefault().register(this);
    }

    /**
     * 主线程中处理返回事件
     *
     * @param messageEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void eventMain(EventMsg messageEvent) {
        switch (messageEvent.getMsg()) {
            case EventMsg.UPDATE_HOME:
                updateList();
                break;

            //更新消息的已读状态
            case EventMsg.UPDATE_HOME_READ:
                String receiveName = (String) messageEvent.getMessageData();
                for(ChatItem item:listAdapter.getData()){
                    if(receiveName.equals(item.chatBean.conversationUserName)){
                        item.chatBean.isRead = true;
                    }
                }

                listAdapter.notifyDataSetChanged();

                //更新首页未读消息
                unReadMsgHint(listAdapter.getData());
//                updateList();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        EventBus.getDefault().unregister(this);
    }

    private void initMessageBeans() {
        listBeans = new ArrayList<>();
        List<ChatItem> messageList = getMessageList();
        if (messageList.size() > 0) {
            listBeans.addAll(messageList);
        }

        unReadMsgHint(messageList);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void updateList() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<ChatItem> messageList = getMessageList();
                if (messageList.size() > 0) {
                    listAdapter.replaceData(messageList);
                    unReadMsgHint(messageList);
                }
            }
        }, 500);
    }

    private void unReadMsgHint(List<ChatItem> messageList) {
        for (ChatItem item : messageList) {
            if (!item.chatBean.isRead) {
                //存在未读消息
                EventMsg eventMsg = new EventMsg();
                eventMsg.setBody("show");
                eventMsg.setMsg(EventMsg.HOME_HINT);
                EventBus.getDefault().post(eventMsg);
                return;
            }
        }

        //全部消息已读
        EventMsg eventMsg = new EventMsg();
        eventMsg.setBody("hide");
        eventMsg.setMsg(EventMsg.HOME_HINT);
        EventBus.getDefault().post(eventMsg);
    }

    /**
     * 获取最后的消息
     *
     * @return
     */
    private List<ChatItem> getMessageList() {
        return DBUtils.recordQueryAllLM();
    }

    private void initListAdapter() {
        for (int i = 0; i < listBeans.size(); i++) {
            ChatItem chatItem = listBeans.get(i);
            if (chatItem.chatBean.isGroup) {
                chatItem.headRes = Constant.getGroupHeadRes();
            } else {
                chatItem.headRes = Constant.getPersonalHeadRes();
            }
        }
        listAdapter = new HomeMessageAdapter(R.layout.item_homemessage, listBeans);

        listAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                ChatItem chatItem = listAdapter.getData().get(position);
                //消息改为已读
                chatItem.chatBean.isRead = true;
                listAdapter.notifyDataSetChanged();

                unReadMsgHint(listAdapter.getData());

                DBUtils.recordSaveLM(listAdapter.getData());

                if (DBUtils.judgeGroup(chatItem.chatBean.conversationId)) {
                    ChatActivity.startActivity(getActivity(), chatItem.chatBean.conversationId, chatItem.chatBean.conversationUserName, true);
                } else {
                    ChatActivity.startActivity(getActivity(), chatItem.chatBean.conversationId, chatItem.chatBean.conversationUserName, false);
                }
            }
        });
        rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvList.setAdapter(listAdapter);

        View view = getActivity().getLayoutInflater().inflate(R.layout.layout_empty, rvList, false);
        listAdapter.setEmptyView(view);
    }

    @Override
    protected void addListeners() {
        ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCreateDialog = new SelectCreateDialog(getActivity());
                selectCreateDialog.setOnClickLis(new SelectCreateDialog.onClickLis() {
                    @Override
                    public void onClick(int pos) {
                        switch (pos) {
                            case SelectCreateDialog.CREATE_GROUP:
//                                CreateGroupActivity.startActivity(getActivity());
                                ConvokeConfNewActivity.startActivity(getActivity(), "创建群组");
                                selectCreateDialog.dismiss();
                                break;

                            case SelectCreateDialog.CREATE_AUDIO:
                                if (Constant.isholdCall) {
                                    ToastUtil.showLongToast(getActivity(), "当前处于会议中，无法召集会议");
                                    return;
                                }
//                                ConvokeConfActivity.startActivity(getContext(), true);
                                ConvokeConfNewActivity.startActivity(getContext(), true);
                                selectCreateDialog.dismiss();
                                break;

                            case SelectCreateDialog.CREATE_VIDEO:
                                if (Constant.isholdCall) {
                                    ToastUtil.showLongToast(getActivity(), "当前处于会议中，无法召集会议");
                                    return;
                                }
//                                ConvokeConfActivity.startActivity(getContext(), false);
                                ConvokeConfNewActivity.startActivity(getContext(), false);
                                selectCreateDialog.dismiss();
                                break;
                        }
                    }
                });
                selectCreateDialog.setBackground(null);
                selectCreateDialog.showPopupWindow(v);
            }
        });

        tvjoinConf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //会议接入码加入会议
                Intent intent = new Intent(getActivity(), JoinConfActivity.class);
                startActivity(intent);
            }
        });
    }

    private Gson gson = new Gson();

    private MaterialDialog exitConfDialog;

    /**
     * 显示结束会议的对话框
     */
    private void showExitConfDialog() {
        if (null == exitConfDialog) {
            View view = View.inflate(getActivity(), R.layout.dialog_exit_conf, null);
            RelativeLayout rlContent = (RelativeLayout) view.findViewById(R.id.rl_content);
            ImageView ivClose = (ImageView) view.findViewById(R.id.ic_close);
            TextView tvEndConf = (TextView) view.findViewById(R.id.tv_end_conf);
            TextView tvLeaveConf = (TextView) view.findViewById(R.id.tv_leave_conf);

            ivClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exitConfDialog.dismiss();
                }
            });

            tvEndConf.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            tvLeaveConf.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            exitConfDialog = new MaterialDialog.Builder(getActivity())
                    .customView(view, false)
                    .build();

            Window window = exitConfDialog.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);  //全屏的flag
            window.setBackgroundDrawableResource(android.R.color.transparent); //设置window背景透明
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.alpha = 0.8f;
            lp.dimAmount = 0.1f; //dimAmount在0.0f和1.0f之间，0.0f完全不暗，1.0f全暗
            window.setAttributes(lp);
        }
        exitConfDialog.show();
    }

}
