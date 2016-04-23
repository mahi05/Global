package com.mahii.WS;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.shanks.easygov.interfaces.AsyncInterface;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class WSHttpGet extends AsyncTask<String, Void, String> {
	ProgressDialog pDialog;
	Context context;
	HttpURLConnection httpConnection;
	AsyncInterface asyncInterface;
	String WSType;

	public WSHttpGet(Context context, String WSType) {
		this.context = context;
		this.WSType = WSType;
		this.asyncInterface = (AsyncInterface) context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		pDialog = new ProgressDialog(context);
		pDialog.setTitle("Connecting...");
		pDialog.setMessage("Please Wait...");
		pDialog.setCancelable(false);
		pDialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		String result = "";
		try {
			URL url = new URL(params[0]);
			httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestProperty("Accept", "application/json");
			httpConnection.setReadTimeout(15000);
			httpConnection.setConnectTimeout(15000);
			httpConnection.setRequestMethod("GET");
			httpConnection.setDoInput(true);
			// httpConnection.setDoOutput(true);

			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream iStream = httpConnection.getInputStream();
				InputStreamReader isReader = new InputStreamReader(iStream);
				BufferedReader br = new BufferedReader(isReader);
				String line;
				while ((line = br.readLine()) != null) {
					result += line;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		pDialog.dismiss();
		// Log.e("response", result);
		asyncInterface.onWSResponse(result, WSType);
	}

}
