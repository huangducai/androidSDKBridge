package com.gamefps.sdk.umessage;


import com.gamefps.sdkbridge.IActivityOverrider;
import com.gamefps.sdkbridge.ISDKWarpper;
import com.gamefps.sdkbridge.PushSdk;
import com.gamefps.sdkbridge.SDKBridge;
import com.gamefps.sdkbridge.SdkConfigInfo;
import com.gamefps.sdkbridge.SdkProviderInfo;
import com.umeng.message.IUmengCallback;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;
import com.umeng.message.common.inter.ITagManager;
import com.umeng.message.tag.TagManager;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

public class UMMessage implements ISDKWarpper, PushSdk ,IActivityOverrider{
	private final static SdkProviderInfo SDK_PROVIDER_INFO = new SdkProviderInfo("UMESSAGE", "UMESSAGE",1,true);
	
	private PushAgent mPushAgent;
	
	private int initNum = 0;
	
	private boolean isInitSuccess = false;
	
	public void startPush(){
		
		if(!isInitSuccess){
			Log.e("push_sdk", " push_sdk init failed cant start push");
			return;
		}
		
		mPushAgent.enable(new IUmengCallback() {
		    @Override
		    public void onSuccess() {

		    }

		    @Override
		    public void onFailure(String s, String s1) {

		    }
		});
	}
	@Override
	public SdkProviderInfo getProviderInfo() {
		// TODO Auto-generated method stub
		return SDK_PROVIDER_INFO;
	}

	public void stopPush(){
		if(!isInitSuccess){
			Log.e("push_sdk", " push_sdk init failed cant stopPush");
			return;
		}
		mPushAgent.disable(new IUmengCallback() {
		    @Override
		    public void onSuccess() {

		    }

		    @Override
		    public void onFailure(String s, String s1) {
		    	Log.e("push_sdk", "stopPush error" + s1);
		    }
		});
	}
	
	public void setTag(String tag){
		if(tag == null || tag.length() <=0){
			Log.e("push_sdk", "setTag tag is null");
			return;
		}
		
		if(!isInitSuccess){
			Log.e("push_sdk", " push_sdk init failed cant setTag");
			return;
		}
		
		mPushAgent.getTagManager().add(new TagManager.TCallBack() {
		    @Override
		    public void onMessage(final boolean isSuccess, final ITagManager.Result result) {
		        //isSuccess��ʾ�����Ƿ�ɹ�         
		    }
		},tag);
	}
	//
	public void getTag(){
		//				mPushAgent.getTagManager().list(new TagManager.TagListCallBack() {
		//				    @Override
		//				    public void onMessage(boolean isSuccess, List<String> result) {
		//		
		//				    }
		//				});
	}
	
	public void delTag(String tag){
		
		if(!isInitSuccess){
			Log.e("push_sdk", " push_sdk init failed cant delTag");
			return;
		}
		
		if(tag == null || tag.length() <= 0){
			delAllTags();
		}else{
			mPushAgent.getTagManager().delete(new TagManager.TCallBack() {
			    @Override
			    public void onMessage(final boolean isSuccess, final ITagManager.Result result) {

			    }
			}, tag);	
		}
	}
	
	public void delAllTags(){
		if(!isInitSuccess){
			Log.e("push_sdk", " push_sdk init failed cant delAllTags");
			return;
		}
		
		mPushAgent.getTagManager().reset(new TagManager.TCallBack() {
		    @Override
		    public void onMessage(boolean isSuccess, ITagManager.Result result) {

		    }
		});
	}
	
	public void setAlias(String alias){
		if(alias == null || alias.length() <=0){
			Log.e("push_sdk", "setAlias alias is null");
			return;
		}
		if(!isInitSuccess){
			Log.e("push_sdk", " push_sdk init failed cant setAlias");
			return;
		}
		mPushAgent.addAlias(alias,"uc", new UTrack.ICallBack() {
		    @Override
		    public void onMessage(boolean isSuccess, String message) {
		    	Log.e("push_sdk", " onMessage :" + isSuccess + "|" + message);
		    }
		});
	}
	
	public void delAlias(String alias){
		if(alias == null || alias.length() <=0){
			Log.e("push_sdk", "delAlias alias is null");
			return;
		}
		
		mPushAgent.removeAlias(alias, "uc", new UTrack.ICallBack(){
		    @Override
		    public void onMessage(boolean isSuccess, String message) {

		    }
		});
	}
	private Thread newThread;         //声明一个子线程
	public void onCreate(Activity act, Bundle savedInstanceState) {
		mPushAgent = PushAgent.getInstance(act);
		//mPushAgent.setResourcePackageName(act.getPackageName());

		final Activity act1 = act;
		final Bundle savedInstanceState1 = savedInstanceState;
		new Thread() {
		@Override
		public void run() {
			mPushAgent.register(new IUmengRegisterCallback() {
			    @Override
			    public void onSuccess(String deviceToken) {
			    	isInitSuccess = true;
			    	Log.e("push_sdk", "deviceToken is:" + deviceToken);
			    }

			    @Override
			    public void onFailure(String s, String s1) {
			    	Log.e("push_sdk", "mPushAgent.register error " + s + "|" + s1);
			    	isInitSuccess = false;
			    	initNum ++;
			    	if(initNum < 3){
				    	onCreate(act1, savedInstanceState1);	
			    	}
			    }
			});
		        }
		   }.start();
		mPushAgent.onAppStart();
		//mPushAgent.setPushCheck(true);//for debug
		mPushAgent.setDebugMode(false);//must set false for online game
		}
	public void onDestroy(Activity act){
		
	}
	
	public void onPause(Activity act){
		
	}
	public void onResume(Activity act){
		
	}
	
	public void onStop(Activity act){
		
	}
	public void onRestart(Activity act){
		
	}
	
	public void onActivityResult(Activity act, int requestCode, int resultCode, Intent data){
		
	}
	
	public void onNewIntent(Activity act,Intent intent){
		
	}
	
	public void onStart(Activity act){
		
	}
	public void onSaveInstanceState(Activity act, Bundle outState){
		
	}
	public void onConfigurationChanged(Activity act, Configuration newConfig){
		
	}

	@Override
	public boolean init(Activity ctx, SdkConfigInfo cfg) {
		
		return true;
	}

	@Override
	public void confirmExit(Activity ctx, SDKBridge callback) {
		// TODO �Զ����ɵķ������
		
	}

	@Override
	public void onGameCheckVersionBegin() {
		// TODO �Զ����ɵķ������
		
	}

	@Override
	public void onGameCheckVersionEnd() {
		// TODO �Զ����ɵķ������
		
	}

	@Override
	public void onEnterLoginView(SDKBridge callback) {
		// TODO �Զ����ɵķ������
		
	}
}
