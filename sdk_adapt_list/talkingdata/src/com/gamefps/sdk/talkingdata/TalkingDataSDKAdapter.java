package com.gamefps.sdk.talkingdata;

import java.util.Map;
import com.gamefps.sdkbridge.IActivityOverrider;
import com.gamefps.sdkbridge.IAnalyticsSDK;
import com.gamefps.sdkbridge.IAuthenticationSDK;
import com.gamefps.sdkbridge.ISDKWarpper;
import com.gamefps.sdkbridge.LoginAccountInfo;
import com.gamefps.sdkbridge.SDKBridge;
import com.gamefps.sdkbridge.SdkConfigInfo;
import com.gamefps.sdkbridge.SdkProviderInfo;
import com.tendcloud.appcpa.TalkingDataAppCpa;
import com.tendcloud.tenddata.TDGAAccount;
import com.tendcloud.tenddata.TDGAAccount.AccountType;
import com.tendcloud.tenddata.TDGAMission;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.tendcloud.tenddata.TalkingDataGA;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

public class TalkingDataSDKAdapter implements ISDKWarpper, IAnalyticsSDK,IAuthenticationSDK,IActivityOverrider {
	TDGAAccount _currentAccount;
	private final SdkProviderInfo _providerInfo = new SdkProviderInfo("TALKINGDATA","TalkingData_GameAnalytics",0,false); 
	
	
	@Override
	public boolean init(Activity ctx,SdkConfigInfo cfg) {

		TalkingDataGA.init(ctx, "0174C774C12011EA2F7ACCAA78EDA179", cfg.channelId);	//FB5B32C387B11282853ACAB9D902E48C
		TalkingDataAppCpa.init(ctx, "2382a605958b4df38d92d7e577931e88", cfg.channelId);	// 0630a43bc79d4c75bc887bf1c73a7b46
		return true;
	}
	@Override
	public void confirmExit(Activity ctx,SDKBridge callback) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("TalkingData plugin not support exit confirm UI!");
	}

	@Override
	public void setRoleInfo(Map<String,String> roleData) {
		if(null == _currentAccount)
			return;
		if(roleData.containsKey("Level")){
			int level = Integer.parseInt(roleData.get("Level"));
			_currentAccount.setLevel(level);
		}
		if(roleData.containsKey("Name")){
			//_currentAccount.setAccountName(roleData.get("Name"));
		}
	}
	@Override
	public void onEnterGame(String serverId, String roleName) {
		if(null!=_currentAccount){
			_currentAccount.setGameServer(serverId);
			_currentAccount.setAccountName(roleName);
		}
	}
	@Override
	public void onResume(Activity act) {
		TalkingDataGA.onResume(act);		
	}

	@Override
	public void onPause(Activity act) {
		TalkingDataGA.onPause(act);
	}

	@Override
	public SdkProviderInfo getProviderInfo() {
		return _providerInfo;
	}


	@Override
	public void Login(Activity act,SDKBridge cb) {
		LoginAccountInfo ai = new LoginAccountInfo();
		ai.channelPrefix = "TD";
		ai.channelAccountId = TalkingDataGA.getDeviceId(act);
		ai.accountId = ai.channelPrefix + "_" + ai.channelAccountId;
		ai.isNewAccount = false;
		cb.OnAuthenticationSuccess(this, ai);
	}

	@Override
	public void logEvent(String eventName, Map<String, String> eventData) {
		TalkingDataGA.onEvent(eventName, eventData);
	}

	@Override
	public void onCreateAccount(String account,Map<String, String> accountData) {
		TalkingDataAppCpa.onRegister(account);
		_currentAccount = TDGAAccount.setAccount(account);
		_currentAccount.setAccountType(AccountType.REGISTERED);
	}
	@Override
	public void onLoginAccount(String account,Map<String, String> accountData) {
		TalkingDataAppCpa.onLogin(account);
		_currentAccount = TDGAAccount.setAccount(account);
	}

	@Override
	public void onCreateRole(String ServerId, String roleName, String roleId, int roleCreateTime) {
		TalkingDataAppCpa.onCreateRole(roleName);
		if(null!=_currentAccount){
			_currentAccount.setGameServer(ServerId);
			_currentAccount.setAccountName(roleName);
		}
	}

	@Override
	public void onQuestUpdate(String questType, String questId, QuestState state, String comment) {
		
		String qid = questType + "_" + questId; 
		switch(state){
		case QS_Complete:
			TDGAMission.onCompleted(qid);
			break;
		case QS_Fail:
			TDGAMission.onFailed(qid, comment);
			break;
		case QS_Start:
			TDGAMission.onBegin(qid);
			break;
		default:
			break;
		}
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
		
		
	}

	@Override
	public void onActivityResult(Activity act, int requestCode, int resultCode, Intent data) {
		
		
	}

	@Override
	public void onNewIntent(Activity act,Intent intent) {
		
		
	}
	@Override
	public void onCurrencyGain(String currencyType, String reason, int amount) {
		
		
	}
	@Override
	public void onCurrencyConsume(String currencyType, String reason, int amount) {
		
		
	}
	@Override
	public void onEnterLoginView(SDKBridge callback){

	}
	@Override
	public void logout(){

	}
	@Override
	public void onGameCheckVersionBegin(){

	}
	@Override
	public void onGameCheckVersionEnd(){

	}


	@Override
	public void onStart(Activity act){

	}
	@Override
	public void onSaveInstanceState(Activity act, Bundle outState){

	}
	@Override
	public void onConfigurationChanged(Activity act, Configuration newConfig){

	}
}
