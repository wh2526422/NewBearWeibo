package com.wh.bear.newbearweibo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class BearWeiboSetting {
	
	public static void write(Context context,String model) {
		if (null==context||null==model) {
			return;
		}
		
		SharedPreferences preferences = context.getSharedPreferences("Bearweibo_setting", Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString("model", model);
		editor.commit();
	}
	
	public static String read(Context context) {
		if (null==context) {
			return null;
		}
		
		SharedPreferences preferences = context.getSharedPreferences("Bearweibo_setting", Context.MODE_PRIVATE);
		String model = preferences.getString("model", "有图");
		
		return model;
		
	}
	
	 /**
     * 清空 SharedPreferences 
     * 
     * @param context 应用程序上下文环境
     */
    public static void clear(Context context) {
        if (null == context) {
            return;
        }
        
        SharedPreferences pref = context.getSharedPreferences("Bearweibo_setting", Context.MODE_PRIVATE);
        Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }
}
