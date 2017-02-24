package com.gamefps.sdkbridge.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class EventReceiver extends BroadcastReceiver {
	public final static String ALARM_ACTION_NOTIFICATION = "AlarmActionNotification";
	@Override
	public void onReceive(Context ctx, Intent intent) {
		// TODO Auto-generated method stub
		Log.d("notification", "EventReceiver:onReceive(" + intent.getAction() + ")");
		if(intent.getAction() == ALARM_ACTION_NOTIFICATION){
			showNotification(ctx.getApplicationContext(),intent);
			long messageId = intent.getLongExtra("messageId",0);
			String activityClassName = intent.getStringExtra("ActivityClassName");
			LocalNotification.scheduleNextNotification(ctx.getApplicationContext(), messageId,activityClassName);
		}
		
	}

	private void showNotification(Context ctx,Intent intent){
		
		int icon = intent.getIntExtra("icon", 0);
		String tag = intent.getStringExtra("tag");
		String title = intent.getStringExtra("title");
		String text = intent.getStringExtra("text");
		String activityClassName = intent.getStringExtra("ActivityClassName");
		
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(ctx)
		        .setSmallIcon(icon)
		        .setContentTitle(title)
		        .setContentText(text);
		
		
		// Creates an explicit intent for an Activity in your app
		Class<?> mainClass = null;
		try {
			mainClass = Class.forName(activityClassName);
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Intent resultIntent = new Intent(ctx,mainClass);
		PendingIntent pi = PendingIntent.getActivity(ctx, 1, resultIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		
		mBuilder.setContentIntent(pi);
		mBuilder.setAutoCancel(true);
		
		NotificationManager mNotificationManager =
		    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification noti =  mBuilder.build();
		mNotificationManager.notify(tag,1,noti);
		
        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        @SuppressWarnings("deprecation")
		WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK |  PowerManager.ACQUIRE_CAUSES_WAKEUP, "GameNotification");
        wl.acquire(2000);		
	}
}
