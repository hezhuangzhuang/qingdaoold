package com.zxwl.frame.activity;

import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.zxwl.commonlibrary.BaseActivity;
import com.zxwl.frame.R;

import static com.zxwl.frame.utils.UIUtils.getMainThreadHandler;

/**
 * 查看照片的界面
 */
public class PhotoViewActivity extends BaseActivity {
    private PhotoView photoView;

    public static final String IMAGE_URL = "IMAGE_URL";

    public static void startActivity(Context context, String imageUrl) {
        Intent intent = new Intent(context, PhotoViewActivity.class);
        intent.putExtra(IMAGE_URL, imageUrl);
        context.startActivity(intent);
    }

    @Override
    protected void findViews() {
        photoView = (PhotoView) findViewById(R.id.photo_view);
    }

    @Override
    protected void initData() {
        String imageUrl = getIntent().getStringExtra(IMAGE_URL);

        Glide.with(this)
                .load(imageUrl)
                .into(photoView);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_photo_view;
    }

    int touchCount = 0;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN){
            touchCount = touchCount + 1;
            //单击
            getMainThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (touchCount == 1){
                        finish();
                    }
                    touchCount = 0;
                }
            }, 500);
        }
        return super.dispatchTouchEvent(ev);
    }
}
