package com.wh.bear.newbearweibo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.wh.bear.newbearweibo.utils.log;

import java.util.ArrayList;

/**
 * 点击浏览图片界面
 *
 * @author Administrator
 */
public class ReviewImagesActivity extends Activity implements OnTouchListener {

    private static final String TAG = "ReviewImagesActivity";
    ViewPager img_viewPager;
    ProgressBar load_img_progress;
    LinearLayout point_layout;
    ImageView[] imgs;//播放图片
    View[] points;
    ImageLoader imageLoader;
    int position = 0;//那张图片被点
    int currentPos;

    //图片相关处理
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    //第一个按下手指的触摸点
    PointF start = new PointF();
    //两个手指的中点
    PointF mid = new PointF();
    //初始两个手指的距离
    float oldDist;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private static final int DOWN = 0;
    private static final int MOVE = 1;
    private int clickFlag = 0;

    public static final String SINGLE = "single";
    public static final String MORE = "more";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_img_layout);

        imageLoader = ImageLoader.getInstance();

        img_viewPager = (ViewPager) findViewById(R.id.img_viewPager);
        point_layout = (LinearLayout) findViewById(R.id.point_layout);
        load_img_progress = (ProgressBar) findViewById(R.id.load_img_progress);
        Intent intent = getIntent();
        String action = intent.getAction();
        //两个action判断是从哪里启动的

        if (SINGLE.equals(action)) {
            ArrayList<String> list = new ArrayList<String>();
            Bundle bundle = intent.getExtras();
            String original_pic = bundle.getString("original_pic");
            list.add(original_pic);
            initPoint(1);
            initImages(1, list);
        } else {
            Bundle bundle = intent.getExtras();
            position = bundle.getInt("position");
            ArrayList<String> pic_urls = bundle.getStringArrayList("pic_urls");
            initPoint(pic_urls.size());
            initImages(pic_urls.size(), pic_urls);
        }

        img_viewPager.setAdapter(new ImageAdapter());
        img_viewPager.setCurrentItem(position);
        currentPos = position;
        img_viewPager.setOnPageChangeListener(new OnPageChangeListener() {
            //修改下方小点的状态
            @Override
            public void onPageSelected(int arg0) {
                points[currentPos].setEnabled(false);
                points[arg0].setEnabled(true);
                currentPos = arg0;
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    /**
     * 初始化小圆点
     *
     * @param size
     */
    private void initPoint(int size) {
        points = new View[size];
        for (int i = 0; i < points.length; i++) {
            View point = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(8, 8);
            params.leftMargin = 4;
            params.rightMargin = 4;
            point.setBackgroundResource(R.drawable.point_seletor);
            point.setLayoutParams(params);
            point.setEnabled(false);
            points[i] = point;
            point_layout.addView(point);
        }
        points[position].setEnabled(true);
    }

    /**
     * 初始化图片
     *
     * @param size
     * @param path
     */
    private void initImages(int size, ArrayList<String> path) {
        String str = path.get(0);
        str = str.substring(str.lastIndexOf(".") + 1);
        if (str.equals("gif")) {
            imgs = new ImageView[size];
            for (int i = 0; i < imgs.length; i++) {
                imgs[i] = new ImageView(this);
                imgs[i].setScaleType(ImageView.ScaleType.FIT_CENTER);
                imgs[i].setLayoutParams(new LinearLayout.LayoutParams
                        (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                Glide.with(this).load(path.get(i)).asGif().into(imgs[i]);
            }

        } else {
            imgs = new ImageView[size];
            for (int i = 0; i < imgs.length; i++) {
                imgs[i] = new ImageView(this);
                imgs[i].setScaleType(ImageView.ScaleType.MATRIX);

                imgs[i].setLayoutParams(new LinearLayout.LayoutParams
                        (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

                setImageOnclick(imgs[i]);
                imgs[i].setOnTouchListener(this);
                imageLoader.displayImage(path.get(i), imgs[i], listener);
            }
        }


    }

    /**
     * 图片居中显示
     *
     * @param imageView
     */
    private void center(ImageView imageView, Bitmap bitmap) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;

        int bWidth = bitmap.getWidth();//图像宽
        int bHeight = bitmap.getHeight();//图像高

        float scaleX = screenWidth / (float) bitmap.getWidth();
        float scaleY = screenHeight / (float) bitmap.getHeight();
        float scale = Math.min(scaleX, scaleY);
        Matrix m = new Matrix();
        m.postScale(scale, scale);
        m.postTranslate((screenWidth - bWidth * scale) / 2, (screenHeight - bHeight * scale) / 2);

        imageView.setImageMatrix(m);
    }

    private void setImageOnclick(ImageView img) {
        img.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mode == NONE && clickFlag == DOWN) {
                    finish();
                    overridePendingTransition(0, R.anim.exit_set);
                }

            }
        });

    }

    /**
     * viewPager适配器
     *
     * @author Administrator
     */
    class ImageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return imgs.length;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(imgs[position]);
            return imgs[position];
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(imgs[position]);
        }
    }
    boolean zoomed = false;
    ImageView currentImg;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView img = (ImageView) v;
        if (currentImg == null || currentImg != img) {
            currentImg = img;
            zoomed = false;
        }
        //判断多点触摸
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            //第一个手指按下
            case MotionEvent.ACTION_DOWN:
                //获得图片的矩阵
                matrix.set(img.getImageMatrix());
                //保存
                savedMatrix.set(matrix);
                //记录第一个点的位置
                start.set(event.getX(), event.getY());
                mode = DRAG;
                clickFlag = DOWN;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //第二个手指按下
                oldDist = distance(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    mid = middle(event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                log.i(TAG,"ACTION_UP");
                // 手指放开事件
                if (mode == DRAG && zoomed) {
                    log.i(TAG,"UP back to home");
                    matrix.postTranslate(start.x - event.getX(), start.y - event.getY());
                }
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                // 手指滑动事件
                log.i(TAG,"ACTION_MOVE");
                if (mode == DRAG && zoomed) {
                    // 是一个手指拖动
                    matrix.set(savedMatrix);
                    log.i(TAG,"x\t" + event.getX() + ",y\t" + event.getY() + "\n" +
                    ",event.getX() - start.x\t" +(event.getX() - start.x) + ",event.getY() - start.y\t" + (event.getY() - start.y) );
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                    clickFlag = MOVE;
                } else if (mode == ZOOM) {
                    zoomed = true;
                    // 两个手指滑动
                    float newDist = distance(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                        clickFlag = MOVE;
                    }
                }
                break;
        }

        // 设置ImageView的Matrix
        img.setImageMatrix(matrix);
        return false;
    }

    /**
     * 计算中心点
     *
     * @param event
     * @return
     */
    private PointF middle(MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        return new PointF(x / 2, y / 2);
    }

    /*
     * 计算距离
     */
    private float distance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private ImageLoadingListener listener = new ImageLoadingListener() {

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            load_img_progress.setVisibility(View.VISIBLE);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            load_img_progress.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            load_img_progress.setVisibility(View.GONE);
            center((ImageView) view, loadedImage);

        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            load_img_progress.setVisibility(View.GONE);
        }
    };

}
