package com.wh.bear.newbearweibo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.wh.bear.newbearweibo.R;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class WeiboMessageFragment extends Fragment implements OnItemClickListener {
	protected static final String TAG = WeiboMessageFragment.class.getName();
	ListView msg_list;
	List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	Oauth2AccessToken mToken;
//	StatusesAPI mStatusesAPI;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		return inflater.inflate(R.layout.weibomsg_list, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		msg_list = (ListView) view.findViewById(R.id.msg_list);
		
		fillData();
		SimpleAdapter adapter = new SimpleAdapter(getActivity(), data, R.layout.msg_item,
				new String[] { "title", "image" }, new int[] { R.id.msg_title, R.id.msg_image });
		msg_list.setAdapter(adapter);
		msg_list.setOnItemClickListener(this);
	}

	/**
	 * 填充listview数据源
	 */
	private void fillData() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("title", "@我的");
		map.put("image", R.drawable.messagescenter_at + "");
		data.add(map);
		map = new HashMap<String, String>();
		map.put("title", "评论");
		map.put("image", R.drawable.messagescenter_comments + "");
		data.add(map);
	}

	/**
	 * listview单项点击事件
	 * 
	 * @param parent
	 * @param view
	 * @param position
	 * @param id
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//		switch (position) {
//		case 0:
//
//			Intent intent = new Intent(getActivity(), WeiboAtmeActivity.class);
//			startActivity(intent);
//			((UserActivity)getActivity()).overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
//			break;
//
//		case 1:
//			Intent intent2=new Intent(getActivity(), CommentsAtmeActivity.class);
//			startActivity(intent2);
//			((UserActivity)getActivity()).overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
//			break;
//		}

	}

}
