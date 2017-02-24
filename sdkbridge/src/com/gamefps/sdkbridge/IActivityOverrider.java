package com.gamefps.sdkbridge;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

public interface IActivityOverrider {
	void onCreate(Activity act, Bundle savedInstanceState) ;
	void onDestroy(Activity act);
	
	void onPause(Activity act);
	void onResume(Activity act);
	
	void onStop(Activity act);
	void onRestart(Activity act);
	
	void onActivityResult(Activity act, int requestCode, int resultCode, Intent data);
	void onNewIntent(Activity act,Intent intent);
	
	void onStart(Activity act);
	void onSaveInstanceState(Activity act, Bundle outState);
	void onConfigurationChanged(Activity act, Configuration newConfig);
}
