package com.gamefps.sdkbridge;

import android.app.Activity;

/**
 * Created by lvyou on 2016/1/3.
 */
public interface ISDKWarpper {
	boolean init(Activity ctx,SdkConfigInfo cfg);
	
	void confirmExit(Activity ctx,SDKBridge callback);
	
	void onGameCheckVersionBegin();
	void onGameCheckVersionEnd();
	void onEnterLoginView(SDKBridge callback);
	
	SdkProviderInfo getProviderInfo();
}
