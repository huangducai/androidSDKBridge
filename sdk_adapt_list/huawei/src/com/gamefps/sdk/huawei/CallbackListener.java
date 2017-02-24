package com.gamefps.sdk.huawei;

import org.json.JSONObject;

interface CallbackListener {
	void callback(int errCode,JSONObject json);
}
