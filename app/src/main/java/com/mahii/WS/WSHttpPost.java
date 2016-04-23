package com.mahii.WS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.shanks.easygov.interfaces.AsyncInterface;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class WSHttpPost extends AsyncTask<String, Void, String> {
    ProgressDialog pDialog;
    Context context;
    HttpURLConnection httpConnection;
    AsyncInterface asyncInterface;
    String WSType;
    ContentValues values;

    public WSHttpPost(Context context, String WSType, ContentValues values) {
        this.context = context;
        this.values = values;
        this.WSType = WSType;
        this.asyncInterface = (AsyncInterface) context;
    }

    public WSHttpPost(Context context, Fragment fragment, String WSType, ContentValues values) {
        this.context = context;
        this.values = values;
        this.WSType = WSType;
        this.asyncInterface = (AsyncInterface) fragment;
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
            //httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConnection.setReadTimeout(10000);
            httpConnection.setConnectTimeout(15000);
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);

            OutputStream os = httpConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(String.valueOf(values));
            writer.flush();
            writer.close();
            os.close();

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
        } catch (java.net.SocketTimeoutException e) {
            Toast.makeText(context, "Network Error : No Data Received.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Error : ", e.toString());
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        pDialog.dismiss();
        try {
            asyncInterface.onWSResponse(result, WSType);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

}
