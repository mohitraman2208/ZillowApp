package com.example.zillowapplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class FetchPropertyDetails extends AsyncTask<String, Integer, JSONObject> {

	private OnResponse myResponse;
	
	public FetchPropertyDetails(OnResponse myActivity) 
	{
		myResponse = myActivity;
	}
	
	@Override
	protected JSONObject doInBackground(String... params) 
	{
		JSONObject propertyDetails = null;
		try
		{
			// Creating HTTP client
	        HttpClient httpClient = new DefaultHttpClient();
	        
	        HttpGet get = new HttpGet(params[0]);
	        
	        HttpResponse response = httpClient.execute(get);
	        
	        Log.d("HTTP Response:", response.toString());

	        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
	        StringBuffer responseString = new StringBuffer();
	        String line;
	        while((line = reader.readLine())!=null)
	        {
	        	responseString.append(line);
	        }
	        
	        propertyDetails = new JSONObject(responseString.toString());
	        
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return propertyDetails;
	}
	
	
	@Override
	protected void onPostExecute(JSONObject result) 
	{
		
		myResponse.processResponse(result);
	}

}
