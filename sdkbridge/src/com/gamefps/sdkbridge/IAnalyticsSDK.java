package com.gamefps.sdkbridge;

import java.util.Map;

/**
 * Created by lvyou on 2016/1/3.
 */
public interface IAnalyticsSDK {
	enum QuestState{
		QS_Start,
		QS_Complete,
		QS_Fail,
	};
	
	
	
//    void setAccount(String account);
   // void setGameZone(String zoneName);
    void setRoleInfo(Map<String,String> roleData);
    
    
    
    void onCreateAccount(String account,Map<String,String> accountData);
    void onLoginAccount(String account,Map<String,String> accountData);
    
    void onCreateRole(String serverId,String roleName, String roleId, int roleCreateTime);
    void onEnterGame(String serverId,String roleName);
    
    void onQuestUpdate(String questType,String questId,QuestState state,String comment);
    
    void onCurrencyGain(String currencyType,String reason,int amount);
    void onCurrencyConsume(String currencyType,String reason,int amount);
    
    
    void logEvent(String event,Map<String,String> eventData);
    //void onResume(Activity act);
  //  void onPause(Activity act);
}
