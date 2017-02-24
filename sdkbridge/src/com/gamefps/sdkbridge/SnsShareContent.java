package com.gamefps.sdkbridge;

import java.util.Map;

public class SnsShareContent {
	public String topic;
	public String title;
	public String description;
	public String url;
	public String image;
	
	public SnsShareContent(Map<String,String> data){
		topic =	data.get("topic");
		title =	data.get("title");
		description =	data.get("description");
		url =	data.get("url");
		image =	data.get("image");
	}
	
}
