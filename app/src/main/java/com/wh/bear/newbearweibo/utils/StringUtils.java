package com.wh.bear.newbearweibo.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 将带有特殊作用的文本进行特殊处理
 * @author Administrator
 *
 */
@SuppressLint("SimpleDateFormat")
public class StringUtils {

	public static SpannableString getWeiboContent(final Context context, TextView teView, String source) {

		String regexAt = "@[\u4e00-\u9fa5\\w]+";//正则表达式 \u4e00-\u9fa5表示中文，\\w表示26个英文字母
		String regexTopic = "#[\u4e00-\u9fa5\\w]+#";
		String link= "http[://.\\w]+";
		//利用正则表达式组的特性进行一次性匹配
		String regx = "(" + regexAt + ")|(" + regexTopic + ")|("+link+")";
		//进行字符串匹配
		SpannableString spannableString = new SpannableString(source);
		Pattern pattern = Pattern.compile(regx);
		Matcher matcher = pattern.matcher(spannableString);
		//如果匹配到
		if (matcher.find()) {
			teView.setMovementMethod(LinkMovementMethod.getInstance());
			matcher.reset();
		}

		while (matcher.find()) {

			final String at = matcher.group(1);
			final String to = matcher.group(2);
			final String li= matcher.group(3) ;

			if (at != null) {
				int start = matcher.start(1);//表示匹配到
				ClickableSpan span = new BearClickSpan(context) {

					@Override
					public void onClick(View widget) {
						Toast.makeText(context, at, Toast.LENGTH_SHORT).show();

					}
				};
				//将匹配成功的字符串进行相应处理，可点击，变颜色
				spannableString.setSpan(span, start, start + at.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			if (to != null) {
				int start = matcher.start(2);
				ClickableSpan span = new BearClickSpan(context) {

					@Override
					public void onClick(View widget) {
						Toast.makeText(context, to, Toast.LENGTH_SHORT).show();

					}
				};
				spannableString.setSpan(span, start, start + to.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			
			if (li!=null) {
				int start = matcher.start(3);
				URLSpan span=new URLSpan(li);
				spannableString.setSpan(span, start, start + li.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
			}

		}

		return spannableString;

	}
	/**
	 * 自定义ClickableSpan，将颜色和下滑现去掉
	 * @author Administrator
	 *
	 */
	static class BearClickSpan extends ClickableSpan {
		Context context;

		public BearClickSpan(Context context) {
			this.context = context;
		}

		@Override
		public void onClick(View widget) {

		}

		@SuppressWarnings("deprecation")
		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setColor(context.getResources().getColor(android.R.color.holo_blue_dark));
			ds.setUnderlineText(false);
		}
	}
	
	public static String formatTime(String time){
        // 00:00:00 Tue Oct 13 18:53:16 +0800 2015
		time=time.replace(" +0800 ", " ");
		time=time.substring(4);
        SimpleDateFormat format=new SimpleDateFormat("MMM dd HH:mm:ss yyyy",Locale.US);
        Date date = null;
		try {
			date = format.parse(time);
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
        long currentTime=System.currentTimeMillis();
        long diffTime=currentTime-date.getTime();
        if (diffTime<=10*60*1000) {
			time="刚刚";
		}else if (10*60*1000<diffTime&&diffTime<60*60*1000) {
        	
			time=((diffTime/1000)/60)+"分钟前";
		}else if (diffTime>60*60*1000&&diffTime<60*60*1000*24) {
			time=(((diffTime/1000)/60)/60)+"小时前";
		}else if (diffTime>60*60*1000*24&&diffTime<60*60*1000*24*7) {
			time=((((diffTime/1000)/60)/60)/24)+"天前";
		}else {
			SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			time=f.format(date);
		}
        
        return time;
    }
	
}
