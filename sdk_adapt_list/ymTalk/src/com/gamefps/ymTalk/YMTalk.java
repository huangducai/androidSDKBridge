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
		// TODO 自动生成的方法存根
		YouMeManager.Init(act);
		//启动服务用来监控网络的变化
        Intent intent = new Intent(act,VoiceEngineService.class);
        act.startService(intent);
	}

	@Override
	public void onDestroy(Activity act) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void onPause(Activity act) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void onResume(Activity act) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void onStop(Activity act) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void onRestart(Activity act) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void onActivityResult(Activity act, int requestCode, int resultCode, Intent data) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void onNewIntent(Activity act, Intent intent) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void onStart(Activity act) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void onSaveInstanceState(Activity act, Bundle outState) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void onConfigurationChanged(Activity act, Configuration newConfig) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public boolean init(Activity ctx, SdkConfigInfo cfg) {
		// TODO 自动生成的方法存根
		return true;
	}

	@Override
	public void confirmExit(Activity ctx, SDKBridge callback) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void onGameCheckVersionBegin() {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void onGameCheckVersionEnd() {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void onEnterLoginView(SDKBridge callback) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public SdkProviderInfo getProviderInfo() {
		// TODO 自动生成的方法存根
		return SDK_PROVIDER_INFO;
	}
	

}
