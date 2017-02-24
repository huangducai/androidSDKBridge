package com.gamefps.sdkbridge;

import android.os.Bundle;

public class SdkConfigInfo {
	public String appKey;	// 所有sdk都会用到的
	public String appSecret;// 部分sdk有保存在客户端的secret信息
	public String channelId;
//	public Double appVer;
	public Bundle metaData;
}
