package com.teezom.ws;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.teezom.interfaces.AsyncInterface;
import com.teezom.models.UploadModel;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SendDataAsJson extends AsyncTask<String, Void, String> {

    private Context context;
    private ArrayList<UploadModel> uploadModels;
    private String url;
    private ProgressDialog pDialog;
    private AsyncInterface asyncInterface;
    private String wsType;

    public SendDataAsJson(Context context, ArrayList<UploadModel> uploadModels, String url, String wsType) {
        this.context = context;
        this.uploadModels = uploadModels;
        this.url = url;
        this.asyncInterface = (AsyncInterface) context;
        this.wsType = wsType;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(context);
        pDialog.setTitle("Teezom");
        pDialog.setMessage("Please Wait...");
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected String doInBackground(String... strings) {

        InputStream inputStream;
        String result = "";

        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            String json;

            JSONObject root = new JSONObject();

            for (int i = 0; i < uploadModels.size(); i++) {

                JSONObject bunch = new JSONObject();

                bunch.put("Name", uploadModels.get(i).getName());
                bunch.put("Amount", uploadModels.get(i).getAmount());
                bunch.put("Type", uploadModels.get(i).getType());
                bunch.put("Image", uploadModels.get(i).getImage());

                root.put("" + i + "", bunch);

            }

            json = root.toString();
            Log.e("Data", json);

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();


            // 10. convert input stream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception ex) {
            Log.e("InputStream", ex.getLocalizedMessage());
        }

        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        pDialog.dismiss();
        Log.e("Result", s);
        asyncInterface.onWSResponse(s, wsType);
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

}
