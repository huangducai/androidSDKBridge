package com.gamefps.ymTalk;


import com.gamefps.sdkbridge.IActivityOverrider;
import com.gamefps.sdkbridge.ISDKWarpper;
import com.gamefps.sdkbridge.SDKBridge;
import com.gamefps.sdkbridge.SdkConfigInfo;
import com.gamefps.sdkbridge.SdkProviderInfo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import  com.youme.voiceengine.mgr.YouMeManager;
import  com.youme.voiceengine.*;

public class YMTalk implements ISDKWarpper, IActivityOverrider {
	private final static SdkProviderInfo SDK_PROVIDER_INFO = new SdkProviderInfo("YMTALK", "YMTALK", 1, true);

	@Override
	public void onCreate(Activity act, Bundle savedInstanceState) {
		// TODO �Զ����ɵķ������
		YouMeManager.Init(act);
		//�������������������ı仯
        Intent intent = new Intent(act,VoiceEngineService.class);
        act.startService(intent);
	}

	@Override
	public void onDestroy(Activity act) {
		// TODO �Զ����ɵķ������
		
	}

	@Override
	public void onPause(Activity act) {
		// TODO �Զ����ɵķ������
		
	}

	@Override
	public void onResume(Activity act) {
		// TODO �Զ����ɵķ������
		
	}

	@Override
	public void onStop(Activity act) {
		// TODO �Զ����ɵķ������
		
	}

	@Override
	public void onRestart(Activity act) {
		// TODO �Զ����ɵķ������
		
	}

	@Override
	public void onActivityResult(Activity act, int requestCode, int resultCode, Intent data) {
		// TODO �Զ����ɵķ������
		
	}

	@Override
	public void onNewIntent(Activity act, Intent intent) {
		// TODO �Զ����ɵķ������
		
	}

	@Override
	public void onStart(Activity act) {
		// TODO �Զ����ɵķ������
		
	}

	@Override
	public void onSaveInstanceState(Activity act, Bundle outState) {
		// TODO �Զ����ɵķ������
		
	}

	@Override
	public void onConfigurationChanged(Activity act, Configuration newConfig) {
		// TODO �Զ����ɵķ������
		
	}

	@Override
	public boolean init(Activity ctx, SdkConfigInfo cfg) {
		// TODO �Զ����ɵķ������
		return true;
	}

	@Override
	public void confirmExit(Activity ctx, SDKBridge callback) {
		// TODO �Զ����ɵķ������
		
	}

	@Override
	public void onGameCheckVersionBegin() {
		// TODO �Զ����ɵķ������
		
	}

	@Override
	public void onGameCheckVersionEnd() {
		// TODO �Զ����ɵķ������
		
	}

	@Override
	public void onEnterLoginView(SDKBridge callback) {
		// TODO �Զ����ɵķ������
		
	}

	@Override
	public SdkProviderInfo getProviderInfo() {
		// TODO �Զ����ɵķ������
		return SDK_PROVIDER_INFO;
	}
	

}
