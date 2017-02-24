package com.gamefps.sdk.oppo;

import org.json.JSONObject;

interface CallbackListener {
	void callback(int errCode,JSONObject json);
}
