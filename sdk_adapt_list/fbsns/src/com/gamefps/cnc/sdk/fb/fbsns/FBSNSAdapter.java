package com.gamefps.cnc.sdk.fb.fbsns;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.share.Sharer;
import com.facebook.share.Sharer.Result;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.AppInviteDialog;
import com.facebook.share.widget.ShareDialog;
import com.gamefps.sdkbridge.IAppInviteCallback;
import com.gamefps.sdkbridge.ISDKWarpper;
import com.gamefps.sdkbridge.IShareCallback;
import com.gamefps.sdkbridge.ISnsAppInviteSDK;
import com.gamefps.sdkbridge.ISnsShareSDK;
import com.gamefps.sdkbridge.SDKBridge;
import com.gamefps.sdkbridge.SdkConfigInfo;
import com.gamefps.sdkbridge.SdkProviderInfo;
import com.gamefps.sdkbridge.SnsShareContent;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

public class FBSNSAdapter implements ISDKWarpper ,ISnsShareSDK ,ISnsAppInviteSDK{
	Activity _appActivity;
	final static SdkProviderInfo PROVIDER_INFO = new SdkProviderInfo("FBSNS", "facebook sns adapter", 0, false);
	final static String LOG_TAG = "FBSNSAdapter";
	
	
	
	CallbackManager callbackManager;
	
	
	@Override
	public boolean init(Activity ctx, SdkConfigInfo cfg) {
		_appActivity = ctx;
		
		FacebookSdk.sdkInitialize(ctx.getApplicationContext());
	    AppEventsLogger.activateApp(ctx.getApplication());
	    
	    
	    
	    
		return true;
	}

	@Override
	public void confirmExit(Activity ctx, SDKBridge callback) {
		// TODO Auto-generated method stub
		
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

	@Override
	public SdkProviderInfo getProviderInfo() {
		return PROVIDER_INFO;
	}

	@Override
	public void share(Activity act, SnsShareContent shareContent,final IShareCallback callback) {
		
		//shareContent.url = "https://fb.me/209300636162071";
		
		
		//shareContent.url = "http://www.facebook.com/MillenniumConquest";
		
		//shareContent.url = "http://recharge.mc.gtarcade.com/cnc/cncfwd/fw/applink.php?A=share";
		
		//shareContent.image = "http://recharge.mc.gtarcade.com/cnc/cncfwd/fw/img/chengka.jpg";
		
		ShareLinkContent linkContent = new ShareLinkContent.Builder()
		        .setContentUrl(Uri.parse(shareContent.url))
		        .setContentTitle(shareContent.title)
		        .setContentDescription(shareContent.description)
		        .setImageUrl(Uri.parse(shareContent.image))
		        .build();
		
		
		if(null == callbackManager)
			callbackManager = CallbackManager.Factory.create();
	

		final ShareDialog shareDialog = new ShareDialog(_appActivity);
		if(shareDialog.canShow(linkContent)){
			shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
				@Override
				public void onSuccess(Sharer.Result arg0) {
					callback.OnShareResult(SDKBridge.S_OK);
				}
				
				@Override
				public void onError(FacebookException arg0) {
					Log.e(LOG_TAG, arg0.getMessage());
					callback.OnShareResult(SDKBridge.E_Failed);
				}
				
				@Override
				public void onCancel() {
					callback.OnShareResult(SDKBridge.S_Canceled);
				}
			});
			
			shareDialog.show(linkContent);
		}else{
			callback.OnShareResult(SDKBridge.E_Failed);
		}
	}

	@Override
	public void appInvite(String topic,final IAppInviteCallback cb) {
		
		String appLinkUrl = "http://recharge.mc.gtarcade.com/cnc/cncfwd/fw/applink.php?A=" + topic;
		appLinkUrl = "https://fb.me/209300636162071";

		    AppInviteContent content = new AppInviteContent.Builder()
		                .setApplinkUrl(appLinkUrl)
		                .build();
		    
		    
			if(null == callbackManager)
				callbackManager = CallbackManager.Factory.create();
		    
			final AppInviteDialog appInviteDialog = new AppInviteDialog(_appActivity);
			if (appInviteDialog.canShow(content)) {
			appInviteDialog.registerCallback(callbackManager, new FacebookCallback<AppInviteDialog.Result>() {

				@Override
				public void onCancel() {
					cb.OnAppInviteResult(SDKBridge.S_Canceled);
				}

				@Override
				public void onError(FacebookException arg0) {
					Log.e(LOG_TAG, arg0.getMessage());
					cb.OnAppInviteResult(SDKBridge.E_Failed);
				}

				@Override
				public void onSuccess(com.facebook.share.widget.AppInviteDialog.Result arg0) {
					cb.OnAppInviteResult(SDKBridge.S_OK);
				}
			});
			
			appInviteDialog.show(content);
		}else{
			cb.OnAppInviteResult(SDKBridge.E_Failed);
		}
		
		
		
	}
	
	

}
