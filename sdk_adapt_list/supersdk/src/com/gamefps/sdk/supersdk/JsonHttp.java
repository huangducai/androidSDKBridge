package com.gamefps.sdk.supersdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;

import android.os.AsyncTask;
import com.gamefps.sdk.supersdk.SuperSDKCallBackListener;


public class JsonHttp {
	public static void GetJsonFromUrlAsync(final String httpMethod, final String url,final String dataParams, final SuperSDKCallBackListener callback){
		class TaskInfo{
			public String url;
			public String retval;
			public SuperSDKCallBackListener callback;
		}
		
		class JsonHttpTask extends AsyncTask<TaskInfo, Void,TaskInfo>{
			@Override
			protected void onPostExecute(TaskInfo result) {
				if(null == result.retval){
					result.callback.callback(-1, result.retval);
				}else{
					result.callback.callback(0, result.retval);
				}
			}

			@Override
			protected TaskInfo doInBackground(TaskInfo... params) {
				TaskInfo retVal = params[0];
				try {
					if (httpMethod.equals("GET") ){
						retVal.retval = sendGetRequest(retVal.url);
				    }else if(httpMethod.equals("POST")){
				    	retVal.retval = sendPostRequest(retVal.url, dataParams);
				    }
				    
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return retVal;
			}
		}
		TaskInfo ti = new TaskInfo();
		ti.callback = callback;
		ti.url = url;
		new JsonHttpTask().execute(ti);
	}
	
	public static String sendGetRequest(String urlString) throws IOException, JSONException {
	    HttpURLConnection urlConnection = null;
	    URL url = new URL(urlString);
	    urlConnection = (HttpURLConnection) url.openConnection();
	    urlConnection.setReadTimeout(10000 /* milliseconds */);
	    urlConnection.setConnectTimeout(15000 /* milliseconds */);
    	urlConnection.setRequestMethod("GET");
	    urlConnection.setDoOutput(true);
	    urlConnection.connect();
	    BufferedReader br=new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
	    String jsonString = new String();
	    StringBuilder sb = new StringBuilder();
	    String line;
	    while ((line = br.readLine()) != null) {
	        sb.append(line+"\n");
	    }
	    br.close();
	    urlConnection.disconnect();
	    jsonString = sb.toString();
	    System.out.println("JSON: " + jsonString);
	    return jsonString;
	}
	
	public static String sendPostRequest(String urlString, String params) throws IOException, JSONException {
	    HttpURLConnection urlConnection = null;
	    URL url = new URL(urlString);
	    urlConnection = (HttpURLConnection) url.openConnection();
	    urlConnection.setReadTimeout(10000 /* milliseconds */);
	    urlConnection.setConnectTimeout(15000 /* milliseconds */);
	    urlConnection.setDoOutput(true);
	    urlConnection.setDoInput(true);
    	urlConnection.setRequestMethod("POST");
//    	urlConnection.setRequestProperty("Accept-Encoding", "gzip");
        byte[] data = params.getBytes("UTF-8");
        OutputStream out = urlConnection.getOutputStream();
        out.write(data);
        out.flush();
        out.close();
    	urlConnection.connect();
	    BufferedReader br=new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
	    String jsonString = new String();
	    StringBuilder sb = new StringBuilder();
	    String line;
	    while ((line = br.readLine()) != null) {
	        sb.append(line+"\n");
	    }
	    br.close();
	    urlConnection.disconnect();
	    jsonString = sb.toString();
	    
	    System.out.println("JSON: " + jsonString);  
	    return jsonString;
	}
}
