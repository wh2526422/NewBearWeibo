package com.wh.bear.newbearweibo.adapter;

import java.util.ArrayList;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wh.bear.newbearweibo.R;
import com.wh.bear.newbearweibo.utils.SearchUser;

public class SearchUserAdapter extends BaseAdapter {
	ArrayList<SearchUser> data;
	Context context;
	
	public SearchUserAdapter(ArrayList<SearchUser> data, Context context) {
		this.data = data;
		this.context = context;
		
	}

	@Override
	public int getCount() {
		
		return data.size();
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
//		Holder holder;
//		if (convertView==null) {
//			convertView=View.inflate(context, R.layout.search_user_item, null);
//			TextView screen_name=(TextView) convertView.findViewById(R.id.search_user_name);
//			TextView fans_count=(TextView) convertView.findViewById(R.id.search_user_fans);
////			AttentionComponentView attentionView=(AttentionComponentView) convertView.findViewById(R.id.attentionView);
////			holder=new Holder(screen_name, fans_count,attentionView);
////			convertView.setTag(holder);
//		}else {
//			holder=(Holder) convertView.getTag();
//		}
//
//		SearchUser searchUser = data.get(position);
//		holder.screen_name.setText(searchUser.screen_name);
//		holder.fans_count.setText("粉丝"+searchUser.followers_count);
////		holder.attentionView.setVisibility(View.INVISIBLE);
//
		return convertView;
	}
	
	class Holder{
		TextView screen_name;
		TextView fans_count;
//		AttentionComponentView attentionView;
//		public Holder(TextView screen_name, TextView fans_count,AttentionComponentView attentionView) {
//			this.screen_name = screen_name;
//			this.fans_count = fans_count;
//			this.attentionView=attentionView;
//		}
//
		
		
	}
}
