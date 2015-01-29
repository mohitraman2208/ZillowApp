package com.example.zillowapplication;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class LoadImageTask extends AsyncTask<HashMap<Integer,String>, Integer, HashMap<Integer,Bitmap>> 
{
	@SuppressLint("UseSparseArrays") @Override
	protected HashMap<Integer,Bitmap> doInBackground(HashMap<Integer,String>... params) 
	{
		HashMap<Integer, String> yearToUrl = params[0];
		HashMap<Integer,Bitmap> yearToImage = new HashMap<Integer, Bitmap>();
		for(Entry<Integer,String> entry : yearToUrl.entrySet())
		{
			try 
			{
				InputStream in = new URL(entry.getValue()).openStream();
				yearToImage.put(entry.getKey(), Bitmap.createScaledBitmap(BitmapFactory.decodeStream(in),600,350,false));
			}
			catch (MalformedURLException e) 
			{
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return yearToImage;
	}

}
