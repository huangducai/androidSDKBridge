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
	        case UserWrapper.ACTION_RET_INIT_SUCCESS://��ʼ��SDK�ɹ��ص�
	        	_anysdk_init_completed = true;
	            break;
	        case UserWrapper.ACTION_RET_INIT_FAIL://��ʼ��SDKʧ�ܻص�
	            break;
	        case UserWrapper.ACTION_RET_LOGIN_SUCCESS://��½�ɹ��ص�
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
	        case UserWrapper.ACTION_RET_LOGIN_TIMEOUT://��½��ʱ�ص�
	        case UserWrapper.ACTION_RET_LOGIN_NO_NEED://�����½�ص�
	        case UserWrapper.ACTION_RET_LOGIN_CANCEL://��½ȡ���ص�
	        case UserWrapper.ACTION_RET_LOGIN_FAIL://��½ʧ�ܻص�
	        	if(null!=_loginCallback){
	        		IAuthenticationCallback loginCallback = _loginCallback;
	        		_loginCallback = null;
	        		loginCallback.OnAuthenticationFailed(this,msg);
	        	}
	            break;
	        case UserWrapper.ACTION_RET_LOGOUT_SUCCESS://�ǳ��ɹ��ص�
	            break;
	        case UserWrapper.ACTION_RET_LOGOUT_FAIL://�ǳ�ʧ�ܻص�
	            break;
	        case UserWrapper.ACTION_RET_PLATFORM_ENTER://ƽ̨���Ľ���ص�
	            break;
	        case UserWrapper.ACTION_RET_PLATFORM_BACK://ƽ̨�����˳��ص�
	            break;
	        case UserWrapper.ACTION_RET_PAUSE_PAGE://��ͣ����ص�
	            break;
	        case UserWrapper.ACTION_RET_EXIT_PAGE://�˳���Ϸ�ص�
	            break;
	        case UserWrapper.ACTION_RET_ANTIADDICTIONQUERY://�����Բ�ѯ�ص�
	            break;
	        case UserWrapper.ACTION_RET_REALNAMEREGISTER://ʵ��ע��ص�
	            break;
	        case UserWrapper.ACTION_RET_ACCOUNTSWITCH_SUCCESS://�л��˺ųɹ��ص�
	            break;
	        case UserWrapper.ACTION_RET_ACCOUNTSWITCH_FAIL://�л��˺�ʧ�ܻص�
	            break;
	        case UserWrapper.ACTION_RET_OPENSHOP://Ӧ�û����лص������ܵ��ûص�������Ϸ�̵����
	            break;
	        default:
	            break;
	        }
		
	}

}
