package com.wh.bear.newbearweibo.utils;

import android.content.Context;
import android.util.Log;

import com.wh.bear.newbearweibo.MainActivity;

/**
 * Created by Administrator on 2017/4/28.
 */

public class log {
    private static final String TAG = "BearWeibo";
    public static void i(String tag, String msg){
        Log.i(TAG + "." + tag,msg);
    }
    public static void d(String tag,String msg){
        Log.d(TAG + "." + tag,msg);
    }
    public static void v(String tag,String msg){
        Log.v(TAG + "." + tag,msg);
    }
}
