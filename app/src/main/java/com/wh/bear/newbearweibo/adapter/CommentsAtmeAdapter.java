package com.wh.bear.newbearweibo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.wh.bear.newbearweibo.R;
import com.wh.bear.newbearweibo.openapi.models.Comment;
import com.wh.bear.newbearweibo.openapi.models.Status;
import com.wh.bear.newbearweibo.openapi.models.User;
import com.wh.bear.newbearweibo.utils.StringUtils;

import java.util.ArrayList;

public class CommentsAtmeAdapter extends BaseAdapter {
	
	Context context;
	ArrayList<Comment> comments;
	ImageLoader imageLoader;
	
	public CommentsAtmeAdapter(Context context, ArrayList<Comment> comments) {
		this.context = context;
		this.comments = comments;
		imageLoader=ImageLoader.getInstance();
	}
	@Override
	public int getCount() {
		
		return comments.size();
	}

	@Override
	public Object getItem(int position) {
		return comments.get(position);
	}

	@Override
	public long getItemId(int position) {
		
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView==null) {
			convertView=View.inflate(context, R.layout.comments_item, null);
			//头部
			View header = convertView.findViewById(R.id.include_comment_header);
			ImageView user_imag = (ImageView) header.findViewById(R.id.user_imag);
			TextView user_name = (TextView) header.findViewById(R.id.user_name);
			TextView createtime = (TextView) header.findViewById(R.id.createtime);
			TextView weibosource = (TextView) header.findViewById(R.id.weibosource);
			Button reply=(Button) convertView.findViewById(R.id.reply);
			//内容
			TextView comment_text=(TextView) convertView.findViewById(R.id.comment_text);
			TextView reply_comment_text=(TextView) convertView.findViewById(R.id.reply_comment_text);
			//尾部内容
			View footer = convertView.findViewById(R.id.include_comment_footer);
			ImageView source_comment_img=(ImageView) footer.findViewById(R.id.source_comment_img);
			TextView source_comment_author=(TextView) footer.findViewById(R.id.source_comment_author);
			TextView source_comment_text=(TextView) footer.findViewById(R.id.source_comment_text);
			
			holder=new Holder(user_imag, user_name, createtime, weibosource, reply, comment_text, reply_comment_text, source_comment_img, source_comment_author, source_comment_text);
			convertView.setTag(holder);
		} else {
			holder=(Holder) convertView.getTag();
		}
		
		final Comment comment = comments.get(position);
		User user=comment.user;
		imageLoader.displayImage(user.profile_image_url, holder.user_imag);
		holder.user_name.setText(user.screen_name);
		String time = StringUtils.formatTime(comment.created_at);
		holder.createtime.setText(time);
		String text = comment.source;
		if (text!=null&&text.length()>0) {
			String source = "from " + text.substring(text.indexOf(">") + 1, text.lastIndexOf("<"));
			holder.weibosource.setText(source);
		}
//		//点击回复按钮
//		holder.reply.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Long comment_id=Long.parseLong(comment.id);
//				Long status_id=Long.parseLong(comment.status.id);
//				Intent intent=new Intent(context,PublishWeiboActivity.class);
//				intent.setAction(PublishWeiboActivity.REPLY_ACTION);
//				intent.putExtra("comment_id", comment_id);
//				intent.putExtra("status_id", status_id);
//				context.startActivity(intent);
//
//			}
//		});
		//微博内容
		holder.comment_text.setText(StringUtils.getWeiboContent(context, holder.comment_text, comment.text));
		//评论来源评论
		Comment reply_comment=comment.reply_comment;
		if (reply_comment!=null) {
			holder.reply_comment_text.setVisibility(View.VISIBLE);
			String s="@"+reply_comment.user.screen_name+":"+reply_comment.text;
			holder.reply_comment_text.setText(StringUtils.getWeiboContent(context, holder.reply_comment_text, s));
		}else {
			holder.reply_comment_text.setVisibility(View.GONE);
		}
		//评论微博信息
		Status status=comment.status;
		if (status.thumbnail_pic!=null) {
			imageLoader.displayImage(status.thumbnail_pic, holder.source_comment_img);
		}else {
			if (status.retweeted_status.thumbnail_pic!=null) {
				imageLoader.displayImage(status.retweeted_status.thumbnail_pic, holder.source_comment_img);
			}
		}
		holder.source_comment_author.setText(StringUtils.getWeiboContent(context, holder.source_comment_author, "@"+status.user.screen_name));
		String s=status.text;
		if (s.length()>=57) {
			s=s.substring(0, 54)+"...";
		}
		holder.source_comment_text.setText(StringUtils.getWeiboContent(context, holder.source_comment_text, s));
		return convertView;
	}
	
	
	class Holder{
		ImageView user_imag;
		TextView user_name;
		TextView createtime;
		TextView weibosource;
		Button reply;
		TextView comment_text;
		TextView reply_comment_text;
		ImageView source_comment_img;
		TextView source_comment_author;
		TextView source_comment_text;
		public Holder(ImageView user_imag, TextView user_name, TextView createtime, TextView weibosource, Button reply,
				TextView comment_text, TextView reply_comment_text, ImageView source_comment_img,
				TextView source_comment_author, TextView source_comment_text) {
			this.user_imag = user_imag;
			this.user_name = user_name;
			this.createtime = createtime;
			this.weibosource = weibosource;
			this.reply = reply;
			this.comment_text = comment_text;
			this.reply_comment_text = reply_comment_text;
			this.source_comment_img = source_comment_img;
			this.source_comment_author = source_comment_author;
			this.source_comment_text = source_comment_text;
		}
		
		
	}
	

}
