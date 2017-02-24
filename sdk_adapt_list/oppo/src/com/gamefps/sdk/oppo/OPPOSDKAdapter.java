package com.gamefps.sdk.oppo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import com.gamefps.sdkbridge.IActivityOverrider;
import com.gamefps.sdkbridge.IAnalyticsSDK;
import com.gamefps.sdkbridge.IAuthenticationSDK;
import com.gamefps.sdkbridge.IPaymentCallback;
import com.gamefps.sdkbridge.IPaymentSDK;
import com.gamefps.sdkbridge.ISDKWarpper;
import com.gamefps.sdkbridge.LoginAccountInfo;
import com.gamefps.sdkbridge.PaymentInfo;
import com.gamefps.sdkbridge.SDKBridge;
import com.gamefps.sdkbridge.SdkConfigInfo;
import com.gamefps.sdkbridge.SdkProviderInfo;
import com.nearme.game.sdk.GameCenterSDK;
import com.nearme.game.sdk.callback.ApiCallback;
import com.nearme.game.sdk.callback.GameExitCallback;
import com.nearme.game.sdk.common.model.biz.PayInfo;
import com.nearme.game.sdk.common.model.biz.ReqUserInfoParam;
import com.nearme.game.sdk.common.util.AppUtil;
import com.nearme.platform.opensdk.pay.PayResponse;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.AvoidXfermode.Mode;
import android.os.Bundle;
import android.print.PrintManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;


public class OPPOSDKAdapter implements  IAuthenticationSDK, ISDKWarpper,IActivityOverrider,IAnalyticsSDK, IPaymentSDK {
	
	private final static SdkProviderInfo SDK_PROVIDER_INFO = new SdkProviderInfo("OPPO", "oppo sdk adapter",1,true);
	public final static String TAG = "OPPOSDKAdapter";
	private Activity _activity;
	private String PlayerAccount = "";
	
	@Override
	public boolean init(Activity act, SdkConfigInfo cfg) {
		// TODO Auto-generated method stub
		
		//初始化经分
		//SDK.getInstance().init(act, "467787787756", "40c2a4d0027b77f298d4dc0a5725ae46",cfg.channelId, cn.uc.dp.sdk.Constants.Mode.PRODUCTION);
		
		//初始化OPPO SDK
		_activity = act;
		String appSecret = "c170Ab7c97e09D4213E3671036d9477c";
		GameCenterSDK.init(appSecret, act);
		
		return true;
	}
	
	@Override
	public void onCreate(Activity act, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
	}
	
	////////--------登录登录登录登录登录登录登录登录登录--------////////
	@Override
	public void Login(final Activity act, final SDKBridge cb) {
		// TODO Auto-generated method stub
		GameCenterSDK.getInstance().doLogin(act, new ApiCallback() {
			
			@Override
			public void onSuccess(String resultMsg) {
				// TODO Auto-generated method stub
				Toast.makeText(act, resultMsg, Toast.LENGTH_LONG).show();
				doGetTokenAndSsoid(cb);
			}
			
			@Override
			public void onFailure(String resultMsg, int resultCode) {
				// TODO Auto-generated method stub
				Toast.makeText(act, resultMsg, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	////////--------获取token和ssoid--------////////
	public void doGetTokenAndSsoid(final SDKBridge cb){
		GameCenterSDK.getInstance().doGetTokenAndSsoid(new ApiCallback() {
			
			@Override
			public void onSuccess(String resultMsg) {
				// TODO Auto-generated method stub
				try {
					JSONObject json = new JSONObject(resultMsg);
					String token = json.getString("token");
					String ssoid = json.getString("ssoid");
					//Toast.makeText(_activity,"token = " + token + "ssoid = " + ssoid,Toast.LENGTH_LONG).show();
					//doGetUserInfoByCpClient(token, ssoid);
					getUserInfoFromSid(token,ssoid,cb);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(String resultMsg, int resultCode) {
				// TODO Auto-generated method stub
				Toast.makeText(_activity, "获取token和ssoid失败:"+resultCode,
						Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	////////--------PHP服务器验证登录--------////////
	private void getUserInfoFromSid(final String token,final String ssoid,final SDKBridge cb){
		final IAuthenticationSDK pThis = this;
		try {
			String _token = URLEncoder.encode(token,"utf-8");
			String _ssoid = URLEncoder.encode(ssoid,"utf-8");
			//String url = "https://gw.uc.cnc.173fun.com/cnc/cncweb/api/sdklogin.php?cncsdk=oppo&cnclang=zh-cn&ssoid=" + _ssoid +"&token=" + _token;
			String url = "http://221.236.172.156:23580/cnc/cncweb/api/sdklogin.php?cncsdk=oppo&cnclang=zh-cn&ssoid=" + _ssoid +"&token=" + _token;
			//String url = "http://192.168.1.234:8080/cnc-web/cncweb/api/sdklogin.php?cncsdk=oppo&cnclang=zh-cn&ssoid=" + _ssoid +"&token=" + _token;
			Log.d(TAG,url);
			JsonHttp.GetJsonFromUrlAsync(url, new CallbackListener() {
				@Override
				public void callback(int code, JSONObject val) {
					if (0 == code){
						try {
							int errCode = val.getInt("errCode");
							if(0==errCode){
								LoginAccountInfo ai = new LoginAccountInfo();
								JSONObject userInfo = val.getJSONObject("retVal");
								ai.channelPrefix = userInfo.getString("channelPrefix");
								ai.channelAccountId = userInfo.getString("channelAccountId");
								ai.accountId = userInfo.getString("accountId");
								PlayerAccount = ai.accountId; //记录玩家账号
								ai.loginTime =   userInfo.getString("time");
								ai.isNewAccount = userInfo.getBoolean("isNewAccount");
								ai.token = userInfo.getString("token");
								cb.OnAuthenticationSuccess(pThis, ai);
							}else{
								//String errMsg
								
								cb.OnAuthenticationFailed(pThis, val.getString("errMsg"));
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}else{
						cb.OnAuthenticationFailed(pThis, "verify sid server failed");
					}
				}
				
			});
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	////////--------充值--------////////
	@Override
	public void pay(final Activity act,final PaymentInfo paymentInfo,final IPaymentCallback callback) {
		// TODO Auto-generated method stub
		TelephonyManager tm = (TelephonyManager) _activity.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        String product_id = getMD5("OP_"+paymentInfo.productId);
		String autoInfo = PlayerAccount + "|" + product_id + "|" + paymentInfo.serverId + "|" + paymentInfo.serverName + "|" + paymentInfo.characterName + "|" + deviceId + "|" + "android" + "|" + "OPPO";
		
		PayInfo payInfo = new PayInfo(System.currentTimeMillis() + new Random().nextInt(1000) + "", autoInfo, (int)paymentInfo.amount);
		payInfo.setProductDesc(paymentInfo.productDesc);
		payInfo.setProductName(paymentInfo.productName);
		//payInfo.setCallbackUrl("http://ps.uc.cnc.173fun.com/cnc/cncweb/channels/oppo/api/paycallback.php");
		payInfo.setCallbackUrl("http://221.236.172.156:23580/cnc/cncweb/channels/op/api/paycallback.php");
		GameCenterSDK.getInstance().doPay(act, payInfo, new ApiCallback() {
			
			@Override
			public void onSuccess(String resultMsg) {
				// TODO Auto-generated method stub
				Toast.makeText(act, resultMsg, Toast.LENGTH_SHORT)
				.show();
				callback.OnPaymentResult(0,resultMsg);
			}
			
			@Override
			public void onFailure(String resultMsg, int resultCode) {
				// TODO Auto-generated method stub
				if (PayResponse.CODE_CANCEL != resultCode) {
					Toast.makeText(act, resultMsg,Toast.LENGTH_SHORT).show();
					callback.OnPaymentResult(resultCode,resultMsg);
				} else {
					// 取消支付处理
					Toast.makeText(act, resultMsg,Toast.LENGTH_SHORT).show();
					callback.OnPaymentResult(resultCode,resultMsg);
				}
			}
		});
	}
	
	public String getMD5(String info)
	{
		try
		{
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(info.getBytes("UTF-8"));
			byte[] encryption = md5.digest();
				
			StringBuffer strBuf = new StringBuffer();
			for (int i = 0; i < encryption.length; i++)
			{
				if (Integer.toHexString(0xff & encryption[i]).length() == 1)
				{
					strBuf.append("0").append(Integer.toHexString(0xff & encryption[i]));
				}
				else
				{
					strBuf.append(Integer.toHexString(0xff & encryption[i]));
				}
			}
			return strBuf.toString();
		}
		catch (NoSuchAlgorithmException e)
		{
			return "";
		}
		catch (UnsupportedEncodingException e)
		{
			return "";
		}
	}
	
	@Override
	public SdkProviderInfo getProviderInfo() {
		// TODO Auto-generated method stub
		return SDK_PROVIDER_INFO;
	}
	
	////////--------退出--------////////
	@Override
	public void confirmExit(final Activity ctx, SDKBridge callback) {
		// TODO Auto-generated method stub
		GameCenterSDK.getInstance().onExit(ctx, new GameExitCallback() {
			
			@Override
			public void exitGame() {
				// TODO Auto-generated method stub
				AppUtil.exitGameProcess(ctx);
			}
		});
	}
	
	@Override
	public void onResume(Activity act) {
		// TODO Auto-generated method stub
		GameCenterSDK.getInstance().onResume(act);
	}
	@Override
	public void onPause(Activity act) {
		// TODO Auto-generated method stub
		GameCenterSDK.getInstance().onPause();
	}
	@Override
	public void onLoginAccount(String account, Map<String, String> accountData) {
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
	public void onCreateRole(String serverId, String roleName, String roleId, int roleCreateTime) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onEnterGame(String serverId, String roleName) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onQuestUpdate(String questType, String questId, QuestState state, String comment) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onCurrencyGain(String currencyType, String reason, int amount) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onCurrencyConsume(String currencyType, String reason, int amount) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void logEvent(String event, Map<String, String> eventData) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onDestroy(final Activity act) {
		// TODO Auto-generated method stub
	}
	@Override
	public void onStop(Activity act) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onRestart(Activity act) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onActivityResult(Activity act, int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onNewIntent(Activity act, Intent intent) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStart(Activity act) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSaveInstanceState(Activity act, Bundle outState) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onConfigurationChanged(Activity act, Configuration newConfig) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void logout() {
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
}
