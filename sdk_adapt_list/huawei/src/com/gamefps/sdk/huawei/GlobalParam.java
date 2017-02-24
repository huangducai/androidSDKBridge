/*
Copyright (C) Huawei Technologies Co., Ltd. 2015. All rights reserved.
See LICENSE.txt for this sample's licensing information.
*/
package com.gamefps.sdk.huawei;

public class GlobalParam
{
	/**
     * 联盟为应用分配的应用ID
     */
    /**
     * APP ID
     */
    public static final String APP_ID = "10717453";
    
    /**
     * 浮标密钥，CP必须存储在服务端，然后通过安全网络（如https）获取下来，存储到内存中，否则存在私钥泄露风险
     */
    /**
     * private key for buoy, the CP need to save the key value on the server for security
     */
    /**************TODO:DELETE*******************/
    public static String BUOY_SECRET = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIZenwjRgFHpjF+Z4OhGThjlnXTMXq4Y45KXEd1onHleqGP/zoeJMzJrXRnhN8FoyGdBm7sPWzfZN2WxGWT9ivKZ76xqwLS03RIIz7rAlM6yRBzrhoSKbzC+hIJIvdirA3+QHR2QXXVJ8vf0tqtAcbpZWqF05XzeZ8GyC/T5DCj1AgMBAAECgYAOZzwi5Nodbax3XvZFz6t1bb+IEpQfk1lpkDoBAsKrG+Nu/K+1xZYwVGPhb0vbtpCsyNKZpZbAkM8oXmh7c9Yd7Ko8t6FldE2n4LaalsdwxeN+J5DfsnjwxhXVKGHF/1BVJSM1RU9ocvLsp/YOv00hYFAKO30mkPELUj51zxJpTQJBAOHzNM7bzhY04afOu4jPptSk67KCt5pknG9olx6pyF78O5o3trumP11q+s8flozU0PqvXDorExATHAgxuHYf2FMCQQCYPW2Xqa5Bi6YbdNUv5dLMvWaIv1FaR+aqVf9FqrwKpATmZUuzIOYta2WXesbQQp8yFg9ZlHnWN1a4noHW/DCXAkBCuQgaIeycwCb23+SfRPq2BrGXeGRxkk5j09X0dxy1d/S92L/8b958XrAL4/8YPWws6wXoI3owoAQCI3SeZU8DAkBwz/RKEp7iS02HwwfHOpL+zMsSQlNShz1Ss+85/M7jQ6+t2xpBXvuyZIcfvQdehD/bqaUuSGwQZWmJ3Utxt+d5AkBXdyvCXQ+ckHPIVxtbmV6jEmTzFIrEfi72y00rBUYJPLEilL37fFZriwzIKvXVZOsgo33pavU8JU5Dz0ILrJVf";
    
    /**
     * 支付ID
     */
	 /**
     * Pay ID
     */
    public static final String PAY_ID = "890086000001003696";
    
    /**
     * 支付私钥，CP必须存储在服务端，然后通过安全网络（如https）获取下来，存储到内存中，否则存在私钥泄露风险
     */
    /**
     * private key for pay, the CP need to save the key value on the server for security
     */
    /**************TODO:DELETE*******************/
    public static String PAY_RSA_PRIVATE = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDEGvzuOzPeEFp8mprA+fAwOA31A5ToOUOe6ecnAQRlwyVX2hS0CzNZYKoTpCLvfMES3kfJPnGxUlGpvoKRouxHDTCKjEmKyCmlksQ8swz9AsqmUlUVmAQWsHH2WmwsftjutZb9aOyrZZfxNYrviCBX+SpBjeTB/J7PtkeGN4hLeukb6mdqEVJr38+2J3Ltxt/NpEgAWH8Sk2vFSfFJKqNUD/EOHEG1eDBGUCTqBSZGy4PXsjjLFDhhaRxKqtwlw83wAnK87sDvj7Vg2e6k4NhbWxMr1ChYUOK+p6vNkDhINq1kbLJkNhqHUp11lFPuMKAsAs1sRdcSasnNZiPQloe7AgMBAAECggEAQOomG74hNzr2mRSccZDKv0tu41IiMpTyG2KWD8U4bW8hp2kbPVYP02A6LHJC1j/XsaU+4hD3lZkIS0Yy9n6V9BcboGE73uLPVm2049rpS79m0ZOjcph1kgUqVEAuWrRKnwohRxU6cqoYl9H/gbbv7e8e28BanxQ+hcdy+azcQw+BxATz+zfqOea/tvroRKtDo1M18hjKJCTKmbnxLtvsZyPTb+Naj6XcN+ZcWWZ2DYoMh9uyFsgsoWh1ka+tX/l7/yeH2Oq+SqfKSxSMMKJPaN5IzZPTsoXZodMdBdUR0BpAMKU9QiOZTpB/3MgDPzcL+KyEYbBPGLgO+CumjVEmYQKBgQD0I4h62q30iy/W47Map678lH4D+vIbNdF+csaRt2TQ89+mxD1nkUOno4NJvYx5KpivlYTL3cQqa5C+rFNPpIJYPFFniv5AySbweSUcOBL5uV0pRwFZCiq+LD0Q53VGSeKkgFYTxaXXI94YI3vvQ5pixd+1vYyycqCz1o1+SzMdgwKBgQDNogqlx1jrA4jYlhlxUcgEqK2CDXyGZzasIViM5cFw0f1sWSGLEPcmLOgKo10X1XcVF27xRpeYmPZf5/Ze+5HMQ0kd4bx+SutFfyTdUJJM2++XexVlkZUW3RTIJ4awIgMgh1HP4crrEldAIYyszNQVhD8GDNX5Zgw4LtgYYCxPaQKBgA2fgfkhBL31mljKYe8l1jzf6Fw+f44HJcaBjxUIf8atJRjAMJD76SOMm0HdIt6MAvMhS8S4fj7Du9L3+Loj+uYDj6NQfTmOP+8BQ5HciyqGLm2kOa4HRDVScKHl2IRwkQYO5z65M7TamoUMTdkfE5lZY09X4Wb/wHnoigK3HYYTAoGBAI2f6fdticcrAwgwsMtESR80UXP99+LVmWkJ+mPoJcefSQxjgt+1ypKnYRVamFY+9io3nnhcZzJ5P06kgyG94Bevy97hxyGwVcPiorAuuuRmoehRpFX045sAnzyPRItwkXjI2kbzuv8V/bO0p5+n3vYGxCVVl9WSPB5B3SFEs5/pAoGAS6Exey6WiTaZHvtaSQWJIig9HIM2aTJcxKMNqwuhEw0UwVefKQiKKudpulXBRb+BSieXcOU1dAr6UmLmsF8SCc5qi2tUnWKBIIxIdXW6dSUsbsELOqUeuegiK5a4P9wKh0QtN1htnLW0KrzBHGaHqed9Ubi/Jbibz4GG6XJotXI=";
    
    /**
     * 支付公钥
     */
    /**
     * public key for pay
     */
    public static final String PAY_RSA_PUBLIC = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxBr87jsz3hBafJqawPnwMDgN9QOU6DlDnunnJwEEZcMlV9oUtAszWWCqE6Qi73zBEt5HyT5xsVJRqb6CkaLsRw0wioxJisgppZLEPLMM/QLKplJVFZgEFrBx9lpsLH7Y7rWW/Wjsq2WX8TWK74ggV/kqQY3kwfyez7ZHhjeIS3rpG+pnahFSa9/Ptidy7cbfzaRIAFh/EpNrxUnxSSqjVA/xDhxBtXgwRlAk6gUmRsuD17I4yxQ4YWkcSqrcJcPN8AJyvO7A74+1YNnupODYW1sTK9QoWFDivqerzZA4SDatZGyyZDYah1KddZRT7jCgLALNbEXXEmrJzWYj0JaHuwIDAQAB";
    
    /**
     * 登录签名公钥
     */
	
	public static final String LOGIN_RSA_PUBLIC = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmKLBMs2vXosqSR2rojMzioTRVt8oc1ox2uKjyZt6bHUK0u+OpantyFYwF3w1d0U3mCF6rGUnEADzXiX/2/RgLQDEXRD22er31ep3yevtL/r0qcO8GMDzy3RJexdLB6z20voNM551yhKhB18qyFesiPhcPKBQM5dnAOdZLSaLYHzQkQKANy9fYFJlLDo11I3AxefCBuoG+g7ilti5qgpbkm6rK2lLGWOeJMrF+Hu+cxd9H2y3cXWXxkwWM1OZZTgTq3Frlsv1fgkrByJotDpRe8SwkiVuRycR0AHsFfIsuZCFwZML16EGnHqm2jLJXMKIBgkZTzL8Z+201RmOheV4AQIDAQAB";

    /*
     * 支付页面横竖屏参数：1表示竖屏，2表示横屏，默认竖屏
     */
    // portrait view for pay UI
	public static final int PAY_ORI = 1;
	// landscape view for pay UI
	public static final int PAY_ORI_LAND = 2;
    

	/**
	 * 生成签名时需要使用RSA的私钥，安全考虑，必须放到服务端，通过此接口使用安全通道获取
	 */
	/**
	 * the server url for getting the pay private key.The CP need to modify the
	 * value for the real server.
	 */
	public static final String GET_PAY_PRIVATE_KEY = "https://ip:port/HuaweiServerDemo/getPayPrivate";

	/**
	 * 调用浮标时需要使用浮标的私钥，安全考虑，必须放到服务端，通过此接口使用安全通道获取
	 */
	/**
	 * the server url for getting the buoy private key.The CP need to modify the
	 * value for the real server.
	 */
	public static final String GET_BUOY_PRIVATE_KEY = "https://ip:port/HuaweiServerDemo/getBuoyPrivate";
	
	public static final String SVERVER_CALL_URL="http://221.236.172.156:23580/cnc/cncweb/channels/hw/api/paycallback.php";
    
    public interface PayParams
    {
        public static final String USER_ID = "userID";
        
        public static final String APPLICATION_ID = "applicationID";
        
        public static final String AMOUNT = "amount";
        
        public static final String PRODUCT_NAME = "productName";
        
        public static final String PRODUCT_DESC = "productDesc";
        
        public static final String REQUEST_ID = "requestId";
        
        public static final String USER_NAME = "userName";
        
        public static final String SIGN = "sign";
        
        public static final String NOTIFY_URL = "notifyUrl";
        
        public static final String SERVICE_CATALOG = "serviceCatalog";
        
        public static final String SHOW_LOG = "showLog";
        
        public static final String SCREENT_ORIENT = "screentOrient";
        
        public static final String SDK_CHANNEL = "sdkChannel";
        
        public static final String URL_VER = "urlver";
        
        public static final String GOOD_INFO = "extReserved";
    }
    
}

