package com.zxwl.frame.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.zxwl.frame.R;

public class ButtomSelectDialog extends Dialog {
    private TextView tvAllPerson;
    private TextView tvPartPerson;
    private TextView tvCancle;

    public static final int TYPE_ONE = 0;
    public static final int TYPE_TWO = 1;

    public ButtomSelectDialog(@NonNull Context context) {
        this(context, 0);
    }

    public ButtomSelectDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_bottom_select_person);

        tvAllPerson = (TextView) findViewById(R.id.tv_all_person);
        tvPartPerson = (TextView) findViewById(R.id.tv_part_person);
        tvCancle = (TextView) findViewById(R.id.tv_cancle);

        setCanceledOnTouchOutside(true);
        setCancelable(true);
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);//dialog底部弹出
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);

        //设置dialog在界面中的属性
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        tvCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        tvAllPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != clickListener) {
                    clickListener.selectClick(TYPE_ONE);
                }
            }
        });

        tvPartPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != clickListener) {
                    clickListener.selectClick(TYPE_TWO);
                }
            }
        });
    }

    public onItemClickListener clickListener;

    public void setClickListener(onItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /**
     * 按钮点击事件
     */
    public interface onItemClickListener {
        public void selectClick(int type);
    }
}
