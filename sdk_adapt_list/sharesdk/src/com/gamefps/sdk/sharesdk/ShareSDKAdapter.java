/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 mob.com. All rights reserved.
 */

package com.gamefps.sdk.sharesdk;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;

import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.OnekeyShareTheme;
import cn.sharesdk.wechat.utils.WXAppExtendObject;
import cn.sharesdk.wechat.utils.WXMediaMessage;
import cn.sharesdk.wechat.utils.WechatHandlerActivity;

import com.gamefps.sdkbridge.IAccountBindCallback;
import com.gamefps.sdkbridge.IActivityOverrider;
import com.gamefps.sdkbridge.IAnalyticsSDK;
import com.gamefps.sdkbridge.IAuthenticationSDK;
import com.gamefps.sdkbridge.IAuthenticationSDKEx;
import com.gamefps.sdkbridge.IPaymentCallback;
import com.gamefps.sdkbridge.IPaymentSDK;
import com.gamefps.sdkbridge.IQueryAccountBindingsCallback;
import com.gamefps.sdkbridge.ISDKWarpper;
import com.gamefps.sdkbridge.IShareCallback;
import com.gamefps.sdkbridge.ISnsShareSDK;
import com.gamefps.sdkbridge.LoginAccountInfo;
import com.gamefps.sdkbridge.PaymentInfo;
import com.gamefps.sdkbridge.SDKBridge;
import com.gamefps.sdkbridge.SdkConfigInfo;
import com.gamefps.sdkbridge.SdkProviderInfo;
import com.gamefps.sdkbridge.SnsShareContent;

/** 微信客户端回调activity示例 */
public class ShareSDKAdapter extends WechatHandlerActivity implements ISDKWarpper, ISnsShareSDK {
	private final static SdkProviderInfo SDK_PROVIDER_INFO = new SdkProviderInfo("SHARESDK", "share sdk adapter", 1,false);
	public final static int S_OK = 0;
	public final static int E_Failed = -1;
	/**
	 * 处理微信发出的向第三方应用请求app message
	 * <p>
	 * 在微信客户端中的聊天页面有“添加工具”，可以将本应用的图标添加到其中
	 * 此后点击图标，下面的代码会被执行。Demo仅仅只是打开自己而已，但你可
	 * 做点其他的事情，包括根本不打开任何页面
	 */
	public void onGetMessageFromWXReq(WXMediaMessage msg) {
		if (msg != null) {
			Intent iLaunchMyself = getPackageManager().getLaunchIntentForPackage(getPackageName());
			startActivity(iLaunchMyself);
		}
	}

	/**
	 * 处理微信向第三方应用发起的消息
	 * <p>
	 * 此处用来接收从微信发送过来的消息，比方说本demo在wechatpage里面分享
	 * 应用时可以不分享应用文件，而分享一段应用的自定义信息。接受方的微信
	 * 客户端会通过这个方法，将这个信息发送回接收方手机上的本demo中，当作
	 * 回调。
	 * <p>
	 * 本Demo只是将信息展示出来，但你可做点其他的事情，而不仅仅只是Toast
	 */
	public void onShowMessageFromWXReq(WXMediaMessage msg) {
		if (msg != null && msg.mediaObject != null
				&& (msg.mediaObject instanceof WXAppExtendObject)) {
			WXAppExtendObject obj = (WXAppExtendObject) msg.mediaObject;
			Toast.makeText(this, obj.extInfo, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean init(Activity act, SdkConfigInfo cfg) {
		ShareSDK.initSDK(act);
		return true;
	}
	
	@Override
	public SdkProviderInfo getProviderInfo() {
		return SDK_PROVIDER_INFO;
	}
	
	@Override
	public void share(Activity act, SnsShareContent shareContent,final IShareCallback callback) {
		OnekeyShare oks = new OnekeyShare();
		
		oks.setTheme(OnekeyShareTheme.CLASSIC);
		//关闭sso授权
		oks.disableSSOWhenAuthorize(); 

		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间等使用
		oks.setTitle(shareContent.title);
		oks.setImagePath(shareContent.image);
		// text是分享文本，所有平台都需要这个字段
		oks.setText(shareContent.description);

		// url仅在微信（包括好友和朋友圈）中使用
		oks.setUrl(shareContent.url);

		//oks.setVenueName("ShareSDK");
		//oks.setVenueDescription("This is a beautiful place!");
		// 将快捷分享的操作结果将通过OneKeyShareCallback回调
		oks.setCallback(new PlatformActionListener() {
			@Override
			public void onComplete(Platform platform, int action, java.util.HashMap<java.lang.String,java.lang.Object> res){
				callback.OnShareResult(S_OK);
			}
			@Override
			public void onCancel(Platform platform, int action){
				callback.OnShareResult(E_Failed);
			}
			@Override
			public void onError(Platform platform, int action, java.lang.Throwable t){
				callback.OnShareResult(E_Failed);
			}
		});

		// 启动分享GUI
		oks.show(act);
	}
	
	@Override
	public void confirmExit(Activity ctx, final SDKBridge callback) {
	}
	
	@Override
	public void onGameCheckVersionBegin() {
	}

	@Override
	public void onGameCheckVersionEnd() {
	}
	
	@Override
	public void onEnterLoginView(final SDKBridge callback) {
	}
}
