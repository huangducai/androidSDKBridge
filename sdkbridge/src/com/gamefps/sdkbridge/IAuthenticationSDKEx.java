package com.gamefps.sdkbridge;

public interface IAuthenticationSDKEx extends IAuthenticationSDK {
	void AccountBind(String accountType,IAccountBindCallback callback);
	void QueryAccountBindings(IQueryAccountBindingsCallback callback);
}
