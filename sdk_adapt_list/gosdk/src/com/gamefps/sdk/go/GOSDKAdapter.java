package com.gamefps.sdk.go;

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
import cn.gosdk.FTGameSdk;
import cn.gosdk.base.event.Subscribe;
import cn.gosdk.base.param.SDKParamKey;
import cn.gosdk.base.param.SDKParams;
import cn.gosdk.event.SDKEventKey;
import cn.gosdk.event.SDKEventReceiver;
import cn.uc.dp.sdk.Constants.Mode;
import cn.uc.dp.sdk.SDK;

public class GOSDKAdapter implements IAuthenticationSDK, ISDKWarpper, IActivityOverrider, IAnalyticsSDK, IPaymentSDK {

	private final static SdkProviderInfo SDK_PROVIDER_INFO = new SdkProviderInfo("GO", "go sdk adapter", 1, true);
	public final static String TAG = "UCSDKAdapter";
	private Activity _activity;
	// 必须登录成功以后才能调用【支付】【注销】【统计】接口
	private boolean isLogined = false;
	private SDKBridge bridgeCB = null;
	private IPaymentCallback payCallback;
	private String _serverId = "";
	private String _serverName = "";
	private int presentLevel = -1;
	private boolean isCreateRole = false;
	private Map<String, String> _roleData;
	private IAuthenticationSDK _pThis = this;
	private String SDKLoginToken = null;

	private boolean _isNormalLogin = false;
	Activity appContext = null;
	
	private String ucAccountId = "";
	
	//是否是测试环境
	private boolean isDebug = true;
	private boolean isUseJingFen = true;
	
	private boolean isInitSuccess = false;
	
	
	// 账号相关回调：只要在这里做强引用，sdk就会正常回调
	@SuppressWarnings("unused")
	private SDKEventReceiver accountEventReceiver = new SDKEventReceiver() {

		@Subscribe(event = SDKEventKey.ON_LOGIN_SUCC)
		private void onLoginSucc(final SDKParams params) {
			_activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					String token = params.get(SDKParamKey.STRING_TOKEN, "");
					if (isLogined && !_isNormalLogin) {
						if (null != bridgeCB) {
							bridgeCB.OnLogout();
							SDKLoginToken = token;
							return;
						}
					}
					_isNormalLogin = false;
					isLogined = true;
					getUserInfoFromSid(token);
					Log.d(TAG, "登录成功,token:" + params.get(SDKParamKey.STRING_TOKEN, ""));
				}
			});
		}

		@Subscribe(event = SDKEventKey.ON_LOGIN_FAILED)
		private void onLoginFailed(final String desc) {

			_activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Log.d(TAG, "登录失败:" + desc);

					bridgeCB.OnAuthenticationFailed(_pThis, "登录失败");
				}
			});
		}

		@Subscribe(event = SDKEventKey.ON_PAY_SUCC)
		private void onPaySucc(SDKParams params) {
			_activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "支付成功: ");
					payCallback.OnPaymentResult(0, "payment completed");
				}
			});
		}

		@Subscribe(event = SDKEventKey.ON_PAY_FAILED)
		private void onPayFailed(SDKParams params) {
			final String errorMsg = params.get(SDKParamKey.STRING_PAY_FAILED_MSG, "");
			_activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "支付失败: " + errorMsg);
					payCallback.OnPaymentResult(SDKBridge.E_Payment_Failed, errorMsg);
				}
			});
		}

		@Subscribe(event = SDKEventKey.ON_LOGOUT)
		private void onLogout() {
			_activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					isLogined = false;
					Log.d(TAG, "退出账号成功");
					if (null != bridgeCB)
						bridgeCB.OnLogout();

				}
			});
		}

		@Subscribe(event = SDKEventKey.ON_INIT_SUCC)
		private void onInitSucc() {
			isInitSuccess = true;
			_activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "初始化成功");
					//初始化成功 才调用 appstartup
					if(isUseJingFen){
						SDK.getInstance().getAppEventHelper().appStartup();		
					}
				}
			});
		}

		@Subscribe(event = SDKEventKey.ON_INIT_FAILED)
		private void onInitFailed(final String desc) {
			_activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "初始化失败:" + desc);
					Log.d(TAG, "重新初始化");
					FTGameSdk.init(_activity, null);
				}
			});
		}

		@Subscribe(event = SDKEventKey.ON_EXIT_CONFIRMED)
		private void onExitConfirmed() {
			_activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Log.d(TAG, "SDK退出");
					_activity.finish();
					System.exit(0);
				}
			});
		}
	};

	private void getUserInfoFromSid(final String token) {
		final IAuthenticationSDK pThis = this;
		final SDKBridge cb = bridgeCB;
		try {
			String esid = URLEncoder.encode(token, "utf-8");
			
			String url;
			if(isDebug){
				url = "http://221.236.172.156:23580/cnc/cncweb/api/sdklogin.php?cncsdk=go&cnclang=zh-cn&token=" + esid;	
			}else{
				url = "https://gw.uc.cnc.173fun.com/cnc/cncweb/api/sdklogin.php?cncsdk=go&cnclang=zh-cn&token=" + esid;	
			}
			
			//			// String url =
			//			// "http://test.gw.cnc.173fun.com/cnc/cncweb/api/sdklogin.php?cncsdk=uc&cnclang=zh-cn&sid="
			//			// + esid;
			//
			//			String url = "http://221.236.172.156:23580/cnc/cncweb/api/sdklogin.php?cncsdk=go&cnclang=zh-cn&token="
			//					+ esid;
			//			// String url =
			//			// "http://221.236.172.156:23580/cnc-web/api/sdklogin.php?cncsdk=go&cnclang=zh-cn&token="
			//			// + esid;
			JsonHttp.GetJsonFromUrlAsync(url, new CallbackListener() {
				@Override
				public void callback(int code, JSONObject val) {
					if (0 == code) {
						try {
							int errCode = val.getInt("errCode");
							if (0 == errCode) {
								LoginAccountInfo ai = new LoginAccountInfo();
								JSONObject userInfo = val.getJSONObject("retVal");
								
								ucAccountId =  userInfo.getString("channelAccountId");
								ai.channelPrefix = userInfo.getString("channelPrefix");
								ai.channelAccountId = ucAccountId;
								ai.accountId = userInfo.getString("accountId");
								ai.loginTime = userInfo.getString("time");
								ai.isNewAccount = userInfo.getBoolean("isNewAccount");
								ai.token = userInfo.getString("token");

								// todo hdc
								TelephonyManager tm = (TelephonyManager) _activity
										.getSystemService(Context.TELEPHONY_SERVICE);
								String deviceId = tm.getDeviceId();
								JSONObject extraJson = new JSONObject();
								extraJson.put("deviceId", deviceId);
								extraJson.put("userType", appContext.getPackageName());
								extraJson.put("accountId",ucAccountId);
								extraJson.put("os", "android");
								extraJson.put("ch", "UC");
								

								ai.extra = extraJson.toString();
								ai.extra = "UC|" + ai.extra;

								cb.OnAuthenticationSuccess(pThis, ai);
							} else {
								// String errMsg
								cb.OnAuthenticationFailed(pThis, val.getString("errMsg"));
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					} else {
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
	public boolean init(Activity ctx, SdkConfigInfo cfg) {
		_activity = ctx;
		FTGameSdk.init(ctx, null);
//		if(isUseJingFen){
//			SDK.getInstance().init(ctx, "467787787756", "40c2a4d0027b77f298d4dc0a5725ae46", cfg.channelId, Mode.PRODUCTION);
//			SDK.getInstance().getAppEventHelper().appStartup();	
//		}
		return true;
	}

	@Override
	public void onCreate(Activity act, Bundle savedInstanceState) {

		// setContentView(R.layout.activity_main);
		// tv_show = (TextView) findViewById(R.id.tv_show);
		// 生命周期
		appContext = act;
		FTGameSdk.lifeCycle().onCreate(act);
	}

	@Override
	public void Login(Activity act, SDKBridge cb) {
		bridgeCB = cb;
		_isNormalLogin = true;
		if (SDKLoginToken != null) {
			getUserInfoFromSid(SDKLoginToken);
			SDKLoginToken = null;
			return;
		}
		try {
			FTGameSdk.login(act, null);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void pay(Activity act, PaymentInfo paymentInfo, IPaymentCallback callback) {
		payCallback = callback;
		TelephonyManager tm = (TelephonyManager) _activity.getSystemService(Context.TELEPHONY_SERVICE);
		String deviceId = tm.getDeviceId();
		Log.d(TAG, "deviceId" + deviceId);

		// cn.uc.gamesdk.open.PaymentInfo ucp = new
		// cn.uc.gamesdk.open.PaymentInfo();
		// String product_id = getMD5("GO_"+paymentInfo.productId);
		String arg = paymentInfo.productId + "|" + paymentInfo.serverId + "|" + paymentInfo.serverName + "|"
				+ paymentInfo.characterName + "|" + deviceId + "|" + "android" + "|" + "GO";

		int amount = Math.round(paymentInfo.amount);
		SDKParams params = new SDKParams();
		params.put(SDKParamKey.STRING_CP_ORDERID, "" + System.currentTimeMillis());
		params.put(SDKParamKey.INT_PAY_AMOUNT, amount);
		// params.put(SDKParamKey.STRING_PAY_CALLBACK_URL,
		// "http://221.236.172.156:23580/cnc/cncweb/channels/go/api/paycallback.php");
		params.put(SDKParamKey.STRING_PAY_CALLBACK_PARAMS, arg);
		params.put(SDKParamKey.STRING_PAY_PRODUCT_NAME, paymentInfo.productName);
		params.put(SDKParamKey.STRING_PAY_PRODUCT_ID, paymentInfo.productId + "");
		params.put(SDKParamKey.STRING_PAY_PRODUCT_DESCRIBE, paymentInfo.productDesc);

		try {
			FTGameSdk.pay(act, params);
		} catch (IllegalAccessException e) {
			Log.d(TAG, "支付失败: " + e.getMessage());
		}

	}

	public String getMD5(String info) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(info.getBytes("UTF-8"));
			byte[] encryption = md5.digest();

			StringBuffer strBuf = new StringBuffer();
			for (int i = 0; i < encryption.length; i++) {
				if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
					strBuf.append("0").append(Integer.toHexString(0xff & encryption[i]));
				} else {
					strBuf.append(Integer.toHexString(0xff & encryption[i]));
				}
			}
			return strBuf.toString();
		} catch (NoSuchAlgorithmException e) {
			return "";
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	@Override
	public void setRoleInfo(Map<String, String> roleData) {
		// TODO Auto-generated method stub
		_roleData = roleData;
		if (isLogined) {
			Integer it = new Integer(roleData.get("Level"));
			int level = it.intValue();
			if (presentLevel != -1 && presentLevel < level) {
				try {
					SDKParams params = new SDKParams();
					params.put(SDKParamKey.STRING_ROLE_ID, roleData.get("AccountId") + "");
					params.put(SDKParamKey.STRING_ROLE_NAME, roleData.get("Name"));
					params.put(SDKParamKey.STRING_ROLE_LEVEL, level + "");
					params.put(SDKParamKey.STRING_ZONE_ID, _serverId);
					params.put(SDKParamKey.STRING_ZONE_NAME, _serverName);
					params.put(SDKParamKey.STRING_OPPORTUNITY_TYPE, SDKParamKey.STRING_OPPORTUNITY_TYPE_UPDATES);
					params.put(SDKParamKey.STRING_UNION_NAME, roleData.get("GuildName"));
					params.put(SDKParamKey.STRING_GAME_MONEY, roleData.get("Gold") + "");
					
					//如果要上360渠道以下角色 参数是必须的
			        params.put(SDKParamKey.STRING_ROLE_UNION_ID, roleData.get("GuildName"));
			        params.put(SDKParamKey.STRING_ROLE_PROFESSION_ID, "0");//角色无职业 
			        params.put(SDKParamKey.STRING_ROLE_PROFESSION_NAME, "无");//角色无职业
			        params.put(SDKParamKey.STRING_ROLE_GENDER, "无");//游戏不获取玩家是男是女
			        params.put(SDKParamKey.STRING_ROLE_POWER_VALUE, "0");//玩家信息 是没有战力这个概念的
			        params.put(SDKParamKey.STRING_ROLE_VIP_LEVEL, roleData.get("VipLevel"));
			        
			        String gid = roleData.get("GuildId");
			        gid = gid.length() > 0 ? gid : "0";
			        params.put(SDKParamKey.STRING_ROLE_UNION_TITLE_ID, gid);
			        
			        String giName = roleData.get("GuildId");
			        giName = giName.length() > 0 ? gid : "0";
			        params.put(SDKParamKey.STRING_ROLE_UNION_TITLE_NAME, giName);
			        
			        params.put(SDKParamKey.STRING_ROLE_FRIEND_LIST, "无");
			        //角色创建时间，这里使用时间戳/1000得到10位数，【必须使用服务端记录的值】，否则审核不通过(阿里游戏必备参数)
			        params.put(SDKParamKey.STRING_ROLE_CREATE_TIME, ""+ roleData.get("CreateTime"));

					FTGameSdk.setRoleData(_activity, params);
					Log.d(TAG, "角色升级setRoleInfo, levelUpTo");
					// -- 服务器上报
					// SDK.getInstance().getGameEventHelper().levelUpTo(_serverId,
					// roleData.get("Name"), level);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			presentLevel = level;
		} else {
			Log.d(TAG, "请先登录");
		}
		Log.d(TAG, "打点触发:setRoleInfo");

	}

	@Override
	public void onEnterGame(String serverId, String roleName) {
		// TODO Auto-generated method stub

		if (isCreateRole) {
			if (isLogined) {
				try {
					SDKParams params = new SDKParams();
					params.put(SDKParamKey.STRING_ROLE_ID, _roleData.get("AccountId") + "");
					params.put(SDKParamKey.STRING_ROLE_NAME, _roleData.get("Name"));
					params.put(SDKParamKey.STRING_ROLE_LEVEL, _roleData.get("Level") + "");
					params.put(SDKParamKey.STRING_ZONE_ID, _serverId);
					params.put(SDKParamKey.STRING_ZONE_NAME, _serverName);
					params.put(SDKParamKey.STRING_OPPORTUNITY_TYPE, SDKParamKey.STRING_OPPORTUNITY_TYPE_CREATEROLE);
					params.put(SDKParamKey.STRING_UNION_NAME, _roleData.get("GuildName"));
					params.put(SDKParamKey.STRING_GAME_MONEY, _roleData.get("Gold") + "");
					
					//如果要上360渠道以下角色 参数是必须的
			        params.put(SDKParamKey.STRING_ROLE_UNION_ID, _roleData.get("GuildName"));
			        params.put(SDKParamKey.STRING_ROLE_PROFESSION_ID, "0");//角色无职业 
			        params.put(SDKParamKey.STRING_ROLE_PROFESSION_NAME, "无");//角色无职业
			        params.put(SDKParamKey.STRING_ROLE_GENDER, "无");//游戏不获取玩家是男是女
			        params.put(SDKParamKey.STRING_ROLE_POWER_VALUE, "0");//玩家信息 是没有战力这个概念的
			        params.put(SDKParamKey.STRING_ROLE_VIP_LEVEL, _roleData.get("VipLevel"));
			        
			        String gid = _roleData.get("GuildId");
			        gid = gid.length() > 0 ? gid : "0";
			        params.put(SDKParamKey.STRING_ROLE_UNION_TITLE_ID, gid);
			        
			        String giName = _roleData.get("GuildId");
			        giName = giName.length() > 0 ? gid : "0";
			        params.put(SDKParamKey.STRING_ROLE_UNION_TITLE_NAME, giName);
			        
			        params.put(SDKParamKey.STRING_ROLE_FRIEND_LIST, "无");
			        //角色创建时间，这里使用时间戳/1000得到10位数，【必须使用服务端记录的值】，否则审核不通过(阿里游戏必备参数)
			        params.put(SDKParamKey.STRING_ROLE_CREATE_TIME, ""+ _roleData.get("CreateTime"));

					
					FTGameSdk.setRoleData(_activity, params);
					Log.d(TAG, "创建角色");
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			} else {
				Log.d(TAG, "请先登录");
			}
		}

		if (isLogined) {
			try {
				SDKParams params = new SDKParams();
				params.put(SDKParamKey.STRING_ROLE_ID, _roleData.get("AccountId") + "");
				params.put(SDKParamKey.STRING_ROLE_NAME, _roleData.get("Name"));
				params.put(SDKParamKey.STRING_ROLE_LEVEL, _roleData.get("Level") + "");
				params.put(SDKParamKey.STRING_ZONE_ID, _serverId);
				params.put(SDKParamKey.STRING_ZONE_NAME, _serverName);
				params.put(SDKParamKey.STRING_OPPORTUNITY_TYPE, SDKParamKey.STRING_OPPORTUNITY_TYPE_ENTERGAME);
				params.put(SDKParamKey.STRING_UNION_NAME, _roleData.get("GuildName"));
				params.put(SDKParamKey.STRING_GAME_MONEY, _roleData.get("Gold") + "");
				
				//如果要上360渠道以下角色 参数是必须的
		        params.put(SDKParamKey.STRING_ROLE_UNION_ID, _roleData.get("GuildName"));
		        params.put(SDKParamKey.STRING_ROLE_PROFESSION_ID, "0");//角色无职业 
		        params.put(SDKParamKey.STRING_ROLE_PROFESSION_NAME, "无");//角色无职业
		        params.put(SDKParamKey.STRING_ROLE_GENDER, "无");//游戏不获取玩家是男是女
		        params.put(SDKParamKey.STRING_ROLE_POWER_VALUE, "0");//玩家信息 是没有战力这个概念的
		        params.put(SDKParamKey.STRING_ROLE_VIP_LEVEL, _roleData.get("VipLevel"));
		        
		        String gid = _roleData.get("GuildId");
		        gid = gid.length() > 0 ? gid : "0";
		        params.put(SDKParamKey.STRING_ROLE_UNION_TITLE_ID, gid);
		        
		        String giName = _roleData.get("GuildId");
		        giName = giName.length() > 0 ? gid : "0";
		        params.put(SDKParamKey.STRING_ROLE_UNION_TITLE_NAME, giName);
		        
		        params.put(SDKParamKey.STRING_ROLE_FRIEND_LIST, "无");
		        //角色创建时间，这里使用时间戳/1000得到10位数，【必须使用服务端记录的值】，否则审核不通过(阿里游戏必备参数)
		        params.put(SDKParamKey.STRING_ROLE_CREATE_TIME, ""+ _roleData.get("CreateTime"));

				FTGameSdk.setRoleData(_activity, params);
				Log.d(TAG, "进入游戏");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			Log.d(TAG, "请先登录");
		}

		Log.d(TAG, "打点触发:onEnterGame,  onlineRole");
		// sdkevent 中打点
		// SDK.getInstance().getGameEventHelper().onlineRole(_serverId,
		// _roleData.get("Name"), Integer.parseInt(_roleData.get("Level")));
	}

	@Override
	public void onCreateRole(String serverId, String roleName, String roleId, int roleCreateTime) {
		// TODO Auto-generated method stub
		isCreateRole = true;
		Log.d(TAG, "打点触发:onCreateRole");
		// sdkevent 中打点
		// SDK.getInstance().getGameEventHelper().createRole(serverId,
		// roleName);
	}

	@Override
	public void onLoginAccount(String account, Map<String, String> accountData) {
		// TODO Auto-generated method stub
		Log.d(TAG, "打点触发:onLoginAccount,  onlineUser");
		if(isUseJingFen){
			SDK.getInstance().getGameEventHelper().onlineUser("UC", ucAccountId);
		}
		// SDK.getInstance().getGameEventHelper().onlineUser("UC", account);
	}

	@Override
	public void onQuestUpdate(String questType, String questId, QuestState state, String comment) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCurrencyGain(String currencyType, String reason, int amount) {
		// TODO Auto-generated method stub
		Log.d(TAG, "打点触发:onCurrencyGain,  gainCoins");
		// SDK.getInstance().getGameEventHelper().gainCoins(_serverId ,
		// _roleData.get("Name"), Integer.parseInt(_roleData.get("Level")),
		// currencyType, reason, amount);
	}

	@Override
	public void onCurrencyConsume(String currencyType, String reason, int amount) {
		// TODO Auto-generated method stub
		Log.d(TAG, "打点触发:onCurrencyConsume,  consumeCoins");
		// SDK.getInstance().getGameEventHelper().consumeCoins(_serverId ,
		// _roleData.get("Name"), Integer.parseInt(_roleData.get("Level")),
		// currencyType, reason, amount);
	}

	@Override
	public void logEvent(String event, Map<String, String> eventData) {
		// TODO Auto-generated method stub
		Log.d(TAG, "logEvent：" + event);
		if (event.equals("EVT_SERVER_INFO")) {
			_serverId = eventData.get("serverID") + "";
			_serverName = eventData.get("serverName");
		}

		sdkEvent(event, eventData);

		// else if(event.equals("INSTALLED")){
		// SDK.getInstance().getAppEventHelper().appActivation();
		// }else if(event.equals("EVENT_PAYMENT_SUCCESS")){
		// SDK.getInstance().getGameEventHelper().chargeOk(eventData.get("serverName"),
		// eventData.get("roleName"), 0, "",
		// "人民币",Double.valueOf(eventData.get("amount")) );
		// }else if(event.equals("EVENT_PAYMENT_FAIL")){
		// SDK.getInstance().getGameEventHelper().chargeFailed(eventData.get("serverName"),
		// eventData.get("roleName"), 0, "",
		// "人民币",Double.valueOf(eventData.get("amount")),eventData.get("reason")
		// );
		// }
	}

	@Override
	public void onDestroy(Activity act) {
		FTGameSdk.lifeCycle().onDestroy(act);

	}

	@Override
	public void onPause(Activity act) {
		FTGameSdk.lifeCycle().onPause(act);

	}

	@Override
	public void onResume(Activity act) {
		FTGameSdk.lifeCycle().onResume(act);

	}

	@Override
	public void onStop(Activity act) {
		FTGameSdk.lifeCycle().onStop(act);

	}

	@Override
	public void onRestart(Activity act) {
		FTGameSdk.lifeCycle().onRestart(act);

	}

	@Override
	public void onActivityResult(Activity act, int requestCode, int resultCode, Intent data) {
		FTGameSdk.lifeCycle().onActivityResult(act, requestCode, resultCode, data);

	}

	@Override
	public void onNewIntent(Activity act, Intent intent) {
		FTGameSdk.lifeCycle().onNewIntent(act, intent);
	}

	@Override
	public void onStart(Activity act) {
		FTGameSdk.lifeCycle().onStart(act);
	}

	@Override
	public void onSaveInstanceState(Activity act, Bundle outState) {
		FTGameSdk.lifeCycle().onSaveInstanceState(act, outState);
	}

	@Override
	public void onConfigurationChanged(Activity act, Configuration newConfig) {
		FTGameSdk.lifeCycle().onConfigurationChanged(act, newConfig);
	}

	@Override
	public void confirmExit(Activity ctx, SDKBridge callback) {
		try {
			FTGameSdk.exit(ctx, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logout() {
		try {
			FTGameSdk.logout(_activity, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

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
		// TODO Auto-generated method stub
		return SDK_PROVIDER_INFO;
	}

	@Override
	public void onCreateAccount(String account, Map<String, String> accountData) {
		// TODO Auto-generated method stub
		Log.d(TAG, "打点触发:onCreateAccount,  createUser");
		if(isUseJingFen){
			SDK.getInstance().getGameEventHelper().createUser("UC", ucAccountId);
		}
		// SDK.getInstance().getGameEventHelper().createUser("UC", account);
	}

	// create by hdc
	public String getAccount(String account) {
		String str = account.substring(0, 6);
		if (str.equals("UC_JY_")) {
			return account.substring(6);
		}
		return account;
	}

	// 所有的在同一处处理 方便bug 查找
	public void sdkEvent(String event, Map<String, String> eventData) {
		try {
			
			if(!isUseJingFen){
				Log.d(TAG, "不使用经分sdk");
				return;
			}
			
			if (event.equals("role_online")) {
				// _serverName = ;
				SDK.getInstance().getGameEventHelper().onlineRole(eventData.get("serverName"),
						eventData.get("roleName"), Integer.parseInt(eventData.get("roleLevel")));
			} else if (event.equals("role_create")) {
				// 创建角色 在这里统计 上面的信息 不完整
				SDK.getInstance().getGameEventHelper().createRole(eventData.get("serverName"),
						eventData.get("roleName"));
			} else if (event.equals("app_activation")) {
				SDK.getInstance().getAppEventHelper().appActivation();
			} else if (event.equals("app_crash")) {
				SDK.getInstance().getAppEventHelper().appCrash(eventData.get("activityName"),
						eventData.get("crashMessage"));
			} else if (event.equals("activity_start")) {
				// 区服名
				String serverName = eventData.get("serverName");
				// 角色名
				String roleName = eventData.get("roleName");
				// 角色等级
				int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
				// vip等级
				int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
				// 关卡类别
				String missionType = eventData.get("missionType");
				// 关卡/任务名称
				String mission = eventData.get("mission");

				SDK.getInstance().getGameEventHelper().startActivity(serverName, roleName, roleLevel, vipLevel,
						missionType, mission);
			} else if (event.equals("activity_finish")) {
				// 区服名
				String serverName = eventData.get("serverName");
				// 角色名
				String roleName = eventData.get("roleName");
				// 角色等级
				int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
				// vip等级
				int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
				// 关卡类别
				String missionType = eventData.get("missionType");
				// 关卡/任务名称
				String mission = eventData.get("mission");
				// 是否通过关卡/任务
				boolean passed = true;
				// 关卡/任务耗时
				int duration = Integer.parseInt(eventData.get("duration"));

				SDK.getInstance().getGameEventHelper().finishActivity(serverName, roleName, roleLevel, vipLevel,
						missionType, mission, passed, duration);
			} else if (event.equals("player_get_exp")) {
				// 区服名
				String serverName = eventData.get("serverName");
				// 角色名
				String roleName = eventData.get("roleName");
				// 角色等级
				int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
				// vip等级
				int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
				// 经验来源
				String source = eventData.get("source");
				// 经验值
				int number = Integer.parseInt(eventData.get("number"));

				SDK.getInstance().getGameEventHelper().gainExp(serverName, roleName, roleLevel, vipLevel, source,
						number);

			} else if (event.equals("player_consume_exp")) {
				// 区服名
				String serverName = eventData.get("serverName");
				// 角色名
				String roleName = eventData.get("roleName");
				// 角色等级
				int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
				// vip等级
				int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
				// 消耗经验目的（用途）
				String destination = eventData.get("destination");
				// 经验值
				int number = Integer.parseInt(eventData.get("number"));
				SDK.getInstance().getGameEventHelper().consumeExp(serverName, roleName, roleLevel, vipLevel,
						destination, number);
			} else if (event.equals("role_delete")) {
				// 区服名
				String serverName = eventData.get("serverName");
				// 角色名
				String roleName = eventData.get("roleName");
				// 角色等级
				int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
				// vip等级
				int vipLevel = Integer.parseInt(eventData.get("vipLevel"));

				SDK.getInstance().getGameEventHelper().deleteRole(serverName, roleName, roleLevel, vipLevel);
			} else if (event.equals("role_offline")) {
				// 区服名
				String serverName = eventData.get("serverName");
				// 角色名
				String roleName = eventData.get("roleName");
				// 角色等级
				int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
				// vip等级
				int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
				// 本次在线时长（单位：秒）
				int onlineTime = Integer.parseInt(eventData.get("onlineTime"));
				// 登出场景
				String scene = eventData.get("scene");
				// 登出时所在场景的坐标
				String axis = eventData.get("axis");
				// 登出前角色最后一次操作或者完成的玩法
				String lastOperation = eventData.get("lastOperation");

				SDK.getInstance().getGameEventHelper().offlineRole(serverName, roleName, roleLevel, vipLevel,
						onlineTime, scene, axis, lastOperation);
			} else {
				System.out.println("不是支持的事件：" + event);
			}
		} catch (ArithmeticException e) {
			System.out.println("参数错误");
		}
		// 服务器上报的东西
		// else if(event.equals("player_item_gain")){
		// // 区服名
		// String serverName = eventData.get("serverName");
		// // 角色名
		// String roleName = eventData.get("roleName");
		// // 角色等级
		// int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
		// // vip等级
		// int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
		// // 道具来源
		// String source = eventData.get("source");
		// // 道具名称
		// String item = eventData.get("source");
		// // 获取数量
		// int number =Integer.parseInt(eventData.get("number"));
		//
		// SDK.getInstance().getGameEventHelper().gainItems(serverName,
		// roleName,
		// roleLevel, vipLevel, source, item, number);
		// }
		// else if(event.equals("player_item_consume")){
		// // 区服名
		// String serverName = eventData.get("serverName");
		// // 角色名
		// String roleName = eventData.get("roleName");
		// // 角色等级
		// int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
		// // vip等级
		// int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
		// // 使用场景
		// String destination = eventData.get("destination");
		// // 道具名称
		// String item = eventData.get("item");
		// // 消耗数量
		// int number = Integer.parseInt(eventData.get("number"));
		// SDK.getInstance().getGameEventHelper().useItems(serverName, roleName,
		// roleLevel, vipLevel, destination, item, number);
		// }
		// else if(event.equals("pay_for_item")){
		// // 区服名
		// String serverName = eventData.get("serverName");
		// // 角色名
		// String roleName = eventData.get("roleName");
		// // 角色等级
		// int roleLevel = Integer.parseInt(eventData.get("roleLevel"));
		// // vip等级
		// int vipLevel = Integer.parseInt(eventData.get("vipLevel"));
		// // 获取道具所在 APP 位置
		// String place = eventData.get("roleName");
		// // 虚拟币类型
		// String coinType = eventData.get("coinType");
		// // 消耗数量
		// int number = Integer.parseInt(eventData.get("number"));
		// // 道具名称
		// String item = eventData.get("item");
		// // 购买数量
		// int amount = Integer.parseInt(eventData.get("amount"));
		// // 玩家剩余虚拟币数量
		// int coins = Integer.parseInt(eventData.get("coins"));
		// SDK.getInstance().getGameEventHelper().consumeForItems(serverName,
		// roleName,
		// roleLevel, vipLevel, place, coinType, number, item, amount, coins);
		// }

	}

}
