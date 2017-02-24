package com.gamefps.sdk.ysdk;

import org.json.JSONObject;

interface CallbackListener {
	void callback(int errCode,JSONObject json);
}
