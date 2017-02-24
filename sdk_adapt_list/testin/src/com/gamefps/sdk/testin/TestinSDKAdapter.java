package com.gamefps.sdk.testin;

import com.gamefps.sdkbridge.ISDKWarpper;
import com.gamefps.sdkbridge.SDKBridge;
import com.gamefps.sdkbridge.SdkConfigInfo;
import com.gamefps.sdkbridge.SdkProviderInfo;
import com.testin.agent.TestinAgent;

import android.app.Activity;

public class TestinSDKAdapter implements ISDKWarpper{
	private final SdkProviderInfo _providerInfo = new SdkProviderInfo("TESTIN","Testin_CrashReportAgent",0,false); 
	@Override
	public boolean init(Activity ctx,SdkConfigInfo cfg) {
		TestinAgent.init(ctx, "e313cbfb5cf48349b0f1ca94d9343cf8",cfg.channelId);
		return true;
	}
	@Override
	public void confirmExit(Activity ctx,SDKBridge callback) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("TestinSDKAdapter plugin not support exit confirm UI!");
	}
	@Override
	public SdkProviderInfo getProviderInfo() {
		// TODO Auto-generated method stub
		return _providerInfo;
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

}
