package com.gamefps.sdk.huawei.net;

import java.util.Map;

import com.huawei.gameservice.sdk.util.LogUtil;

import android.os.AsyncTask;

public class ReqTask extends AsyncTask<Void, Void, String>
{
    private static final String TAG = ReqTask.class.getSimpleName();
    
    private Delegate delegate = null;
    
    private Map<String, String> reqParams = null;
    
    private String reqUrl = null;
    
    public ReqTask(Delegate deg, Map<String, String> params, String url)
    {
        delegate = deg;
        reqParams = params;
        reqUrl = url;
    }
    
    @Override
    protected String doInBackground(Void... params)
    {
        String result = null;
        try
        {
            /**
             * ����������������ʹ��һ��һ����ӳٴ��棬��CPʹ�ð�ȫ������ʵ�ֽ���
             */
            Thread.sleep(1000);
            result = "result";
            LogUtil.d(TAG, "request the network for result");
            /**
             * �����ַΪreqUrl�������POST����ΪreqParams��ʹ��UTF-8�����ʽ
             */
        }
        catch (Exception e)
        {
            LogUtil.d(TAG, e.getMessage());
        }
        return result;
    }
    
    @Override
    protected void onPostExecute(String result)
    {
        delegate.execute(result);
    }
    
    public interface Delegate
    {
        public void execute(String result);
    }
    
}