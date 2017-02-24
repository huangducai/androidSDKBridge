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
	
	
	//���е���ͬһ������ ����bug ����
	public void sdkEvent(String event,Map<String, String> eventData){
		try{
		if(event.equals("role_online")){
			//_serverName = ;
			SDK.getInstance().getGameEventHelper().onlineRole(eventData.get("serverName"), eventData.get("roleName"),  Integer.parseInt(eventData.get("roleLevel")));
		}else 	if(event.equals("role_create")){
			//������ɫ ������ͳ�� �������Ϣ ������
			SDK.getInstance().getGameEventHelper().createRole(eventData.get("serverName"), eventData.get("roleName"));
		}else if(event.equals("app_activation")){
			SDK.getInstance().getAppEventHelper().appActivation();
		}else if(event.equals("app_crash")){
			SDK.getInstance().getAppEventHelper().appCrash(eventData.get("activityName"), eventData.get("crashMessage"));
		}else if(event.equals("pay_for_item")){
			// ������
			String serverName = eventData.get("serverName");
			// ��ɫ��
			String roleName = eventData.get("roleName");
			// ��ɫ�ȼ�
			int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
			// vip�ȼ�
			int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
			// ��ȡ�������� APP λ��
			String place = eventData.get("roleName");
			// ���������
			String coinType =  eventData.get("coinType");
			// ��������
			int number = Integer.parseInt(eventData.get("number"));
			// ��������
			String item = eventData.get("item");
			// ��������
			int amount = Integer.parseInt(eventData.get("amount"));
			// ���ʣ�����������
			int coins =  Integer.parseInt(eventData.get("coins"));
			SDK.getInstance().getGameEventHelper().consumeForItems(serverName, roleName,
			    roleLevel, vipLevel, place, coinType, number, item, amount, coins);
		}else if(event.equals("player_item_gain")){
			// ������
			String serverName = eventData.get("serverName");
			// ��ɫ��
			String roleName = eventData.get("roleName");
			// ��ɫ�ȼ�
			int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
			// vip�ȼ�
			int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
			// ������Դ
			String source = eventData.get("source");
			// ��������
			String item = eventData.get("source");
			// ��ȡ����
			int number =Integer.parseInt(eventData.get("number"));

			SDK.getInstance().getGameEventHelper().gainItems(serverName, roleName,
			    roleLevel, vipLevel, source, item, number);
		}else if(event.equals("player_item_consume")){
			// ������
			String serverName = eventData.get("serverName");
			// ��ɫ��
			String roleName = eventData.get("roleName");
			//  ��ɫ�ȼ�
			int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
			// vip�ȼ�
			int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
			// ʹ�ó���
			String destination = eventData.get("destination");
			// ��������
			String item = eventData.get("item");
			// ��������
			int number = Integer.parseInt(eventData.get("number"));
			SDK.getInstance().getGameEventHelper().useItems(serverName, roleName,
			    roleLevel, vipLevel, destination, item, number);
		}else if(event.equals("activity_start")){
			// ������
			String serverName = eventData.get("serverName");
			// ��ɫ��
			String roleName = eventData.get("roleName");
			// ��ɫ�ȼ�
			int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
			// vip�ȼ�
			int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
			// �ؿ����
			String missionType = eventData.get("missionType");
			// �ؿ�/��������
			String mission = eventData.get("mission");

			SDK.getInstance().getGameEventHelper().startActivity(serverName,
			    roleName, roleLevel, vipLevel, missionType, mission);
		}else if(event.equals("activity_finish")){
			// ������
			String serverName =  eventData.get("serverName");
			// ��ɫ��
			String roleName =  eventData.get("roleName");
			// ��ɫ�ȼ�
			int roleLevel =  Integer.parseInt(eventData.get("roleLevel"));
			// vip�ȼ�
			int vipLevel =  Integer.parseInt(eventData.get("vipLevel"));
			// �ؿ����
			String missionType =  eventData.get("missionType");
			// �ؿ�/��������
			String mission =  eventData.get("mission");
			// �Ƿ�ͨ���ؿ�/����
			boolean passed = true;
			// �ؿ�/�����ʱ
			int duration = Integer.parseInt(eventData.get("duration"));

			SDK.getInstance().getGameEventHelper().finishActivity(serverName,
			    roleName, roleLevel, vipLevel, missionType,  mission, passed, duration);
		}else if(event.equals("player_get_exp")){
			// ������
			String serverName =  eventData.get("serverName");
			// ��ɫ��
			String roleName = eventData.get("roleName");
			// ��ɫ�ȼ�
			int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
			// vip�ȼ�
			int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
			// ������Դ
			String source = eventData.get("source");
			// ����ֵ
			int number = Integer.parseInt(eventData.get("number"));

			SDK.getInstance().getGameEventHelper().gainExp(serverName, roleName,
			    roleLevel, vipLevel, source, number);
			
		}else if(event.equals("player_consume_exp")){
			// ������
			String serverName = eventData.get("serverName");
			// ��ɫ��
			String roleName = eventData.get("roleName");
			// ��ɫ�ȼ�
			int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
			// vip�ȼ�
			int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
			// ���ľ���Ŀ�ģ���;��
			String destination = eventData.get("destination");
			// ����ֵ
			int number = Integer.parseInt(eventData.get("number"));
			SDK.getInstance().getGameEventHelper().consumeExp(serverName, roleName,
			    roleLevel, vipLevel, destination, number);
		}else if(event.equals("role_delete")){
			// ������
			String serverName = eventData.get("serverName");
			// ��ɫ��
			String roleName = eventData.get("roleName");
			// ��ɫ�ȼ�
			int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
			// vip�ȼ�
			int vipLevel = Integer.parseInt(eventData.get("vipLevel"));

			SDK.getInstance().getGameEventHelper().deleteRole(serverName, roleName,
			    roleLevel, vipLevel);
		}else if(event.equals("role_offline")){
			// ������
			String serverName = eventData.get("serverName");
			// ��ɫ��
			String roleName = eventData.get("roleName");
			// ��ɫ�ȼ�
			int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
			// vip�ȼ�
			int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
			// ��������ʱ������λ���룩
			int onlineTime = Integer.parseInt(eventData.get("onlineTime"));
			// �ǳ�����
			String scene = eventData.get("scene");
			// �ǳ�ʱ���ڳ���������
			String axis = eventData.get("axis");
			// �ǳ�ǰ��ɫ���һ�β���������ɵ��淨
			String lastOperation = eventData.get("lastOperation");

			SDK.getInstance().getGameEventHelper().offlineRole(serverName, roleName,
			    roleLevel, vipLevel, onlineTime, scene, axis, lastOperation);
			
			}else{
				  System.out.println("����֧�ֵ��¼���" + event);	
			}
		}catch(ArithmeticException  e){
			  System.out.println("��������");
		}
	}
}
