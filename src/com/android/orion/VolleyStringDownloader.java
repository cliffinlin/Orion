package com.android.orion;

import android.os.AsyncTask;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

public abstract class VolleyStringDownloader {
	public DownloadTask mDownloadTask = null;
	public StringRequest mStringRequest = null;

	abstract void handleResponse(String response);

	public VolleyStringDownloader() {
	}

	public VolleyStringDownloader(String urlString) {
		mStringRequest = new StringRequest(urlString,
				new Response.Listener<String>() {
					@Override
					public void onResponse(final String response) {
						mDownloadTask = new DownloadTask();
						if (mDownloadTask != null) {
							mDownloadTask.execute(response);
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
					}
				});

		mStringRequest.setTag(Constants.TAG);
	}

	class DownloadTask extends AsyncTask<String, Integer, Long> {

		@Override
		protected Long doInBackground(String... params) {
			handleResponse(params[0]);
			return null;
		}
	}
}
