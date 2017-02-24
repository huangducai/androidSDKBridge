/*
Copyright (C) Huawei Technologies Co., Ltd. 2015. All rights reserved.
See LICENSE.txt for this sample's licensing information.
 */
package com.gamefps.sdk.huawei;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.res.Configuration;
import android.util.Log;

import com.android.huawei.pay.util.HuaweiPayUtil;
import com.android.huawei.pay.util.Rsa;
import com.huawei.gameservice.sdk.GameServiceSDK;
import com.huawei.gameservice.sdk.api.GameEventHandler;
import com.huawei.gameservice.sdk.util.LogUtil;
import com.gamefps.sdk.huawei.net.ReqTask;
import com.gamefps.sdk.huawei.net.ReqTask.Delegate;;

public class GameBoxUtil
{
    public static final String TAG = GameBoxUtil.class.getSimpleName();
    
    public static void pay(
        final Activity activity,
        final String price,
        final String productName,
        final String productDesc,
        final String requestId,
        final String productInfo,
            final GameEventHandler handler, Map<String, String> paramMap)
    {
    	
        Map<String, String> params = new HashMap<String, String>();
        // 必填字段，不能为null或者""，请填写从联盟获取的支付ID
        // the pay ID is required and can not be null or "" 
        params.put(GlobalParam.PayParams.USER_ID, GlobalParam.PAY_ID);
        // 必填字段，不能为null或者""，请填写从联盟获取的应用ID
        // the APP ID is required and can not be null or "" 
        params.put(GlobalParam.PayParams.APPLICATION_ID, GlobalParam.APP_ID);
        // 必填字段，不能为null或者""，单位是元，精确到小数点后两位，如1.00
        // the amount (accurate to two decimal places) is required
        params.put(GlobalParam.PayParams.AMOUNT, price);
        // 必填字段，不能为null或者""，道具名称
        // the product name is required and can not be null or "" 
        params.put(GlobalParam.PayParams.PRODUCT_NAME, productName);
        // 必填字段，不能为null或者""，道具描述
        // the product description is required and can not be null or "" 
        params.put(GlobalParam.PayParams.PRODUCT_DESC, productDesc);
        // 必填字段，不能为null或者""，最长30字节，不能重复，否则订单会失败
        // the request ID is required and can not be null or "". Also it must be unique.
        params.put(GlobalParam.PayParams.REQUEST_ID, requestId);

        String noSign = HuaweiPayUtil.getSignData(params);
        LogUtil.d("startPay", "noSign：" + noSign);
        
        // CP必须把参数传递到服务端，在服务端进行签名，然后把sign传递下来使用；服务端签名的代码和客户端一致
        // the CP need to send the params to the server and sign the params on the server , 
        // then the server passes down the sign to client;
        String sign = Rsa.sign(noSign, GlobalParam.PAY_RSA_PRIVATE);
        LogUtil.d("startPay", "sign： " + sign);


        Map<String, Object> payInfo = new HashMap<String, Object>();
        // 必填字段，不能为null或者""
        // the amount is required and can not be null or "" 
        payInfo.put(GlobalParam.PayParams.AMOUNT, price);
        // 必填字段，不能为null或者""
        // the product name is required and can not be null or ""
        payInfo.put(GlobalParam.PayParams.PRODUCT_NAME, productName);
        // 必填字段，不能为null或者""
        // the request ID is required and can not be null or ""
        payInfo.put(GlobalParam.PayParams.REQUEST_ID, requestId);
        // 必填字段，不能为null或者""
        // the product description is required and can not be null or ""
        payInfo.put(GlobalParam.PayParams.PRODUCT_DESC, productDesc);
        // 必填字段，不能为null或者""，请填写自己的公司名称
        // the user name is required and can not be null or "". Input the company name of CP.
        payInfo.put(GlobalParam.PayParams.USER_NAME, "广州彩瞳网络技术有限公司");
        // 必填字段，不能为null或者""
        // the APP ID is required and can not be null or "". 
        payInfo.put(GlobalParam.PayParams.APPLICATION_ID, GlobalParam.APP_ID);
        // 必填字段，不能为null或者""
        // the user ID is required and can not be null or "". 
        payInfo.put(GlobalParam.PayParams.USER_ID, GlobalParam.PAY_ID);
        // 必填字段，不能为null或者""
        // the sign is required and can not be null or "".
        payInfo.put(GlobalParam.PayParams.SIGN, sign);
        
        payInfo.put(GlobalParam.PayParams.NOTIFY_URL, GlobalParam.SVERVER_CALL_URL);
        
        // 必填字段，不能为null或者""，此处写死X6
        // the service catalog is required and can not be null or "".
        payInfo.put(GlobalParam.PayParams.SERVICE_CATALOG, "X6");
        //商品参数
        payInfo.put(GlobalParam.PayParams.GOOD_INFO, productInfo);
        

        // 调试期可打开日志，发布时注释掉
        // print the log for demo
        payInfo.put(GlobalParam.PayParams.SHOW_LOG, true);
        
        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            payInfo.put(GlobalParam.PayParams.SCREENT_ORIENT,
                    GlobalParam.PAY_ORI_LAND);
        }
        else
        {
            payInfo.put(GlobalParam.PayParams.SCREENT_ORIENT,
                    GlobalParam.PAY_ORI);
        }
        
        
        GameServiceSDK.startPay(activity, payInfo, handler);
        
    }
    
    public static void startPay(final Activity activity, final String price, final String productName, final String productDesc,
            final String requestId,final String productInfo, final GameEventHandler handler, final Map<String, String> param)
    {
        if ("".equals(GlobalParam.PAY_RSA_PRIVATE))
        {
            ReqTask getPayPrivate = new ReqTask(new Delegate()
            {

                @Override
                public void execute(String privateKey)
                {
                    /**
                     * 从服务端获取的支付私钥，由于没有部署最终的服务端，所以返回值写死一个值，CP需要从服务端获取， 服务端代码参考华为Demo 请查阅 华为游戏中心SDK开发指导书.docx 的2.5节
                     */
                	/**
                	 * The demo app did not deployed the server, so the return value is written fixed.For real app,the CP need to get the  key from server.
                	 */
                    privateKey = "";
                    GlobalParam.PAY_RSA_PRIVATE = privateKey;
                    pay(activity, price, productName, productDesc, requestId,productInfo, handler, param);
                }
            }, null, GlobalParam.GET_PAY_PRIVATE_KEY);
            getPayPrivate.execute();
        }
        else
        {
            pay(activity, price, productName, productDesc, requestId,productInfo, handler, param);
        }
        
    }
}