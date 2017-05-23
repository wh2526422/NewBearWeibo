package com.wh.bear.newbearweibo.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.utils.LogUtil;
import com.wh.bear.newbearweibo.R;
import com.wh.bear.newbearweibo.ReviewImagesActivity;
import com.wh.bear.newbearweibo.WeiboMessageActivity;
import com.wh.bear.newbearweibo.adapter.HomeListAdapter;
import com.wh.bear.newbearweibo.adapter.StatusGridImgsAdapter;
import com.wh.bear.newbearweibo.bean.OnRefreshListener;
import com.wh.bear.newbearweibo.bean.OnViewClickListener;
import com.wh.bear.newbearweibo.bean.OnViewsClickListener;
import com.wh.bear.newbearweibo.openapi.legacy.StatusesAPI;
import com.wh.bear.newbearweibo.openapi.models.ErrorInfo;
import com.wh.bear.newbearweibo.openapi.models.Status;
import com.wh.bear.newbearweibo.openapi.models.StatusList;
import com.wh.bear.newbearweibo.openapi.models.User;
import com.wh.bear.newbearweibo.utils.Constants;
import com.wh.bear.newbearweibo.utils.PictureLoader;
import com.wh.bear.newbearweibo.utils.SQLiteOptionHelper;
import com.wh.bear.newbearweibo.utils.log;
import com.wh.bear.newbearweibo.widget.BearListView;

import java.util.ArrayList;
import java.util.List;

public class WeiboHomeFragment extends Fragment {
	protected static final String TAG = WeiboHomeFragment.class.getName();

	public static final int START_ACTION = 1;
	public static final int REFRESH_ACTION = 2;
	public static final int LOAD_ACTION = 3;
	private int option;
	BearListView msg_list;
	ImageView expend;
	StatusesAPI mStatusesAPI;
	Oauth2AccessToken mAccessToken;
	HomeListAdapter adapter;
	ArrayList<Status> status;
	int currentPage=1;//记录当前页
	SQLiteOptionHelper helper;
	ArrayList<Status> dblist;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		return inflater.inflate(R.layout.weibohome_list, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		msg_list = (BearListView) view.findViewById(R.id.home_list);
		expend = (ImageView) view.findViewById(R.id.expend);
		
		helper=new SQLiteOptionHelper(getActivity(), "WeiboData", 1);
		dblist = helper.getStatuses(SQLiteOptionHelper.STATUS_PUBLIC);
		
		// 获取已保存的token信息
		mAccessToken = AccessTokenKeeper.readAccessToken(getActivity());
		// 实例化StatusesAPI
		mStatusesAPI = new StatusesAPI(getActivity(), Constants.APP_KEY, mAccessToken);
		log.d(TAG,"oncreateview");
		if (dblist!=null&&dblist.size()>0) {
			adapter = new HomeListAdapter(getActivity(), dblist, WeiboHomeFragment.this);
			msg_list.setAdapter(adapter);
			registerAdapterListenner(adapter);
		}else {
			// 获得微博列表
			log.d(TAG,"获取微博列表");

			mStatusesAPI.homeTimeline(0L, 0L, 20, 1, false, 0, false, mListener);
		}
		

		/**
		 * 刷新、加载监听
		 */
		msg_list.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onLoadingMore(View view) {
				currentPage++;
				option = LOAD_ACTION;
				mStatusesAPI.friendsTimeline(0L, 0L, 20, currentPage, false, 0, false, mListener);
				adapter.notifyDataSetChanged();

			}

			@Override
			public void onDownPullRefresh(View view) {
				option = REFRESH_ACTION;
				mStatusesAPI.friendsTimeline(0L, 0L, 20, 1, false, 0, false, mListener);
			}
		});
		
		msg_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Status st;
				if (status==null) {
					st = dblist.get(position-1);
				}else {
					st=status.get(position-1);
				}

				Intent intent=new Intent(getActivity(),WeiboMessageActivity.class);
				Bundle bundle=new Bundle();
				bundle.putString("id", st.id);
				User user = st.user;
				bundle.putString("profile_image_url", user.profile_image_url);
				bundle.putString("screen_name",user.screen_name);
				bundle.putString("created_at",st.created_at);
				String text = st.source;
				if (text != null && text.length() > 0) {
					String source = "from " + text.substring(text.indexOf(">") + 1, text.lastIndexOf("<"));
					bundle.putString("source",source);
				}
				// 设置内容
				bundle.putString("text",st.text);
				bundle.putStringArrayList("pic_urls",st.pic_urls);
				// 设置转发内容
				Status retweeted_status = st.retweeted_status;

				if (retweeted_status!=null) {
					String s = "@" + retweeted_status.user.screen_name + ":" + retweeted_status.text;
					bundle.putString("retweeted_status_text", s);
					bundle.putStringArrayList("retweeted_status_pic_urls", retweeted_status.pic_urls);
				}
				// 设置尾部
				intent.putExtras(bundle);
				startActivity(intent);

			}
		});

	}

	/**
	 * 微博 OpenAPI 回调接口。
	 */
	private RequestListener mListener = new RequestListener() {

		@Override
		public void onWeiboException(WeiboException arg0) {
			LogUtil.e(TAG, arg0.getMessage());
			ErrorInfo info = ErrorInfo.parse(arg0.getMessage());
			Toast.makeText(getActivity(), info.toString(), Toast.LENGTH_LONG).show();
			if (option == REFRESH_ACTION) {
				msg_list.refreshFailed();
			} else {
				msg_list.loadFailed();
			}
		}

		@Override
		public void onComplete(String response) {
			log.i(TAG,response);
			if (!TextUtils.isEmpty(response)) {
				if (response.startsWith("{\"statuses\"")) {
					// 调用 StatusList#parse 解析字符串成微博列表对象
					StatusList statuses = StatusList.parse(response);
					List<Status> list = statuses.statusList;
					if (option == LOAD_ACTION) {
						if (status==null) {
							status=dblist;
						}
						status.addAll(list);
						adapter.notifyDataSetChanged();
						msg_list.loaded();
					} else {
						status = statuses.statusList;
						adapter = new HomeListAdapter(getActivity(), status, WeiboHomeFragment.this);
						msg_list.setAdapter(adapter);
						msg_list.refreshed();
						registerAdapterListenner(adapter);
						storeData(status);
					}
				} else {
					if (option == REFRESH_ACTION) {
						msg_list.refreshFailed();
					} else {
						msg_list.loadFailed();
					}

				}
			}

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
				Intent intent = new Intent(getActivity(), ReviewImagesActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("original_pic", imageUrl);
				intent.putExtras(bundle);
				intent.setAction(ReviewImagesActivity.SINGLE);
				getActivity().startActivity(intent);

				getActivity().overridePendingTransition(R.anim.enter_set, 0);
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
				Intent intent = new Intent(getActivity(), ReviewImagesActivity.class);
				Bundle bundle = new Bundle();
				bundle.putStringArrayList("pic_urls", imgPathses);
				bundle.putInt("position", position);
				intent.putExtras(bundle);
				intent.setAction(ReviewImagesActivity.MORE);
				getActivity().startActivity(intent);
				getActivity().overridePendingTransition(R.anim.enter_set, 0);

			}
		});

	}
	/**
	 * 储存数据
	 * @param status
	 */
	public void storeData(final ArrayList<Status> status) {
		if (status!=null) {

			new Thread(new Runnable() {

				@Override
				public void run() {
					helper.clearStatus(SQLiteOptionHelper.STATUS_PUBLIC,SQLiteOptionHelper.USER_INSTATUS);
					helper.setStatus(status, SQLiteOptionHelper.STATUS_PUBLIC);
				}
			}).start();

		}
	}

}
