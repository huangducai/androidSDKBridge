package com.gamefps.sdkbridge;

import android.app.Activity;

public interface IAuthenticationSDK {
//	String getAccountPrefix();
	void Login(Activity act,SDKBridge cb);
	void logout();
}
