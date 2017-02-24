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
 * TODO GAME 游戏需要根据自己的逻辑实现自己的YSDKCallback对象。 
 * YSDK通过UserListener抽象类中的方法将授权或查询结果回调给游戏。
 * 游戏根据回调结果调整UI等。只有设置回调，游戏才能收到YSDK的响应。
 * 这里是Java层回调(设置了Java层回调会优先调用Java层回调, 如果要使用C++层回调则不能设置Java层回调)
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
            // 游戏逻辑，对登录失败情况分别进行处理
            case eFlag.QQ_UserCancel:
                adapter.showToastTips(act,"用户取消授权，请重试");
                adapter.letUserLogout();
                break;
            case eFlag.QQ_LoginFail:
                adapter.showToastTips(act,"QQ登录失败，请重试");
                adapter.letUserLogout();
                break;
            case eFlag.QQ_NetworkErr:
                adapter.showToastTips(act,"QQ登录异常，请重试");
                adapter.letUserLogout();
                break;
            case eFlag.QQ_NotInstall:
                adapter.showToastTips(act,"手机未安装手Q，请安装后重试");
                adapter.letUserLogout();
                break;
            case eFlag.QQ_NotSupportApi:
                adapter.showToastTips(act,"手机手Q版本太低，请升级后重试");
                adapter.letUserLogout();
                break;
            case eFlag.WX_NotInstall:
                adapter.showToastTips(act,"手机未安装微信，请安装后重试");
                adapter.letUserLogout();
                break;
            case eFlag.WX_NotSupportApi:
                adapter.showToastTips(act,"手机微信版本太低，请升级后重试");
                adapter.letUserLogout();
                break;
            case eFlag.WX_UserCancel:
                adapter.showToastTips(act,"用户取消授权，请重试");
                adapter.letUserLogout();
                break;
            case eFlag.WX_UserDeny:
                adapter.showToastTips(act,"用户拒绝了授权，请重试");
                adapter.letUserLogout();
                break;
            case eFlag.WX_LoginFail:
                adapter.showToastTips(act,"微信登录失败，请重试");
                adapter.letUserLogout();
                break;
            case eFlag.Login_TokenInvalid:
                adapter.showToastTips(act,"您尚未登录或者之前的登录已过期，请重试");
                adapter.letUserLogout();
                break;
            case eFlag.Login_NotRegisterRealName:
                // 显示登录界面
                adapter.showToastTips(act,"您的账号没有进行实名认证，请实名认证后重试");
                adapter.letUserLogout();
                break;
            default:
                // 显示登录界面
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
        // TODO GAME 游戏需要在这里增加处理异账号的逻辑
        if (eFlag.Wakeup_YSDKLogining == ret.flag) {
            // 用拉起的账号登录，登录结果在OnLoginNotify()中回调
        } else if (ret.flag == eFlag.Wakeup_NeedUserSelectAccount) {
            // 异账号时，游戏需要弹出提示框让用户选择需要登录的账号
            Log.d(YSDKAdapter.TAG,"diff account");
            adapter.showDiffLogin(act);
        } else if (ret.flag == eFlag.Wakeup_NeedUserLogin) {
            // 没有有效的票据，登出游戏让用户重新登录
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

        // 发送结果到结果展示界面
        adapter.sendResult(result);
    }

    @Override
    public String OnCrashExtMessageNotify() {
        // 此处游戏补充crash时上报的额外信息
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
            //支付流程成功
            switch (ret.payState){
                //支付成功
                case PayRet.PAYSTATE_PAYSUCC:
                	adapter.sendResult(
                            "用户支付成功，支付金额"+ret.realSaveNum+";" +
                            "使用渠道："+ret.payChannel+";" +
                            "发货状态："+ret.provideState+";" +
                            "业务类型："+ret.extendInfo+";建议查询余额："+ret.toString());
                	
                	adapter.callbackpayOk();
                	
                    break;
                //取消支付
                case PayRet.PAYSTATE_PAYCANCEL:
                	adapter.sendResult("用户取消支付："+ret.toString());
                	adapter.callbackpayFailed("用户取消支付："+ret.toString());
                    break;
                //支付结果未知
                case PayRet.PAYSTATE_PAYUNKOWN:
                	adapter.sendResult("用户支付结果未知，建议查询余额："+ret.toString());
                	adapter.callbackpayOk();
                    break;
                //支付失败
                case PayRet.PAYSTATE_PAYERROR:
                	adapter.sendResult("支付异常"+ret.toString());
                	adapter.callbackpayFailed("支付异常"+ret.toString());
                    break;
            }
        }else{
            switch (ret.flag){
                case eFlag.Login_TokenInvalid:
                    //用户取消支付
                    adapter.sendResult("登录态过期，请重新登录："+ret.toString());
                    adapter.letUserLogout();
                    adapter.callbackpayFailed("登录态过期，请重新登录："+ret.toString());
                    break;
                case eFlag.Pay_User_Cancle:
                    //用户取消支付
                    adapter.sendResult("用户取消支付："+ret.toString());
                    adapter.callbackpayFailed("用户取消支付："+ret.toString());
                    break;
                case eFlag.Pay_Param_Error:
                    adapter.sendResult("支付失败，参数错误"+ret.toString());
                    adapter.callbackpayFailed("支付失败，参数错误"+ret.toString());
                    break;
                case eFlag.Error:
                default:
                    adapter.sendResult("支付异常"+ret.toString());
                    adapter.callbackpayFailed("支付异常"+ret.toString());
                    break;
            }
        }
    }
}

