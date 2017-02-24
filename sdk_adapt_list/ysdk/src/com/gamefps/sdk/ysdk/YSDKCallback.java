package com.gamefps.sdk.ysdk;

import android.app.Activity;
import android.util.Log;

import com.tencent.ysdk.framework.common.eFlag;
import com.tencent.ysdk.module.bugly.BuglyListener;
import com.tencent.ysdk.module.pay.PayListener;
import com.tencent.ysdk.module.pay.PayRet;
import com.tencent.ysdk.module.user.PersonInfo;
import com.tencent.ysdk.module.user.UserListener;
import com.tencent.ysdk.module.user.UserLoginRet;
import com.tencent.ysdk.module.user.UserRelationRet;
import com.tencent.ysdk.module.user.WakeupRet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** 
 * TODO GAME ��Ϸ��Ҫ�����Լ����߼�ʵ���Լ���YSDKCallback���� 
 * YSDKͨ��UserListener�������еķ�������Ȩ���ѯ����ص�����Ϸ��
 * ��Ϸ���ݻص��������UI�ȡ�ֻ�����ûص�����Ϸ�����յ�YSDK����Ӧ��
 * ������Java��ص�(������Java��ص������ȵ���Java��ص�, ���Ҫʹ��C++��ص���������Java��ص�)
 */
public class YSDKCallback implements UserListener, BuglyListener,PayListener {
    public static YSDKAdapter adapter;
    public static Activity act;

    
    public YSDKCallback(YSDKAdapter _adapter, Activity _act) {
    	adapter = _adapter;
    	act = _act;
    }
    
    public void callback(int errCode, String jsonStr)
    {
    }
    
    public void OnLoginNotify(UserLoginRet ret) {
        Log.d(YSDKAdapter.TAG,"called");
        Log.d(YSDKAdapter.TAG,ret.getAccessToken());
        Log.d(YSDKAdapter.TAG,"ret.flag" + ret.flag);
        Log.d(YSDKAdapter.TAG,ret.toString());
        String result = "";
        adapter.stopWaiting(act);
        switch (ret.flag) {
            case eFlag.Succ:
                adapter.letUserLogin(act);
                break;
            // ��Ϸ�߼����Ե�¼ʧ������ֱ���д���
            case eFlag.QQ_UserCancel:
                adapter.showToastTips(act,"�û�ȡ����Ȩ��������");
                adapter.letUserLogout();
                break;
            case eFlag.QQ_LoginFail:
                adapter.showToastTips(act,"QQ��¼ʧ�ܣ�������");
                adapter.letUserLogout();
                break;
            case eFlag.QQ_NetworkErr:
                adapter.showToastTips(act,"QQ��¼�쳣��������");
                adapter.letUserLogout();
                break;
            case eFlag.QQ_NotInstall:
                adapter.showToastTips(act,"�ֻ�δ��װ��Q���밲װ������");
                adapter.letUserLogout();
                break;
            case eFlag.QQ_NotSupportApi:
                adapter.showToastTips(act,"�ֻ���Q�汾̫�ͣ�������������");
                adapter.letUserLogout();
                break;
            case eFlag.WX_NotInstall:
                adapter.showToastTips(act,"�ֻ�δ��װ΢�ţ��밲װ������");
                adapter.letUserLogout();
                break;
            case eFlag.WX_NotSupportApi:
                adapter.showToastTips(act,"�ֻ�΢�Ű汾̫�ͣ�������������");
                adapter.letUserLogout();
                break;
            case eFlag.WX_UserCancel:
                adapter.showToastTips(act,"�û�ȡ����Ȩ��������");
                adapter.letUserLogout();
                break;
            case eFlag.WX_UserDeny:
                adapter.showToastTips(act,"�û��ܾ�����Ȩ��������");
                adapter.letUserLogout();
                break;
            case eFlag.WX_LoginFail:
                adapter.showToastTips(act,"΢�ŵ�¼ʧ�ܣ�������");
                adapter.letUserLogout();
                break;
            case eFlag.Login_TokenInvalid:
                adapter.showToastTips(act,"����δ��¼����֮ǰ�ĵ�¼�ѹ��ڣ�������");
                adapter.letUserLogout();
                break;
            case eFlag.Login_NotRegisterRealName:
                // ��ʾ��¼����
                adapter.showToastTips(act,"�����˺�û�н���ʵ����֤����ʵ����֤������");
                adapter.letUserLogout();
                break;
            default:
                // ��ʾ��¼����
                adapter.letUserLogout();
                break;
        }
    }

    public void OnWakeupNotify(WakeupRet ret) {
        Log.d(YSDKAdapter.TAG,"called");
        Log.d(YSDKAdapter.TAG,"flag:" + ret.flag);
        Log.d(YSDKAdapter.TAG,"msg:" + ret.msg);
        Log.d(YSDKAdapter.TAG,"platform:" + ret.platform);
        YSDKAdapter.platform = ret.platform;
        // TODO GAME ��Ϸ��Ҫ���������Ӵ������˺ŵ��߼�
        if (eFlag.Wakeup_YSDKLogining == ret.flag) {
            // ��������˺ŵ�¼����¼�����OnLoginNotify()�лص�
        } else if (ret.flag == eFlag.Wakeup_NeedUserSelectAccount) {
            // ���˺�ʱ����Ϸ��Ҫ������ʾ�����û�ѡ����Ҫ��¼���˺�
            Log.d(YSDKAdapter.TAG,"diff account");
            adapter.showDiffLogin(act);
        } else if (ret.flag == eFlag.Wakeup_NeedUserLogin) {
            // û����Ч��Ʊ�ݣ��ǳ���Ϸ���û����µ�¼
            Log.d(YSDKAdapter.TAG,"need login");
            adapter.letUserLogout();
        } else {
            Log.d(YSDKAdapter.TAG,"logout");
            adapter.letUserLogout();
        }
    }

    @Override
    public void OnRelationNotify(UserRelationRet relationRet) {
    	String result = "";
        result = result +"flag:" + relationRet.flag + "\n";
        result = result +"msg:" + relationRet.msg + "\n";
        result = result +"platform:" + relationRet.platform + "\n";
        if (relationRet.persons != null && relationRet.persons.size()>0) {
            PersonInfo personInfo = (PersonInfo)relationRet.persons.firstElement();
            StringBuilder builder = new StringBuilder();
            builder.append("UserInfoResponse json: \n");
            builder.append("nick_name: " + personInfo.nickName + "\n");
            builder.append("open_id: " + personInfo.openId + "\n");
            builder.append("userId: " + personInfo.userId + "\n");
            builder.append("gender: " + personInfo.gender + "\n");
            builder.append("picture_small: " + personInfo.pictureSmall + "\n");
            builder.append("picture_middle: " + personInfo.pictureMiddle + "\n");
            builder.append("picture_large: " + personInfo.pictureLarge + "\n");
            builder.append("provice: " + personInfo.province + "\n");
            builder.append("city: " + personInfo.city + "\n");
            builder.append("country: " + personInfo.country + "\n");
//            builder.append("is_yellow_vip: " + personInfo.is_yellow_vip + "\n");
//            builder.append("is_yellow_year_vip: " + personInfo.is_yellow_year_vip + "\n");
//            builder.append("yellow_vip_level: " + personInfo.yellow_vip_level + "\n");
//            builder.append("is_yellow_high_vip: " + personInfo.is_yellow_high_vip + "\n");
            result = result + builder.toString();
        } else {
            result = result + "relationRet.persons is bad";
        }
        Log.d(YSDKAdapter.TAG,"OnRelationNotify" + result);

        // ���ͽ�������չʾ����
        adapter.sendResult(result);
    }

    @Override
    public String OnCrashExtMessageNotify() {
        // �˴���Ϸ����crashʱ�ϱ��Ķ�����Ϣ
        Log.d(YSDKAdapter.TAG,String.format(Locale.CHINA, "OnCrashExtMessageNotify called"));
        Date nowTime = new Date();
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return "new Upload extra crashing message for bugly on " + time.format(nowTime);
    }

	@Override
	public byte[] OnCrashExtDataNotify() {
		return null;
	}

    @Override
    public void OnPayNotify(PayRet ret) {
        Log.d(YSDKAdapter.TAG,ret.toString());
        if(PayRet.RET_SUCC == ret.ret){
            //֧�����̳ɹ�
            switch (ret.payState){
                //֧���ɹ�
                case PayRet.PAYSTATE_PAYSUCC:
                	adapter.sendResult(
                            "�û�֧���ɹ���֧�����"+ret.realSaveNum+";" +
                            "ʹ��������"+ret.payChannel+";" +
                            "����״̬��"+ret.provideState+";" +
                            "ҵ�����ͣ�"+ret.extendInfo+";�����ѯ��"+ret.toString());
                	
                	adapter.callbackpayOk();
                	
                    break;
                //ȡ��֧��
                case PayRet.PAYSTATE_PAYCANCEL:
                	adapter.sendResult("�û�ȡ��֧����"+ret.toString());
                	adapter.callbackpayFailed("�û�ȡ��֧����"+ret.toString());
                    break;
                //֧�����δ֪
                case PayRet.PAYSTATE_PAYUNKOWN:
                	adapter.sendResult("�û�֧�����δ֪�������ѯ��"+ret.toString());
                	adapter.callbackpayOk();
                    break;
                //֧��ʧ��
                case PayRet.PAYSTATE_PAYERROR:
                	adapter.sendResult("֧���쳣"+ret.toString());
                	adapter.callbackpayFailed("֧���쳣"+ret.toString());
                    break;
            }
        }else{
            switch (ret.flag){
                case eFlag.Login_TokenInvalid:
                    //�û�ȡ��֧��
                    adapter.sendResult("��¼̬���ڣ������µ�¼��"+ret.toString());
                    adapter.letUserLogout();
                    adapter.callbackpayFailed("��¼̬���ڣ������µ�¼��"+ret.toString());
                    break;
                case eFlag.Pay_User_Cancle:
                    //�û�ȡ��֧��
                    adapter.sendResult("�û�ȡ��֧����"+ret.toString());
                    adapter.callbackpayFailed("�û�ȡ��֧����"+ret.toString());
                    break;
                case eFlag.Pay_Param_Error:
                    adapter.sendResult("֧��ʧ�ܣ���������"+ret.toString());
                    adapter.callbackpayFailed("֧��ʧ�ܣ���������"+ret.toString());
                    break;
                case eFlag.Error:
                default:
                    adapter.sendResult("֧���쳣"+ret.toString());
                    adapter.callbackpayFailed("֧���쳣"+ret.toString());
                    break;
            }
        }
    }
}

