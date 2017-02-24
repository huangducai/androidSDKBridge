package com.gamefps.sdk.supersdk;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.gamefps.sdkbridge.IAccountBindCallback;
import com.gamefps.sdkbridge.IActivityOverrider;
import com.gamefps.sdkbridge.IAnalyticsSDK;
import com.gamefps.sdkbridge.IAuthenticationSDK;
import com.gamefps.sdkbridge.IAuthenticationSDKEx;
import com.gamefps.sdkbridge.IPaymentCallback;
import com.gamefps.sdkbridge.IPaymentSDK;
import com.gamefps.sdkbridge.IQueryAccountBindingsCallback;
import com.gamefps.sdkbridge.ISDKWarpper;
import com.gamefps.sdkbridge.LoginAccountInfo;
import com.gamefps.sdkbridge.PaymentInfo;
import com.gamefps.sdkbridge.SDKBridge;
import com.gamefps.sdkbridge.SdkConfigInfo;
import com.gamefps.sdkbridge.SdkProviderInfo;
import com.supersdk.framework.ErrorCode;
import com.supersdk.framework.IHttpRequestJsonCallBack;
import com.supersdk.framework.SuperSdkHttpApi;
import com.supersdk.framework.SuperSdkHttpApi.SuperSdkHttpMethod;
import com.supersdk.framework.SuperSdkPublicVariables;
import com.supersdk.framework.callbacklistener.IExitCallBack;
import com.supersdk.framework.callbacklistener.ILoginCallBack;
import com.supersdk.framework.callbacklistener.ILogoutCallBack;
import com.supersdk.framework.callbacklistener.IOtherFunctionCallBack;
import com.supersdk.framework.callbacklistener.IPayCallBack;
import com.supersdk.framework.callbacklistener.IPlatformInitCallBack;
import com.supersdk.framework.data.GameData;
import com.supersdk.openapi.SuperSdkOpenApi;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

public class SuperSDKAdapter implements IAuthenticationSDKEx, ISDKWarpper, IActivityOverrider, IAnalyticsSDK, IPaymentSDK {
	private final static SdkProviderInfo SDK_PROVIDER_INFO = new SdkProviderInfo("SUPERSDK", "super sdk adapter", 1,true);
	public final static String TAG = "SuperSDKAdapter";

	private boolean _sdk_init_completed = false;
	private int _roleLevel = -1;
	private static GameData gameData;
	private static int spUrlType = SuperSdkPublicVariables.SP_URL_TYPE_FOREIGN;

	private SDKBridge _sdkBridge = null;
	
	private void getUserInfoFromSid(String token, final SDKBridge cb) {
		final IAuthenticationSDK pThis = this;
		try {
			String etoken = URLEncoder.encode(token, "utf-8");
			String url = "http://recharge.mc.gtarcade.com/cnc/cncweb/api/sdklogin.php?cncsdk=yz&osdk_ticket=" + etoken + "&cnclang=zh-cn";
			new SuperSdkHttpApi().send(SuperSdkHttpMethod.GET, url, null, new IHttpRequestJsonCallBack() {
				@Override
				public void callBack(JSONObject jsonObj) {
					try {
						int errCode = jsonObj.getInt("errCode");
						Log.e(TAG, "errCode = " + errCode);
						if (0 == errCode) {
							SuperSdkOpenApi.getInstance().adEvent("ad_login", null);

							LoginAccountInfo ai = new LoginAccountInfo();
							JSONObject userInfo = jsonObj.getJSONObject("retVal");
							ai.channelPrefix = userInfo.getString("channelPrefix");
							ai.channelAccountId = userInfo.getString("channelAccountId");
							ai.accountId = userInfo.getString("accountId");
							ai.loginTime = userInfo.getString("time");
							ai.isNewAccount = userInfo.getBoolean("isNewAccount");
							ai.token = userInfo.getString("token");

							String clientIp = userInfo.getString("clientIp");
							String superDeviceId = SuperSdkOpenApi.getInstance().getDeviceUUID();
							String superSdkVersionId = SuperSdkOpenApi.getInstance().getPlatformConfig(SuperSdkPublicVariables.OSDK_CONFIG_ID, "-1");
							String superSdkPlatformId = SuperSdkOpenApi.getInstance().getPlatformConfig(SuperSdkPublicVariables.EXT, "-1");
							ai.extra = "YZ" + "|" + superDeviceId + "|" + clientIp + "|" + superSdkVersionId + "|"+ superSdkPlatformId;

							cb.OnAuthenticationSuccess(pThis, ai);
						} else {
							cb.OnAuthenticationFailed(pThis, jsonObj.getString("errMsg"));
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logout() {
		SuperSdkOpenApi.getInstance().logout(gameData);
	}

	// public void forum() {
	// if (!SuperSdkOpenApi.getInstance().hasForum()) {
	// } else {
	// SuperSdkOpenApi.getInstance().openForum(gameData);
	// }
	// if (!SuperSdkOpenApi.getInstance().hasPlatformCenter() ){
	// } else {
	// SuperSdkOpenApi.getInstance().enterPlatformCenter(gameData);
	// }
	// if (!SuperSdkOpenApi.getInstance().hasCustomerService()) {
	// } else {
	// SuperSdkOpenApi.getInstance().enterCustomerService(gameData);
	// }
	// }
	@Override
	public void onGameCheckVersionBegin() {
		SuperSdkOpenApi.getInstance().onGameCheckVersionBegin();
	}

	@Override
	public void onGameCheckVersionEnd() {
		SuperSdkOpenApi.getInstance().onGameCheckVersionEnd(true);
	}

	@Override
	public void onEnterLoginView(final SDKBridge callback) {
		SuperSdkOpenApi.getInstance().onEnterLoginView();
		String superDeviceId = SuperSdkOpenApi.getInstance().getDeviceUUID();
		String superSdkPlatformId = SuperSdkOpenApi.getInstance().getPlatformConfig(SuperSdkPublicVariables.EXT, "-1");

		String[] ids = superSdkPlatformId.split("\\|");
		String url = "https://msl.gtarcade.com/servers";
		String bodyData = "columns=ip|is_active|is_recommend|server_url|server_port|socket_domain|socket_port&gameId="
				+ ids[1] + "&opId=" + ids[0] + "&userid=" + superDeviceId;
		JsonHttp.GetJsonFromUrlAsync("POST", url, bodyData, new SuperSDKCallBackListener() {
			@Override
			public void callback(int code, String jsonString) {
				Log.e(TAG, "code = " + code);
				if (code == 0){
					Log.e(TAG, "jsonString = " + jsonString);
					callback.enterLoginCallback("YZ", jsonString);
				}
			}
		});
	}
	@Override
	public boolean init(Activity act, SdkConfigInfo cfg) {
		
		SuperSdkOpenApi.getInstance().adInit(act, SuperSdkPublicVariables.SP_URL_TYPE_FOREIGN);
		SuperSdkOpenApi.getInstance().adEvent("ad_open", null);

		gameData = new GameData();

		SuperSdkOpenApi.getInstance().showLogo(act, null);
		SuperSdkOpenApi.getInstance().isOpenLog(true);
		SuperSdkOpenApi.getInstance().isOpenStatLog(true);
		SuperSdkOpenApi.getInstance().init(act, spUrlType, new IPlatformInitCallBack() {
			@Override
			public void OnInitedCallback(int code, String msg) {
				if (code == ErrorCode.SUCCESS) {
					_sdk_init_completed = true;
					Log.e(TAG, "super sdk init completed");
					SuperSdkOpenApi.getInstance().onGameInitEnd();
				} else {
					Log.e(TAG, "super sdk init failed:" + msg);
				}
			}

			@Override
			public void OnCheckVersionCallback(int code, String msg, int updateType) {
				if (code == ErrorCode.SUCCESS) {
					if (updateType == SuperSdkPublicVariables.CHECK_WITH_NEW_VERSION) {
					} else if (updateType == SuperSdkPublicVariables.CHECK_WITHOUT_NEW_VERSION) {
					} else if (updateType == SuperSdkPublicVariables.CHECK_WITHOUT_CHECK_VERSION) {
					}
				} else {
				}
			}
		}, new ILogoutCallBack() {
			@Override
			public void onFinished(String msg, int code, int type) {
				if (ErrorCode.SUCCESS != code) {
					return;
				}
				// GameData
				_roleLevel = -1;
				gameData.setServerId("");
				gameData.setServerName("");
				gameData.setAccountId("");
				gameData.setRoleId("");
				gameData.setRoleName("");
				gameData.setRoleLevel("");
				gameData.setLoginData("");
				gameData.setData("roleCreateTime", "");

				if (SuperSdkOpenApi.getInstance().hasFloatWindow()) {
					SuperSdkOpenApi.getInstance().closeFloatWindow();
				}
				
				if(null!=_sdkBridge)
					_sdkBridge.OnLogout();
				
			}
		});
		return true;
	}

	@Override
	public void Login(Activity act, final SDKBridge cb) {
		_sdkBridge = cb;
		
		final IAuthenticationSDK pThis = this;
		if (!_sdk_init_completed) {
			Log.e(TAG, "call login before sdk init");
			cb.OnAuthenticationFailed(this, "sdk not init");
			return;
		}
		Log.e(TAG, "super sdk login begin");
		SuperSdkOpenApi.getInstance().loginMode2(new ILoginCallBack() {
			@Override
			public void onFinished(String msg, int code) {
				if (code == ErrorCode.SUCCESS) {
					String loginData = "";
					String accessToken = "";
					String userId = "";

					try {
						JSONObject jsonObj = new JSONObject(msg);
						accessToken = jsonObj.getString("osdk_ticket");
						userId = jsonObj.getJSONObject("userinfo").getString("user_id");
						loginData = jsonObj.getJSONObject("data").toString();
					} catch (Exception e) {
						e.printStackTrace();
					}

					gameData.setAccountId(userId);
					gameData.setServerName("serverName");
					gameData.setLoginData(loginData);
					getUserInfoFromSid(accessToken, cb);
				} else {
					cb.OnAuthenticationFailed(pThis, msg);
				}
			}
		});
	}

	@Override
	public void setRoleInfo(Map<String, String> roleData) {
		String roleEntityId = roleData.get("entityId");
		gameData.setRoleId(roleEntityId);
		String roleCreateTime = roleData.get("CreateTime");
		gameData.setData("roleCreateTime", roleCreateTime);
		String roleName = roleData.get("Name");
		gameData.setRoleName(roleName);
		int level = Integer.parseInt(roleData.get("Level"));
		gameData.setRoleLevel(Integer.toString(level));
		if (_roleLevel > 0 && level > _roleLevel) {
			SuperSdkOpenApi.getInstance().onLevelUp(gameData);

			HashMap<String, String> map = new HashMap<String, String>();
			map.put("event_value", gameData.getRoleLevel());
			SuperSdkOpenApi.getInstance().adEvent("ad_levelup", map);

			if (level == 7) {
				HashMap<String, String> map2 = new HashMap<String, String>();
				map2.put("event_value", "7");
				SuperSdkOpenApi.getInstance().adEvent("ad_effectivelevel", map2);
			} else if (level == 35) {
				SuperSdkOpenApi.getInstance().adEvent("ad_FB_onlevel35", null);
			} else if (level == 55) {
				SuperSdkOpenApi.getInstance().adEvent("ad_FB_onlevel55", null);
			} else if (level == 70) {
				SuperSdkOpenApi.getInstance().adEvent("ad_FB_onlevel70", null);
			} else if (level == 80) {
				SuperSdkOpenApi.getInstance().adEvent("ad_GG_onlevel80", null);
			} else if (level == 85) {
				SuperSdkOpenApi.getInstance().adEvent("ad_GG_onlevel85", null);
			}
		}
		_roleLevel = level;
	}

	@Override
	public void onCreateRole(String serverId, String roleName, String roleId, int roleCreateTime) {
		gameData.setRoleId(roleId);
		gameData.setServerId(serverId);
		gameData.setRoleName(roleName);
		gameData.setRoleLevel("1");
		gameData.setData("roleCreateTime", roleCreateTime + "");
		SuperSdkOpenApi.getInstance().onCreatRole(gameData);

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("event_value", gameData.getAccountId());
		SuperSdkOpenApi.getInstance().adEvent("ad_createrole", map);
	}

	@Override
	public void onEnterGame(String serverId, String roleName) {
		gameData.setServerId(serverId);
		gameData.setRoleName(roleName);
		SuperSdkOpenApi.getInstance().onEnterGame(gameData);
		SuperSdkOpenApi.getInstance().onOpenMainPage(gameData);
		if (SuperSdkOpenApi.getInstance().hasFloatWindow()) {
			SuperSdkOpenApi.getInstance().openFloatWindow(10, 10, gameData);
		}
	}

	@Override
	public void pay(Activity act, final PaymentInfo paymentInfo, final IPaymentCallback callback) {
		if (!_sdk_init_completed) {
			callback.OnPaymentResult(-1, "sdk not init");
			return;
		}

		try {
			String productId = paymentInfo.productId;
			int price = Math.round(paymentInfo.amount * 0.01f);
			String productName = paymentInfo.productName;
			String productDesc = paymentInfo.productDesc;
			
			Log.e(TAG, "price = " + price);
			Log.e(TAG, "productId = " + productId);
			Log.e(TAG, "productName = " + productName);
			Log.e(TAG, "productDesc = " + productDesc);
			
			SuperSdkOpenApi.getInstance().pay(price, productId, productName, productDesc,
					"10","diamond", "pay", "", 0, new IPayCallBack() {
				@Override
				public void OnGetOrderIdSuccess(int code, String msg, String orderId) {
					Log.e(TAG, "orderId = " + orderId);
				}

				@Override
				public void OnFinished(int code, String msg) {
					Log.e(TAG, "pay,OnFinished = " + code);
					Log.e(TAG, "msg = " + msg);
					if (code == 1){
						callback.OnPaymentResult(0, "payment completed");
					}else{
						callback.OnPaymentResult(-1, "payment fail");
					}
				}
			}, gameData);

			Map<String, String> map = new HashMap<String, String>();
			map.put("event_value", Integer.toString(price));
			SuperSdkOpenApi.getInstance().adEvent("ad_pay", map);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void confirmExit(Activity ctx, final SDKBridge callback) {
		SuperSdkOpenApi.getInstance().exit(new IExitCallBack() {
			@Override
			public void onFinished(String msg, int code) {
				SuperSdkOpenApi.getInstance().onExitGame(gameData);
				callback.confirmExitCallback(true);
			}
		}, gameData);
	}

	@Override
	public SdkProviderInfo getProviderInfo() {
		return SDK_PROVIDER_INFO;
	}

	@Override
	public void onDestroy(Activity act) {
		if (SuperSdkOpenApi.getInstance().hasFloatWindow()) {
			SuperSdkOpenApi.getInstance().closeFloatWindow();
		}
		SuperSdkOpenApi.getInstance().onDestroy();
	}

	@Override
	public void onCreate(Activity act, Bundle savedInstanceState) {

	}

	@Override
	public void onCreateAccount(String account, Map<String, String> accountData) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoginAccount(String account, Map<String, String> accountData) {
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
		String FIRST_PAY = "FIRST_PAY"; 
		String SEVEN_RETENTION = "SEVEN_RETENTION";
		String THIRTY_RETENTION = "THIRTY_RETENTION";
		String SIXTY_RETENTION = "SIXTY_RETENTION";
		String INSTALLED = "INSTALLED";
		String OCCUPY_THREE_LAND = "OCCUPY_THREE_LAND";
		String OCCUPY_FIVE_LAND = "OCCUPY_FIVE_LAND";
		String OCCUPY_NINE_LAND = "OCCUPY_NINE_LAND";
		String OCCUPY_FIVE_CITY = "OCCUPY_FIVE_CITY";
		String OCCUPY_NINE_CITY = "OCCUPY_NINE_CITY";
		String OCCUPY_TEN_CITY = "OCCUPY_TEN_CITY";
		String COLLECT_ORANGEROLE_15 = "COLLECT_ORANGEROLE_15";
		String COLLECT_ORANGEROLE_30 = "COLLECT_ORANGEROLE_30";
		String RECEIVE_FIRST_PAY_GIFT = "RECEIVE_FIRST_PAY_GIFT";
		
		if (FIRST_PAY.equals(event)){
			SuperSdkOpenApi.getInstance().adEvent("ad_firstpay", null);
		}else if(SEVEN_RETENTION.equals(event)){
			SuperSdkOpenApi.getInstance().adEvent("ad_7retention", null);
		}else if(THIRTY_RETENTION.equals(event)){
			SuperSdkOpenApi.getInstance().adEvent("ad_30retention", null);
		}else if(SIXTY_RETENTION.equals(event)){
			SuperSdkOpenApi.getInstance().adEvent("ad_60retention", null);
		}else if(INSTALLED.equals(event)){
			SuperSdkOpenApi.getInstance().adEvent("ad_FB_installed", null);
		}else if(OCCUPY_THREE_LAND.equals(event)){
			SuperSdkOpenApi.getInstance().adEvent("ad_Newbee", null);
		}else if(OCCUPY_FIVE_LAND.equals(event)){
			SuperSdkOpenApi.getInstance().adEvent("ad_Wellplayed", null);
		}else if(OCCUPY_NINE_LAND.equals(event)){
			SuperSdkOpenApi.getInstance().adEvent("ad_Hardcore", null);
		}else if(OCCUPY_FIVE_CITY.equals(event)){
			SuperSdkOpenApi.getInstance().adEvent("ad_FB_occupy_five_city", null);
		}else if(OCCUPY_NINE_CITY.equals(event)){
			SuperSdkOpenApi.getInstance().adEvent("ad_FB_occupy_nine_city", null);
		}else if(OCCUPY_TEN_CITY.equals(event)){
			SuperSdkOpenApi.getInstance().adEvent("ad_FB_occupy_ten_city", null);
		}else if(COLLECT_ORANGEROLE_15.equals(event)){
			SuperSdkOpenApi.getInstance().adEvent("ad_MiddlePay", null);
		}else if(COLLECT_ORANGEROLE_30.equals(event)){
			SuperSdkOpenApi.getInstance().adEvent("ad_HighPay", null);
		}else if(RECEIVE_FIRST_PAY_GIFT.equals(event)){
			SuperSdkOpenApi.getInstance().adEvent("ad_FB_receive_first_pay_gift", null);
		}else{
			SuperSdkOpenApi.getInstance().adEvent(event, null);
		}
	}

	@Override
	public void onPause(Activity act) {
		SuperSdkOpenApi.getInstance().onPauseGame(gameData);
	}

	@Override
	public void onResume(Activity act) {
		SuperSdkOpenApi.getInstance().onResumeGame(gameData);
	}

	@Override
	public void onStop(Activity act) {
		SuperSdkOpenApi.getInstance().onStopGame(gameData);
	}

	@Override
	public void onRestart(Activity act) {
		SuperSdkOpenApi.getInstance().onRestartGame();
	}

	@Override
	public void onActivityResult(Activity act, int requestCode, int resultCode, Intent data) {
		SuperSdkOpenApi.getInstance().onActivityResult(requestCode, resultCode, data, gameData);
	}

	@Override
	public void onNewIntent(Activity act, Intent intent) {
		SuperSdkOpenApi.getInstance().onNewIntent(intent);
	}

	@Override
	public void onStart(Activity act) {
		SuperSdkOpenApi.getInstance().onStartGame(gameData);
	}

	@Override
	public void onSaveInstanceState(Activity act, Bundle outState) {
		SuperSdkOpenApi.getInstance().onSaveInstanceState(outState);
	}

	@Override
	public void onConfigurationChanged(Activity act, Configuration newConfig) {
		SuperSdkOpenApi.getInstance().onConfigurationChanged(newConfig);
	}

	
	
	@Override
	public void AccountBind(final String accountType,final IAccountBindCallback callback) {
		Log.d(TAG, "AccountBind:" + accountType);
		String linkArg; 
		if(accountType.equals("gta")){
			linkArg = "1";
		}else if(accountType.equals("facebook")){
			linkArg = "2";
		}else if(accountType.equals("twitter")){
			linkArg = "3";
		}else{
			linkArg = null;
		}
		if(null != linkArg){
			Log.d(TAG, "AccountBind.callOtherFunction.accountLink");
			SuperSdkOpenApi.getInstance().callOtherFunction("accountLink", linkArg, new IOtherFunctionCallBack() {
				@Override
				public void onFinished(String arg0, int errCode, String strData) {
					Log.d(TAG, "AccountBind.onFinished:" + errCode);
					callback.OnAccountBindResult(accountType, ErrorCode.SUCCESS == errCode);					
				}
			});
		}else{
			Log.e(TAG, "AccountBind invalid type:" + accountType);
			callback.OnAccountBindResult(accountType, false);		
		}
	}

	@Override
	public void QueryAccountBindings(final IQueryAccountBindingsCallback callback) {
		SuperSdkOpenApi.getInstance().callOtherFunction("queryLinkStatus", "true", 
				new IOtherFunctionCallBack() {
					@Override
					public void onFinished(String arg0, int errCode, String strData) {
						Map<String,String> retVal = new HashMap<String, String>();
						if(ErrorCode.SUCCESS == errCode){
							try {
								JSONObject json = new JSONObject(strData);
								
								if(1==json.getInt("gta")){
									retVal.put("gta","true");
								}else{
									retVal.put("gta", "false");
								}
								
								if(1==json.getInt("facebook")){
									retVal.put("facebook","true");
								}else{
									retVal.put("facebook", "false");
								}
								
								if(1==json.getInt("twitter")){
									retVal.put("twitter","true");
								}else{
									retVal.put("twitter", "false");
								}
								
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						callback.OnQueryAccountBindingsResult(retVal);
					}
				});
	}

}