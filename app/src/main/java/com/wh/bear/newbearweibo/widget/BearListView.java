package com.wh.bear.newbearweibo.widget;

import java.text.SimpleDateFormat;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewPropertyAnimator;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wh.bear.newbearweibo.R;
import com.wh.bear.newbearweibo.bean.OnRefreshListener;

@SuppressLint("HandlerLeak")
public class BearListView extends ListView implements OnTouchListener {

	// 表示三个动作，启动页面，刷新，加载
	public static final int START_ACTION = 1;
	public static final int REFRESH_ACTION = 2;
	public static final int LOAD_ACTION = 3;

	private Context mContext;
	// 头布局参数
	private View headerView;
	private ProgressBar refreshBar;
	private TextView refreshText;
	private TextView lastRefreshTime;
	private int headerViewHeight;
	private int headerPadding;

	int downY = 0;// 手指点下时坐标
	int moveY = 0;// 手指移动时坐标

	private OnRefreshListener mListener;// 向外部提供调用的接口
	// 脚布局参数
	private View footerView;
	private ProgressBar loadBar;
	private TextView loadText;
	private int footerViewHeight;
	private int footerPadding;

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			// 处理头布局自动缩回事件
			if (msg.what == 0x001) {
				headerView.setPadding(0, headerPadding, 0, 0);
				headerPadding -= 4;
				if (headerPadding > -headerViewHeight) {
					sendEmptyMessageDelayed(0x001, 2);
				} else {
					refreshText.setText("下拉刷新");
				}

			}
			// 处理头布局自动缩回事件
			if (msg.what == 0x002) {
				footerView.setPadding(0, footerPadding, 0, 0);
				footerPadding -= 2;
				if (footerPadding > -footerViewHeight) {
					sendEmptyMessageDelayed(0x002, 3);
				} else {
					loadText.setText("上拉加载");
				}
			}

		};
	};

	public BearListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initHeaderView();
		initFooterView();
		this.setOnTouchListener(this);
		mContext = context;
	}

	public BearListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BearListView(Context context) {
		this(context, null);
	}

	/**
	 * 初始化头布局
	 */
	private void initHeaderView() {
		headerView = View.inflate(getContext(), R.layout.listview_header, null);
		refreshBar = (ProgressBar) headerView.findViewById(R.id.refreshBar);
		refreshText = (TextView) headerView.findViewById(R.id.refreshing);
		lastRefreshTime = (TextView) headerView.findViewById(R.id.lastrefreshingtime);

		lastRefreshTime.setText("最后刷新时间：" + getLastUpdateTime());
		headerView.measure(0, 0);
		headerViewHeight = headerView.getMeasuredHeight();
		headerPadding = -headerViewHeight;
		handler.sendEmptyMessage(0x001);
		this.addHeaderView(headerView);
	}

	/**
	 * 初始化脚布局
	 */
	private void initFooterView() {
		footerView = View.inflate(getContext(), R.layout.listview_footer, null);
		loadBar = (ProgressBar) footerView.findViewById(R.id.loadBar);
		loadText = (TextView) footerView.findViewById(R.id.loadText);
		footerView.measure(0, 0);
		footerViewHeight = footerView.getMeasuredHeight();
		footerPadding = -footerViewHeight;
		handler.sendEmptyMessage(0x002);
		this.addFooterView(footerView);
	}

	/**
	 * 获得系统时间
	 * 
	 * @return
	 */
	@SuppressLint({ "SimpleDateFormat", "ClickableViewAccessibility" })
	private String getLastUpdateTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(System.currentTimeMillis());
	}


	/**
	 * 监听触屏事件
	 * 
	 * @param v
	 * @param event
	 * @return
	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY = (int) event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			moveY = (int) event.getY();
			if (getFirstVisiblePosition() == 0) {
				if (moveY - downY > 0) {
					headerPadding = (moveY - downY) / 4 - headerViewHeight;
					headerView.setPadding(0, headerPadding, 0, 0);
					if (headerPadding >= 0) {
						refreshText.setText("松开刷新");
					}
				}
			}

			if (this.getLastVisiblePosition() == this.getCount() - 1) {
				if (downY - moveY > 0) {
					footerPadding = (downY - moveY) / 3 - footerViewHeight;
					footerView.setPadding(0, footerPadding, 0, 0);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if (headerPadding >= 0) {
				refreshBar.setVisibility(View.VISIBLE);
				refreshText.setText("刷新中...");
				if (mListener != null) {
					mListener.onDownPullRefresh(this);
				}

			} else {
				handler.sendEmptyMessage(0x001);
			}

			if (footerPadding >= 0) {
				loadBar.setVisibility(View.VISIBLE);
				loadText.setText("加载中...");
				if (mListener != null) {
					mListener.onLoadingMore(this);
				}

			} else {
				handler.sendEmptyMessage(0x002);
			}
			break;
		}
		return false;
	}

	/**
	 * 刷新完成后应做处理
	 */
	public void refreshed() {
		refreshText.setText("刷新成功");
		lastRefreshTime.setText("最后刷新时间：" + getLastUpdateTime());
		refreshBar.setVisibility(View.INVISIBLE);
		handler.sendEmptyMessage(0x001);
	}

	/**
	 * 加载完成后应做处理
	 */
	public void loaded() {
		loadBar.setVisibility(View.INVISIBLE);
		handler.sendEmptyMessage(0x002);
	}

	/**
	 * 刷新失败应做处理
	 */
	public void refreshFailed() {
		refreshText.setText("刷新失败");
		refreshBar.setVisibility(View.INVISIBLE);
		handler.sendEmptyMessage(0x001);
	}

	/**
	 * 加载失败应做处理
	 */
	public void loadFailed() {
		loadText.setText("加载失败");
		loadBar.setVisibility(View.INVISIBLE);
		handler.sendEmptyMessage(0x002);
	}

	/**
	 * 向外部提供接口方法
	 * 
	 * @param mListener
	 */
	public void setOnRefreshListener(OnRefreshListener mListener) {
		this.mListener = mListener;
	}
	
	public void setHeaderviewVisblityGone() {
		this.removeHeaderView(headerView);
	}

}
