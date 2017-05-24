package com.wh.bear.newbearweibo.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.wh.bear.newbearweibo.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2016/5/24.
 */
public class PictureLoader {
    private static final String TAG = "PictureLoader";
    private static final boolean DEBUG = false;
    private static ExecutorService priorityThreadPool;
    private static List<String> urls;                                  //  储存加载过得图片的url
    private static PictureLoader loader;
    private static HashMap<String, SoftReference<Bitmap>> mImageCaches;

    public interface PictureLoaderListener{
        void onLoadingStarted(String imageUri, View view);

        void onLoadingFailed(String imageUri, View view, String failReason);

        void onLoadingComplete(String imageUri, View view, Bitmap loadedImage);
    }

    private PictureLoader() {
        if (DEBUG) log.i(TAG, "PictureLoader");
        urls = new ArrayList<>();
        priorityThreadPool = Executors.newFixedThreadPool(10);
        mImageCaches = new HashMap<>();
    }

    public static PictureLoader getInstance() {
        if (loader == null) {
            loader = new PictureLoader();
        }
        return loader;
    }

    /**
     * 无优先级下载
     *
     * @param urlStr    资源链接
     * @param imageView 要显示的ImageView
     */
    public void displayImage(String urlStr, ImageView imageView) {
        displayImageByPriority(urlStr, imageView, 0,null);
    }
    public void displayImageWithCallback(String urlStr, ImageView imageView,PictureLoaderListener loaderListener) {
        displayImageByPriority(urlStr, imageView, 0,loaderListener);
    }
    /**
     * 按优先级来下载网络资源图片
     *
     * @param urlStr    资源链接
     * @param imageView 要显示的ImageView
     * @param priority  下载优先级
     */
    public void displayImageByPriority(final String urlStr, final ImageView imageView, int priority, final PictureLoaderListener loaderListener) {
        if (loaderListener != null)
            loaderListener.onLoadingStarted(urlStr,imageView);
        else
            imageView.setImageResource(R.drawable.load_before);

        if (mImageCaches.containsKey(urlStr)) {
            SoftReference<Bitmap> bitmapSoftReference = mImageCaches.get(urlStr);
            Bitmap bitmap = bitmapSoftReference.get();
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                return;
            }
        }

        if (!urls.contains(urlStr)) {
            if (DEBUG) log.i(TAG, "displayImage\turlStr\t" + urlStr);
            loadImage(urlStr, imageView, priority, loaderListener);
            urls.add(urlStr);
        } else {
            if (DEBUG)
                log.i(TAG, "Image had been downloaded,so get the local image\turl\t" + urlStr);
            Bitmap localeImage = null;
            try {
                localeImage = getLocaleImage(urlStr, imageView);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (localeImage != null) {
                mImageCaches.put(urlStr, new SoftReference<>(localeImage));
                imageView.setImageBitmap(localeImage);
            }
        }

    }

    public void loadImage(final String urlStr, final ImageView imageView, final int priority, final PictureLoaderListener loaderListener) {
        if (priorityThreadPool == null){
            if (DEBUG) log.i(TAG,"ExecutorService is null");
            return;
        }
        priorityThreadPool.execute(new PriorityRunnable(priority, imageView) {
            @Override
            public Bitmap loadPicture() {
                URL url ;
                HttpURLConnection urlConnection = null;
                Bitmap bitmap = null;
                try {
                    url = new URL(urlStr);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoInput(true);
                    urlConnection.connect();
                    InputStream inputStream = urlConnection.getInputStream();
                    bitmap = decodeBitmapWithStream(inputStream, imageView.getWidth(), imageView.getHeight());
                    inputStream.close();

                    if (bitmap != null) {
                        if (loaderListener != null)
                            loaderListener.onLoadingComplete(urlStr,imageView,bitmap);

                        saveImage(bitmap, urlStr);
                        mImageCaches.put(urlStr, new SoftReference<>(bitmap));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    if (loaderListener != null)
                        loaderListener.onLoadingFailed(urlStr,imageView,e.getMessage());
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
                return bitmap;
            }
        });
    }

    private Bitmap decodeBitmapWithUrl(String path, int width, int height) throws IOException {
        return decodeBitmapInSampleSize(path, null, width, height);
    }

    private Bitmap decodeBitmapWithStream(InputStream inputStream, int width, int height) throws IOException {
        return decodeBitmapInSampleSize(null, inputStream, width, height);
    }

    /**
     * 从数据流或者路径解析位图
     *
     * @param path        资源路径
     * @param inputStream 数据流
     * @param width       要预览的控件宽
     * @param height      要预览的控件高
     * @return 返回解析好的位图，如果数据流出现异常护着路径有误可能返回空
     * @throws IOException
     */
    private Bitmap decodeBitmapInSampleSize(String path, InputStream inputStream, int width, int height) throws IOException {

        byte[] bytes = null;
        Bitmap bitmap = null;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;

        if (inputStream != null) {
            bytes = streamToBytes(inputStream);
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        } else if (path != null) {
            BitmapFactory.decodeFile(path, opts);
        }

        opts.inSampleSize = calculateInSampleSize(opts, width, height);
        if (DEBUG) log.i(TAG, "sampleSize\t" + opts.inSampleSize);
        opts.inJustDecodeBounds = false;

        if (inputStream != null)
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        else if (path != null)
            bitmap = BitmapFactory.decodeFile(path, opts);

        bytes = null;
        return bitmap;
    }

    /**
     * 输入流转字节数组
     *
     * @param in 输入流
     * @return 字节数组
     * @throws IOException
     */
    private byte[] streamToBytes(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int count = -1;
        while ((count = in.read(buff, 0, 1024)) != -1) {
            baos.write(buff, 0, count);
        }
        buff = null;
        return baos.toByteArray();
    }

    private int calculateInSampleSize(BitmapFactory.Options opts, int width, int height) {
        int outWidth = opts.outWidth;
        int outHeight = opts.outHeight;
        if (DEBUG)
            log.i(TAG, "width\t" + width + ",height\t" + height + ",outWidth\t" + outWidth + ",outHeight\t" + outHeight);
        if (width > outWidth || height > outHeight || width == 0 || height == 0) {
            return 1;
        }

        return outWidth / width >= outHeight / height ? outWidth / width : outHeight / height;
    }

    /**
     * 取消所有的正在运行的任务，并且清空数据
     */
    public void cancelAllTasksAndCleanData() {
        if (urls != null) {
            urls.clear();
            urls = null;
        }

        if (priorityThreadPool != null) {
            priorityThreadPool.shutdown();
            priorityThreadPool = null;
        }

        loader = null;
    }

    private Bitmap getLocaleImage(String imageUrl, ImageView imageView) throws IOException {
        imageUrl = Environment.getExternalStorageDirectory() + "/BearWeibo/Cache/Pictures/" + imageUrl.substring(imageUrl.lastIndexOf("/"));
        File file = new File(imageUrl);
        Bitmap bitmap = null;
        if (file.exists()) {
            bitmap = decodeBitmapWithUrl(imageUrl, imageView.getWidth(), imageView.getHeight());
        }

        return bitmap;
    }

    public void cleanPictureCache() {
        String url = Environment.getExternalStorageDirectory() + "/BearWeibo/Cache/Pictures/";
        File file = new File(url);
        if (!file.exists()) {
            return;
        }
        final File[] childFiles = file.listFiles();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (File f : childFiles) {
                    f.delete();
                }
            }
        }).start();

    }

    public void releaseImages() {
        if (mImageCaches != null) {
            if (DEBUG) log.i(TAG, "releaseImages\tsize\t" + mImageCaches.size());
            Set<String> strings = mImageCaches.keySet();
            for (String path : strings) {
                Bitmap bitmap = mImageCaches.get(path).get();
                if (bitmap != null)
                    bitmap.recycle();
            }

            mImageCaches.clear();
        }
    }

    /**
     * 将bitmap本地保存
     *
     * @param bitmap   要保存的bitmap
     * @param imageUrl 要保存的url
     * @return 是否保存成功
     * @throws IOException
     */
    private boolean saveImage(Bitmap bitmap, String imageUrl) throws IOException {
        File fileDirs = new File(Environment.getExternalStorageDirectory() + "/BearWeibo/Cache/Pictures/");
        if (!fileDirs.exists()) fileDirs.mkdirs();
        imageUrl = imageUrl.substring(imageUrl.lastIndexOf("/"));
        File file = new File(fileDirs, imageUrl);
        if (DEBUG) log.i(TAG, "url\t" + file.getAbsolutePath());

        if (file.exists()) {
            return false;
        }
        boolean newFile = file.createNewFile();
        if (!newFile) {
            return false;
        }
        FileOutputStream fos = new FileOutputStream(file);
        boolean compress = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
        return compress;
    }

    /**
     * 带优先级的线程
     */
    private abstract class PriorityRunnable implements Runnable, Comparable<PriorityRunnable> {

        private int priority;
        private ImageView imageView;
        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                doPostExcuses((Bitmap) msg.obj);
            }
        };

        private PriorityRunnable(int priority, ImageView imageView) {
            this.priority = priority;
            this.imageView = imageView;
        }

        @Override
        public int compareTo(@NonNull PriorityRunnable otherRunnable) {
            int mine = this.priority;
            int other = otherRunnable.getPriority();
            return mine < other ? 1 : mine < other ? -1 : 0;
        }

        @Override
        public void run() {
            Bitmap bitmap = loadPicture();
            Message msg = Message.obtain();
            msg.obj = bitmap;
            handler.sendMessage(msg);
        }

        public abstract Bitmap loadPicture();

        private void doPostExcuses(Bitmap bitmap) {
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }

        private int getPriority() {
            return priority;
        }
    }
}
