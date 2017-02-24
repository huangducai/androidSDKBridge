package com.gamefps.sdkbridge;

import android.app.Activity;


public interface ISnsShareSDK {
	void share(Activity act, SnsShareContent shareContent,IShareCallback callback);
}
