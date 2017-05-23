package com.wh.bear.newbearweibo.bean;

import android.view.View;

public interface OnRefreshListener {
	 /**
	   * 下拉刷新
	   */
	  void onDownPullRefresh(View view);

	  /**
	   * 上拉加载
	   */
	  void onLoadingMore(View view);
}
