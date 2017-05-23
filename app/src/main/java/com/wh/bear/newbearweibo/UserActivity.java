package com.wh.bear.newbearweibo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.wh.bear.newbearweibo.fragment.UserinfoFragment;
import com.wh.bear.newbearweibo.fragment.WeiboHomeFragment;
import com.wh.bear.newbearweibo.fragment.WeiboMessageFragment;
import com.wh.bear.newbearweibo.fragment.WeiboSettingFragment;
import com.wh.bear.newbearweibo.utils.PictureLoader;

public class UserActivity extends Activity implements OnClickListener{
	ImageView img_home,img_message,img_setting,img_user,publish_weibo_btn;
	Fragment userFragment;
	Fragment weiboHomeFragment;
	Fragment weiboMsgFragment; 
	Fragment weiboSettingFragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_layout);
		
//		getActionBar().hide();
		img_home=(ImageView) findViewById(R.id.img_home);
		img_message=(ImageView) findViewById(R.id.img_message);
		img_setting=(ImageView) findViewById(R.id.img_setting);
		img_user=(ImageView) findViewById(R.id.img_user);
		publish_weibo_btn=(ImageView) findViewById(R.id.publish_weibo_btn);
		
		img_home.setOnClickListener(this);
		img_message.setOnClickListener(this);
		img_setting.setOnClickListener(this);
		img_user.setOnClickListener(this);
		publish_weibo_btn.setOnClickListener(this);
		
		FragmentManager manager = getFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		weiboHomeFragment=new WeiboHomeFragment();
		transaction.replace(R.id.layout, weiboHomeFragment);
		transaction.commitAllowingStateLoss();
	}

	/**
	 * 功能区图片被点
	 * @param v
	 */
	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		FragmentManager manager = getFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		switch (v.getId()) {
		case R.id.img_home:
			weiboHomeFragment=new WeiboHomeFragment();
			transaction.replace(R.id.layout, weiboHomeFragment);
			changImage(R.id.img_home);
			break;
		case R.id.img_message:
			weiboMsgFragment=new WeiboMessageFragment();
			transaction.replace(R.id.layout, weiboMsgFragment);
			changImage(R.id.img_message);
			break;
		case R.id.img_setting:
			weiboSettingFragment=new WeiboSettingFragment();
			transaction.replace(R.id.layout, weiboSettingFragment);
			changImage(R.id.img_setting);
			break;
		case R.id.img_user:
			userFragment=new UserinfoFragment();
			transaction.replace(R.id.layout, userFragment);
			changImage(R.id.img_user);
			break;
		case R.id.publish_weibo_btn:
			Intent intent=new Intent(this,PublishWeiboActivity.class);
			intent.setAction(PublishWeiboActivity.PUBLISH_ACTION);
			startActivity(intent);
			break;
		}
		transaction.commitAllowingStateLoss();
	}
	/**
	 * 更改功能区图片
	 * @param resourceId
	 */
	private void changImage(int resourceId) {
		
		switch (resourceId) {
		case R.id.img_home:
			img_home.setImageResource(R.drawable.home_focus);
			img_message.setImageResource(R.drawable.message_losefocus);
			img_setting.setImageResource(R.drawable.setting_losefucus);
			img_user.setImageResource(R.drawable.user_losefocus);
			break;
		case R.id.img_message:
			img_home.setImageResource(R.drawable.home_losefocus);
			img_message.setImageResource(R.drawable.message_focus);
			img_setting.setImageResource(R.drawable.setting_losefucus);
			img_user.setImageResource(R.drawable.user_losefocus);
			break;
		case R.id.img_setting:
			img_home.setImageResource(R.drawable.home_losefocus);
			img_message.setImageResource(R.drawable.message_losefocus);
			img_setting.setImageResource(R.drawable.setting_focus);
			img_user.setImageResource(R.drawable.user_losefocus);
			break;
		case R.id.img_user:
			img_home.setImageResource(R.drawable.home_losefocus);
			img_message.setImageResource(R.drawable.message_losefocus);
			img_setting.setImageResource(R.drawable.setting_losefucus);
			img_user.setImageResource(R.drawable.user_focus);
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		PictureLoader loader = PictureLoader.getInstance();
		loader.releaseImages();
		loader.cancelAllTasksAndCleanData();
	}
}
