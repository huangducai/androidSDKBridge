package com.gamefps.sdkbridge.notification;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class LocalNotification {
	public final static String DB_FILE_NAME = "Notifications.db";
	public final static int ALARM_ID = 234256;

public static void clearNotifications(Context ctx){

		SQLiteDatabase db = getDB(ctx);
		db.delete("notification",null,null );
		db.close();
		cancelAlarm(ctx);
	}
	

	public static void setNotifications(Context ctx,ArrayList<NotificationItem> notifications,Class<? extends Activity> _appActivityClass){
		if (null == notifications || notifications.isEmpty()){
			clearNotifications(ctx);		
			return;
		}
		
		NotificationItem earlestItem  = null;
		
		SQLiteDatabase db = getDB(ctx);
		db.beginTransaction();
		db.delete("notification",null,null );
		for (NotificationItem item : notifications){
			ContentValues vals = new ContentValues(5);
			vals.put("time",item.time);
			vals.put("tag", item.tag);
			vals.put("title", item.title);
			vals.put("text",item.text);
			vals.put("icon", item.icon);
			item.id  = db.insert("notification", "", vals);
			if(null==earlestItem){
				earlestItem = item;
			}else if(earlestItem.time>item.time){
				earlestItem = item;
			}	
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		
		setupAlarm(ctx,earlestItem,_appActivityClass.getName());
	}
	public static void scheduleNextNotification(Context ctx,long currentId,String targetActivityName){
		Log.d("noti","scheduleNextNotification");
		SQLiteDatabase db = getDB(ctx);
		if( currentId!=0 ){
			int deleteCount = db.delete("notification","id=?",new String[]{ Long.toString(currentId) });
			Log.d("notification", "deleted " + Integer.toString(deleteCount) + " rows!");
		}
			
		
		Cursor cr = db.query("notification",new String[]{"id","tag","time","icon","title","text"} , null, null, null, null, "time","1");
		if(cr.moveToFirst()){
			NotificationItem noti = new NotificationItem();
			noti.id  = cr.getLong(0);
			noti.tag = cr.getString(1);
			noti.time = cr.getInt(2);
			noti.icon = cr.getInt(3);
			noti.title = cr.getString(4);
			noti.text = cr.getString(5);			
			setupAlarm(ctx, noti,targetActivityName);
		}
		db.close();
	}
	
	
	
	public static void setupAlarm(Context ctx,NotificationItem noti,String targetActivityName){
		
		long triggerTime = noti.time;
		triggerTime *=1000;
		Log.d("notification","alarm set0!");
		
		Intent reciver = new Intent(ctx,EventReceiver.class);
		reciver.setAction(EventReceiver.ALARM_ACTION_NOTIFICATION);
		reciver.putExtra("messageId", noti.id);
		reciver.putExtra("icon",noti.icon);
		reciver.putExtra("title", noti.title);
		reciver.putExtra("text",noti.text);
		reciver.putExtra("tag", noti.tag);
		reciver.putExtra("ActivityClassName",targetActivityName);
		
		PendingIntent pi = PendingIntent.getBroadcast(ctx, ALARM_ID, reciver,PendingIntent.FLAG_UPDATE_CURRENT);
		Log.d("notification","alarm set1!");
		
		AlarmManager am = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
		
		am.set(AlarmManager.RTC_WAKEUP, triggerTime, pi);
		Log.d("notification","alarm set2! time:" + Long.toString(triggerTime) );
	}
	
	public static void cancelAlarm(Context ctx){
		Log.d("NotiDbg", "cancelAlarm_begin");
		Intent reciver = new Intent(ctx,EventReceiver.class);
		reciver.setAction(EventReceiver.ALARM_ACTION_NOTIFICATION);
		PendingIntent pi = PendingIntent.getBroadcast(ctx, ALARM_ID, reciver,PendingIntent.FLAG_NO_CREATE);
		if(null!=pi){
			AlarmManager am = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
			
			Log.d("NotiDbg", "cancelAlarm_cancel");
			am.cancel(pi);
		}
		Log.d("NotiDbg", "cancelAlarm_end");
	}
	
	private static SQLiteDatabase getDB(Context ctx){
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(ctx.getApplicationInfo().dataDir + "/" + DB_FILE_NAME, null);
		db.execSQL("CREATE TABLE IF NOT EXISTS notification (id INTEGER PRIMARY KEY,time INTEGER,tag TEXT,title TEXT,text TEXT,icon INTEGER);");
		return db;
	}
	
	
}
