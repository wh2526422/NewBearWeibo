package com.wh.bear.newbearweibo.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.AsyncWeiboRunner;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.net.WeiboParameters;
import com.sina.weibo.sdk.utils.LogUtil;
import com.wh.bear.newbearweibo.R;
import com.wh.bear.newbearweibo.utils.BearWeiboSetting;
import com.wh.bear.newbearweibo.utils.Constants;
import com.wh.bear.newbearweibo.utils.SQLiteOptionHelper;
import com.wh.bear.newbearweibo.utils.SearchUser;
import com.wh.bear.newbearweibo.widget.ParallaxScollListView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

public class WeiboSettingFragment extends Fragment implements OnQueryTextListener{
    protected static final String TAG = WeiboSettingFragment.class.getName();
	private ParallaxScollListView setting_list;//设置页面的列表
    private ImageView mImageView;
    private SearchView search_user;
    private ListView search_list;//联想列表
    List<String> mStrings;
    SQLiteOptionHelper helper;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.setting_list, container, false);
	}
	
	@SuppressLint("InflateParams")
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		setting_list=(ParallaxScollListView) view.findViewById(R.id.setting_list);
		View header = LayoutInflater.from(getActivity()).inflate(R.layout.sclist_header, null);
        mImageView = (ImageView) header.findViewById(R.id.layout_header_image);
        search_user=(SearchView) header.findViewById(R.id.search_user);
        search_list=(ListView) header.findViewById(R.id.search_list);
        //搜索数据库中的所有数据
        helper=new SQLiteOptionHelper(getActivity(), "WeiboData", 1);
        
       
        search_list.setTextFilterEnabled(true);
        
        setting_list.setZoomRatio(ParallaxScollListView.ZOOM_X2);
        setting_list.setParallaxImageView(mImageView);
        setting_list.addHeaderView(header);
        String model = BearWeiboSetting.read(getActivity());
        final String culFilesize = culFilesize();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                new String[]{
                        "通用设置    "+model,
                        "清理缓存    "+culFilesize
                }
        );
        setting_list.setAdapter(adapter);
		setting_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position==1) {
					AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
					builder.setTitle("请选择浏览模式");
					builder.setSingleChoiceItems(new String[]{"有图","无图"}, 0, new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which==0) {
								BearWeiboSetting.write(getActivity(), "有图");
							}else {
								BearWeiboSetting.write(getActivity(), "无图");
							}
						}
					});
					
					builder.setPositiveButton("确定", null);
					builder.setNegativeButton("取消", null);
					builder.show();
				}else {
					AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
					builder.setTitle("信息提示");
					builder.setMessage("是否确定清除缓存   "+culFilesize+"??");
					builder.setPositiveButton("确定", new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							clearFilecache();
						}
					});
					builder.setNegativeButton("取消", null);
					builder.show();
				}
				
			}
		});
		//设置是否自动缩小为图标
		search_user.setIconifiedByDefault(false);
		search_user.setOnQueryTextListener(this);
		search_user.setSubmitButtonEnabled(true);
		search_user.setQueryHint("请输入要查找用户昵称");
		search_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView tv=(TextView) view.findViewById(android.R.id.text1);
				CharSequence text = tv.getText();
				search_user.setQuery(text, true);
				
			}
		});
	}
	/**
	 * 计算文件夹中缓存大小
	 * @return
	 */
	private String culFilesize() {
		long size=0L;
		File cacheDir=new File(Environment.getExternalStorageDirectory(), "bearWeibo");
		File[] files = cacheDir.listFiles();
		for(File file:files){
			size+=file.length();
		}
		size=size/(1024);
		if (size>=1024) {
			size=size/1024;
			return size+"M";
		}
		return size+"KB";
	}
	/**
	 * 清理文件夹和缓存
	 */
	private void clearFilecache() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				ImageLoader imageLoader=ImageLoader.getInstance();
		 		imageLoader.clearDiscCache();
		 		imageLoader.clearMemoryCache();
				
			}
		}).start();
	}
	/**
	 * 搜索框监听,点击搜索按钮响应事件
	 */
	@Override
	public boolean onQueryTextSubmit(String query) {
		//先查找数据库中是否已有记录，没有则进行储存
		String text = helper.getSearchImformation(query);
		if (text==null) {
			helper.setSearchImformation(query);
		}
		Oauth2AccessToken token = AccessTokenKeeper.readAccessToken(getActivity());
		AsyncWeiboRunner runner=new AsyncWeiboRunner(getActivity());
		WeiboParameters params=new WeiboParameters(Constants.APP_KEY);
		params.put("access_token", token.getToken());
		params.put("q", query);
		params.put("count", 10);
		
		runner.requestAsync(
				"https://api.weibo.com/2/search/suggestions/users.json",
				params,
				"GET",
				listener);
		
		return false;
	}
	/**
	 * 搜索框内容改变监听
	 */
	@Override
	public boolean onQueryTextChange(String newText) {
		mStrings=helper.getSearchImformations();
		 search_list.setAdapter(new ArrayAdapter<String>(getActivity(),
	        		android.R.layout.simple_list_item_1, android.R.id.text1,mStrings));
		if (TextUtils.isEmpty(newText)) {
			//清除listview过滤
			search_list.clearTextFilter();
			search_list.setVisibility(View.GONE);
		}else {
			//使用用户输入的内容对listview列表项进行过滤
			search_list.setFilterText(newText);
			search_list.setVisibility(View.VISIBLE);
		}
		return true;
	}
	
	private RequestListener listener=new RequestListener() {
		
		@Override
		public void onWeiboException(WeiboException e) {
			LogUtil.e(TAG, e.getMessage());
//			ErrorInfo info = ErrorInfo.parse(e.getMessage());
//			Toast.makeText(getActivity(), info.toString(), Toast.LENGTH_LONG).show();
			
		}
		
		@Override
		public void onComplete(String response) {
//			ArrayList<SearchUser> users = SearchUser.parseSeachUsers(response);
//
//			Intent intent=new Intent(getActivity(),SearchUsersActivity.class);
//			Bundle bundle=new Bundle();
//			bundle.putParcelableArrayList("users", users);
//			intent.putExtras(bundle);
//			startActivity(intent);
			
		}
	};
}
