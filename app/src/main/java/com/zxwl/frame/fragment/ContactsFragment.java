package com.zxwl.frame.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.flyco.tablayout.SlidingTabLayout;
import com.zxwl.commonlibrary.BaseLazyFragment;
import com.zxwl.commonlibrary.utils.ToastUtil;
import com.zxwl.commonlibrary.widget.NoScrollViewPager;
import com.zxwl.frame.R;
import com.zxwl.frame.activity.ConvokeConfNewActivity;
import com.zxwl.frame.adapter.MyPagerAdapter;
import com.zxwl.frame.bean.EventMsg;
import com.zxwl.frame.utils.Constant;
import com.zxwl.frame.widget.SelectCreateDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * author：pc-20171125
 * data:2019/10/30 14:40
 */
public class ContactsFragment extends BaseLazyFragment {
    private SlidingTabLayout tbLayout;
    private NoScrollViewPager vpContent;

    private String[] mTitles = {"全部", "组织机构", "群聊"};

    private List<Fragment> mFragments = new ArrayList<>();

    private int currentIndex;

    private ImageView ivMore;

    public static ContactsFragment newInstance() {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            //相当于Fragment的onPause
        } else {
            // 相当于Fragment的onResume
            update();
        }
    }

    public void update() {
        try {
            EventMsg eventMsg = new EventMsg();
            eventMsg.setMsg(EventMsg.UPDATE_GROUP);
            EventBus.getDefault().post(eventMsg);
        } catch (Exception e) {

        }
    }

    @Override
    protected View inflateContentView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    protected void findViews(View view) {
        tbLayout = (SlidingTabLayout) view.findViewById(R.id.tb_layout);
        vpContent = (NoScrollViewPager) view.findViewById(R.id.vp_content);
        ivMore = view.findViewById(R.id.iv_right_operate);
        ((TextView) view.findViewById(R.id.tv_top_title)).setText("通讯录");
    }

    private SelectCreateDialog selectCreateDialog;

    @Override
    protected void addListeners() {
        ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreDialog(v);
            }
        });
    }

    private void showMoreDialog(View v) {
        initMoreDialog();
        selectCreateDialog.setBackground(null);
        selectCreateDialog.showPopupWindow(v);
    }

    private void initMoreDialog() {
        if (null == selectCreateDialog) {
            selectCreateDialog = new SelectCreateDialog(getActivity());
            selectCreateDialog.setOnClickLis(new SelectCreateDialog.onClickLis() {
                @Override
                public void onClick(int pos) {
                    switch (pos) {
                        case SelectCreateDialog.CREATE_GROUP:
                            ConvokeConfNewActivity.startActivity(getActivity(), "创建群组");
                            selectCreateDialog.dismiss();
                            break;

                        case SelectCreateDialog.CREATE_AUDIO:
                            if (Constant.isholdCall) {
                                ToastUtil.showLongToast(getActivity(), "当前处于会议中，无法召集会议");
                                return;
                            }
                            ConvokeConfNewActivity.startActivity(getContext(), true);
                            selectCreateDialog.dismiss();
                            break;

                        case SelectCreateDialog.CREATE_VIDEO:
                            if (Constant.isholdCall) {
                                ToastUtil.showLongToast(getActivity(), "当前处于会议中，无法召集会议");
                                return;
                            }
                            ConvokeConfNewActivity.startActivity(getContext(), false);
                            selectCreateDialog.dismiss();
                            break;
                    }
                }
            });
        }
    }

    @Override
    protected void initData() {
        ivMore.setVisibility(View.VISIBLE);
        ivMore.setImageResource(R.mipmap.ic_home_more);
        initTab();
    }

    private void initTab() {
        mFragments.add(AllPeopleFragment.newInstance());
        mFragments.add(OrganizationFragment.newInstance());
        mFragments.add(GroupFragment.newInstance());

        MyPagerAdapter mAdapter = new MyPagerAdapter(getChildFragmentManager(), mFragments, mTitles);
        vpContent.setAdapter(mAdapter);
        tbLayout.setViewPager(vpContent);

        vpContent.setCurrentItem(currentIndex);
        vpContent.setOffscreenPageLimit(mTitles.length);

        tbLayout.setCurrentTab(currentIndex);
        tbLayout.onPageSelected(currentIndex);
    }
}
