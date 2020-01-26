package com.zxwl.frame.fragment;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zxwl.commonlibrary.BaseLazyFragment;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.frame.R;
import com.zxwl.frame.activity.ConfListActivity;
import com.zxwl.frame.activity.ConvokeConfNewActivity;
import com.zxwl.frame.activity.JoinConfDialogActivity;
import com.zxwl.frame.utils.Constant;
import com.zxwl.frame.widget.SelectCreateDialog;

/**
 * 新版本会议页面，提供三个复选框
 */
public class ConfFragment extends BaseLazyFragment {
    private ImageView ivRightOperate;
    private SelectCreateDialog selectCreateDialog;

    private RelativeLayout rlCreate;
    private RelativeLayout rlJoin;
    private RelativeLayout rlMyConf;

    public ConfFragment() {
    }

    public static ConfFragment newInstance() {
        ConfFragment fragment = new ConfFragment();
        return fragment;
    }

    @Override
    protected View inflateContentView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_conf, container, false);
    }

    @Override
    protected void findViews(View view) {
        ivRightOperate = (ImageView) view.findViewById(R.id.iv_right_operate);
        ivRightOperate.setVisibility(View.VISIBLE);
        ivRightOperate.setImageResource(R.mipmap.ic_home_more);

        //初始化标题栏
        ((TextView) view.findViewById(R.id.tv_top_title)).setText("会议");

        rlCreate = (RelativeLayout) view.findViewById(R.id.rl_create);
        rlJoin = (RelativeLayout) view.findViewById(R.id.rl_join);
        rlMyConf = (RelativeLayout) view.findViewById(R.id.rl_my_conf);
    }

    @Override
    protected void addListeners() {
        View.OnClickListener commonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.rl_create:
                        ConvokeConfNewActivity.startActivity(getActivity(), false);
                        break;

                    case R.id.rl_join:
                        JoinConfDialogActivity.startActivity(getActivity());
                        break;

                    case R.id.rl_my_conf:
                        ConfListActivity.startActivity(getActivity());
                        break;

                    case R.id.iv_right_operate:
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
                        selectCreateDialog.showPopupWindow(ivRightOperate);
                        break;
                }
            }
        };

        ivRightOperate.setOnClickListener(commonListener);
        rlCreate.setOnClickListener(commonListener);
        rlJoin.setOnClickListener(commonListener);
        rlMyConf.setOnClickListener(commonListener);
    }

    @Override
    protected void initData() {

    }
}
