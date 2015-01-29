package com.example.zillowapplication;
/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

import com.example.android.common.view.SlidingTabLayout;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;

/**
 * A basic sample which shows how to use {@link com.example.android.common.view.SlidingTabLayout}
 * to display a custom {@link ViewPager} title strip which gives continuous feedback to the user
 * when scrolling.
 */
public class SlidingTabsBasicFragment extends Fragment {

	static final String LOG_TAG = "SlidingTabsBasicFragment";

	private UiLifecycleHelper uiHelper;
	/**
	 * A custom {@link ViewPager} title strip which looks much like Tabs present in Android v4.0 and
	 * above, but is designed to give continuous feedback to the user when scrolling.
	 */
	private SlidingTabLayout mSlidingTabLayout;

	private TextSwitcher textSwitcher;
	private ImageSwitcher imageSwitcher;
	int mCounter = 0;

	/**
	 * A {@link ViewPager} which will be used in conjunction with the {@link SlidingTabLayout} above.
	 */
	private ViewPager mViewPager;

	private List<PropertyDetailRow> detailRows =new ArrayList<PropertyDetailRow>();
	protected List<Drawable> chartImages = new ArrayList<Drawable>();
	protected List<String> chartHeaders = new ArrayList<String>();

	private PropertyDetailRow homeLink;
	private String imageUrl;
	private String overallChange;
	private String lastSoldProce;
	/**
	 * Inflates the {@link View} which will be displayed by this {@link Fragment}, from the app's
	 * resources.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		uiHelper = new UiLifecycleHelper(getActivity(),null);
		uiHelper.onCreate(savedInstanceState);
		return inflater.inflate(R.layout.fragment_sample, container, false);
	}

	// BEGIN_INCLUDE (fragment_onviewcreated)
	/**
	 * This is called after the {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has finished.
	 * Here we can pick out the {@link View}s we need to configure from the content view.
	 *
	 * We set the {@link ViewPager}'s adapter to be an instance of {@link SamplePagerAdapter}. The
	 * {@link SlidingTabLayout} is then given the {@link ViewPager} so that it can populate itself.
	 *
	 * @param view View created in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{

		fetchPropertyDetails();

		// Get the ViewPager and set it's PagerAdapter so that it can display items
		mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
		mViewPager.setAdapter(new SamplePagerAdapter(this.detailRows,this.chartImages));

		// Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
		// it's PagerAdapter set.
		mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
		mSlidingTabLayout.setViewPager(mViewPager);
	}

	@SuppressWarnings("unchecked")
	@SuppressLint("UseSparseArrays") private void fetchPropertyDetails() 
	{
		String propertyDetails = getActivity().getIntent().getStringExtra("propertyDetails");
		try
		{
			JSONObject propertyJSON = new JSONObject(propertyDetails);
			HashMap<Integer, String> yearsToUrl = new HashMap<Integer, String>();			
			getDetailsListFromJSON(propertyJSON,this.detailRows,yearsToUrl);
			AsyncTask<HashMap<Integer, String>, Integer, HashMap<Integer, Bitmap>> task = new LoadImageTask().execute(yearsToUrl);
			HashMap<Integer, Bitmap> yearToImageMap = null;
			try 
			{
				yearToImageMap = (HashMap<Integer, Bitmap>) task.get(10000, TimeUnit.MILLISECONDS);

			} 
			catch (InterruptedException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (ExecutionException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (TimeoutException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			passImagesToFragment(yearToImageMap);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void passImagesToFragment(HashMap<Integer, Bitmap> yearToImageMap) 
	{
		for(Entry<Integer,Bitmap> entry :yearToImageMap.entrySet())
		{
			chartHeaders.add(String.format(Constants.CHART_HEADER, entry.getKey()+ (entry.getKey() > 1 ? " years":" year")));
			chartImages.add(new BitmapDrawable(getResources(), entry.getValue()));
		}
		//TODO Handle Timeout
	}

	private void getDetailsListFromJSON(JSONObject propertyJSON,List<PropertyDetailRow> propertyDetails,HashMap<Integer,String> yearToUrl)
	{

		try 
		{
			//Header
			PropertyDetailRow header = new PropertyDetailRow(Constants.TABLE_HEADER_KEY, Constants.TABLE_HEADER);
			header.setKey(Constants.TABLE_HEADER_KEY);
			propertyDetails.add(header);

			//Zillow Link
			homeLink = new PropertyDetailRow(propertyJSON.getString(Constants.ZILLOW_LINK_KEY), propertyJSON.getString(Constants.COMPLETE_ADDR_KEY));
			homeLink.setKey(Constants.ZILLOW_LINK_KEY);
			propertyDetails.add(homeLink);

			this.lastSoldProce = propertyJSON.getString(PropertyDetailsRowsConstants.LAST_SOLD_PRICE.getKey());
			this.overallChange = propertyJSON.getString(Constants.OVERALL_CHANGE_SIGN) + propertyJSON.getString(PropertyDetailsRowsConstants.DAYSOVERALLCHANGE.getKey());
			//All the rows of the table
			for(PropertyDetailsRowsConstants row : PropertyDetailsRowsConstants.values())
			{
				if( row == PropertyDetailsRowsConstants.ZESTIMATEPROPERTYESTIMATE || row == PropertyDetailsRowsConstants.RENTZESTIMATE)
				{
					String dateUpdated = row == PropertyDetailsRowsConstants.ZESTIMATEPROPERTYESTIMATE ? propertyJSON.getString(Constants.ESTIMATE_LAST_UPDATED_KEY) : propertyJSON.getString(Constants.RENT_LAST_UPDATED_KEY);
					propertyDetails.add(new PropertyDetailRow(row.getLabel()+dateUpdated, propertyJSON.getString(row.getKey())));
				} else if(row == PropertyDetailsRowsConstants.DAYSOVERALLCHANGE || row == PropertyDetailsRowsConstants.DAYSRENTCHANGE)
				{
					PropertyDetailRow property = new PropertyDetailRow(row.getLabel(), propertyJSON.getString(row.getKey()));
					property.setKey(propertyJSON.getString(
							row == PropertyDetailsRowsConstants.DAYSOVERALLCHANGE ? 
									Constants.ESTIMATE_CHANGE_IMAGE_KEY : Constants.RENT_CHANGE_IMAGE_KEY ));
					propertyDetails.add(property);
				}
				else
				{
					propertyDetails.add(new PropertyDetailRow(row.getLabel(), propertyJSON.getString(row.getKey())));
				}
			}
			//Charts
			//Store first Chart reference for facebook;
			imageUrl = propertyJSON.getString(Constants.ONE_YEAR_CHART_URL_KEY);

			yearToUrl.put(1,imageUrl);
			yearToUrl.put(5,propertyJSON.getString(Constants.FIVE_YEARS_CHART_URL_KEY));
			yearToUrl.put(10, propertyJSON.getString(Constants.TEN_YEARS_CHART_URL_KEY));

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * The {@link android.support.v4.view.PagerAdapter} used to display pages in this sample.
	 * The individual pages are simple and just display two lines of text. The important section of
	 * this class is the {@link #getPageTitle(int)} method which controls what is displayed in the
	 * {@link SlidingTabLayout}.
	 */
	/*
    class CustomPagerAdapter extends FragmentPagerAdapter {

		public CustomPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			String pageTitle = null;
			switch (position) {
			case 0:
				pageTitle = "Property Details";
				break;
			case 1:
				pageTitle = "Historical Zestimate";
				break;
			default:
				break;
			}
			return pageTitle;
		}

		@Override
		public Fragment getItem(int position)
		{
			Fragment retval = null;
			switch (position) 
			{
			case 0:
				retval = new PropertyDetailsFragment();
				break;
			case 1:
				retval = new HistoricalZestimateFragment();

			default:
				break;
			}
			return retval;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 2;
		}
    }*/

	class SamplePagerAdapter extends PagerAdapter {

		public SamplePagerAdapter(List<PropertyDetailRow> detailRows,
				List<Drawable> chartImages) {
			super();
		}

		/**
		 * @return the number of pages to display
		 */
		@Override
		public int getCount() {
			return 2;
		}

		/**
		 * @return true if the value returned from {@link #instantiateItem(ViewGroup, int)} is the
		 * same object as the {@link View} added to the {@link ViewPager}.
		 */
		@Override
		public boolean isViewFromObject(View view, Object o) {
			return o == view;
		}

		// BEGIN_INCLUDE (pageradapter_getpagetitle)
		/**
		 * Return the title of the item at {@code position}. This is important as what this method
		 * returns is what is displayed in the {@link SlidingTabLayout}.
		 * <p>
		 * Here we construct one using the position value, but for real application the title should
		 * refer to the item's contents.
		 */
		@Override
		public CharSequence getPageTitle(int position) {
			String pageTitle = null;
			switch (position) {
			case 0:
				pageTitle = "Property Details";
				break;
			case 1:
				pageTitle = "Historical Zestimate";
				break;
			default:
				break;
			}
			return pageTitle;
		}
		// END_INCLUDE (pageradapter_getpagetitle)

		/**
		 * Instantiate the {@link View} which should be displayed at {@code position}. Here we
		 * inflate a layout from the apps resources and then change the text view to signify the position.
		 */
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// Inflate a new layout from our resources
			View view = null;
			try
			{

				switch (position) 
				{
				case 0:
					view = getActivity().getLayoutInflater().inflate(R.layout.property_details,
							container, false);
					showPropertyDetails(view);
					addFooterLink(view);
					break;
				case 1:
					view = getActivity().getLayoutInflater().inflate(R.layout.property_charts,
							container, false);
					showChartScreen(view);
					addFooterLink(view);
					break;
				default:
					break;
				}
				// Add the newly created View to the ViewPager
				container.addView(view);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			return view;
		}

		private void addFooterLink(View view) {
			try
			{
				TextView secondLine = (TextView) view.findViewById(R.id.zillowCopyrightSecondLine);

				secondLine.setClickable(true);
				secondLine.setMovementMethod(LinkMovementMethod.getInstance());
				secondLine.setText(Html.fromHtml(getResources().getString(R.string.zillowCopyrightSecondLine)));

				TextView thirdLine = (TextView) view.findViewById(R.id.zillowCopyrightThirdLine);

				thirdLine.setClickable(true);
				thirdLine.setMovementMethod(LinkMovementMethod.getInstance());
				thirdLine.setText(Html.fromHtml(getResources().getString(R.string.zillowCopyrightThirdLine)));
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		private void showChartScreen(View view) 
		{
			initTextSwitcher(view);
			initImageSwitcher(view);

			//Complete Address
			TextView completeAddrView = (TextView)view.findViewById(R.id.completeAddr);
			completeAddrView.setText(homeLink.getPropertyValue());

			Button nextButton = (Button) view.findViewById(R.id.buttonNext);
			nextButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mCounter = ++mCounter > 2 ?  0: mCounter;
					textSwitcher.setText(chartHeaders.get(mCounter));
					imageSwitcher.setImageDrawable(chartImages.get(mCounter));
				}
			});

			Button prevButton = (Button) view.findViewById(R.id.buttonPrevious);
			prevButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mCounter = --mCounter < 0 ? 2 : mCounter;
					textSwitcher.setText(chartHeaders.get(mCounter));
					imageSwitcher.setImageDrawable(chartImages.get(mCounter));
				}
			});


			// Set the initial text without an animation
			textSwitcher.setCurrentText(chartHeaders.get(mCounter));
			// Set the initial Image without an animation
			imageSwitcher.setImageDrawable(chartImages.get(mCounter));

		}

		private void initImageSwitcher(View view) {
			imageSwitcher = (ImageSwitcher)view.findViewById(R.id.imageSwitcher1);
			imageSwitcher.setFactory(new ViewFactory() {

				@Override
				public View makeView() {

					// Create a new TextView
					ImageView myView = new ImageView(getActivity());
					myView.setAdjustViewBounds(true);
					return myView;
				}
			});


			/*
			 * Set the in and out animations. Using the fade_in/out animations
			 * provided by the framework.
			 */
			Animation in = AnimationUtils.loadAnimation(getActivity(),
					android.R.anim.fade_in);
			Animation out = AnimationUtils.loadAnimation(getActivity(),
					android.R.anim.fade_out);
			imageSwitcher.setInAnimation(in);
			imageSwitcher.setOutAnimation(out);

		}

		void initTextSwitcher(View view)
		{
			textSwitcher = (TextSwitcher) view.findViewById(R.id.switcher);
			textSwitcher.setFactory(new ViewFactory() {

				@Override
				public View makeView() {

					// Create a new TextView
					TextView t = new TextView(getActivity());
					t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
					t.setTextSize(18);
					t.setTypeface(null,Typeface.BOLD);
					return t;
				}
			});
			/*
			 * Set the in and out animations. Using the fade_in/out animations
			 * provided by the framework.
			 */
			Animation in = AnimationUtils.loadAnimation(getActivity(),
					android.R.anim.fade_in);
			Animation out = AnimationUtils.loadAnimation(getActivity(),
					android.R.anim.fade_out);
			textSwitcher.setInAnimation(in);
			textSwitcher.setOutAnimation(out);
		}

		/**
		 * Destroy the item from the {@link ViewPager}. In our case this is simply removing the
		 * {@link View}.
		 */
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		public void showPropertyDetails(View view) 
		{
			PropertyDetailAdapter adapter = new PropertyDetailAdapter(getActivity(), detailRows,uiHelper,homeLink,imageUrl,lastSoldProce,overallChange);
			ListView listView = (ListView) view.findViewById(R.id.propertyDetailsTab);
			listView.setAdapter(adapter);
			//initFacebookButton(view);
		}
	}

	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (state.isOpened()) {
			Log.i(LOG_TAG, "Logged in...");
		} else if (state.isClosed()) {
			Log.i(LOG_TAG, "Logged out...");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Session session = Session.getActiveSession();
		if (session != null &&
				(session.isOpened() || session.isClosed()) ) {
			onSessionStateChange(session, session.getState(), null);
		}



		uiHelper.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try
		{
			Session.getActiveSession().onActivityResult(this.getActivity(), requestCode, resultCode, data);
			uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() 
			{


				@Override
				public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
					Log.e("Activity", String.format("Error: %s", error.toString()));
				}

				@Override
				public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {

					String postid=FacebookDialog.getNativeDialogCompletionGesture(data);
					if(postid != null && !postid.equals("post"))
					{	
						Toast toast = Toast.makeText(getActivity(), "Post Cancelled", Toast.LENGTH_SHORT);
						toast.show();

						/*Random r = new Random();
					long n = 100000 + Math.abs(r.nextLong()*99999);
					long n1 = 100000 + Math.abs(r.nextLong() * 99999);
					String nL="86709"+Long.toString(n1)+"6079";
					String np="100001"+Long.toString(n)+"035";
					//100001035
					String postId = "Your post id is "+np+"_"+nL;// 100001035901206_384361345941672";//+FacebookDialog.getNativeDialogPostId(data);
					Toast toast = Toast.makeText(getActivity(), postId, Toast.LENGTH_SHORT);
					toast.show();*/
					}
					Log.i("Activity", "Success!");
				}
			});
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}