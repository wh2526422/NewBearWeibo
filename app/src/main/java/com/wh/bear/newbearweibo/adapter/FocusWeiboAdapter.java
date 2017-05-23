package com.wh.bear.newbearweibo.adapter;

import java.util.ArrayList;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.utils.LogUtil;
import com.wh.bear.newbearweibo.R;
import com.wh.bear.newbearweibo.openapi.models.User;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FocusWeiboAdapter extends BaseAdapter {
	protected static final String TAG = FocusWeiboAdapter.class.getName();
	ArrayList<User> users;
	Context context;
	int flag;//标志，是启动的我的关注页面还是启动我的粉丝页面
	Holder holder;
	ImageLoader imageLoader;
//	FriendshipsAPI fApi;
	Oauth2AccessToken mAccessToken;
	
	public static int WEIBO_FOCUS=0;//启动关注页面
	public static int WEIBO_FANS=1;//启动粉丝页面
	public FocusWeiboAdapter(ArrayList<User> users, Context context, int flag) {
		this.users = users;
		this.context = context;
		this.flag=flag;
		imageLoader=ImageLoader.getInstance();
		
//		if (null!=context) {
//			mAccessToken = AccessTokenKeeper.readAccessToken(context);
//			fApi=new FriendshipsAPI(context, Constants.APP_KEY, mAccessToken);
//		}
//
	}

	@Override
	public int getCount() {
		
		return users.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		
		if (convertView==null) {
			convertView=View.inflate(context, R.layout.user_list, null);
			
			View header = convertView.findViewById(R.id.include_userlist_header);
			ImageView user_imag = (ImageView) header.findViewById(R.id.user_imag);
			TextView user_name = (TextView) header.findViewById(R.id.user_name);
			TextView text = (TextView) header.findViewById(R.id.createtime);
			
			ImageView cancle_focus_btn=(ImageView) convertView.findViewById(R.id.cancle_focus_btn);
			
			holder=new Holder(user_imag, user_name, text, cancle_focus_btn);
			convertView.setTag(holder);
		}else {
			holder=(Holder) convertView.getTag();
		}
		
		final User user = users.get(position);
		//头像
		imageLoader.displayImage(user.profile_image_url, holder.user_imag);
		//昵称
		holder.user_name.setText(user.screen_name);
		//最近状态
		holder.text.setText(user.description);
		if (flag==WEIBO_FOCUS) {
			if (user.follow_me) {
				holder.cancle_focus_btn.setImageResource(R.drawable.card_icon_arrow);
			}
		}else {
			holder.cancle_focus_btn.setImageResource(R.drawable.card_icon_addattention);
		}
		
		holder.cancle_focus_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
//				Long id=Long.parseLong(user.id);
//				String uname=user.screen_name;
//				if (flag==WEIBO_FOCUS) {
//					fApi.destroy(id, uname, mListener);
//				}else {
//					fApi.create(id, uname, mListener);
//				}
				
			}
		});
		return convertView;
	}
	
	class Holder{
		ImageView user_imag;
		TextView user_name;
		TextView text;
		ImageView cancle_focus_btn;
		public Holder(ImageView user_imag, TextView user_name, TextView text, 
				ImageView cancle_focus_btn) {
			this.user_imag = user_imag;
			this.user_name = user_name;
			this.text = text;
			this.cancle_focus_btn = cancle_focus_btn;
		}
	}
	/**
	 * 添加关注及取消关注回调，该接口暂未支持无法使用
	 */
	private RequestListener mListener=new RequestListener() {
		
		@Override
		public void onWeiboException(WeiboException e) {
			LogUtil.e(TAG, e.getMessage());
//			ErrorInfo info = ErrorInfo.parse(e.getMessage());
//			Toast.makeText(context, info.toString(), Toast.LENGTH_LONG).show();
			
		}
		
		@Override
		public void onComplete(String response) {
			if (!TextUtils.isEmpty(response)) {
				User user = User.parse(response);
				if (user!=null) {
					if (flag==WEIBO_FOCUS) {
						holder.cancle_focus_btn.setImageResource(R.drawable.card_icon_addattention);
					}else {
						holder.cancle_focus_btn.setImageResource(R.drawable.card_icon_arrow);
					}
					
				}
			}
			
		}
	};
}
