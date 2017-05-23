package com.wh.bear.newbearweibo.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.wh.bear.newbearweibo.openapi.models.User;

/**
 * 从网络解析多个user对象类
 * @author Administrator
 *
 */
public class UsersList{
	/* 用户列表 */
	public ArrayList<User> usersList;
	public User user;
	public boolean hasvisible;
	public String previous_cursor;
	public String next_cursor;
	public int total_number;

	public static UsersList parse(String jsonString){
		if (TextUtils.isEmpty(jsonString)) {
			return null;
		}
		
		UsersList userses=new UsersList();
		
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			userses.hasvisible      = jsonObject.optBoolean("hasvisible", false);
			userses.previous_cursor = jsonObject.optString("previous_cursor", "0");
			userses.next_cursor     = jsonObject.optString("next_cursor", "0");
			userses.total_number    = jsonObject.optInt("total_number", 0);
			
			JSONArray jsonArray      = jsonObject.optJSONArray("users");
			if (jsonArray != null && jsonArray.length() > 0) {
                int length = jsonArray.length();
                userses.usersList = new ArrayList<User>(length);
                for (int ix = 0; ix < length; ix++) {
                	userses.usersList.add(User.parse(jsonArray.getJSONObject(ix)));
                }
            }
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return userses;
	}

}
