package webservice;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class NewCallService extends AsyncTask<String, Void, String> {

    OnServiceCall OnServiceCall;
    Context context;
    Uri.Builder values;
    URL url;
    boolean showDialog;
    String dialogMessage;
    HttpURLConnection connection = null;
    ProgressDialog progressDialog;

    public NewCallService(Context context, Uri.Builder values, boolean showDialog, String dialogMessage, OnServiceCall OnServiceCall) {
        this.context = context;
        this.OnServiceCall = OnServiceCall;
        this.values = values;
        this.showDialog = showDialog;
        this.dialogMessage = dialogMessage;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(dialogMessage);
        progressDialog.setCancelable(false);
        if (showDialog) progressDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            /* START Create connection */
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

            /* START Get Response */
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
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (progressDialog.isShowing()) progressDialog.dismiss();
        OnServiceCall.onServiceCall(result);
    }

    public interface OnServiceCall {
        void onServiceCall(String response);
    }

}
