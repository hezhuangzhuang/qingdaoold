package com.zxwl.commonlibrary.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by hcc on 16/9/4 19:44
 * 100332338@qq.com
 * <p/>
 * 软键盘工具类
 */
public class KeyBoardUtil {
    public static void openKeybord(EditText mEditText, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public static void closeKeybord(EditText mEditText, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    /**
     * 隐藏键盘
     */
    public static void  hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        IBinder binder;
        if (null == view) {
            return;
        } else {
            binder = view.getWindowToken();
        }

        if (null == binder) {
            return;
        }
        ((InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(binder, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 判断键盘是否显示
     * @param rootView activity的根布局
     * @param showKeyboard  当前键盘的显示状态
     * @return true代表键盘显示，false代表隐藏
     */
    private boolean setKeyboardShow(ViewGroup rootView, boolean showKeyboard) {
        // 应用可以显示的区域。此处包括应用占用的区域，包括标题栏不包括状态栏
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        // 键盘最小高度
        int minKeyboardHeight = 150;
        // 获取状态栏高度
        int statusBarHeight = SystemBarHelper.getStatusBarHeight(rootView.getContext());

        // 屏幕高度,不含虚拟按键的高度
        int screenHeight = rootView.getRootView().getHeight();
        // 在不显示软键盘时，height等于状态栏的高度
        int height = screenHeight - (r.bottom - r.top);

        if (showKeyboard) {
            //键盘收起
            // 如果软键盘是弹出的状态，并且height小于等于状态栏高度，
            // 说明这时软键盘已经收起
            if (height - statusBarHeight < minKeyboardHeight) {
                //键盘隐藏
                showKeyboard = false;

            }
        } else {
            //键盘弹出
            // 如果软键盘是收起的状态，并且height大于状态栏高度，
            // 说明这时软键盘已经弹出
            if (height - statusBarHeight > minKeyboardHeight) {
                //键盘显示
                showKeyboard = true;

            }
        }
        return showKeyboard;
    }
}
