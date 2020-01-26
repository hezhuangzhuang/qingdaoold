package com.zxwl.frame.activity;


import android.view.View;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.zxwl.commonlibrary.BaseActivity;
import com.zxwl.commonlibrary.imageloader.GlideImageView;
import com.zxwl.commonlibrary.imageloader.progress.CircleProgressView;
import com.zxwl.commonlibrary.imageloader.progress.OnProgressListener;
import com.zxwl.commonlibrary.imageloader.transformation.BlurTransformation;
import com.zxwl.frame.R;

/**
 * 主界面
 */
public class ImageActivity extends BaseActivity {
    private Gson gson = new Gson();

    public GlideImageView image11;
    public GlideImageView image12;
    public GlideImageView image13;
    public GlideImageView image14;

    public GlideImageView image21;
    public GlideImageView image22;
    public GlideImageView image23;
    public GlideImageView image24;

    public GlideImageView image31;
    public CircleProgressView progressView1;

    public GlideImageView image32;
    public CircleProgressView progressView2;

    public GlideImageView image41;

    public String url1 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1497688355699&di=ea69a930b82ce88561c635089995e124&imgtype=0&src=http%3A%2F%2Fcms-bucket.nosdn.127.net%2Ff84e566bcf654b3698363409fbd676ef20161119091503.jpg";
    public String url2 = "http://img1.imgtn.bdimg.com/it/u=4027212837,1228313366&fm=23&gp=0.jpg";
    public String url3 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1529402445474&di=b5da3b2f6a466e618e1e32d4dd2bda4d&imgtype=0&src=http%3A%2F%2F2b.zol-img.com.cn%2Fproduct%2F133_500x2000%2F801%2Fce21ke76FRh4A.jpg";

    public String gif1 = "http://img.zcool.cn/community/01e97857c929630000012e7e3c2acf.gif";
    public String gif2 = "http://5b0988e595225.cdn.sohucs.com/images/20171202/a1cc52d5522f48a8a2d6e7426b13f82b.gif";
    public String gif3 = "http://img.zcool.cn/community/01d6dd554b93f0000001bf72b4f6ec.jpg";

    public static final String cat = "https://raw.githubusercontent.com/sfsheng0322/GlideImageView/master/resources/cat.jpg";
    public static final String cat_thumbnail = "https://raw.githubusercontent.com/sfsheng0322/GlideImageView/master/resources/cat_thumbnail.jpg";

    public static final String girl = "https://raw.githubusercontent.com/sfsheng0322/GlideImageView/master/resources/girl.jpg";
    public static final String girl_thumbnail = "https://raw.githubusercontent.com/sfsheng0322/GlideImageView/master/resources/girl_thumbnail.jpg";

    @Override
    protected void findViews() {
        image11 = (GlideImageView) findViewById(R.id.image11);
        image12 = (GlideImageView) findViewById(R.id.image12);
        image13 = (GlideImageView) findViewById(R.id.image13);
        image14 = (GlideImageView) findViewById(R.id.image14);

        image21 = (GlideImageView) findViewById(R.id.image21);
        image22 = (GlideImageView) findViewById(R.id.image22);
        image23 = (GlideImageView) findViewById(R.id.image23);
        image24 = (GlideImageView) findViewById(R.id.image24);

        image31 = (GlideImageView) findViewById(R.id.image31);
        progressView1 = (CircleProgressView) findViewById(R.id.progressView1);
        image32 = (GlideImageView) findViewById(R.id.image32);
        progressView2 = (CircleProgressView) findViewById(R.id.progressView2);

        image41 = (GlideImageView) findViewById(R.id.image41);
    }

    @Override
    protected void initData() {
        line1();
        line2();
        line3();
        line4();

//        HttpUtils.getInstance(this)
//                .getRetofitClinet()
//                .setBaseUrl(ApiUrls.IM_BASE_URL)
//                .builder(ImApi.class)
//                .
    }

    @Override
    protected void setListener() {
        image11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(ImageActivity.this, SetUrlActivity.class);
//                startActivity(intent);
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_image;
    }

    private void line1() {
        image11.enableState(true).load(url1);
        image12.loadCircle(url1);
        image13.load(url2, R.mipmap.image_loading);
        image14.load(url2, R.mipmap.image_loading, 15);
    }

    private void line2() {
        image21.fitCenter().load(gif2, R.mipmap.image_loading, 10);
        image22.fitCenter().load(gif1);
        image23.fitCenter().loadCircle(gif3);
    }

    private void line3() {
        image31.centerCrop()
                .error(R.mipmap.image_load_err)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .load(girl, R.color.placeholder, 15, new OnProgressListener() {
                    @Override
                    public void onProgress(boolean isComplete, int percentage, long bytesRead, long totalBytes) {
                        if (isComplete) {
                            progressView1.setVisibility(View.GONE);
                        } else {
                            progressView1.setVisibility(View.VISIBLE);
                            progressView1.setProgress(percentage);
                        }
                    }
                });

        image32.centerCrop()
                .error(R.mipmap.image_load_err)
                .load(cat, R.color.placeholder, new OnProgressListener() {
                    @Override
                    public void onProgress(boolean isComplete, int percentage, long bytesRead, long totalBytes) {
                        if (isComplete) {
                            progressView2.setVisibility(View.GONE);
                        } else {
                            progressView2.setVisibility(View.VISIBLE);
                            progressView2.setProgress(percentage);
                        }
                    }
                });
    }

    private void line4() {
        image41.fitCenter()
                .load(girl, R.mipmap.image_loading, new BlurTransformation(this, 25, 1));
    }

    public boolean isBaseOnWidth() {
        return true;
    }

    public float getSizeInDp() {
        return 1080;
    }
}
