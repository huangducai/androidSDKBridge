package com.gamefps.sdk.ysdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import com.tencent.ysdk.api.YSDKApi;
import com.tencent.ysdk.framework.common.BaseRet;
import com.tencent.ysdk.framework.common.ePlatform;
import com.tencent.ysdk.module.user.UserLoginRet;

import com.tencent.ysdk.api.YSDKApi;
import com.tencent.ysdk.framework.common.BaseRet;
import com.tencent.ysdk.framework.common.eFlag;
import com.tencent.ysdk.framework.common.ePlatform;
import com.tencent.ysdk.module.user.UserLoginRet;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import android.content.res.Configuration;
import com.gamefps.sdk.ysdk.JsonHttp;

public class YSDKAdapter implements IAuthenticationSDK, ISDKWarpper, IActivityOverrider, IAnalyticsSDK, IPaymentSDK {
	private final static SdkProviderInfo SDK_PROVIDER_INFO = new SdkProviderInfo("YSDK", "ysdk adapter", 1,true);
	public final static String TAG = "YSDKAdapter";
	public final String LOCAL_ACTION = "com.tencent.ysdkdemo";
	public static ProgressDialog mAutoLoginWaitingDlg;
    public static ProgressDialog mProgressDialog;

	private boolean _sdk_init_completed = false;
	public LocalBroadcastManager lbm;
	public BroadcastReceiver mReceiver;
	private int _roleLevel = -1;	
	protected static int platform = ePlatform.None.val();
	private SDKBridge bridgeCB=null;
	private String SDKopen_id = "";
	private String SDKopen_key = "";

	private String SDKpay_token;
	private String SDKpf;
	private String SDKpf_key;
	private String SDKaccount_type;
	
	private String _serverId = "";
	
	private String _productId ="";
	private String _amount;
	
	private Activity payActivity;
	
	private IPaymentCallback payCallback;
	
	@Override
	public boolean init(Activity act, SdkConfigInfo cfg) {
		return true;
	}

	@Override
	public void Login(Activity act, final SDKBridge cb) {
		bridgeCB = cb;
		int seletedType = 1;
		if (ePlatform.QQ.val() == seletedType) {
            if (getPlatform() == ePlatform.QQ) {
            	getUserInfoFromSid("qq");
                // 如已登录直接进入相应模块视图
            } else if (getPlatform() == ePlatform.None) {
            	YSDKApi.login(ePlatform.QQ);
                startWaiting(act);
            } else {

            }
        } else if (ePlatform.WX.val() == seletedType) {
            if (getPlatform() == ePlatform.WX) {
                // 如已登录直接进入相应模块视图
            	getUserInfoFromSid("wx");
            } else if (getPlatform() == ePlatform.None) {
            	YSDKApi.login(ePlatform.WX);
                startWaiting(act);
            } else {

            }
        } else {
            // 进行其它功能模块
        }
	}

	@Override
	public SdkProviderInfo getProviderInfo() {
		return SDK_PROVIDER_INFO;
	}

	@Override
	public void onDestroy(Activity act) {
        YSDKApi.onDestroy(act);
        Log.d(TAG, "onDestroy");

        if (lbm != null) {
            lbm.unregisterReceiver(mReceiver);
        }
	}

	@Override
	public void onCreate(Activity act, Bundle savedInstanceState) {
		
		
        YSDKApi.onCreate(act);
        YSDKApi.setUserListener(new YSDKCallback(this, act));
        YSDKApi.setBuglyListener(new YSDKCallback(this, act));
        
        // 设置局部广播，处理回调信息
        lbm = LocalBroadcastManager.getInstance(act.getApplicationContext());
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String result = intent.getExtras().getString("Result");
                Log.d(TAG,result);
            }
        };
        lbm.registerReceiver(mReceiver, new IntentFilter(LOCAL_ACTION));

        // 初始化下载进度条
        mProgressDialog = new ProgressDialog(act);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle("更新中");
        mProgressDialog.setMessage("下载进度");
        
        Log.d(TAG,"LoginPlatform is not Hall");
        YSDKApi.handleIntent(act.getIntent());
	}


	@Override
	public void onEnterLoginView(SDKBridge callback) {
		callback.enterLoginCallback("Y", "");
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
	}

	@Override
	public void onPause(Activity act) {
		YSDKApi.onPause(act);
	}

	@Override
	public void onResume(Activity act) {
		YSDKApi.onResume(act);
	}

	@Override
	public void onStop(Activity act) {
		YSDKApi.onStop(act);
	}

	@Override
	public void onRestart(Activity act) {
		YSDKApi.onRestart(act);
	}

	@Override
	public void setRoleInfo(Map<String, String> roleData) {

		
	}

	@Override
	public void onCreateRole(String serverId, String roleName, String roleId, int roleCreateTime) {
		
	}

	@Override
	public void onEnterGame(String serverId, String roleName) {
		_serverId = serverId + "";
	}

	@Override
	public void pay(Activity act, final PaymentInfo paymentInfo, final IPaymentCallback callback) {
		_productId = paymentInfo.productId;
		//_amount    = paymentInfo.amount + "";
		_amount    = 10 + "";
		payActivity = act;
		payCallback = callback;
		payRequest();
	}
	public void yRecharge(){
		YSDKApi.recharge("1", _amount, false, null, "ysdkExt" ,new YSDKCallback(this,payActivity) );
	}
	public void payRequest(){
		
		//yRecharge();
		try {
			String code_open_id     = URLEncoder.encode(SDKopen_id,"utf-8");
			String code_accessToken = URLEncoder.encode(SDKopen_key,"utf-8");
			
			String code_pay_token   = URLEncoder.encode(SDKpay_token,"utf-8");
			String code_pf          = URLEncoder.encode(SDKpf,"utf-8");
			String code_pf_key      = URLEncoder.encode(SDKpf_key,"utf-8");
			
			String url = "http://192.168.1.233:8080/cnc-web/cncweb/channels/yyb/api/paycallback.php?cncsdk=yyb&cnclang=zh-cn&account_type=" + SDKaccount_type + 
						 "&openid=" + code_open_id + "&openkey=" + code_accessToken + "&pay_token=" + code_pay_token + 
						 "&pf=" + code_pf + "&pfkey=" + code_pf_key + "&server_id=" + _serverId + 
						 "&game_productid=" + _productId + "&amt=" + _amount;
			JsonHttp.GetJsonFromUrlAsync(url, new CallbackListener() {
				@Override
				public void callback(int code, JSONObject val) {
					if (0 == code){
						try {
							int errCode = val.getInt("errCode");
							if(0==errCode){
								payCallback.OnPaymentResult(0,"payment completed");
							}else{
								yRecharge();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}else{
						payCallback.OnPaymentResult(SDKBridge.E_Payment_Failed,"充值失败");
					}
				}
				
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void callbackpayOk() {
		// TODO Auto-generated method stub
		payRequest();
		//payCallback.OnPaymentResult(0,"payment completed");
	}
	public void callbackpayFailed(String msg) {
		if(null != payCallback){
			payCallback.OnPaymentResult(SDKBridge.E_Payment_Failed,msg);
		}
	}


	@Override
	public void confirmExit(Activity ctx, final SDKBridge callback) {

	}
	@Override
	public void onActivityResult(Activity act, int requestCode, int resultCode, Intent data) {
        YSDKApi.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"onActivityResult");
	}

	@Override
	public void onNewIntent(Activity act, Intent intent) {
        Log.d(TAG,"onNewIntent");
        // TODO GAME 处理游戏被拉起的情况
        Log.d(TAG,"LoginPlatform is not Hall");
        YSDKApi.handleIntent(intent);
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
	
	//-----------回调-----------
	// TODO GAME 在异账号时，模拟游戏弹框提示用户选择登陆账号
    public void showDiffLogin(final Activity act) {
    	act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(act);
                builder.setTitle("异账号提示");
                builder.setMessage("你当前拉起的账号与你本地的账号不一致，请选择使用哪个账号登陆：");
                builder.setPositiveButton("本地账号",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                showToastTips(act,"选择使用本地账号");
                                if (!YSDKApi.switchUser(false)) {
                                    letUserLogout();
                                }
                            }
                        });
                builder.setNeutralButton("拉起账号",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                showToastTips(act, "选择使用拉起账号");
                                if (!YSDKApi.switchUser(true)) {
                                    letUserLogout();
                                }
                            }
                        });
                builder.show();
            }
        });

    }

    // 平台授权成功,让用户进入游戏. 由游戏自己实现登录的逻辑
    public void letUserLogin(final Activity act) {
        UserLoginRet ret = new UserLoginRet();
        YSDKApi.getLoginRecord(ret);
        Log.d(TAG,"flag: " + ret.flag);
        Log.d(TAG,"platform: " + ret.platform);
        if (ret.ret != BaseRet.RET_SUCC) {
            showToastTips(act,"UserLogin error!!!");
            Log.d(TAG,"UserLogin error!!!");
            letUserLogout();
            return;
        }
        if (ret.platform == ePlatform.PLATFORM_ID_QQ) {
        	SDKopen_id     = ret.open_id;
        	SDKopen_key    = ret.getAccessToken();
        	SDKpay_token   = ret.getPayToken();
        	SDKpf          = ret.pf;
        	SDKpf_key      = ret.pf_key;
        	SDKaccount_type = "qq";
        	getUserInfoFromSid("qq");
        } else if (ret.platform == ePlatform.PLATFORM_ID_WX) {
        	SDKopen_id     = ret.open_id;
        	//手Q登陆时传手Q登陆回调里获取的paytoken值，微信登陆时传微信登陆回调里获取的传access_token值
        	SDKopen_key    = ret.getAccessToken();
        	SDKopen_id     = ret.open_id;
        	SDKopen_key    = ret.getAccessToken();
        	SDKpay_token   = ret.getPayToken();
        	SDKpf          = ret.pf;
        	SDKpf_key      = ret.pf_key;
        	SDKaccount_type = "wx";
        	getUserInfoFromSid("wx");
        }
    }
	private void getUserInfoFromSid(final String account_type){
		final IAuthenticationSDK pThis = this;
		final SDKBridge cb = bridgeCB;
		
		if ( null == cb || SDKopen_id.equals("") ||  SDKopen_key.equals("") )
			return;
		final String open_id     = SDKopen_id;
		final String open_key    = SDKopen_key;
		try {
			String code_open_id     = URLEncoder.encode(open_id,"utf-8");
			String code_open_key = URLEncoder.encode(open_key,"utf-8");
			String url = "http://221.236.172.156:23580/cnc/cncweb/api/sdklogin.php?cncsdk=yyb&cnclang=zh-cn&account_type=" + account_type + "&openid=" + code_open_id + "&openkey=" + code_open_key;
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
								ai.accountId        = userInfo.getString("accountId");
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
	

    // 登出后, 更新view. 由游戏自己实现登出的逻辑
    public void letUserLogout() {
    	YSDKApi.logout();
    }


    public void startWaiting(final Activity act) {
    	act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"startWaiting");
                mAutoLoginWaitingDlg = new ProgressDialog(act);
                stopWaiting(act);
                mAutoLoginWaitingDlg.setTitle("登录中...");
                mAutoLoginWaitingDlg.show();
            }
        });

    }

    public void stopWaiting(final Activity act) {
    	act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"stopWaiting");
                if (mAutoLoginWaitingDlg != null && mAutoLoginWaitingDlg.isShowing()) {
                    mAutoLoginWaitingDlg.dismiss();
                }
            }
        });

    }

    public void showToastTips(Activity act, String tips) {
        Toast.makeText(act,tips,Toast.LENGTH_LONG).show();
    }

    // 获取当前登录平台
    public ePlatform getPlatform() {
        UserLoginRet ret = new UserLoginRet();
        YSDKApi.getLoginRecord(ret);
        if (ret.flag == eFlag.Succ) {
            return ePlatform.getEnum(ret.platform);
        }
        return ePlatform.None;
    }
    
 // ***********************界面布局相关*************************
    public void sendResult(String result) {
        if(lbm != null) {
            Intent sendResult = new Intent(LOCAL_ACTION);
            sendResult.putExtra("Result", result);
            Log.d(TAG,"send: "+ result);
            lbm.sendBroadcast(sendResult);
        }
    }
	@Override
	public void logout() {
	}
	
	@Override
	public void onGameCheckVersionBegin() {
	}

	@Override
	public void onGameCheckVersionEnd() {
	}





}