package com.mahii.WS;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.shanks.easygov.interfaces.AsyncInterface;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class WsHttpPostWithNamePair extends AsyncTask<String, Void, String> {
    ProgressDialog pDialog;
    URL url;
    HttpURLConnection connection = null;
    Context context;
    String WSType;
    Uri.Builder values;
    AsyncInterface asyncInterface;

    public WsHttpPostWithNamePair(Context context, String WSType, Uri.Builder values) {
        this.context = context;
        this.WSType = WSType;
        this.values = values;
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
        try {
            //Create connection
            url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
			
            String query = values.build().getEncodedQuery();

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        pDialog.dismiss();
        try {
            asyncInterface.onWSResponse(s, WSType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
