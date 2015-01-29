package com.example.zillowapplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements TextWatcher,OnClickListener,OnResponse  
{

	private String streetAddress;

	private String city;

	private String state;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button searchButton = (Button) findViewById(R.id.searchbutton); 
		searchButton.setOnClickListener(this);
		
		ImageView img = (ImageView)findViewById(R.id.zillowlogo);
		img.setOnClickListener(new View.OnClickListener(){
		    public void onClick(View v){
		        Intent intent = new Intent();
		        intent.setAction(Intent.ACTION_VIEW);
		        intent.addCategory(Intent.CATEGORY_BROWSABLE);
		        intent.setData(Uri.parse("http://www.zillow.com/"));
		        startActivity(intent);
		    }
		});
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub

	}


	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onClick(View v) 
	{
		try
		{
			boolean retval = validateForm();
			if(retval)
			{

				String url = buildGetURL();
				new FetchPropertyDetails(this).execute(url);
			}
		}
		catch(Exception e)
		{

		}
	}
	public String buildGetURL()
	{
		String url = "http://zillowapp.elasticbeanstalk.com?";

		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("mf_streetAddress", this.streetAddress));
		params.add(new BasicNameValuePair("mf_city", this.city));
		params.add(new BasicNameValuePair("mf_state", this.state));
		String query = URLEncodedUtils.format(params, "utf-8");

		url = url+query;

		return url;
	}


	public JSONObject getPropertyDetails() 
	{
		JSONObject propertyDetails = null;
		try
		{
			// Creating HTTP client
			HttpClient httpClient = new DefaultHttpClient();

			String url = "http://zillowapp.elasticbeanstalk.com?";

			List<NameValuePair> params = new LinkedList<NameValuePair>();
			params.add(new BasicNameValuePair("mf_streetAddress", this.streetAddress));
			params.add(new BasicNameValuePair("mf_city", this.city));
			params.add(new BasicNameValuePair("mf_state", this.state));
			String query = URLEncodedUtils.format(params, "utf-8");

			url = url+query;

			HttpGet get = new HttpGet(url);

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

		}catch(Exception e)
		{
			e.printStackTrace();
		}

		return propertyDetails;
	}


	private boolean validateForm() 
	{
		boolean retval = true;
		try
		{
			EditText streetAddress =(EditText) findViewById(R.id.streetAddress);
			EditText city = (EditText)findViewById(R.id.city);
			Spinner state = (Spinner) findViewById(R.id.states);
			TextView errorStreet = (TextView) findViewById(R.id.streetAddressError);
			TextView errorCity = (TextView) findViewById(R.id.cityError);
			TextView errorState = (TextView) findViewById(R.id.stateError);
			
			errorStreet.setVisibility(View.GONE);
			errorCity.setVisibility(View.GONE);
			errorState.setVisibility(View.GONE);
			findViewById(R.id.responseError).setVisibility(View.GONE);
			
			if(streetAddress.getText() == null || streetAddress.getText().toString().trim().length() == 0)
			{

				errorStreet.setVisibility(View.VISIBLE);
				retval = false;
			}
			else
			{
				this.streetAddress = streetAddress.getText().toString().trim();
			}
			if(city.getText() == null || city.getText().toString().trim().length() == 0)
			{
				errorCity.setVisibility(View.VISIBLE);
				retval= false;
			}
			else
			{
				this.city = city.getText().toString().trim();
			}
			if(state.getSelectedItem() == null || (state.getSelectedItem().toString().equals(getResources().getString(R.string.enterState))))
			{
				errorState.setVisibility(View.VISIBLE);
				retval= false;
			}
			else
			{
				this.state = state.getSelectedItem().toString();
			}
		}
		catch(Exception e)
		{

		}
		return retval;
	}


	@Override
	public void processResponse(JSONObject response) 
	{
		try 
		{
			if( response == null || response.getInt(Constants.TAG_STATUS) != 0)
			{
				TextView responseError = (TextView) findViewById(R.id.responseError);

				responseError.setText(response.getString(Constants.TAG_ERROR_MSG));

				responseError.setVisibility(View.VISIBLE);
			}
			else
			{
				/*Toast t=Toast.makeText(getApplicationContext(), "Success",Toast.LENGTH_SHORT);
				t.show();*/
				Intent intent = new Intent(getApplicationContext(), PropertyDetailActivity.class);
				//JSONPojo pojo = new JSONPojo(response);
				//intent.putExtra("propertyDetails", (Serializable)pojo);
				intent.putExtra("propertyDetails", response.toString());
				startActivity(intent);
			}	
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
