package com.gamefps.sdk.anysdk;

import java.util.Map;

import com.anysdk.framework.PluginWrapper;
import com.anysdk.framework.UserWrapper;
import com.anysdk.framework.java.AnySDK;
import com.anysdk.framework.java.AnySDKListener;
import com.anysdk.framework.java.AnySDKUser;
import com.gamefps.sdkbridge.IActivityOverrider;
import com.gamefps.sdkbridge.IAnalyticsSDK;
import com.gamefps.sdkbridge.IAuthenticationCallback;
import com.gamefps.sdkbridge.IAuthenticationSDK;
import com.gamefps.sdkbridge.ISDKWarpper;
import com.gamefps.sdkbridge.SdkConfigInfo;
import com.gamefps.sdkbridge.SdkProviderInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class AnySDKAdapter implements AnySDKListener, IAuthenticationSDK, ISDKWarpper,IActivityOverrider,IAnalyticsSDK {
	
	private final static SdkProviderInfo SDK_PROVIDER_INFO = new SdkProviderInfo("ANYSDK", "any sdk adapter");
	
	private boolean _anysdk_init_completed; 
	private IAuthenticationCallback _loginCallback;
	
	@Override
	public boolean init(Activity ctx, SdkConfigInfo cfg) {
		_anysdk_init_completed = false;
		_loginCallback = null;
		String appKey =  "0DCEBB66-65E0-91DE-DBAA-86A567EFC3E2";
		String appSecret = "6dbbb32f78d85ded1287d21874b6a9a7";
		String privateKey = "16CAEE88846E98DA267C5BC619242748";
		String oauthLoginServer = "http://oauth.anysdk.com/api/OauthLoginDemo/Login.php";
		AnySDK.getInstance().init(ctx, appKey, appSecret, privateKey, oauthLoginServer);
		AnySDKUser.getInstance().setListener(this);
		return true;
	}

	@Override
	public SdkProviderInfo getProviderInfo() {
		// TODO Auto-generated method stub
		return SDK_PROVIDER_INFO;
	}

	@Override
	public String getAccountPrefix() {
		//AnySDKUser.getInstance().getPluginId();
		return "ANYSDK_";
	}

	@Override
	public void Login(Activity act, IAuthenticationCallback cb) {
		if(!_anysdk_init_completed){
			cb.OnAuthenticationFailed(this, "sdk not init");
			return;
		}
		_loginCallback = cb;
		AnySDKUser.getInstance().login();
	}

	@Override
	public void setAccount(String account) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGameZone(String zoneName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRoleInfo(Map<String, String> roleData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCreateAccount(String account, Map<String, String> accountData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCreateRole(String roleName, Map<String, String> roleData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onQuestUpdate(String questType, String questId, QuestState state, String comment) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void logEvent(String event, Map<String, String> eventData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResume(Activity act) {
		PluginWrapper.onResume();
	}

	@Override
	public void onPause(Activity act) {
		PluginWrapper.onPause();
	}

	@Override
	public void onCreate(Activity act, Bundle savedInstanceState) {
		
	}

	@Override
	public void onDestroy(Activity act) {
		PluginWrapper.onDestroy();
		AnySDK.getInstance().release();
	}

	@Override
	public void onStop(Activity act) {
		PluginWrapper.onStop();
		
	}

	@Override
	public void onRestart(Activity act) {
		PluginWrapper.onRestart();
		
	}

	@Override
	public void onActivityResult(Activity act, int requestCode, int resultCode, Intent data) {
		PluginWrapper.onActivityResult(requestCode, resultCode, data);
		
	}

	@Override
	public void onNewIntent(Activity act,Intent intent) {
		PluginWrapper.onNewIntent(intent);
		
	}
	//AnySDKListener.onCallBack
	@Override
	public void onCallBack(int code, String msg) {
		
		  switch(code)
	        {
	        case UserWrapper.ACTION_RET_INIT_SUCCESS://初始化SDK成功回调
	        	_anysdk_init_completed = true;
	            break;
	        case UserWrapper.ACTION_RET_INIT_FAIL://初始化SDK失败回调
	            break;
	        case UserWrapper.ACTION_RET_LOGIN_SUCCESS://登陆成功回调
	        {
	        	if(null!=_loginCallback){
	        		IAuthenticationCallback loginCallback = _loginCallback;
	        		_loginCallback = null;
	        		if(AnySDKUser.getInstance().isLogined()){
	        			String account = AnySDKUser.getInstance().getUserID();
	        			loginCallback.OnAuthenticationSuccess(this, account, account, msg);
	        		}
	        	}
	        }
	            break;
	        case UserWrapper.ACTION_RET_LOGIN_TIMEOUT://登陆超时回调
	        case UserWrapper.ACTION_RET_LOGIN_NO_NEED://无需登陆回调
	        case UserWrapper.ACTION_RET_LOGIN_CANCEL://登陆取消回调
	        case UserWrapper.ACTION_RET_LOGIN_FAIL://登陆失败回调
	        	if(null!=_loginCallback){
	        		IAuthenticationCallback loginCallback = _loginCallback;
	        		_loginCallback = null;
	        		loginCallback.OnAuthenticationFailed(this,msg);
	        	}
	            break;
	        case UserWrapper.ACTION_RET_LOGOUT_SUCCESS://登出成功回调
	            break;
	        case UserWrapper.ACTION_RET_LOGOUT_FAIL://登出失败回调
	            break;
	        case UserWrapper.ACTION_RET_PLATFORM_ENTER://平台中心进入回调
	            break;
	        case UserWrapper.ACTION_RET_PLATFORM_BACK://平台中心退出回调
	            break;
	        case UserWrapper.ACTION_RET_PAUSE_PAGE://暂停界面回调
	            break;
	        case UserWrapper.ACTION_RET_EXIT_PAGE://退出游戏回调
	            break;
	        case UserWrapper.ACTION_RET_ANTIADDICTIONQUERY://防沉迷查询回调
	            break;
	        case UserWrapper.ACTION_RET_REALNAMEREGISTER://实名注册回调
	            break;
	        case UserWrapper.ACTION_RET_ACCOUNTSWITCH_SUCCESS://切换账号成功回调
	            break;
	        case UserWrapper.ACTION_RET_ACCOUNTSWITCH_FAIL://切换账号失败回调
	            break;
	        case UserWrapper.ACTION_RET_OPENSHOP://应用汇特有回调，接受到该回调调出游戏商店界面
	            break;
	        default:
	            break;
	        }
		
	}

}
