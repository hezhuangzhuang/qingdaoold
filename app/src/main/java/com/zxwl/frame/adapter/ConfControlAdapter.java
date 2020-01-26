package com.zxwl.frame.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zxwl.frame.R;
import com.zxwl.frame.bean.respone.ConfBeanRespone;

import java.util.List;

/**
 * author：pc-20171125
 * data:2019/1/3 17:43
 * 会控的适配器
 */
public class ConfControlAdapter extends RecyclerView.Adapter<ConfControlAdapter.ConfControlHolder> {
    private Context context;
    private List<ConfBeanRespone.DataBean.SiteStatusInfoListBean> list;

    public List<ConfBeanRespone.DataBean.SiteStatusInfoListBean> getDatas() {
        return list;
    }

    //是否是主席
    private boolean isChair = false;

    //会议模式：0表示主席模式，1表示多画面模式
    //（1）主席模式：所有参会会场都观看主席会场，在此模式下，任意会场可以选看任意会场
    //（2）多画面模式：所有参会会场都观看多画面，在此模式下，选看功能无效

    //多画面模式，显示广播按钮
    //选看模式，显示选看按钮
    private int confMode = -1;

    /**
     * 设置是否有主席功能
     *
     * @param chair
     */
    public void setChair(boolean chair) {
        isChair = chair;
    }

    /**
     * 设置设置会议模式
     */
    public void setConfMode(int confMode) {
        this.confMode = confMode;
    }

    public ConfControlAdapter(Context context, List<ConfBeanRespone.DataBean.SiteStatusInfoListBean> list) {
        this.context = context;
        this.list = list;
    }

    private onConfControlClickListener recyclerClick;

    public void setRecyclerClick(onConfControlClickListener recyclerClick) {
        this.recyclerClick = recyclerClick;
    }

    public void replceData(List<ConfBeanRespone.DataBean.SiteStatusInfoListBean> newList) {
        // 不是同一个引用才清空列表
        if (newList != list) {
            list.clear();
            list.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @Override
    public ConfControlHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conf_control, null);
        return new ConfControlHolder(view);
    }

    @Override
    public void onBindViewHolder(ConfControlHolder holder, int position) {
        ConfBeanRespone.DataBean.SiteStatusInfoListBean site = list.get(position);
        holder.tvName.setText(site.siteName);
//        holder.ivCheck.setImageResource(site.isCheck ? R.mipmap.ic_no_login_select : R.mipmap.ic_no_login_un_select);
//        holder.ivCheck.setVisibility(View.GONE);
        holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.white));
        holder.ivBroadcase.setImageResource(1 == site.broadcastStatus ? R.mipmap.ic_control_broadcast_select : R.mipmap.ic_control_broadcast);
        holder.ivMic.setImageResource(1 == site.microphoneStatus ? R.mipmap.icon_mic : R.mipmap.icon_mic_close);
        holder.ivHangup.setImageResource(2 == site.siteStatus ? R.mipmap.ic_control_hangup : R.mipmap.ic_control_call);
        holder.ivLouder.setImageResource(1 == site.loudspeakerStatus ? R.mipmap.ic_louder_open : R.mipmap.ic_louder_close);
        holder.ivWatchSite.setImageResource(site.isWatch ? R.mipmap.ic_watch_site_true : R.mipmap.ic_watch_site_false);

        holder.ivBroadcase.setVisibility(isChair && 1 == confMode ? View.VISIBLE : View.GONE);
        holder.ivWatchSite.setVisibility(0 == confMode ? View.VISIBLE : View.GONE);

        holder.ivMic.setVisibility(isChair ? View.VISIBLE : View.GONE);
        holder.ivHangup.setVisibility(isChair ? View.VISIBLE : View.GONE);
        holder.ivLouder.setVisibility(isChair ? View.VISIBLE : View.GONE);

        holder.ivLouder.setVisibility(View.GONE);

        holder.tvName.setTextColor(ContextCompat.getColor(context, 2 == site.siteStatus ? R.color.white : R.color.color_999));

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != recyclerClick) {
                    recyclerClick.onClick(position);
                }
            }
        });

        holder.ivMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != recyclerClick) {
                    recyclerClick.onMic(position);
                }
            }
        });

        holder.ivHangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != recyclerClick) {
                    recyclerClick.onHangUp(position);
                }
            }
        });

        holder.ivBroadcase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != recyclerClick) {
                    recyclerClick.onBroadcast(position);
                }
            }
        });

        holder.ivLouder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != recyclerClick) {

                    recyclerClick.onLouder(position);
                    //todo 变更为扬声器控制

                }
            }
        });

        holder.ivWatchSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != recyclerClick) {
                    //观看会场
                    recyclerClick.onWatchSite(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return null == list ? 0 : list.size();
    }

    class ConfControlHolder extends RecyclerView.ViewHolder {
        public View rootView;
        public TextView tvName;
        public ImageView ivCheck;

        public ImageView ivMic;
        public ImageView ivHangup;
        public ImageView ivBroadcase;
        public ImageView ivLouder;
        public ImageView ivWatchSite;

        public ConfControlHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            ivCheck = itemView.findViewById(R.id.iv_check);
            rootView = itemView.findViewById(R.id.rl_content);

            ivMic = itemView.findViewById(R.id.iv_mic);
            ivHangup = itemView.findViewById(R.id.iv_hangup);
            ivBroadcase = itemView.findViewById(R.id.iv_broadcast);
            ivLouder = itemView.findViewById(R.id.iv_louder);
            ivWatchSite = itemView.findViewById(R.id.iv_watch_site);
        }
    }
}
