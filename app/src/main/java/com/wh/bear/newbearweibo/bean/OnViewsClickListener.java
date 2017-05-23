package com.wh.bear.newbearweibo.bean;

import java.util.ArrayList;

import android.view.View;

/**
 * 多图点击监听接口
 * @author Administrator
 *
 */
public interface OnViewsClickListener {

	void OnViewsClick(View view, ArrayList<String> imgPathses, int position);
}
