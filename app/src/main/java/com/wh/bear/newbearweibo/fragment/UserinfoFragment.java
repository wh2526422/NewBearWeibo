package com.wh.bear.newbearweibo.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.utils.LogUtil;
import com.wh.bear.newbearweibo.R;
import com.wh.bear.newbearweibo.openapi.models.User;
import com.wh.bear.newbearweibo.utils.UserinformationKeeper;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 主要用于显示用户的个人资料
 * @author Administrator
 *
 */
@SuppressLint("NewApi")
public class UserinfoFragment extends Fragment {
	private static final String TAG = UserinfoFragment.class.getName();
	ImageView user_icon;
	TextView user_name,description,weiboNum,attentionNum,fansNum,
				address,blogAddress,special_address;
	LinearLayout weibo_count_layout,focus_count_layout,fans_count_layout;
//	UsersAPI mUsersAPI;
	LoadIconTask task;//下载头像的任务
	User user;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.userinfo_layout, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		//头像
		user_icon=(ImageView) view.findViewById(R.id.user_icon);
		//昵称
		user_name=(TextView) view.findViewById(R.id.userName);
		//描述
		description=(TextView) view.findViewById(R.id.description);
		//微博数
		weibo_count_layout=(LinearLayout) view.findViewById(R.id.weibo_count_layout);
		weiboNum=(TextView) view.findViewById(R.id.weiboNum);
		//关注数
		focus_count_layout=(LinearLayout) view.findViewById(R.id.focus_count_layout);
		attentionNum=(TextView) view.findViewById(R.id.attentionNum);
		//粉丝数
		fans_count_layout=(LinearLayout) view.findViewById(R.id.fans_count_layout);
		fansNum=(TextView) view.findViewById(R.id.fansNum);
		//所在地
		address=(TextView) view.findViewById(R.id.address);
		//博客地址
		blogAddress=(TextView) view.findViewById(R.id.blogAddress);
		//个性化域名
		special_address=(TextView) view.findViewById(R.id.special_address);
		ConnectivityManager manager=(ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		
		Oauth2AccessToken mAccessToken = AccessTokenKeeper.readAccessToken(getActivity().getApplicationContext());
//		mUsersAPI = new UsersAPI(getActivity().getApplicationContext(), Constants.APP_KEY, mAccessToken);
		long uid = Long.parseLong(mAccessToken.getUid());
		User user= UserinformationKeeper.readInformation(getActivity());
//		if (info!=null&&info.isAvailable()) {
//			mUsersAPI.show(uid, mListener);
//		}
		if (user!=null) {
			setUserinfo(user);
		}
		
//		/* 当微博数被点击，显示用户发过的微博*/
//        weibo_count_layout.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Intent intent=new Intent(getActivity(),UsersendWeibosActivity.class);
//				startActivity(intent);
//				((UserActivity)getActivity()).overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
//			}
//		});
//        /*当关注数被点击时显示关注列表*/
//        focus_count_layout.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Intent intent=new Intent(getActivity(),FocusWeibousersActivity.class);
//				startActivity(intent);
//				((UserActivity)getActivity()).overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
//			}
//		});
//        /*当粉丝数被点击时显示粉丝列表*/
//        fans_count_layout.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Intent intent=new Intent(getActivity(),FansWeibousersActivity.class);
//				startActivity(intent);
//				((UserActivity)getActivity()).overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
//			}
//		});
	}
	
	private RequestListener mListener=new RequestListener() {
		
		@Override
		public void onWeiboException(WeiboException arg0) {
			LogUtil.e(TAG, arg0.getMessage());
//            ErrorInfo info = ErrorInfo.parse(arg0.getMessage());
//            Toast.makeText(getActivity().getApplicationContext(), info.toString(), Toast.LENGTH_LONG).show();
		}
		
		@Override
		public void onComplete(String arg0) {
			if (!TextUtils.isEmpty(arg0)) {
				user=User.parse(arg0);
				if (user!=null) {
					setUserinfo(user);
				}else {
					Toast.makeText(getActivity().getApplicationContext(), arg0, Toast.LENGTH_LONG).show();
				}
			}
			
		}
	};
	/**
	 * 下载头像
	 * @param profile_image_url
	 */
	private void setuserIcon(String profile_image_url) {
		task=new LoadIconTask();
		task.execute(profile_image_url);
	}
	
	class LoadIconTask extends AsyncTask<String, Void, Bitmap>{

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap bitmap=null;
			HttpURLConnection connection=null;
			try {
				URL url=new URL(params[0]);
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.setConnectTimeout(30000);
				if (connection.getResponseCode()==HttpURLConnection.HTTP_OK) {
					InputStream is = connection.getInputStream();
					bitmap=BitmapFactory.decodeStream(is);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (ProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				
				if (connection!=null) {
					connection.disconnect();
				}
				
			}
			return bitmap;
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			user_icon.setImageBitmap(result);
		}
	}
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (task!=null) {
			task.cancel(true);
		}
		if (user!=null) {
			UserinformationKeeper.writeInformation(getActivity(), user);
		}
	}
	
	private void setUserinfo(User user) {
		user_name.setText(user.screen_name);
		setuserIcon(user.profile_image_url);
		description.setText("简介："+user.description);
		weiboNum.setText(user.statuses_count+"");
		attentionNum.setText(user.friends_count+"");
		fansNum.setText(user.followers_count+"");
		address.setText(user.location);
		blogAddress.setText(user.url);
		special_address.setText(user.domain);
	}
	
	
}
