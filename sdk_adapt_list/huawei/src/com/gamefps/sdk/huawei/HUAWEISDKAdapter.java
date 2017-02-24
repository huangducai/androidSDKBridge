package com.gamefps.sdk.huawei;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.huawei.pay.plugin.PayParameters;
import com.android.huawei.pay.util.HuaweiPayUtil;
import com.android.huawei.pay.util.Rsa;
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
import com.huawei.gameservice.sdk.GameServiceSDK;
import com.huawei.gameservice.sdk.api.GameEventHandler;
import com.huawei.gameservice.sdk.api.PayResult;
import com.huawei.gameservice.sdk.api.Result;
import com.huawei.gameservice.sdk.api.UserResult;
import com.huawei.gameservice.sdk.util.StringUtil;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;



public class HUAWEISDKAdapter implements  IAuthenticationSDK, ISDKWarpper,IActivityOverrider,IAnalyticsSDK, IPaymentSDK {
	
	private final static SdkProviderInfo SDK_PROVIDER_INFO = new SdkProviderInfo("HUAWEI", "HUAWEI sdk adapter",1,true);
	public final static String TAG = "HUAWEISDKAdapter";
	
	private boolean _sdk_init_completed = false; 
	private SDKBridge bridgeCB=null;
	
	private Activity _activity;
	private Handler uiHandler = null;
	private IPaymentCallback m_callback;
	
	private String m_account;
	
	@Override
	public boolean init(Activity ctx, SdkConfigInfo cfg) {
		
		Log.d(TAG, "进入init()er..........................");
		_activity = ctx;
		uiHandler = new Handler(ctx.getMainLooper()){
			@Override
    		public void handleMessage(Message msg) {
    			if (msg.getData() == null) {
    				return;
    			}
    			String errorMsg = msg.getData().getString("errorMsg");
    			if (!StringUtil.isNull(errorMsg)) {
    				Toast.makeText(_activity, errorMsg, Toast.LENGTH_LONG)
    						.show();
    			}
    		}
		};
		
		initSdk();
		return true;
	}
	
	private void checkUpdate()
    {
		GameServiceSDK.checkUpdate(_activity, new GameEventHandler(){
        	
			@Override
			public void onResult(Result result) {
				if(result.rtnCode != Result.RESULT_OK){
					handleError("check update failed:" + result.rtnCode);
				}
			}
			
			@Override
			public String getGameSign(String appId, String cpId, String ts){
				return createGameSign(appId+cpId+ts);
			}
			
		});
	}
	
	protected void initSdk()
	{
		GameServiceSDK.init(_activity, GlobalParam.APP_ID, GlobalParam.PAY_ID, "com.gamefps.cnc.huawei.installnewtype.provider", new GameEventHandler(){
			@Override
			public void onResult(Result result) {
				if(result.rtnCode != Result.RESULT_OK){
					handleError("init the game service SDK failed:" + result.rtnCode);
					return;
				}
//                login(1);
				_sdk_init_completed= true;
			}
			
			@Override
			public String getGameSign(String appId, String cpId, String ts){
				return createGameSign(appId+cpId+ts);
			}
			
		});
	}
	
	@Override
	public void onCreate(Activity act, Bundle savedInstanceState) {
		Log.d(TAG, "进入onCreate()..........................");
		
	}
	
	
	@Override
	public void Login(Activity act, SDKBridge cb) {
		if(!_sdk_init_completed)
		{
			Log.e(TAG, "call login before sdk init");
			cb.OnAuthenticationFailed(this, "sdk not init");
			return ;
		}
		bridgeCB = cb;
		final IAuthenticationSDK pThis = this;
		Log.d(TAG, "huawei sdk login begin");
		GameServiceSDK.login(act, new GameEventHandler(){
        	
			@Override
			public void onResult(Result result) {
				
                if (result.rtnCode != Result.RESULT_OK)
                {
                	handleError("login failed:" + result.toString());
				}else {
					UserResult userResult = (UserResult) result;
                    if(userResult.isAuth != null)
                    {
                    	if(userResult.isAuth == 1)
                    	{
                    		boolean checkResult = checkSign(GlobalParam.APP_ID + userResult.ts + userResult.playerId, userResult.gameAuthSign);
                            if (checkResult)
                            {
                            	getUserInfoFromSid(userResult);
                            }else{
                                handleError("login auth failed check game auth sign error");
                            }
                    	}else{
//                    		handleError("isAuth == 0");
//                    		getUserInfoFromSid(userResult);
                    	}
                        
                    }else if(userResult.isChange != null && userResult.isChange == 1){
                    	if(bridgeCB!=null)
                    	{
                    		bridgeCB.OnLogout();
                    	}
                    }
                    else
                    {
                        handleError("login success:" + userResult.toString());
//                    	getUserInfoFromSid(userResult);
                    }
				    

				}
			}
			
			@Override
			public String getGameSign(String appId, String cpId, String ts){
				return createGameSign(appId+cpId+ts);
			}
			
		}, 1);
	}
	
	private void getUserInfoFromSid(final UserResult result ){
		
		final IAuthenticationSDK pThis = this;
		final SDKBridge cb = bridgeCB;
		
		//String url = "http://test.gw.cnc.173fun.com/cnc/cncweb/api/sdklogin.php?cncsdk=uc&cnclang=zh-cn&sid=" + esid;
		try {
			String esid = "playerId="+URLEncoder.encode(result.playerId,"utf-8")+"&isAuth="+URLEncoder.encode(result.isAuth.toString(),"utf-8")+"&ts="+URLEncoder.encode(result.ts,"utf-8")+"&sign="+URLEncoder.encode(result.gameAuthSign,"utf-8")
							+"&displayName="+URLEncoder.encode(result.displayName,"utf-8");
			String url = "http://221.236.172.156:23580/cnc/cncweb/api/sdklogin.php?cncsdk=huawei&cnclang=zh-cn&" + esid;
			JsonHttp.GetJsonFromUrlAsync(url, new CallbackListener() {
				@Override
				public void callback(int code, JSONObject val) {
					if (0 == code){
						try {
							int errCode = val.getInt("errCode");
							if(0==errCode){
								LoginAccountInfo ai = new LoginAccountInfo();
								JSONObject userInfo = val.getJSONObject("retVal");
								ai.channelPrefix    = userInfo.getString("channelPrefix");
								ai.channelAccountId = userInfo.getString("channelAccountId");
								m_account 			= userInfo.getString("accountId");
								ai.accountId        = m_account;
								ai.loginTime        = userInfo.getString("time");
								ai.isNewAccount     = userInfo.getBoolean("isNewAccount");
								ai.token            = userInfo.getString("token");
								
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
	
	private GameEventHandler payHandler = new GameEventHandler()
    {
		@Override
		public String getGameSign(String appId, String cpId, String ts) {
			return createGameSign(appId+cpId+ts);
		}
		
        @Override
        public void onResult(Result result)
        {
            Map<String, String> payResp = ((PayResult)result).getResultMap();
            // 支付成功，进行验签
            // payment successful, then check the response value
            if ("0".equals(payResp.get(PayParameters.returnCode)))
            {
                if ("success".equals(payResp.get(PayParameters.errMsg)))
                {
                    // 支付成功，验证信息的安全性；待验签字符串中如果有isCheckReturnCode参数且为yes，则去除isCheckReturnCode参数
                	// If the response value contain the param "isCheckReturnCode" and its value is yes, then remove the param "isCheckReturnCode".
                	if (payResp.containsKey("isCheckReturnCode") && "yes".equals(payResp.get("isCheckReturnCode")))
                    {
                        payResp.remove("isCheckReturnCode");
                        
                    }
                	// 支付成功，验证信息的安全性；待验签字符串中如果没有isCheckReturnCode参数活着不为yes，则去除isCheckReturnCode和returnCode参数
                	// If the response value does not contain the param "isCheckReturnCode" and its value is yes, then remove the param "isCheckReturnCode".
                	else
                    {
                        payResp.remove("isCheckReturnCode");
                        payResp.remove(PayParameters.returnCode);
                    }
                    // 支付成功，验证信息的安全性；待验签字符串需要去除sign参数
                	// remove the param "sign" from response
                    String sign = payResp.remove(PayParameters.sign);
                    
                    String noSigna = HuaweiPayUtil.getSignData(payResp);
                    
                    // 使用公钥进行验签
                    // check the sign using RSA public key
                    boolean s = Rsa.doCheck(noSigna, sign, GlobalParam.PAY_RSA_PUBLIC);
                    
                    if (s)
                    {
//                        pay = getString(R.string.pay_result_success);
                        m_callback.OnPaymentResult(0, "支付成功");
                    }
                    else
                    {
//                        pay = getString(R.string.pay_result_check_sign_failed);
                        m_callback.OnPaymentResult(0, "支付成功，但签名校验失败");
                    }
                }
               
            }
            else if ("30002".equals(payResp.get(PayParameters.returnCode)))
            {
                m_callback.OnPaymentResult(30002, "支付超时");
            }
            
        }
    };
	
    
	@Override 
	public void pay(Activity act, PaymentInfo paymentInfo, IPaymentCallback callback)
	{
		if(!_sdk_init_completed){
			callback.OnPaymentResult(-1,"sdk not init");
			return;
		}
		m_callback = callback;
		DecimalFormat  fnum = new DecimalFormat("##0.00");  
		String price = fnum.format(paymentInfo.amount/100);
//		price="0.01";
        String productName =paymentInfo.productName;
        String productDesc = paymentInfo.productDesc;
        String requestId = String.valueOf(System.currentTimeMillis())+"-"+paymentInfo.characterId;
        
        String productInfo=m_account+"|"+String.valueOf(paymentInfo.serverId)+"|"+paymentInfo.productId;
        // 调用公共方法进行支付
        // call the pay method
        GameBoxUtil.startPay(act, price, productName, productDesc, requestId,productInfo, payHandler, null);
	}
	@Override
	public void setRoleInfo(Map<String, String> roleData) {
		Log.d(TAG, "进入RoleInfo()..........................");
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
	public void onDestroy(Activity act) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onPause(Activity act) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onResume(Activity act) {
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
	public void confirmExit(Activity ctx, SDKBridge callback) {
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
//		Intent goInGame = new Intent(_activity, _activity.class);
//        startActivity(goInGame);
	}
	@Override
	public SdkProviderInfo getProviderInfo() {
		return SDK_PROVIDER_INFO;
	}
	
    protected boolean checkSign(String data, String gameAuthSign)
    {

    	/*
         * 建议CP获取签名后去游戏自己的服务器校验签名
         */
    	/*
         * The CP need to deployed a server for checking the sign.
         */
        try
        {
            return RSAUtil.verify(data.getBytes("UTF-8"), GlobalParam.LOGIN_RSA_PUBLIC, gameAuthSign);
        }
        catch (Exception e)
        {
            return false;
        }
    }
	
	/**
	 * 生成游戏签名
	 * generate the game sign
	 */
	private String createGameSign(String data){
		
		String str = data;
		try {
			String result = RSAUtil.sha256WithRsa(str.getBytes("UTF-8"), GlobalParam.BUOY_SECRET);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	private void handleError(String errorMsg) {
		Message msg = new Message();
		Bundle data = new Bundle();
		String strMsg = errorMsg;
		data.putString("errorMsg", strMsg);
		msg.setData(data);
		uiHandler.sendMessage(msg);
	}
	
}
