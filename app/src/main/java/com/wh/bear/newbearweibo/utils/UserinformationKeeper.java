package com.wh.bear.newbearweibo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.wh.bear.newbearweibo.openapi.models.User;

public class UserinformationKeeper {
	
	public static void writeInformation(Context context,User user) {
		if (null==context||null==user) {
			return;
		}
		
		SharedPreferences preferences = context.getSharedPreferences("Bearweibo_user_info", Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString("profile_image_url", user.profile_image_url);
		editor.putString("screen_name"	, user.screen_name);
		editor.putString("description", user.description);
		editor.putInt("statuses_count", user.statuses_count);
		editor.putInt("friends_count", user.friends_count);
		editor.putInt("followers_count", user.followers_count);
		editor.putString("location", user.location);
		editor.putString("url", user.url);
		editor.putString("domain", user.domain);
		editor.commit();
	}
	
	public static User readInformation(Context context) {
		if (null==context) {
			return null;
		}
		
		User user=new User();
		
		SharedPreferences preferences = context.getSharedPreferences("Bearweibo_user_info", Context.MODE_PRIVATE);
		
		user.profile_image_url=preferences.getString("profile_image_url", null);
		user.screen_name=preferences.getString("screen_name", null);
		user.description=preferences.getString("description", null);
		user.statuses_count=preferences.getInt("statuses_count", 0);
		user.friends_count=preferences.getInt("friends_count", 0);
		user.followers_count=preferences.getInt("followers_count", 0);
		user.location=preferences.getString("location", null);
		user.url=preferences.getString("url", null);
		user.domain=preferences.getString("domain", null);
		
		return user;
		
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
        
        SharedPreferences pref = context.getSharedPreferences("Bearweibo_user_info", Context.MODE_PRIVATE);
        Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }

}
