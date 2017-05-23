package com.wh.bear.newbearweibo.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * 解析用户搜索的结果
 * @author Administrator
 *
 */
public class SearchUser implements Parcelable{

	public String uid;
	public String screen_name;
	public int followers_count;
	
	public SearchUser(String uid, String screen_name, int followers_count) {
		this.uid = uid;
		this.screen_name = screen_name;
		this.followers_count = followers_count;
	}

	public SearchUser() {
	}
	
	public static ArrayList<SearchUser> parseSeachUsers(String jsonString) {
		if (TextUtils.isEmpty(jsonString)) {
			return null;
		}
		ArrayList<SearchUser> userses=new ArrayList<SearchUser>();
		
		try {
			JSONArray jsonArray = new JSONArray(jsonString);
			for (int i = 0; i < jsonArray.length(); i++) {
				
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				SearchUser user=new SearchUser();
				user.screen_name=jsonObject.getString("screen_name");
				user.uid=jsonObject.getInt("uid")+"";
				user.followers_count=jsonObject.getInt("followers_count");
				userses.add(user);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return userses;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(uid);
		dest.writeString(screen_name);
		dest.writeInt(followers_count);
	}
	
	 public static final Creator<SearchUser> CREATOR = new Creator<SearchUser>()
	    {
	        @Override
	        public SearchUser[] newArray(int size)
	        {
	            return new SearchUser[size];
	        }
	        
	        @Override
	        public SearchUser createFromParcel(Parcel in)
	        {
	            return new SearchUser(in);
	        }
	    };
	    
	    public SearchUser(Parcel in)
	    {
	        uid = in.readString();
	        screen_name = in.readString();
	        followers_count = in.readInt();
	    }

}
