package com.gamefps.sdk.reyun;

import java.util.Map;

import com.gamefps.sdkbridge.IActivityOverrider;
import com.gamefps.sdkbridge.IAnalyticsSDK;
import com.gamefps.sdkbridge.ISDKWarpper;
import com.gamefps.sdkbridge.SdkConfigInfo;
import com.gamefps.sdkbridge.SdkProviderInfo;
import com.reyun.sdk.ReYun;
import com.reyun.sdk.ReYun.AccountType;
import com.reyun.sdk.ReYun.Gender;
import com.reyun.sdk.ReYun.QuestStatus;
import com.reyun.sdk.ReYunChannel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ReYunSDKAdapter implements ISDKWarpper, IAnalyticsSDK,IActivityOverrider  {
	
	private final SdkProviderInfo _providerInfo = new SdkProviderInfo("REYUN","Reyun_GameAnalytics"); 
	private boolean _reyun_is_active = true;
	private boolean _reyunchannel_is_active = true;
	private String _account;
	private int _playerLevel = -1;
	private String _gameZone;
	@Override
	public boolean init(Activity ctx,SdkConfigInfo cfg) {
		ReYun.initWithKeyAndChannelId(ctx,cfg.channelId);
		ReYunChannel.initWithKeyAndChannelId(ctx, cfg.channelId);
		return true;
	}

	@Override
	public void setAccount(String account) {
		_account = account;
	}
	@Override
    public void setGameZone(String zoneName){
		_gameZone = zoneName;
    }
	@Override
	public void setRoleInfo(Map<String,String> roleData) {
		int level = 0;
		
		if(roleData.containsKey("Level")){
			level = Integer.parseInt(roleData.get("Level"));
		}
		ReYun.setLoginWithAccountID(_account, level, _gameZone);
		ReYunChannel.setLoginSuccessBusiness(_account);
	}

	@Override
	public void onResume(Activity act) {
		if(!_reyun_is_active){
			ReYun.startHeartBeat(act);
			_reyun_is_active = true;	
		}
		if(!_reyunchannel_is_active){
			ReYunChannel.startHeartBeat(act);
			_reyunchannel_is_active = true;
		}
		
		
	}

	@Override
	public void onPause(Activity act) {
		if(!ReYun.isAppOnForeground()){
			_reyun_is_active = false;
		}
		if(!ReYunChannel.isAppOnForeground()){
			_reyunchannel_is_active = false;
		}
		
	}

	@Override
	public SdkProviderInfo getProviderInfo() {
		return _providerInfo;
	}



	@Override
	public void logEvent(String eventName, Map<String, String> eventData) {
		ReYun.setEvent(eventName, eventData);
	}

	@Override
	public void onCreateAccount(String account,Map<String, String> accountData) {
		_gameZone = accountData.get("ServerId");
		ReYun.setRegisterWithAccountID(account,AccountType.ANONYMOUS.name() , Gender.UNKNOWN  ,-1, _gameZone);
		ReYunChannel.setRegisterWithAccountID(account);
	}

	@Override
	public void onCreateRole(String roleName,Map<String, String> roleData) {
		//TalkingDataAppCpa.onCreateRole(roleName);
	}

	@Override
	public void onQuestUpdate(String questType, String questId, QuestState state, String comment) {
		
		QuestStatus st;
		switch (state) {
		case QS_Complete:
			st = QuestStatus.c;
			break;
		case QS_Fail:
			st = QuestStatus.f;
			break;
		case QS_Start:
			st = QuestStatus.a;
			break;
		default:
			return;
		}
		ReYun.setQuest(questId, st, questType,_playerLevel);
	}

	@Override
	public void onCreate(Activity act, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestroy(Activity act) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStop(Activity act) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRestart(Activity act) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActivityResult(Activity act, int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNewIntent(Activity act,Intent intent) {
		// TODO Auto-generated method stub
		
	}
}
