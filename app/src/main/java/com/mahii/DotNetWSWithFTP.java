package com.teezom.ws;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.teezom.ftp_work.FtpInterface;
import com.teezom.ftp_work.MyFTPClientFunctions;
import com.teezom.global.AppConstant;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class DotNetWSWithFTP extends AsyncTask<String, String, String> {

    Context context;
    FtpInterface asyncInterface;
    ProgressDialog pDialog;
    String picPath, newFileName, dirPath, wsType;
    private MyFTPClientFunctions ftpclient = null;
    SoapObject request;
    String action;

    public DotNetWSWithFTP(Context context, String picPath, String newFileName, String dirPath, String wsType, SoapObject request, String action) {
        this.context = context;
        this.picPath = picPath;
        this.newFileName = newFileName;
        this.dirPath = dirPath;
        this.wsType = wsType;
        this.request = request;
        this.action = action;
        this.asyncInterface = (FtpInterface) context;
        ftpclient = new MyFTPClientFunctions();
    }

    public DotNetWSWithFTP(Context context, Fragment fragment, String picPath, String newFileName, String dirPath, String wsType, SoapObject request, String action) {
        this.context = context;
        this.picPath = picPath;
        this.newFileName = newFileName;
        this.dirPath = dirPath;
        this.wsType = wsType;
        this.request = request;
        this.action = action;
        this.asyncInterface = (FtpInterface) fragment;
        ftpclient = new MyFTPClientFunctions();
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
    protected String doInBackground(String... params) {

        final String host = "182.50.132.57";
        final String username = "KirtiPradip";
        final String password = "kp1029384756";

        boolean status;
        status = ftpclient.ftpConnect(host, username, password, 21);
        if (status) {
            Log.e("Tag", "Connection Success");
        } else {
            Log.e("Tag", "Connection failed");
        }

        boolean status2;
        status2 = ftpclient.ftpUpload(picPath, newFileName, dirPath, context);

        if (status2) {
            Log.e("Tag", "Upload success");
        } else {
            Log.e("Tag", "Upload failed");
        }

        final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        new MarshalBase64().register(envelope); // serialization

        HttpTransportSE androidHttpTransport = new HttpTransportSE(AppConstant.BASE_URL);
        try {
            androidHttpTransport.call(AppConstant.NAMESPACE + action, envelope);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        Object result = null;
        try {
            result = envelope.getResponse();
        } catch (SoapFault soapFault) {
            soapFault.printStackTrace();
        }

        return result.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        pDialog.dismiss();
        try {
            JSONObject jsonObject = new JSONObject(s);
            String status = jsonObject.getString("status");
            if(status.equals("1")){
                asyncInterface.onFTPResponse("done", wsType, newFileName);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
