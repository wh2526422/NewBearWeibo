package com.wh.bear.newbearweibo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.AsyncWeiboRunner;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.net.WeiboParameters;
import com.sina.weibo.sdk.utils.LogUtil;
import com.wh.bear.newbearweibo.openapi.CommentsAPI;
import com.wh.bear.newbearweibo.openapi.legacy.StatusesAPI;
import com.wh.bear.newbearweibo.openapi.models.ErrorInfo;
import com.wh.bear.newbearweibo.openapi.models.Status;
import com.wh.bear.newbearweibo.utils.Constants;

/**
 * 发微博界面
 * @author Administrator
 *
 */
public class PublishWeiboActivity extends Activity {
	protected static final String TAG = PublishWeiboActivity.class.getName();
	protected static final int PICTUURE = 0;
	protected static final int CAMERA = 1;
	private boolean close=false;
	
	public static final String PUBLISH_ACTION="publish";
	public static final String RETWEETED_ACTION="retweeted";
	public static final String COMMENT_ACTION="comment";
	public static final String REPLY_ACTION="reply";
	Button publish_back_btn, publish_send_btn;
	EditText publish_text;
	ImageButton publish_image_choose,publish_image_canmera;
	Spinner publish_type;
	ImageView publish_image;
	ProgressBar publish_progres;
	View include_transmit_body;//转发微博视图显示
	CheckBox type_check;//转发或评论视图显示
	Bitmap image;
	StatusesAPI mApi;
	CommentsAPI mApi2;
	Long status_id;//转发微博或者评论微博传过来的微博id
	Long comment_id;//回复评论的评论id
	ImageView source_comment_img;//转发微博视图显示
	TextView source_comment_author;//转发微博视图显示
	TextView source_comment_text;//转发微博视图显示
	Intent intent;
	String action;
	ImageLoader imageLoader;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.publish_layout);
		publish_back_btn = (Button) findViewById(R.id.publish_back_btn);
		publish_send_btn = (Button) findViewById(R.id.publish_send_btn);
		publish_text = (EditText) findViewById(R.id.publish_weibo_text);
		publish_image_choose = (ImageButton) findViewById(R.id.publish_image_choose);
		publish_type = (Spinner) findViewById(R.id.publish_type);
		publish_image = (ImageView) findViewById(R.id.publish_image);
		publish_image_canmera=(ImageButton) findViewById(R.id.publish_image_canmera);
		publish_progres=(ProgressBar) findViewById(R.id.publish_progress);
		
		publish_send_btn.setEnabled(false);
		//设置发微博类型
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
				new String[] { "公开", "仅自己可见", "好友圈" });
		publish_type.setAdapter(adapter);
		Oauth2AccessToken mAccessToken = AccessTokenKeeper.readAccessToken(this);
		mApi = new StatusesAPI(this, Constants.APP_KEY, mAccessToken);
		intent = getIntent();
		action = intent.getAction();
		
		if (RETWEETED_ACTION.equals(action)) {
			//启动转发微博
			initTransmitMessageView();
		}else if (COMMENT_ACTION.equals(action)) {
			//启动评论微博
			initCommentMessageView(mAccessToken);
		} else if (REPLY_ACTION.equals(action)) {
			initReplyCommentMessageView(mAccessToken);
		}
		
		// 返回按钮点击事件
		publish_back_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PublishWeiboActivity.this.finish();
			}
		});
		// 文本框监听
		publish_text.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String teString=s.toString();
				if ("".equals(teString)||teString==null) {
					publish_send_btn.setEnabled(false);
				}else {
					publish_send_btn.setEnabled(true);
				}
			}
		});
		// 发送监听
		publish_send_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Oauth2AccessToken mToken = AccessTokenKeeper.readAccessToken(PublishWeiboActivity.this);
				AsyncWeiboRunner runner = new AsyncWeiboRunner(PublishWeiboActivity.this);
				String message = publish_text.getText().toString();
				boolean checked=false;;
				int commentType=0;
				if (!PUBLISH_ACTION.equals(action)) {
					checked = type_check.isChecked();
					commentType = 0;
					if (checked) {
						commentType = 1;
					} 
				}
				//启动  发表微博
				if (PUBLISH_ACTION.equals(action)) {
					int id = (int) publish_type.getSelectedItemId();
					if (image == null) {
						publishWeibo(mToken, "update", runner, message, id);
					} else {
						publishWeibo(mToken, "upload", runner, message, id);
					}
					close=true;
				}else if (RETWEETED_ACTION.equals(action)) {//启动转发微博
					mApi.repost(status_id, message, commentType, listener);
					close=true;
					publish_progres.setVisibility(View.VISIBLE);
				}else if (COMMENT_ACTION.equals(action)) {//启动评论微博
					mApi2.create(message, status_id, checked, listener);
					close=true;
					publish_progres.setVisibility(View.VISIBLE);
				}else if (REPLY_ACTION.equals(action)) {
					mApi2.reply(comment_id, status_id, message, true, false, listener);
					close=true;
					publish_progres.setVisibility(View.VISIBLE);
				}
				
			}

			
		});

		// 调用系统相册
		publish_image_choose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_PICK,
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, PICTUURE);

			}
		});
		// 调用系统相机
		publish_image_canmera.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, CAMERA);
			}
		});
	}
	/**
	 * 初始化回复评论视图
	 * @param mAccessToken
	 */
	private void initReplyCommentMessageView(Oauth2AccessToken mAccessToken) {
		status_id=intent.getLongExtra("status_id", 0);
		comment_id=intent.getLongExtra("comment_id", 0);
		mApi2=new CommentsAPI(this, Constants.APP_KEY, mAccessToken);
		type_check=(CheckBox) findViewById(R.id.type_check);
		type_check.setText(getResources().getString(R.string.withtransmit));
		type_check.setVisibility(View.VISIBLE);
		
	}

	private RequestListener listener = new RequestListener() {

		@Override
		public void onWeiboException(WeiboException e) {
			LogUtil.e(TAG, e.getMessage());
			ErrorInfo info = ErrorInfo.parse(e.getMessage());
			Toast.makeText(PublishWeiboActivity.this, info.toString(), Toast.LENGTH_LONG).show();
			publish_progres.setVisibility(View.GONE);
		}

		@Override
		public void onComplete(String response) {
			if (!TextUtils.isEmpty(response)) {
				LogUtil.i(TAG, response);
				if (response.startsWith("{\"created_at\"")) {
					// 调用 Status#parse 解析字符串成微博对象
					Status status = Status.parse(response);
					if (close) {
						Toast.makeText(PublishWeiboActivity.this, "发送一送微博成功, id = " + status.id, Toast.LENGTH_LONG)
								.show();
						publish_progres.setVisibility(View.GONE);
						finish();
					}
				} else {
					Toast.makeText(PublishWeiboActivity.this, response, Toast.LENGTH_LONG).show();
					publish_progres.setVisibility(View.GONE);
					close=false;
					publish_progres.setVisibility(View.GONE);
				}
			}

		}
	};
	/**
	 * 发表微博
	 * @param mToken
	 * @param runner
	 * @param message
	 * @param id
	 */
	private void publishWeibo(Oauth2AccessToken mToken, String publishUri, AsyncWeiboRunner runner, String message, int id) {
		if (image!=null) {
			int byteCount = image.getByteCount();
			if (byteCount >= 5 * 1024 * 1024) {
				Toast.makeText(PublishWeiboActivity.this, "图片大于5M无法上传", Toast.LENGTH_LONG).show();
				return;
			} 
		}
		WeiboParameters wbparams = new WeiboParameters(Constants.APP_KEY);
		wbparams.put("access_token", mToken.getToken());
		wbparams.put("status",       message);
		wbparams.put("visible",      id);
		wbparams.put("list_id",      "");
		if (image!=null) {
			wbparams.put("pic", image);
		}
		wbparams.put("lat",          14.5f);
		wbparams.put("long",         23.0f);
		wbparams.put("annotations",  "");
		
		runner.requestAsync(
		        "https://api.weibo.com/2/statuses/"+publishUri+".json", 
		        wbparams, 
		        "POST", 
		        listener);
		publish_progres.setVisibility(View.VISIBLE);
	}
	/**
	 * 初始化转发微博视图
	 */
	private void initTransmitMessageView() {
		Bundle bundle = intent.getExtras();
		status_id=bundle.getLong("id");
		
		imageLoader= ImageLoader.getInstance();
		
		include_transmit_body=findViewById(R.id.include_transmit_body);
		include_transmit_body.setVisibility(View.VISIBLE);
		source_comment_img=(ImageView) include_transmit_body.findViewById(R.id.source_comment_img);
		source_comment_author=(TextView) include_transmit_body.findViewById(R.id.source_comment_author);
		source_comment_text=(TextView) include_transmit_body.findViewById(R.id.source_comment_text);
		type_check=(CheckBox) findViewById(R.id.type_check);
		type_check.setVisibility(View.VISIBLE);
		setTransmitMessage(bundle);
	}
	/**
	 * 初始化评论视图
	 */
	private void initCommentMessageView(Oauth2AccessToken mAccessToken) {
		status_id=intent.getLongExtra("id", 0);
		mApi2=new CommentsAPI(this, Constants.APP_KEY, mAccessToken);
		type_check=(CheckBox) findViewById(R.id.type_check);
		type_check.setText(getResources().getString(R.string.withtransmit));
		type_check.setVisibility(View.VISIBLE);
	}

	/**
	 * 设置转发视图
	 * @param bundle
	 */
	private void setTransmitMessage(Bundle bundle) {
		imageLoader.displayImage(bundle.getString("thumbnail_pic"), source_comment_img);
		source_comment_author.setText("@ "+bundle.getString("screen_name"));
		source_comment_text.setText(bundle.getString("text"));
	}
	
	
	/**
	 * 获取系统相册和相机返回的内容
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PICTUURE) {
			try {
				Uri uri = data.getData();
				publish_image.setImageURI(uri);
				String[] pathColumns = { MediaStore.Images.Media.DATA };
				Cursor c = getContentResolver().query(uri, pathColumns, null, null, null);
				c.moveToFirst();
				String path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
				image = BitmapFactory.decodeFile(path);
				c.close();
				publish_image.setVisibility(View.VISIBLE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (requestCode==CAMERA) {
			try {
				Bundle bundle = data.getExtras();
				image=(Bitmap) bundle.get("data");
				if (image!=null) {
					publish_image.setImageBitmap(image);
					publish_image.setVisibility(View.VISIBLE);
				}
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}

	};
	
}
