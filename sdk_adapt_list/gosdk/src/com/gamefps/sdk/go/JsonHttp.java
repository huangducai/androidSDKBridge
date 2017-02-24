package com.gamefps.sdk.go;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;


public class JsonHttp {
	public static void GetJsonFromUrlAsync(final String url,final CallbackListener callback){
		
		class TaskInfo{
			public String url;
			public JSONObject retval;
			public CallbackListener callback;
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
					retVal.retval = getJSONObjectFromURL(retVal.url);
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
	
	
	
	public static JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {

	    HttpURLConnection urlConnection = null;

	    URL url = new URL(urlString);

	    urlConnection = (HttpURLConnection) url.openConnection();

	    urlConnection.setRequestMethod("GET");
	    urlConnection.setReadTimeout(10000 /* milliseconds */);
	    urlConnection.setConnectTimeout(15000 /* milliseconds */);

	    urlConnection.setDoOutput(true);

	    urlConnection.connect();

	    BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));


	    String jsonString = new String();

	    StringBuilder sb = new StringBuilder();
	    String line;
	    while ((line = br.readLine()) != null) {
	        sb.append(line+"\n");
	    }
	    br.close();

	    jsonString = sb.toString();

	    System.out.println("JSON: " + jsonString);

	    return new JSONObject(jsonString);
	}
	
	
	
	
}
