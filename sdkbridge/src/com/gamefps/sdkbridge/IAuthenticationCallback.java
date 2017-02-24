package com.gamefps.sdkbridge;

public interface IAuthenticationCallback {
	void OnAuthenticationSuccess( IAuthenticationSDK provider,String accountId,String userName,String token );
	void OnAuthenticationFailed( IAuthenticationSDK provider,String Message );
}
