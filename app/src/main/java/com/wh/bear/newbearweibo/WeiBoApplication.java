package com.wh.bear.newbearweibo;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.wh.bear.newbearweibo.utils.Constants;
import com.wh.bear.newbearweibo.utils.ImageOptHelper;

/**
 * Created by Administrator on 2017/4/28.
 */

public class WeiBoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        WbSdk.install(this,new AuthInfo(this, Constants.APP_KEY,Constants.REDIRECT_URL,Constants.SCOPE));
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
    }
}
