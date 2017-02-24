package com.gamefps.sdkbridge;

public class SdkProviderInfo {
		public SdkProviderInfo(String id,String name,int priority,boolean hasExitUI){
			this.Id = id;
			this.Name = name;
			this.priority = priority;
			this.hasExitUI = hasExitUI;
		}
		final public String Id;	// ΨһID
		final public String Name;
		final public int priority;
		final public boolean hasExitUI;
}
