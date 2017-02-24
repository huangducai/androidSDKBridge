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
        // �����ֶΣ�����Ϊnull����""������д�����˻�ȡ��֧��ID
        // the pay ID is required and can not be null or "" 
        params.put(GlobalParam.PayParams.USER_ID, GlobalParam.PAY_ID);
        // �����ֶΣ�����Ϊnull����""������д�����˻�ȡ��Ӧ��ID
        // the APP ID is required and can not be null or "" 
        params.put(GlobalParam.PayParams.APPLICATION_ID, GlobalParam.APP_ID);
        // �����ֶΣ�����Ϊnull����""����λ��Ԫ����ȷ��С�������λ����1.00
        // the amount (accurate to two decimal places) is required
        params.put(GlobalParam.PayParams.AMOUNT, price);
        // �����ֶΣ�����Ϊnull����""����������
        // the product name is required and can not be null or "" 
        params.put(GlobalParam.PayParams.PRODUCT_NAME, productName);
        // �����ֶΣ�����Ϊnull����""����������
        // the product description is required and can not be null or "" 
        params.put(GlobalParam.PayParams.PRODUCT_DESC, productDesc);
        // �����ֶΣ�����Ϊnull����""���30�ֽڣ������ظ������򶩵���ʧ��
        // the request ID is required and can not be null or "". Also it must be unique.
        params.put(GlobalParam.PayParams.REQUEST_ID, requestId);

        String noSign = HuaweiPayUtil.getSignData(params);
        LogUtil.d("startPay", "noSign��" + noSign);
        
        // CP����Ѳ������ݵ�����ˣ��ڷ���˽���ǩ����Ȼ���sign��������ʹ�ã������ǩ���Ĵ���Ϳͻ���һ��
        // the CP need to send the params to the server and sign the params on the server , 
        // then the server passes down the sign to client;
        String sign = Rsa.sign(noSign, GlobalParam.PAY_RSA_PRIVATE);
        LogUtil.d("startPay", "sign�� " + sign);


        Map<String, Object> payInfo = new HashMap<String, Object>();
        // �����ֶΣ�����Ϊnull����""
        // the amount is required and can not be null or "" 
        payInfo.put(GlobalParam.PayParams.AMOUNT, price);
        // �����ֶΣ�����Ϊnull����""
        // the product name is required and can not be null or ""
        payInfo.put(GlobalParam.PayParams.PRODUCT_NAME, productName);
        // �����ֶΣ�����Ϊnull����""
        // the request ID is required and can not be null or ""
        payInfo.put(GlobalParam.PayParams.REQUEST_ID, requestId);
        // �����ֶΣ�����Ϊnull����""
        // the product description is required and can not be null or ""
        payInfo.put(GlobalParam.PayParams.PRODUCT_DESC, productDesc);
        // �����ֶΣ�����Ϊnull����""������д�Լ��Ĺ�˾����
        // the user name is required and can not be null or "". Input the company name of CP.
        payInfo.put(GlobalParam.PayParams.USER_NAME, "���ݲ�ͫ���缼�����޹�˾");
        // �����ֶΣ�����Ϊnull����""
        // the APP ID is required and can not be null or "". 
        payInfo.put(GlobalParam.PayParams.APPLICATION_ID, GlobalParam.APP_ID);
        // �����ֶΣ�����Ϊnull����""
        // the user ID is required and can not be null or "". 
        payInfo.put(GlobalParam.PayParams.USER_ID, GlobalParam.PAY_ID);
        // �����ֶΣ�����Ϊnull����""
        // the sign is required and can not be null or "".
        payInfo.put(GlobalParam.PayParams.SIGN, sign);
        
        payInfo.put(GlobalParam.PayParams.NOTIFY_URL, GlobalParam.SVERVER_CALL_URL);
        
        // �����ֶΣ�����Ϊnull����""���˴�д��X6
        // the service catalog is required and can not be null or "".
        payInfo.put(GlobalParam.PayParams.SERVICE_CATALOG, "X6");
        //��Ʒ����
        payInfo.put(GlobalParam.PayParams.GOOD_INFO, productInfo);
        

        // �����ڿɴ���־������ʱע�͵�
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
                     * �ӷ���˻�ȡ��֧��˽Կ������û�в������յķ���ˣ����Է���ֵд��һ��ֵ��CP��Ҫ�ӷ���˻�ȡ�� ����˴���ο���ΪDemo ����� ��Ϊ��Ϸ����SDK����ָ����.docx ��2.5��
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