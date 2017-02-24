package com.gamefps.sdk.uceventsdk;

import java.util.Map;

import com.gamefps.sdkbridge.IActivityOverrider;
import com.gamefps.sdkbridge.IAnalyticsSDK;
import com.gamefps.sdkbridge.ISDKWarpper;
import com.gamefps.sdkbridge.SDKBridge;
import com.gamefps.sdkbridge.SdkConfigInfo;
import com.gamefps.sdkbridge.SdkProviderInfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import cn.uc.dp.sdk.Constants.Mode;
import cn.uc.dp.sdk.SDK;


public class UCEventSDKAdapter implements ISDKWarpper,IActivityOverrider,IAnalyticsSDK {
	
	private final static SdkProviderInfo SDK_PROVIDER_INFO = new SdkProviderInfo("UCEVENTSDK", "ucevent sdk adapter",1,true);
	public final static String TAG = "UCEventSDKAdapter";
	private Activity _activity;
	
	@Override
	public SdkProviderInfo getProviderInfo() {
		// TODO Auto-generated method stub
		return SDK_PROVIDER_INFO;
	}
	
	@Override
	public boolean init(Activity act, SdkConfigInfo cfg) {
		_activity = act;
		SDK.getInstance().init(act, "467787787756", "40c2a4d0027b77f298d4dc0a5725ae46",cfg.channelId, Mode.PRODUCTION);
		
		SDK.getInstance().getAppEventHelper().appStartup();
		
		TelephonyManager tm = (TelephonyManager) _activity.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        
    	Log.e("uc_____deviced:", deviceId);
		return true;
	}
	
	@Override
	public void confirmExit(Activity ctx, SDKBridge callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGameCheckVersionBegin() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGameCheckVersionEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEnterLoginView(SDKBridge callback) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onCreate(Activity act, Bundle savedInstanceState) {
		
	}
	
	@Override
	public void onDestroy(Activity act) {
		SDK.getInstance().onStop();
	}
	
	@Override
	public void onPause(Activity act) {
		
	}
	
	@Override
	public void onResume(Activity act) {
		SDK.getInstance().onCheck();
	}
	
	@Override
	public void onStop(Activity act) {
		
	}
	
	@Override
	public void onRestart(Activity act) {

	}
	
	@Override
	public void onActivityResult(Activity act, int requestCode, int resultCode, Intent data) {
	}
	
	@Override
	public void onNewIntent(Activity act,Intent intent) {
	}
	
	@Override
	public void onStart(Activity act) {
	}

	@Override
	public void onSaveInstanceState(Activity act, Bundle outState) {
	}
	
	@Override
	public void onConfigurationChanged(Activity act, Configuration newConfig) {
	}
	
	@Override
	public void setRoleInfo(Map<String, String> roleData) {
	}
	
	@Override
	public void onCreateRole(String serverID, String roleName, String roleId, int roleCreateTime) {
	}
	
	public String getAccount(String account){
		String str = account.substring(0, 6);
		if(str.equals("UC_JY_")){
			return account.substring(6);
		}
		return account;
	}
	
	@Override
	public void onCreateAccount(String account, Map<String, String> accountData) {
		SDK.getInstance().getGameEventHelper().createUser("UC", getAccount(account));
	}
	
	@Override
	public void onLoginAccount(String account, Map<String, String> accountData) {
		SDK.getInstance().getGameEventHelper().onlineUser("UC", getAccount(account));
	}
	
	@Override
	public void onEnterGame(String serverId, String roleName) {
	}
	
	@Override
	public void onQuestUpdate(String questType, String questId, QuestState state, String comment) {
	}
	
	@Override
	public void onCurrencyGain(String currencyType, String reason, int amount) {
	}
	
	@Override
	public void onCurrencyConsume(String currencyType, String reason, int amount) {
	}
	
	@Override
	public void logEvent(String event, Map<String, String> eventData) {
		sdkEvent(event, eventData);
	}
	
	
	//所有的在同一处处理 方便bug 查找
	public void sdkEvent(String event,Map<String, String> eventData){
		try{
		if(event.equals("role_online")){
			//_serverName = ;
			SDK.getInstance().getGameEventHelper().onlineRole(eventData.get("serverName"), eventData.get("roleName"),  Integer.parseInt(eventData.get("roleLevel")));
		}else 	if(event.equals("role_create")){
			//创建角色 在这里统计 上面的信息 不完整
			SDK.getInstance().getGameEventHelper().createRole(eventData.get("serverName"), eventData.get("roleName"));
		}else if(event.equals("app_activation")){
			SDK.getInstance().getAppEventHelper().appActivation();
		}else if(event.equals("app_crash")){
			SDK.getInstance().getAppEventHelper().appCrash(eventData.get("activityName"), eventData.get("crashMessage"));
		}else if(event.equals("pay_for_item")){
			// 区服名
			String serverName = eventData.get("serverName");
			// 角色名
			String roleName = eventData.get("roleName");
			// 角色等级
			int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
			// vip等级
			int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
			// 获取道具所在 APP 位置
			String place = eventData.get("roleName");
			// 虚拟币类型
			String coinType =  eventData.get("coinType");
			// 消耗数量
			int number = Integer.parseInt(eventData.get("number"));
			// 道具名称
			String item = eventData.get("item");
			// 购买数量
			int amount = Integer.parseInt(eventData.get("amount"));
			// 玩家剩余虚拟币数量
			int coins =  Integer.parseInt(eventData.get("coins"));
			SDK.getInstance().getGameEventHelper().consumeForItems(serverName, roleName,
			    roleLevel, vipLevel, place, coinType, number, item, amount, coins);
		}else if(event.equals("player_item_gain")){
			// 区服名
			String serverName = eventData.get("serverName");
			// 角色名
			String roleName = eventData.get("roleName");
			// 角色等级
			int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
			// vip等级
			int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
			// 道具来源
			String source = eventData.get("source");
			// 道具名称
			String item = eventData.get("source");
			// 获取数量
			int number =Integer.parseInt(eventData.get("number"));

			SDK.getInstance().getGameEventHelper().gainItems(serverName, roleName,
			    roleLevel, vipLevel, source, item, number);
		}else if(event.equals("player_item_consume")){
			// 区服名
			String serverName = eventData.get("serverName");
			// 角色名
			String roleName = eventData.get("roleName");
			//  角色等级
			int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
			// vip等级
			int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
			// 使用场景
			String destination = eventData.get("destination");
			// 道具名称
			String item = eventData.get("item");
			// 消耗数量
			int number = Integer.parseInt(eventData.get("number"));
			SDK.getInstance().getGameEventHelper().useItems(serverName, roleName,
			    roleLevel, vipLevel, destination, item, number);
		}else if(event.equals("activity_start")){
			// 区服名
			String serverName = eventData.get("serverName");
			// 角色名
			String roleName = eventData.get("roleName");
			// 角色等级
			int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
			// vip等级
			int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
			// 关卡类别
			String missionType = eventData.get("missionType");
			// 关卡/任务名称
			String mission = eventData.get("mission");

			SDK.getInstance().getGameEventHelper().startActivity(serverName,
			    roleName, roleLevel, vipLevel, missionType, mission);
		}else if(event.equals("activity_finish")){
			// 区服名
			String serverName =  eventData.get("serverName");
			// 角色名
			String roleName =  eventData.get("roleName");
			// 角色等级
			int roleLevel =  Integer.parseInt(eventData.get("roleLevel"));
			// vip等级
			int vipLevel =  Integer.parseInt(eventData.get("vipLevel"));
			// 关卡类别
			String missionType =  eventData.get("missionType");
			// 关卡/任务名称
			String mission =  eventData.get("mission");
			// 是否通过关卡/任务
			boolean passed = true;
			// 关卡/任务耗时
			int duration = Integer.parseInt(eventData.get("duration"));

			SDK.getInstance().getGameEventHelper().finishActivity(serverName,
			    roleName, roleLevel, vipLevel, missionType,  mission, passed, duration);
		}else if(event.equals("player_get_exp")){
			// 区服名
			String serverName =  eventData.get("serverName");
			// 角色名
			String roleName = eventData.get("roleName");
			// 角色等级
			int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
			// vip等级
			int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
			// 经验来源
			String source = eventData.get("source");
			// 经验值
			int number = Integer.parseInt(eventData.get("number"));

			SDK.getInstance().getGameEventHelper().gainExp(serverName, roleName,
			    roleLevel, vipLevel, source, number);
			
		}else if(event.equals("player_consume_exp")){
			// 区服名
			String serverName = eventData.get("serverName");
			// 角色名
			String roleName = eventData.get("roleName");
			// 角色等级
			int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
			// vip等级
			int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
			// 消耗经验目的（用途）
			String destination = eventData.get("destination");
			// 经验值
			int number = Integer.parseInt(eventData.get("number"));
			SDK.getInstance().getGameEventHelper().consumeExp(serverName, roleName,
			    roleLevel, vipLevel, destination, number);
		}else if(event.equals("role_delete")){
			// 区服名
			String serverName = eventData.get("serverName");
			// 角色名
			String roleName = eventData.get("roleName");
			// 角色等级
			int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
			// vip等级
			int vipLevel = Integer.parseInt(eventData.get("vipLevel"));

			SDK.getInstance().getGameEventHelper().deleteRole(serverName, roleName,
			    roleLevel, vipLevel);
		}else if(event.equals("role_offline")){
			// 区服名
			String serverName = eventData.get("serverName");
			// 角色名
			String roleName = eventData.get("roleName");
			// 角色等级
			int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
			// vip等级
			int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
			// 本次在线时长（单位：秒）
			int onlineTime = Integer.parseInt(eventData.get("onlineTime"));
			// 登出场景
			String scene = eventData.get("scene");
			// 登出时所在场景的坐标
			String axis = eventData.get("axis");
			// 登出前角色最后一次操作或者完成的玩法
			String lastOperation = eventData.get("lastOperation");

			SDK.getInstance().getGameEventHelper().offlineRole(serverName, roleName,
			    roleLevel, vipLevel, onlineTime, scene, axis, lastOperation);
			
			}else{
				  System.out.println("不是支持的事件：" + event);	
			}
		}catch(ArithmeticException  e){
			  System.out.println("参数错误");
		}
	}
}
