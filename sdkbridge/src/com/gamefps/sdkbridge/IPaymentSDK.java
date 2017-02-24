package com.gamefps.sdkbridge;

import android.app.Activity;

public interface IPaymentSDK {
	void pay(Activity act, PaymentInfo paymentInfo,IPaymentCallback callback);
	
}
