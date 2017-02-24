package com.gamefps.sdk.go;

import org.json.JSONObject;

interface CallbackListener {
	void callback(int errCode,JSONObject json);
}
