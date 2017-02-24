package com.gamefps.sdk.uc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import cn.uc.gamesdk.UCGameSdk;
import cn.uc.gamesdk.exception.UCCallbackListenerNullException;
import cn.uc.gamesdk.exception.UCMissActivityException;
import cn.uc.gamesdk.open.GameParamInfo;
import cn.uc.gamesdk.open.OrderInfo;
import cn.uc.gamesdk.open.UCCallbackListener;
import cn.uc.gamesdk.open.UCGameSdkStatusCode;
import cn.uc.gamesdk.open.UCLogLevel;
import cn.uc.gamesdk.open.UCOrientation;



public class UCSDKAdapter implements  IAuthenticationSDK, ISDKWarpper,IActivityOverrider,IAnalyticsSDK, IPaymentSDK {
	
	private final static SdkProviderInfo SDK_PROVIDER_INFO = new SdkProviderInfo("UC", "uc sdk adapter",1,true);
	public final static String TAG = "UCSDKAdapter";
	private boolean _sdk_init_completed = false; 
	private String _serverId = "";
	private String _roleName = "";
	//private int _roleLevel = -1;
	private boolean _toolbar = false;
	private Activity _activity;
	Activity appContext = null;
	
	//是否是测试环境
	private boolean isDebug = true;
	
	@Override
	public void logout() {
	}
	
	@Override
	public void onGameCheckVersionBegin() {
	}

	@Override
	public void onGameCheckVersionEnd() {
	}

	@Override
	public void onEnterLoginView(SDKBridge callback) {
		callback.enterLoginCallback("UC", "");
	}
	
	@Override
	public boolean init(Activity act, SdkConfigInfo cfg) {
		_activity = act;
		//SDK.getInstance().init(act, "467787787756", "40c2a4d0027b77f298d4dc0a5725ae46",cfg.channelId, Mode.PRODUCTION);

		initUCSDK();
		return true;
	}

	public void initUCSDK()
	{
		GameParamInfo gpi = new GameParamInfo();
		gpi.setCpId(0);
		if(isDebug){
			gpi.setGameId(729689);
		}else{
			gpi.setGameId(729514);
		}
		
		gpi.setEnablePayHistory(false);
		gpi.setEnableUserChange(false);
		gpi.setOrientation(UCOrientation.LANDSCAPE);
		
		try {
			UCGameSdk.defaultSdk().initSdk(_activity,UCLogLevel.ERROR, false, gpi, 
						new UCCallbackListener<String>() {
							@Override
							public void callback(int code, String msg) {
								if(UCGameSdkStatusCode.SUCCESS == code)
								{
									_sdk_init_completed = true;
								}else{
									Log.e(TAG, "uc sdk init failed:" + msg);
//									Map<String, Object> attr = new HashMap<String, Object>();
//									attr.put("errCode", code  );
//									attr.put("errMsg", msg);
//									SDK.getInstance().getAppEventHelper().appGeneral("UC_SDK_INIT_FAILED", attr);
									new android.os.Handler().postDelayed(
										new Runnable() {
											public void run() {
												Log.w(TAG, "uc sdk reinit");
												initUCSDK();
											}
										}, 
									1000);
								}
							}
						}
					);
		} catch (UCCallbackListenerNullException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UCMissActivityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}




	@Override
	public SdkProviderInfo getProviderInfo() {
		// TODO Auto-generated method stub
		return SDK_PROVIDER_INFO;
	}


	@Override
	public void Login(Activity act,final SDKBridge cb) {
		final IAuthenticationSDK pThis = this;
		if(!_sdk_init_completed){
			Log.e(TAG, "call login before sdk init");
			cb.OnAuthenticationFailed(this, "sdk not init");
			return;
		}
		//_loginCallback = cb;
		
		
		try {
			Log.d(TAG, "uc sdk login begin");

			UCGameSdk.defaultSdk().login(new UCCallbackListener<String>() {
						boolean succeeded = false;
				
						@Override
						public void callback(int code, String msg) {
							Log.d(TAG, "uc sdk login callback with code" + code);
							if(UCGameSdkStatusCode.SUCCESS == code){
								succeeded = true;
							}else if(UCGameSdkStatusCode.LOGIN_EXIT == code){
								if(succeeded){
									String sid = UCGameSdk.defaultSdk().getSid();
									if(null==sid || sid.isEmpty()){
										//cb.OnAuthenticationSuccess(provider, accountId, userName, token);
										cb.OnAuthenticationFailed(pThis, msg);
									}else{
										getUserInfoFromSid(sid,cb);
									}
								}else{
									cb.OnAuthenticationFailed(pThis, msg);	
								}
								
							}
						}
					}
				);
		} catch (UCCallbackListenerNullException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void getUserInfoFromSid(final String sid,final SDKBridge cb){
		final IAuthenticationSDK pThis = this;
		try {
			String esid = URLEncoder.encode(sid,"utf-8");
			String url;
			if(isDebug){
				url = "http://221.236.172.156:23580/cnc/cncweb/api/sdklogin.php?cncsdk=uc&cnclang=zh-cn&sid=" + esid;	
			}else{
				url = "https://gw.uc.cnc.173fun.com/cnc/cncweb/api/sdklogin.php?cncsdk=uc&cnclang=zh-cn&sid=" + esid;	
			}

			JsonHttp.GetJsonFromUrlAsync(url, new UCCallbackListener<JSONObject>() {

				@Override
				public void callback(int code, JSONObject val) {
					if (UCGameSdkStatusCode.SUCCESS == code){
						try {
							int errCode = val.getInt("errCode");
							if(0==errCode){
								LoginAccountInfo ai = new LoginAccountInfo();
								JSONObject userInfo = val.getJSONObject("retVal");
								ai.channelPrefix = userInfo.getString("channelPrefix");
								ai.channelAccountId = userInfo.getString("channelAccountId");
								ai.accountId = userInfo.getString("accountId");
								ai.loginTime =   userInfo.getString("time");
								ai.isNewAccount = userInfo.getBoolean("isNewAccount");
								ai.token = userInfo.getString("token");
								//"UC|{\n  \"accountId\" : \"UC_JY_efeabac51612241ce953043b63cfeab7\",\n  \"deviceId\" : \"EDF98A6F-72B1-4814-AF36-718305DEC89E\",\n  \"os\" : \"ios\",\n  \"ch\" : \"UC\",\n  \"userType\" : \"com.peijie.xingji\"\n}"
								//todo 增加extra 信息
								TelephonyManager tm = (TelephonyManager) _activity.getSystemService(Context.TELEPHONY_SERVICE);
						        String deviceId = tm.getDeviceId();
								JSONObject extraJson = new JSONObject();
								extraJson.put("deviceId", deviceId);
								extraJson.put("userType", appContext.getPackageName());
								extraJson.put("accountId", userInfo.getString("channelAccountId"));
								extraJson.put("os", "android");
								extraJson.put("ch", "UC");
								
								ai.extra = extraJson.toString();
								ai.extra = "UC|" + ai.extra ;
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
	
	


	@Override
	public void setRoleInfo(Map<String, String> roleData) {
		JSONObject obj = new JSONObject(roleData);
		try {
			obj.put("roleId", roleData.get("entityId"));
			obj.put("roleName", roleData.get("Name"));
			obj.put("roleLevel", Integer.parseInt(roleData.get("Level")));
			obj.put("zoneId", _serverId);
			obj.put("zoneName", "S" + _serverId.toString());
			obj.put("roleCTime", -1);
			obj.put("roleLevelMTime", -1);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		UCGameSdk.defaultSdk().submitExtendData("loginGameRole", obj);
//		_roleName = roleData.get("Name");
//		int level = Integer.parseInt(roleData.get("Level"));
//		if(_roleLevel>0 && level>_roleLevel){
//			SDK.getInstance().getGameEventHelper().levelUpTo(_serverId, _roleName, level);
//		}
//		_roleLevel = level;
	}

	@Override
	public void onCreateAccount(String account, Map<String, String> accountData) {
		//SDK.getInstance().getGameEventHelper().createUser("UC", account);
		
	}

	@Override
	public void onLoginAccount(String account, Map<String, String> accountData) {
		//SDK.getInstance().getGameEventHelper().onlineUser("UC", account);
	}
	@Override
	public void onCreateRole(String serverID, String roleName, String roleId, int roleCreateTime) {
		_serverId = serverID;
		_roleName = roleName;
		//SDK.getInstance().getGameEventHelper().createRole(serverID, roleName);
	}

	@Override
	public void onEnterGame(String serverId, String roleName) {
		_serverId = serverId;
		_roleName = roleName;
		//SDK.getInstance().getGameEventHelper().onlineRole(_serverId, _roleName, _roleLevel);
		if(!_toolbar){
			_toolbar = true;
		//UCGameSdk.defaultSdk().createFloatButton(_activity);
		//UCGameSdk.defaultSdk().showFloatButton(_activity, 50, 50);
		}
	}
	@Override
	public void onQuestUpdate(String questType, String questId, QuestState state, String comment) {
		
		
	}

	@Override
	public void logEvent(String event, Map<String, String> eventData) {
//		if(event.equals("INSTALLED")){
//			SDK.getInstance().getAppEventHelper().appActivation();
//		}
	}

	@Override
	public void onResume(Activity act) {
		//SDK.getInstance().onCheck();
	}

	@Override
	public void onPause(Activity act) {
		
	}

	@Override
	public void onCreate(Activity act, Bundle savedInstanceState) {
		//SDK.getInstance().getAppEventHelper().appStartup();
		appContext = act;
	}

	@Override
	public void onDestroy(Activity act) {
		if(_toolbar){
			_toolbar = false;
			UCGameSdk.defaultSdk().destoryFloatButton(_activity);
		}
		//SDK.getInstance().onStop();
	}

	@Override
	public void onStop(Activity act) {
		
	}

	@Override
	public void onRestart(Activity act) {

	}

	@Override
	public void onActivityResult(Activity act, int requestCode, int resultCode, Intent data) {
		//PluginWrapper.onActivityResult(requestCode, resultCode, data);
		
	}

	@Override
	public void onNewIntent(Activity act,Intent intent) {
		//PluginWrapper.onNewIntent(intent);
		
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
	public void pay(Activity act,final PaymentInfo paymentInfo,final IPaymentCallback callback) {
	//	final String GEM_NAME = "GEM"; 
		if(!_sdk_init_completed){
			callback.OnPaymentResult(-1,"sdk not init");
			return;
		}
		TelephonyManager tm = (TelephonyManager) _activity.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        
        
		cn.uc.gamesdk.open.PaymentInfo ucp = new cn.uc.gamesdk.open.PaymentInfo();
        String product_id = paymentInfo.productId;//getMD5("UC_"+paymentInfo.productId);
		ucp.setCustomInfo(product_id + "|" + paymentInfo.serverId + "|" + paymentInfo.serverName + "|" + paymentInfo.characterName + "|" + deviceId + "|" + "android" + "|" + "UC");
		ucp.setAmount(paymentInfo.amount/100);
		if(isDebug){
			ucp.setNotifyUrl("http://221.236.172.156:23580/cnc/cncweb/channels/uc/api/paycallback.php");
		}else{
			ucp.setNotifyUrl("http://ps.uc.cnc.173fun.com/cnc/cncweb/channels/uc/api/paycallback.php");
		}
		
		try {
			UCGameSdk.defaultSdk().pay(ucp,new UCCallbackListener<OrderInfo>() {
				@Override
				public void callback(int code, OrderInfo orderInfo) {
					if(UCGameSdkStatusCode.SUCCESS == code){
						//SDK.getInstance().getGameEventHelper().chargeOk( Integer.toString(paymentInfo.serverId) , paymentInfo.characterName, _roleLevel, orderInfo.getPayWayName(),paymentInfo.itemName ,(double)paymentInfo.itemCount );
						callback.OnPaymentResult(0,"payment completed");
					}else{
						//SDK.getInstance().getGameEventHelper().chargeFailed(Integer.toString(paymentInfo.serverId),"",_roleLevel,"","",(double)paymentInfo.amount,Integer.toString(code) );
						callback.OnPaymentResult(code,"payment failed");
					}
				}
			});
		} catch (UCCallbackListenerNullException e) {
			e.printStackTrace();
		}
		
		
	}

	@Override
	public void confirmExit(Activity ctx,final SDKBridge callback) {
		
		callback.confirmExitCallback(true);
		
//		try {
//			UCGameSdk.defaultSdk().exitSDK(ctx, new UCCallbackListener<String>() {
//				@Override
//				public void callback(int errInfo, String errMsg) {
//					callback.confirmExitCallback(UCGameSdkStatusCode.SDK_EXIT == errInfo);
//				}
//			});
//			callback.confirmExitCallback(true);
//		} catch (UCCallbackListenerNullException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (UCMissActivityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}

	@Override
	public void onCurrencyGain(String currencyType, String reason, int amount) {
		//SDK.getInstance().getGameEventHelper().gainCoins(_serverId , _roleName, _roleLevel, currencyType, reason, amount);
		
	}

	@Override
	public void onCurrencyConsume(String currencyType, String reason, int amount) {
		//SDK.getInstance().getGameEventHelper().consumeCoins(_serverId , _roleName, _roleLevel, currencyType, reason, amount);
	}


	@Override
	public void onStart(Activity act) {
	}

	@Override
	public void onSaveInstanceState(Activity act, Bundle outState) {
	}

	@Override
	public void onConfigurationChanged(Activity act, Configuration newConfig) {
	}
}
