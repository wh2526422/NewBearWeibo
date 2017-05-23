package com.wh.bear.newbearweibo.adapter;

import java.util.ArrayList;
import java.util.List;


import com.nostra13.universalimageloader.core.ImageLoader;
import com.wh.bear.newbearweibo.R;
import com.wh.bear.newbearweibo.bean.OnViewClickListener;
import com.wh.bear.newbearweibo.openapi.StatusMessage;
import com.wh.bear.newbearweibo.openapi.models.Comment;
import com.wh.bear.newbearweibo.utils.StringUtils;

import android.content.Context;
import android.text.SpannableString;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeiboMessageAdapter extends BaseAdapter{
	Context context;
	List<StatusMessage> data;
	ImageLoader imageLoader;
	private OnViewClickListener mListener;
	public WeiboMessageAdapter(Context context, List<StatusMessage> data) {
		this.context = context;
		this.data = data;
		imageLoader=ImageLoader.getInstance();
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
	public boolean isEnabled(int position) {
		if (position==0) {
			return false;
		}
		return super.isEnabled(position);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			convertView = View.inflate(context, R.layout.home_item, null);
			// 头布局
			View header = convertView.findViewById(R.id.include_home_header);
			ImageView user_imag = (ImageView) header.findViewById(R.id.user_imag);
			TextView user_name = (TextView) header.findViewById(R.id.user_name);
			TextView createtime = (TextView) header.findViewById(R.id.createtime);
			TextView weibosource = (TextView) header.findViewById(R.id.weibosource);

			// 内容
			View body = convertView.findViewById(R.id.include_home_body_text);
			TextView text = (TextView) body.findViewById(R.id.msg_body);
			FrameLayout flayout = (FrameLayout) body.findViewById(R.id.flayout);
			GridView img_layout = (GridView) body.findViewById(R.id.img_layout);
			ImageView img_single = (ImageView) body.findViewById(R.id.img_single);
			// 转发内容
			LinearLayout transmit = (LinearLayout) convertView.findViewById(R.id.transmit);
			View ret_body = convertView.findViewById(R.id.include_home_body_ret_text);

			TextView ret_text = (TextView) ret_body.findViewById(R.id.msg_body);
			FrameLayout ret_flayout = (FrameLayout) ret_body.findViewById(R.id.flayout);
			GridView ret_img_layout = (GridView) ret_body.findViewById(R.id.img_layout);
			ImageView ret_img_single = (ImageView) ret_body.findViewById(R.id.img_single);

			// 尾部
			View footer = convertView.findViewById(R.id.include_home_footer);
			holder = new Holder(user_imag, user_name, createtime, weibosource, text, flayout, img_layout, img_single,
					transmit, ret_text, ret_flayout, ret_img_layout, ret_img_single, footer);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		// 设置头部
		StatusMessage message = data.get(position);
		
		if (position==0) {
			imageLoader.displayImage(message.getProfile_image_url(), holder.user_imag);
			holder.user_name.setText(message.getScreen_name());
			holder.createtime.setText(message.getCreated_at());
			holder.weibosource.setText(message.getSource());
			//设置内容
			SpannableString content = StringUtils.getWeiboContent(context,
			holder.text, message.getText());
			holder.text.setText(content);
			setImages(message.getPic_urls(), holder.flayout, holder.img_layout, holder.img_single);
			holder.ret_text.setVisibility(View.VISIBLE);
			holder.ret_text.setText(message.getRetweeted_status_text());
			setImages(message.getRetweeted_status_pic_urls(), holder.ret_flayout, holder.ret_img_layout,
					holder.ret_img_single);
		}else {
			Comment comment = message.getComment();
			if (comment!=null) {
				imageLoader.displayImage(comment.user.profile_image_url, holder.user_imag);
				holder.user_name.setText(comment.user.screen_name);
				String time = StringUtils.formatTime(comment.created_at);
				holder.createtime.setText(time);
				String text=comment.source;
				if (text!=null&&text.length()>0) {
					String source = "from " + text.substring(text.indexOf(">") + 1, text.lastIndexOf("<"));
					holder.weibosource.setText(source);
				}
				SpannableString content = StringUtils.getWeiboContent(context,
						holder.text, comment.text);
				holder.text.setText(content);
				holder.flayout.setVisibility(View.GONE);
				holder.ret_text.setVisibility(View.GONE);
				holder.ret_flayout.setVisibility(View.GONE);
			}
		}
		
		holder.footer.setVisibility(View.GONE);

		return convertView;
	}
	class Holder {
		ImageView user_imag;
		TextView user_name;
		TextView createtime;
		TextView weibosource;
		// 内容
		TextView text;
		FrameLayout flayout;
		GridView img_layout;
		ImageView img_single;
		// 转发内容
		LinearLayout transmit;
		TextView ret_text;
		FrameLayout ret_flayout;
		GridView ret_img_layout;
		ImageView ret_img_single;
		// 尾部
		View footer;
		public Holder(ImageView user_imag, TextView user_name, TextView createtime, TextView weibosource, TextView text,
				FrameLayout flayout, GridView img_layout, ImageView img_single, LinearLayout transmit,
				TextView ret_text, FrameLayout ret_flayout, GridView ret_img_layout, ImageView ret_img_single,
				View footer) {
			this.user_imag = user_imag;
			this.user_name = user_name;
			this.createtime = createtime;
			this.weibosource = weibosource;
			this.text = text;
			this.flayout = flayout;
			this.img_layout = img_layout;
			this.img_single = img_single;
			this.transmit = transmit;
			this.ret_text = ret_text;
			this.ret_flayout = ret_flayout;
			this.ret_img_layout = ret_img_layout;
			this.ret_img_single = ret_img_single;
			this.footer = footer;
		}
		
		
	}
	
	/**
	 * 设置图片显示
	 * 
	 * @param pic_urls
	 * @param imgContainer
	 * @param gv_images
	 * @param iv_image
	 */
	private void setImages(final ArrayList<String> pic_urls,FrameLayout imgContainer, GridView gv_images, final ImageView iv_image) {
		
		if (pic_urls != null && pic_urls.size() > 1) {
			imgContainer.setVisibility(View.VISIBLE);
			gv_images.setVisibility(View.VISIBLE);
			iv_image.setVisibility(View.GONE);
			
			StatusGridImgsAdapter gvAdapter = new StatusGridImgsAdapter(context, pic_urls);
			gv_images.setAdapter(gvAdapter);
			
//			if (context!=null) {
//				((WeiboMessageActivity)context).registerGridAdapterListenner(gvAdapter);
//			}
		} else if (pic_urls != null && pic_urls.size() == 1) {
			imgContainer.setVisibility(View.VISIBLE);
			gv_images.setVisibility(View.GONE);
			iv_image.setVisibility(View.VISIBLE);
			imageLoader.displayImage(pic_urls.get(0), iv_image);
			//图片点击
			iv_image.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (mListener!=null) {
						String url=pic_urls.get(0).replace("thumbnail", "large");
						mListener.OnViewClick(iv_image,url);
					}
				}
			});
		} else {
			imgContainer.setVisibility(View.GONE);
		}
	}

	public void setOnImageClickListener(OnViewClickListener onViewClickListener) {
		this.mListener=onViewClickListener;
		
	}

}
