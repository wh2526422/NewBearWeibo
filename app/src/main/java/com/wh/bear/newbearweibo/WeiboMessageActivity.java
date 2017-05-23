package com.wh.bear.newbearweibo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.utils.LogUtil;
import com.wh.bear.newbearweibo.adapter.StatusGridImgsAdapter;
import com.wh.bear.newbearweibo.adapter.WeiboMessageAdapter;
import com.wh.bear.newbearweibo.bean.OnRefreshListener;
import com.wh.bear.newbearweibo.bean.OnViewClickListener;
import com.wh.bear.newbearweibo.bean.OnViewsClickListener;
import com.wh.bear.newbearweibo.openapi.CommentsAPI;
import com.wh.bear.newbearweibo.openapi.StatusMessage;
import com.wh.bear.newbearweibo.openapi.models.Comment;
import com.wh.bear.newbearweibo.openapi.models.CommentList;
import com.wh.bear.newbearweibo.openapi.models.ErrorInfo;
import com.wh.bear.newbearweibo.utils.Constants;
import com.wh.bear.newbearweibo.widget.BearListView;

import java.util.ArrayList;
import java.util.List;

public class WeiboMessageActivity extends Activity{
	public static final int START_ACTION = 1;
	public static final int REFRESH_ACTION = 2;
	public static final int LOAD_ACTION = 3;
	protected static final String TAG = WeiboMessageActivity.class.getName();
	ImageLoader imageLoader;
	BearListView weibo_msg_list;
	LinearLayout transmit_layout;
	LinearLayout comment_layout;
	LinearLayout thumbUp_layout;
	StatusMessage message;
	CommentsAPI mApi;
	List<StatusMessage> data;
	long id;
	String profile_image_url;
	String screen_name;
	String text;
	String retweeted_status_text;
	private int currentPage=1;
	private int option;
	WeiboMessageAdapter adapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weibo_message_layout);
		
		weibo_msg_list=(BearListView) findViewById(R.id.weibo_message_list);
		View footer=findViewById(R.id.include_weibo_msg_footer);
		transmit_layout = (LinearLayout) footer.findViewById(R.id.transmit_layout);
		comment_layout = (LinearLayout) footer.findViewById(R.id.comment_layout);
		thumbUp_layout = (LinearLayout) footer.findViewById(R.id.thumbUp_layout);
		
		Oauth2AccessToken token = AccessTokenKeeper.readAccessToken(this);
		mApi=new CommentsAPI(this, Constants.APP_KEY, token);
		Intent intent = getIntent();
		initMessage(intent);
		
		/**
		 * 转发事件监听
		 */
		transmit_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(WeiboMessageActivity.this, PublishWeiboActivity.class);
				intent.setAction(PublishWeiboActivity.RETWEETED_ACTION);
				Bundle bundle=new Bundle();
				bundle.putLong("id", id);
				bundle.putString("thumbnail_pic", profile_image_url);
				bundle.putString("screen_name", screen_name);
				if (retweeted_status_text!=null) {
					bundle.putString("text", retweeted_status_text);
				}else {
					bundle.putString("text", text);
				}
				
				intent.putExtras(bundle);
				WeiboMessageActivity.this.startActivity(intent);
				
			}
		});
		
		/**
		 * 评论事件监听
		 */
		comment_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(WeiboMessageActivity.this, PublishWeiboActivity.class);
				intent.setAction(PublishWeiboActivity.COMMENT_ACTION);
				intent.putExtra("id", id);
				WeiboMessageActivity.this.startActivity(intent);

			}
		});
		weibo_msg_list.setHeaderviewVisblityGone();
		weibo_msg_list.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onLoadingMore(View view) {
				currentPage++;
				option = LOAD_ACTION;
				mApi.show(id, 0L, 0L, 20, currentPage, 0, mlListener);
				adapter.notifyDataSetChanged();
				
			}
			
			@Override
			public void onDownPullRefresh(View view) {
				// TODO Auto-generated method stub
			}
		});
		
	}
	/**
	 * 获取上个activity传递来的信息
	 * @param intent
	 */
	private void initMessage(Intent intent) {
		Bundle bundle = intent.getExtras();
		id = Long.parseLong(bundle.getString("id"));
		profile_image_url = bundle.getString("profile_image_url");
		screen_name = bundle.getString("screen_name");
		String created_at = bundle.getString("created_at");
		
		String source = bundle.getString("source");
		text = bundle.getString("text");
		ArrayList<String> pic_urls = bundle.getStringArrayList("pic_urls");
		retweeted_status_text = bundle.getString("retweeted_status_text");
		
		ArrayList<String> retweeted_status_pic_urls = bundle.getStringArrayList("retweeted_status_pic_urls");
		message=new StatusMessage(id+"", profile_image_url, 
				screen_name, created_at, source, text, pic_urls,
				retweeted_status_text, retweeted_status_pic_urls);
		data=new ArrayList<StatusMessage>();
		data.add(message);
		
		mApi.show(id, 0L, 0L, 20, 1, 0, mlListener);
	}
	
	private RequestListener mlListener=new RequestListener() {
		
		@Override
		public void onWeiboException(WeiboException arg0) {
			LogUtil.e(TAG, arg0.getMessage());
			ErrorInfo info = ErrorInfo.parse(arg0.getMessage());
			Toast.makeText(WeiboMessageActivity.this, info.toString(), Toast.LENGTH_LONG).show();
			if (option == REFRESH_ACTION) {
				weibo_msg_list.refreshFailed();
			} else {
				weibo_msg_list.loadFailed();
			}
			
		}
		
		@Override
		public void onComplete(String arg0) {
			if (!TextUtils.isEmpty(arg0)) {
				if (arg0.startsWith("{\"comments\"")) {
					CommentList commentList = CommentList.parse(arg0);
					Log.i("commentList", commentList.total_number+"");
					ArrayList<Comment> comments=commentList.commentList;;
					List<StatusMessage> list = new ArrayList<StatusMessage>();
					if (option == LOAD_ACTION) {
						if (comments!=null) {
							for (Comment comment : comments) {
								StatusMessage message = new StatusMessage();
								message.setComment(comment);
								list.add(message);
							} 
						}
						data.addAll(list);
						adapter.notifyDataSetChanged();
						weibo_msg_list.loaded();
					} else {
						
						if (comments!=null) {
							for (Comment comment : comments) {
								StatusMessage message = new StatusMessage();
								message.setComment(comment);
								data.add(message);
							} 
						}
						adapter=new WeiboMessageAdapter(WeiboMessageActivity.this, data);
						weibo_msg_list.setAdapter(adapter);
					}
					
					registerAdapterListenner(adapter);
					
				}
			}
			
		}
	};
	
	/**
	 * 注册监听单图点击事件
	 * 
	 * @param adapter
	 */
	public void registerAdapterListenner(WeiboMessageAdapter adapter) {
		adapter.setOnImageClickListener(new OnViewClickListener() {

			@Override
			public void OnViewClick(View view, String imageUrl) {
				Intent intent = new Intent(WeiboMessageActivity.this, ReviewImagesActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("original_pic", imageUrl);
				intent.putExtras(bundle);
				intent.setAction(ReviewImagesActivity.SINGLE);
				WeiboMessageActivity.this.startActivity(intent);
				
				WeiboMessageActivity.this.overridePendingTransition(R.anim.enter_set, 0);
			}
		});

	}
	
	/**
	 * 注册监听多图点击事件
	 * 
	 * @param gridAdapter
	 */
	public void registerGridAdapterListenner(StatusGridImgsAdapter gridAdapter) {
		gridAdapter.setOnImagesClickListener(new OnViewsClickListener() {

			@Override
			public void OnViewsClick(View view, ArrayList<String> imgPathses,int position) {
				Intent intent = new Intent(WeiboMessageActivity.this, ReviewImagesActivity.class);
				Bundle bundle = new Bundle();
				bundle.putStringArrayList("pic_urls", imgPathses);
				bundle.putInt("position", position);
				intent.putExtras(bundle);
				intent.setAction(ReviewImagesActivity.MORE);
				WeiboMessageActivity.this.startActivity(intent);
				WeiboMessageActivity.this.overridePendingTransition(R.anim.enter_set, 0);

			}
		});
	}
}
