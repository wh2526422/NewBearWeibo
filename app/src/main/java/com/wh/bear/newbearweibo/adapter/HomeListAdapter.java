package com.wh.bear.newbearweibo.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.AsyncWeiboRunner;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.net.WeiboParameters;
import com.wh.bear.newbearweibo.PublishWeiboActivity;
import com.wh.bear.newbearweibo.R;
import com.wh.bear.newbearweibo.UsersendWeibosActivity;
import com.wh.bear.newbearweibo.bean.OnViewClickListener;
import com.wh.bear.newbearweibo.fragment.WeiboHomeFragment;
import com.wh.bear.newbearweibo.openapi.models.Status;
import com.wh.bear.newbearweibo.openapi.models.User;
import com.wh.bear.newbearweibo.utils.BearWeiboSetting;
import com.wh.bear.newbearweibo.utils.Constants;
import com.wh.bear.newbearweibo.utils.PictureLoader;
import com.wh.bear.newbearweibo.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class HomeListAdapter extends BaseAdapter {
	private Context context;
	private List<Status> statuses;
	private OnViewClickListener mListener;
//	ImageLoader imageLoader;
	WeiboHomeFragment fragment;
	Holder holder;
	PictureLoader pictureLoader;
	int count=0;//用于标记是否点赞
	public HomeListAdapter(Context context, List<Status> status, WeiboHomeFragment fragment) {
		this.context = context;
		this.statuses = status;
		this.fragment=fragment;
//		imageLoader = ImageLoader.getInstance();
		pictureLoader = PictureLoader.getInstance();

	}
	
	public void setOnImageClickListener(OnViewClickListener listener) {
		this.mListener=listener;
	}
	
	@Override
	public int getCount() {
		if (statuses == null) {
			return 0;
		}
		return statuses.size();
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
			LinearLayout transmit_layout = (LinearLayout) footer.findViewById(R.id.transmit_layout);
			TextView transmitNum = (TextView) footer.findViewById(R.id.transmitNum);
			LinearLayout comment_layout = (LinearLayout) footer.findViewById(R.id.comment_layout);
			TextView commentsNum = (TextView) footer.findViewById(R.id.commentsNum);
			LinearLayout thumbUp_layout = (LinearLayout) footer.findViewById(R.id.thumbUp_layout);
			ImageView thumbup_image = (ImageView) footer.findViewById(R.id.thumbup_image);
			TextView thumbUpNum = (TextView) footer.findViewById(R.id.thumbUpNum);
			holder = new Holder(user_imag, user_name, createtime, weibosource, text, flayout, img_layout, img_single,
					transmit, ret_text, ret_flayout, ret_img_layout, ret_img_single, transmit_layout, transmitNum,
					comment_layout, commentsNum, thumbUp_layout, thumbup_image, thumbUpNum);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		String model = BearWeiboSetting.read(context);
		// 设置头部
		final Status status = statuses.get(position);
		User user = status.user;
//		imageLoader.displayImage(user.profile_image_url, holder.user_imag);
		pictureLoader.displayImage(user.profile_image_url,holder.user_imag);
		holder.user_name.setText(user.screen_name);
		String time = StringUtils.formatTime(status.created_at);
		holder.createtime.setText(time);
		String text = status.source;
		if (text != null && text.length() > 0) {
			String source = "from " + text.substring(text.indexOf(">") + 1, text.lastIndexOf("<"));
			holder.weibosource.setText(source);
		}
		// 设置内容
		SpannableString content = StringUtils.getWeiboContent(context, holder.text, status.text);
		holder.text.setText(content);
		if ("有图".equals(model)) {
			setImages(status, holder.flayout, holder.img_layout, holder.img_single);
		}

		// 设置转发内容
		Status retweeted_status = status.retweeted_status;
		if (retweeted_status != null&&retweeted_status.user!=null) {
			holder.transmit.setVisibility(View.VISIBLE);
			String s="@" + retweeted_status.user.screen_name + ":" + retweeted_status.text;
			SpannableString content2 = StringUtils.getWeiboContent(context, holder.ret_text, s);
			holder.ret_text.setText(content2);
			if ("有图".equals(model)) {
				setImages(retweeted_status, holder.ret_flayout, holder.ret_img_layout, holder.ret_img_single);
			}
		} else {
			holder.transmit.setVisibility(View.GONE);
		}

		// 设置尾部
		if (status.reposts_count != 0) {
			holder.transmitNum.setText("" + status.reposts_count);
		}
		/**
		 * 转发事件监听
		 */
		holder.transmit_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				long id = Long.parseLong(status.id);
				Intent intent = new Intent(context, PublishWeiboActivity.class);
				intent.setAction(PublishWeiboActivity.RETWEETED_ACTION);
				Bundle bundle=new Bundle();
				bundle.putLong("id", id);

				bundle.putString("screen_name", status.user.screen_name);
				if (status.retweeted_status!=null) {
					bundle.putString("thumbnail_pic", status.retweeted_status.thumbnail_pic);
					bundle.putString("text", status.retweeted_status.text);
				}else {
					bundle.putString("thumbnail_pic", status.thumbnail_pic);
					bundle.putString("text", status.text);

				}

				intent.putExtras(bundle);
				context.startActivity(intent);

			}
		});
		if (status.comments_count != 0) {
			holder.commentsNum.setText("" + status.comments_count);
		}
		/**
		 * 评论事件监听
		 */
		holder.comment_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Long id = Long.parseLong(status.id);
				Intent intent = new Intent(context, PublishWeiboActivity.class);
				intent.setAction(PublishWeiboActivity.COMMENT_ACTION);
				intent.putExtra("id", id);
				context.startActivity(intent);

			}
		});
		if (status.attitudes_count != 0) {
			holder.thumbUpNum.setText("" + status.attitudes_count);
		}
		/**
		 * 点赞事件监听,并未找到官方发布接口，点赞即为假象
		 */
		holder.thumbUp_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AsyncWeiboRunner runner=new AsyncWeiboRunner(context);
				Oauth2AccessToken token = AccessTokenKeeper.readAccessToken(context);
				if (count%2==0) {
					WeiboParameters parameters=new WeiboParameters(Constants.APP_KEY);
					parameters.put("access_token", token);
					parameters.put("id", status.id);
					
					runner.requestAsync("https://api.weibo.com/2/attitudes/create.json",
							parameters, "POST", listener);
				}else {
					WeiboParameters parameters=new WeiboParameters(Constants.APP_KEY);
					
					parameters.put("access_token", token.getToken());
					parameters.put("id", status.id);
					
					runner.requestAsync("https://api.weibo.com/2/attitudes/destroy.json",
							parameters, "POST", listener);
				}
			}
		});

		return convertView;
	}

	/**
	 * 设置图片显示
	 * 
	 * @param status
	 * @param imgContainer
	 * @param gv_images
	 * @param iv_image
	 */
	private void setImages(final Status status, FrameLayout imgContainer, GridView gv_images, final ImageView iv_image) {
		ArrayList<String> pic_urls = status.pic_urls;
		String thumbnail_pic = status.bmiddle_pic;
		
		if (pic_urls != null && pic_urls.size() > 1) {
			imgContainer.setVisibility(View.VISIBLE);
			gv_images.setVisibility(View.VISIBLE);
			iv_image.setVisibility(View.GONE);

			StatusGridImgsAdapter gvAdapter = new StatusGridImgsAdapter(context, pic_urls);
			gv_images.setAdapter(gvAdapter);
			//注册主页多图监听
			if (fragment!=null) {
				fragment.registerGridAdapterListenner(gvAdapter);
			}
			//注册我发的微博页监听
			if (context instanceof UsersendWeibosActivity) {
				((UsersendWeibosActivity)context).registerGridAdapterListenner(gvAdapter);
			}
		} else if (pic_urls != null && pic_urls.size() == 1) {
			imgContainer.setVisibility(View.VISIBLE);
			gv_images.setVisibility(View.GONE);
			iv_image.setVisibility(View.VISIBLE);
//			imageLoader.displayImage(thumbnail_pic, iv_image);
			pictureLoader.displayImage(thumbnail_pic,iv_image);
			iv_image.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mListener!=null) {
						mListener.OnViewClick(iv_image,status.original_pic);
					}

				}
			});
		} else {
			imgContainer.setVisibility(View.GONE);
		}
	}

	private class Holder {
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
		LinearLayout transmit_layout;
		TextView transmitNum;
		LinearLayout comment_layout;
		TextView commentsNum;
		LinearLayout thumbUp_layout;
		ImageView thumbup_image;
		TextView thumbUpNum;

		Holder(ImageView user_imag, TextView user_name, TextView createtime, TextView weibosource, TextView text,
			   FrameLayout flayout, GridView img_layout, ImageView img_single, LinearLayout transmit,
			   TextView ret_text, FrameLayout ret_flayout, GridView ret_img_layout, ImageView ret_img_single,
			   LinearLayout transmit_layout, TextView transmitNum, LinearLayout comment_layout, TextView commentsNum,
			   LinearLayout thumbUp_layout, ImageView thumbup_image, TextView thumbUpNum) {
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
			this.transmit_layout = transmit_layout;
			this.transmitNum = transmitNum;
			this.comment_layout = comment_layout;
			this.commentsNum = commentsNum;
			this.thumbUp_layout = thumbUp_layout;
			this.thumbup_image = thumbup_image;
			this.thumbUpNum = thumbUpNum;
		}
	}
	
	private RequestListener listener=new RequestListener() {
		
		@Override
		public void onWeiboException(WeiboException arg0) {
			Toast.makeText(context, arg0.getMessage(), Toast.LENGTH_SHORT).show();
			
		}
		
		@Override
		public void onComplete(String arg0) {
			if (!TextUtils.isEmpty(arg0)) {
				if (arg0.startsWith("{\"attitude\"")) {
					if (count%2==0) {
						holder.thumbup_image.setImageResource(R.drawable.thumbup_focus);
						holder.thumbUpNum.setText(Integer.parseInt((String) holder.thumbUpNum.getText()) + 1+"");
						count++;
					}else {
						holder.thumbup_image.setImageResource(R.drawable.thumbup);
						holder.thumbUpNum.setText(Integer.parseInt((String) holder.thumbUpNum.getText()) - 1+"");
						count++;
					}
					
				}else {
					Toast.makeText(context, arg0, Toast.LENGTH_SHORT).show();
				}
			}
			
		}
	};

}
