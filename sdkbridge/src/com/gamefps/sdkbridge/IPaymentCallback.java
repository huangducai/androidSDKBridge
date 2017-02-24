package com.gamefps.sdkbridge;

public interface IPaymentCallback {
	void OnPaymentResult(int errCode,String errMsg);
}
