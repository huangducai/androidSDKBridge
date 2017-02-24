package com.gamefps.sdkbridge;


public interface PushSdk {
	void startPush();
	void stopPush();
	void setTag(String tag);
	void delTag(String tag);
	void setAlias(String alias);
	void delAlias(String alias);
}
