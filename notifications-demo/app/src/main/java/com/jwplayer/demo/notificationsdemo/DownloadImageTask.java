package com.jwplayer.demo.notificationsdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Downloads images and returns them as bitmap.
 *
 * Clients need to extend it and implement their own onPostExecute method.
 */
public abstract class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

	@Override
	protected Bitmap doInBackground(String... urls) {
		if (urls.length != 1 || urls[0] == null) {
			return null;
		}

		Bitmap bitmap = null;
		URL url;
		try {
			url = new URL(urls[0]);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
		HttpURLConnection urlConnection = null;
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
			if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
				bitmap = BitmapFactory.decodeStream(stream);
			}
		} catch (IOException e) {
			/* ignore */
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
		return bitmap;
	}

}
