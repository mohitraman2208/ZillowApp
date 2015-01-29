package com.example.zillowapplication;

import java.util.Arrays;
import java.util.List;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Session.OpenRequest;
import com.facebook.Session.StatusCallback;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PropertyDetailAdapter extends ArrayAdapter<PropertyDetailRow> {

	private Activity myActivity;
	private PropertyDetailRow homeLink;
	private String imageUrl;
	private String lastSoldPrice;
	private String overallChange;
	FacebookDialog shareDialog;
	private UiLifecycleHelper uiHelper;
	public PropertyDetailAdapter(Context context,List<PropertyDetailRow> objects, UiLifecycleHelper uiHelper,PropertyDetailRow homeLink,String imageUrl, String lastSoldPrice, String overallChange) {
		super(context,0,objects);
		this.myActivity = (Activity)context;
		this.uiHelper=uiHelper;
		this.homeLink = homeLink;
		this.imageUrl = imageUrl;
		this.lastSoldPrice = lastSoldPrice;
		this.overallChange = overallChange;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN) @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		// Get the data item for this position
		PropertyDetailRow row = getItem(position);    
		// Check if an existing view is being reused, otherwise inflate the view
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.property_detail, parent, false);
		}
		if(position > 1 && position % 2 == 0)
		{
			convertView.setBackground(getContext().getResources().getDrawable(R.drawable.row_even_shape));
		}
		else
		{
			convertView.setBackground(getContext().getResources().getDrawable(R.drawable.row_odd_shape));
		}
		// Lookup view for data population
		TextView propName = (TextView) convertView.findViewById(R.id.propertyKey);
		TextView propValue = (TextView) convertView.findViewById(R.id.propertyValue);
		propName.setVisibility(View.VISIBLE);
		propValue.setVisibility(View.VISIBLE);

		ImageView img = (ImageView) convertView.findViewById(R.id.up_down);
		img.setVisibility(View.INVISIBLE);

		ImageButton fbButton = (ImageButton) convertView.findViewById(R.id.fbShareButton);
		fbButton.setVisibility(View.GONE);
		/*View emptyView = (View)convertView.findViewById(R.id.empty);
		emptyView.setVisibility(View.GONE);*/
		// Display Table Header and Home Link
		if(row.getKey() != null && row.getKey() != "")
		{
			if(row.getKey().equals(Constants.TABLE_HEADER_KEY))
			{
				propName.setText(Constants.TABLE_HEADER);
				fbButton.setVisibility(View.VISIBLE);
				initFacebookButton(fbButton);
				propValue.setText("");
				propValue.setVisibility(View.GONE);
				//propValue.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.1f));
			}
			else if(row.getKey().equals(Constants.ZILLOW_LINK_KEY))
			{

				propName.setClickable(true);
				propName.setMovementMethod(LinkMovementMethod.getInstance());
				String text = "<a href='"+ row.getPropertyName()+"'>"+ row.getPropertyValue()+"</a>";
				propName.setText(Html.fromHtml(text));
				propValue.setText("");
				propValue.setVisibility(View.GONE);
				//propValue.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.1f));
				//
			}
			else if(row.getPropertyName().equals(PropertyDetailsRowsConstants.DAYSOVERALLCHANGE.getLabel()) ||
					row.getPropertyName().equals(PropertyDetailsRowsConstants.DAYSRENTCHANGE.getLabel()))
			{
				if(row.getKey().indexOf("up_g") != -1)
				{
					img.setImageDrawable(myActivity.getResources().getDrawable(R.drawable.up_g));
					img.setVisibility(View.VISIBLE);
				}
				else
				{
					img.setImageDrawable(myActivity.getResources().getDrawable(R.drawable.down_r));
					img.setVisibility(View.VISIBLE);
				}
				//emptyView.setVisibility(View.INVISIBLE);
				propName.setText(row.getPropertyName());
				propValue.setText(row.getPropertyValue());
				//propValue.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
			}
		}
		else
		{
			// Populate the data into the template view using the data object
			propName.setText(row.getPropertyName());
			propValue.setText(row.getPropertyValue());
			//propValue.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0.8f));
		}
		// Return the completed view to render on screen
		return convertView;
	}

	private void initFacebookButton(ImageButton fbShareButton) {
		fbShareButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						myActivity);
				alertDialogBuilder
				.setMessage("Post to Facebook?")
				.setCancelable(false)
				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						shareDialog = null;
						openFacebookSession();
						// if this button is clicked, close
						// current activity
						//MainActivity.this.finish();
					}
				})
				.setNegativeButton("No",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						Toast toast = Toast.makeText(myActivity, "Post Cancelled", Toast.LENGTH_SHORT);
						toast.show();
						dialog.cancel();
					}
				});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();			
				// openFacebookSession();
			}
		});
	}


	private void openFacebookSession()
	{
		openActiveSession(myActivity, true, Arrays.asList("email","basic_info","user_birthday", "user_hometown", "user_location"), new Session.StatusCallback()
		{

			@Override
			public void call(Session session, SessionState state,
					Exception exception) {
				if(shareDialog == null)
				{
					shareDialog = new FacebookDialog.ShareDialogBuilder(myActivity)
					.setLink(homeLink.getPropertyName())
					.setCaption("Property Informatio from Zillow.com")
					.setPicture(imageUrl)
					.setName(homeLink.getPropertyValue()).setDescription("Last Sold Price: "+ lastSoldPrice + ", Overall Change Price:" + overallChange)
					.build();

					uiHelper.trackPendingDialogCall(shareDialog.present());
				}
			}

		});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Session openActiveSession(Activity activity, boolean allowLoginUI, List permissions, StatusCallback callback) { 
		OpenRequest openRequest = new OpenRequest(activity).setPermissions(permissions).setCallback(callback);
		Session session = new Session.Builder(activity).build();
		if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState()) || allowLoginUI) {
			Session.setActiveSession(session);
			session.openForRead(openRequest);
			return session;
		}
		return null;
	}
}
