package com.wh.bear.newbearweibo.utils;

import java.util.ArrayList;
import java.util.List;

import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.wh.bear.newbearweibo.openapi.models.Comment;
import com.wh.bear.newbearweibo.openapi.models.Status;
import com.wh.bear.newbearweibo.openapi.models.User;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 对数据库操作的类 微博类型 0为原微博，1位转发微博，2为at我的微博，3为评论中的微博，4位我发表的微博 
 * 用户类型 0表示微博中的用户，1表示评论中的用户,2表示我的关注用户，3表示我的粉丝 
 * 评论类型 0为原评论 ，1为回复评论
 * 
 * @author Administrator
 *
 */
public class SQLiteOptionHelper extends SQLiteOpenHelper {
	//database类型
	public static final int READ=0;
	public static final int WRITE=1;
	//微博类型
	public static final int STATUS_PUBLIC=0;
	public static final int STATUS_RETWEET=1;
	public static final int STATUS_ATME=2;
	public static final int STATUS_INCOMMENT=3;
	public static final int STATUS_SEND=4;
	//用户类型
	public static final int USER_INSTATUS=0;
	public static final int USER_INCOMMENT=1;
	public static final int USER_FOCUS=2;
	public static final int USER_FANS=3;
	//评论类型
	public static final int COMMENT_ORIGINAL=0;
	public static final int COMMENT_REPLY=1;
	//是否有多图
	public static final int NO_NINE=0;
	public static final int NINE=1;
	
	
	SQLiteDatabase db=null;
	public SQLiteOptionHelper(Context context, String name, int version) {
		super(context, AccessTokenKeeper.readAccessToken(context).getUid() + name, null, version);
	}
	
	public SQLiteDatabase getInstance(int type) {
		
		if (type==WRITE&&db==null) {
			db=getWritableDatabase();
		}
		if (type==READ&&db==null) {
			db=getReadableDatabase();
		}
		return db;
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql1 = "create table status("
				+ "id integer primary key,"
				+ "status_id vachar(20),"
				+ "created_at vachar(100),"
				+ "text vachar(280),"
				+ "source vachar(100),"
				+ "thumbnail_pic vachar(100),"
				+ "bmiddle_pic vachar(100),"
				+ "original_pic vachar(100),"
				+ "uid vachar(20),"
				+ "retweeted_status_id vachar(20),"
				+ "reposts_count integer,"
				+ "comments_count integer,"
				+ "attitudes_count integer,"
				+ "urls integer default(0),"
				+ "type integer default(0)"
				+ ");";
		String sql2 = "create table comment("
				+ "id integer primary key,"
				+ "comment_id vachar(20),"
				+ "created_at vachar(100),"
				+ "text vachar(280),"
				+ "source vachar(100),"
				+ "uid vachar(20),"
				+ "status_id vachar(20),"
				+ "reply_comment_id vachar(20),"
				+ "type integer default(0)"
				+ ");";
		String sql3 = "create table user("
				+ "id integer primary key,"
				+ "uid varchar(20),"
				+ "screen_name varchar(20),"
				+ "description varchar(50),"
				+ "profile_image_url vachar(100),"
				+ "avatar_large vachar(100),"
				+ "avatar_hd vachar(100),"
				+ "type integer default(0)"
				+ ");";
		String sql4 = "create table pictures("
				+ "id integer primary key,"
				+ "status_id varchar(20),"
				+ "url varchar(100)"
				+ ");";
		String sql5="create table searchImfo("
				+ "id integer primary key,"
				+ "msg varchar(20)"
				+ ")";
		db.beginTransaction();
		try {
			db.execSQL(sql1);
			db.execSQL(sql2);
			db.execSQL(sql3);
			db.execSQL(sql4);
			db.execSQL(sql5);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	/**
	 * 插入微博数据
	 * 
	 * @param statuses
	 */
	public void setStatus(ArrayList<Status> statuses, int type) {
		 db= getInstance(WRITE);
		for (Status status : statuses) {
			ContentValues values = new ContentValues();
			values.put("status_id", status.id);
			values.put("created_at", status.created_at);
			values.put("text", status.text);
			values.put("source", status.source);
			if (status.thumbnail_pic != null) {
				values.put("thumbnail_pic", status.thumbnail_pic);
				values.put("bmiddle_pic", status.bmiddle_pic);
				values.put("original_pic", status.original_pic);
			}
			values.put("uid", status.user.id);
			setUser(status.user, USER_INSTATUS);
			// 插入转发微博数据
			if (status.retweeted_status != null) {
				values.put("retweeted_status_id", status.retweeted_status.id);
				setRetStatus(status.retweeted_status);
			}
			values.put("reposts_count", status.reposts_count);
			values.put("comments_count", status.comments_count);
			values.put("attitudes_count", status.attitudes_count);
			// 插入九宫格数据
			// 表头 urls 1 表示有九图，0表示没有
			if (status.pic_urls != null) {
				values.put("urls", NINE);
				setPicurls(status.id, status.pic_urls);
			} else {
				values.put("urls", NO_NINE);
			}
			values.put("type", type);
			db.insert("status", null, values);
		}

	}

	/**
	 * 插入转发微博数据
	 * 
	 * @param status
	 */
	public void setRetStatus(Status status) {
		db = getInstance(WRITE);
		ContentValues values = new ContentValues();
		values.put("status_id", status.id);
		values.put("text", status.text);
		if (status.thumbnail_pic != null) {
			values.put("thumbnail_pic", status.thumbnail_pic);
			values.put("bmiddle_pic", status.bmiddle_pic);
			values.put("original_pic", status.original_pic);
		}
		if (status.user!=null) {
			values.put("uid", status.user.id);
			setUser(status.user, USER_INSTATUS);
		}
		values.put("type", 1);
		if (status.pic_urls != null) {
			values.put("urls", NINE);
			setPicurls(status.id, status.pic_urls);
		} else {
			values.put("urls", NO_NINE);
		}
		db.insert("status", null, values);
	}

	/**
	 * 插入评论中的微博
	 * 
	 * @param status
	 */
	public void setCommentStatus(Status status) {
		db = getInstance(WRITE);
		ContentValues values = new ContentValues();
		values.put("status_id", status.id);
		values.put("text", status.text);
		if (status.thumbnail_pic != null) {
			values.put("thumbnail_pic", status.thumbnail_pic);
			values.put("bmiddle_pic", status.bmiddle_pic);
			values.put("original_pic", status.original_pic);
		}
		values.put("uid", status.user.id);
		setUser(status.user, USER_INCOMMENT);
		values.put("type", STATUS_INCOMMENT);
		if (status.pic_urls != null) {
			values.put("urls", NINE);
			setPicurls(status.id, status.pic_urls);
		} else {
			values.put("urls", NO_NINE);
		}
		db.insert("status", null, values);
	}

	/**
	 * 根据类型得到微博
	 * 
	 * @param type
	 *            微博类型，0为原微博，1位转发微博，2为at我的微博，3为我发表的微博
	 * @return
	 */
	public ArrayList<Status> getStatuses(int type) {
		db = getInstance(READ);
		ArrayList<Status> list = new ArrayList<Status>();

		Cursor cursor = db.query("status", null, "type=?", new String[] { type + "" }, null, null, null);
		while (cursor.moveToNext()) {

			Status status = new Status();
			status.id = cursor.getString(cursor.getColumnIndex("status_id"));
			status.created_at = cursor.getString(cursor.getColumnIndex("created_at"));
			status.text = cursor.getString(cursor.getColumnIndex("text"));
			status.source = cursor.getString(cursor.getColumnIndex("source"));
			status.thumbnail_pic = cursor.getString(cursor.getColumnIndex("thumbnail_pic"));
			status.bmiddle_pic = cursor.getString(cursor.getColumnIndex("bmiddle_pic"));
			status.original_pic = cursor.getString(cursor.getColumnIndex("original_pic"));

			String uid = cursor.getString(cursor.getColumnIndex("uid"));
			
			User user = null;
			if (uid!=null) {
				user = getUser(uid, USER_INSTATUS);
			}
			status.user = user;

			String sid = cursor.getString(cursor.getColumnIndex("retweeted_status_id"));
			if (sid != null) {
				status.retweeted_status = getRetStatus(sid);
			}
			status.reposts_count = cursor.getInt(cursor.getColumnIndex("reposts_count"));
			status.comments_count = cursor.getInt(cursor.getColumnIndex("comments_count"));
			status.attitudes_count = cursor.getInt(cursor.getColumnIndex("attitudes_count"));
			// urls
			int i = cursor.getInt(cursor.getColumnIndex("urls"));
			if (i == NINE) {
				status.pic_urls = getPicurls(status.id);
			}

			list.add(status);
		}
		cursor.close();
		return list;

	}

	/**
	 * 查询转发微博
	 * 
	 * @param retweeted_status_id
	 * @return
	 */
	public Status getRetStatus(String retweeted_status_id) {
		db = getInstance(READ);
		Cursor cursor = db.query("status", null, "status_id=? and type=1", new String[] { retweeted_status_id },
				null, null, null);
		Status status = new Status();
		if (cursor.moveToFirst()) {
			status.id = cursor.getString(cursor.getColumnIndex("status_id"));
			status.text = cursor.getString(cursor.getColumnIndex("text"));
			status.thumbnail_pic = cursor.getString(cursor.getColumnIndex("thumbnail_pic"));
			status.bmiddle_pic = cursor.getString(cursor.getColumnIndex("bmiddle_pic"));
			status.original_pic = cursor.getString(cursor.getColumnIndex("original_pic"));
			// user
			String uid = cursor.getString(cursor.getColumnIndex("uid"));
			if (uid!=null) {
				status.user = getUser(uid, USER_INSTATUS);
			}
			// urls
			int i = cursor.getInt(cursor.getColumnIndex("urls"));
			if (i == NINE) {
				status.pic_urls = getPicurls(retweeted_status_id);
			}
		}
		cursor.close();
		return status;

	}

	/**
	 * 得到评论微博源
	 * 
	 * @param retweeted_status_id
	 * @return
	 */
	public Status getCommentStatus(String retweeted_status_id) {
		db = getInstance(READ);
		Cursor cursor = db.query("status", null, "status_id=? and type=3", new String[] { retweeted_status_id },
				null, null, null);
		Status status = new Status();
		if (cursor.moveToFirst()) {
			status.id = cursor.getString(cursor.getColumnIndex("status_id"));
			status.text = cursor.getString(cursor.getColumnIndex("text"));
			status.thumbnail_pic = cursor.getString(cursor.getColumnIndex("thumbnail_pic"));
			status.bmiddle_pic = cursor.getString(cursor.getColumnIndex("bmiddle_pic"));
			status.original_pic = cursor.getString(cursor.getColumnIndex("original_pic"));
			// user
			String uid = cursor.getString(cursor.getColumnIndex("uid"));
			if (uid!=null) {
				status.user = getUser(uid, USER_INCOMMENT);
			}
			// urls
			int i = cursor.getInt(cursor.getColumnIndex("urls"));
			if (i == NINE) {
				status.pic_urls = getPicurls(retweeted_status_id);
			}
		}
		cursor.close();
		return status;

	}

	/**
	 * 清除原数据库数据
	 * 
	 * @return
	 */
	public boolean clearStatus(int stype,int utype) {
		db = getInstance(WRITE);
		ArrayList<Status> statuses = getStatuses(stype);
		int delete;
		for (Status status : statuses) {
			if (status.retweeted_status != null) {
				String status_id = status.retweeted_status.id;
				db.delete("status", "type=1 and status_id=?", new String[] { status_id });
				db.delete("pictures", "status_id=?", new String[] { status_id });
			}
			db.delete("pictures", "status_id=?", new String[] { status.id });
			if (utype==USER_INSTATUS) {
				db.delete("user", "uid=? and type=0", new String[] { status.user.id});
			}
		}
		delete = db.delete("status", "type=?", new String[] { stype + "" });
		return delete > 0 ? true : false;
	}

	/**
	 * 插入用户信息
	 * 
	 * @param user
	 * @param type
	 */
	public void setUser(User user, int type) {
		db = getInstance(WRITE);
		ContentValues values = new ContentValues();
		values.put("uid", user.id);
		values.put("screen_name", user.screen_name);
		values.put("description", user.description);
		values.put("profile_image_url", user.profile_image_url);
		values.put("avatar_large", user.avatar_large);
		values.put("avatar_hd", user.avatar_hd);
		values.put("type", type);
		db.insert("user", null, values);
	}

	/**
	 * 插入多条用户数据
	 * 
	 * @param users
	 * @param type
	 */
	public void setUsers(ArrayList<User> users, int type) {
		db = getInstance(WRITE);

		for (User user : users) {

			ContentValues values = new ContentValues();
			values.put("uid", user.id);
			values.put("screen_name", user.screen_name);
			values.put("description", user.description);
			values.put("profile_image_url", user.profile_image_url);
			values.put("avatar_large", user.avatar_large);
			values.put("avatar_hd", user.avatar_hd);
			values.put("type", type);
			db.insert("user", null, values);
		}
	}

	/**
	 * 查询用户
	 * 
	 * @param uid
	 *            用户id
	 * @param type
	 *            用户类型
	 * @return
	 */
	public User getUser(String uid, int type) {
		db = getInstance(READ);
		User user = new User();
		Cursor cursor = db.query("user", null, "uid=? and type=?", new String[] { uid, type + "" }, null, null, null);
		if (cursor.moveToFirst()) {
			user.id=cursor.getString(cursor.getColumnIndex("uid"));
			user.screen_name = cursor.getString(cursor.getColumnIndex("screen_name"));
			user.description = cursor.getString(cursor.getColumnIndex("description"));
			user.profile_image_url = cursor.getString(cursor.getColumnIndex("profile_image_url"));
			user.avatar_large = cursor.getString(cursor.getColumnIndex("avatar_large"));
			user.avatar_hd = cursor.getString(cursor.getColumnIndex("avatar_hd"));
		}
		cursor.close();
		return user;
	}

	/**
	 * 返回多条用户数据
	 * 
	 * @param type
	 * @return
	 */
	public ArrayList<User> getUsers(int type) {
		db = getInstance(READ);
		ArrayList<User> users = new ArrayList<User>();
		Cursor cursor = null;
		cursor = db.query("user", null, "type=?", new String[] { type + "" }, null, null, null);
		while (cursor.moveToNext()) {
			User user = new User();
			user.id=cursor.getString(cursor.getColumnIndex("uid"));
			user.screen_name = cursor.getString(cursor.getColumnIndex("screen_name"));
			user.description = cursor.getString(cursor.getColumnIndex("description"));
			user.profile_image_url = cursor.getString(cursor.getColumnIndex("profile_image_url"));
			user.avatar_large = cursor.getString(cursor.getColumnIndex("avatar_large"));
			user.avatar_hd = cursor.getString(cursor.getColumnIndex("avatar_hd"));
			users.add(user);
		}
		cursor.close();
		return users;
	}

	/**
	 * 清空用户
	 * 
	 * @param type
	 * @return
	 */
	public boolean clearUser(int type) {
		db = getInstance(WRITE);
		int delete = db.delete("user", "type=?", new String[] { type + "" });
		return delete > 0 ? true : false;
	}

	/**
	 * 插入九宫格图片地址
	 * 
	 * @param status_id
	 * @param pic_urls
	 */
	public void setPicurls(String status_id, ArrayList<String> pic_urls) {
		db = getInstance(WRITE);
		for (int i = 0; i < pic_urls.size(); i++) {
			ContentValues values = new ContentValues();
			values.put("status_id", status_id);
			values.put("url", pic_urls.get(i));
			db.insert("pictures", null, values);
		}
	}

	/**
	 * 查询九宫格图片地址
	 * 
	 * @param status_id
	 * @return
	 */
	public ArrayList<String> getPicurls(String status_id) {
		db = getInstance(READ);
		ArrayList<String> list = new ArrayList<String>();

		Cursor cursor = db.query("pictures", null, "status_id=?", new String[] { status_id }, null, null, null);
		while (cursor.moveToNext()) {
			String url = cursor.getString(cursor.getColumnIndex("url"));
			list.add(url);
		}
		cursor.close();
		return list;
	}

	/**
	 * 插入评论数据
	 * 
	 * @param comments
	 */
	public void setComments(ArrayList<Comment> comments) {
		db = getInstance(WRITE);
		for (Comment comment : comments) {
			ContentValues values = new ContentValues();
			values.put("comment_id", comment.id);
			values.put("created_at", comment.created_at);
			values.put("text", comment.text);
			values.put("source", comment.source);
			values.put("uid", comment.user.id);
			setUser(comment.user, USER_INCOMMENT);
			values.put("status_id", comment.status.id);
			setCommentStatus(comment.status);
			if (comment.reply_comment != null) {
				values.put("reply_comment_id", comment.reply_comment.id);
				setComment(comment.reply_comment);
			}
			values.put("type", COMMENT_ORIGINAL);
			db.insert("comment", null, values);
		}
	}

	/**
	 * 插入单条评论来源评论
	 * 
	 * @param reply_comment
	 */
	public void setComment(Comment reply_comment) {
		db = getInstance(WRITE);
		ContentValues values = new ContentValues();
		values.put("comment_id", reply_comment.id);
		values.put("text", reply_comment.text);
		values.put("uid", reply_comment.user.id);
		setUser(reply_comment.user, USER_INCOMMENT);
		values.put("type", COMMENT_REPLY);
		db.insert("comment", null, values);
	}

	/**
	 * 返回评论数据
	 * 
	 * @param type
	 * @return
	 */
	public ArrayList<Comment> getComments(int type) {
		db = getInstance(READ);
		ArrayList<Comment> comments = new ArrayList<Comment>();
		Cursor cursor = db.query("comment", null, "type=?", new String[] { type + "" }, null, null, null);
		while (cursor.moveToNext()) {
			Comment comment = new Comment();
			comment.id = cursor.getString(cursor.getColumnIndex("comment_id"));
			comment.created_at = cursor.getString(cursor.getColumnIndex("created_at"));
			comment.text = cursor.getString(cursor.getColumnIndex("text"));
			comment.source = cursor.getString(cursor.getColumnIndex("source"));
			String uid = cursor.getString(cursor.getColumnIndex("uid"));
			
			if (uid!=null) {
				comment.user = getUser(uid, USER_INCOMMENT);
			}
			String status_id = cursor.getString(cursor.getColumnIndex("status_id"));
			comment.status = getCommentStatus(status_id);
			String reply_comment_id = cursor.getString(cursor.getColumnIndex("reply_comment_id"));
			if (reply_comment_id != null) {
				comment.reply_comment = getComment(reply_comment_id);
			}

			comments.add(comment);
		}
		cursor.close();
		return comments;

	}

	/**
	 * 获取单条评论来源评论
	 * 
	 * @param reply_comment_id
	 * @return
	 */
	public Comment getComment(String reply_comment_id) {
		db = getInstance(READ);
		Cursor cursor = db.query("comment", null, "comment_id=?", new String[] { reply_comment_id }, null, null, null);
		Comment comment = new Comment();
		if (cursor.moveToFirst()) {
			comment.id = cursor.getString(cursor.getColumnIndex("comment_id"));
			comment.text = cursor.getString(cursor.getColumnIndex("text"));
			String uid = cursor.getString(cursor.getColumnIndex("uid"));
			if (uid!=null) {
				comment.user = getUser(uid, USER_INCOMMENT);
			}
		}
		cursor.close();
		return comment;
	}

	/**
	 * 清除评论数据
	 * 
	 * @return
	 */
	public boolean clearComment(int type) {
		db = getInstance(WRITE);
		db.delete("user", "type=?", new String[]{type+""});
		
		int delete = db.delete("comment", null, null);
		return delete > 0 ? true : false;
	}
	/**
	 * 储存联想搜索的信息
	 * @param info
	 * @return
	 */
	public long setSearchImformation(String info) {
		db=getInstance(WRITE);
		ContentValues values=new ContentValues();
		values.put("msg", info);
		
		return db.insert("searchImfo", null, values);
	}
	/**
	 * 查找联想搜索的信息
	 * @return
	 */
	public List<String> getSearchImformations() {
		List<String> data=new ArrayList<String>();
		db=getInstance(READ);
		Cursor cursor = db.query("searchImfo", null, null, null, null, null, null);
		while(cursor.moveToNext()){
			String msg = cursor.getString(cursor.getColumnIndex("msg"));
			data.add(msg);
		}
		cursor.close();
		return data;
	}
	
	/**
	 * 查找联想搜索的信息
	 * @return
	 */
	public String getSearchImformation(String text) {
		db=getInstance(READ);
		Cursor cursor = db.query("searchImfo", null, "msg=?", new String[]{text}, null, null, null);
		String msg=null;
		if (cursor.moveToFirst()) {
			msg = cursor.getString(cursor.getColumnIndex("msg"));
		}
		
		cursor.close();
		return msg;
	}
}
