package com.zxwl.commonlibrary.utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.zxwl.commonlibrary.R;

/**
 * author：pc-20171125
 * data:2019/4/12 15:17
 */
public class DialogUtils {
    private static ProgressDialog dialog;

    private static Dialog progressDialog;

    public static void showProgressDialog(Context context, String content) {
        if (context instanceof Activity) {
            if (((Activity) context).isFinishing()) {
                return;
            }
        }

        if (null != progressDialog && progressDialog.isShowing()) {
            return;
        }

        if (null == progressDialog) {
            progressDialog = new Dialog(context, R.style.CustomDialogStyle);
        }

        View dialogView = View.inflate(context, R.layout.dialog_progress, null);
        TextView tvContent = dialogView.findViewById(R.id.tv_content);
        if (TextUtils.isEmpty(content)) {
            tvContent.setVisibility(View.GONE);
        } else {
            tvContent.setText(content);
            tvContent.setVisibility(View.VISIBLE);
        }
        progressDialog.setContentView(dialogView);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        try {
            progressDialog.show();
        } catch (WindowManager.BadTokenException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * 判断进度对话框是否显示
     *
     * @return
     */
    public static void dismissProgressDialog(Context context) {
        if (context instanceof Activity) {
            if (((Activity) context).isFinishing()) {
                progressDialog = null;
                return;
            }
        }

        if (progressDialog != null && progressDialog.isShowing()) {
            Context loadContext = progressDialog.getContext();
            if (loadContext != null && loadContext instanceof Activity) {
                if (((Activity) loadContext).isFinishing()) {
                    progressDialog = null;
                    return;
                }
            }
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

}
