package com.wh.bear.newbearweibo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

import com.wh.bear.newbearweibo.R;
import com.wh.bear.newbearweibo.bean.OnViewsClickListener;
import com.wh.bear.newbearweibo.utils.PictureLoader;

import java.util.ArrayList;

/**
 * gridview使用适配器
 * @author Administrator
 *
 */
public class StatusGridImgsAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<String> datas;
	private OnViewsClickListener mListener;
	private PictureLoader pictureLoader;

	StatusGridImgsAdapter(Context context, ArrayList<String> datas) {
		this.context = context;
		this.datas = datas;
//		for (String s : datas) {
//			this.datas.add(s.replace("thumbnail", "bmiddle"));
//		}
		pictureLoader = PictureLoader.getInstance();
	}

	public void setOnImagesClickListener(OnViewsClickListener mListener) {
		this.mListener=mListener;
	}
	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public String getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView=View.inflate(context, R.layout.grid_item, null);
			holder.iv_image = (ImageView) convertView.findViewById(R.id.img_grid);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		GridView gv = (GridView) parent;
		//水平间隙
		int horizontalSpacing = 4;
		//列数
		int numColumns = 3;
		//每一项的宽度
		int itemWidth = (gv.getWidth() - (numColumns-1) * horizontalSpacing
				- gv.getPaddingLeft() - gv.getPaddingRight()) / numColumns;

		LayoutParams params = new LayoutParams(itemWidth, itemWidth);
		holder.iv_image.setLayoutParams(params);
		
		String url = getItem(position);
//		imageLoader.displayImage(url, holder.iv_image);
		pictureLoader.displayImageByPriority(url.replace("thumbnail", "bmiddle"), holder.iv_image, position);
		holder.iv_image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (mListener!=null) {
					ArrayList<String> pic_urls=new ArrayList<String>();
					for (String s : datas) {
						pic_urls.add(s.replace("thumbnail", "large"));
					}
					mListener.OnViewsClick(holder.iv_image, pic_urls,position);
				}
			}
		});
		return convertView;
	}

	private class ViewHolder {
		ImageView iv_image;
	}

}
