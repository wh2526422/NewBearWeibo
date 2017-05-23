package com.wh.bear.newbearweibo.openapi;


import com.wh.bear.newbearweibo.openapi.models.Comment;

import java.util.ArrayList;

public class StatusMessage {
	String id;
	String profile_image_url;
	String screen_name;
	String created_at;
	String source;
	String text;
	ArrayList<String> pic_urls;
	String retweeted_status_text;
	ArrayList<String> retweeted_status_pic_urls;
	Comment comment;
	
	public StatusMessage() {
	}
	
	public StatusMessage(String id, String profile_image_url, String screen_name, String created_at, String source,
						 String text, ArrayList<String> pic_urls, String retweeted_status_text,
						 ArrayList<String> retweeted_status_pic_urls) {
		this.id = id;
		this.profile_image_url = profile_image_url;
		this.screen_name = screen_name;
		this.created_at = created_at;
		this.source = source;
		this.text = text;
		this.pic_urls = pic_urls;
		this.retweeted_status_text = retweeted_status_text;
		this.retweeted_status_pic_urls = retweeted_status_pic_urls;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getProfile_image_url() {
		return profile_image_url;
	}
	public void setProfile_image_url(String profile_image_url) {
		this.profile_image_url = profile_image_url;
	}
	public String getScreen_name() {
		return screen_name;
	}
	public void setScreen_name(String screen_name) {
		this.screen_name = screen_name;
	}
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public ArrayList<String> getPic_urls() {
		return pic_urls;
	}
	public void setPic_urls(ArrayList<String> pic_urls) {
		this.pic_urls = pic_urls;
	}
	public String getRetweeted_status_text() {
		return retweeted_status_text;
	}
	public void setRetweeted_status_text(String retweeted_status_text) {
		this.retweeted_status_text = retweeted_status_text;
	}
	public ArrayList<String> getRetweeted_status_pic_urls() {
		return retweeted_status_pic_urls;
	}
	public void setRetweeted_status_pic_urls(ArrayList<String> retweeted_status_pic_urls) {
		this.retweeted_status_pic_urls = retweeted_status_pic_urls;
	}
	

	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

	
}

