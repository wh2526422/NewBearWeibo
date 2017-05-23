package com.wh.bear.newbearweibo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.wh.bear.newbearweibo.utils.Constants;
import com.wh.bear.newbearweibo.utils.PictureLoader;
import com.wh.bear.newbearweibo.utils.log;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG ="MainActivity";
    private SsoHandler mSsoHandler;
    private Oauth2AccessToken mAccessToken;
    private TextView token_msg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        token_msg = (TextView) findViewById(R.id.token_msg);
        mSsoHandler = new SsoHandler(this);
        final WbAuthListener wbListener = new SelfWbAuthListener();
        findViewById(R.id.web_sso).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSsoHandler.authorizeWeb(wbListener);
                if (mAccessToken.isSessionValid()) {
                    Intent intent = new Intent(MainActivity.this, UserActivity.class);
                    startActivity(intent);
                }else {
                    mSsoHandler.authorize(wbListener);
                }
            }
        });

        findViewById(R.id.client_sso).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSsoHandler.authorizeClientSso(wbListener);
            }
        });
        findViewById(R.id.all_sso).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSsoHandler.authorize(wbListener);
            }
        });
        findViewById(R.id.refresh_token).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(mAccessToken.getRefreshToken())){
                    AccessTokenKeeper.refreshToken(Constants.APP_KEY, MainActivity.this, new RequestListener() {
                        @Override
                        public void onComplete(String response) {

                        }

                        @Override
                        public void onWeiboException(WeiboException e) {

                        }
                    });
                }
            }
        });
        // 从 SharedPreferences 中读取上次已保存好 AccessToken 等信息，
        // 第一次启动本应用，AccessToken 不可用
        mAccessToken = AccessTokenKeeper.readAccessToken(this);
        if (mAccessToken.isSessionValid()) {
            updateTokenView(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mSsoHandler != null){
            mSsoHandler.authorizeCallBack(requestCode,resultCode,data);
        }
    }
    
    class SelfWbAuthListener implements  WbAuthListener{

        @Override
        public void onSuccess(Oauth2AccessToken oauth2AccessToken) {
            mAccessToken = oauth2AccessToken;
            if (mAccessToken.isSessionValid()) {
                // 显示 Token
                updateTokenView(false);
                // 保存 Token 到 SharedPreferences
                AccessTokenKeeper.writeAccessToken(MainActivity.this, mAccessToken);
                Toast.makeText(MainActivity.this, "成功了", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void cancel() {

        }

        @Override
        public void onFailure(WbConnectErrorMessage wbConnectErrorMessage) {
            Toast.makeText(MainActivity.this, "失败了", Toast.LENGTH_SHORT).show();
            log.i(TAG,wbConnectErrorMessage.getErrorMessage() + "," + wbConnectErrorMessage.getErrorCode());
        }
    }
    /**
     * 显示当前 Token 信息。
     *
     * @param hasExisted 配置文件中是否已存在 token 信息并且合法
     */
    private void updateTokenView(boolean hasExisted) {
        String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                new java.util.Date(mAccessToken.getExpiresTime()));
        String format = getString(R.string.token_to_string_format_1);
        token_msg.setText(String.format(format, mAccessToken.getToken(), date));

        String message = String.format(format, mAccessToken.getToken(), date);
        if (hasExisted) {
            message = getString(R.string.token_has_existed) + "\n" + message;
        }
        token_msg.setText(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PictureLoader.getInstance().cleanPictureCache();
    }
}
