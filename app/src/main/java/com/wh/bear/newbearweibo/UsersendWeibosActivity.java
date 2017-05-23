package com.wh.bear.newbearweibo;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.utils.LogUtil;
import com.wh.bear.newbearweibo.adapter.HomeListAdapter;
import com.wh.bear.newbearweibo.adapter.StatusGridImgsAdapter;
import com.wh.bear.newbearweibo.bean.OnRefreshListener;
import com.wh.bear.newbearweibo.bean.OnViewClickListener;
import com.wh.bear.newbearweibo.bean.OnViewsClickListener;
import com.wh.bear.newbearweibo.openapi.legacy.StatusesAPI;
import com.wh.bear.newbearweibo.openapi.models.ErrorInfo;
import com.wh.bear.newbearweibo.openapi.models.Status;
import com.wh.bear.newbearweibo.openapi.models.StatusList;
import com.wh.bear.newbearweibo.utils.Constants;
import com.wh.bear.newbearweibo.utils.SQLiteOptionHelper;
import com.wh.bear.newbearweibo.widget.BearListView;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户发自己发的微博浏览界面
 * @author Administrator
 *
 */
public class UsersendWeibosActivity extends Activity {
	protected static final String TAG = UsersendWeibosActivity.class.getName();
	BearListView msg_list;
	HomeListAdapter adapter;
	StatusesAPI mApi;
	
	private int currentPage=1;
	private int option;//表示当前进行的动作
	
	ArrayList<Status> status;
	SQLiteOptionHelper helper;
	ArrayList<Status> dblist;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weiboatme_list);
		
		msg_list=(BearListView) findViewById(R.id.wbat_list);
		
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(getResources().getString(R.string.back));
		actionBar.setDisplayHomeAsUpEnabled(true);
		//从数据库读取数据
		helper=new SQLiteOptionHelper(this, "WeiboData", 1);
		dblist=helper.getStatuses(SQLiteOptionHelper.STATUS_SEND);
		
		Oauth2AccessToken mToken= AccessTokenKeeper.readAccessToken(this);
		mApi=new StatusesAPI(this, Constants.APP_KEY, mToken);
		//如果数据库有数据则读取数据库
		if (dblist!=null&&dblist.size()>0) {
			adapter = new HomeListAdapter(UsersendWeibosActivity.this, dblist,null);
			msg_list.setAdapter(adapter);
			registerAdapterListenner(adapter);
		}else {
			mApi.userTimeline(0L, 0L, 5, 1, false, 0, false, listener);
		}
		
		msg_list.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onLoadingMore(View view) {
				currentPage++;
				option = BearListView.LOAD_ACTION;
				mApi.userTimeline(0L, 0L, 50, currentPage, false, 0, false, listener);
				adapter.notifyDataSetChanged();
				
			}
			
			@Override
			public void onDownPullRefresh(View view) {
				option = BearListView.REFRESH_ACTION;
				mApi.userTimeline(0L, 0L, 5, 1, false, 0, false, listener);
			}
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	private RequestListener listener=new RequestListener() {
		
		@Override
		public void onWeiboException(WeiboException e) {
			LogUtil.e(TAG, e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(UsersendWeibosActivity.this, info.toString(), Toast.LENGTH_LONG).show();
            if (option == BearListView.REFRESH_ACTION) {
				msg_list.refreshFailed();
			} else {
				msg_list.loadFailed();
			}
		}
		
		@Override
		public void onComplete(String response) {
			LogUtil.i(TAG, response);
			if (!TextUtils.isEmpty(response)) {
				if (response.startsWith("{\"statuses\"")) {
					// 调用 StatusList#parse 解析字符串成微博列表对象
					StatusList statuses = StatusList.parse(response);
					if(statuses!=null&&statuses.total_number>0){
						List<Status> list = statuses.statusList;
						
						if (option == BearListView.LOAD_ACTION) {
							if (list!=null) {
								if (status==null) {
									status=dblist;
								}
								status.addAll(list);
								adapter.notifyDataSetChanged();
								msg_list.loaded();
							}else {
								msg_list.loadFailed();
							}
							
						} else {
							status = statuses.statusList;
							adapter = new HomeListAdapter(UsersendWeibosActivity.this, status,null);
							msg_list.setAdapter(adapter);
							msg_list.refreshed();
							storeData(status);
							registerAdapterListenner(adapter);
						}
					}
				}else {
					if (option == BearListView.REFRESH_ACTION) {
						msg_list.refreshFailed();
					} else {
						msg_list.loadFailed();
					}

				}
			}
			
		}
	};
	protected void storeData(final ArrayList<Status> status) {

		if (status!=null) {
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					helper.clearStatus(SQLiteOptionHelper.STATUS_SEND,SQLiteOptionHelper.USER_INSTATUS);
					helper.setStatus(status, SQLiteOptionHelper.STATUS_SEND);
				}
			}).start();
			
		}
		
	};
	
	/**
	 * 注册监听单图点击事件
	 * 
	 * @param adapter
	 */
	public void registerAdapterListenner(HomeListAdapter adapter) {
		adapter.setOnImageClickListener(new OnViewClickListener() {

			@Override
			public void OnViewClick(View view, String imageUrl) {
				Intent intent = new Intent(UsersendWeibosActivity.this, ReviewImagesActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("original_pic", imageUrl);
				intent.putExtras(bundle);
				intent.setAction(ReviewImagesActivity.SINGLE);
				UsersendWeibosActivity.this.startActivity(intent);
				
				UsersendWeibosActivity.this.overridePendingTransition(R.anim.enter_set, 0);
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
				Intent intent = new Intent(UsersendWeibosActivity.this, ReviewImagesActivity.class);
				Bundle bundle = new Bundle();
				bundle.putStringArrayList("pic_urls", imgPathses);
				bundle.putInt("position", position);
				intent.putExtras(bundle);
				intent.setAction(ReviewImagesActivity.MORE);
				UsersendWeibosActivity.this.startActivity(intent);
				UsersendWeibosActivity.this.overridePendingTransition(R.anim.enter_set, 0);

			}
		});

	}
}
