package com.gamefps.sdkbridge;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import com.gamefps.sdkbridge.IAnalyticsSDK.QuestState;
import com.gamefps.sdkbridge.notification.LocalNotification;
import com.gamefps.sdkbridge.notification.NotificationItem;
import com.gamefps.sdkbridge.PushSdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

/**
 * Created by lvyou on 2016/1/3.
 */
public class SDKBridge implements IPaymentCallback,IShareCallback,IAppInviteCallback,IQueryAccountBindingsCallback,IAccountBindCallback {
	final static String LOG_TAG = "SDKBridge";
	
	public final static int S_Canceled = 100;
	public final static int S_OK = 0;
	public final static int E_Failed = -1;
	public final static int E_Authentication_Failed = -100;
	public final static int E_Payment_Failed = -200;
	
	
	final static String SYS_EVENT_RoleCreate = "RoleCreate";
	final static String SYS_EVENT_EnterGame = "EnterGame";
	
	
	final static String SYS_EVENT_AccountCreate = "AccountCreate";
	final static String SYS_EVENT_AccountLogin = "AccountLogin";
	
	final static String SYS_EVENT_QuestUpdate = "QuestUpdate";
	final static String SYS_EVENT_CurrencyConsume = "CurrencyConsume";
	final static String SYS_EVENT_CurrencyGain = "CurrencyGain";

	
	final static int RESULT_ID_PICK_PHOTO = 5282411;
	
//	public enum ErrorCode{
//		E_OK,
//		E_No_Suitable_Plugin,
//		E_Plugin_Not_Avaliable,
//		E_Authentication_Failed,
//	}
//	
	
	
	static SDKBridge _instance;
	Activity appContext = null;
	int _notificationIconResId;
	Class<? extends Activity> _appActivityClass;
//	private Map<String,ISDKWarpper> _sdk_obj_by_name;
	private Map<String,ISDKWarpper> _sdk_obj_by_Id;
	
	private List<IAnalyticsSDK> _analytics_sdk_list;
	private List<IAuthenticationSDK> _authentication_sdk_list;
	private List<IPaymentSDK> _payment_sdk_list;
	private List<IActivityOverrider> _activity_event_handlers;
	private List<ISnsShareSDK> _share_sdk_list;
	private List<ISnsAppInviteSDK> _invite_sdk_list;
	
	private List<PushSdk> _push_sdk_list;
	
	private SDKBridge() {
		super();
	}
	public static SDKBridge getInstance(){
		if(null == _instance)
			_instance = new SDKBridge();
		return _instance;
	}
	
	

	
	
	static void InvokeOnUIThread(final String fnName ,final Object[] args){
		getInstance();
		
		//Log.d("SDKBridge", "InvokeOnUIThread2");
		Method methodToInvoke = null;
		
		Class<?> argTypes[] = new Class[args.length];
		for(int i=0;i<args.length;++i){
			Object arg = args[i];
			Class<?> temp = arg.getClass();
			
			
			if(arg.getClass() == java.lang.Integer.class){
				argTypes[i] = int.class;
			}else if( temp == java.util.TreeMap.class ){
				argTypes[i] = java.util.Map.class;
			}else{
				argTypes[i] = args[i].getClass();	
			}
			
		}
		
		/*
		for(Method m : _instance.getClass().getDeclaredMethods()){
			//if(m.getName()=="OnInit"){
				Log.d("PARAM","---BEGNIN---:" + m.getName());	
				Class<?>[] params = m.getParameterTypes();
				for(Class<?> c:params){
					Log.d("PARAM", c.getName());	
				}
				Log.d("PARAM","---END---");
			//}
		}
		*/
		

		try {
			methodToInvoke = _instance.getClass().getDeclaredMethod(fnName, argTypes);
		} catch (NoSuchMethodException e1) {
			String signature = "";
			for(Class<?> c:argTypes){
				signature += c.getSimpleName() + ",";
			}
			Log.e("SDKBridge", "[InvokeOnUIThread]:SDKBridge method not found on:" + fnName +"(" + signature + ")" );
		}
		
		 if(null!=methodToInvoke){
			 final Method unmutableInvoke = methodToInvoke;
			 Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						//Log.d("SDKBridge", "unmutableInvoke.invoke_begin");
						unmutableInvoke.invoke(_instance, args);
						//Log.d("SDKBridge", "unmutableInvoke.invoke_end");
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			};
			Activity act = (Activity)org.cocos2dx.lib.Cocos2dxActivity.getContext();
			act.runOnUiThread(r);
		 }
		  
	}
	
	
	
    void OnInit(){
    	//Toast.makeText(appContext, "On SDK Init!",Toast.LENGTH_LONG).show();
    }



	private void initSDKMoules() {
		Bundle metaData;
		ApplicationInfo appInfo;
		try {
			appInfo = appContext.getPackageManager().getApplicationInfo(appContext.getPackageName()  , PackageManager.GET_META_DATA);
			metaData = appInfo.metaData;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		ArrayList<ISDKWarpper> sdk_order_by_priority = new ArrayList<ISDKWarpper>(_sdk_obj_by_Id.size());
		sdk_order_by_priority.addAll(_sdk_obj_by_Id.values());
		Collections.sort(sdk_order_by_priority,new Comparator<ISDKWarpper>() {
			@Override
			public int compare(ISDKWarpper o1, ISDKWarpper o2) {
				return o2.getProviderInfo().priority - o1.getProviderInfo().priority;
			}
		});
		
		String channelId;
		try {
			InputStream istm = appContext.getAssets().open("game_channel_id.txt");
			InputStreamReader rdr = new InputStreamReader(istm);
			BufferedReader br = new BufferedReader(rdr);
			channelId = br.readLine();
			br.close();
			rdr.close();
			istm.close();
		} catch (IOException e) {
			e.printStackTrace();
			channelId= "";
		}
		
		
		for(ISDKWarpper sdk:  sdk_order_by_priority){
			SdkConfigInfo cfg = new SdkConfigInfo();
			cfg.channelId = channelId;
			//cfg.appVer = 2016050601.0;
			cfg.metaData = new Bundle();
			
			final String argKeyPrefix = "SDK_ARG_KEY_"+  sdk.getProviderInfo().Id +"_";
			final int argKeyPrefixLen = argKeyPrefix.length();
			//  闂佺尨鎷烽柛顐ｆ礃閼茬娀鏌熺紒妯虹瑐婵炲棎鍨藉Λ渚�鏁撻挊澹囧磼濠婂嫮鏆犻梺鍛婄懃閸婂綊寮抽敓锟�
			for(String k: metaData.keySet()){
	    		if(k.startsWith(argKeyPrefix)){
	    			String sdkKey = k.substring(argKeyPrefixLen);
	    			String metaKey = metaData.getString(sdkKey);
	    			String metaVal = metaData.getString(metaKey);
	    			cfg.metaData.putString(sdkKey,metaVal);
	    		}
			}
			
			
			if(sdk.init(appContext,cfg)){
				if(sdk instanceof IAnalyticsSDK){
					_analytics_sdk_list.add((IAnalyticsSDK)sdk);
				}
				if(sdk instanceof IAuthenticationSDK){
					_authentication_sdk_list.add((IAuthenticationSDK)sdk);
				}
				if(sdk instanceof IActivityOverrider){
					_activity_event_handlers.add((IActivityOverrider)sdk);
				}
				if(sdk instanceof IPaymentSDK){
					_payment_sdk_list.add((IPaymentSDK)sdk);
				}

				if(sdk instanceof ISnsShareSDK){
					_share_sdk_list.add((ISnsShareSDK)sdk);
				}
				
				if(sdk instanceof ISnsAppInviteSDK){
					_invite_sdk_list.add((ISnsAppInviteSDK)sdk);
				}
				//增加 push 的接口
				if(sdk instanceof PushSdk){
					_push_sdk_list.add((PushSdk)sdk);
				}
			}
		}
	}



	private void findAvaliableSDK() {
		if(null != _sdk_obj_by_Id){
			Log.e(LOG_TAG, "[OnInit] cannot be invoked more than once!");
		}
		_sdk_obj_by_Id = new TreeMap<String, ISDKWarpper>();
		_analytics_sdk_list = new ArrayList<IAnalyticsSDK>();
		_authentication_sdk_list = new ArrayList<IAuthenticationSDK>();
		_payment_sdk_list = new ArrayList<IPaymentSDK>();
		_activity_event_handlers = new ArrayList<IActivityOverrider>();
		_share_sdk_list = new ArrayList<ISnsShareSDK>();
		_invite_sdk_list = new ArrayList<ISnsAppInviteSDK>();
		
		_push_sdk_list =  new ArrayList<PushSdk>();
		
		final String SDK_ENTRANC = "SDK_ENTRANCE_";
		
		Bundle metaData;
    	try {
			ApplicationInfo appInfo =  appContext.getPackageManager().getApplicationInfo(appContext.getPackageName()  , PackageManager.GET_META_DATA);
			metaData = appInfo.metaData;
	    	for(String k: metaData.keySet()){
	    		if(k.startsWith(SDK_ENTRANC)){
	    			final String sdkName = k.substring(SDK_ENTRANC.length());
	    			final String className = metaData.getString(k);
	    			try {
						Class<?> cls = Class.forName(className);
						Object sdkObj = cls.newInstance();
						ISDKWarpper sdkModule = (ISDKWarpper)sdkObj;
						if(null!=sdkModule){
							SdkProviderInfo info = sdkModule.getProviderInfo();
							if(!info.Id.equals(sdkName))
								throw new ParserConfigurationException("SDK configuration name does not match: name in xml:" + sdkName + " name in code:" + info.Id );
							_sdk_obj_by_Id.put(info.Id, sdkModule);
						}
							
					} catch (ClassNotFoundException e) {
						Log.e(LOG_TAG, "cannot find class(ClassNotFoundException):" + className + " for sdk:" + sdkName);
					} catch (InstantiationException e) {
						Log.e(LOG_TAG, "cannot create instance for class(InstantiationException):" + className + " for sdk:" + sdkName);
					} catch (IllegalAccessException e) {
						Log.e(LOG_TAG, "cannot create instance for class(IllegalAccessException):" + className + " for sdk:" + sdkName);
					} catch (ParserConfigurationException e) {
						Log.e(LOG_TAG, "cannot create instance for class(ParserConfigurationException):" + className + " for sdk:" + sdkName + " msg:" + e.getMessage());
					}
	    		}
	    	}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
    public void OnCreate(Activity act,Bundle savedInstanceState,int iconId){
    	if(null == appContext){
        	appContext = act;
        	_notificationIconResId = iconId;
        	_appActivityClass = act.getClass();
        	findAvaliableSDK();
        	initSDKMoules();
    	}
    	
    	for(IActivityOverrider handler :  _activity_event_handlers){
    		handler.onCreate(act, savedInstanceState);
    	}
    	
    }
    
    public void OnActivityPause(Activity act){
    	for(IActivityOverrider sdk:_activity_event_handlers){
    		sdk.onPause(act);
    	}
    }
    public void OnActivityResume(Activity act){
    	for(IActivityOverrider sdk:_activity_event_handlers){
    		sdk.onResume(act);
    	}
    }
    public void OnActivityDestroy(Activity act){
    	for(IActivityOverrider sdk:_activity_event_handlers){
    		sdk.onDestroy(act);
    	}
    }
    public void OnActivityStop(Activity act){
    	for(IActivityOverrider sdk:_activity_event_handlers){
    		sdk.onStop(act);
    	}
    }
    public void OnActivityRestart(Activity act){
    	for(IActivityOverrider sdk:_activity_event_handlers){
    		sdk.onRestart(act);
    	}
    }
    public void OnActivityStart(Activity act){
    	for(IActivityOverrider sdk:_activity_event_handlers){
    		sdk.onStart(act);
    	}
    }
    public void OnActivitySaveInstanceState(Activity act, Bundle outState){
    	for(IActivityOverrider sdk:_activity_event_handlers){
    		sdk.onSaveInstanceState(act, outState);
    	}
    }
    public void OnActivityConfigurationChanged(Activity act, Configuration newConfig){
    	for(IActivityOverrider sdk:_activity_event_handlers){
    		sdk.onConfigurationChanged(act, newConfig);
    	}
    }
    
    public void OnActivityNewIntent(Activity act,Intent intent){
    	for(IActivityOverrider sdk:_activity_event_handlers){
    		sdk.onNewIntent(act,intent);
    	}
    }
    public void onActivityResult(Activity act, int requestCode, int resultCode, Intent data){
    	if(RESULT_ID_PICK_PHOTO == requestCode){
    		Log.d(LOG_TAG, "pickMedia:resultCode:" + Integer.toString(resultCode) );
    		 if(resultCode != Activity.RESULT_CANCELED){
    			 Log.d(LOG_TAG, "pickMedia:onActivityResult:OK");
    			 final Uri imageUri = data.getData();
    			 try {
					final InputStream imageStream = appContext.getContentResolver().openInputStream(imageUri);
					int fileLen = imageStream.available();
					byte[] imageData = new byte[fileLen];
					imageStream.read(imageData);
					imageStream.close();
					OnPickMediaResult(imageData);
					return;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		 }else{
    			 Log.d(LOG_TAG, "pickMedia:onActivityResult:fail");
    		 }
    		 
    		 OnPickMediaResult(null);
    		 
    	}else{
        	for(IActivityOverrider sdk:_activity_event_handlers){
        		sdk.onActivityResult(act, requestCode, resultCode, data);
        	}
    	}
    }

    void setRoleInfo( Map<String,String> eventData ){
    	for(IAnalyticsSDK sdk:_analytics_sdk_list){
    		sdk.setRoleInfo(eventData);
    	}
    	
    //	String roleName = eventData.get("Name");
    	
    	
    	//Toast.makeText(appContext, "On SDK setRoleInfo:" + roleName, Toast.LENGTH_SHORT).show();   
    }
    
    void logSysEvent(String eventName,Map<String,String> eventData){
    	//Toast.makeText(appContext, "On SDK logSysEvent:" + eventName,Toast.LENGTH_SHORT).show();  
    	
    	if(SYS_EVENT_EnterGame.equals(eventName)){
    		String ServerId = eventData.get("ServerId");
    		String roleName = eventData.get("RoleName");
        	for(IAnalyticsSDK sdk:_analytics_sdk_list){
        		sdk.onEnterGame(ServerId,roleName);
        	}
    	}else if(SYS_EVENT_RoleCreate.equals(eventName)){
    		String serverId = eventData.get("ServerId");
    		String roleName = eventData.get("RoleName");
    		String roleId = eventData.get("RoleId");
    		int roleCreateTime = Integer.parseInt(eventData.get("RoleCreateTime"));
    		
        	for(IAnalyticsSDK sdk:_analytics_sdk_list){
        		sdk.onCreateRole(serverId,roleName, roleId, roleCreateTime);
        	}
    	}else if(SYS_EVENT_AccountCreate.equals(eventName)){
    		String account = eventData.get("Account") ;
        	for(IAnalyticsSDK sdk:_analytics_sdk_list){
        		sdk.onCreateAccount(account,eventData);
        	}
    	}else if(SYS_EVENT_AccountLogin.equals(eventName)){
    		String account = eventData.get("Account") ;
        	for(IAnalyticsSDK sdk:_analytics_sdk_list){
        		sdk.onLoginAccount(account,eventData);
        	}
    	}else if(SYS_EVENT_QuestUpdate.equals(eventName)){
    		String questId = eventData.get("QuestId");
    		String questType = eventData.get("QuestType");
    		String status = eventData.get("Status");
    		String comment = eventData.get("Comment");
    		
    		QuestState qs;
    		if(status.equals("Accept")){
    			qs = QuestState.QS_Start;
    		}else if(status.equals("Complete")){
    			qs = QuestState.QS_Complete;
    		}else if(status.equals("Fail")){
    			qs = QuestState.QS_Fail;
    		}else{
    			Log.e(LOG_TAG, "[logSysEvent]:unkonwn quest status:" + status);
    			return;
    		}
        	for(IAnalyticsSDK sdk:_analytics_sdk_list){
        		sdk.onQuestUpdate(questType, questId, qs, comment);
        	}
    	}else if(SYS_EVENT_CurrencyGain.equals(eventName)){
    		String cType = eventData.get("Type");
    		String cReason = eventData.get("Reason");
    		int cAmount = Integer.parseInt(eventData.get("Amount"));
        	for(IAnalyticsSDK sdk:_analytics_sdk_list){
        		sdk.onCurrencyGain(cType, cReason, cAmount);
        	}
    	}else if(SYS_EVENT_CurrencyConsume.equals(eventName)){
    		String cType = eventData.get("Type");
    		String cReason = eventData.get("Reason");
    		int cAmount = Integer.parseInt(eventData.get("Amount"));
        	for(IAnalyticsSDK sdk:_analytics_sdk_list){
        		sdk.onCurrencyConsume(cType, cReason, cAmount);
        	}
    	}else{
    		Log.e(LOG_TAG, "[logSysEvent]:unkonwn sys event:" + eventName);
    	}
    }
    
    void logCustomEvent(String eventName,Map<String,String> eventData){
    	//Toast.makeText(appContext, "On SDK logCustomEvent:" + eventName,Toast.LENGTH_SHORT).show();  
    	for(IAnalyticsSDK sdk:_analytics_sdk_list){
    		sdk.logEvent(eventName, eventData);
    	}
    }

	void AccountLogin(String pluginId){
		
		//Toast.makeText(appContext, "On SDK AccountLogin",Toast.LENGTH_SHORT).show();  

		IAuthenticationSDK plugin = null;
		
		if(null != pluginId && !pluginId.isEmpty()){
			ISDKWarpper sdk = _sdk_obj_by_Id.get(pluginId);
			if(sdk instanceof IAuthenticationSDK){
				plugin = (IAuthenticationSDK) sdk;
			}else{
				Toast.makeText(appContext, "On SDK AccountLogin: Authentication SDK ID=["+ pluginId +"] not found!",Toast.LENGTH_LONG).show();
				OnAuthenticationFailed(null,"Authentication SDK ID=["+ pluginId +"] not found!");
				return;
			}
		}
		
		if(null == plugin){	// 濠电偛澶囬崜婵嗭耿娓氾拷楠炴劙宕惰閺嗕即鏌熺紒妯虹瑐婵炲棎鍨介弫宥囦沪缁涘娈洪梺鍦暬缁犳牜鍒掗婊勫闁靛牆娲﹂悾锟�
			if(!_authentication_sdk_list.isEmpty())
				plugin = _authentication_sdk_list.get(0);
		}
		
		if(null==plugin){	// 婵炲濮寸粔鍫曞礉瑜旈獮宥夊矗閵壯呮喒闂佸憡甯掓惔婊呮濠靛绠柕澶嗘櫆閺咃拷
			Toast.makeText(appContext, "On SDK AccountLogin: no authentication sdk found!",Toast.LENGTH_LONG).show();
			OnAuthenticationFailed(null,"there is no Authentication SDK plugged!");
			return;
		}
		//Log.d(LOG_TAG, "login plugin prefix:" + plugin.getAccountPrefix());
		plugin.Login(appContext, this);

    }
	
	void Pay(Map<String,String> paymentInfo){
		
		//Toast.makeText(appContext, "On SDK AccountLogin",Toast.LENGTH_SHORT).show();  

		IPaymentSDK plugin = null;
		if(!_payment_sdk_list.isEmpty())
			plugin = _payment_sdk_list.get(0);
		
		if(null==plugin){	// 婵炲濮寸粔鍫曞礉瑜旈獮宥夊矗閵壯呮喒闂佸憡甯掓惔婊呮濠靛绠柕澶嗘櫆閺咃拷
			Toast.makeText(appContext, "On SDK Pay: no payment sdk found!",Toast.LENGTH_LONG).show();
			OnPayResult(E_Payment_Failed,"there is no payment SDK plugged!");
			return;
		}
		
		
		PaymentInfo pi = new PaymentInfo();
		pi.amount = Float.parseFloat(paymentInfo.get("Amount"));
		pi.currencyUnit = "CNY";
		pi.serverId = Integer.parseInt( paymentInfo.get("ServerId") );
		pi.universe = "YZ";
		pi.characterId = paymentInfo.get("PlayerId");
		pi.playerAccountId = Integer.parseInt(paymentInfo.get("PlayerAccountId"));
		
		pi.productId = paymentInfo.get("ProductId");
		pi.productName = paymentInfo.get("ProductName");
		pi.productDesc = paymentInfo.get("ProductDesc");
		pi.pointName = "yuanbao";
		pi.orderTitle = "OrderTitle1";
		
		pi.serverName    = paymentInfo.get("ServerName");
		pi.characterName = paymentInfo.get("CharacterName");
		
		
		
		plugin.pay(appContext, pi,this);

    }
	@Override
	public void OnPaymentResult(int errCode, String errMsg) {
		OnPayResult(errCode,errMsg);
	}
	

	public void ConfirmExit() {
		  for (ISDKWarpper sdk : _sdk_obj_by_Id.values()){
			  if(sdk.getProviderInfo().hasExitUI){
				  sdk.confirmExit(appContext, this);
				  break;
			  }
		  }
		
	}
	
	public void confirmExitCallback(boolean bExit){
		OnConfirmExitResult(bExit);
	}
	
	
	public void pickMedia(int mediaType){
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		appContext.startActivityForResult(photoPickerIntent, RESULT_ID_PICK_PHOTO);
	}
	
	
	private ArrayList<NotificationItem> notifications;
	
    void BeginAddNotification(){
    	notifications = new ArrayList<NotificationItem>();
    }
    void AddNotification(Map<String,String> info){
    	NotificationItem item = new NotificationItem();
    	item.tag = info.get("tag");
    	item.icon = _notificationIconResId;
    	item.time = Integer.parseInt(info.get("time"));
    	item.title = info.get("title");
    	item.text = info.get("text");
    	notifications.add(item);
    }
    void EndAddNotification(){
    	LocalNotification.setNotifications(appContext, notifications, _appActivityClass );
    	notifications = new ArrayList<NotificationItem>();
    }
	
	
	
	public native void OnLogout();
	public native void OnAuthenticateResult(int resultCode,String message,Map<String,String> data);
	public native void OnPayResult(int resultCode,String message);
	public native void OnConfirmExitResult(boolean bExit);
	public native void OnPickMediaResult(byte[] data);
	public native void OnEnterLoginResult(String sdkName,  String serverList);
	

	public void OnAuthenticationSuccess(IAuthenticationSDK provider,LoginAccountInfo accountInfo) {
		
		
		Map<String,String> data = new TreeMap<String,String>();
		data.put("Prefix",accountInfo.channelPrefix);
		data.put("ChannelAccount",accountInfo.channelAccountId);
		data.put("Account", accountInfo.accountId);
		data.put("Token", accountInfo.token);
		data.put("Time",accountInfo.loginTime);
		data.put("Extra",accountInfo.extra);
		data.put("isNewAccount",accountInfo.isNewAccount?"1":"0");
		
		
//		Iterator<Entry<String,String>> iter = data.entrySet().iterator();
//		while(iter.hasNext()){
//			Entry<String,String> kv = iter.next(); 
//			String key = kv.getKey();
//			String val = kv.getValue();
//			
//			Log.d(LOG_TAG , key + "->" + val);
//			
//		}
		OnAuthenticateResult(S_OK,null,data);
	}

	public void OnAuthenticationFailed(IAuthenticationSDK provider, String Message) {
		OnAuthenticateResult(E_Authentication_Failed,Message,new TreeMap<String,String>());
	}
    

	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public Map<String,String>  GetSystemInfo()
	{
		Map<String,String> retVal = new TreeMap<String, String>();
		retVal.put("osVersion",Integer.toString(android.os.Build.VERSION.SDK_INT));
		retVal.put("deviceModel", android.os.Build.MODEL);
		

		Point screenRes = new Point();
		Display disp = appContext.getWindowManager().getDefaultDisplay();
		if(android.os.Build.VERSION.SDK_INT>=17){
			disp.getRealSize(screenRes);
		}else{
			screenRes.x = disp.getWidth();
			screenRes.y = disp.getHeight();
		}
		if(screenRes.y > screenRes.x){
			int tmp = screenRes.x;
			screenRes.x = screenRes.y;
			screenRes.y = tmp;
		}
		
		retVal.put("screenWidth", Integer.toString(screenRes.x));
		retVal.put("screenHeight",Integer.toString(screenRes.y));

		
		return retVal;
	}
	
	public void Logout(){
		for (IAuthenticationSDK sdk : _authentication_sdk_list)
		{
			sdk.logout();
	    }
	}
	
	public void OnGameCheckVersionBegin(){
		for (ISDKWarpper sdk : _sdk_obj_by_Id.values())
		{
			sdk.onGameCheckVersionBegin();
	    }
	}
	
	public void OnGameCheckVersionEnd(){
		for (ISDKWarpper sdk : _sdk_obj_by_Id.values())
		{
			sdk.onGameCheckVersionEnd();
	    }
	}
	
	public void OnEnterLoginView(){
		for (ISDKWarpper sdk : _sdk_obj_by_Id.values())
		{
			sdk.onEnterLoginView(this);
	    }
	}
	
	public void enterLoginCallback( String sdkName,  String serverList){
		OnEnterLoginResult(sdkName, serverList);
	}
	
	
	
	
	public void snsShare(Map<String,String> shareContent){
		if(_share_sdk_list.isEmpty()){
			OnShareResult(E_Failed);
			return;
		}
		// TODO:闁哄嫬澧介妵姘舵偩瀹�鍕〃闁挎稑鐭傞敓钘夘槹鐎氥劑宕氶崱鏇㈢叐闂侇偅鏌ㄧ欢锟�
		ISnsShareSDK impl = _share_sdk_list.get(0);
		
		
		SnsShareContent c = new SnsShareContent(shareContent);
		
		
		impl.share(appContext,c,this);
		
		
	}
	
	@Override
	public native void OnShareResult(int errCode);
	
	
	
	public void snsAppInvite(String topic){
		if(_invite_sdk_list.isEmpty()){
			OnAppInviteResult(E_Failed);
			return;
		}
		ISnsAppInviteSDK impl =_invite_sdk_list.get(0);
		impl.appInvite(topic, this);
		
	}
	@Override
	public native void OnAppInviteResult(int errCode);
	
	
	public void AccountBind(String accountType){
		IAuthenticationSDKEx plugin = null;
		for(IAuthenticationSDK sdk : _authentication_sdk_list){
			plugin = (IAuthenticationSDKEx)sdk;
			if(null!=plugin)
				break;
		}
		if(null == plugin){
			Toast.makeText(appContext, "On SDK AccountBind: not supported",Toast.LENGTH_LONG).show();
			return;
		}
		plugin.AccountBind(accountType,this);
	}
	@Override
	public native void OnAccountBindResult(String accountType, boolean succ);
	
	
	public void QueryAccountBindings(){
		IAuthenticationSDKEx plugin = null;
		for(IAuthenticationSDK sdk : _authentication_sdk_list){
			plugin = (IAuthenticationSDKEx)sdk;
			if(null!=plugin)
				break;
		}
		if(null == plugin){
			Toast.makeText(appContext, "On SDK AccountBind: not supported",Toast.LENGTH_LONG).show();
			return;
		}
		
		plugin.QueryAccountBindings(this);
		
	}
	@Override
	public native void OnQueryAccountBindingsResult(Map<String, String> bindings);
	
	
	
	//push_sdk
	public void startPush(){
		for(PushSdk sdk:_push_sdk_list){
    		sdk.startPush();
    	}
	}
	void stopPush(){
		for(PushSdk sdk:_push_sdk_list){
    		sdk.stopPush();
    	}	
	}
	void setTag(String tag){
		for(PushSdk sdk:_push_sdk_list){
    		sdk.setTag(tag);
    	}
	}
	void delTag(String tag){
		for(PushSdk sdk:_push_sdk_list){
    		sdk.delTag(tag);
    	}
	}
	void setAlias(String alias){
		for(PushSdk sdk:_push_sdk_list){
    		sdk.setAlias(alias);
    	}
	}
	void delAlias(String alias){
		for(PushSdk sdk:_push_sdk_list){
    		sdk.delAlias(alias);
    	}
	}
	
}
